package j2k;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.LinkedList;


/**
 * This class creates the ImageView for an image,
 * this includes creating the region of interest.
 * 
 * @author Dylan McCarthy
 * @since 14/02/2013
 */
public class ImageView {

	private int discardLevels;          
	private int[] imageBuffer;          
	private BufferedImage image;        
	private Rectangle imageROI;         
	private Rectangle newImageROI;      
	private ArrayList<ImageObserver> observers;        
	private ImageInput imageInput;          
	boolean completed;                  
	boolean contentCompleted;           
	private LinkedList<Rectangle> regionsList;     
	private int imageWidth;             
	private int imageHeight; 

	/**
	 * Creating a new ImageView requires an ImageInput, Rectangle representing the region of interest
	 * and an integer to specify the resolution
	 * 
	 * @param imageIn ImageInput
	 * @param roi Rectangle
	 * @param resolution int
	 */
	public ImageView(ImageInput imageIn, Rectangle roi, int resolution)
	{
		//Initialize variables
		image = null;
		completed = false;
		imageInput = imageIn;
		discardLevels = resolution;
		observers = new ArrayList<ImageObserver>();

		regionsList = new LinkedList<Rectangle>();

		imageROI = new Rectangle(0, 0, 0, 0);
		newImageROI = new Rectangle(0, 0, 0, 0);

		//Check if image is being transfered remotely
		contentCompleted = !imageInput.isRemote();

		//Create imageWidth and Height
		imageWidth = (int)Math.ceil((double)imageInput.getRealWidth() / (double)(1 << discardLevels));
		imageHeight = (int)Math.ceil((double)imageInput.getRealHeight() / (double)(1 << discardLevels));

		//Create ROI
		if(roi != null) {
			imageROI.setBounds(roi);
			newImageROI.setBounds(roi);

		} else {
			imageROI.setBounds(0, 0, imageWidth, imageHeight);
			newImageROI.setBounds(0, 0, imageWidth, imageHeight);
		}

		//Add ROI to list of regions
		regionsList.add(imageROI);
	}
	/**
	 * Creates image
	 */
	public void make()
	{
		//Stop decoding, change ROI to match current ROI, resume decoding
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

	public LinkedList<Rectangle> getRegionsToDecode()
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

	/**
	 * Sets this ImageView to complete
	 */
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

	/**
	 * Set new bounds for image
	 * @param newRect Rectangle
	 */
	public void setBounds(Rectangle newRect)
	{
		//Stop decoding, create new Bounds, change ROI, resume decoding
		imageInput.stopDecoding();
		newImageROI.setBounds(newRect);
		changeROI();
		imageInput.startDecoding(this);
	}

	/**
	 * Change location of ROI
	 * 
	 * @param x int
	 * @param y int
	 */
	public void setLocation(int x, int y)
	{
		//Stop decoding, create new location, change ROI, resume decoding
		imageInput.stopDecoding();
		newImageROI.setLocation(x, y);
		changeROI();
		imageInput.startDecoding(this);
	}
	/**
	 * Change size of ROI
	 * @param width int
	 * @param height int
	 */
	public void setSize(int width, int height)
	{
		//Stop decoding, create new Size, change ROI, resume decoding
		imageInput.stopDecoding();
		newImageROI.setSize(width, height);
		changeROI();
		imageInput.startDecoding(this);
	}

	/**
	 * Change resolution of ROI
	 * 
	 * @param px int
	 * @param py int 
	 * @param width int
	 * @param height int
	 * @param resIncr int 
	 */
	public void changeResolution(int px, int py, int width, int height, int resIncr)
	{
		//Find quality level
		int newDiscardLevels = discardLevels + resIncr;

		//Check if at highest quality level
		if(newDiscardLevels < 0) newDiscardLevels = 0;
		if(newDiscardLevels > imageInput.getMaxDiscardLevels()) newDiscardLevels = imageInput.getMaxDiscardLevels();

		//If unchanged return
		if(discardLevels == newDiscardLevels) return;

		//Reset Image
		image = null;
		//Create new x and y values
		
		px = (imageROI.x + px) * (1 << discardLevels);
		py = (imageROI.y + py) * (1 << discardLevels);

		discardLevels = newDiscardLevels;

		px /= (1 << discardLevels);
		py /= (1 << discardLevels);

		px -= (width / 2);
		if(width % 2 != 0) px--;
		py -= (height / 2);
		if(height % 2 != 0) py--;

		//Set image Width and Height
		
		imageWidth = (int)Math.ceil((double)imageInput.getRealWidth() / (double)(1 << discardLevels));
		imageHeight = (int)Math.ceil((double)imageInput.getRealHeight() / (double)(1 << discardLevels));

		//Stop decoding
		imageInput.stopDecoding();

		//Create new ROI
		newImageROI.setBounds(px, py, width, height);
		changeROI();

		//Start decoding
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

	/**
	 * Changes the ROI
	 */
	private void changeROI()
	{
		//Check if not changed or if image empty
		if(newImageROI.equals(imageROI) && (image != null)) return;

		//Check if image is being transfered
		contentCompleted = !imageInput.isRemote();
		completed = false;

		//Create ROI width, height, x and y values
		if(newImageROI.width > imageWidth) newImageROI.width = imageWidth;
		if(newImageROI.height > imageHeight) newImageROI.height = imageHeight;
		if(newImageROI.x < 0) newImageROI.x = 0;
		if(newImageROI.y < 0) newImageROI.y = 0;

		//Scale with base image
		if(newImageROI.x + newImageROI.width > imageWidth)
			newImageROI.x -= ((newImageROI.x + newImageROI.width) - imageWidth);
		if(newImageROI.y + newImageROI.height > imageHeight)
			newImageROI.y -= ((newImageROI.y + newImageROI.height) - imageHeight);

		//Create new buffered Image
		BufferedImage newImage = new BufferedImage(newImageROI.width, newImageROI.height, BufferedImage.TYPE_INT_RGB);
		int[] newImageBuffer = ((DataBufferInt)newImage.getRaster().getDataBuffer()).getData();

		//If image contains no data or not all data present, add region to list
		if((image == null) || !imageROI.intersects(newImageROI)) {
			regionsList.clear();
			regionsList.add(newImageROI);

		} else {
			//Copy previous ROI and add to list
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

		//Set new image as Image and set bounds
		image = newImage;
		imageBuffer = newImageBuffer;
		imageROI.setBounds(newImageROI);
	}

	/**
	 * Update new region
	 * @param region Rectangle
	 * @param regionBuffer int[]
	 */
	public void updateNewRegion(Rectangle region, int regionBuffer[])
	{
		//Check if imageBuffer is empty
		if(imageBuffer == null) return;

		//if ROI intersects region in list
		if(imageROI.intersects(region)) {

			//Copy ROI from list
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
	

