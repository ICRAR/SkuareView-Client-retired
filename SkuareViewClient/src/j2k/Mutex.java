package j2k;

/**
 * Mutex for locking and unlocking the codestream thread
 * @author Dylan McCarthy
 * @since 14/02/2013
 *
 */
public class Mutex {

	private Thread owner = null;
	/**
	 * Lock the current thread
	 */
	public synchronized void lock()
	{
		//Get current thread
		if(owner == Thread.currentThread()) return;

		while(owner != null)
		{
			try{
				wait();
			}
			catch(InterruptedException ex){}

		}
	}
	/**
	 * Unlock the thread
	 */
	public synchronized void unlock()
	{
		//reset owner and notify thread
		owner = null;
		notify();
	}
}


