package j2k;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.*;

/**
 * Outputs an Image to a standard image format
 * @author Dylan McCarthy
 * @since 14/02/2013
 */
public class ImageOutput {

	/**
	 * Save output to file
	 * 
	 * @param img BufferedImage
	 * @param ext String
	 * @param file File
	 * @return boolean
	 */
	public static boolean Save(BufferedImage img, String ext,File file)
	{
		boolean sucess = false;
		try {
           sucess = ImageIO.write(img, ext, file);  // ignore returned boolean
        } catch(IOException e) {
            System.out.println("Write error for " + file.getPath() +
                               ": " + e.getMessage());
        }
		return sucess;
	}
	
}
