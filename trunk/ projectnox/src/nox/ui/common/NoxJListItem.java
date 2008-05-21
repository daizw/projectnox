package nox.ui.common;

import java.io.Serializable;
import java.util.Date;

import javax.swing.ImageIcon;

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
		this.stat = ItemStatus.UNKNOWN;
		updateTimeStamp();
	}
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
	 * ��ȡ״̬
	 * 
	 * @return ��ǰ(����)״̬
	 */
	public ItemStatus getOnlineStatus(){
		return stat;
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
		updateTimeStamp();
	}

	/**
	 * ��������(�ǳ�)
	 */
	public void setName(String n){
		name = n;
		updateTimeStamp();
	}
	/**
	 * ����ǩ����(����)
	 */
	public void setDesc(String desc){
		discription = desc;
		updateTimeStamp();
	}
	/**
	 * ����״̬
	 * 
	 * @param adv
	 *            ���ݸù����ĳԪ������?
	 */
	public void setOnlineStatus(ItemStatus st) {
		stat = st;
		updateTimeStamp();
	}
}