package j2k;

import kdu_jni.KduException;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_thread_env;

public class Enviroment {

	private Kdu_thread_env env;
	private int num_threads;
	
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
	public Kdu_thread_env getEnv()
	{
		return env;
	}
	public int getNumThreads()
	{
		return num_threads;
	}
	public void Dispose() throws KduException
	{

		env.Destroy();
		env.Terminate(null);

	}
	public void Join() throws KduException
	{
		env.Join(null);
	}
	
}
