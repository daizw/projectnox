package nox.net;

import net.jxta.document.Advertisement;
/**
 * �����¼���������, ���ڼ̳�
 * @author shinysky
 *
 */
abstract public class DiscoveryEventHandler {
	abstract public void eventOccured(Advertisement adv, Object src, long delay);
}
