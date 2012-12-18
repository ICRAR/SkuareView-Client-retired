package j2k;

import java.net.ProtocolException;

public class HTTPRequest extends HTTPMessage
{
	/** The URI of the object */
	private String uri;
	
	/** The request type */
	private	String type;
	
	
	/**
		Constructs a new HTTP request indicating the request type. 
		
		@throws ProtocolException
	*/
	public HTTPRequest(String type) throws ProtocolException
	{
		if(!type.equals("GET")) throw new ProtocolException("HTTP request type not supported");
		this.type = type;
	}

	/**
		Returns the URI of the object requested
	*/
	public String getURI()
	{
		return uri;
	}
	
	/**
		Sets the URI of the object.
	*/
	public void setURI(String uri)
	{
		this.uri = uri;
	}

	/**
		Returns the type of the request.
	*/
	public String getType()
	{
		return type;
	}
	
	/**
		This is a request message so this method always
		returns <code>true</code>.
	*/
	public boolean isRequest()
	{
		return true;
	}
}