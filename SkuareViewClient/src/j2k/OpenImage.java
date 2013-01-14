package j2k;


import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;


@SuppressWarnings("serial")
public class OpenImage extends JFrame implements ImageWindow  {

	private static String imageName;
	private static ImagePanel imagePanel;
	private static JFrame frame;
	private JScrollBar verticalScrollBar;
	private JScrollBar horizontalScrollBar;
	
	//Attempt to open input stream
	public static JPanel open(String fname)
	{
		OpenImage open = new OpenImage();
		imageName = fname;
		imagePanel = new ImagePanel(open);
		if(fname != null)
		{
			try{

				imagePanel.openImage(imageName);
			}
			catch(Exception ex)
			{
				System.err.println("Error loading the image:");
				ex.printStackTrace();

				JOptionPane.showMessageDialog(null, 
						"It was impossible to load the image '" + imageName + "'", 
						"Error loading image", 
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return imagePanel;
	}
	//constructor
	public OpenImage()
	{
		//Set base frame
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		//Add scrollbars to frame
		verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL,0,0,0,180);
		verticalScrollBar.setBounds(571, 0, 15, 518);
		horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL,0,0,0,180);
		horizontalScrollBar.setBounds(0, 520, 586, 15);

		verticalScrollBar.setVisible(false);
		horizontalScrollBar.setVisible(false);
		//Create new image panel
		imagePanel = new ImagePanel(this);
		imagePanel.setBounds(0, 0, 571, 518);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setBounds(0, 23, 587, 538);
		centerPanel.setLayout(null);
		centerPanel.add(imagePanel);
		centerPanel.add(verticalScrollBar);
		centerPanel.add(horizontalScrollBar);
		frame.getContentPane().add(centerPanel);

	}
	public JScrollBar getHorizontalScrollBar() {
		return horizontalScrollBar;
	}
	public JScrollBar getVerticalScrollBar() {
		return horizontalScrollBar;
	}
	public void notifyImageInfo(String info) {
		// TODO Auto-generated method stub
		
	}
	public void notifyTotalBytes(int bytes) {
		// TODO Auto-generated method stub
		
	}
	public JPanel getMiniView()
	{
		return imagePanel.showViewFrame();
	}
}
