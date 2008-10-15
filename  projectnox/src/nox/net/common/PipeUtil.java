package nox.net.common;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.protocol.PipeAdvertisement;

public class PipeUtil {
	private static final Logger LOG = Logger.getLogger(PipeUtil.class.getName());
	
	private static final long WaitTime = LANTimeLimit.UNIT_TIME;		// 远程发现的等待时间，需根据网络情况调整
	private static final int MAXRETRIES = LANTimeLimit.FETCH_PIPEADV_MAXRETRIES;		// 远程发现时的重试次数，需根据网络情况调整
	
	/**
	 * 获取管道广告，首先从本地缓存中发现管道广告，如果没有发现，再从远程发现管道广告，如果没有发现，那么创建管道广告。
	 * @param pg		用于发现或创建管道广告的节点组
	 * @param name		用于发现或创建管道广告的名称，如果本地或远程都没有发现管道广告，那么将使用该名称创建管道广告，因此该名称不要使用通配符
	 * @param type		创建管道广告的类型，目前支持3种基本类型
	 * 						JxtaUnicast			单向、不安全和不可靠
	 * 						JxtaUnicastSecure	单向、安全（使用TLS）
	 * 						JxtaPropagate		传播广告 
	 * @param pipeId	指定生成管道广告的PipeId,如果该值为空或null，那么使用系统生成的Id
	 * @param remotePublish	如果创建管道广告，那么是否远程发布
	 * @return			在节点组内发现或创建的广告对象
	 */
	public static PipeAdvertisement getPipeAdv(PeerGroup pg, String name, String type, String pipeId, boolean remotePublish) {
		PipeAdvertisement myAdv = null;
		
		try {
			myAdv = findPipeAdv(pg, name);
			
			if(myAdv == null) {
				// We did not find the pipe advertisement, so create one
				LOG.info("Could not find the Pipe Advertisement");
				
				// Create a pipe advertisement
				myAdv = createAdv(pg, name, type, pipeId);
				
				// We have the advertisement; publish it into our local cache or remote peer
				publish(pg, myAdv, remotePublish);
				
				LOG.info("Created the Pipe Advertisement");
			}
		} catch(Exception e) {
			LOG.severe("Could not get pipe Advertisement");
			return null;
		}
		
		return myAdv;
	}
	
	/**
	 * 获取管道广告，首先从本地缓存中发现管道广告，如果没有发现，再从远程发现管道广告，如果没有发现，那么使用系统生成的Id创建管道广告。
	 * @param pg		用于发现或创建管道广告的节点组
	 * @param name		用于发现或创建管道广告的名称，如果本地或远程都没有发现管道广告，那么将使用该名称创建管道广告，因此该名称不要使用通配符
	 * @param type		创建管道广告的类型，目前支持3种基本类型
	 * 						JxtaUnicast			单向、不安全和不可靠
	 * 						JxtaUnicastSecure	单向、安全（使用TLS）
	 * 						JxtaPropagate		传播广告 
	 * @param remotePublish	如果创建管道广告，那么是否远程发布
	 * @return			在节点组内发现或创建的广告对象
	 */
	public static PipeAdvertisement getPipeAdv(PeerGroup pg, String name, String type, boolean remotePublish) {
		return getPipeAdv(pg, name, type, null, remotePublish);
	}
	
	/**
	 * 获取管道广告，首先从本地缓存中发现管道广告，如果没有发现，那么创建管道广告。
	 * @param pg		用于发现或创建管道广告的节点组
	 * @param name		用于发现或创建管道广告的名称，如果本地或远程都没有发现管道广告，那么将使用该名称创建管道广告，因此该名称不要使用通配符
	 * @param type		创建管道广告的类型，目前支持3种基本类型
	 * 						JxtaUnicast			单向、不安全和不可靠
	 * 						JxtaUnicastSecure	单向、安全（使用TLS）
	 * 						JxtaPropagate		传播广告 
	 * @param pipeId	指定生成管道广告的PipeId,如果该值为空或null，那么使用系统生成的Id
	 * @param remotePublish	如果创建管道广告，那么是否远程发布
	 * @return			在节点组内发现或创建的广告对象
	 */
	public static PipeAdvertisement getPipeAdvWithoutRemoteDiscovery(PeerGroup pg, String name, String type, String pipeId, boolean remotePublish) {
        PipeAdvertisement pa = searchLocal(pg, name);

        if (pa == null) {
            pa = createAdv(pg, name, type, pipeId);

            publish(pg, pa, remotePublish);
        }else
        	System.out.println("Find a pipe adv locally, using it.");

        return pa;
	}
	
