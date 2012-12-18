package UI;
import info.clearthought.layout.TableLayout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.ContentManager;
import org.noos.xing.mydoggy.ContentManagerUIListener;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.FloatingTypeDescriptor;
import org.noos.xing.mydoggy.RepresentativeAnchorDescriptor;
import org.noos.xing.mydoggy.SlidingTypeDescriptor;
import org.noos.xing.mydoggy.TabbedContentManagerUI;
import org.noos.xing.mydoggy.TabbedContentUI;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowManager;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.event.ContentManagerUIEvent;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;


public class SkuareViewClient  {

	private JFrame frame;
	private ToolWindowManager toolWindowManager;
	private int index;
	private String imageName;
	
	protected void run()
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				setUp();
				start();
			}
		});
	}
	protected void setUp()
	{
		init();
		initToolWindowManager();
	}
	protected void start()
	{
		ToolWindow debugTool = toolWindowManager.getToolWindow("Menu");
		debugTool.setActive(true);
		index = 1;
		frame.setVisible(true);
	}
	protected void init()
	{
		this.frame = new JFrame("SkuareView Client 0.1");
		this.frame.setSize(640,480);
		this.frame.setLocation(100,100);
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem openMenuitem = new JMenuItem("Open");
		openMenuitem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				GetUrl getUrl = new GetUrl();
				getUrl.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
				imageName = getUrl.showDialog();
				createNewWindow(j2k.OpenImage.open(imageName));
			}
		});
		fileMenu.add(openMenuitem);
		JMenuItem exitMenuitem = new JMenuItem("Exit");
		exitMenuitem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				frame.setVisible(false);
				frame.dispose();
			}
		});
		fileMenu.add(exitMenuitem);
		
		menubar.add(fileMenu);
		this.frame.setJMenuBar(menubar);
		
		this.frame.getContentPane().setLayout(new TableLayout(new double[][]{{0,-1,0},{0,-1,0}}));
		
	}
	protected void initToolWindowManager()
	{
		MyDoggyToolWindowManager twManager = new MyDoggyToolWindowManager();
		this.toolWindowManager = twManager;
		toolbox tb = new toolbox(this);
		toolWindowManager.registerToolWindow("Menu", "", null, tb,ToolWindowAnchor.RIGHT);
		toolWindowManager.registerToolWindow("Console","",null,new JPanel(),ToolWindowAnchor.BOTTOM);
		
		for(ToolWindow window : toolWindowManager.getToolWindows())
			window.setAvailable(true);
		
		initContentManager();
		
		this.frame.getContentPane().add(twManager,"1,1,");
	}
	protected void setupDebugTool()
	{
		ToolWindow debugTool = toolWindowManager.getToolWindow("Menu");
		debugTool.setVisible(true);
		
		RepresentativeAnchorDescriptor<ToolWindow> representativeAnchorDescriptor = debugTool.getRepresentativeAnchorDescriptor();
		representativeAnchorDescriptor.setPreviewEnabled(true);
		representativeAnchorDescriptor.setPreviewDelay(1500);
		representativeAnchorDescriptor.setPreviewTransparentRatio(0.4f);
		
		DockedTypeDescriptor dockedTypeDescriptor = (DockedTypeDescriptor) debugTool.getTypeDescriptor(ToolWindowType.DOCKED);
		dockedTypeDescriptor.setAnimating(true);
		dockedTypeDescriptor.setHideRepresentativeButtonOnVisible(true);
		dockedTypeDescriptor.setDockLength(300);
		dockedTypeDescriptor.setPopupMenuEnabled(true);

		dockedTypeDescriptor.setAnimating(true);
		
		SlidingTypeDescriptor slidingTypeDescriptor = (SlidingTypeDescriptor) debugTool.getTypeDescriptor(ToolWindowType.SLIDING);
		slidingTypeDescriptor.setEnabled(true);
		slidingTypeDescriptor.setTransparentMode(true);
		slidingTypeDescriptor.setTransparentRatio(0.8f);
		slidingTypeDescriptor.setTransparentDelay(0);
		slidingTypeDescriptor.setAnimating(true);
		
		FloatingTypeDescriptor floatingTypeDescriptor = (FloatingTypeDescriptor) debugTool.getTypeDescriptor(ToolWindowType.FLOATING);
		floatingTypeDescriptor.setEnabled(true);
		floatingTypeDescriptor.setLocation(150, 200);
		floatingTypeDescriptor.setSize(320, 200);
		floatingTypeDescriptor.setModal(false);
		floatingTypeDescriptor.setTransparentMode(true);
		floatingTypeDescriptor.setTransparentRatio(0.2f);
		floatingTypeDescriptor.setTransparentDelay(1000);
		floatingTypeDescriptor.setAnimating(true);
	}
	protected void initContentManager(){
		JPanel imageContent = new JPanel();
		ContentManager contentManager = toolWindowManager.getContentManager();
		Content content = contentManager.addContent("Image Window","Image Title",null,imageContent);
		content.setToolTipText("Image");
		
		setupContentManagerUI();
	}
	protected void createNewWindow(JPanel newWindow)
	{
		String id = imageName + index;
		String title = imageName;
		ContentManager contentManager = toolWindowManager.getContentManager();
		Content content = contentManager.addContent(id,title,null,newWindow);
		content.setToolTipText(title);
		index++;
	}
	protected void setupContentManagerUI()
	{
		TabbedContentManagerUI contentManagerUI = (TabbedContentManagerUI)toolWindowManager.getContentManager().getContentManagerUI();
		contentManagerUI.setShowAlwaysTab(true);
		contentManagerUI.setTabPlacement(TabbedContentManagerUI.TabPlacement.TOP);
		contentManagerUI.addContentManagerUIListener(new ContentManagerUIListener(){
			public boolean contentUIRemoving(ContentManagerUIEvent event){
				return JOptionPane.showConfirmDialog(frame, "Are you sure?") == JOptionPane.OK_OPTION;
			}
			public void contentUIDetached(ContentManagerUIEvent event){
				
			}
		});
		TabbedContentUI contentUI = (TabbedContentUI) toolWindowManager.getContentManager().getContent(0).getContentUI();
		
		contentUI.setCloseable(true);
		contentUI.setDetachable(true);
		contentUI.setTransparentMode(true);
		contentUI.setTransparentRatio(0.7f);
		contentUI.setTransparentDelay(1000);
	}
	public static void main(String[] args) {
		SkuareViewClient client = new SkuareViewClient();
		try{
			client.run();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void getContent()
	{
		ContentManager contManager = toolWindowManager.getContentManager();
		Content selected = contManager.getSelectedContent();
		System.out.println(selected.getId());
	}

}

