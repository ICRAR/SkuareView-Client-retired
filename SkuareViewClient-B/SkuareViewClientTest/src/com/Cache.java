package com;

import kdu_jni.KduException;
import kdu_jni.Kdu_cache;

/**
 * Cache extends KDU_cache and ties JpipConstants to the cache object
 * 
 * @author Dylan McCarthy
 * @since 7/2/2013
 */
public class Cache extends Kdu_cache
{
	private Mutex cacheMutex = new Mutex();

	private static final int KDU_PRECINCT_DATABIN    	= 0;
	private static final int KDU_TILE_HEADER_DATABIN 	= 1;
	private static final int KDU_TILE_DATABIN					= 2;
	private static final int KDU_MAIN_HEADER_DATABIN 	= 3;
	private static final int KDU_META_DATABIN					= 4;

	/**
	 * Returns the kakadu class ID represented by the int input
	 * 
	 * @param classId	int classID
	 * @return int kakaduClassId
	 * @throws Exception	Throws exception if unable to find class id
	 */
	private int kakaduClassId(int classId) throws Exception
	{
		if(classId == JpipConstants.PRECINCT_DATA_BIN_CLASS) return KDU_PRECINCT_DATABIN;
		else if(classId == JpipConstants.TILE_HEADER_DATA_BIN_CLASS) return KDU_TILE_HEADER_DATABIN;
		else if(classId == JpipConstants.MAIN_HEADER_DATA_BIN_CLASS) return KDU_MAIN_HEADER_DATABIN;
		else if(classId == JpipConstants.META_DATA_BIN_CLASS) return KDU_META_DATABIN;
		else if(classId == JpipConstants.TILE_DATA_BIN_CLASS) return KDU_TILE_DATABIN;
		else throw new Exception("Data-bin class not supported by Kakadu");
	}
	/**
	 * Locks the cache
	 */
	public void Acquire_lock() throws KduException
	{
		cacheMutex.lock();
	}
	/**
	 * releases lock on cache
	 */
	public void Release_lock() throws KduException
	{
		cacheMutex.unlock();
	}
	/**
	 * Sets the inital scope for a new cache
	 * @throws Exception	Throws KduException
	 */
	public void setInitialScope() throws Exception
	{
		try {
			Set_read_scope(KDU_MAIN_HEADER_DATABIN, 0, 0);

		} catch(KduException ex) {
			throw new Exception("Internal Kakadu error: " + ex.getMessage());
		}
	}
	/**
	 * Adds a data segment to the cache
	 * 
	 * @param data	JpipDataSegment data 
	 * @throws Exception	Throws KduException
	 */
	public void addDataSegment(JpipDataSegment data) throws Exception
	{
		try {
			Add_to_databin(kakaduClassId((int)data.classId), (int)data.codestream, 
					(int)data.id, data.data, (int)data.offset, (int)data.length, data.isFinal, 
					true, false);

		} catch(KduException ex) {
			throw new Exception("Internal Kakadu error: " + ex.getMessage());
		}
	}
	/**
	 * return the Length of the selected databin
	 * 
	 * @param classId	int classID Identifier of the class
	 * @param codestream	int codestream	Identifier of the codestream 
	 * @param id	int id Databin Identifier
	 * @return	int length	Length of the databin
	 * @throws Exception	Throws KduException
	 */
	public int getDataBinLength(int classId, int codestream, int id) throws Exception
	{
		int len = 0;

		try {
			len = Get_databin_length(kakaduClassId(classId), codestream, id, null);

		} catch(KduException ex) {
			throw new Exception("Internal Kakadu error: " + ex.getMessage());
		}

		return len;
	}
	/**
	 * Checks if Databin is completed
	 * @param classId 	Identifier of the class
	 * @param codestream	Identifier of the codestream
	 * @param id	databin identifier
	 * @return	boolean complete
	 * @throws Exception	KduException
	 */
	public boolean isDataBinCompleted(int classId, int codestream, int id) throws Exception
	{
		boolean complete[] = new boolean[1];

		try {
			Get_databin_length(kakaduClassId(classId), codestream, id, complete);

		} catch(KduException ex) {
			throw new Exception("Internal Kakadu error: " + ex.getMessage());
		}

		return complete[0];
	}
}