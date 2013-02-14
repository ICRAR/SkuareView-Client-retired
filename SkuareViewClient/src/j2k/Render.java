package j2k;

import java.awt.Rectangle;
import java.util.LinkedList;

//import org.apache.log4j.Logger;

import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_dims;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_region_decompressor;
import kdu_jni.Kdu_thread_env;

/**
 * The Render class is responsible for rendering all the JPIP data into a BufferedImage format.
 * The current implementation uses the KDU_region_decompressor to decompress the JPIP data.
 * 
 * @author dmccarthy
 * @since 14/02/2013
 */
public class Render implements Runnable {
	private ImageInput image;                        
	private Thread myThread;                       
	private boolean finish;                        
	private int buffer[];                          
	private int bufferSize;                        
	private int nextBufferSize;                    
	private Kdu_dims incompletePart;               
	private Kdu_dims decompressedPart;             
	private Rectangle decompressedPartR;           
	private Kdu_region_decompressor decompressor;  
	private LinkedList<Rectangle> regionsList;
	private ImageView actualView;
	private Kdu_thread_env env;
	private Enviroment enviro;

	/**
	 * Set up new Render with a jpeg2000 format image input
	 * 
	 * @param j2kImage - JPEG2000 format image input
	 */
	public Render(ImageInput j2kImage)
	{
		//Initialize Variables
		bufferSize = 0;
		myThread = null;
		image = j2kImage;
		//Set buffer Size
		nextBufferSize = 16384000;
		
		incompletePart = new Kdu_dims();
		decompressedPart = new Kdu_dims();
		decompressedPartR = new Rectangle();

		decompressor = new Kdu_region_decompressor();
	}
	/**
	 * Start the decompressor
	 */
	public void start()
	{
		//Check for running thread 
		if(myThread != null) return;
		//Check for image Input
		if(image.getActualView() == null) return;

		//Get view and regions to be decoded
		actualView = image.getActualView();
		regionsList = actualView.getRegionsToDecode();

		finish = false;

		//Create new thread, set priority and start
		myThread = new Thread(this);
		myThread.setPriority(Thread.NORM_PRIORITY);
		myThread.start();
		
	}

	/**
	 * Stop render thread and clean up
	 */
	public void stop()
	{
		if(myThread == null) return;
		finish = true;
		try { myThread.join(); } catch(InterruptedException ex) { }
		myThread = null;
		
	}
	/**
	 * Check if stopped
	 * @return boolean - Check if stopped
	 */
	public boolean isStopped()
	{
		return (myThread == null);
	}
	/**
	 * Set buffer Size
	 * @param size - Size of buffer
	 */
	public void setBufferSize(int size)
	{
		nextBufferSize = size;
	}
/** 
 * Run decompressor
 * This sets up the enviroment for KDU, uses the regions list from the View to read in data from JPIP and decompress it
 */
	public void run()
	{
		try{
			//Set up thread Enviroment for KDU
			enviro = new Enviroment();
			env = enviro.getEnv();

			while(!finish && !regionsList.isEmpty()) {
				//Get first region to be decompressed
				Rectangle actualRegion = regionsList.removeFirst();
				if(!actualView.isContentCompleted()) regionsList.add(actualRegion);
				//Set access position and size
				incompletePart.Access_pos().Set_x(actualRegion.x);
				incompletePart.Access_pos().Set_y(actualRegion.y);
				incompletePart.Access_size().Set_x(actualRegion.width);
				incompletePart.Access_size().Set_y(actualRegion.height);
				//Create buffer
				if(bufferSize != nextBufferSize) {
					bufferSize = nextBufferSize;
					buffer = new int[bufferSize];
				}
				//Lock code stream
				image.lockCodeStream();
				//Start decompression
				decompressor.Start(image.getCodestream(), image.getChannels(), -1,image.getDiscardLevels(), 0, incompletePart, image.getExpansion(), new Kdu_coords(1, 1),false, Kdu_global.KDU_WANT_CODESTREAM_COMPONENTS,	false, env);

				//Process Data
				while(!finish && decompressor.Process(buffer, new Kdu_coords(0, 0), 0, 0,
						bufferSize, incompletePart,
						decompressedPart)) {
					//Set bounds on decompressed data
					decompressedPartR.setBounds(
							decompressedPart.Access_pos().Get_x(),
							decompressedPart.Access_pos().Get_y(),
							decompressedPart.Access_size().Get_x(),
							decompressedPart.Access_size().Get_y()
							);
					//Update View with new data
					actualView.updateNewRegion(decompressedPartR, buffer);
				}
				//If not finished, find new Region to process and add it to the regions list
				if(incompletePart.Access_size().Get_x() != 0 &&	incompletePart.Access_size().Get_y() != 0)
					regionsList.add(new Rectangle(
							incompletePart.Access_pos().Get_x(),
							incompletePart.Access_pos().Get_y(),
							incompletePart.Access_size().Get_x(),
							incompletePart.Access_size().Get_y()
							));
				//Finish decompressing
				decompressor.Finish();
				//unlock code Stream
				image.unlockCodeStream();
				//Yield the thread
				Thread.yield();
			}
			//Dispose of KDU enviroment and java Thread
			enviro.Dispose();
			myThread = null;
			//Check if any more regions to decompress
			if(regionsList.size() == 0)
				actualView.setCompleted();

		} catch(Exception ex) {
			System.err.println("Error in the render thread:");
			ex.printStackTrace();
		}
	}
}
