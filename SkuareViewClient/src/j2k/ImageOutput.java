package j2k;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.*;

public class ImageOutput {

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
