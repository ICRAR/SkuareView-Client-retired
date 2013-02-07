package UI;
import info.clearthought.layout.TableLayout;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import kdu_jni.KduException;
import kdu_jni.Kdu_global;
import kdu_jni.Kdu_message_formatter;
import Engine.Kdu_sysout_message;

import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.ContentManager;
import org.noos.xing.mydoggy.ContentManagerListener;
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
import org.noos.xing.mydoggy.event.ContentManagerEvent;
import org.noos.xing.mydoggy.event.ContentManagerUIEvent;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;

import Engine.ImagePanel;
import Engine.OpenImage;


public class SkuareViewClient  {

	public static String WorkingDir;
	public static String OS;
	private JFrame frame;
	private ToolWindowManager toolWindowManager;
	private toolbox tb;
	private int index;
	private String imageName;
	public ArrayList<String> prev;
	public static Console console;

	//create GUI
	protected void run()
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				setUp();
				start();
			}
		});
	}
	//Initialize
	protected void setUp()
	{
		init();
		initToolWindowManager();
		prev = new ArrayList<String>();
	}
	//Start GUI
	protected void start()
	{
		ToolWindow debugTool = toolWindowManager.getToolWindow("Menu");
		debugTool.setActive(true);
		index = 1;
		frame.setVisible(true);
	}
	protected void init()
	{
		//Create base frame
		this.frame = new JFrame("SkuareView Client 0.3");
		this.frame.setSize(800,600);
		this.frame.setLocation(100,100);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create menu
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem openMenuitem = new JMenuItem("Open");
		//Add function for opening new stream
		openMenuitem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				GetUrl getUrl = new GetUrl(prev);
				getUrl.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
				imageName = getUrl.showDialog();
				if(!prev.contains(imageName))
					prev.add(imageName);
				if(imageName != null && !imageName.isEmpty())
				{
					console.println("Opening: " + imageName);
					createNewWindow(OpenImage.open(imageName));
				}
			}
		});
		fileMenu.add(openMenuitem);
		JMenuItem saveMenuitem = new JMenuItem("Save");
		saveMenuitem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unused")
				SaveImageDialog save = new SaveImageDialog(getImageContent());
			}
		});
		fileMenu.add(saveMenuitem);

		//Add Exit option
		JMenuItem exitMenuitem = new JMenuItem("Exit");
		exitMenuitem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				frame.setVisible(false);
				frame.dispose();
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuitem);
		//Add Menubar to frame
		menubar.add(fileMenu);
		this.frame.setJMenuBar(menubar);

		//Set main layout
		this.frame.getContentPane().setLayout(new TableLayout(new double[][]{{0,-1,0},{0,-1,0}}));

	}
	protected void initToolWindowManager()
	{
		//Create tool window manager
		MyDoggyToolWindowManager twManager = new MyDoggyToolWindowManager();
		this.toolWindowManager = twManager;
		tb = new toolbox(this);
		console = new Console();
		toolWindowManager.registerToolWindow("Menu", "", null, tb,ToolWindowAnchor.RIGHT);
		toolWindowManager.registerToolWindow("Console","",null,console,ToolWindowAnchor.BOTTOM);

		for(ToolWindow window : toolWindowManager.getToolWindows())
			window.setAvailable(true);
		//Initalize content
		initContentManager();

		this.frame.getContentPane().add(twManager,"1,1,");
	}
	protected void setupDebugTool()
	{
		//Setup debug window
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
		//Create new panel
		JPanel imageContent = new JPanel();
		ContentManager contentManager = toolWindowManager.getContentManager();
		Content content = contentManager.addContent("Image Window","Image Title",null,imageContent);
		content.setToolTipText("Image");

		contentManager.addContentManagerListener(new ContentManagerListener(){

			@Override
			public void contentSelected(ContentManagerEvent arg0) {
				ImagePanel img = null;
				Content selected = toolWindowManager.getContentManager().getSelectedContent();
				if(selected != null)
				{
					try{


						ImageContainer cont = (ImageContainer)selected.getComponent();
						Component[] comps = cont.getComponents();

						//Find either ImagePanel
						for(int i = 0; i<comps.length;i++)
						{
							if(comps[i].getClass() == ImagePanel.class)
								img = (ImagePanel)comps[i];
						}
						if(img!=null)
						{
							String name = img.getName();
							Worker worker = new Worker(img,"miniview",tb,name);
							try {
								worker.doInBackground();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
							}
							//tb.setMiniView(img.showViewFrame());
						}
					}
					catch(ClassCastException ex)
					{
						return;
					}
				}
			}
			@Override
			public void contentAdded(ContentManagerEvent arg0) {


			}
			@Override
			public void contentRemoved(ContentManagerEvent arg0) {


			}

		});
		//Set up manager
		setupContentManagerUI();
	}
	protected void createNewWindow(JPanel newWindow)
	{
		//Create new Image panel
		String id = imageName + index;
		String title = imageName;
		ImageContainer container = new ImageContainer();
		newWindow.setVisible(true);
		container.add(BorderLayout.CENTER,newWindow);
		container.setVisible(true);

		ContentManager contentManager = toolWindowManager.getContentManager();
		Content content = contentManager.addContent(id,title,null,container);
		content.setToolTipText(title);
		content.setSelected(true);
		container.setBounds(container.getParent().getBounds());
		index++;

	}
	protected void setupContentManagerUI()
	{
		//Set up tab behaviour
		TabbedContentManagerUI<?> contentManagerUI = (TabbedContentManagerUI<?>)toolWindowManager.getContentManager().getContentManagerUI();
		contentManagerUI.setShowAlwaysTab(true);
		contentManagerUI.setTabPlacement(TabbedContentManagerUI.TabPlacement.TOP);
		//Listen for window event, display popup box for exit
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
		//Main Application method
		 Kdu_sysout_message sysout =
			        new Kdu_sysout_message(false); // Non-throwing message printer
			      Kdu_sysout_message syserr =
			        new Kdu_sysout_message(true); // Exception-throwing message printer
			      Kdu_message_formatter pretty_sysout =
			        new Kdu_message_formatter(sysout); // Non-throwing formatted printer
			      Kdu_message_formatter pretty_syserr =
			        new Kdu_message_formatter(syserr); // Throwing formatted printer

			      
			      try {
			    	  Kdu_global.Kdu_customize_warnings(pretty_sysout);
			    	  Kdu_global.Kdu_customize_errors(pretty_syserr);
				} catch (KduException e1) {
					e1.printStackTrace();
				}
			      
		SkuareViewClient client = new SkuareViewClient();
		try{
			OS = System.getProperty("os.name");
			WorkingDir = System.getProperty("user.dir");
			//System.loadLibrary("kdu_v72R");
			//System.loadLibrary("kdu_a72R");
			if(OS.toUpperCase().contains("MAC"))
			{
				System.load(WorkingDir + "/lib/libkdu_v72R.dylib");
				System.load(WorkingDir + "/lib/libkdu_a72R.dylib");
				System.load(WorkingDir + "/lib/libkdu_v72D.dylib");
				System.load(WorkingDir + "/lib/libkdu_a72D.dylib");
				System.load(WorkingDir + "/lib/libkdu_jni.jnilib");
				System.load(WorkingDir + "/lib/libkdu_jni_dbg.jnilib");
			}
			else
				if(OS.toUpperCase().contains("WIN"))
				{
					System.load(WorkingDir + "/lib/kdu_v72D.dll");
					System.load(WorkingDir + "/lib/kdu_a72D.dll");
					System.load(WorkingDir + "/lib/kdu_jni.dll");

				}
				else
					if(OS.toUpperCase().contains("LINUX"))
					{
						System.load(WorkingDir + "/lib/libkdu_v72R.so");
						System.load(WorkingDir + "/lib/libkdu_a72R.so");
						System.load(WorkingDir + "/lib/libkdu_jni.so");
					}
					else
					{
						System.out.println("Unsupported Operating System Detected");
						System.exit(1);
					}

			//System.out.println(System.getProperty("java.class.path"));
			//System.out.println(System.getProperty("java.library.path"));
			client.run();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void getContent(boolean activated)
	{
		//get content and set zoom mode
		ContentManager contManager = toolWindowManager.getContentManager();
		Content selected = contManager.getSelectedContent();
		if(selected != null)
		{
			ImagePanel img = null;
			if(!activated)
			{
				console.println("Zoom Enabled for " + selected.getId());
			}
			else
			{
				console.println("Zoom Disabled for " + selected.getId());
			}
			//System.out.println(selected.getId());
			ImageContainer container = (ImageContainer)selected.getComponent();
			Component[] comps = container.getComponents();
			//Find the ImagePanel
			for(int i = 0; i<comps.length;i++)
			{
				if(comps[i].getClass() == ImagePanel.class)
					img = (ImagePanel)comps[i];
			}
			Worker worker = new Worker(img,"zoom",activated);
			try {
				worker.doInBackground();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			console.println("Nothing to select");
			//System.out.println("Nothing to select");
		}
	}
	public void showLayer(boolean show)
	{
		//Get Content
		ContentManager contManager = toolWindowManager.getContentManager();
		Content selected = contManager.getSelectedContent();
		if(selected != null)
		{
			Worker worker = new Worker(selected,"layer",show);
			try {
				worker.doInBackground();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	public ImagePanel getImageContent()
	{
		ContentManager contManager = toolWindowManager.getContentManager();
		Content selected = contManager.getSelectedContent();
		ImagePanel img = null;
		if(selected != null)
		{

			ImageContainer cont = (ImageContainer)selected.getComponent();
			Component[] comps = cont.getComponents();
			for(Component c : comps)
			{
				if(c.getClass() == ImagePanel.class){
					img = (ImagePanel)c;
				}
			}
		}
		return img;
	}
	@SuppressWarnings("unused")
	public void setSelect(boolean activated)
	{
		ImagePanel img = getImageContent();
		//img.setSelectMode(activated);
	}

}

