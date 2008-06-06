/**
 * 
 */
package nox.net.common;

import java.util.Hashtable;
import java.util.Map;

import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import nox.net.group.GroupConnectionHandler;
import nox.net.peer.PeerConnectionHandler;
import nox.ui.common.NoxJListItem;
import nox.ui.me.Cheyenne;
import nox.ui.search.AdvTable;
/**
 * NoX ���߰�
 * @author shinysky
 */
public class NoxToolkit {
	/**
	 * �����¼�������,
	 * Ŀǰ����
	 * @author shinysky
	 *
	 */
	public class HuntingEventHandler extends DiscoveryEventHandler{
		AdvTable table;
		public HuntingEventHandler(AdvTable tab){
			table = tab;
		}
		public void setAdvTable(AdvTable tab){
			table = tab;
		}
		public void eventOccured(AdvTable searchResultTable, Advertisement adv, Object src, long delay) {
			try{
				searchResultTable.addRow(adv, src, delay);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		@Override
		public void eventOccured(Advertisement adv, Object src, long delay) {
			eventOccured(table, adv, src, delay);
		}
	}
	/**
	 * ���ڼ������״̬���¼�������
	 * @author shinysky
	 *
	 */
	public class CheckStatusEventHandler extends DiscoveryEventHandler{
		NoxJListItem listItem;
		
		public CheckStatusEventHandler(NoxJListItem item){
			listItem = item;
		}
		public void setItem(NoxJListItem it){
			listItem = it;
		}

		@Override
		public void eventOccured(Advertisement adv, Object src, long delay) {
			eventOccured(listItem, adv, src, delay);
		}

		public void eventOccured(NoxJListItem item, Advertisement adv,
				Object src, long delay) {
			/*try{
				item.setStatus(adv);
			}catch(Exception ex){
				ex.printStackTrace();
			}*/
		}
	}
	
	private static JXTANetwork network;
	private static NetworkManager manager;
	private static NetworkConfigurator configer;
	//private static AdvHunter advhunter;
	private static HuntingEventHandler hehandler;
	private static CheckStatusEventHandler cshandler;
	private static Cheyenne cheyenne;
	//TODO ����ʹ��Map, ʹ�÷����ɲο�tutorial�е�PropagatedPipServer��
	//private static Set<ChatroomUnit> chatrooms;
	//private transient Map<PeerID, JxtaBiDiPipe> bidipipeCache = new Hashtable<PeerID, JxtaBiDiPipe>();
	private static transient Map<PeerID, PeerConnectionHandler> pconnHdlerCache = new Hashtable<PeerID, PeerConnectionHandler>();
	private static transient Map<PeerGroupID, GroupConnectionHandler> gconnHdlerCache = new Hashtable<PeerGroupID, GroupConnectionHandler>();
	
	private static float Opacity = 100;
	
	public NoxToolkit(){ }
	
	public NoxToolkit(JXTANetwork nw, NetworkManager mng, NetworkConfigurator conf, /*AdvHunter ah,*/ HuntingEventHandler heh, CheckStatusEventHandler csh){
		network = nw;
		manager = mng;
		configer = conf;
		//advhunter = ah;
		hehandler = heh;
		cshandler = csh;
	}
	
	public static JXTANetwork getNetwork(){
		return network;
	}
	public static NetworkManager getNetworkManager(){
		return manager;
	}
	public static NetworkConfigurator getNetworkConfigurator(){
		return configer;
	}
	/*public AdvHunter getAdvHunter(){
		return advhunter;
	}*/
	public static HuntingEventHandler getHuntingEventHandler(){
		return hehandler;
	}
	public static CheckStatusEventHandler getCheckStatusEventHandler(){
		return cshandler;
	}
	
	public static void setCheyenne(Cheyenne chy){
		cheyenne = chy;
	}
	public static Cheyenne getCheyenne(){
		return cheyenne;
	}
	public static float getOpacity(){
		return Opacity;
	}
	public static void setOpacity(float opa){
		Opacity = opa;
	}
	////++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	////+++++                       �ڵ�                                                             +++++++
	////++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * ����ID��Ӧ�Ľڵ����ӹ�����. ���������ID��Ϣ, ����null.
	 * @param pid Ҫ��ѯ��ID
	 * @return �ڵ����ӹ�����
	 */
	public static PeerConnectionHandler getPeerConnectionHandler(PeerID pid){
		if(pconnHdlerCache.containsKey(pid)){
			return pconnHdlerCache.get(pid);
		}
		return null;
	}
	/**
	 * ע��ڵ����ӹ�����. �����������Ϊ��, �ҹ�ϣ�����޸�id, ��ע��֮, ����true; ���򷵻�false.
	 * @param pid Ҫע���ID
	 * @param handler ��ID��Ӧ�Ľڵ����ӹ�����
	 * @return ע��ɹ���. ������ղ������߹�ϣ�������иù������򷵻�false.
	 */
	public static boolean registerPeerConnectionHandler(PeerID pid, PeerConnectionHandler handler){
		if(pid != null && handler != null
				&& !pconnHdlerCache.containsKey(pid)){
			pconnHdlerCache.put(pid, handler);
			return true;
		}
		return false;
	}
	/**
	 * ǿ��ע��ڵ����ӹ�����. �����������Ϊ��, ��ע��֮, ����true; ���򷵻�false.
	 * ��������Ѻ���key, ��ɵ�value�ᱻ����.
	 * 
	 * @param pid Ҫע���ID
	 * @param handler ��ID��Ӧ�Ľڵ����ӹ�����
	 * @return ע��ɹ���. ������ղ����򷵻�false.
	 */
	public static boolean forceRegisterPeerConnectionHandler(PeerID pid, PeerConnectionHandler handler){
		if(pid != null && handler != null){
			pconnHdlerCache.put(pid, handler);
			return true;
		}
		return false;
	}
	////++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	////+++++                       ��                                                               +++++++
	////++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * ����ID��Ӧ�Ľڵ����ӹ�����. ���������ID��Ϣ, ����null.
	 * @param gid Ҫ��ѯ��ID
	 * @return �ڵ����ӹ�����
	 */
	public static GroupConnectionHandler getGroupConnectionHandler(PeerGroupID gid){
		if(gconnHdlerCache.containsKey(gid)){
			return gconnHdlerCache.get(gid);
		}
		return null;
	}
	/**
	 * ע��ڵ����ӹ�����. �����������Ϊ��, �ҹ�ϣ�����޸�id, ��ע��֮, ����true; ���򷵻�false.
	 * @param gid Ҫע���ID
	 * @param handler ��ID��Ӧ�Ľڵ����ӹ�����
	 * @return ע��ɹ���. ������ղ������߹�ϣ�������иù������򷵻�false.
	 */
	public static boolean registerGroupConnectionHandler(PeerGroupID gid, GroupConnectionHandler handler){
		if(gid != null && handler != null
				&& !gconnHdlerCache.containsKey(gid)){
			gconnHdlerCache.put(gid, handler);
			return true;
		}
		return false;
	}
	/**
	 * ǿ��ע��ڵ����ӹ�����. �����������Ϊ��, ��ע��֮, ����true; ���򷵻�false.
	 * ��������Ѻ���key, ��ɵ�value�ᱻ����.
	 * 
	 * @param gid Ҫע���ID
	 * @param handler ��ID��Ӧ�Ľڵ����ӹ�����
	 * @return ע��ɹ���. ������ղ����򷵻�false.
	 */
	public static boolean forceRegisterGroupConnectionHandler(PeerGroupID gid, GroupConnectionHandler handler){
		if(gid != null && handler != null){
			gconnHdlerCache.put(gid, handler);
			return true;
		}
		return false;
	}
	/**
	 * ��cache���Ƴ�connection handler<br>
	 * һ�㽫������ӵ���������ֱ��ɾ��ʱ��Ҫʹ�ô˺���
	 * 
	 * @param pid ���ѵ�ID
	 * @return ����ú���IDΪ�ջ򲻴�����cache�У�����false�����򷵻�true
	 */
	public static boolean removePeer(PeerID pid){
		if(pid != null && pconnHdlerCache.containsKey(pid)){
			PeerConnectionHandler handler = pconnHdlerCache.get(pid);
			handler.stop();
			pconnHdlerCache.remove(pid);
			return true;
		}
		return false;
	}
	/**
	 * �˳���
	 * 
	 * @param gid Ҫ�˳������ID
	 * @return �������Ϊ�ջ򲻴�����cache�У�����false�����򷵻�true
	 */
	public static boolean resignGroup(PeerGroupID gid){
		if(gid != null && gconnHdlerCache.containsKey(gid)){
			GroupConnectionHandler handler = gconnHdlerCache.get(gid);
			handler.resign();
			gconnHdlerCache.remove(gid);
			return true;
		}
		return false;
	}
}
