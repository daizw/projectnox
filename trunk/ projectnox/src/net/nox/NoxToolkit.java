/**
 * 
 */
package net.nox;

import net.jxta.document.Advertisement;
import net.jxta.platform.NetworkManager;
import noxUI.NoxJListItem;
import noxUI.SearchingFrame.AdvTable;

/**
 * NoX ¹¤¾ß°ü
 * @author shinysky
 */
public class NoxToolkit {
	public class HuntingEventHandler extends DiscoveryEventHandler{
		AdvTable table;
		public HuntingEventHandler(AdvTable tab){
			table = tab;
		}
		public void setAdvTable(AdvTable tab){
			table = tab;
		}
		@Override
		public void eventOccured(Advertisement adv, Object src, long delay) {
			try{
				table.addRow(adv, src, delay);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	public class CheckStatusEventHandler extends DiscoveryEventHandler{
		NoxJListItem listItem;
		
		public CheckStatusEventHandler(NoxJListItem item){
			listItem = item;
		}

		@Override
		public void eventOccured(Advertisement adv, Object src, long delay) {
			try{
				listItem.setStatus(adv);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	private static NetworkManager manager;
	//private static AdvHunter advhunter;
	private static HuntingEventHandler hehandler;
	private static CheckStatusEventHandler cshandler;
	
	public NoxToolkit(){
	}
	
	public NoxToolkit(NetworkManager mng, /*AdvHunter ah,*/ HuntingEventHandler heh, CheckStatusEventHandler csh){
		manager = mng;
		//advhunter = ah;
		hehandler = heh;
		cshandler = csh;
	}
	public NetworkManager getNetworkManager(){
		return manager;
	}
	/*public AdvHunter getAdvHunter(){
		return advhunter;
	}*/
	public HuntingEventHandler getHuntingEventHandler(){
		return hehandler;
	}
	public CheckStatusEventHandler getCheckStatusEventHandler(){
		return cshandler;
	}
}
