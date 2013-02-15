package com;
import java.io.IOException;

/**
 * Basic HTTP GET class for getting data through HTTP
 * 
 * @author Dylan McCarthy
 * @since 14/02/2013
 *
 */
public class HTTP_GET extends Cache {

	@SuppressWarnings("unused")
	private int readData;
	private HTTPSocket socket;
	
	/**
	 * Takes a path input as a string and connects to the JPIP server
	 * @param nameIn
	 */
	@SuppressWarnings("unused")
	public HTTP_GET(String nameIn)
	{
		int i;
		HTTPRequest req;
		HTTPResponse res;
		String name, header, parts[];
		readData =0;

		name = nameIn;
		parts = name.substring(7).split("/");

		String jpipPath = parts[1];

		try{
			req = new HTTPRequest("GET");
			req.setHeader("Cache-Control", "no-cache");
			req.setHeader("Connection","Keep-Alive");

			socket = new HTTPSocket();

			socket.connect(parts[0]);


			req.setURI("/" + parts[1] + " HTTP/1.1");

			socket.send(req);

			HTTPMessage resp = socket.receive();

			res = (HTTPResponse)resp;

			String jpipChannel = null;

			header = res.getHeader("JPIP-cnew");
			
			readFromChunkedInput();
			setInitialScope();
			
		}catch(IOException ex)
		{
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Reads from Chunked Input and returns boolean
	 * 
	 * @return returns true of false depending on how the input ended
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
}