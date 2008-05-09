package noxUI;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import net.jxta.document.Advertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.nox.NoxToolkit;

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
class AdvTableModel extends DefaultTableModel {
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
//+++++++++++++++++++++++++++++++++++++++++++++++++
class PeerAdvTable extends AdvTable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	PeerAdvTable(AdvTableModel model) {
		super(model);
	}
	@Override
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
	@Override
	public void addRow(Advertisement adv, Object src, long delay) {
		/**
		 * ����Ѿ���ӹ��������Լ���adv, �Ͳ����, ֱ�ӷ���
		 * ��ʱcastΪPeerAdv����
		 */
		if(adv instanceof PeerAdvertisement)
			;
		else{
			System.out.println("Just got a Adv which's not a PeerAdv, omitting it...");
			return;
		}
		
		if(((PeerAdvertisement) adv).getPeerID()
				.equals(NoxToolkit.getNetworkConfigurator().getPeerID()))
			return;
		//System.out.println("Got:	" + ((PeerAdvertisement) adv).getPeerID());
		//System.out.println("Me:	" + NoxToolkit.getNetworkConfigurator().getPeerID());
		
		int rows = model.getRowCount();
		for (int i = 0; i < rows; i++) {
			if (model.getValueAt(i, 2).equals(((PeerAdvertisement) adv).getPeerID()))
				return;
		}

		Object[] advitem = new Object[4];
		advitem[0] = ((PeerAdvertisement) adv).getName();
		advitem[1] = ((PeerAdvertisement) adv).getDescription();
		advitem[2] = ((PeerAdvertisement) adv).getPeerID();
		if (delay < 0)
			advitem[3] = "-";
		else
			advitem[3] = delay;

		model.addRow(advitem);
		/**
		 * ͬ���������
		 */
		advVector.add(adv);
		//System.out.println("Add a Adv to the vector:" + advVector.size());
	}
}
//+++++++++++++++++++++++++++++++++++++++++++++++++
class GroupAdvTable extends AdvTable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GroupAdvTable(AdvTableModel model) {
		super(model);
	}
	@Override
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
	@Override
	public void addRow(Advertisement adv, Object src, long delay) {
		/**
		 * ����Ѿ���ӹ��������Լ���adv, �Ͳ����, ֱ�ӷ���
		 * ��ʱcastΪPeerAdv����
		 */
		if(adv instanceof PeerGroupAdvertisement)
			;
		else{
			System.out.println("Just got a Adv which's not a PGA, omitting it...");
			return;
		}
		
		if(((PeerGroupAdvertisement) adv).getPeerGroupID()
				.equals(NoxToolkit.getNetworkConfigurator().getPeerID()))
			return;
		//System.out.println("Got:	" + ((PeerAdvertisement) adv).getPeerID());
		//System.out.println("Me:	" + NoxToolkit.getNetworkConfigurator().getPeerID());
		
		int rows = model.getRowCount();
		for (int i = 0; i < rows; i++) {
			if (model.getValueAt(i, 2).equals(((PeerGroupAdvertisement) adv).getPeerGroupID()))
				return;
		}

		Object[] advitem = new Object[4];
		advitem[0] = ((PeerGroupAdvertisement) adv).getName();
		advitem[1] = ((PeerGroupAdvertisement) adv).getDescription();
		advitem[2] = ((PeerGroupAdvertisement) adv).getPeerGroupID();
		if (delay < 0)
			advitem[3] = "-";
		else
			advitem[3] = delay;

		model.addRow(advitem);
		/**
		 * ͬ���������
		 */
		advVector.add(adv);
		//System.out.println("Add a Adv to the vector:" + advVector.size());
	}
}