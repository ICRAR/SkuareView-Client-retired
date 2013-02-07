package Engine;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

import javax.swing.JPanel;

import kdu_jni.KduException;
import kdu_jni.Kdu_coords;

@SuppressWarnings("serial")
public class ImagePanel extends JPanel
{
	private Image img;
	int[] img_buf;
	MemoryImageSource image_source;
	private Kdu_coords _res;
	private ImageView mainView;

	public ImagePanel(Kdu_coords view_size) throws KduException
	{
		
		_res = view_size;
		this.setSize(view_size.Get_x(), view_size.Get_y());
		setPreferredSize(new Dimension(view_size.Get_x(),view_size.Get_y()));
		this.setBounds(0, 0, view_size.Get_x(), view_size.Get_y());

	}

	public void updateSize(Kdu_coords view_size) throws KduException
	{
		setPreferredSize(new Dimension(view_size.Get_x(),view_size.Get_y()));
	}
	public void paint(Graphics g)
	{
		if (img != null)
			g.drawImage(img,0,0,this);
	}

	/// <summary>
	/// Copies the supplied region to the `image_source' and updates the
	/// corresponding region on the panel.
	/// </summary>
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
	public void clearImg()
	{
		img = null;
	}
	public Kdu_coords getRes()
	{
		return _res;
	}
}