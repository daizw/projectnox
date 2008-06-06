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
	 * 向搜索结果列表中添加一行. 添加之前判断表中是否已有该元素, 没有才需要添加. TODO 以后根据具体情况还需要改
	 * 
	 * @param adv
	 *            获取的广告
	 * @param src
	 *            从src处获取的广告
	 * @param delay
	 *            时间延迟
	 */
	public abstract void addRow(Advertisement adv, Object src, long delay);

	public Advertisement getAdvAt(int row) {
		System.out.println("Fetch a Adv from the vector, which has "
				+ advVector.size() + " Advertisements: ");
		//System.out.println(advs.get(row));
		return advVector.get(row);
	}
}
