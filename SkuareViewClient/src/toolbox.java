import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;



public class toolbox extends JPanel {

	/**
	 * Create the panel.
	 */
	public toolbox() {
		setLayout(null);
		
		JToggleButton tglbtnTool = new JToggleButton("Tool 1");
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
