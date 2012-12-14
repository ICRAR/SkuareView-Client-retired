package Test;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

import java.awt.BorderLayout;

import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import javax.swing.JDesktopPane;
import javax.swing.border.BevelBorder;
import java.awt.FlowLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;

@SuppressWarnings("serial")
public class GUI extends JFrame implements ImageWindow {

	private JFrame frame;
	private ImagePanel imagePanel;
	private JScrollBar verticalScrollBar;
	private JScrollBar horizontalScrollBar;
	public String imageName;
	private JLabel statusTxt;
	private JLabel bytesTransmitted;
	private double prev;
	private double prevTime;
	private double rate;
	private double lastUpdate;
	private static JPanel menu;
	private JMenu FileMenu;


	/**
	 * Create the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setSize(800,600);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JPanel infobar = new JPanel();
		infobar.setBounds(0, 562, 800, 16);
		infobar.setLayout(new BorderLayout());
		infobar.add(statusTxt = new JLabel("No Image"),BorderLayout.WEST);
		infobar.add(bytesTransmitted = new JLabel(""),BorderLayout.EAST);
		frame.getContentPane().add(infobar);


		verticalScrollBar = new JScrollBar(JScrollBar.VERTICAL,0,0,0,180);
		verticalScrollBar.setBounds(571, 0, 15, 518);
		horizontalScrollBar = new JScrollBar(JScrollBar.HORIZONTAL,0,0,0,180);
		horizontalScrollBar.setBounds(0, 520, 586, 15);

		verticalScrollBar.setVisible(false);
		horizontalScrollBar.setVisible(false);

		imagePanel = new ImagePanel(this);
		imagePanel.setBounds(0, 0, 571, 518);
		JPanel centerPanel = new JPanel();
		centerPanel.setBounds(0, 23, 587, 538);
		centerPanel.setLayout(null);
		centerPanel.add(imagePanel);
		centerPanel.add(verticalScrollBar);
		centerPanel.add(horizontalScrollBar);
		frame.getContentPane().add(centerPanel);

		menu = new JPanel();
		menu.setBounds(587, 23, 213, 538);
		frame.getContentPane().add(menu);

		JButton Refresh = new JButton("Refresh");
		Refresh.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				imagePanel.reloadImage();
			}
		});

		menu.add(Refresh);

		final JButton ZoomMode = new JButton("Zoom");
		ZoomMode.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e)
			{

				if(!imagePanel.getZoomMode())
				{
					imagePanel.setZoomMode(true);
					ZoomMode.setForeground(Color.RED);
				}
				else
				{
					imagePanel.setZoomMode(false);
					ZoomMode.setForeground(Color.BLACK);
				}
			}
		});
		menu.add(ZoomMode);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 800, 22);
		frame.getContentPane().add(menuBar);

		FileMenu = new JMenu("File");
		menuBar.add(FileMenu);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GetUrl getUrl = new GetUrl();
				getUrl.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
				imageName = getUrl.showDialog();
				openImage(imageName);
			}
		});
		FileMenu.add(mntmOpen);

		JMenuItem mntmClose = new JMenuItem("Close");
		FileMenu.add(mntmClose);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		FileMenu.add(mntmExit);

	}
	public void openImage(String fname)
	{
		imageName = fname;
		if(fname != null)
		{
			try{
				verticalScrollBar.setVisible(true);
				horizontalScrollBar.setVisible(true);

				imagePanel.openImage(imageName);
				Dimension imageSize = imagePanel.getImageRealSize();
				statusTxt.setText("Image: " + imageName + " " + imageSize.width + "x" + imageSize.height + " Scale: "+ imagePanel.getScale() + "% ");

			}
			catch(Exception ex)
			{
				System.err.println("Error loading the image:");
				ex.printStackTrace();

				JOptionPane.showMessageDialog(null, 
						"It was impossible to load the image '" + imageName + "'", 
						"Error loading image", 
						JOptionPane.ERROR_MESSAGE);



				statusTxt.setText("No image");
				bytesTransmitted.setText("");

				verticalScrollBar.setVisible(false);
				horizontalScrollBar.setVisible(false);
			}
		}
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
		double current;
		double timeElasped;

		double timeNow = System.nanoTime();
		timeElasped = timeNow - prevTime;
		double seconds = timeElasped / 1000000000.0;

		if(bytes == 0) 
		{
			bytesTransmitted.setText("");
			rate = 0;
			lastUpdate = 0;
		}
		else
		{
			current = (double) bytes/1000.0;
			if(current != prev);
			{
				if(seconds > 0.01)
				{
					rate = round((current/prev)/seconds,2);
				}
			}

			bytesTransmitted.setText("Rate: "+ rate + "KB/s   " + "[" + String.valueOf((double)bytes/1000.0) + "KBs]  ");

		}
		prev = (double)bytes/1000.0;
		prevTime = System.nanoTime();

	}
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
	public static JPanel getMenu()
	{
		return menu;
	}
}
