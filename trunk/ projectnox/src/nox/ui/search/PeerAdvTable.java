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
	 * 向搜索结果列表中添加一行. 添加之前判断表中是否已有该元素, 没有才需要添加. TODO 以后根据具体情况还需要改
	 * 
	 * @param adv
	 *            获取的广告
	 * @param src
	 *            从src处获取的广告
	 * @param delay
	 *            时间延迟
	 */
	@Override
	public void addRow(Advertisement adv, Object src, long delay) {
		/**
		 * 如果已经添加过或者是自己的adv, 就不添加, 直接返回
		 * 暂时cast为PeerAdv类型
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
		 * 同步广告向量
		 */
		advVector.add(adv);
		//System.out.println("Add a Adv to the vector:" + advVector.size());
	}
}