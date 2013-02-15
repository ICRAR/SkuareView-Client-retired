package com;

import kdu_jni.KduException;
import kdu_jni.Kdu_client;
import kdu_jni.Kdu_codestream;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_window;

/**
 * This class attempts to connect to a JPIP server using the KDU_Client interface
 * Does not currently Work
 * 
 * @author Dylan McCarthy
 * @since 14/02/2013
 */
public class Client_Connect {

	private String _server;
	private String _proxy;
	private String _req;
	private String _trans;
	private String _cache_dir;
	private int _mode;
	private boolean connected;
	private int queue;
	
	private Kdu_client client;
	
	/**
	 * Takes the path as input and connects to the JPIP server 
	 * 
	 * @param path - Path to JPIP server
	 */
	public Client_Connect(String path)
	{
		_server = path.substring(0,path.lastIndexOf('/'));
		_proxy = null;
		_req = path.substring(path.lastIndexOf('/')+1,path.length());
		_trans = "http-tcp";
		_cache_dir = null;
		_mode = Kdu_global.KDU_CLIENT_MODE_AUTO;
		client = new Kdu_client();
		
		try {
			if(check_conn())
			{
				client.Connect(_server, _proxy, _req, _trans, _cache_dir, _mode);
				queue = client.Add_queue();
				if(client.Is_alive(queue))
				{
					connected = true;
				}
				
				
			}
		} catch (KduException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Check the connection is valid
	 * 
	 * @return returns value of Check_compatiable_connection call
	 * @throws KduException
	 */
	private boolean check_conn() throws KduException
	{
		return client.Check_compatible_connection(_server, _req, _mode);
	}
	/**
	 * Check if there is a connection
	 * 
	 * @return state of connection
	 */
	public boolean isConnected()
	{
		return connected;
	}
	/**
	 * close connection and clean up
	 */
	public void close()
	{
		if(connected)
		{
			try {
				client.Close();
			} catch (KduException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * Post a valid window as a reference for the JPIP connection
	 * 
	 * @param window - window for the current ROI
	 * @throws KduException
	 */
	public void post_window(Kdu_window window) throws KduException
	{
		if(connected)
		{
			client.Post_window(window);
		}
	}
	/**
	 * Get Data from connection
	 */
	public void getData()
	{	
		try{
		boolean complete[] = {false};
		client.Get_databin_length(JpipConstants.META_DATA_BIN_CLASS,client.Get_next_codestream(queue), 0,complete);
	
		if(complete[0])
		{
			client.Get_databin_length(JpipConstants.MAIN_HEADER_DATA_BIN_CLASS, client.Get_next_codestream(queue), 0,complete);
			if(complete[0])
			{
				Kdu_codestream codestream = new Kdu_codestream();
				codestream.Create(client);
			}
		}
		} catch (KduException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
