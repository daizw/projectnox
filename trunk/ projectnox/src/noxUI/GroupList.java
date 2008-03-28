package noxUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class GroupList extends JList {
	/**
	 * 好友列表
	 */
	private static final long serialVersionUID = 1L;

	static Color listForeground, listBackground, listSelectionForeground,
			listSelectionBackground;

	JLabel portrait;
	JLabel nick;
	JLabel sign;

	GroupList(Object[] objs) {
		super(objs);
		portrait = new JLabel();
		nick = new JLabel();
		sign = new JLabel();
		this.setCellRenderer(new FriendCellRender());
	}

	public class FriendCellRender extends JPanel implements ListCellRenderer {
		/**
		 * JList单元格渲染器
		 */
		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			FriendItem item = (FriendItem) value;

			portrait.setIcon((Icon) item.getPortrait());
			nick.setText(item.getNick());
			sign.setText(item.getSign());

			// setOpaque(true);
			Font defaultFont = sign.getFont();
			Font nameFont = defaultFont.deriveFont(Font.BOLD, defaultFont
					.getSize() + 1);
			nick.setFont(nameFont);

			FriendCellRender.this.setLayout(new GridBagLayout());
			addWithGridBag(portrait, FriendCellRender.this, 0, 0, 1, 2,
					GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 0);
			addWithGridBag(nick, FriendCellRender.this, 1, 0, 1, 1,
					GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1,
					0);
			addWithGridBag(sign, FriendCellRender.this, 1, 1, 1, 1,
					GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1,
					0);

			setBackground(isSelected ? list.getSelectionBackground() : list
					.getBackground());
			setForeground(isSelected ? list.getSelectionForeground() : list
					.getForeground());
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

}
