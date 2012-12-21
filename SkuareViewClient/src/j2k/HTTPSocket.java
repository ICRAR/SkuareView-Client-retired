package j2k;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.Enumeration;

public class HTTPSocket extends Socket
{
	/** The last used port */
	private int lastUsedPort = 0;

	/** The last used host */
	private String lastUsedHost = null;

	/** The default port for the HTTP socket */
	static public final int PORT = 80;

	/** The maximum HTTP version supported */
	static public final double version = 1.1;

	/** The version in standard formated text */
	static public final String versionText = "HTTP/" + Double.toString(version);

	/** The array of bytes that contains the CRLF codes */
	static public final byte CRLFBytes[] = {13, 10};

	/** The string representation of the CRLF codes */
	static public final String CRLF = new String(CRLFBytes);


	/**
			Connects to the specified host using the default port. If the string
			contains port information (using the format <code>host:port</code>),
			it is decoded.

			@param host Host to connect.
			@throws java.io.IOException
	 */
	public void connect(String host) throws IOException
	{
		String parts[] = host.split(":", 2);

		if(parts.length < 2) {
			lastUsedHost = host;
			lastUsedPort = PORT;

		} else {
			lastUsedHost = parts[0];

			try {
				lastUsedPort = Integer.parseInt(parts[1]);

			} catch(NumberFormatException ex) {
				throw new ProtocolException("Invalid port number");
			}
		}

		super.connect(new InetSocketAddress(lastUsedHost, lastUsedPort));
	}

	/**
			Connects to the specified host using the specified port.

			@param host Host to connect.
			@param port Port to use.
			@throws java.io.IOException
	 */
	public void connect(String host, int port) throws IOException
	{
		lastUsedHost = host;
		lastUsedPort = port;

		super.connect(new InetSocketAddress(lastUsedHost, lastUsedPort));
	}

	/**
			Reconnects to the last used host, and using the last used port.

			@throws java.io.IOException
	 */
	public void reconnect() throws IOException
	{
		super.connect(new InetSocketAddress(lastUsedHost, lastUsedPort));
	}

	/**
			Sends a HTTP message. Currently it is only supported to send HTTP requests.

			@param msg A <code>HTTPMessage</code> object with the message.
			@throws java.io.IOException
	 */
	public void send(HTTPMessage msg) throws IOException
	{
		if(msg.isRequest()) {
			String key, str = "";
			HTTPRequest req = (HTTPRequest)msg;

			str += req.getType() + " " + req.getURI()  + " " + versionText + CRLF;

			for(Enumeration<?> e = req.getHeaders(); e.hasMoreElements();) {
				key = (String)e.nextElement();
				str += key + ": ";
				str += req.getHeader(key) + CRLF;
			}

			str += CRLF;

			getOutputStream().write(str.getBytes());

		} else {
			throw new ProtocolException("Responses sending not yet supported :-)");
		}
	}

	/**
			Receives a HTTP message from the socket. Currently it is only supported
			to receive HTTP responses.

			@return A new <code>HTTPMessage</code> object with the message read or 
						<code>null</code> if the end of stream was reached.
			@throws java.io.IOException
	 */
	public HTTPMessage receive() throws IOException
	{
		int code;
		double ver;
		String line;
		String parts[];

		InputStream input = getInputStream();
		@SuppressWarnings("resource")
		StringInputStream lineInput = new StringInputStream(input);

		line = lineInput.readLine();
		if(line == null) return null;

		parts = line.split(" ", 3);

		if(parts.length != 3) 
			throw new ProtocolException("Invalid HTTP message");

		if(parts[0].startsWith("HTTP/")) {
			try {
				ver = Double.parseDouble(parts[0].substring(5));

			} catch(NumberFormatException ex) {
				throw new ProtocolException("Invalid HTTP version format");
			}

			if((ver < 1) || (ver > version))
				throw new ProtocolException("HTTP version not supported");

			try {
				code = Integer.parseInt(parts[1]);

			} catch(NumberFormatException ex) {
				throw new ProtocolException("Invalid HTTP status code format");
			}

			HTTPResponse res = new HTTPResponse(code, parts[2]);

			for(;;) {
				line = lineInput.readLine();
				if(line == null) throw new EOFException("End of stream reached before end of HTTP message");
				else if(line.length() <= 0) break;

				parts = line.split(": ", 2);

				if(parts.length != 2)
					throw new ProtocolException("Invalid HTTP header format");

				res.setHeader(parts[0], parts[1]);
			}

			return res;

		} else {
			throw new ProtocolException("Requests receiving not yet supported :-)");
		}
	}

}