	/**
	 * 获取管道广告，首先从本地缓存中发现管道广告，如果没有发现，那么使用系统生成的Id创建管道广告。
	 * @param pg		用于发现或创建管道广告的节点组
	 * @param name		用于发现或创建管道广告的名称，如果本地或远程都没有发现管道广告，那么将使用该名称创建管道广告，因此该名称不要使用通配符
	 * @param type		创建管道广告的类型，目前支持3种基本类型
	 * 						JxtaUnicast			单向、不安全和不可靠
	 * 						JxtaUnicastSecure	单向、安全（使用TLS）
	 * 						JxtaPropagate		传播广告 
	 * @param remotePublish	如果创建管道广告，那么是否远程发布
	 * @return			在节点组内发现或创建的广告对象
	 */
	public static PipeAdvertisement getPipeAdvWithoutRemoteDiscovery(PeerGroup pg, String name, String type, boolean remotePublish) {
		return getPipeAdvWithoutRemoteDiscovery(pg, name, type, null, remotePublish);
	}
	
	/**
	 * 同步方式发现一定时间内得到的最新管道广告
	 * @param pg	节点组，在该节点组内发现管道广告
	 * @param name	管道广告名称，可使用通配符
	 * @param time 时间上限
	 * @return		最新的管道广告对象，如果没有找到或发现过程发生异常，那么返回null
	 */
	public static PipeAdvertisement findNewestPipeAdv(PeerGroup pg, String name, long time) {
		DiscoveryService discovery = pg.getDiscoveryService();
		
		int count = (int) (time/WaitTime); // Discovery retry count
		
		PipeAdvertisement myAdv = null;
		
		try {
			LOG.info("Attempting to Discover the pipe advertisement");
			
			// Check if we have already published ourselves
			while(count-- > 0) {
				// We did not find the advertisement locally;
				// send a remote request
				// remote advs will be stored in local cache
				discovery.getRemoteAdvertisements(null, DiscoveryService.ADV, PipeAdvertisement.NameTag, name, Integer.MAX_VALUE, null);
				
				// Sleep to allow time for peers to respond to the discovery request
				try {
					Thread.sleep(WaitTime);
				} catch(InterruptedException e) {
					// ignored
				}
			}
			
			//发送了N个远程搜索请求后, 检查本地最新的管道广告.
			myAdv = searchLocal(pg, name);
			
		} catch(Exception e) {
			LOG.severe("Could not get pipe Advertisement");
			return null;
		}
		
		if(myAdv != null) {
			LOG.info(myAdv.toString());
		} else {
			LOG.info("myAdv is null.");
		}
		
		return myAdv;
	}
	
