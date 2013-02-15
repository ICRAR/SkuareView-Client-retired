package UI;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;
import java.awt.Component;


/**
 * Toolbox Menu
 * This holds the implementations of the Zoom, Pixel Recolour and
 * Select Area functions
 * 
 * @author Dylan McCarthy
 * @since 7/2/2013
 */
@SuppressWarnings("serial")
public class toolbox extends JPanel {

	private SkuareViewClient _client;
	public JPanel miniViewPanel;
/**
 * Creates the menu and is passed the client object so that it
 * can pass back states of the toggle buttons
 * 
 * @param client	SkuareViewClient client
 */
	public toolbox(SkuareViewClient client) {
		_client = client;

		final JToggleButton tglbtnTool = new JToggleButton("Zoom");
		tglbtnTool.setBounds(43, 5, 84, 29);
		tglbtnTool.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tglbtnTool.isSelected())
				{
					_client.getContent(false);
				}
				else
				{
					_client.getContent(true);
				}
			}
		});
		setLayout(null);
		tglbtnTool.setVerticalAlignment(SwingConstants.TOP);
		add(tglbtnTool);

		final JToggleButton tglbtnTool_1 = new JToggleButton("Pixel Colour");
		tglbtnTool_1.setBounds(43, 39, 84, 29);
		tglbtnTool_1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(tglbtnTool_1.isSelected())
				{
					_client.showLayer(true);
				}
				else
				{
					_client.showLayer(false);
				}
			}
		});
		add(tglbtnTool_1);

		final JToggleButton tglbtnTool_2 = new JToggleButton("Select");
		tglbtnTool_2.setBounds(43, 73, 84, 29);
		tglbtnTool_2.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if(tglbtnTool_2.isSelected())
				{
					_client.setSelect(true);
				}
				else
				{
					_client.setSelect(false);
				}

			}

		});
		add(tglbtnTool_2);

		miniViewPanel = new JPanel();
		miniViewPanel.setBounds(20, 174, 125, 120);
		miniViewPanel.setVisible(true);
		add(miniViewPanel);
		miniViewPanel.setLayout(new CardLayout(0, 0));

	}
	/**
	 * Creates the MiniView image for the menu panel
	 * 
	 * @param view		JPanel view 	This is the JPanel holding the MiniView image
	 * @param id		String id		This is the id of the content which is checked to make sure the correct miniview is displayed
	 */
	public void setMiniView(JPanel view,String id)
	{
		boolean found = false;
		CardLayout layout = (CardLayout)miniViewPanel.getLayout();
		Component[] comps = miniViewPanel.getComponents();
		for(int i = 0; i < comps.length; i++)
		{
			if(comps[i].equals(view))
				found = true;
		}

		if(!found)
		{
			view.setBounds(miniViewPanel.getBounds());
			miniViewPanel.add(view,id);
			miniViewPanel.setVisible(true);
		}
		layout.show(miniViewPanel, id);
	}
}
