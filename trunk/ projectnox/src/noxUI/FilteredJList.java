package noxUI;

import javax.swing.event.*;
import javax.swing.*;
import java.util.ArrayList;

public class FilteredJList extends JList {

    /**
	 * SWING HACK
	 */
	private static final long serialVersionUID = 1L;
	private FilterField filterField;
    private int DEFAULT_FIELD_WIDTH = 20;

    public FilteredJList() {
        super();
        setModel (new FilterModel());
        filterField = new FilterField (DEFAULT_FIELD_WIDTH);
    }

    public void setModel (ListModel m) {
        if (! (m instanceof FilterModel))
            throw new IllegalArgumentException();
        super.setModel (m);
    }

    public void addItem (Object o) {
        ((FilterModel)getModel()).addElement (o);
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
		ArrayList items;
        ArrayList filterItems;
        public FilterModel() {
            super();
            items = new ArrayList();
            filterItems = new ArrayList();
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
            items.add (o);
            refilter();
        }
        private void refilter() {
            filterItems.clear();
            String term = getFilterField().getText();
            for (int i=0; i<items.size(); i++)
                if (items.get(i).toString().indexOf(term, 0) != -1)
                    filterItems.add (items.get(i));
            fireContentsChanged (this, 0, getSize());
        }
    }

    // inner class provides filter-by-keystroke field
    class FilterField extends JTextField implements DocumentListener {
        public FilterField (int width) {
            super(width);
            getDocument().addDocumentListener (this);
        }
        public void changedUpdate (DocumentEvent e) { ((FilterModel)getModel()).refilter(); }
        public void insertUpdate (DocumentEvent e) {((FilterModel)getModel()).refilter(); }
        public void removeUpdate (DocumentEvent e) {((FilterModel)getModel()).refilter(); }
    }
}
