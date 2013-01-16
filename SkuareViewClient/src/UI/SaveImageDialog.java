package UI;

import j2k.ImageOutput;
import j2k.ImagePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import kdu_jni.Jp2_family_tgt;
import kdu_jni.Jpx_codestream_target;
import kdu_jni.Jpx_target;
import kdu_jni.KduException;

public class SaveImageDialog extends JPanel implements ActionListener{

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
