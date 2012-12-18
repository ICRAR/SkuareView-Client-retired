package j2k;

public class Mutex {

	private Thread owner = null;

	public synchronized void lock()
	{
		if(owner == Thread.currentThread()) return;

		while(owner != null)
		{
			try{
				wait();
			}
			catch(InterruptedException ex){}

		}
	}
	public synchronized void unlock()
	{
		owner = null;
		notify();
	}
}


