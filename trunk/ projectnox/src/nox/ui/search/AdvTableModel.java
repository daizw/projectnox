package nox.ui.search;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class AdvTableModel extends DefaultTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AdvTableModel(Object[][] data, Object[] columns) {
		super(data, columns);
	}

	public int getColumnCount() {
		return 4;
	}

	// public int getRowCount() { return 10; }

	/**
	 * ��Ԫ���Ƿ�ɱ༭
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/*
	 * public int getColumns(){ return 3; }
	 */
	/**
	 * ����
	 */
	public Class<?> getColumnClass(int column) {
		Vector<?> v = (Vector<?>) dataVector.elementAt(0);
		return v.elementAt(column).getClass();
	}
}
