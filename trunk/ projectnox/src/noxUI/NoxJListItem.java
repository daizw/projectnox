package noxUI;

import javax.swing.ImageIcon;

import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.PeerAdvertisement;

public abstract class NoxJListItem {
	private ImageIcon portrait;
	private String nickname;
	private String sign;
	private ID UUID;
	protected ItemStatus stat;

	NoxJListItem(ImageIcon portr, String nick, String signstr, ID uuid) {
		this.portrait = portr;
		this.nickname = nick;
		this.sign = signstr;
		this.UUID = uuid;
	}
	
	public abstract void setStatus(Advertisement adv);
	public abstract ItemStatus getStatus();
	/**
	 * 获取头像
	 * @return 头像ImageIcon
	 */
	protected ImageIcon getPortrait() {
		return portrait;
	}
	/**
	 * 获取昵称
	 * @return 昵称Text
	 */
	protected String getNick() {
		return nickname;
	}
	/**
	 * 获取签名档
	 * @return 签名档Text
	 */
	protected String getSign() {
		return sign;
	}
	/**
	 * 获取该Item的ID
	 * @return 组的PeerGroupID,或者Peer的PeerID
	 */
	protected ID getUUID(){
		return UUID;
	}
}

class PeerItem extends NoxJListItem{
	PeerItem(ImageIcon portr, PeerAdvertisement adv){
		super(portr, adv.getName(), adv.getDescription(), adv.getPeerID());
	}

	/**
	 * 根据adv来确定...混乱了...
	 */
	@Override
	public	void setStatus(Advertisement adv) {
		// TODO 根据adv设置状态
		stat = ItemStatus.ONLINE;
	}

	@Override
	public ItemStatus getStatus() {
		// TODO Auto-generated method stub
		return stat;
	}
}

class GroupItem extends NoxJListItem{
	private int onlineCount;
	private int memberCount;
	GroupItem(ImageIcon portr, String name, String signstr, PeerGroupID uuid, int oc, int mc){
		super(portr, name, signstr, uuid);
		onlineCount = oc;
		memberCount = mc;
	}
	public int getOnlineCount(){
		return onlineCount;
	}
	public int getCount(){
		return memberCount;
	}
	@Override
	public	void setStatus(Advertisement adv) {
		// TODO 根据adv设置状态
		stat = ItemStatus.ONLINE;
	}
	@Override
	public ItemStatus getStatus() {
		// TODO Auto-generated method stub
		return stat;
	}
}
