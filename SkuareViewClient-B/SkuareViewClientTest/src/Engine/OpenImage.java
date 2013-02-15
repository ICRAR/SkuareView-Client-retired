package Engine;

/**
 * This class manages the functions that are required to open an image
 * and return a ImagePanel object
 * @author Dylan McCarthy
 * @since 14/02/2013
 *
 */
public class OpenImage {

	private static ImagePanel imagePanel;
	
	/**
	 * Takes an input path and opens the image
	 * 
	 * @param fname - Input String
	 * @return Returns an ImagePanel object
	 */
	@SuppressWarnings("unused")
	public static ImagePanel open(String fname)
	{
		String imageName = fname;
		if(fname != null)
		{
			ImageInput input = new ImageInput();
			try {
				input.open(fname);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			imagePanel = input.getDisplay();
			imagePanel.validate();
			imagePanel.setVisible(true);
		}
		
		return imagePanel;
	}
}
