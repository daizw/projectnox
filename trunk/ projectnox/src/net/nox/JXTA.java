package net.nox;

import net.jxta.document.Advertisement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;

public class JXTA {
	public static void main(String args[]) {
		System.out.println("Starting JXTA .......");
		JXTA myapp = new JXTA();
		myapp.startJXTA();
		System.exit(0);
	}
	//urn:jxta:uuid-59616261646162614A787461503250333EE1504910FE482FA5FDF3EF44684D7D03
	//urn:jxta:uuid-59616261646162614A78746150325033898A3DFB75AB4AD19E9995A4D4E498D103

	public void init(PeerGroup arg0, ID arg1, Advertisement arg2) throws PeerGroupException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int startApp(String[] args) {
        return 0;
    }

    public void stopApp() {
    }

    /**
	 * 创建默认组netPeerGroup 并输出相关的信息
	 */
    public void startJXTA() {
        PeerGroup pg = null;
        try {
            NetPeerGroupFactory npgf = new NetPeerGroupFactory();
            pg = npgf.getInterface();
        } catch (PeerGroupException e) {
            System.out.println("fatal error: Group creation failure");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Hello JXTA! :)");
        System.out.println("Group name = " + pg.getPeerGroupName());
        System.out.println("Group ID = " + pg.getPeerGroupID().toString());
        System.out.println("Peer name = " + pg.getPeerName());
        System.out.println("Peer ID = " + pg.getPeerID().toString());
    }
}
