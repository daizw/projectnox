package nox.net;

import net.jxta.document.Advertisement;
/**
 * 发现事件处理纯虚类, 用于继承
 * @author shinysky
 *
 */
abstract public class DiscoveryEventHandler {
	abstract public void eventOccured(Advertisement adv, Object src, long delay);
}
