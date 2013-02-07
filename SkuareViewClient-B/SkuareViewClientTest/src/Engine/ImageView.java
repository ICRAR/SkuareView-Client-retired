package Engine;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.LinkedList;

public class ImageView {

	private int discardLevels;
	private int[] imageBuffer;
	private BufferedImage image;
	private Rectangle imageROI;
	private Rectangle newImageROI;
	private ArrayList<ImageObserver> observers;
	boolean completed;
	boolean contentCompleted;
	private LinkedList<Rectangle> regionsList;
	private int imageWidth;
	private int imageHeight;
	private ImagePanel imagePanel;
	private int scale;

	public ImageView(ImagePanel img, Rectangle roi, int resolution,int scale)
	{
		image = null;
		completed = false;
		imagePanel = img;
		discardLevels = resolution;
		this.scale = scale;
		observers = new ArrayList<ImageObserver>();

		regionsList = new LinkedList<Rectangle>();

		imageROI = new Rectangle(0,0,0,0);
		newImageROI = new Rectangle(0,0,0,0);

		imageWidth = imagePanel.getWidth();
		imageHeight = imagePanel.getHeight();

		if(roi != null)
		{
			imageROI.setBounds(roi);
			newImageROI.setBounds(roi);
		}
		else
		{
			imageROI.setBounds(0,0,imageWidth,imageHeight);
			newImageROI.setBounds(0,0,imageWidth,imageHeight);
		}

		regionsList.add(imageROI);

	}
	private void changeROI()
	{
		if(newImageROI.equals(imageROI) && (image != null)) return;

		completed = false;
		if(newImageROI.width > imageWidth) newImageROI.width = imageWidth;
		if(newImageROI.height > imageHeight) newImageROI.height = imageHeight;
		if(newImageROI.x < 0) newImageROI.x = 0;
		if(newImageROI.y < 0) newImageROI.y = 0;

		if(newImageROI.x + newImageROI.width > imageWidth)
			newImageROI.x -= ((newImageROI.x + newImageROI.width) - imageWidth);
		if(newImageROI.y + newImageROI.height > imageHeight)
			newImageROI.y -= ((newImageROI.y + newImageROI.height) - imageHeight);

		BufferedImage newImage = new BufferedImage(newImageROI.width, newImageROI.height, BufferedImage.TYPE_INT_RGB);
		int[] newImageBuffer = ((DataBufferInt)newImage.getRaster().getDataBuffer()).getData();

		if((image == null) || !imageROI.intersects(newImageROI)) {
			regionsList.clear();
			regionsList.add(newImageROI);

		} else {
			Rectangle copyRect = imageROI.intersection(newImageROI);

			int orgX = copyRect.x - imageROI.x;
			int orgY = copyRect.y - imageROI.y;
			int destX = copyRect.x - newImageROI.x;
			int destY = copyRect.y - newImageROI.y;

			int orgPos = (orgX + (orgY * imageROI.width));
			int destPos = (destX + (destY * newImageROI.width));

			for(int j = 0; j < copyRect.height; j++) {
				System.arraycopy(imageBuffer, orgPos, newImageBuffer, destPos, copyRect.width);
				destPos += newImageROI.width;
				orgPos += imageROI.width;
			}

			regionsList.clear();
			regionsList.add(newImageROI);
		}

		image = newImage;
		imageBuffer = newImageBuffer;
		imageROI.setBounds(newImageROI);

	}
	public void setLocation(int x, int y)
	{
		newImageROI.setLocation(x,y);
		changeROI();
	}
	public void setSize(int width, int height)
	{
		newImageROI.setSize(width, height);
		changeROI();
	}
	public int getX()
	{
		return imageROI.x;
	}
	public int getY()
	{
		return imageROI.y;
	}
	public int getHeight()
	{
		return imageROI.height;
	}
	public int getWidth()
	{
		return imageROI.width;
	}
	public int getScale()
	{
		return scale;
	}
}
