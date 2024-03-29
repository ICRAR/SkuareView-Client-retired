package Engine;

import com.Reader;

import kdu_jni.Jp2_family_src;
import kdu_jni.Jpx_source;
import kdu_jni.KduException;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_simple_file_source;

/**
 * This class manages the Compressor
 * 
 * @author Dylan McCarthy
 * @since 14/02/2013
 *
 */
public class Renderer {

	@SuppressWarnings("unused")
	private Kdu_simple_file_source raw_src;
	private Jp2_family_src family_src;
	private Jpx_source wrapped_src;
	private Reader reader;
	public ImagePanel display;
	private Compositor comp;

	/**
	 * Open file from path
	 * 
	 * @param path path to JPIP server
	 * @throws KduException
	 */
	public void OpenFile(String path) throws KduException
	{
		if(path.contains("jpip://"))
		{
			try {
				family_src = new Jp2_family_src();
				wrapped_src = new Jpx_source();
				
				//reader = new Reader(path);
				
				start_display(new Kdu_coords(800,600));
				reader.getData(0,0,display.getWidth(),display.getHeight(),1);
				
				
				family_src.Open(reader);
				wrapped_src.Open(family_src, true);
				
				comp = new Compositor(wrapped_src,display);
				comp.start(reader);
				
				update_display();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * Create the display based off of a Kdu_coords object
	 * @param view_size Kdu_coords ojbect
	 * @throws KduException
	 */
	private void start_display(Kdu_coords view_size) throws KduException
	{
		display = new ImagePanel(view_size);
		display.setVisible(true);
	}
	/**
	 * Update the display and repaint
	 * @throws KduException
	 */
	private void update_display() throws KduException
	{
		display = comp.updateDisplay();
		display.repaint();
	}
	/**
	 * Create new instance of the renderer and open the path
	 * 
	 * @param path Path to JPIP server
	 */
	public Renderer(String path)
	{
		try{
			OpenFile(path);
		}
		catch(KduException e)
		{
			System.out.println(e.Get_kdu_exception_code());
		}
	}
	/**
	 * Get the ImagePanel created from this renderer
	 * @return ImagePanel with current image data
	 */
	public ImagePanel getImagePanel()
	{
		return display;
	}
}
