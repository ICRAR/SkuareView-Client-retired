package UI;

import java.awt.CardLayout;
import java.awt.Container;

/**
 * Custom container for the Image window
 * 
 * @author dmccarthy
 * @since 7/2/2013
 */
@SuppressWarnings("serial")
public class ImageContainer extends Container {
	/**
	 * Creates new ImageContainer and sets the layout as a CardLayout
	 */
	public ImageContainer() {
		setLayout(new CardLayout(0, 0));
	}
	
	
}
