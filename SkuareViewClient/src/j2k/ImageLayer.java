package j2k;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageLayer extends JPanel{
	private BufferedImage image;
	private ImagePanel oImage;
	private int pixelWidth;
	private int pixelHeight;
	
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
		//Iterate over each pixel
		for(int i =0; i < pixelWidth;i++)
		{
			for(int j = 0; j < pixelHeight;j++)
			{
				//Separate out ARGB values
				int pixel = image.getRGB(i, j);
				int pixelnum = pixel & 0xFF;
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
				
				//Debug output
				//System.out.println("Pixel: " + pixelnum + " Red: " + red + " Green: " + green + " Blue: " + blue + " Alpha: " + alpha);
				
				//Split Intensity mapping between colours - TODO: Create dynamic way of assigning these variables
				if(intensity > 170)
				{
					Color c = new Color(intensity,0,0);
					image.setRGB(i,j,c.getRGB());
				}
				else
				if( intensity >= 130)
				{
					Color c = new Color(0,intensity,0);
					image.setRGB(i, j, c.getRGB());
				}
				else
				if(intensity < 130)
				{
					Color c = new Color(0,0,intensity);
					image.setRGB(i, j, c.getRGB());
				}
				
			}
			
		}
		
		//System.out.println("Highest Value: " + highest + " Lowest Value: " + lowest);
	}
	public void paint(Graphics g)
	{
		g.drawImage(image, 0,0,null);
	}
	public ImagePanel getImagePanel()
	{
		return oImage;
	}
}
