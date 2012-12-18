package j2k;

import java.awt.Dimension;
import java.awt.Point;
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
	private ArrayList observers;        
	private ImageInput imageInput;          
	boolean completed;                  
	boolean contentCompleted;           
	private LinkedList regionsList;     
	private int imageWidth;             
	private int imageHeight; 

	public ImageView(ImageInput imageIn, Rectangle roi, int resolution)
	{
		image = null;
		completed = false;
		imageInput = imageIn;
		discardLevels = resolution;
		observers = new ArrayList();

		regionsList = new LinkedList();

		imageROI = new Rectangle(0, 0, 0, 0);
		newImageROI = new Rectangle(0, 0, 0, 0);

		contentCompleted = !imageInput.isRemote();

		imageWidth = (int)Math.ceil((double)imageInput.getRealWidth() / (double)(1 << discardLevels));
		imageHeight = (int)Math.ceil((double)imageInput.getRealHeight() / (double)(1 << discardLevels));

		if(roi != null) {
			imageROI.setBounds(roi);
			newImageROI.setBounds(roi);

		} else {
			imageROI.setBounds(0, 0, imageWidth, imageHeight);
			newImageROI.setBounds(0, 0, imageWidth, imageHeight);
		}

		regionsList.add(imageROI);
	}
	public void make()
	{
		if(!completed) {
			imageInput.stopDecoding();
			changeROI();
			imageInput.startDecoding(this);
		}
	}

	public int getImageWidth()
	{
		return imageWidth;
	}

	public int getImageHeight()
	{
		return imageHeight;
	}

	public int getResolution()
	{
		return discardLevels;
	}

	public LinkedList getRegionsToDecode()
	{
		return regionsList;
	}

	public boolean isCompleted()
	{
		return completed;
	}

	public boolean isContentCompleted()
	{
		return contentCompleted;
	}

	public void setContentCompleted()
	{
		contentCompleted = true;
	}

	public void setCompleted()
	{
		int numObservers = observers.size();
		for(int i = 0; i < numObservers; i++) {
			((ImageObserver)observers.get(i)).imageUpdate(image, ImageObserver.ALLBITS, 0, 0, 0, 0);
		}

		completed = true;
	}

	public void addObserver(ImageObserver observer)
	{
		observers.add(observer);
	}

	public void removeObserver(ImageObserver observer)
	{
		observers.remove(observer);
	}

	public void setBounds(Rectangle newRect)
	{
		imageInput.stopDecoding();
		newImageROI.setBounds(newRect);
		changeROI();
		imageInput.startDecoding(this);
	}

	public void setLocation(int x, int y)
	{
		imageInput.stopDecoding();
		newImageROI.setLocation(x, y);
		changeROI();
		imageInput.startDecoding(this);
	}

	public void setSize(int width, int height)
	{
		imageInput.stopDecoding();
		newImageROI.setSize(width, height);
		changeROI();
		imageInput.startDecoding(this);
	}

	public void changeResolution(int px, int py, int width, int height, int resIncr)
	{
		int newDiscardLevels = discardLevels + resIncr;

		if(newDiscardLevels < 0) newDiscardLevels = 0;
		if(newDiscardLevels > imageInput.getMaxDiscardLevels()) newDiscardLevels = imageInput.getMaxDiscardLevels();

		if(discardLevels == newDiscardLevels) return;

		image = null;

		px = (imageROI.x + px) * (1 << discardLevels);
		py = (imageROI.y + py) * (1 << discardLevels);

		discardLevels = newDiscardLevels;

		px /= (1 << discardLevels);
		py /= (1 << discardLevels);

		px -= (width / 2);
		if(width % 2 != 0) px--;
		py -= (height / 2);
		if(height % 2 != 0) py--;

		imageWidth = (int)Math.ceil((double)imageInput.getRealWidth() / (double)(1 << discardLevels));
		imageHeight = (int)Math.ceil((double)imageInput.getRealHeight() / (double)(1 << discardLevels));

		imageInput.stopDecoding();

		newImageROI.setBounds(px, py, width, height);
		changeROI();

		imageInput.startDecoding(this);
	}

	public int getX() 
	{ 
		return imageROI.x; 
	}

	public int getY() 
	{ 
		return imageROI.y; 
	}

	public int getWidth() 
	{ 
		return imageROI.width; 
	}

	public int getHeight() 
	{ 
		return imageROI.height; 
	}

	public Dimension getSize() 
	{ 
		return imageROI.getSize(); 
	}

	public Point getLocation() 
	{ 
		return imageROI.getLocation(); 
	}

	public Rectangle getBounds() 
	{ 
		return new Rectangle(imageROI); 
	}

	private void changeROI()
	{
		if(newImageROI.equals(imageROI) && (image != null)) return;

		contentCompleted = !imageInput.isRemote();
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

	public void updateNewRegion(Rectangle region, int regionBuffer[])
	{
		if(imageBuffer == null) return;

		if(imageROI.intersects(region)) {

			Rectangle copyRect = imageROI.intersection(region);

			int orgX = copyRect.x - region.x;
			int orgY = copyRect.y - region.y;
			int destX = copyRect.x - imageROI.x;
			int destY = copyRect.y - imageROI.y;

			int orgPos = (orgX + (orgY * region.width));
			int destPos = (destX + (destY * imageROI.width));

			for(int j = 0; j < copyRect.height; j++) {
				System.arraycopy(regionBuffer, orgPos, imageBuffer, destPos, copyRect.width);
				orgPos += region.width;
				destPos += imageROI.width;
			}

			int numObservers = observers.size();
			for(int i = 0; i < numObservers; i++)
				((ImageObserver)observers.get(i)).imageUpdate(image, 0, destX, destY, copyRect.width, copyRect.height);
		}
	}

	public BufferedImage getImage()
	{
		return image;
	}

	public double getScale()
	{
		return (100.0 / (double)(1 << discardLevels));
	}
}
	