	/**
	 * 同步方式发现一定时间内最先得到的管道广告
	 * @param pg	节点组，在该节点组内发现管道广告
	 * @param name	管道广告名称，可使用通配符
	 * @return		管道广告对象，如果没有找到或发现过程发生异常，那么返回null
	 * @deprecated
	 */
	public static PipeAdvertisement findPipeAdv(PeerGroup pg, String name) {
		DiscoveryService discovery = pg.getDiscoveryService();
		
		int count = MAXRETRIES; // Discovery retry count
		
		PipeAdvertisement myAdv = null;
		
		try {
			LOG.info("Attempting to Discover the pipe advertisement");
			
			// Check if we have already published ourselves
			while(count-- > 0) {
				// First, check locally if the advertisement is cached
				myAdv = searchLocal(pg, name);
				
				// If we found our pipe advertisement, we are done
				if(myAdv != null)
					break;

				// We did not find the advertisement locally;
				// send a remote request
				// remote advs will be stored in local cache
				discovery.getRemoteAdvertisements(null, DiscoveryService.ADV, PipeAdvertisement.NameTag, name, Integer.MAX_VALUE, null);
				
				// Sleep to allow time for peers to respond to the discovery request
				try {
					Thread.sleep(WaitTime);
				} catch(InterruptedException e) {
					// ignored
				}
			}
		} catch(Exception e) {
			LOG.severe("Could not get pipe Advertisement");
			return null;
		}
		
		if(myAdv != null) {
			LOG.info(myAdv.toString());
		} else {
			LOG.info("myAdv is null.");
		}
		
		return myAdv;
	}
	
	/**
	 * 按给定的pipeID同步方式发现管道广告
	 * @param pg 节点组，在该节点组内发现管道广告
	 * @param pipeID 要查找的管道广告的管道ID
	 * @return 管道广告对象，如果没有找到或发现过程发生异常，那么返回null
	 */
	public static PipeAdvertisement findPipeAdvByPipeID(PeerGroup pg, String pipeID) {
		DiscoveryService discovery = pg.getDiscoveryService();
		
		int count = MAXRETRIES; // Discovery retry count
		
		PipeAdvertisement myAdv = null;
		
		try {
			LOG.info("Attempting to Discover the pipe advertisement");
			
			// Check if we have already published ourselves
			while(count-- > 0) {
				// First, check locally if the advertisement is cached
				myAdv = searchLocalByPipeID(pg, pipeID);
				
				// If we found our pipe advertisement, we are done
				if(myAdv != null)
					break;

				// We did not find the advertisement locally;
				// send a remote request
				// remote advs will be stored in local cache
				discovery.getRemoteAdvertisements(null, DiscoveryService.ADV, PipeAdvertisement.IdTag, pipeID, 10, null);
				
				// Sleep to allow time for peers to respond to the discovery request
				try {
					Thread.sleep(WaitTime);
				} catch(InterruptedException e) {
					// ignored
				}
			}
		} catch(Exception e) {
			LOG.severe("Could not get pipe Advertisement");
			return null;
		}
		
		if(myAdv != null) {
			LOG.info(myAdv.toString());
		} else {
			LOG.info("myAdv is null.");
		}
		
		return myAdv;
	}
	
	/**
	 * 创建管道广告
	 * @param pg		用于创建管道广告的节点组
	 * @param name		使用该名称创建管道广告
	 * @param type		创建管道广告的类型，目前支持3种基本类型
	 * 						JxtaUnicast			单向、不安全和不可靠
	 * 						JxtaUnicastSecure	单向、安全（使用TLS）
	 * 						JxtaPropagate		传播广告 
	 * @param pipeId	指定生成管道广告的PipeId,如果该值为空或null，那么使用系统生成的Id
	 * @return			在节点组内创建的广告对象
	 */
    public static PipeAdvertisement createAdv(PeerGroup pg, String name, String type, String pipeId) {
    	PipeAdvertisement pa = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());

    	try {
			pa.setPipeID((pipeId == null || pipeId.equals(""))?
					IDFactory.newPipeID(pg.getPeerGroupID())
					:(PipeID) IDFactory.fromURI(new URI(pipeId)));
	    	pa.setName(name);
	    	pa.setType(type);
	    	pa.setDescription(new Date().getTime() + "");
		} catch (URISyntaxException e) {
			LOG.warning("a string could not be parsed as a URI reference");
			e.printStackTrace();
		}

