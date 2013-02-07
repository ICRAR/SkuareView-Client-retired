package Engine;


public class OpenImage {

	private static ImagePanel imagePanel;
	
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
