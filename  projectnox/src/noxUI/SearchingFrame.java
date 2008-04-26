/**
 * SearchingFrame.java
 * �����ڵ�/�鴰��
 */
package noxUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.table.DefaultTableModel;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.nox.JXTA;
import net.nox.NoxToolkit;

/**
 * @author shinysky
 * 
 */
public class SearchingFrame extends JFrame {
	public class AdvTable extends JTable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		AdvTable(AdvTableModel model) {
			super(model);

			this.getSelectionModel().setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);
		}

		public void addRow(Advertisement adv) {
			addRow(adv, "Unknown", -1);
		}

		/**
		 * ����������б������һ��. ���֮ǰ�жϱ����Ƿ����и�Ԫ��, û�в���Ҫ���. TODO �Ժ���ݾ����������Ҫ��
		 * 
		 * @param adv
		 *            ��ȡ�Ĺ��
		 * @param src
		 *            ��src����ȡ�Ĺ��
		 * @param delay
		 *            ʱ���ӳ�
		 */
		public void addRow(Advertisement adv, Object src, long delay) {
			int rows = model.getRowCount();
			for (int i = 0; i < rows; i++) {
				if (model.getValueAt(i, 2).equals(src))
					return;
			}

			Object[] advitem = new Object[4];
			advitem[0] = adv.getAdvType();
			advitem[1] = adv.getClass();
			advitem[2] = src;
			if (delay < 0)
				advitem[3] = "-";
			else
				advitem[3] = delay;

			model.addRow(advitem);
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
	AdvTable searchResultTable;
	AdvTableModel model;

	JXTA MyLogin;
	//HuntingEventHandler hehandler=new HuntingEventHandler(searchResultTable);;
	/**
	 * 
	 */
	public SearchingFrame(JXTA MyLogin) {
		super("Searching");
		this.MyLogin = MyLogin;
		infinitePane = buildInfinitePanel();

		String[] columns = { "Name", "Sign", "UUID", "Delay/ms" };
		Object[][] data = {
				{ "Who", "I am Who", "uuid:jxta:xxxxxxxxxxxxxxxxxx", 1000 },
				{ "Who", "I am Who", "uuid:jxta:xxxxxxxxxxxxxxxxxx", 1000 }};
		model = new AdvTableModel(data, columns);
		searchResultTable = new AdvTable(model);
		/**
		 * ����֮ǰ�������趨Ŀ��JTable, �����¼��������֪������������������
		 */
		new NoxToolkit().getHuntingEventHandler().setAdvTable(searchResultTable);
		/*
		 * TableColumn column = searchResultTable.getColumnModel().getColumn(3);
		 * column.setPreferredWidth(20);
		 */
		searchResultTable.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent me) {
					JPopupMenu ResultOprMenu = new JPopupMenu();
					if (me.getButton() == MouseEvent.BUTTON3) {
						/*
						 * TODO ʵ���Ҽ���ѡȡJTable ����
						 */
						searchResultTable.getVisibleRect();
						int row = me.getY() / searchResultTable.getRowHeight();
						searchResultTable.getSelectionModel()
								.setLeadSelectionIndex(row);
						ResultOprMenu
						.add(new AbstractAction("Add him/her to my friendlist") {
							public void actionPerformed(ActionEvent e) {
								// TODO ��ӵ������б�
								/*int[] selected = searchResultTable.getSelectedRows();
								for (int i = 0; i < selected.length; i++) {
									// TODO ��ѡ�е�Ԫ����ӵ�����, Ŀǰֻ�������ѡ��
									System.out.println(searchResultTable.getValueAt(
											selected[i], 2));
								}*/
							}
						});
				ResultOprMenu.add(new AbstractAction("Talk to him/her") {
					public void actionPerformed(ActionEvent e) {
						// TODO �����촰��
					}
				});
				MenuElement els[] = ResultOprMenu.getSubElements();
				for (int i = 0; i < els.length; i++)
					els[i].getComponent().setBackground(Color.WHITE);
				ResultOprMenu.setLightWeightPopupEnabled(true);
				ResultOprMenu.pack();
						ResultOprMenu.show((Component) me.getSource(), me
								.getPoint().x, me.getPoint().y);
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
					// AdvTable.this.getSelectedRow();
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

		this.pack();
	}

	protected Container buildInfinitePanel() {
		JPanel pane = new JPanel(new BorderLayout());

		glassPane = new InfiniteProgressPanel("������, ���Ժ�...", 12);
		glassPane.setScale(0.2d);
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
							MyLogin.GoHunting(	DiscoveryService.PEER, new NoxToolkit().getHuntingEventHandler());
						}
					}, "Hunter");
					hunter.start();
				} else if (searchPeersBtn.getText() == "Stop") {
					glassPane.stop();
					searchPeersBtn.setText("Search");
					MyLogin.StopHunting();
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

		JXTA MyLogin;
		MyLogin = new JXTA();
		MyLogin.SeekRendezVousConnection();

		SearchingFrame sfrm = new SearchingFrame(MyLogin);
		sfrm.setLocation(0, 0);
		sfrm.setSize(new Dimension(1000, 350));
		// sfrm.pack();
		sfrm.setVisible(true);
	}
}

class AdvTableModel extends DefaultTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AdvTableModel(Object[][] data, Object[] columns) {
		super(data, columns);
	}

	/**
	 * ��Ԫ���Ƿ�ɱ༭
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/**
	 * ����
	 */
	public Class<?> getColumnClass(int column) {
		Vector<?> v = (Vector<?>) dataVector.elementAt(0);
		return v.elementAt(column).getClass();
	}
}