    	return pa;
    }
	
    /**
     * 本地搜索广告对象, 返回得到的最新的广告
     * @param pg		用于搜索管道广告的节点组
     * @param name		用于搜索管道广告的名称，可使用通配符
     * @return			在节点组内创建的广告对象（返回发现的最新的管道广告）
     */
    public static PipeAdvertisement searchLocal(PeerGroup pg, String name) {
        DiscoveryService discoveryService = pg.getDiscoveryService();
        Enumeration<Advertisement> pas = null;
        try {
            pas = discoveryService.getLocalAdvertisements(DiscoveryService.ADV, PipeAdvertisement.NameTag, name);
        } catch (IOException e) {
            return null;
        }
        PipeAdvertisement curpa = null;
        PipeAdvertisement newestPA = null;
        
        while (pas.hasMoreElements()) {
        	try{
        		curpa = (PipeAdvertisement) pas.nextElement();
                
                if (curpa.getName().equals(name)) {
                	 if(newestPA == null)
                     	newestPA = curpa;
                     else if(Long.parseLong(newestPA.getDescription()) < Long.parseLong(curpa.getDescription()))
                    	 newestPA = curpa;
                }
        	}catch(Exception e){
        		System.out.println("Find a non-pipeAdv, just pass");
        		e.printStackTrace();
        	}
        }
        return newestPA;
    }
    
    /**
     * 按给定的pipeID本地搜索广告对象
     * @param pg		用于搜索管道广告的节点组
     * @param pipeID 要查找的管道广告的管道ID
     * @return			在节点组内创建的广告对象（返回发现的最新的管道广告）
     */
    public static PipeAdvertisement searchLocalByPipeID(PeerGroup pg, String pipeID) {
        DiscoveryService discoveryService = pg.getDiscoveryService();
        Enumeration<Advertisement> pas = null;
        try {
            pas = discoveryService.getLocalAdvertisements(DiscoveryService.ADV, PipeAdvertisement.IdTag, pipeID);
        } catch (IOException e) {
            return null;
        }
        PipeAdvertisement curpa = null;
        PipeAdvertisement newestPA = null;
        
        while (pas.hasMoreElements()) {
        	try{
        		curpa = (PipeAdvertisement) pas.nextElement();
                
                if (curpa.getID().equals(pipeID)) {
                	 if(newestPA == null)
                     	newestPA = curpa;
                     else if(Long.parseLong(newestPA.getDescription()) < Long.parseLong(curpa.getDescription()))
                    	 newestPA = curpa;
                }
        	}catch(Exception e){
        		System.out.println("Find a non-pipeAdv, just pass");
        		e.printStackTrace();
        	}
        }
        return newestPA;
    }
    
	/**
	 * 本地发布广告
	 * @param pg 组
	 * @param pa 管道广告
	 */
    public static void publish(PeerGroup pg, PipeAdvertisement pa) {
        publish(pg, pa, false);
    }

    /**
     * 本地发布广告, 如果remote为真则远程发布
     * @param pg 组
     * @param pa 管道广告
     * @param remote 是否远程发布 
     */
    public static void publish(PeerGroup pg, PipeAdvertisement pa, boolean remote) {
        DiscoveryService ds = pg.getDiscoveryService();

        try {
            ds.publish(pa);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (remote) {
             ds.remotePublish(pa, DiscoveryService.ADV);
        }
    }

	public static void flushOldPipeAdvs(PeerGroup pg, String name) {
		DiscoveryService discoveryService = pg.getDiscoveryService();
        Enumeration<Advertisement> pas = null;
        try {
            pas = discoveryService.getLocalAdvertisements(DiscoveryService.ADV, PipeAdvertisement.NameTag, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PipeAdvertisement curpa = null;
        PipeAdvertisement newestPA = null;
        
        while (pas.hasMoreElements()) {
        	try{
        		curpa = (PipeAdvertisement) pas.nextElement();
        		if(newestPA == null)
                 	newestPA = curpa;
                 else if(Long.parseLong(newestPA.getDescription()) < Long.parseLong(curpa.getDescription()))
                 {
                	 discoveryService.flushAdvertisement(newestPA);
                	 newestPA = curpa;
                 }else
                	 discoveryService.flushAdvertisement(curpa);
        	}catch(Exception e){
        		System.out.println("Find a non-pipeAdv, just pass");
        		e.printStackTrace();
        	}
        }
	}
}