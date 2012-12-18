package j2k;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.JToggleButton;


public class HTTP_Test extends JFrame implements ActionListener, ImageWindow {

	private ImagePanel imagePanel;
	private JScrollBar verticalScrollBar;
	private JScrollBar horizontalScrollBar;
	private String imageName;
	private final int BUTTON_ZOOM = 0;
	private final int BUTTON_VIEW = 1;
	private final int BUTTON_REFRESH = 2;

	private AbstractButton buttons[] = new AbstractButton[3];
	private String buttonIcons[] = {"Zoom","View","Refresh"};

	private String buttonTexts[] = {"Enable/disable zoom mode", "Show/hide mini-view", "Reload image"};

	private JLabel statusTxt;
	private JTextField addressTxt;
	private JLabel bytesTransmittedLabel;

	public void Start()
	{
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		contentPane.add(centerPanel, BorderLayout.CENTER);

		verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL,0,0,0,180);
		horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL,0,0,0,180);

		verticalScrollBar.setVisible(false);
		horizontalScrollBar.setVisible(false);

		imagePanel = new ImagePanel(this);

		centerPanel.add(imagePanel,BorderLayout.CENTER);


		centerPanel.add(verticalScrollBar,BorderLayout.EAST);
		centerPanel.add(horizontalScrollBar,BorderLayout.SOUTH);	

		JPanel panelButton = new JPanel();
		panelButton.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 3));


		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		for(int i = 0; i < buttons.length; i++) {
			if(i == BUTTON_ZOOM) buttons[i] = new JToggleButton();
			else buttons[i] = new JButton();

			buttons[i].setRolloverEnabled(true);
			buttons[i].setFocusPainted(false);
			buttons[i].setBorderPainted(false);
			buttons[i].setContentAreaFilled(false);
			buttons[i].setMargin(new Insets(0, 0, 0, 0));
			buttons[i].setBorder(BorderFactory.createEmptyBorder());
			buttons[i].addActionListener(this);
			buttons[i].setToolTipText(buttonTexts[i]);
			panelButton.add(buttons[i]);
		}

		panel.add(panelButton, BorderLayout.NORTH);
		panelButton.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.NORTH);

		JPanel downPanel = new JPanel();
		downPanel.setLayout(new BorderLayout());
		downPanel.add(statusTxt = new JLabel("No image"), BorderLayout.WEST);
		downPanel.add(bytesTransmittedLabel = new JLabel(""), BorderLayout.EAST);

		downPanel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(downPanel, BorderLayout.SOUTH);

		buttons[BUTTON_ZOOM].setEnabled(false);
		buttons[BUTTON_VIEW].setEnabled(false);
		buttons[BUTTON_REFRESH].setEnabled(false);
		buttons[BUTTON_REFRESH].setVisible(false);
	}
	@Override
	public JScrollBar getHorizontalScrollBar() {
		return horizontalScrollBar;
	}

	@Override
	public JScrollBar getVerticalScrollBar() {
		return verticalScrollBar;
	}
	@Override
	public void notifyImageInfo(String info) {
		statusTxt.setText(info);
	}

	@Override
	public void notifyTotalBytes(int bytes) {
		if(bytes == 0) bytesTransmittedLabel.setText("");
		else bytesTransmittedLabel.setText("[" + String.valueOf((double)bytes / 1000.0) + " KBs]");
	}
	public void openImage(String fname)
	{
		imageName = fname;
		try{
			verticalScrollBar.setVisible(true);
			horizontalScrollBar.setVisible(true);
			buttons[BUTTON_ZOOM].setSelected(false);

			imagePanel.openImage(imageName);

			Dimension imageSize = imagePanel.getImageRealSize();
			statusTxt.setText("Image: " + imageSize.width + "x" + imageSize.height 
					+ "  Scale: " + imagePanel.getScale() + "%");

			buttons[BUTTON_ZOOM].setEnabled(true);
			buttons[BUTTON_VIEW].setEnabled(true);
			buttons[BUTTON_REFRESH].setEnabled(true);
			imagePanel.setZoomMode(true);
		}catch(Exception ex)
		{
			System.err.println("Error loading the image:");
			ex.printStackTrace();

			JOptionPane.showMessageDialog(null, 
					"It was impossible to load the image '" + imageName + "'", 
					"Error loading image", 
					JOptionPane.ERROR_MESSAGE);



			statusTxt.setText("No image");
			bytesTransmittedLabel.setText("");

			verticalScrollBar.setVisible(false);
			horizontalScrollBar.setVisible(false);

			buttons[BUTTON_ZOOM].setEnabled(false);
			buttons[BUTTON_VIEW].setEnabled(false);
			buttons[BUTTON_REFRESH].setEnabled(false);
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		 if(e.getSource() == buttons[BUTTON_ZOOM]) {
		      imagePanel.setZoomMode(!imagePanel.getZoomMode());

		    } else if(e.getSource() == buttons[BUTTON_VIEW]) {
		      imagePanel.showViewFrame();

		    } else if(e.getSource() == buttons[BUTTON_REFRESH]) {
		      imagePanel.reloadImage();
		    } 

	}
}
