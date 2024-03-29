package Engine;
import kdu_jni.KduException;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_message;


/**
 * Kdu Exception Handling
 *
 */
public class Kdu_sysout_message extends Kdu_message
{
	public Kdu_sysout_message(boolean raise_exception)
	{
		this.raise_exception_on_end_of_message = raise_exception;
	}
	public void Put_text(String text)
	{ // Implements the C++ callback function `kdu_message::put_text'
		System.out.print(text);
	}
	public void Flush(boolean end_of_message) throws KduException
	{ // Implements the C++ callback function `kdu_message::flush'.
		if (end_of_message && raise_exception_on_end_of_message)
			throw new KduException(Kdu_global.KDU_ERROR_EXCEPTION,
					"In `Kdu_sysout_message'.");
	}
	private boolean raise_exception_on_end_of_message;
}

