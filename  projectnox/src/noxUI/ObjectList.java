package noxUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import db.nox.DBTableName;

public class ObjectList extends JList {
	/**
	 * �����б�
	 */
	private static final long serialVersionUID = 1L;

	private FilterModel fmod;
	private FilterField filterField;
	private int DEFAULT_FIELD_WIDTH = 20;

	/**
	 * �������Ϊrender���ڲ�����, �������б�Ԫ�ر���ӵ�ͬһ��, ���Ҹ����ظ�N�ε����
	 */
	JLabel portrait;
	JLabel nick;
	JLabel sign;

	/**
	 * �����Զ����б�Ԫ�غ͹��˹��ܵ��б�
	 * 
	 * @param objs
	 *            �б�Ԫ��(FriendItem����)����
	 */
	ObjectList(Connection sqlconn, String tablename, boolean good) {
		// super(objs);
		fmod = new FilterModel();
		this.setModel(fmod);
		portrait = new JLabel();
		nick = new JLabel();
		sign = new JLabel();
		this.setCellRenderer(new NoxJListCellRender());
		filterField = new FilterField(DEFAULT_FIELD_WIDTH);
		filterField.setSize(new Dimension(100, 20));
		filterField.setPreferredSize(new Dimension(100, 20));
		filterField.setMaximumSize(new Dimension(10000, 20));
		filterField.setMinimumSize(new Dimension(20, 20));

		try {
			Statement stmt = sqlconn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from " +
					tablename + " where Good = " + good);
			
			while(rs.next()){
				ObjectInputStream objInput = new ObjectInputStream(rs.getBinaryStream("Object"));
				System.out.println("Found object. Contents: ");
				
				NoxJListItem tmpItem = (NoxJListItem)objInput.readObject();
				addItem(tmpItem, sqlconn, tablename, good);
				
				System.out.println("Nick:	" + tmpItem.getNick());
				System.out.println("Desc:	" + tmpItem.getSign());
				System.out.println("ID:	" + tmpItem.getUUID());
				System.out.println("Portrait:	" + tmpItem.getPortrait());
				System.out.println("TimeStamp:	" + tmpItem.getTimeStamp());
				System.out.println();
			}
			stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getButton() == MouseEvent.BUTTON3) {
					/*
					 * ʵ���Ҽ���ѡȡJListItem
					 */
					int index = ObjectList.this.locationToIndex(me.getPoint());
					ObjectList.this.setSelectedIndex(index);
				}
			}

			@Override
			public void mouseEntered(MouseEvent menter) {
				// TODO �Զ��ı䱳��ɫ
				// int index =
				// ObjectList.this.locationToIndex(menter.getPoint());
				// ObjectList.this.set
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
	}

	public void setModel(ListModel m) {
		if (!(m instanceof FilterModel))
			throw new IllegalArgumentException();
		super.setModel(m);
	}

	public Object addItem(Object o, Connection sqlconn, String tablename, boolean good) throws SQLException, IOException {
		//TODO ��֮ǰӦ�����ж��Ƿ�����!!
		Statement stmt = sqlconn.createStatement();
		ResultSet rs = null;
		
		int size =  ((FilterModel) (ObjectList.this.getModel())).getSize();
		for(int index = 0; index <  size; index++){
			//����Ѿ�����, �򷵻�.
			NoxJListItem newItem = (NoxJListItem)o;
			NoxJListItem curItem = (NoxJListItem)(((FilterModel) getModel()).getElementAt(index));
			if(newItem.getUUID().equals(curItem.getUUID())){
				//�ҵ����ݿ��е���ͬitem
				rs = stmt.executeQuery("select * from " +
						tablename + " where ID = " + newItem.getUUID());
				//�����ǰ�ĸ���, �����
				if(newItem.getTimeStamp() > curItem.getTimeStamp()){
					curItem = newItem;
					//ɾ�����ݿ��и�ID
					stmt.execute("delete from "+
						tablename + " where ID = " + curItem.getUUID());
					//��ӵ����ݿ�
					PreparedStatement pstmt = sqlconn.prepareStatement("insert into " +
							tablename + " values (?, ?, ?)");
					pstmt.setString(1, newItem.getUUID().toString());
					pstmt.setString(2, good + "");
					
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
					out.writeObject((Object) newItem);
					ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
					pstmt.setBinaryStream(3, input, byteArrayStream.size());
					int recCount = pstmt.executeUpdate();
					pstmt.close();
				}
				stmt.close();
				return (Object)curItem;
			}
		}
		((FilterModel) getModel()).addElement((NoxJListItem) o);
		//��ӵ����ݿ�
		PreparedStatement pstmt = sqlconn.prepareStatement("insert into " +
				tablename + " values (?, ?, ?)");
		pstmt.setString(1, ((NoxJListItem) o).getUUID().toString());
		pstmt.setString(2, good + "");
		
		ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
		out.writeObject((Object) o);
		ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
		pstmt.setBinaryStream(3, input, byteArrayStream.size());
		int recCount = pstmt.executeUpdate();
		pstmt.close();
		stmt.close();
		
		return o;
	}

	public class NoxJListCellRender extends JPanel implements ListCellRenderer {
		/**
		 * JList��Ԫ����Ⱦ��
		 */
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(final JList list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			NoxJListItem item = (NoxJListItem) value;

			portrait.setIcon((Icon) item.getPortrait());
			nick.setText(item.getNick());
			sign.setText('(' + item.getSign() + ')');

			/*
			 * portrait.setOpaque(false); nick.setOpaque(false);
			 * sign.setOpaque(false); setOpaque(true);
			 */
			Font defaultFont = sign.getFont();
			Font nameFont = defaultFont.deriveFont(Font.BOLD, defaultFont
					.getSize() + 1);
			nick.setFont(nameFont);

			NoxJListCellRender.this.setLayout(new GridBagLayout());
			addWithGridBag(portrait, NoxJListCellRender.this, 0, 0, 1, 2,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 0);
			addWithGridBag(nick, NoxJListCellRender.this, 1, 0, 1, 1,
					GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1,
					0);
			addWithGridBag(sign, NoxJListCellRender.this, 1, 1, 1, 1,
					GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1,
					0);

			setBackground(isSelected ? list.getSelectionBackground() : list
					.getBackground());
			setForeground(isSelected ? list.getSelectionForeground() : list
					.getForeground());

			/*
			 * setBackground(cellHasFocus ? Color.RED : list .getBackground());
			 * setForeground(cellHasFocus ? Color.GREEN : list
			 * .getForeground());
			 */
			/**
			 * ��ʱֻ���þ���·��, ������Ҫ��ͷ��洢������
			 */
			//System.out.println(item.getClass().toString().endsWith("PeerItem"));
			if (item.getClass().toString().endsWith("PeerItem")) {
				this
						.setToolTipText("<html><BODY bgColor=#ffffff>"
								+ "<img width=64 height=64 src=\"file:/"
								+ System.getProperty("user.dir")
								+ System.getProperty("file.separator")
								+ SystemPath.PORTRAIT_RESOURCE_PATH
								+"chat.png\"><br>"
								+ "<Font color=black>�ǳ�:</Font> <Font color=blue>"
								+ item.getNick()
								+ "<br></Font>"
								+ "<Font color=black>ǩ����:</Font> <Font color=blue>"
								+ item.getSign()
								+ "<br></Font>"
								+ "<Font color=black>��ϵ��ʽ:</Font> <Font color=blue>"
								+ "110, 119, 120, 114, 117"
								+ "<br></Font>"
								+ "<Font color=black>����˵��:</Font> <Font color=blue>"
								+ item.getNick()
								+ " owns me so much MONEY!! "
								+ "<br></Font>"
								+ "<Font color=black>UUID:</Font> <Font color=blue>"
								+ item.getUUID() + "<br></Font></BODY></html>");
			} else if (item.getClass().toString().endsWith("GroupItem")) {
				this
						.setToolTipText("<html><BODY bgColor=#ffffff>"
								+ "<img width=64 height=64 src=\"file:/"
								+ System.getProperty("user.dir")
								+ System.getProperty("file.separator")
								+ SystemPath.PORTRAIT_RESOURCE_PATH
								+ "chat.png\"><br>"
								+ "<Font color=black>����:</Font> <Font color=blue>"
								+ item.getNick()
								+ "<br></Font>"
								+ "<Font color=black>����:</Font> <Font color=blue>"
								+ item.getSign()
								+ "<br></Font>"
								+ "<Font color=black>��Ա����:</Font> <Font color=blue>"
								+ "110, 119, 120, 114, 117"
								+ "<br></Font>"
								+ "<Font color=black>UUID:</Font> <Font color=blue>"
								+ item.getUUID() + "<br></Font></BODY></html>");
			}
			return this;
		}

		private void addWithGridBag(Component comp, Container cont, int x,
				int y, int width, int height, int anchor, int fill,
				int weightx, int weighty) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = x;
			gbc.gridy = y;
			gbc.gridwidth = width;
			gbc.gridheight = height;
			gbc.anchor = anchor;
			gbc.fill = fill;
			gbc.weightx = weightx;
			gbc.weighty = weighty;
			cont.add(comp, gbc);
		}
	}

	public JTextField getFilterField() {
		return filterField;
	}

	// inner class to provide filtered model
	class FilterModel extends AbstractListModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ArrayList<NoxJListItem> items;
		ArrayList<NoxJListItem> filterItems;

		public FilterModel() {
			super();
			items = new ArrayList<NoxJListItem>();
			filterItems = new ArrayList<NoxJListItem>();
		}

		public Object getElementAt(int index) {
			if (index < filterItems.size())
				return filterItems.get(index);
			else
				return null;
		}

		public int getSize() {
			return filterItems.size();
		}

		public void addElement(Object o) {
			items.add((NoxJListItem) o);
			// System.out.println("addElement...");
			refilter();
		}

		private void refilter() {
			// System.out.println("refiltering...");
			filterItems.clear();
			String term = getFilterField().getText();
			for (int i = 0; i < items.size(); i++)
				if (items.get(i).getNick().indexOf(term, 0) != -1) {
					// System.out.println(items.get(i).getNick());
					filterItems.add(items.get(i));
				}

			fireContentsChanged(this, 0, getSize());
		}
	}

	// inner class provides filter-by-keystroke field
	class FilterField extends JTextField implements DocumentListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FilterField(int width) {
			super(width);
			this.setToolTipText(getHtmlText("����ؼ����������б�"));
			getDocument().addDocumentListener(this);
		}

		private String getHtmlText(String text) {
			return ("<html><BODY bgColor=#ffffff><Font color=black>" + text + "</Font></BODY></html>");
	}

		public void changedUpdate(DocumentEvent e) {
			((FilterModel) getModel()).refilter();
		}

		public void insertUpdate(DocumentEvent e) {
			((FilterModel) getModel()).refilter();
		}

		public void removeUpdate(DocumentEvent e) {
			((FilterModel) getModel()).refilter();
		}
	}
}
