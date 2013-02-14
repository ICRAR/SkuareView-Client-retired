package j2k;

import UI.SkuareViewClient;
import kdu_jni.*;

public class ImageInput {

	private int discardLevels;
	private int maxDiscardLevels;      
	private int referenceComponent;    
	private int maxComponents;         
	private int imageRealWidth;        
	private int imageRealHeight;       
	private int imageWidth;            
	private int imageHeight;           
	private int numLayers;             
	@SuppressWarnings("unused")
	private int maxNumLayers;          
	private String imageName;

	private Kdu_dims varDim;
	private Kdu_coords minExpansion;
	private Kdu_coords expansion;
	private Kdu_compressed_source input;
	private Kdu_codestream codestream;
	private Kdu_channel_mapping channels;

	private Kdu_simple_file_source fileIn;
	private Jp2_family_src jp2FamilyIn;
	private Jp2_source jp2In;
	private Jp2_locator jp2Loc;

	private Reader reader;
	private Mutex codestreamMutex;
	private ImageView actualView;
	private Render render;

	/**
	 * The ImageInput class defines the input source for an image,
	 * it handles the management of the communication, cache and rendering
	 * of the image.
	 */
	public ImageInput()
	{
		//Initialize Variables
		input = null;

		codestreamMutex = new Mutex();
		minExpansion = new Kdu_coords();
		expansion = new Kdu_coords();
		varDim = new Kdu_dims();

		codestream = new Kdu_codestream();
		channels = new Kdu_channel_mapping();

		fileIn = new Kdu_simple_file_source();
		jp2FamilyIn = new Jp2_family_src();
		jp2Loc = new Jp2_locator();
		jp2In = new Jp2_source();

		reader = null;
		actualView = null;
		render = new Render(this);

	}
	/**
	 * Lock the code stream
	 */
	public void lockCodeStream()
	{
		codestreamMutex.lock();
	}
	/**
	 * Unlock the code stream
	 */
	public void unlockCodeStream()
	{
		codestreamMutex.unlock();
	}
	/**
	 * Check if there is an input
	 * 
	 * @return boolean
	 */
	public boolean isOpened()
	{
		return (input != null);
	}
	/**
	 * Get instance of the JPIP reader
	 * 
	 * @return JPIP Reader
	 */
	public Reader getReader()
	{
		return reader;
	}
/**
 * Open a new image
 * The path name is passed to the JPIP Reader then the Reader is used to create
 * the codestream. The codestream is then configured and the base values for the dimensions
 * are set. Further information about the codestream is printed to the programs console.
 * 
 * @param fname Path to image, usually in the format JPIP://server.com/imagename.jpx
 * @throws Exception
 */
	public void open(String fname) throws Exception
	{
		//check for empty input
		if(input != null) close();

		imageName = fname;
		//Force input string to capitals
		String upper = fname.toUpperCase();

		//Check input type
		try {
			if(upper.startsWith("JPIP://")) {
				reader = new Reader(this);
				input = reader;

			} else if(upper.endsWith("JP2") || upper.endsWith("JPX")) {
				jp2FamilyIn.Open(fname, true);
				jp2In.Open(jp2FamilyIn, jp2Loc);
				jp2In.Read_header();
				input = jp2In;

			} else {
				fileIn.Open(fname, true);
				input = fileIn;
			}
			//Create codestream from input
			codestream.Create(input);
			
			if(jp2In.Exists()) channels.Configure(jp2In, false);
			else channels.Configure(codestream);

			//Determine details of codestream
			discardLevels = 0;
			//Get levels of Quality
			maxDiscardLevels = codestream.Get_min_dwt_levels();
			
			referenceComponent = channels.Get_source_component(0);
			determineReferenceExpansion();
			//Get dimensions of image
			codestream.Get_dims(referenceComponent, varDim);
			imageWidth = imageRealWidth = varDim.Access_size().Get_x();
			imageHeight = imageRealHeight = varDim.Access_size().Get_y();
			
			//Set codestream as persistant
			codestream.Set_persistent();
			
			//Recheck levels of quality
			maxDiscardLevels = codestream.Get_min_dwt_levels();
			
			//Find tile 
			Kdu_coords coord = new Kdu_coords();
			codestream.Find_tile(referenceComponent, minExpansion, coord);
			
			//Open tile
			codestream.Create_tile(coord);
			
			//Get max layers, number of components and quality levels
			numLayers = maxNumLayers = codestream.Get_max_tile_layers();
			SkuareViewClient.console.println("Number of Layers: " + numLayers);
			int num_comps = codestream.Get_num_components();
			SkuareViewClient.console.println("Number of Components: " + num_comps);
			SkuareViewClient.console.println("Levels of Quality: " + maxDiscardLevels);

		} catch(KduException ex) {
			reader = null;
			input = null;

			throw new Exception("Internal Kakadu exception (" + ex.getMessage() + ")");	
		}

		if(reader != null) reader.init();
	}
	/**
	 * Return the ImageView instance associated with this object
	 * @return ImageView - ImageView oject
	 */
	public ImageView getActualView()
	  {
	    return actualView;
	  }
	/**
	 * Determine the size of the expansions for the image
	 * 
	 * @throws KduException
	 */
	private void determineReferenceExpansion() throws KduException
	{
		maxComponents = 0;

		Kdu_coords ref_subs = new Kdu_coords();
		Kdu_coords subs = new Kdu_coords();
		codestream.Get_subsampling(referenceComponent, ref_subs);
		Kdu_coords min_subs = new Kdu_coords(); min_subs.Assign(ref_subs);

		for(int c = 0; c < channels.Get_num_channels(); c++)
		{
			codestream.Get_subsampling(channels.Get_source_component(c),subs);
			if(subs.Get_x() < min_subs.Get_x()) min_subs.Set_x(subs.Get_x());
			if (subs.Get_y() < min_subs.Get_y()) min_subs.Set_y(subs.Get_y());
		}

		minExpansion.Set_x(ref_subs.Get_x() / min_subs.Get_x());
		minExpansion.Set_y(ref_subs.Get_y() / min_subs.Get_y());

		for(int c = 0; c < channels.Get_num_channels(); c++)
		{
			codestream.Get_subsampling(channels.Get_source_component(c), subs);
			if((((subs.Get_x() * minExpansion.Get_x()) % ref_subs.Get_x()) != 0) ||
					(((subs.Get_y() * minExpansion.Get_y()) % ref_subs.Get_y()) != 0))
			{
				maxComponents = 1;
				codestream.Apply_input_restrictions(0, maxComponents, 0, 0, null,
						Kdu_global.KDU_WANT_CODESTREAM_COMPONENTS);

				channels.Configure(codestream);

				minExpansion.Set_x(1);
				minExpansion.Set_y(1);
			}
		}

		expansion.Assign(minExpansion);
	}
	/**
	 * Stop the renderer from decoding the image
	 */
	public synchronized void stopDecoding()
	  {
	    if(reader != null) reader.stop();
	    render.stop();

	    actualView = null;
	  }
	/**
	 * Start decoding the image
	 * 
	 * @param newView ImageView that defines the current ROI that is being rendered
	 */
	public synchronized void startDecoding(ImageView newView)
	  {
		//Check to see if decoding is required
	    if(newView.isCompleted()) return;
	    if(actualView == newView) return;

	    actualView = newView;
	    //Get resolution of view
	    discardLevels = newView.getResolution();

	    //Set up codestream
	    try {
	      codestream.Apply_input_restrictions(0, maxComponents, discardLevels, 0, null,
					Kdu_global.KDU_WANT_CODESTREAM_COMPONENTS);
	      codestream.Augment_cache_threshold(1*1024*1024);
				
	      codestream.Get_dims(referenceComponent, varDim);

	      imageWidth = varDim.Access_size().Get_x();
	      imageHeight = varDim.Access_size().Get_y();

	      Kdu_dims region = new Kdu_dims();
	      Kdu_dims realRegion = new Kdu_dims();

	      //Determine region of interest
	      region.Access_pos().Set_x(newView.getX());
	      region.Access_pos().Set_y(newView.getY());
	      region.Access_size().Set_x(newView.getWidth());
	      region.Access_size().Set_y(newView.getHeight());

	      codestream.Map_region(0, region, realRegion);
	      codestream.Apply_input_restrictions(0, maxComponents, discardLevels, 0, realRegion, 
					Kdu_global.KDU_WANT_CODESTREAM_COMPONENTS);
	      codestream.Augment_cache_threshold(1*1024*1024);

	    } catch(KduException ex) { 
				System.err.println("Internal Kakadu error:");
				ex.printStackTrace();
			}

	    //Start Render then reader
	    render.start();
	    if(reader != null) reader.start();
	  }
/**
 * Close input and clean up
 * @throws Exception
 */
	public void close() throws Exception
	{
		stopDecoding();

		input = null;
		reader = null;

		try {
			if(codestream.Exists()) codestream.Destroy();
			if(fileIn.Exists()) fileIn.Close();
			if(jp2FamilyIn.Exists()) jp2FamilyIn.Close();
			if(jp2In.Exists()) jp2In.Close();
			channels.Clear();

		} catch(KduException ex) {
			throw new Exception("Internal Kakadu exception (" + ex.getMessage() + ")");	
		}
	}
	/**
	 * Return the name of the image
	 * @return String imageName
	 */
	public String getImageName()
	{
		return imageName;
	}
	/**
	 * Checks to see if the image is remotely accessed
	 * @return Check if reader is null
	 */
	public boolean isRemote()
	{
		return(reader != null);
	}
	/**
	 * Get the Real width of the image
	 * @return width of image
	 */
	public int getRealWidth() 
	{ 
		return imageRealWidth; 
	}
	/**
	 * Get the Real Height of the image
	 * @return Height of image
	 */
	public int getRealHeight() 
	{ 
		return imageRealHeight; 
	}
/**
 *  Get the width of the displayed image
 * @return width of image
 */
	public int getWidth() 
	{ 
		return imageWidth; 
	}
/**
 *  Get the Height of the displayed image
 * @return height of image
 */
	public int getHeight() 
	{ 
		return imageHeight; 
	}
	
	public Kdu_codestream getCodestream() { return codestream; }
	public Kdu_channel_mapping getChannels() { return channels; }
	public int getDiscardLevels() { return discardLevels; }
	public int getMaxDiscardLevels() { return maxDiscardLevels; }
	public int getReferenceComponent() { return referenceComponent; }
	public Kdu_coords getExpansion() { return expansion; }
	public int getNumLayers() { return numLayers; }

/**
 * Create a view based on this image
 * @return	ImageView object
 */
	public ImageView createView()
	{
		if(input == null) return null;
		return new ImageView(this, null, 0);
	}
	/**
	 * Create a view based on given width and height limits
	 * 
	 * @param limitWidth
	 * @param limitHeight
	 * @return ImageView
	 */
	public ImageView createView(int limitWidth, int limitHeight)
	{
		if(input == null) return null;

		int res = 0;
		int or_width = imageRealWidth;
		int or_height = imageRealHeight;
		while(or_width > limitWidth || or_height > limitHeight) {
			or_width >>= 1;
			or_height >>= 1;
			res++;
		}

		return new ImageView(this, null, res);
	}
}
