package Engine;

import java.util.ArrayList;

import com.Reader;

import kdu_jni.Jpx_codestream_source;
import kdu_jni.Jpx_frame;
import kdu_jni.Jpx_frame_expander;
import kdu_jni.Jpx_source;
import kdu_jni.KduException;
import kdu_jni.Kdu_codestream;
import kdu_jni.Kdu_compositor_buf;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_dims;
import kdu_jni.Kdu_ilayer_ref;
import kdu_jni.Kdu_istream_ref;
import kdu_jni.Kdu_region_compositor;

/**
 * Compositor for JPX image data
 * 
 * @author dmccarthy
 *
 */
public class Compositor implements Runnable {

	private Kdu_region_compositor compositor;
	private Jpx_source jpx_src;
	private Enviroment enviro;
	private Kdu_coords display_size;
	private Kdu_coords view_size;
	private Kdu_dims total_dims;
	private ImagePanel display;
	private Thread myThread;
	private Reader inputReader;
	private Kdu_istream_ref current_istream;
	private ArrayList<Kdu_ilayer_ref> iLayers;

	/**
	 * 
	 * Takes the input from the JPX source and processes it 
	 * then outputs it to the ImagePanel that is being used to display the image
	 *
	 * @param input_src
	 * @param display
	 * @throws KduException
	 * 
	 * 
	 */
	public Compositor(Jpx_source input_src,ImagePanel display) throws KduException
	{
		this.display = display;
		jpx_src = input_src;
		//Create new environment for compositor
		enviro = new Enviroment();
		compositor = new Kdu_region_compositor(enviro.getEnv(),enviro.getQueue());
		compositor.Create(jpx_src);

	}
	@Override
	public void run() {

		while(!inputReader.finished())
		{
			System.out.println("waiting on reader");
		}
		try {

			//Determine if the view size is greater then the display size
			if(view_size.Get_x() > display_size.Get_x())
				view_size.Set_x(display_size.Get_x());
			if(view_size.Get_y() > display_size.Get_y())
				view_size.Set_y(display_size.Get_y());
			//Set the surface buffer
			compositor.Set_buffer_surface(total_dims);
			//Create compositor buffer
			Kdu_compositor_buf compositor_buf = compositor.Get_composition_buffer(total_dims);

			//initilize buffer
			int region_buf_size = 0;
			int[] region_buf = null;
			//initialize new region object
			Kdu_dims new_region = new Kdu_dims();


			//Process data
			while(compositor.Process(100000, new_region))
			{
				//Determine offset
				Kdu_coords new_offset = new_region.Access_pos();
				Kdu_coords new_size = new_region.Access_size();
				new_offset.Subtract(total_dims.Access_pos());

				//Determine number of pixels
				int new_pixels = new_size.Get_x() * new_size.Get_y();

				if(new_pixels == 0)continue;
				//Create buffer
				if(new_pixels > region_buf_size)
				{
					region_buf_size = new_pixels;
					region_buf = new int[region_buf_size];
				}
				//Assign compositor buffer
				compositor_buf.Get_region(new_region, region_buf);
				//Output to ImagePanel display
				display.put_region(view_size.Get_x(), view_size.Get_y(), new_size.Get_x(), new_size.Get_y(), new_offset.Get_x(), new_offset.Get_y(), region_buf);

			}

			boolean cdx_comp = compositor.Is_codestream_processing_complete();
			boolean proc_comp = compositor.Is_processing_complete();
			System.out.println("Processing Status: " + proc_comp);
			System.out.println("Codestream Status: " + cdx_comp);


		} catch (KduException e) {
			e.printStackTrace();
		}
	}
	public void start(Reader reader)
	{
		inputReader = reader;
		try {
			//Get size of display
			display_size = new Kdu_coords(display.getWidth(),display.getHeight());
			//Wait for stream headers
			System.out.println("Waiting on stream headers? " + compositor.Waiting_for_stream_headers());
			while(compositor.Waiting_for_stream_headers())
			{
				System.out.println("Wating for stream headers");
			}
			while(!inputReader.finished())
			{
				System.out.println("waiting on reader");
				if(!inputReader.finished())
				{
					compositor.Refresh();
				}
			}
			//Access Codestream
			Jpx_codestream_source jpx_code = jpx_src.Access_codestream(0);
			//Get size of source image
			Kdu_coords jpx_size = jpx_code.Access_dimensions().Get_size();

			//Create source image Dimensions 
			Kdu_dims full_source_dims = new Kdu_dims();
			full_source_dims.Access_size().Assign(jpx_size);
			System.out.println("Source size: " + jpx_size.Get_x() + " " + jpx_size.Get_y());
			full_source_dims.Access_pos().Set_x(0);
			full_source_dims.Access_pos().Set_y(0);

			//display_size.Set_x(jpx_size.Get_x());
			//display_size.Set_y(jpx_size.Get_y());
			
			//Create target image Dimensions
			Kdu_dims full_target_dims = new Kdu_dims();
			full_target_dims.Access_size().Assign(display_size);
			System.out.println("Display size: " + display_size.Get_x() + " " + display_size.Get_y());
			full_target_dims.Access_pos().Set_x(0);
			full_target_dims.Access_pos().Set_y(0);

			//Get buffer
			compositor.Get_composition_buffer(full_target_dims);
			//Set buffer surface
			compositor.Set_buffer_surface(full_source_dims);			
			
			//Get Ilayer
			Kdu_ilayer_ref ilayer_ref = compositor.Add_ilayer(0, full_source_dims,full_target_dims);
			
			//Get next imagery stream
			Kdu_istream_ref istream_ref = compositor.Get_next_istream(new Kdu_istream_ref(),false,false,0);
			current_istream = istream_ref;
			
			//Check max layers
			int max_layers = compositor.Get_max_available_quality_layers();
			System.out.println("Max Layers: "+max_layers);
			
			//Get info from ilayer
			int[] layer_src = {0};
			int[] direct_codestream_idx = {0};
			boolean[] is_opaque = {true};
			int num_streams = compositor.Get_ilayer_info(ilayer_ref, layer_src, direct_codestream_idx, is_opaque);
			
			System.out.println("Layer number: " + layer_src[0]);
			System.out.println("Codestream id: " + direct_codestream_idx[0]);
			System.out.println("Is opaque?: " + is_opaque[0]);
			System.out.println("Number of streams: " + num_streams);
			
			//Access imagery stream and determine if it exsists
			Kdu_codestream codestream = compositor.Access_codestream(istream_ref);
			boolean exsists = codestream.Exists();
			System.out.println("Codestream exsists?" + exsists);

			//Output number of imagery layers
			int num_layer = compositor.Get_num_ilayers();
			System.out.println("number of layers: " + num_layer);

			//Set Scale
			float scale_anchor = 1;
			float min_scale = 0.2f;
			float max_scale = 10;
			float scale = compositor.Find_optimal_scale(full_target_dims, scale_anchor, min_scale, max_scale, istream_ref);
			System.out.println("Scale Anchor: " + scale_anchor);
			System.out.println("Min scale: " + min_scale);
			System.out.println("Max Scale: " + max_scale);
			System.out.println("Optimal Scale: " + scale);

			compositor.Set_scale(false, false, false, 0.5f);
			//Check for Scale errors
			int scale_code = compositor.Check_invalid_scale_code();
			System.out.println("Scale Code = " + scale_code);

			//Get total dimensions of data
			total_dims = new Kdu_dims();
			boolean check = compositor.Get_total_composition_dims(total_dims);
			System.out.println("Total Dimensions correct? " + check);
			System.out.println("Total Dims size: " + total_dims.Access_size().Get_x() + " " + total_dims.Access_size().Get_y());
			System.out.println("Total Dims pos: " + total_dims.Access_pos().Get_x() + " " + total_dims.Access_pos().Get_y());

			view_size = total_dims.Access_size();

		} catch (KduException e) {
			e.printStackTrace();
		}
		//Run thread
		myThread = new Thread(this);
		myThread.run();
	}
	public void stop()
	{
		try {
			//Halt processing
			compositor.Halt_processing();
			myThread.join();
			myThread = null;
		} catch (KduException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public ImagePanel updateDisplay()
	{
		return display;
	}

}
