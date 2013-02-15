package j2k;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * ImageLayer
 * This class creates an Image Layer with a pixel recolouring, however this may be no longer used as
 * there is problems in re-displaying the new image after recolouring that have not yet been resolved
 * 
 * @author Dylan McCarthy
 * @since 14/2/2013
 */
@SuppressWarnings("serial")
public class ImageLayer extends JPanel{
	private BufferedImage image;
	private ImagePanel oImage;
	private int pixelWidth;
	private int pixelHeight;
	
	/**
	 * Constructor
	 * This constructs the Image Layer and recolours the pixels based off an automatic algorithm
	 * The algorithm colours the pixels based off their intensity.
	 * 
	 * @param in	BufferedImage
	 * @param base	ImagePanel
	 */
	public ImageLayer(BufferedImage in,ImagePanel base)
	{
		//Image to be modified
		image = in;
		//Base ImagePanel;
		oImage = base;
		//Image Width, Height
		pixelWidth = image.getWidth();
		pixelHeight = image.getHeight();
		int intensity = 0;
		int lowest = 255;
		int highest = 0;
		
		int threshold1 = 0;
		int threshold2 = 0;
		
		for(int i =0; i < pixelWidth;i++)
		{
			for(int j = 0; j < pixelHeight;j++)
			{
				//Separate out ARGB values
				int pixel = image.getRGB(i, j);
				@SuppressWarnings("unused")
				int pixelnum = pixel & 0xFF;
				@SuppressWarnings("unused")
				int alpha = (pixel >> 24) & 0xFF;
				int red = ((pixel >> 16) & 0xFF) ;
				int green = ((pixel >> 8) & 0xFF);
				int blue = ((pixel >> 0) & 0xFF);
				intensity = (red+green+blue)/3;
				//Record Highest and Lowest Intensity values
				if(intensity > highest)
					highest = intensity;
				if(intensity < lowest)
					lowest = intensity;
			}
		}
		int thres_diff = highest/3;
		threshold1 = highest - thres_diff;
		threshold2 = threshold1 - thres_diff;
		
		//Iterate over each pixel
		for(int i =0; i < pixelWidth;i++)
		{
			for(int j = 0; j < pixelHeight;j++)
			{
				//Separate out ARGB values
				int pixel = image.getRGB(i, j);
				@SuppressWarnings("unused")
				int pixelnum = pixel & 0xFF;
				@SuppressWarnings("unused")
				int alpha = (pixel >> 24) & 0xFF;
				int red = ((pixel >> 16) & 0xFF) ;
				int green = ((pixel >> 8) & 0xFF);
				int blue = ((pixel >> 0) & 0xFF);
				intensity = (red+green+blue)/3;
				//Record Highest and Lowest Intensity values
				/*if(intensity > highest)
					highest = intensity;
				if(intensity < lowest)
					lowest = intensity;
				*/
				//Debug output
				//System.out.println("Pixel: " + pixelnum + " Red: " + red + " Green: " + green + " Blue: " + blue + " Alpha: " + alpha);
				
				//Split Intensity mapping between colours - TODO: Create dynamic way of assigning these variables
				if(intensity > threshold1)
				{
					Color c = new Color(intensity,0,0);
					image.setRGB(i,j,c.getRGB());
				}
				else
				if( intensity >= threshold2)
				{
					Color c = new Color(0,intensity+(255-threshold1),0);
					image.setRGB(i, j, c.getRGB());
				}
				else
				if(intensity < threshold2)
				{
					Color c = new Color(0,0,intensity+(255-threshold2));
					image.setRGB(i, j, c.getRGB());
				}
				
			}
			
		}
		
		//System.out.println("Highest Value: " + highest + " Lowest Value: " + lowest);
	}
	/**
	 * Paint Panel
	 */
	public void paint(Graphics g)
	{
		g.drawImage(image, 0,0,null);
	}
	/**
	 * Return ImagePanel
	 * @return	ImagePanel
	 */
	public ImagePanel getImagePanel()
	{
		return oImage;
	}
}
