package Engine;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

import kdu_jni.KduException;
import kdu_jni.Kdu_coords;

/**
 * This class defines the JPanel that holds the image data to be displayed as well as some basic functionality for event handling
 * 
 * @author Dylan McCarthy
 * @since 14/02/2013
 *
 */
@SuppressWarnings("serial")
public class ImagePanel extends JPanel
{
	private Image img;
	int[] img_buf;
	MemoryImageSource image_source;
	private Kdu_coords _res;
	@SuppressWarnings("unused")
	private ImageView mainView;

	/**
	 * Creates a new ImagePanel based off of a Kdu_coords object
	 * 
	 * @param view_size - KDU_coords object specifies the size
	 * @throws KduException
	 */
	public ImagePanel(Kdu_coords view_size) throws KduException
	{
		_res = view_size;
		this.setSize(view_size.Get_x(), view_size.Get_y());
		setPreferredSize(new Dimension(view_size.Get_x(),view_size.Get_y()));
		this.setBounds(0, 0, view_size.Get_x(), view_size.Get_y());

	}
	/**
	 * Update the size of the ImagePanel
	 * @param view_size - Kdu_coords object specifies the size
	 * @throws KduException
	 */
	public void updateSize(Kdu_coords view_size) throws KduException
	{
		setPreferredSize(new Dimension(view_size.Get_x(),view_size.Get_y()));
	}
	/**
	 * Paint the image to the GUI
	 * @param g
	 */
	public void paint(Graphics g)
	{
		if (img != null)
			g.drawImage(img,0,0,this);
	}

	/**
	 * Copies the supplied region to the `image_source' and updates the corresponding region on the panel.
	 * 
	 * @param view_width - width of the view
	 * @param view_height - height of the view
	 * @param reg_width - region width
	 * @param reg_height - region height
	 * @param reg_off_x - region offset x
	 * @param reg_off_y - region offset y
	 * @param reg_buf - region buffer
	 */
	public void put_region(int view_width, int view_height,
			int reg_width, int reg_height,
			int reg_off_x, int reg_off_y,
			int[] reg_buf)
	{
		if (img == null)
		{
			setPreferredSize(new Dimension(view_width,view_height));
			img_buf = new int[view_width*view_height];
			image_source = new MemoryImageSource(view_width,view_height,
					img_buf,0,view_width);
			image_source.setAnimated(true);
			img = createImage(image_source);
		}

		int dest_idx = reg_off_x + reg_off_y*view_width;
		int src_idx = 0;
		int extra_row_gap = view_width - reg_width;
		int i, j;
		for (j=0; j < reg_height; j++, dest_idx+=extra_row_gap)
			for (i=0; i < reg_width; i++, src_idx++, dest_idx++)
				img_buf[dest_idx] = reg_buf[src_idx];
		image_source.newPixels(reg_off_x,reg_off_y,reg_width,reg_height);
		repaint(reg_off_x,reg_off_y,reg_width,reg_height);
	}
	/**
	 * Clear the image
	 */
	public void clearImg()
	{
		img = null;
	}
	/**
	 * Get the resolution of the image
	 * @return Returns the Kdu_coords that correspond with the resolution
	 */
	public Kdu_coords getRes()
	{
		return _res;
	}
}