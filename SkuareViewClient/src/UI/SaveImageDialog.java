package UI;

import j2k.ImageOutput;
import j2k.ImagePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import kdu_jni.Jp2_family_tgt;
import kdu_jni.Jpx_codestream_target;
import kdu_jni.Jpx_target;
import kdu_jni.KduException;

public class SaveImageDialog extends JPanel implements ActionListener{

	public SaveImageDialog(ImagePanel img)
	{
		JFileChooser fc = new JFileChooser();
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
