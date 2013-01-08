package j2k;

import java.awt.Rectangle;
import java.util.LinkedList;

import kdu_jni.KduException;
import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_dims;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_region_decompressor;
import kdu_jni.Kdu_thread_env;
import kdu_jni.Kdu_thread_queue;

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

	public Render(ImageInput j2kImage)
	{

		bufferSize = 0;
		myThread = null;
		image = j2kImage;
		nextBufferSize = 100000;

		incompletePart = new Kdu_dims();
		decompressedPart = new Kdu_dims();
		decompressedPartR = new Rectangle();

		decompressor = new Kdu_region_decompressor();
	}

	public void start()
	{
		if(myThread != null) return;
		if(image.getActualView() == null) return;

		actualView = image.getActualView();
		regionsList = actualView.getRegionsToDecode();

		finish = false;

		myThread = new Thread(this);
		myThread.setPriority(Thread.MIN_PRIORITY);
		myThread.start();
		
	}

	public void stop()
	{
		if(myThread == null) return;
		finish = true;
		try { myThread.join(); } catch(InterruptedException ex) { }
		myThread = null;
		
	}

	public boolean isStopped()
	{
		return (myThread == null);
	}

	public void setBufferSize(int size)
	{
		nextBufferSize = size;
	}

	public void run()
	{
		try{
			enviro = new Enviroment();
			env = enviro.getEnv();

			while(!finish && !regionsList.isEmpty()) {
				Rectangle actualRegion = regionsList.removeFirst();
				if(!actualView.isContentCompleted()) regionsList.add(actualRegion);

				incompletePart.Access_pos().Set_x(actualRegion.x);
				incompletePart.Access_pos().Set_y(actualRegion.y);
				incompletePart.Access_size().Set_x(actualRegion.width);
				incompletePart.Access_size().Set_y(actualRegion.height);

				if(bufferSize != nextBufferSize) {
					bufferSize = nextBufferSize;
					buffer = new int[bufferSize];
				}

				image.lockCodeStream();

				decompressor.Start(image.getCodestream(), image.getChannels(), -1,image.getDiscardLevels(), 0, incompletePart, image.getExpansion(), new Kdu_coords(1, 1),false, Kdu_global.KDU_WANT_CODESTREAM_COMPONENTS,	false, env);

				while(!finish && decompressor.Process(buffer, new Kdu_coords(0, 0), 0, 0,
						bufferSize, incompletePart,
						decompressedPart)) {

					decompressedPartR.setBounds(
							decompressedPart.Access_pos().Get_x(),
							decompressedPart.Access_pos().Get_y(),
							decompressedPart.Access_size().Get_x(),
							decompressedPart.Access_size().Get_y()
							);

					actualView.updateNewRegion(decompressedPartR, buffer);
				}

				if(incompletePart.Access_size().Get_x() != 0 &&
						incompletePart.Access_size().Get_y() != 0)

					regionsList.add(new Rectangle(
							incompletePart.Access_pos().Get_x(),
							incompletePart.Access_pos().Get_y(),
							incompletePart.Access_size().Get_x(),
							incompletePart.Access_size().Get_y()
							));
				decompressor.Finish();
				image.unlockCodeStream();
				Thread.yield();
			}
			enviro.Dispose();
			myThread = null;

			if(regionsList.size() == 0)
				actualView.setCompleted();

		} catch(Exception ex) {
			System.err.println("Error in the render thread:");
			ex.printStackTrace();
		}
	}
}
