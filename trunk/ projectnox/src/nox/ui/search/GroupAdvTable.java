package nox.ui.search;

import net.jxta.document.Advertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import nox.net.common.NoxToolkit;

public class GroupAdvTable extends AdvTable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GroupAdvTable(AdvTableModel model) {
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