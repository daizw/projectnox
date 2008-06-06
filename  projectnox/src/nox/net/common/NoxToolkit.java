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
 * NoX 工具包
 * @author shinysky
 */
public class NoxToolkit {
	/**
	 * 搜索事件处理器,
	 * 目前无用
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
	 * 用于检查在线状态的事件处理器
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
	//TODO 考虑使用Map, 使用方法可参考tutorial中的PropagatedPipServer类
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
	////+++++                       节点                                                             +++++++
	////++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 返回ID对应的节点连接管理器. 如果不含该ID信息, 返回null.
	 * @param pid 要查询的ID
	 * @return 节点连接管理器
	 */
	public static PeerConnectionHandler getPeerConnectionHandler(PeerID pid){
		if(pconnHdlerCache.containsKey(pid)){
			return pconnHdlerCache.get(pid);
		}
		return null;
	}
	/**
	 * 注册节点连接管理器. 如果参数都不为空, 且哈希表中无该id, 则注册之, 返回true; 否则返回false.
	 * @param pid 要注册的ID
	 * @param handler 该ID对应的节点连接管理器
	 * @return 注册成功否. 如果含空参数或者哈希表中已有该管理器则返回false.
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
	 * 强行注册节点连接管理器. 如果参数都不为空, 则注册之, 返回true; 否则返回false.
	 * 如果表中已含该key, 则旧的value会被覆盖.
	 * 
	 * @param pid 要注册的ID
	 * @param handler 该ID对应的节点连接管理器
	 * @return 注册成功否. 如果含空参数则返回false.
	 */
	public static boolean forceRegisterPeerConnectionHandler(PeerID pid, PeerConnectionHandler handler){
		if(pid != null && handler != null){
			pconnHdlerCache.put(pid, handler);
			return true;
		}
		return false;
	}
	////++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	////+++++                       组                                                               +++++++
	////++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	/**
	 * 返回ID对应的节点连接管理器. 如果不含该ID信息, 返回null.
	 * @param gid 要查询的ID
	 * @return 节点连接管理器
	 */
	public static GroupConnectionHandler getGroupConnectionHandler(PeerGroupID gid){
		if(gconnHdlerCache.containsKey(gid)){
			return gconnHdlerCache.get(gid);
		}
		return null;
	}
	/**
	 * 注册节点连接管理器. 如果参数都不为空, 且哈希表中无该id, 则注册之, 返回true; 否则返回false.
	 * @param gid 要注册的ID
	 * @param handler 该ID对应的节点连接管理器
	 * @return 注册成功否. 如果含空参数或者哈希表中已有该管理器则返回false.
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
	 * 强行注册节点连接管理器. 如果参数都不为空, 则注册之, 返回true; 否则返回false.
	 * 如果表中已含该key, 则旧的value会被覆盖.
	 * 
	 * @param gid 要注册的ID
	 * @param handler 该ID对应的节点连接管理器
	 * @return 注册成功否. 如果含空参数则返回false.
	 */
	public static boolean forceRegisterGroupConnectionHandler(PeerGroupID gid, GroupConnectionHandler handler){
		if(gid != null && handler != null){
			gconnHdlerCache.put(gid, handler);
			return true;
		}
		return false;
	}
	/**
	 * 从cache中移除connection handler<br>
	 * 一般将好友添加到黑名单或直接删除时需要使用此函数
	 * 
	 * @param pid 好友的ID
	 * @return 如果该好友ID为空或不存在于cache中，返回false；否则返回true
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
	 * 退出组
	 * 
	 * @param gid 要退出的组的ID
	 * @return 如果该组为空或不存在于cache中，返回false；否则返回true
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
