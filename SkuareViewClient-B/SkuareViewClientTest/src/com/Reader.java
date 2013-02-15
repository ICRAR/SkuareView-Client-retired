package com;

import java.io.IOException;
import java.net.SocketException;

import Engine.ImageView;

/**
 * The Reader is responsible for managing the JPIP communications over HTTP.
 * It is currently implemented to manage a stateless HTTP connection.
 * 
 * @author Dylan McCarthy
 * @since 14/02/2013
 */
public class Reader extends Cache implements Runnable {

	@SuppressWarnings("unused")
	private int readData;
	private boolean finish;
	private Thread myThread;
	private String jpipPath;
	private HTTPSocket socket;
	private String jpipChannel;
	private String host;
	private ImageView actualView;
	
	@SuppressWarnings("unused")
	private static final int MAX_LEN = 2000;
	
	/**
	 * A new Reader is created by supplying an ImageView object that has a JPIP path
	 * 
	 * @param path - path to the JPIP server
	 * @param actualView - Valid ImageView with a JPIP compatible path
	 * @throws Exception
	 */
	public Reader(String path,ImageView actualView) throws Exception
	{
		int i;
		HTTPRequest req;
		HTTPResponse res;
		String name, header, parts[];
		
		this.actualView = actualView;
		
		//initialize read data, finished status and thread
		readData = 0;
		finish = false;
		myThread = null;
		
		name = path;
		
		//Split path for relevant details
		parts = name.substring(7).split("/",2);
		
		//Check format
		if(parts.length != 2)
			throw new Exception("Error in format");
		
		jpipPath = parts[1];
		
		try{
			//Create new HTTP GET request
			req = new HTTPRequest("GET");
			req.setHeader("Cache-Control", "no-cache");
			req.setHeader("Connection","Keep-Alive");
			
			//Create socket connection
			socket = new HTTPSocket();
			//find host details
			host = parts[0];
			//Connect to host
			socket.connect(host);
			socket.setSoTimeout(300000);
			
			//Send initial request
			req.setURI("/" + parts[1] + "?cnew=jpip-ht&type=jpp-stream&len=");
			socket.send(req);
			
			//Receive response
			res = (HTTPResponse)socket.receive();

			//Check for null response or invalid status response
			if(res == null)
				throw new Exception("Connection Closed");
			
			if(res.getCode() != 200)
				throw new Exception("Error invalid status code");
			
			//Get header
			header = res.getHeader("JPIP-cnew");
			
			//Find return channel and path
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
			//Get transfer header
			header = res.getHeader("Transfer-Encoding");
			
			//read from chunked input and set inital scopes
			readFromChunkedInput();
			setInitialScope();

		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			
			throw new Exception("Cannot Connect");
		}
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
	/**
	 * Get data from a specified ROI
	 * 
	 * @param x	- x location
	 * @param y - y location 
	 * @param width - width of ROI
	 * @param height - height of ROI
	 * @param scale - scale of ROI
	 */
	public void getData(int x, int y, int width, int height, int scale)
	{
		try{
			String roi = "";
			HTTPResponse res;
			HTTPRequest req = new HTTPRequest("GET");
			
			req.setHeader("Cache-Control", "no-cache");
			req.setHeader("Connection", "Keep-Alive");
			

			
			roi += "&roff=" + x + "," + y;
			roi += "&rsiz=" + width + "," + height;
			roi += "&fsiz=" + (int)(width * (scale / 100.0));
			roi += "," + (int)(height * (scale / 100.0));
			roi += ",round-up";

			
			System.out.println("ROI: " + roi);
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

			}


		} catch(IOException ex) {
			ex.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Start the reader, this sets up the thread for the reader to run in and checks to make sure that there is an input
	 */
	public void start()
	{
		if(host == null) return;
		myThread = new Thread(this);
		myThread.start();
	}
	/**
	 * Stop the reader and clean up thread
	 */
	public void stop()
	{
		if(myThread !=null)
		{
			finish = true;
			
			try{
				myThread.join();
			}
			catch(InterruptedException ex){}
			
			myThread = null;
			finish = false;
		}
	}
	/**
	 * Reads in Data from active connection based on ROI of image.
	 */
	@Override
	public void run() {
		try{
			String roi = "";
			HTTPResponse res;
			HTTPRequest req = new HTTPRequest("GET");
			
			req.setHeader("Cache-Control", "no-cache");
			req.setHeader("Connection", "Keep-Alive");
			

			
			roi += "&roff=" + actualView.getX() + "," + actualView.getY();
			roi += "&rsiz=" + actualView.getWidth() + "," + actualView.getHeight();
			roi += "&fsiz=" + (int)(actualView.getWidth() * (actualView.getScale() / 100.0));
			roi += "," + (int)(actualView.getHeight() * (actualView.getScale() / 100.0));
			roi += ",round-up";

			
			System.out.println("ROI: " + roi);
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

				if(readFromChunkedInput())
				{
					finish = true;	
					break;
				}
				Thread.yield();

			}


		} catch(IOException ex) {
			ex.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	/**
	 * Returns the state of the reader
	 * @return	boolean - state of reader
	 */
	public boolean finished()
	{
		return finish;
	}
	public void init()
	{
		
	}
}
