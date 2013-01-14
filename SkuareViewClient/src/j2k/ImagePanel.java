package j2k;

import java.awt.Color;
import java.awt.Image;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.BorderLayout;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ComponentEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseMotionListener;

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;


@SuppressWarnings("serial")
public class ImagePanel extends JPanel implements 
ComponentListener, AdjustmentListener,
MouseListener, MouseMotionListener,
InternalFrameListener
{
	private ImageInput j2kImage;
	private ImageView mainView;
	private ImageView miniView;

	private int incrX, incrY;
	private int pressX, pressY;
	private boolean panelMoving;
	private boolean mousePressed;
	private Rectangle miniViewRect;

	private boolean zoomMode;
	private JPanel miniViewPanel;
	private JScrollBar verScroll;
	private JScrollBar horScroll;
	private ImageWindow parentWindow;
	private JPanel miniViewFrame;

	private Cursor zoomCursor;
	private Cursor openHandCursor;
	private Cursor closedHandCursor;

	private int miniViewWidth;
	private int miniViewHeight;


	public ImagePanel(ImageWindow imageWindow)
	{
		//Set initial Values
		zoomMode = false;
		panelMoving = false;
		incrX = 0; incrY = 0;
		mousePressed = false;
		miniViewFrame = null;
		parentWindow = imageWindow;

		miniViewWidth = 250;
		miniViewHeight = 250;

		mainView = null;
		j2kImage = new ImageInput();

		addMouseListener(this);
		addComponentListener(this);
		addMouseMotionListener(this);

		if(parentWindow == null) {
			horScroll = null;
			verScroll = null;

		} else {
			horScroll = parentWindow.getHorizontalScrollBar();
			verScroll = parentWindow.getVerticalScrollBar();
		}

		if(horScroll != null) {
			horScroll.addMouseListener(this);
			horScroll.addAdjustmentListener(this);
		}

		if(verScroll != null) {
			verScroll.addMouseListener(this);
			verScroll.addAdjustmentListener(this);
		}

		zoomCursor = null;
		openHandCursor = null;
		closedHandCursor = null;
	}

	public void setZoomCursor(Cursor cur)
	{
		zoomCursor = cur;
		if(zoomMode) setCursor(cur);
	}

	public void setOpenHandCursor(Cursor cur)
	{
		openHandCursor = cur;
		if(!zoomMode) setCursor(cur);
	}

	public void setClosedHandCursor(Cursor cur)
	{
		closedHandCursor = cur;
	}
	//Open image function
	public void openImage(String fname) throws Exception
	{

		incrX = 0;
		incrY = 0;
		mainView = null;
		zoomMode = false;
		panelMoving = false;
		mousePressed = false;

		j2kImage.open(fname);

		Dimension size = new Dimension(320,200);

		mainView = j2kImage.createView(size.width, size.height);
		mainView.addObserver(this);
		mainView.make();

	}

	public boolean getZoomMode()
	{
		return zoomMode;
	}

	public void setZoomMode(boolean mode)
	{
		zoomMode = mode;
		if(zoomMode) { if(zoomCursor != null) setCursor(zoomCursor); }
		else { if(openHandCursor != null) setCursor(openHandCursor); }
	}

	public Dimension getImageSize()
	{
		return new Dimension(j2kImage.getWidth(), j2kImage.getHeight());
	}

	public Dimension getImageRealSize()
	{
		return new Dimension(j2kImage.getRealWidth(), j2kImage.getRealHeight());
	}
	public BufferedImage getBufferedImage()
	{
		return mainView.getImage();
	}

	public double getScale()
	{
		return (100.0 / (double)(1 << mainView.getResolution()));
	}

	public JPanel showViewFrame()
	{
		if(!j2kImage.isOpened()) return null;

		if(miniViewFrame == null) {
			miniView = j2kImage.createView(miniViewWidth, miniViewWidth);
			miniView.addObserver(this);

			miniViewFrame = new JPanel();

			double mainViewScale = mainView.getScale();
			double miniViewScale = miniView.getScale();

			miniViewRect = new Rectangle(
					(int)((mainView.getX() / mainViewScale) * miniViewScale),
					(int)((mainView.getY() / mainViewScale) * miniViewScale),
					(int)((mainView.getWidth() / mainViewScale) * miniViewScale),
					(int)((mainView.getHeight() / mainViewScale) * miniViewScale)
					);


			if(miniViewRect.width >= miniView.getWidth()) miniViewRect.width = miniView.getWidth() - 1;
			if(miniViewRect.height >= miniView.getHeight()) miniViewRect.height = miniView.getHeight() - 1;

			miniViewPanel = new JPanel() {
				public void paint(Graphics g)
				{
					Image img = miniView.getImage();
					if(img != null) g.drawImage(img, 0, 0, this);

					g.setColor(Color.red);
					g.drawRect(miniViewRect.x, miniViewRect.y, miniViewRect.width, miniViewRect.height);
				}

				public Dimension getPreferredSize()
				{
					return miniView.getSize();
				}
			};

			miniViewPanel.addMouseListener(this);
			miniViewPanel.addMouseMotionListener(this);

			
			miniViewFrame.setLayout(new BorderLayout());
			miniViewFrame.add(miniViewPanel, BorderLayout.CENTER);

			miniViewFrame.setVisible(true);

			miniView.make();
		}

		//miniViewFrame.setLocation(5, 5);
		return miniViewFrame;
	}

	public void reloadImage() 
	{
		paintComponent(this.getGraphics());
	}

	protected void paintComponent(Graphics g)
	{
		Dimension size = getSize();

		g.setColor(Color.black);
		g.fillRect(0, 0, size.width, size.height);

		if(mainView != null)
			g.drawImage(mainView.getImage(), incrX, incrY, this);
	}

	public void mouseDragged(MouseEvent e)
	{
		if(mainView == null) return;

		if(e.getSource() == this) {
			if(zoomMode) return;

			incrX += (e.getX() - pressX);
			incrY += (e.getY() - pressY);

			Rectangle imageROI = mainView.getBounds();

			int minX = mainView.getImageWidth() - (imageROI.x + imageROI.width);
			if(incrX > imageROI.x) incrX = imageROI.x;
			if(incrX < -minX) incrX = -minX;

			int minY = mainView.getImageHeight() - (imageROI.y + imageROI.height);
			if(incrY > imageROI.y) incrY = imageROI.y;
			if(incrY < -minY) incrY = -minY;

			if(horScroll != null) horScroll.setValue(imageROI.x - incrX);
			if(verScroll != null) verScroll.setValue(imageROI.y - incrY);

			pressX = e.getX();
			pressY = e.getY();

			updateMiniView();
			repaint();

		} else if(e.getSource() == miniViewPanel) {
			if(panelMoving) {
				double mainViewScale = mainView.getScale();
				double miniViewScale = miniView.getScale();

				Rectangle imageROI = mainView.getBounds();

				incrX -= (int)(((double)(e.getX() - pressX) / miniViewScale) * mainViewScale);
				incrY -= (int)(((double)(e.getY() - pressY) / miniViewScale) * mainViewScale);

				int minX = mainView.getImageWidth() - (imageROI.x + imageROI.width);
				if(incrX > imageROI.x) incrX = imageROI.x;
				if(incrX < -minX) incrX = -minX;

				int minY = mainView.getImageHeight() - (imageROI.y + imageROI.height);
				if(incrY > imageROI.y) incrY = imageROI.y;
				if(incrY < -minY) incrY = -minY;

				if(horScroll != null) horScroll.setValue(imageROI.x - incrX);
				if(verScroll != null) verScroll.setValue(imageROI.y - incrY);

				pressX = e.getX();
				pressY = e.getY();

				updateMiniView();
				repaint();
			}
		}
	}

	public void mouseMoved(MouseEvent e)
	{
		if(e.getSource() == miniViewPanel) {
			if(miniViewRect.contains(e.getPoint()) && (openHandCursor != null))
				miniViewPanel.setCursor(openHandCursor);
			else
				miniViewPanel.setCursor(Cursor.getDefaultCursor());
		}
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	public void mouseReleased(MouseEvent e)
	{
		if(mainView == null) return;

		if(e.getSource() == this) {
			if(zoomMode) return;
			else {
				if(openHandCursor != null) setCursor(openHandCursor);
				panelMoving = false;
			}

		} else if(e.getSource() == miniViewPanel) {
			if(openHandCursor != null) miniViewPanel.setCursor(openHandCursor);
			panelMoving = false;
		}

		mainView.setLocation(mainView.getX() - incrX, mainView.getY() - incrY);
		adjustProgressBars();

		mousePressed = false;

		incrX = 0;
		incrY = 0;
	}

	@SuppressWarnings("static-access")
	public void mousePressed(MouseEvent e)
	{
		if(mainView == null) return;

		pressX = e.getX();
		pressY = e.getY();

		mousePressed = true;

		if(e.getSource() == miniViewPanel) {
			if(miniViewRect.contains(e.getPoint())) {
				if(closedHandCursor != null) miniViewPanel.setCursor(closedHandCursor);
				panelMoving = true;
			}

		} if(e.getSource() == this) {
			if(!zoomMode) {
				if(closedHandCursor != null) setCursor(closedHandCursor);
				panelMoving = true;

			} else {
				mousePressed = false;

				Dimension size = getSize();
				if((e.getModifiers() & e.BUTTON1_MASK) != 0)
					mainView.changeResolution(pressX, pressY, size.width, size.height, -1);
				else if((e.getModifiers() & e.BUTTON3_MASK) != 0)
					mainView.changeResolution(pressX, pressY, size.width, size.height, 1);

				adjustProgressBars();
				updateMiniView();
				repaint();

				if(parentWindow != null) {
					Dimension imageSize = getImageRealSize();
					parentWindow.notifyImageInfo("Image: " + imageSize.width + "x" + imageSize.height + "  Scale: " + getScale() + "%");
				}
			}
		}
	}

	public void adjustZoom()
	{
		
	}
	private void adjustProgressBars()
	{
		Rectangle imageROI = mainView.getBounds();

		if(horScroll != null) {
			horScroll.setMinimum(0);
			horScroll.setMaximum(j2kImage.getWidth());
			horScroll.setValue(imageROI.x);
			horScroll.setVisibleAmount(imageROI.width);
		}

		if(verScroll != null) {
			verScroll.setMinimum(0);
			verScroll.setMaximum(j2kImage.getHeight());
			verScroll.setValue(imageROI.y);
			verScroll.setVisibleAmount(imageROI.height);
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		if(panelMoving) return;

		if(!mousePressed) {
			//horScroll.setValue(mainView.getX());
			//verScroll.setValue(mainView.getY());

		} else {
			if(e.getAdjustable() == horScroll) incrX = mainView.getX() - e.getValue();
			else incrY = mainView.getY() - e.getValue();

			updateMiniView();
			repaint();
		}
	}

	public void updateMiniView()
	{
		if(miniView == null) return;

		double mainViewScale = mainView.getScale();
		double miniViewScale = miniView.getScale();

		miniViewRect = new Rectangle(
				(int)(((mainView.getX() - incrX) / mainViewScale) * miniViewScale),
				(int)(((mainView.getY() - incrY) / mainViewScale) * miniViewScale),
				(int)((mainView.getWidth() / mainViewScale) * miniViewScale),
				(int)((mainView.getHeight() / mainViewScale) * miniViewScale)
				);

		if(miniViewRect.width >= miniView.getWidth()) miniViewRect.width = miniView.getWidth() - 1;
		if(miniViewRect.height >= miniView.getHeight()) miniViewRect.height = miniView.getHeight() - 1;

		miniViewPanel.repaint();
	}

	public void componentResized(ComponentEvent e)
	{
		if(mainView == null) return;

		Dimension newSize = getSize();
		mainView.setSize(newSize.width, newSize.height);
		adjustProgressBars();
		updateMiniView();
	}

	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}

	public void internalFrameActivated(InternalFrameEvent e) {}
	public void internalFrameClosing(InternalFrameEvent e) {}
	public void internalFrameDeactivated(InternalFrameEvent e) {}
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameOpened(InternalFrameEvent e) {}

	public void internalFrameClosed(InternalFrameEvent e) 
	{
		if(j2kImage.getActualView() == miniView) mainView.make();
		miniViewFrame = null;
		miniView = null;
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
	{
		if((mainView != null) && (img == mainView.getImage())) {
			if(infoflags != ImageObserver.ALLBITS) repaint(x, y, width, height);

		} else if((miniView != null) && (img == miniView.getImage())) {
			if(infoflags == ImageObserver.ALLBITS) mainView.make();
			else miniViewPanel.repaint(x, y, width, height);
		}

		if((parentWindow != null)  && (j2kImage.isRemote()))
			parentWindow.notifyTotalBytes(j2kImage.getReader().getReadData());

		return false;
	}

	public void setMiniViewSize(Dimension size)
	{
		miniViewWidth = size.width;
		miniViewHeight = size.height;
	}

	public Dimension getMiniViewSize()
	{
		return new Dimension(miniViewWidth, miniViewHeight);
	}
	public boolean CheckMainView()
	{
		if(mainView != null)
		{
			return true;
		}
		else
			return false;
	}

}