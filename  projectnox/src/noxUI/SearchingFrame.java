/**
 * SearchingFrame.java
 * 搜索节点/组窗口
 */
package noxUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.nox.JXTA;

/**
 * @author shinysky
 * 
 */
public class SearchingFrame extends JFrame {

	public class AdvTableModel extends DefaultTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public AdvTableModel(Object[][] data, Object[] columns) {
			super(data, columns);
		}

		/**
		 * 单元格是否可编辑
		 */
		public boolean isCellEditable(int row, int col) {
			return false;
		}

		/**
		 * 列类
		 */
		public Class<?> getColumnClass(int column) {
			Vector<?> v = (Vector<?>) dataVector.elementAt(0);
			return v.elementAt(column).getClass();
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected InfiniteProgressPanel glassPane;
	JPanel rootpane = new JPanel();
	Container infinitePane = new JPanel();
	JButton searchPeersBtn = new JButton("Search");
	JTable searchResultTable;
	AdvTableModel model;
	
	JXTA MyLogin;

	/**
	 * 
	 */
	public SearchingFrame() {
		super("Search Frame");
		infinitePane = buildInfinitePanel();

		String[] columns = { "Name", "Sign", "UUID", "Delay/ms" };
		Object[][] data = { { "Who", "I am Who",
				"uuid:jxta:xxxxxxxxxxxxxxxxxx", 1000 } };
		model = new AdvTableModel(data, columns);
		searchResultTable = new JTable(model);
		/*TableColumn column = searchResultTable.getColumnModel().getColumn(3);
		column.setPreferredWidth(20);*/
		searchResultTable.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				int[] selected = searchResultTable.getSelectedRows();
				for(int i = 0; i< selected.length; i++){
					//TODO 把选中的元素添加到好友
					System.out.println(searchResultTable.getValueAt(selected[i], 2));
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});

		JScrollPane scrollPane = new JScrollPane(searchResultTable);

		// rootpane.add(searchPeersBtn);
		rootpane.setLayout(new BorderLayout());
		rootpane.add(BorderLayout.NORTH, infinitePane);
		rootpane.add(BorderLayout.CENTER, scrollPane);

		this.setContentPane(rootpane);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		MyLogin = new JXTA();
        MyLogin.SeekRendezVousConnection();
        
        this.pack();
	}

	protected Container buildInfinitePanel() {
		JPanel pane = new JPanel(new BorderLayout());

		glassPane = new InfiniteProgressPanel("搜索中, 请稍候...", 12);
		glassPane.setScale(0.2d);
		// this.setGlassPane(glassPane);
		Dimension size = new Dimension(100, 100);
		glassPane.setSize(size);
		glassPane.setPreferredSize(size);
		glassPane.setMaximumSize(size);
		glassPane.setMinimumSize(size);

		searchPeersBtn.setSize(size);
		searchPeersBtn.setPreferredSize(size);
		searchPeersBtn.setMaximumSize(size);
		searchPeersBtn.setMinimumSize(size);

		searchPeersBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (searchPeersBtn.getText() == "Search") {
					glassPane.start();
					Thread indicating = new Thread(new Runnable() {
						public void run() {
							try {
								searchPeersBtn.setText("Stop");
								Thread.sleep(2000);
							} catch (InterruptedException ie) {
							}
						}
					}, "Indicating");
					indicating.start();
					Thread hunter = new Thread(new Runnable() {
						public void run() {
					MyLogin.GoHunting(model);
					// TODO 将搜索到的节点添加到表中
					/*Object[] advitem = new Object[4];
					advitem[0] = "";
					advitem[1] = "";
					advitem[2] = "";
					advitem[3] = 1000;
					
					model.addRow(advitem);*/
						}
					}, "Hunter");
					hunter.start();
				} else if (searchPeersBtn.getText() == "Stop") {
					glassPane.stop();
					searchPeersBtn.setText("Search");
					MyLogin.StopNetwork();
				}
			}
		});
		pane.add(BorderLayout.CENTER, glassPane);
		pane.add(BorderLayout.EAST, searchPeersBtn);
		pane.setBackground(Color.WHITE);
		return pane;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

        SearchingFrame sfrm = new SearchingFrame();
		sfrm.setLocation(100, 50);
		sfrm.setSize(new Dimension(800, 650));
		//sfrm.pack();
		sfrm.setVisible(true);
		
		//MyLogin.StopNetwork();
	}
}
