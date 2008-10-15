package nox.ui.chat.group;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
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
import nox.ui.common.NoxJListItem;
import nox.ui.common.SystemPath;

public class GroupMemberList extends JList {
	/**
	 * 好友列表
	 */
	private static final long serialVersionUID = 1L;

	private FilterModel fmod;
	private FilterField filterField;
	private int DEFAULT_FIELD_WIDTH = 20;

	/**
	 * 如果变量为render类内部变量, 则会出现列表元素被添加到同一行, 并且该行重复N次的情况
	 */
	JLabel portrait;
	JLabel nick;
	JLabel sign;

	/**
	 * 具有自定义列表元素和过滤功能的列表
	 * 
	 * @param objs
	 *            列表元素(FriendItem类型)数组
	 */
	GroupMemberList(Object[] objs) {
		// super(objs);

		if (objs == null)
			return;
		portrait = new JLabel();
		nick = new JLabel();
		sign = new JLabel();
		this.setCellRenderer(new GroupMemberListCellRender());
		filterField = new FilterField(DEFAULT_FIELD_WIDTH);
		filterField.setSize(new Dimension(100, 20));
		filterField.setPreferredSize(new Dimension(100, 20));
		filterField.setMaximumSize(new Dimension(10000, 20));
		filterField.setMinimumSize(new Dimension(20, 20));

		fmod = new FilterModel();
		this.setModel(fmod);
		for (int i = 0; i < objs.length; i++) {
			// System.out.println(objs[i].getClass().toString());
			addItem((NoxJListItem) objs[i]);
		}
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent me) {
				if (me.getButton() == MouseEvent.BUTTON3) {
					/*
					 * 实现右键可选取JListItem
					 */
					int index = GroupMemberList.this.locationToIndex(me.getPoint());
					GroupMemberList.this.setSelectedIndex(index);
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

	public void setModel(ListModel m) {
		if (!(m instanceof FilterModel))
			throw new IllegalArgumentException();
		super.setModel(m);
	}

	public Object addItem(Object o) {
		System.out.println("In GroupMemberList.addItems().");
		//TODO 加之前应该先判断是否已有!!
		int size =  ((FilterModel) getModel()).getSize();
		for(int index = 0; index <  size; index++){
			//如果已经有了, 则返回.
			ID newID = ((NoxJListItem)o).getUUID();
			ID curID = ((NoxJListItem)(((FilterModel) getModel()).getElementAt(index))).getUUID();
			if(newID.equals(curID))
					return o;
		}
		System.out.println("In GroupMemberList.addItems(). getModel().addElement()");
		((FilterModel) getModel()).addElement((NoxJListItem) o);
		return o;
	}

	public class GroupMemberListCellRender extends JPanel implements ListCellRenderer {
		/**
		 * JList单元格渲染器
		 */
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(final JList list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			NoxJListItem item = (NoxJListItem) value;

			portrait.setIcon((Icon) item.getPortrait());
			nick.setText(item.getName());
			sign.setText('(' + item.getDesc() + ')');

			/*
			 * portrait.setOpaque(false); nick.setOpaque(false);
			 * sign.setOpaque(false); setOpaque(true);
			 */
			Font defaultFont = sign.getFont();
			Font nameFont = defaultFont.deriveFont(Font.BOLD, defaultFont
					.getSize() + 1);
			nick.setFont(nameFont);

			/*GroupMemberListCellRender.this.setLayout(new GridBagLayout());
			addWithGridBag(portrait, GroupMemberListCellRender.this, 0, 0, 1, 2,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 0);
			addWithGridBag(nick, GroupMemberListCellRender.this, 1, 0, 1, 1,
					GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1,
					0);
			addWithGridBag(sign, GroupMemberListCellRender.this, 1, 1, 1, 1,
					GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1,
					0);*/
			GroupMemberListCellRender.this.setLayout(new BoxLayout(GroupMemberListCellRender.this, BoxLayout.X_AXIS));
			add(portrait);
			add(nick);
			
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
								+ "<Font color=black>Nickname:</Font> <Font color=blue>"
								+ item.getName()
								+ "<br></Font>"
								+ "<Font color=black>Status Message:</Font> <Font color=blue>"
								+ item.getDesc()
								+ "<br></Font>"
								+ "<Font color=black>Phone:</Font> <Font color=blue>"
								+ "110, 119, 120, 114, 117"
								+ "<br></Font>"
								+ "<Font color=black>Description:</Font> <Font color=blue>"
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
								+ "<Font color=black>Group Name:</Font> <Font color=blue>"
								+ item.getName()
								+ "<br></Font>"
								+ "<Font color=black>Description:</Font> <Font color=blue>"
								+ item.getDesc()
								+ "<br></Font>"
								+ "<Font color=black>No of Members:</Font> <Font color=blue>"
								+ "110, 119, 120, 114, 117"
								+ "<br></Font>"
								+ "<Font color=black>UUID:</Font> <Font color=blue>"
								+ item.getUUID() + "<br></Font></BODY></html>");
			}
			return this;
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
		
		public int getRealSize() {
			return items.size();
		}

		public void addElement(Object o) {
			System.out.println("in FilterModel.addElement().");
			System.out.println("size: " + this.getRealSize());
			items.add((NoxJListItem) o);
			System.out.println("after adding size: " + this.getRealSize());
			refilter();
		}

		private void refilter() {
			// System.out.println("refiltering...");
			filterItems.clear();
			String term = getFilterField().getText();
			for (int i = 0; i < items.size(); i++)
				if (items.get(i).getName().indexOf(term, 0) != -1) {
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
			this.setToolTipText(getHtmlText("Input keywords to search"));
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