package j2k;


import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

/**
 * This class ties together all the required classes and functions required to open an image
 * and return the created ImagePanel ready to be used in the GUI
 * 
 * @author dmccarthy
 * @since 14/02/2013
 */
@SuppressWarnings("serial")
public class OpenImage extends JFrame implements ImageWindow  {

	private static String imageName;
	private static ImagePanel imagePanel;
	private static JFrame frame;

	
	/**
	 * Open a image from a filename/pathname
	 * 
	 * @param fname This is the input string representing the input's path
	 * @return Returns an ImagePanel object
	 */
	public static JPanel open(String fname)
	{
		//Create new OpenImage instance
		OpenImage open = new OpenImage();
		imageName = fname;
		//Create base ImagePanel
		imagePanel = new ImagePanel(open);
		imagePanel.setName(fname);
		//If path is not empty
		if(fname != null)
		{
			try{
				//Open image to get data
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
		//Return ImagePanel
		return imagePanel;
	}
	/**
	 * Initializes base variables and creates instance of the class
	 */
	public OpenImage()
	{
		//Set base frame
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		//Create new image panel
		imagePanel = new ImagePanel(this);
		imagePanel.setBounds(0, 0, 571, 518);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setBounds(0, 23, 587, 538);
		centerPanel.setLayout(null);
		centerPanel.add(imagePanel);
		frame.getContentPane().add(centerPanel);

	}
	public void notifyImageInfo(String info) {
		
		
	}
	public void notifyTotalBytes(int bytes) {
		
		
	}
	public JPanel getMiniView()
	{
		return imagePanel.showViewFrame();
	}
	@Override
	public JScrollBar getHorizontalScrollBar() {
		
		return null;
	}
	@Override
	public JScrollBar getVerticalScrollBar() {
		return null;
	}
}
