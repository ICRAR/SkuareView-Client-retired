package UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
/**
 * This class creates and displays a custom dialog window for 
 * connecting to a JPIP server
 * @author dmccarthy
 *
 */
@SuppressWarnings("serial")
public class GetUrl extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JComboBox<String> cbox;
	private ArrayList<String> previous;
	private String[] list;
	public String URL;

	/**
	 * Dialog box constructor
	 * Remembers previously opened files through the use of a ArrayList of strings
	 * 
	 * @param prev		ArrayList<String> previously opened urls
	 */
	public GetUrl(ArrayList<String> prev) {
		previous = prev;
		URL = null;
		setBounds(100, 100, 353, 131);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblUrl = new JLabel("URL:");
			lblUrl.setBounds(53, 30, 28, 16);
			contentPanel.add(lblUrl);
		}
		{
			if(!previous.isEmpty())
			{
				list = new String[previous.size()];
				previous.toArray(list);
			}
			else
			{
			list = new String[1];
			}
			cbox = new JComboBox<String>(list);
			cbox.setBounds(93, 24, 210, 27);
			cbox.setEditable(true);
			cbox.setEnabled(true);
			contentPanel.add(cbox);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						URL = (String) cbox.getSelectedItem();
						previous.add(URL);
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	/**
	 * Shows the dialog and returns a string URL
	 * 
	 * @return		String URL
	 */
	public String showDialog()
	{
		
		setVisible(true);
		return URL;
	}

}
