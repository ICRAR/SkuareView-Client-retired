package UI;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



public class toolbox extends JPanel {

	private SkuareViewClient _client;
	/**
	 * Create the panel.
	 */
	public toolbox(SkuareViewClient client) {
		_client = client;
		setLayout(null);
		
		final JToggleButton tglbtnTool = new JToggleButton("Tool 1");
		tglbtnTool.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(tglbtnTool.isSelected())
				{
					_client.getContent();
				}
				else
				{
					System.out.println("Not Selected");
				}
			}
		});
		tglbtnTool.setBounds(43, 5, 84, 29);
		tglbtnTool.setVerticalAlignment(SwingConstants.TOP);
		add(tglbtnTool);
		
		JToggleButton tglbtnTool_1 = new JToggleButton("Tool 2");
		tglbtnTool_1.setBounds(43, 39, 84, 29);
		add(tglbtnTool_1);
		
		JToggleButton tglbtnTool_2 = new JToggleButton("Tool 3");
		tglbtnTool_2.setBounds(43, 73, 84, 29);
		add(tglbtnTool_2);

	}

}
