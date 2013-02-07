package UI;

import j2k.ImageOutput;
import j2k.ImagePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;


/**
 * Creates a custom Save Dialog which saves the currently viewed image to a
 * standard image format.
 * @author dmccarthy
 * @since 7/2/2013
 */
@SuppressWarnings("serial")
public class SaveImageDialog extends JPanel implements ActionListener{

	/**
	 * Creates the Dialog box then saves the file based off the users input
	 * 
	 * @param img		ImagePanel img 	This is the current image area being viewed
	 */
	public SaveImageDialog(ImagePanel img)
	{
		JFileChooser fc = new JFileChooser();
		FileFilter filter = new ExtensionFileFilter("JPG,JPEG,BMP or PNG",new String[]{"JPG","JPEG","BMP","PNG"});
		fc.setFileFilter(filter);
		int retVal = fc.showSaveDialog(this);
		if(retVal == JFileChooser.APPROVE_OPTION)
		{
			File file = fc.getSelectedFile();
			String filename = file.getName();
			String ext = filename.substring(filename.indexOf('.')+1);
			if(ext.compareTo("jpg")==0||ext.compareTo("png")==0||ext.compareTo("bmp")==0)
			{
				ImageOutput.Save(img.getBufferedImage(), ext,file);
			}
			else
			{
				
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {

	}


}
/**
 * Custom Extension FileFilter
 * 
 * @author dmccarthy
 * @since 7/2/2013
 */
class ExtensionFileFilter extends FileFilter {
	  String description;

	  String extensions[];

	  public ExtensionFileFilter(String description, String extension) {
	    this(description, new String[] { extension });
	  }

	  public ExtensionFileFilter(String description, String extensions[]) {
	    if (description == null) {
	      this.description = extensions[0];
	    } else {
	      this.description = description;
	    }
	    this.extensions = (String[]) extensions.clone();
	    toLower(this.extensions);
	  }

	  private void toLower(String array[]) {
	    for (int i = 0, n = array.length; i < n; i++) {
	      array[i] = array[i].toLowerCase();
	    }
	  }

	  public String getDescription() {
	    return description;
	  }

	  public boolean accept(File file) {
	    if (file.isDirectory()) {
	      return true;
	    } else {
	      String path = file.getAbsolutePath().toLowerCase();
	      for (int i = 0, n = extensions.length; i < n; i++) {
	        String extension = extensions[i];
	        if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
	          return true;
	        }
	      }
	    }
	    return false;
	  }
	}
