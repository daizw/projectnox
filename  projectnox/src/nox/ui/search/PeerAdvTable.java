package nox.ui.search;

import net.jxta.document.Advertisement;
import net.jxta.protocol.PeerAdvertisement;
import nox.net.common.NoxToolkit;

public class PeerAdvTable extends AdvTable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PeerAdvTable(AdvTableModel model) {
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