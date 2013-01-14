package UI;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;



@SuppressWarnings("serial")
public class toolbox extends JPanel {

	private SkuareViewClient _client;
	public JPanel miniViewPanel;
	/**
	 * Create the panel.
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
		
		JToggleButton tglbtnTool_2 = new JToggleButton("Tool 3");
		tglbtnTool_2.setBounds(43, 73, 84, 29);
		add(tglbtnTool_2);
		
		miniViewPanel = new JPanel();
		miniViewPanel.setBounds(20, 174, 125, 120);
		miniViewPanel.setVisible(true);
		add(miniViewPanel);
		miniViewPanel.setLayout(new BorderLayout(0, 0));

	}
	public void setMiniView(JPanel view)
	{
		view.setBounds(miniViewPanel.getBounds());
		miniViewPanel.add(view,BorderLayout.CENTER);
		miniViewPanel.setVisible(true);
	}
}
