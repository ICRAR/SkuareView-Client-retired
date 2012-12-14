package Test;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

import kdu_jni.*;

class Kdu_sysout_message extends Kdu_message
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



public class JPIP_Test {

	public static ImagePanel display;

	static Kdu_sysout_message sysout =
			new Kdu_sysout_message(false); // Non-throwing message printer
	static Kdu_sysout_message syserr =
			new Kdu_sysout_message(true); // Exception-throwing message printer
	static Kdu_message_formatter pretty_sysout =
			new Kdu_message_formatter(sysout); // Non-throwing formatted printer
	static Kdu_message_formatter pretty_syserr =
			new Kdu_message_formatter(syserr); // Throwing formatted printer



	public static void main(String[] args) {
		try{
			Kdu_global.Kdu_customize_warnings(pretty_sysout);
			Kdu_global.Kdu_customize_errors(pretty_syserr);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try {
			
			HTTP_Test test = new HTTP_Test();
			test.setSize(new Dimension(600,400));
			test.setVisible(true);
			test.Start();
			test.openImage("jpip://127.0.0.1:8080/test3.j2k");
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
