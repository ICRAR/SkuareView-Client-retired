package Engine;

import java.awt.Rectangle;

import com.Mutex;
import com.Reader;

import kdu_jni.Jp2_family_src;
import kdu_jni.Jp2_locator;
import kdu_jni.Jp2_source;
import kdu_jni.Jpx_source;
import kdu_jni.KduException;
import kdu_jni.Kdu_compressed_source;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_simple_file_source;

public class ImageInput {

	private Kdu_compressed_source input;
	private Mutex codestreamMutex;
	
	@SuppressWarnings("unused")
	private Kdu_simple_file_source fileIn;
	private Jp2_family_src jp2_family;
	@SuppressWarnings("unused")
	private Jp2_source jp2_src;
	@SuppressWarnings("unused")
	private Jp2_locator jp2_loc;
	private Jpx_source jpx_src;
	
	private Reader reader;
	private Compositor compositor;
	
	private ImageView actualView;
	
	private String inputName;
	
	private ImagePanel display;
	
	public ImageInput()
	{
		input = null;
		codestreamMutex = new Mutex();
		
		jp2_family = new Jp2_family_src();
		jp2_loc = new Jp2_locator();
		jp2_src = new Jp2_source();
		jpx_src = new Jpx_source();
		
		reader = null;
		actualView = null;
		
		
		
	}
	public void lockCodeStream()
	{
		codestreamMutex.lock();
	}
	public void unlockCodeStream()
	{
		codestreamMutex.unlock();
	}
	public boolean isOpened()
	{
		return (input != null);
	}
	public Reader getReader()
	{
		return reader;
	}
	public void open(String fname) throws Exception
	{
		if(input != null)return;
		
		inputName = fname;
		String upper = fname.toUpperCase();
		if(upper.startsWith("JPIP://")){
			try{
				
				start_display(new Kdu_coords(800,600));
				actualView = new ImageView(display,new Rectangle(0,0,800,600),1,1);
				
				reader = new Reader(inputName,actualView);
				
				jp2_family.Open(reader);
				jpx_src.Open(jp2_family, true);
				
				compositor = new Compositor(jpx_src,display);
				
			}catch(Exception e)
			{
				
			}
		}
		startDecoding(this.actualView);
		
	}
	private void start_display(Kdu_coords view_size) throws KduException
	{
		display = new ImagePanel(view_size);
	}
	public synchronized void stopDecoding()
	{
		if(reader != null) reader.stop();
		compositor.stop();
		
		actualView = null;
	}
	public synchronized void startDecoding(ImageView newView)
	{
		actualView = newView;
		if(reader != null)
		{
			reader.start();
			compositor.start(reader);
		}
		
	}
	public ImagePanel getDisplay()
	{
		return display;
	}
	
}
