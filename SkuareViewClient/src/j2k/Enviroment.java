package j2k;

import kdu_jni.KduException;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_thread_env;

/**
 * Multithread Enviroment
 * This class sets up the multithreaded enviroment required by Kakadu
 * 
 * @author dmccarthy
 * @since 08/02/2013
 */
public class Enviroment {

	private Kdu_thread_env env;
	private int num_threads;
	
	/**
	 * Constructor
	 * Creates the Enviroment based off the amount of available processors
	 */
	public Enviroment()
	{
		try {
			num_threads = Kdu_global.Kdu_get_num_processors();

			env = new Kdu_thread_env();
			env.Create();
			for (int nt = 1; nt < num_threads; nt++)
				if (!env.Add_thread())
					num_threads = nt;
			

		} catch (KduException e) {
		}
	}
	/**
	 * Constructor
	 * Creates the Enviroment based off a given number of threads
	 * @param threads	Given number of threads to be generated
	 */
	public Enviroment(int threads)
	{
		try{
		num_threads = threads;
		
		env = new Kdu_thread_env();
		env.Create();
		for(int nt = 1; nt < num_threads; nt++)
		{
			if(!env.Add_thread())
				num_threads = nt;
		}
		}catch(KduException e)
		{
			
		}
	}
	/**
	 * Return the created enviroment
	 * @return Kdu_thread_env object
	 */
	public Kdu_thread_env getEnv()
	{
		return env;
	}
	/**
	 * Return the number of threads created
	 * 
	 * @return Number of threads
	 */
	public int getNumThreads()
	{
		return num_threads;
	}
	/**
	 * Clean up and dispose of Enviroment
	 * 
	 * @throws KduException
	 */
	public void Dispose() throws KduException
	{

		env.Destroy();
		env.Terminate(null);

	}
	/**
	 * Forces the Threads to join
	 * @throws KduException
	 */
	public void Join() throws KduException
	{
		env.Join(null);
	}
	
}
