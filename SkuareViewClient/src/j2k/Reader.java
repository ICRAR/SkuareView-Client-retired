package j2k;

import java.io.IOException;
import java.net.SocketException;

public class Reader extends Cache implements Runnable {

	private int readData;
	private boolean finish;
	private ImageInput image;
	private Thread myThread;
	private String jpipPath;
	private HTTPSocket socket;
	private String jpipChannel;
	private ImageView actualView;
	private String host;
	
	private static final int MAX_LEN = 2000;
	
	public Reader(ImageInput imageInput) throws Exception
	{
		int i;
		HTTPRequest req;
		HTTPResponse res;
		String name, header, parts[];
		
		readData = 0;
		finish = false;
		myThread = null;
		image = imageInput;
		
		name = image.getImageName();
		
		parts = name.substring(7).split("/",2);
		
		if(parts.length != 2)
			throw new Exception("Error in format");
		
		jpipPath = parts[1];
		
		try{
			req = new HTTPRequest("GET");
			req.setHeader("Cache-Control", "no-cache");
			req.setHeader("Connection","Keep-Alive");
			
			socket = new HTTPSocket();
			host = parts[0];
			
			socket.connect(host);
			socket.setSoTimeout(300000);
			req.setURI("/" + parts[1] + "?cnew=jpip-ht&type=jpp-stream&len=" + MAX_LEN);
			
			socket.send(req);
			res = (HTTPResponse)socket.receive();

			if(res == null)
				throw new Exception("Connection Closed");
			
			if(res.getCode() != 200)
				throw new Exception("Error invalid status code");
			
			header = res.getHeader("JPIP-cnew");
			
			jpipChannel = null;
			if(header != null)
			{
				parts = header.split(",");
				for(i = 0; i < parts.length; i++)
				{
					if(parts[i].startsWith("cid=")) jpipChannel = parts[i].substring(4);
					else if(parts[i].startsWith("path=")) jpipPath = parts[i].substring(5);
				}

				if(jpipChannel == null)
					throw new Exception("Channel not sent");
			}
			header = res.getHeader("Transfer-Encoding");
			
			if(header == null)
				readFromTCP();
			else
			readFromChunkedInput();
			setInitialScope();

		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			
			throw new Exception("Cannot Connect");
		}
	}
	@SuppressWarnings("unused")
	private boolean readFromTCP() throws IOException, Exception
	{
		boolean res = false;
		
		HTTPResponse resp;
		HTTPRequest req = new HTTPRequest("GET");
		
		req.setHeader("Cache-Control", "no-cache");
		req.setHeader("Connection", "Keep-Alive");
		
		req.setURI("/" + jpipPath + "?cid=" + jpipChannel + "&len=" + MAX_LEN);

		if(!socket.isConnected()) socket.reconnect();
		
		resp = (HTTPResponse)socket.receive();
		
		JpipDataInputStream jpip = new JpipDataInputStream(socket.getInputStream());
		JpipDataSegment data = new JpipDataSegment();
		while(jpip.readSegment(data))
		{
			readData +=(int)data.length;
			
			if(!data.isEOR) addDataSegment(data);
			else
			{
				if(data.id == JpipConstants.EOR_WINDOW_DONE) res = true;
				else if(data.id == JpipConstants.EOR_IMAGE_DONE) res = true;
			}
		}
		
		
		return res;
	}
	private boolean readFromChunkedInput() throws IOException, Exception
	{
		boolean res = false;
		
		ChunkedInputStream input = new ChunkedInputStream(socket.getInputStream());
		JpipDataInputStream jpip = new JpipDataInputStream(input);
		JpipDataSegment data = new JpipDataSegment();
		
		while(jpip.readSegment(data))
		{
			readData +=(int)data.length;
			
			if(!data.isEOR) addDataSegment(data);
			else
			{
				if(data.id == JpipConstants.EOR_WINDOW_DONE) res = true;
				else if(data.id == JpipConstants.EOR_IMAGE_DONE) res = true;
			}
		}
		return res;
	}
	public int getReadData()
	{
		return readData;
	}
	public void start()
	{
		if(image.getActualView() == null) return;
		actualView = image.getActualView();
		myThread = new Thread(this);
		myThread.start();
	}
	public void stop()
	{
		if(myThread != null)
		{
			finish = true;
			
			try{
				myThread.join();
			}catch(InterruptedException ex){}
			
			myThread = null;
			finish = false;
			
		}
	}
	public void run() {
		try{
			String roi = "";
			HTTPResponse res;
			HTTPRequest req = new HTTPRequest("GET");
			
			req.setHeader("Cache-Control", "no-cache");
			req.setHeader("Connection", "Keep-Alive");

			
			roi += "&roff=" + actualView.getX() + "," + actualView.getY();
			roi += "&rsiz=" + actualView.getWidth() + "," + actualView.getHeight();
			roi += "&fsiz=" + (int)(image.getRealWidth() * (actualView.getScale() / 100.0));
			roi += "," + (int)(image.getRealHeight() * (actualView.getScale() / 100.0));
			roi += ",round-up";

			req.setURI("/" + jpipPath + "?cid=" + jpipChannel + roi);
			
			if(!socket.isConnected()) socket.reconnect();
			
			while(!finish) {
				try{	
					socket.send(req);
				}catch(SocketException s)
				{
					//socket.close();
					if(!socket.isConnected())
					socket.reconnect();
					
					socket.send(req);
				}
				res = (HTTPResponse)socket.receive();

				if(res.getCode() != 200)
					throw new IOException("Invalid status code returned by the server");

				if(readFromChunkedInput()) break;	

				Thread.yield();
			}

			actualView.setContentCompleted();

		} catch(IOException ex) {
			ex.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	public void init() {
		
	}
}
