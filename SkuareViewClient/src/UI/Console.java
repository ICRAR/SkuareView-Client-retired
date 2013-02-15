package UI;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.ScrollPane;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextPane;


/**
 * Console Window
 * This object creates a text console for outputting information to the user
 * 
 * @author Dylan McCarthy
 * @since 7/2/2013
 */
@SuppressWarnings("serial")
public class Console extends JPanel  {
	
	private JTextPane textPane;
	/**
	 * Console constructor
	 */
	public Console() {
		setLayout(new BorderLayout(0, 0));
		
		textPane = new JTextPane();
		textPane.setBounds(this.getBounds());
		ScrollPane scrollpane = new ScrollPane();
		scrollpane.add(textPane);
		add(scrollpane, BorderLayout.CENTER);
		textPane.setText("SkuareViewClient 0.3 Console (" + SkuareViewClient.OS + ", " + SkuareViewClient.WorkingDir + ")" + "\n================================" );
	}
	/**
	 * This function prints text lines to the console window.
	 * @param input		String input
	 */
	public void println(String input)
	{
		String current = textPane.getText();
		textPane.setText(current + "\n" + getTime() + input);
	}
	/**
	 * Clear all text on the console window
	 * 
	 */
	public void clear()
	{
		textPane.setText("");
	}
	/**
	 * Gets the current time to generate timestamp for the window
	 * @return	String 		returns the current timestamp in the format "hh:mm:ss"
	 */
	private String getTime()
	{
		Date d = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("hh:mm:ss");
		return "<" + sf.format(d) + ">    ";
	}
}
