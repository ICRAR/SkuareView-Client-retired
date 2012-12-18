package j2k;
import javax.swing.JScrollBar;


/**
 *
 * The interface ImageWindow must be implemented by all windows that
 * contain a ImagePanel object.
 *
 **/
public interface ImageWindow
{
  /**
   * Returns the horizontal scroll bar of the window, associated to
   * the image.
   **/
  public JScrollBar getHorizontalScrollBar();
  
  /**
   * Returns the vertical scroll bar of the window, associated to 
   * the image.
   */
  public JScrollBar getVerticalScrollBar();
  
  /**
   * The ImagePanel object notifies to the window some image information.
   */
  public void notifyImageInfo(String info);
  
  /**
   * The ImagePanel object notifies to the window the total received bytes.
   */
  public void notifyTotalBytes(int bytes);
}