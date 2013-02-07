package com;


import java.util.Enumeration;
import java.util.Hashtable;

public abstract class HTTPMessage
{
	/** A hash table with the headers of the message */
	private Hashtable headers = new Hashtable();
	

	/**
		Returns <code>true</code> if the message is a request.
	*/
	public abstract boolean isRequest();
	
	/**
		Returns <code>true</code> if the message is a response.
	*/
	public final boolean isResponse()
	{
		return !isRequest();
	}
	
	/**
		Returns the value of a message header.
		
		@param key The header name.
		@return The value of the specified header or <code>null</code> if 
			it was not found.
	*/
	public final String getHeader(String key)
	{
		return (String)headers.get(key);
	}

	/**
		Sets a new value for a spcific HTTP message header. If the header
		does not exists, it will be added to the header list of the message.
		
		@param key Header name.
		@param val Header value.
	*/
	public final void setHeader(String key, String val)
	{
		headers.put(key, val);
	}

	/**
		Returns an <code>Enumeration</code> with all the headers keys.
	*/
	public final Enumeration getHeaders()
	{
		return headers.keys();
	}
	
	/**
		Removes all the message headers.
	*/
	public final void clearHeaders()
	{
		headers.clear();
	}
}