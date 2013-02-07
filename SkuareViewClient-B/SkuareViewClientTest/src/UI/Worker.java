package UI;

import java.awt.CardLayout;
import java.awt.Component;
import javax.swing.SwingWorker;
import org.noos.xing.mydoggy.Content;

import Engine.ImagePanel;

public class Worker extends SwingWorker<String,ImagePanel> {

	@SuppressWarnings("unused")
	private ImagePanel panel;
	private String action;
	private Boolean activated;
	@SuppressWarnings("unused")
	private toolbox tb;
	private Content content;
	@SuppressWarnings("unused")
	private String id;

	public Worker(ImagePanel panel,String action,boolean activated)
	{
		this.panel = panel;
		this.action = action;
		this.activated = activated;
	}
	public Worker(ImagePanel panel, String action, toolbox tb,String id) {

		this.panel = panel;
		this.action = action;
		this.tb = tb;
		this.id = id;
	}
	public Worker(Content content,String action,boolean activated)
	{
		this.content = content;
		this.action = action;
		this.activated = activated;
	}
	@SuppressWarnings("unused")
	@Override
	protected String doInBackground() throws Exception {
		if(action.compareTo("zoom")==0)
		{
			if(!activated)
			{
				//panel.setZoomMode(true);
			}
			else
			{
				//panel.setZoomMode(false);
			}
		}
		else
			if(action.compareTo("miniview")==0)
			{
				//tb.setMiniView(panel.showViewFrame(),id);
			}
			else
				if(action.compareTo("layer")==0)
				{
					ImagePanel img = null;
					//ImageLayer layer = null;
					final ImageContainer container = (ImageContainer)content.getComponent();
					Component[] comps = container.getComponents();
					//Find either ImagePanel or ImageLayer
					for(int i = 0; i<comps.length;i++)
					{
						if(comps[i].getClass() == ImagePanel.class)
							img = (ImagePanel)comps[i];
						//if(comps[i].getClass() == ImageLayer.class)
							//layer = (ImageLayer)comps[i];
					}
					CardLayout layout = (CardLayout)container.getLayout();
					if(activated)
					{
						//Create new ImageLayer
						/*
						if(layer == null)
						{
							layer = new ImageLayer(img.getBufferedImage(),img);
							layer.setVisible(true);
							container.add(layer,"Pixel Filter");

						}
						layout.show(container, "Pixel Filter");
						*/
					}
					else
					{
						//container.remove(layer);
						layout.show(container, "image");
					}
				}
		return null;
	}



}
