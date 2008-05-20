package nox.ui;

import java.io.Serializable;
import java.util.Date;

import javax.swing.ImageIcon;

import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

public abstract class NoxJListItem implements Serializable{
	protected ImageIcon portrait;
	//�������㱣����, �����ڴ洢�����ݿ�ʱ����XML�ĵ��������л�!
	//ֻ���ٸĻ���, ֻ����һЩ��Ҫ��Ϣ.
	//Holy shit!
	//protected Advertisement adv;
	protected String name;
	protected String discription;
	protected ID id;
	protected ItemStatus stat;
	protected Long timeStamp;

	NoxJListItem(ImageIcon portr, String name, String disc, ID uuid) {
		this.portrait = portr;
		this.name = name;
		this.discription = disc;
		this.id = uuid;
		updateTimeStamp();
	}

	/**
	 * ����״̬
	 * 
	 * @param adv
	 *            ���ݸù����ĳԪ������?
	 */
	public abstract void setStatus(Advertisement adv);

	/**
	 * ��ȡ״̬
	 * 
	 * @return ��ǰ(����)״̬
	 */
	public abstract ItemStatus getOnlineStatus();

	/**
	 * ��ȡͷ��
	 * 
	 * @return ͷ��ImageIcon
	 */
	public ImageIcon getPortrait() {
		return portrait;
	}

	/**
	 * ��ȡ����(�ǳ�)
	 * 
	 * @return �ǳ�Text
	 */
	public String getName(){
		return name;
	}
	/**
	 * ��ȡǩ����(����)
	 * 
	 * @return ǩ����(����)Text
	 */
	public String getDesc(){
		return discription;
	}
	/**
	 * ��ȡ��Item��ID
	 * 
	 * @return ���PeerGroupID,����Peer��PeerID
	 */
	public ID getUUID() {
		return id;
	}

	/**
	 * ��ȡʱ���
	 * 
	 * @return ʱ���
	 */
	public Long getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * ��ʱ�������Ϊ��ǰʱ��
	 */
	public void updateTimeStamp() {
		this.timeStamp = new Date().getTime();
	}
	
	/**
	 * ����ͷ��
	 */
	public void setPortrait(ImageIcon portr) {
		portrait = portr;
	}

	/**
	 * ��������(�ǳ�)
	 */
	public void setName(String n){
		name = n;
	}
	/**
	 * ����ǩ����(����)
	 */
	public void setDesc(String desc){
		discription = desc;
	}
}

@SuppressWarnings("serial")
class PeerItem extends NoxJListItem {
	PeerItem(ImageIcon portr, PeerAdvertisement adv) {
		super(portr, adv.getName(), adv.getDescription(), adv.getPeerID());
	}

	/**
	 * ����adv��ȷ��...������...
	 */
	@Override
	public void setStatus(Advertisement adv) {
		// TODO ����adv����״̬
		stat = ItemStatus.ONLINE;
		updateTimeStamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ItemStatus getOnlineStatus() {
		return stat;
	}
}

@SuppressWarnings("serial")
class GroupItem extends NoxJListItem {
	private String password = null;
	private int onlineCount;
	private int memberCount;
	//private Object[] members;

	GroupItem(ImageIcon portr, PeerGroupAdvertisement adv, String password) {
		super(portr, adv.getName(), adv.getDescription(), adv.getPeerGroupID());
		//this.members = new PeerItem[0];
		this.password = password;
	}

	GroupItem(ImageIcon portr, PeerGroupAdvertisement adv) {
		this(portr, adv, null);
	}

	public String getPassword() {
		return password;
	}
	
	/*public Object[] getMembers(){
		return members;
	}*/

	public int getOnlineCount() {
		return onlineCount;
	}

	public int getCount() {
		return memberCount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStatus(Advertisement adv) {
		// TODO ����adv����״̬
		stat = ItemStatus.ONLINE;
		updateTimeStamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ItemStatus getOnlineStatus() {
		return stat;
	}
}
