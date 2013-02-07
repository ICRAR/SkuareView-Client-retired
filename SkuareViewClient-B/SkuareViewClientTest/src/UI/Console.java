package UI;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.ScrollPane;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTextPane;

@SuppressWarnings("serial")
public class Console extends JPanel  {
	
	private JTextPane textPane;
	public Console() {
		setLayout(new BorderLayout(0, 0));
		
		textPane = new JTextPane();
		textPane.setBounds(this.getBounds());
		ScrollPane scrollpane = new ScrollPane();
		scrollpane.add(textPane);
		add(scrollpane, BorderLayout.CENTER);
		textPane.setText("SkuareViewClient 0.3 Console (" + SkuareViewClient.OS + ", " + SkuareViewClient.WorkingDir + ")" + "\n================================" );
	}
	public void println(String input)
	{
		String current = textPane.getText();
		textPane.setText(current + "\n" + getTime() + input);
	}
	public void clear()
	{
		textPane.setText("");
	}
	private String getTime()
	{
		Date d = new Date();
		SimpleDateFormat sf = new SimpleDateFormat("hh:mm:ss");
		return "<" + sf.format(d) + ">    ";
	}

	
}
