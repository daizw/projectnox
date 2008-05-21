/**
 * SearchingFrame.java
 * 搜索节点/组窗口
 */
package nox.ui.search;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import nox.ui.common.SystemPath;
import nox.ui.me.Cheyenne;

/**
 * 搜索窗口, 用于搜索peer/group
 * @author shinysky
 */
@SuppressWarnings("serial")
public class SearchingFrame extends JFrame {
	Cheyenne parent;

	private Toolkit tk;
	private Image img_logo;
	private Image img_logo_big;

	JTabbedPane searchTabPane;
	PeerSearchPanel peerSearchPane;
	GroupSearchPanel groupSearchPane;
	/**
	 * @param chyn
	 * 
	 */
	public SearchingFrame(Cheyenne chyn) {
		super("Searching");
		parent = chyn;
		
		tk = Toolkit.getDefaultToolkit();
		img_logo = tk.getImage(SystemPath.ICONS_RESOURCE_PATH + "search_20.png");
		img_logo_big = tk.getImage(SystemPath.ICONS_RESOURCE_PATH + "search_48.png");
		ArrayList<Image> icons = new ArrayList<Image>();
		icons.add(img_logo);
		icons.add(img_logo_big);
		//this.setIconImage(img_logo);
		this.setIconImages(icons);
		
		peerSearchPane = new PeerSearchPanel(parent);
		groupSearchPane = new GroupSearchPanel(parent);

		searchTabPane = new JTabbedPane();
		searchTabPane.addTab("Peer", peerSearchPane);
		searchTabPane.addTab("Group", groupSearchPane);

		this.setContentPane(searchTabPane);
		//this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.addWindowListener(new WindowListener(){
			@Override
			public void windowActivated(WindowEvent arg0) {
			}
			@Override
			public void windowClosed(WindowEvent arg0) {
				StopSearching();
			}
			@Override
			public void windowClosing(WindowEvent arg0) {
				StopSearching();
			}
			@Override
			public void windowDeactivated(WindowEvent arg0) {
			}
			@Override
			public void windowDeiconified(WindowEvent arg0) {
			}
			@Override
			public void windowIconified(WindowEvent arg0) {
			}
			@Override
			public void windowOpened(WindowEvent arg0) {
			}
		});
		//this.pack();
	}
	
	private void StopSearching() {
		peerSearchPane.StopSearching();
		groupSearchPane.StopSearching();
	}
}
