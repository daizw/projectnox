package noxUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

public class FriendList extends JList{
	/**
	 * 好友列表
	 */
	private static final long serialVersionUID = 1L;
	
	private FilterModel fmod;
	private FilterField filterField;
	private int DEFAULT_FIELD_WIDTH = 20;
	
	JLabel portrait;
	JLabel nick;
	JLabel sign;
	
	FriendList(Object[] objs)
	{
		//super(objs);
		
		portrait = new JLabel();
		nick = new JLabel();
		sign = new JLabel();
		this.setCellRenderer(new FriendCellRender());
		filterField = new FilterField (DEFAULT_FIELD_WIDTH);
		
		fmod = new FilterModel();
		this.setModel (fmod);
		for(int i = 0; i<objs.length; i++)
		{
			//System.out.println(objs[i].getClass().toString());
			addItem((FriendItem)objs[i]);
		}
	}
	
	public void setModel (ListModel m) {
        if (! (m instanceof FilterModel))
            throw new IllegalArgumentException();
        super.setModel (m);
    }

    public void addItem (Object o) {
        ((FilterModel)getModel()).addElement ((FriendItem)o);
    }
    
	public class FriendCellRender extends JPanel implements ListCellRenderer {
		/**
		 * JList单元格渲染器
		 */
		private static final long serialVersionUID = 1L;
				
		public Component getListCellRendererComponent(JList list,
		                                              Object value,
		                                              int index,
		                                              boolean isSelected,
		                                              boolean cellHasFocus) {
			FriendItem item = (FriendItem) value;
			
			portrait.setIcon((Icon)item.getPortrait());
			nick.setText(item.getNick());
			sign.setText(item.getSign());
			
			//setOpaque(true);
			Font defaultFont = sign.getFont();
			Font nameFont = defaultFont.deriveFont(Font.BOLD, defaultFont.getSize()+1);
			nick.setFont(nameFont);
			
			FriendCellRender.this.setLayout(new GridBagLayout());
			addWithGridBag (portrait, FriendCellRender.this, 0, 0, 1, 2,
                    GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 0);
		    addWithGridBag (nick, FriendCellRender.this, 1, 0, 1, 1,
		    		GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 0);
		    addWithGridBag (sign, FriendCellRender.this, 1, 1, 1, 1,
                    GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, 1, 0);
		        
			setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			return this;
		}
		private void addWithGridBag (Component comp, Container cont,
                int x, int y,
                int width, int height,
                int anchor, int fill,
                int weightx, int weighty)
		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = x;
			gbc.gridy = y;
			gbc.gridwidth = width;
			gbc.gridheight = height;
			gbc.anchor = anchor;
			gbc.fill = fill;
			gbc.weightx = weightx;
			gbc.weighty = weighty;
			cont.add (comp, gbc);
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
		ArrayList<FriendItem> items;
        ArrayList<FriendItem> filterItems;
        public FilterModel() {
            super();
            items = new ArrayList<FriendItem>();
            filterItems = new ArrayList<FriendItem>();
        }
        public Object getElementAt (int index) {
            if (index < filterItems.size())
                return filterItems.get (index);
            else
                return null;
        }
        public int getSize() {
            return filterItems.size();
        }
        public void addElement (Object o) {
            items.add((FriendItem) o);
            //System.out.println("addElement...");
            refilter();
        }
        private void refilter() {
        	//System.out.println("refiltering...");
            filterItems.clear();
            String term = getFilterField().getText();
            for (int i=0; i<items.size(); i++)
                if (items.get(i).getNick().indexOf(term, 0) != -1)
                {
                	//System.out.println(items.get(i).getNick());
                	filterItems.add (items.get(i));
                }
            
            fireContentsChanged (this, 0, getSize());
        }
    }
    
	// inner class provides filter-by-keystroke field
    class FilterField extends JTextField implements DocumentListener {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public FilterField (int width) {
            super(width);
            getDocument().addDocumentListener (this);
        }
        public void changedUpdate (DocumentEvent e) { ((FilterModel)getModel()).refilter(); }
        public void insertUpdate (DocumentEvent e) {((FilterModel)getModel()).refilter(); }
        public void removeUpdate (DocumentEvent e) {((FilterModel)getModel()).refilter(); }
    }
}
