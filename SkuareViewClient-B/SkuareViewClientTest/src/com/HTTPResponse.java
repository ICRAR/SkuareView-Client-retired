package com;


public class HTTPResponse extends HTTPMessage
{
	/** The status code */
	private int code;
	
	/** The reason phrase */
	private String reason;
	
	
	/**
		Constructs a new HTTP response, with its status code
		and reason phrase.
		
		@param code Status code.
		@param reason Reason phrase.
	*/
	public HTTPResponse(int code, String reason)
	{
		this.code = code;
		this.reason = reason;
	}
	
	/**
		Returns the status code.
	*/
	public int getCode()
	{
		return code;
	}
	
	/**
		Returns the reason phrase.
	*/
	public String getReason()
	{
		return reason;
	}
	
	/**
		This is a response message so this method always
		returns <code>false</code>.
	*/
	public boolean isRequest()
	{
		return false;
	}
}