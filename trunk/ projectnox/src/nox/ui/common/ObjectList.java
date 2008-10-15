package nox.ui.common;

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
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.jxta.id.ID;
import nox.xml.NoxPeerStatusUnit;

public class ObjectList extends JList {
	/**
	 * 好友列表
	 */
	private static final long serialVersionUID = 1L;

	private FilterModel fmod;
	private FilterField filterField;
	private int DEFAULT_FIELD_WIDTH = 20;
	
	private boolean isGood;
	private Connection sqlconn;
	private String tablename;

	/**
	 * 如果变量为render类内部变量, 则会出现列表元素被添加到同一行, 并且该行重复N次的情况
	 */
	JLabel portrait;
	JLabel nick;
	JLabel sign;
	
	Dimension portraitsize = new Dimension(40, 40);

	/**
	 * 具有自定义列表元素和过滤功能的列表
	 * 
	 * @param objs
	 *            列表元素(FriendItem类型)数组
	 */
	public ObjectList(Connection sqlconn, String tablename, boolean isGood) {
		// super(objs);
		fmod = new FilterModel();
		this.setModel(fmod);
		this.sqlconn = sqlconn;
		this.tablename = tablename;
		this.isGood = isGood;
		
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
					tablename + " where Good = " + isGood);
			
			while(rs.next()){
				ObjectInputStream objInput = new ObjectInputStream(rs.getBinaryStream("Object"));
				System.out.println("Found object. Contents: ");
				
				NoxJListItem tmpItem = (NoxJListItem)objInput.readObject();
				addDBObject2List(tmpItem);
				
				System.out.println("isGood:	" + isGood);
				System.out.println("Nick:	" + tmpItem.getName());
				System.out.println("Desc:	" + tmpItem.getDesc());
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
					 * 实现右键可选取JListItem
					 */
					int index = ObjectList.this.locationToIndex(me.getPoint());
					ObjectList.this.setSelectedIndex(index);
				}
			}
			@Override
			public void mouseEntered(MouseEvent menter) {
				// TODO 自动改变背景色
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
	/**
	 * 判断是否列表中已存在ID为id的表项
	 * @param id 要查找的ID
	 * @return 是否存在
	 */
	public boolean isExist(ID id){
		int size =  fmod.getRealSize();
		for(int index = 0; index <  size; index++){
			if(((NoxJListItem)fmod.getRealElementAt(index)).getUUID().equals(id))
				return true;
		}
		return false;
	}
	/**
	 * 返回列表项中所有ID-password对.
	 * 只能当表项是GroupItem对象时才能调用.
	 * @return 所有广告
	 */
	public Map<ID, String> getGroupIDPwds(){
		Map<ID, String> idpwds = new HashMap<ID, String>();
		idpwds.clear();
		try{
			int size =  fmod.getRealSize();
			System.out.println("ObjectList size here: " + size);
			GroupItem tempItem = null;
			for(int index = 0; index <  size; index++){
				tempItem = (GroupItem)fmod.getRealElementAt(index);
				idpwds.put(tempItem.getUUID(), tempItem.getPassword());
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		return idpwds;
	}
	
	public Object deleteItem(int index){
		return deleteItem(sqlconn, tablename, isGood, index);
	}
	/**
	 * 从List中删除某项
	 * @param sqlconn 数据库连接
	 * @param tablename 表名
	 * @param isGood 好友/黑名单成员 (这个参数其实不必要, 因为peer要么属于好友列表, 要么属于黑名单)
	 * @param index (列表索引值)
	 * @return 被删除的项
	 */
	public Object deleteItem(Connection sqlconn, String tablename, boolean isGood, int index){
		System.out.println("removing the item from this list: " + index + "/" + fmod.getRealSize());
		System.out.println("Items before removing: " + fmod.getSize());
		Statement stmt;
		try {
			stmt = sqlconn.createStatement();
			
			stmt.execute("delete from "
					+ tablename 
					+ " where ID = '"
					+ ((NoxJListItem)fmod.getRealElementAt(index)).getUUID().toString()
					+ "'");
					//+ " and Good = " + isGood);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Items left: " + fmod.getRealSize());
		return fmod.deleteElementAt(index);
	}

	public void setModel(ListModel m) {
		if (!(m instanceof FilterModel))
			throw new IllegalArgumentException();
		super.setModel(m);
	}
	/**
	 * 系统初始化时调用, 用于将数据库中数据添加到列表
	 * @param o
	 * @return
	 */
	private Object addDBObject2List(Object o){
		int size =  fmod.getRealSize();
		NoxJListItem newItem = (NoxJListItem)o;
		for(int index = 0; index <  size; index++){
			//如果已经有了, 则返回.
			NoxJListItem curItem = (NoxJListItem)(fmod.getRealElementAt(index));
			if(newItem.getUUID().equals(curItem.getUUID())){
				//如果当前的更新, 则更新
				if(newItem.getTimeStamp() > curItem.getTimeStamp()){
					curItem = newItem;
				}
				return (Object)curItem;
			}
		}
		fmod.addElement((NoxJListItem) o);		
		return o;
	}

	/**
	 * 用于外部对象调用, 向列表中添加项.
	 * @param object 要添加的项
	 * @param sqlconn 数据库连接
	 * @param tablename 表名
	 * @param isGood true: 好友; false: 黑名单成员
	 * @return 已添加的项, 或原来的更新的项
	 * @throws SQLException
	 * @throws IOException
	 */
	public Object addItem(Object object) throws SQLException, IOException {
		//TODO 加之前应该先判断是否已有!!
		Statement stmt = sqlconn.createStatement();
		
		System.out.println("Items before adding: " + fmod.getRealSize());
		
		int size =  fmod.getRealSize();
		for(int index = 0; index <  size; index++){
			//如果已经有了, 则返回.
			NoxJListItem newItem = (NoxJListItem)object;
			NoxJListItem curItem = (NoxJListItem)fmod.getRealElementAt(index);
			if(newItem.getUUID().equals(curItem.getUUID())){
				//如果当前的更新, 则更新
				if(newItem.getTimeStamp() > curItem.getTimeStamp()){
					System.out.println("Got a newer item");
					curItem = newItem;
					//删除数据库中该ID
					stmt.execute("delete from "+
						tablename + " where ID = '" + curItem.getUUID().toString() + "'");
					//添加到数据库
					PreparedStatement pstmt = sqlconn.prepareStatement("insert into " +
							tablename + " values (?, ?, ?)");
					pstmt.setString(1, newItem.getUUID().toString());
					pstmt.setString(2, isGood + "");
					
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
					out.writeObject((Serializable)newItem);
					ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
					pstmt.setBinaryStream(3, input, byteArrayStream.size());
					pstmt.executeUpdate();
					pstmt.close();
				}
				stmt.close();
				System.out.println("Got a item that already exist in the list");
				System.out.println("Items now: " + fmod.getSize());
				return (Object)curItem;
			}
		}
		fmod.addElement((NoxJListItem) object);
		//删除数据库中该ID
		System.out.println("stmt.execute:delete from "+
			tablename + " where ID = '" + ((NoxJListItem) object).getUUID().toString() + "'");
		stmt.execute("delete from "+
			tablename + " where ID = '" + ((NoxJListItem) object).getUUID().toString() + "'");
		//添加到数据库
		PreparedStatement pstmt = sqlconn.prepareStatement("insert into " +
				tablename + " values (?, ?, ?)");
		pstmt.setString(1, ((NoxJListItem) object).getUUID().toString());
		pstmt.setString(2, isGood + "");
		
		ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
		out.writeObject((NoxJListItem)object);
		ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
		pstmt.setBinaryStream(3, input, byteArrayStream.size());
		pstmt.executeUpdate();
		pstmt.close();
		stmt.close();
		System.out.println("Add a totally new item to the list");
		System.out.println("Items now: " + fmod.getSize());
		return object;
	}
	/**
	 * 更新状态(昵称/签名档/头像/在线状态)
	 * @param id 目标的ID
	 * @param stat 含状态信息的数据结构, stat为null则表示离线...
	 */
	public void setStatus(ID id, NoxPeerStatusUnit stat) throws SQLException, IOException{
		//注意!!!!!!!!因为好友之间才会互ping, 所以如果出现在好友列表中找不到id是不正常的.
		//同时, 最好能更新数据库中昵称和签名档信息
		int size =  fmod.getRealSize();
		for(int index = 0; index <  size; index++){
			//如果已经有了, 则返回.
			NoxJListItem curItem = (NoxJListItem)fmod.getRealElementAt(index);
			if(id.equals(curItem.getUUID())){
				//更新
				if(stat == null){
					//离线
					System.out.println("该用户不在线");
					curItem.setOnlineStatus(ItemStatus.OFFLINE);
				}else{
					/**
					 * @Fixme 这样修改有用吗? curItem是引用吗?
					 * 是引用...
					 */
					//如果没有修改, 则返回, 可以避免频繁的数据库读写操作
					if(curItem.getName() != null && curItem.getName().equals(stat.getNickName())
							&& curItem.getDesc() != null && curItem.getDesc().equals(stat.getSign())
							&& curItem.getPortrait() != null && curItem.getPortrait().equals(stat.getPortrait())
							&& curItem.getOnlineStatus() != null && curItem.getOnlineStatus().equals(stat.getOnlineStatus())){
						System.out.println("状态未变, 取消修改...");
						curItem.updateTimeStamp();
						this.repaint();
						return;
					}
					System.out.println("Refreshing item");
					
					curItem.setName(stat.getNickName());
					curItem.setDesc(stat.getSign());
					if(stat.getPortrait() != null)
						curItem.setPortrait(stat.getPortrait());
					curItem.setOnlineStatus(stat.getOnlineStatus());
					
					Statement stmt = sqlconn.createStatement();
					//删除数据库中该ID
					stmt.execute("delete from "+
						tablename + " where ID = '" + id.toString() + "'");
					//添加到数据库
					PreparedStatement pstmt = sqlconn.prepareStatement("insert into " +
							tablename + " values (?, ?, ?)");
					pstmt.setString(1, id.toString());
					pstmt.setString(2, isGood + "");
					
					ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
					ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
					out.writeObject((Serializable)curItem);
					ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
					pstmt.setBinaryStream(3, input, byteArrayStream.size());
					pstmt.executeUpdate();
					pstmt.close();
					stmt.close();
					System.out.println("刷新状态完毕");
				}
				this.repaint();
				return;
			}
		}
		System.out.println("好友中未发现该ID, 好诡异~");
		return;
	}

	public class NoxJListCellRender extends JPanel implements ListCellRenderer {
		/**
		 * JList单元格渲染器
		 */
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(final JList list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			NoxJListItem item = (NoxJListItem) value;

			portrait.setIcon((Icon) item.getPortrait());
			
			portrait.setSize(portraitsize);
			portrait.setPreferredSize(portraitsize);
			portrait.setMaximumSize(portraitsize);
			portrait.setMinimumSize(portraitsize);
			
			String onlingStat = ItemStatus.UnknownStr;
			if(item.getOnlineStatus() != null){
				if(item.getOnlineStatus().equals(ItemStatus.ONLINE))
					onlingStat = ItemStatus.OnlineStr;
				else if(item.getOnlineStatus().equals(ItemStatus.BUSY))
					onlingStat = ItemStatus.BusyStr;
				else if(item.getOnlineStatus().equals(ItemStatus.UNAVAILABLE))
					onlingStat = ItemStatus.UnavailableStr;
				else if(item.getOnlineStatus().equals(ItemStatus.OFFLINE))
					onlingStat = ItemStatus.OfflineStr;
			}else {
				item.setOnlineStatus(ItemStatus.UNKNOWN);
			}
			
			nick.setText(item.getName() + "[" + onlingStat + "]");
			
			sign.setText("<html><Font color=gray>"
					+ '('
					+ item.getDesc()
					+ ')'
					+ "</Font></html>");

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
			 * 暂时只能用绝对路径, 并且需要把头像存储到本地
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
								+ "<Font color=black>昵称:</Font> <Font color=blue>"
								+ item.getName()
								+ "<br></Font>"
								+ "<Font color=black>签名档:</Font> <Font color=blue>"
								+ item.getDesc()
								+ "<br></Font>"
								+ "<Font color=black>联系方式:</Font> <Font color=blue>"
								+ "110, 119, 120, 114, 117"
								+ "<br></Font>"
								+ "<Font color=black>个人说明:</Font> <Font color=blue>"
								+ item.getName()
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
								+ "<Font color=black>组名:</Font> <Font color=blue>"
								+ item.getName()
								+ "<br></Font>"
								+ "<Font color=black>公告:</Font> <Font color=blue>"
								+ item.getDesc()
								+ "<br></Font>"
								+ "<Font color=black>成员数量:</Font> <Font color=blue>"
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
	@SuppressWarnings("serial")
	public class FilterModel extends DefaultListModel {
		ArrayList<NoxJListItem> items;
		ArrayList<NoxJListItem> filterItems;
		Vector<Integer> indexes;

		public FilterModel() {
			super();
			items = new ArrayList<NoxJListItem>();
			filterItems = new ArrayList<NoxJListItem>();
			indexes = new Vector<Integer>();
		}

		public Object getElementAt(int index) {
			if (index < filterItems.size())
				return filterItems.get(index);
			else
				return null;
		}
		public Object getRealElementAt(int index) {
			if (index < filterItems.size() && indexes.elementAt(index) < items.size())
				return items.get(indexes.elementAt(index));
			else
				return null;
		}

		public int getSize() {
			return filterItems.size();
		}
		
		public int getRealSize() {
			return items.size();
		}

		public void addElement(Object o) {
			items.add((NoxJListItem) o);
			// System.out.println("addElement...");
			refilter();
		}
		
		public Object deleteElementAt(int index){
			//真正删除之
			NoxJListItem ob = items.remove((int)indexes.elementAt(index));
			refilter();
			
			return ob;
		}

		private void refilter() {
			// System.out.println("refiltering...");
			filterItems.clear();
			indexes.clear();
			int index = 0;
			String term = getFilterField().getText();
			for (int i = 0; i < items.size(); i++)
				if (items.get(i).getName().indexOf(term, 0) != -1) {
					// System.out.println(items.get(i).getNick());
					filterItems.add(items.get(i));
					index++;
					indexes.add(i);
				}
			fireContentsChanged(this, 0, getSize());
		}
	}

	// inner class provides filter-by-keystroke field
	public class FilterField extends JTextField implements DocumentListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FilterField(int width) {
			super(width);
			this.setToolTipText(getHtmlText("输入关键字以搜索列表"));
			getDocument().addDocumentListener(this);
		}

		public String getHtmlText(String text) {
			return ("<html><BODY bgColor=#ffffff><Font color=black>"
					+ text + "</Font></BODY></html>");
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