package j2k;

import java.io.IOException;
import java.net.SocketException;

/**
 * The Reader is responcible for managing the JPIP communications over HTTP.
 * It is currently implemented to manage a stateless HTTP connection.
 * 
 * @author dmccarthy
 * @since 14/02/2013
 */
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
	
	/**
	 * A new Reader is created by supplying an ImageInput object that has a JPIP path
	 * 
	 * @param imageInput - Valid ImageInput with a JPIP compatable path
	 * @throws Exception
	 */
	public Reader(ImageInput imageInput) throws Exception
	{
		//Set initial Variables
		int i;
		HTTPRequest req;
		HTTPResponse res;
		String name, header, parts[];
		
		readData = 0;
		finish = false;
		myThread = null;
		image = imageInput;
		
		name = image.getImageName();
		
		//Parse the Input string
		parts = name.substring(7).split("/",2);
		
		if(parts.length != 2)
			throw new Exception("Error in format");
		
		//Save the path name
		jpipPath = parts[1];
		
		//Start initial connection
		try{
			//Form request
			req = new HTTPRequest("GET");
			req.setHeader("Cache-Control", "no-cache");
			req.setHeader("Connection","Keep-Alive");
			
			//Create socket
			socket = new HTTPSocket();
			host = parts[0];
			//Connect to Host
			socket.connect(host);
			socket.setSoTimeout(300000);
			//Send Request
			req.setURI("/" + parts[1] + "?cnew=jpip-ht&type=jpp-stream&len=" + MAX_LEN);
			socket.send(req);
			
			//Receive Response
			res = (HTTPResponse)socket.receive();

			if(res == null)
				throw new Exception("Connection Closed");
			
			if(res.getCode() != 200)
				throw new Exception("Error invalid status code");
			
			//Get header of response
			header = res.getHeader("JPIP-cnew");
			
			jpipChannel = null;
			//Check if header is empty
			if(header != null)
			{
				//split header into components
				parts = header.split(",");
				//Find client id and jpip path from header
				for(i = 0; i < parts.length; i++)
				{
					if(parts[i].startsWith("cid=")) jpipChannel = parts[i].substring(4);
					else if(parts[i].startsWith("path=")) jpipPath = parts[i].substring(5);
				}

				if(jpipChannel == null)
					throw new Exception("Channel not sent");
			}
			//Look for Transfer Encoding header
			header = res.getHeader("Transfer-Encoding");
			
			//Read any transfered data
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
	/**
	 *  This function reads from a direct TCP connection, is not currently used
	 * @return boolean - Returns true when read if finished, or false if there is a problem
	 * @throws IOException
	 * @throws Exception
	 */
	private boolean readFromTCP() throws IOException, Exception
	{
		//Set response to false
		boolean res = false;
		
		//Create new query
		HTTPResponse resp;
		HTTPRequest req = new HTTPRequest("GET");
		
		req.setHeader("Cache-Control", "no-cache");
		req.setHeader("Connection", "Keep-Alive");
		
		req.setURI("/" + jpipPath + "?cid=" + jpipChannel + "&len=" + MAX_LEN);

		//Check if socket is still connected, if not then reconnect
		if(!socket.isConnected()) socket.reconnect();
		
		//Receive response
		resp = (HTTPResponse)socket.receive();
		
		//Get data from input stream
		JpipDataInputStream jpip = new JpipDataInputStream(socket.getInputStream());
		JpipDataSegment data = new JpipDataSegment();
		//Read data into JpipDataSegment format
		while(jpip.readSegment(data))
		{
			//Record how much data is read
			readData +=(int)data.length;
			
			//If no more data, check that data has finished correctly
			if(!data.isEOR) addDataSegment(data);
			else
			{
				if(data.id == JpipConstants.EOR_WINDOW_DONE) res = true;
				else if(data.id == JpipConstants.EOR_IMAGE_DONE) res = true;
			}
		}
		return res;
	}
	/**
	 * This function reads from a Chunked input, this is the transfer method of the currently implemented JPIP connection
	 * 
	 * @return boolean - Returns true when read if finished, or false if there is a problem
	 * @throws IOException
	 * @throws Exception
	 */
	private boolean readFromChunkedInput() throws IOException, Exception
	{
		//Set response to false
		boolean res = false;
		
		//Create ChunkedInputStream from input
		ChunkedInputStream input = new ChunkedInputStream(socket.getInputStream());
		//Create JPIP input stream from ChunkedInput
		JpipDataInputStream jpip = new JpipDataInputStream(input);
		JpipDataSegment data = new JpipDataSegment();
		//Read JPIP input into JpipDataSegment format
		while(jpip.readSegment(data))
		{
			//Record how much data is read
			readData +=(int)data.length;
			//Check for end of data and correct end of data stream
			if(!data.isEOR) addDataSegment(data);
			else
			{
				if(data.id == JpipConstants.EOR_WINDOW_DONE) res = true;
				else if(data.id == JpipConstants.EOR_IMAGE_DONE) res = true;
			}
		}
		return res;
	}
	/**
	 * Returns how much data has been read
	 * @return int - Data read
	 */
	public int getReadData()
	{
		return readData;
	}
	/**
	 * Start the reader, this sets up the thread for the reader to run in and checks to make sure that there is an input
	 */
	public void start()
	{
		if(image.getActualView() == null) return;
		actualView = image.getActualView();
		myThread = new Thread(this);
		myThread.start();
	}
	/**
	 * Stop the reader and clean up thread
	 */
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
	/**
	 * Reads in Data from active connection based on ROI of image.
	 */
	public void run() {
		try{
			//Create new Query
			String roi = "";
			HTTPResponse res;
			HTTPRequest req = new HTTPRequest("GET");
			//Set Header
			req.setHeader("Cache-Control", "no-cache");
			req.setHeader("Connection", "Keep-Alive");
			//Set up ROI segment of the query
			roi += "&roff=" + actualView.getX() + "," + actualView.getY();
			roi += "&rsiz=" + actualView.getWidth() + "," + actualView.getHeight();
			roi += "&fsiz=" + (int)(image.getRealWidth() * (actualView.getScale() / 100.0));
			roi += "," + (int)(image.getRealHeight() * (actualView.getScale() / 100.0));
			roi += ",round-up";

			//Set up request
			req.setURI("/" + jpipPath + "?cid=" + jpipChannel + roi);
			
			//Check if socket is connected
			if(!socket.isConnected()) socket.reconnect();
			
			
			while(!finish) {
				try{
					//Send Request
					socket.send(req);
				}catch(SocketException s)
				{
					if(!socket.isConnected())
					socket.reconnect();
					
					socket.send(req);
				}
				//Receive response
				res = (HTTPResponse)socket.receive();

				//Check response HTTP code
				if(res.getCode() != 200)
					throw new IOException("Invalid status code returned by the server");

				//Read Chunked Input
				if(readFromChunkedInput()) break;	

				//Yield Thread
				Thread.yield();
			}
			//Set the View as completed
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
