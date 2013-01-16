package j2k;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kdu_jni.Kdu_coords;
import kdu_jni.Kdu_dims;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_region_decompressor;
import kdu_jni.Kdu_thread_env;
import kdu_jni.Kdu_thread_queue;

public class RenderWorker implements Runnable {

	private ImageInput image;
	private int bufferSize;
	private int nextBufferSize;
	private boolean finish;
	private int buffer[];
	private Kdu_dims incompletePart;
	private Kdu_dims decompressedPart;
	private Rectangle decompressedPartR;
	private Kdu_region_decompressor decompressor;
	private LinkedList<Rectangle> regionsList;
	private ImageView actualView;
	private Kdu_thread_env env;
	private Enviroment enviro;
	private ExecutorService exec;
	private Kdu_thread_queue queue;
	
	public RenderWorker(ImageInput j2kimage)
	{
		bufferSize = 0;
		exec = null;
		nextBufferSize = 16384000;
		image = j2kimage;
		
		incompletePart = new Kdu_dims();
		decompressedPart = new Kdu_dims();
		decompressedPartR = new Rectangle();
		
		decompressor = new Kdu_region_decompressor();
	
	}
	public void start()
	{
		if(exec != null) return;
		if(image.getActualView() == null) return;
		actualView = image.getActualView();
		regionsList = actualView.getRegionsToDecode();
		finish = false;
		
		enviro = new Enviroment();
		env = enviro.getEnv();
		
		exec = Executors.newCachedThreadPool();
		exec.execute(this);
		
	}
	public void stop()
	{
		if(exec == null) return;
		finish = true;
		try{
			exec.shutdown();
		}catch(Exception ex){};
		exec = null;
	}
	public boolean isStopped()
	{
		return (exec.isShutdown());
	}
	public void setBufferSize(int size)
	{
		nextBufferSize = size;
	}
	public void run()
	{
		try{
			

			while(!finish && !regionsList.isEmpty()) {
				//loops++;
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
				//logger.debug("Buffer size: " + bufferSize + " Loop: "+loops);

				image.lockCodeStream();

				decompressor.Start(image.getCodestream(), image.getChannels(), -1,image.getDiscardLevels(), 0, incompletePart, image.getExpansion(), new Kdu_coords(1, 1),false, Kdu_global.KDU_WANT_CODESTREAM_COMPONENTS,	false, null,null);

				
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
				//logger.debug("Incomplete X: "+ incompletePart.Access_size().Get_x() + " Y: " + incompletePart.Access_size().Get_y());
				if(incompletePart.Access_size().Get_x() != 0 &&	incompletePart.Access_size().Get_y() != 0)
					regionsList.add(new Rectangle(
							incompletePart.Access_pos().Get_x(),
							incompletePart.Access_pos().Get_y(),
							incompletePart.Access_size().Get_x(),
							incompletePart.Access_size().Get_y()
							));
				//logger.debug("regionList size " + regionsList.size());
				decompressor.Finish();
				image.unlockCodeStream();
				Thread.yield();
			}
			enviro.Dispose();
			exec.shutdown();
			if(regionsList.size() == 0)
				actualView.setCompleted();

		} catch(Exception ex) {
			System.err.println("Error in the render thread:");
			ex.printStackTrace();
		}
	}
}
