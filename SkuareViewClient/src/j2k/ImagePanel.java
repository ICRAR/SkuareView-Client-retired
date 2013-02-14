package j2k;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
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

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * This class defines the JPanel on which an image is stored, as well as event functions
 * such as the click and drag movement and zooming.
 * 
 * @author dmccarthy
 * @since 14/02/2013
 */
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
	private Rectangle selectedArea;
	private Point startPoint;
	private Point endPoint;
	private boolean zoomMode;
	private boolean selectMode;
	private JPanel miniViewPanel;
	private ImageWindow parentWindow;
	private JPanel miniViewFrame;

	private Cursor zoomCursor;
	private Cursor openHandCursor;
	private Cursor closedHandCursor;

	private int miniViewWidth;
	private int miniViewHeight;


	/**
	 * Given an ImageWindow which defines the parent of the ImagePanel
	 * the ImagePanel is created as well as a smaller panel which creates the 
	 * miniView.
	 * 
	 * @param ImageWindow imageWindow
	 */
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
		//Get input
		j2kImage = new ImageInput();

		//Add Listeners
		addMouseListener(this);
		addComponentListener(this);
		addMouseMotionListener(this);

		//Add Cursors (yet to be implemented)
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
	/**
	 * This method takes in a string with the image name, creates the base image
	 * then assigns the image to the panel
	 * 
	 * @param fname
	 * @throws Exception
	 */
	public void openImage(String fname) throws Exception
	{

		incrX = 0;
		incrY = 0;
		mainView = null;
		zoomMode = false;
		panelMoving = false;
		mousePressed = false;

		//Create base image
		j2kImage.open(fname);

		//Set default initial size
		Dimension size = new Dimension(320,200);

		//Create view
		mainView = j2kImage.createView(size.width, size.height);
		mainView.addObserver(this);
		mainView.make();

	}

	public boolean getZoomMode()
	{
		return zoomMode;
	}
	public boolean getSelectMode()
	{
		return selectMode;
	}

	/**
	 * This sets the ImagePanel's click behavior to zoom mode
	 * 
	 * @param mode boolean
	 */
	public void setZoomMode(boolean mode)
	{
		zoomMode = mode;
		if(zoomMode) { if(zoomCursor != null) setCursor(zoomCursor); }
		else { if(openHandCursor != null) setCursor(openHandCursor); }
	}
	public void setSelectMode(boolean mode)
	{
		selectMode = mode;


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
	/**
	 * this function creates the miniView for the image which is used 
	 * to help navigation of the image.
	 * 
	 * @return JPanel
	 */
	public JPanel showViewFrame()
	{
		//Check if the base image is open
		if(!j2kImage.isOpened()) return null;

		//If miniViewFrame is null then create new miniView image from base image
		if(miniViewFrame == null) {
			miniView = j2kImage.createView(miniViewWidth, miniViewWidth);
			miniView.addObserver(this);

			miniViewFrame = new JPanel();

			double mainViewScale = mainView.getScale();
			double miniViewScale = miniView.getScale();
			//Create miniView image
			miniViewRect = new Rectangle(
					(int)((mainView.getX() / mainViewScale) * miniViewScale),
					(int)((mainView.getY() / mainViewScale) * miniViewScale),
					(int)((mainView.getWidth() / mainViewScale) * miniViewScale),
					(int)((mainView.getHeight() / mainViewScale) * miniViewScale)
					);

			//Resize image to fit within the frame limits
			if(miniViewRect.width >= miniView.getWidth()) miniViewRect.width = miniView.getWidth() - 1;
			if(miniViewRect.height >= miniView.getHeight()) miniViewRect.height = miniView.getHeight() - 1;

			//Create the new panel and paint it
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

			//Add Listeners
			miniViewPanel.addMouseListener(this);
			miniViewPanel.addMouseMotionListener(this);

			//Add to Panel to frame
			miniViewFrame.setLayout(new BorderLayout());
			miniViewFrame.add(miniViewPanel, BorderLayout.CENTER);
			miniViewFrame.setVisible(true);
			//Create miniView final image
			miniView.make();
		}

		//return frame
		return miniViewFrame;
	}

	public void reloadImage() 
	{
		paintComponent(this.getGraphics());
	}

	/**
	 * Sets background to Black and also implements a drawing function to allow for 
	 * the selection of an area
	 * 
	 * @param g Graphics
	 */
	protected void paintComponent(Graphics g)
	{
		Dimension size = getSize();

		g.setColor(Color.black);
		g.fillRect(0, 0, size.width, size.height);

		if(mainView != null)
			g.drawImage(mainView.getImage(), incrX, incrY, this);
		if(selectMode)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.setStroke(new BasicStroke(2));

			if(selectedArea != null)
			{
				g2.setPaint(Color.red);
				g2.draw(selectedArea);
			}
			else
				if(startPoint != null && endPoint != null)
				{
					g2.setPaint(Color.blue);
					Rectangle r = MakeRect(startPoint.x,startPoint.y,endPoint.x,endPoint.y);
					g2.draw(r);
				}
		}

	}

	/**
	 * 
	 * This method implements the functions for the mouseDragged event.
	 * This includes Area selection and moving of the image.
	 * 
	 * @param e MouseEvent
	 */
	public void mouseDragged(MouseEvent e)
	{
		//Check if image is Open
		if(mainView == null) return;

		//Check ImagePanel mode
		//If mode is select then draw
		if(selectMode)
		{
			endPoint = new Point(e.getX(),e.getY());
			repaint();
		}
		else
			if(e.getSource() == this) {
				//If zoomMode do nothing
				if(zoomMode) return;

				//Determine location of event
				incrX += (e.getX() - pressX);
				incrY += (e.getY() - pressY);

				//Determine bounds of image
				Rectangle imageROI = mainView.getBounds();

				//Change Image Width
				int minX = mainView.getImageWidth() - (imageROI.x + imageROI.width);
				if(incrX > imageROI.x) incrX = imageROI.x;
				if(incrX < -minX) incrX = -minX;

				//Change Image Height
				int minY = mainView.getImageHeight() - (imageROI.y + imageROI.height);
				if(incrY > imageROI.y) incrY = imageROI.y;
				if(incrY < -minY) incrY = -minY;

				pressX = e.getX();
				pressY = e.getY();
				//Update miniview and repaint
				updateMiniView();
				repaint();

			} 
		//Check if event happened on miniView
			else if(e.getSource() == miniViewPanel)
			{
				//Check if panel is moving
				if(panelMoving) {

					//Get scales of image
					double mainViewScale = mainView.getScale();
					double miniViewScale = miniView.getScale();

					//Find image bounds
					Rectangle imageROI = mainView.getBounds();

					//Find location of event
					incrX -= (int)(((double)(e.getX() - pressX) / miniViewScale) * mainViewScale);
					incrY -= (int)(((double)(e.getY() - pressY) / miniViewScale) * mainViewScale);

					//Change Width
					int minX = mainView.getImageWidth() - (imageROI.x + imageROI.width);
					if(incrX > imageROI.x) incrX = imageROI.x;
					if(incrX < -minX) incrX = -minX;

					//Change Height
					int minY = mainView.getImageHeight() - (imageROI.y + imageROI.height);
					if(incrY > imageROI.y) incrY = imageROI.y;
					if(incrY < -minY) incrY = -minY;

					pressX = e.getX();
					pressY = e.getY();

					//Update Miniview and repaint
					updateMiniView();
					repaint();
				}
			}
	}

	/**
	 * 
	 * This method implements functions for the mouseMoved event
	 * 
	 * @param e MouseEvent
	 */
	public void mouseMoved(MouseEvent e)
	{
		//Detect if cursor is over miniview and change cursor.
		if(e.getSource() == miniViewPanel) {
			if(miniViewRect.contains(e.getPoint()) && (openHandCursor != null))
				miniViewPanel.setCursor(openHandCursor);
			else
				miniViewPanel.setCursor(Cursor.getDefaultCursor());
		}
	}
	//Unused events
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	/**
	 * 
	 * This method implements functions for the mouseReleased event
	 * 
	 * @param e MouseEvent
	 */
	public void mouseReleased(MouseEvent e)
	{
		//Check image is Open
		if(mainView == null) return;

		//If Select mode then finalize rectangle and repaint
		if(selectMode)
		{
			selectedArea = MakeRect(startPoint.x,startPoint.y,e.getX(),e.getY());
			repaint();
		}
		else
		{
			//If source is main view
			if(e.getSource() == this) {
				//if ZoomMode do nothing
				if(zoomMode) return;
				//Reset cursor and set panelMoving to false
				else {
					if(openHandCursor != null) setCursor(openHandCursor);
					panelMoving = false;
				}

			} 
			//If source is miniView
			else if(e.getSource() == miniViewPanel) {
				//reset cursor and set panelMoving to false
				if(openHandCursor != null) miniViewPanel.setCursor(openHandCursor);
				panelMoving = false;
			}

			//Set location of main view based on event
			mainView.setLocation(mainView.getX() - incrX, mainView.getY() - incrY);

			mousePressed = false;

			incrX = 0;
			incrY = 0;
		}
	}

	/**
	 * 
	 * This method implements functions for the mousePressed event
	 * 
	 * @param e MouseEvent
	 */
	@SuppressWarnings("static-access")
	public void mousePressed(MouseEvent e)
	{
		//Check image is open
		if(mainView == null) return;

		//get Location of event
		pressX = e.getX();
		pressY = e.getY();

		//Set mousePressed true
		mousePressed = true;

		//If select mode then create new start point and repaint
		if(selectMode)
		{
			startPoint = new Point(e.getX(),e.getY());
			endPoint = startPoint;
			repaint();
		}
		else
		{
			//If miniView is source change cursor and set panelMoving to true
			if(e.getSource() == miniViewPanel) {
				if(miniViewRect.contains(e.getPoint())) {
					if(closedHandCursor != null) miniViewPanel.setCursor(closedHandCursor);
					panelMoving = true;
				}

			}
			//If source is mainView
			if(e.getSource() == this) {
				//If not zoom mode change cursor and set panelMoving to true
				if(!zoomMode) {
					if(closedHandCursor != null) setCursor(closedHandCursor);
					panelMoving = true;

				}
				//Set mousePressed to False
				else {
					mousePressed = false;

					//Determine size
					Dimension size = getSize();
					//Determine which button pressed and either zoom in or out
					if((e.getModifiers() & e.BUTTON1_MASK) != 0)
						mainView.changeResolution(pressX, pressY, size.width, size.height, -1);
					else if((e.getModifiers() & e.BUTTON3_MASK) != 0)
						mainView.changeResolution(pressX, pressY, size.width, size.height, 1);


					//Update miniView and repaint
					updateMiniView();
					repaint();

				}
			}
		}
	}
	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		if(panelMoving) return;

		if(mousePressed)
		{
			incrY = mainView.getY() - e.getValue();
			updateMiniView();
			repaint();
		}
	}

	/**
	 * 
	 * This method updates the miniView image
	 */
	public void updateMiniView()
	{
		//Check if miniview is open
		if(miniView == null) return;

		//Get scales
		double mainViewScale = mainView.getScale();
		double miniViewScale = miniView.getScale();

		//Create new Rectangle based off base image
		miniViewRect = new Rectangle(
				(int)(((mainView.getX() - incrX) / mainViewScale) * miniViewScale),
				(int)(((mainView.getY() - incrY) / mainViewScale) * miniViewScale),
				(int)((mainView.getWidth() / mainViewScale) * miniViewScale),
				(int)((mainView.getHeight() / mainViewScale) * miniViewScale)
				);

		//Resize to miniView panel
		if(miniViewRect.width >= miniView.getWidth()) miniViewRect.width = miniView.getWidth() - 1;
		if(miniViewRect.height >= miniView.getHeight()) miniViewRect.height = miniView.getHeight() - 1;

		//repaint
		miniViewPanel.repaint();
	}

	/**
	 * 
	 * This method resizes a component
	 */
	public void componentResized(ComponentEvent e)
	{
		//Check if image is open
		if(mainView == null) return;

		//Resize and update miniView
		Dimension newSize = getSize();
		mainView.setSize(newSize.width, newSize.height);
		updateMiniView();
	}

	//Unhandled events
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}

	public void internalFrameActivated(InternalFrameEvent e) {}
	public void internalFrameClosing(InternalFrameEvent e) {}
	public void internalFrameDeactivated(InternalFrameEvent e) {}
	public void internalFrameDeiconified(InternalFrameEvent e) {}
	public void internalFrameIconified(InternalFrameEvent e) {}
	public void internalFrameOpened(InternalFrameEvent e) {}

	/**
	 * 
	 * This method checks if the View for the image is the same as the miniView, if it is
	 * then it creates a new mainView, if not then it removes the miniView
	 */
	public void internalFrameClosed(InternalFrameEvent e) 
	{
		if(j2kImage.getActualView() == miniView) mainView.make();
		miniViewFrame = null;
		miniView = null;
	}

	/**
	 * 
	 * This method updates the image
	 */
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

	/**
	 * 
	 * This method allows for the miniView size to be set
	 * 
	 * @param size Dimension
	 */
	public void setMiniViewSize(Dimension size)
	{
		miniViewWidth = size.width;
		miniViewHeight = size.height;
	}

	/**
	 * 
	 * returns the size of the miniview
	 * 
	 * @return Dimension
	 */
	public Dimension getMiniViewSize()
	{
		return new Dimension(miniViewWidth, miniViewHeight);
	}
	/**
	 * 
	 * checks if main view exists 
	 * 
	 * @return boolean
	 */
	public boolean CheckMainView()
	{
		if(mainView != null)
		{
			return true;
		}
		else
			return false;
	}
	/**
	 * 
	 * This method returns the raw imaged assosiated with this ImagePanel
	 * @return
	 */
	public ImageInput getRaw()
	{
		return j2kImage;
	}
	/**
	 * 
	 * This method creates a new Rectangle object
	 * 
	 * @param x1 int
	 * @param y1 int
	 * @param x2 int
	 * @param y2 int
	 * @return Rectangle
	 */
	public Rectangle MakeRect(int x1, int y1, int x2, int y2)
	{
		return new Rectangle(Math.min(x1, x2), Math.min(y1, y2),Math.abs(x1-x2),Math.abs(y1-y2));
	}

}