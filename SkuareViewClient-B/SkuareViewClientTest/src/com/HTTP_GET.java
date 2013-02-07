package com;
import java.io.IOException;


public class HTTP_GET extends Cache {

	private int readData;
	private HTTPSocket socket;
	
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