package nox.ui.search;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import net.jxta.document.Advertisement;

public abstract class AdvTable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AdvTableModel model;
	Vector<Advertisement> advVector = new Vector<Advertisement>();

	AdvTable(AdvTableModel model) {
		super(model);
		this.model = model;
		this.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
	}
	
	public abstract void addRow(Advertisement adv);
	public abstract void addRow(Advertisement adv, long delay);

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
	public abstract void addRow(Advertisement adv, Object src, long delay);

	public Advertisement getAdvAt(int row) {
		System.out.println("Fetch a Adv from the vector, which has "
				+ advVector.size() + " Advertisements: ");
		//System.out.println(advs.get(row));
		return advVector.get(row);
	}
}
