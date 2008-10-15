package nox.ui.common;

import javax.swing.ImageIcon;

import net.jxta.protocol.PeerAdvertisement;

@SuppressWarnings("serial")
public class PeerItem extends NoxJListItem {
	public PeerItem(ImageIcon portr, PeerAdvertisement adv) {
		super(portr, adv.getName(), adv.getDescription(), adv.getPeerID());
	}
}