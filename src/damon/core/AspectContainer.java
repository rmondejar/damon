/*****************************************************************************************
 * Damon : a Distributed AOP Middleware on top of a p2p Overlay Network
 * Copyright (C) 2006-2008 Ruben Mondejar
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *****************************************************************************************/
package damon.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdRange;
import rice.p2p.commonapi.NodeHandle;

import com.sun.management.OperatingSystemMXBean;

import damon.activation.AspectActivation;
import damon.activation.AspectActivator;
import damon.annotation.Abstractions;
import damon.annotation.DamonRequirement;
import damon.invokation.AspectInvokation;
import damon.invokation.AspectInvoker;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.reflection.MetaData;
import damon.reflection.ReflectionHandler;
import damon.reflection.thisEndPoint;
import damon.registry.Registry;
import damon.registry.RegistryException;
import damon.util.AnnotationParser;
import damon.util.Context;
import damon.util.Mutex;
import damon.util.XMLParser;
import damon.util.collections.Cache;
import damon.util.collections.SetHashtable;
import easypastry.cast.CastContent;
import easypastry.cast.CastFilter;
import easypastry.cast.CastHandler;
import easypastry.cast.CastListener;
import easypastry.core.PastryConnection;
import easypastry.dht.DHTException;

/**
 * Damon AspectManager remote class
 * 
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */

public class AspectContainer implements AspectActivator, AspectInvoker, ReflectionHandler {

	static Logger logger = Logger.getLogger(AspectContainer.class);
	 
	private PastryConnection conn;
	private CastHandler cast;
	private AspectStorage storage;
	private AspectHotDeployer deployer;

	private final int numMaxNeightbours = 12;
	private OperatingSystemMXBean mxbean;
	private Set<String> groups;
	private Hashtable<String, String> aspectIds;	
	

	private Mutex mutex;
	private Cache<String,Boolean> asynchroInvokCache;
	private Cache<String,Object> synchroInvokCache;
	
	private SetHashtable<String,Object[]> proxies;	
	private DamonPublisher publisher;

	public AspectContainer(PastryConnection conn, CastHandler cast,
			AspectStorage storage, AspectHotDeployer deployer) {

		this.conn = conn;
		this.cast = cast;
		this.storage = storage;
		this.deployer = deployer;
		this.mxbean = (OperatingSystemMXBean) ManagementFactory
				.getOperatingSystemMXBean();
		this.groups = new HashSet<String>();
		this.aspectIds = new Hashtable<String, String>();
		this.mutex = new Mutex();
		this.asynchroInvokCache = new Cache<String,Boolean>();
		this.synchroInvokCache = new Cache<String,Object>();
		this.proxies = new SetHashtable<String,Object[]>();
	}

	public void close() {
		cast.close();
		conn.close();
	}

	public boolean isRegistered(String url) {
		return groups.contains(url);
	}

	public void register(String url) {

		// init listeners
		cast.addDeliverListener(url, new CastListener() {

			public boolean contentAnycasting(CastContent content) {
				//System.out.println("CONTAINER ANYCASTING : "+content);
				logger.debug("anycasting : "+content);
				if (content instanceof AspectActivation) {
					AspectActivation activation = ((AspectActivation) content);
					return checkRequirements(activation.getName(), activation.isXML());
				} else if (content instanceof AspectInvokation) {
					AspectInvokation ai = ((AspectInvokation) content);
					
					boolean hasArrived = checkConditions(ai.getSubject(), ai.getId(), ai.getArgs());
					//if (hasArrived) System.out.println("HAS ARRIVED");
					//else System.out.println("HAS NOT ARRIVED");
					
					if (!hasArrived) { //it's a intermedium host
						
						boolean stop = true;
						
						//check filters first (around meta-pointcuts)
						Collection<CastFilter> filters = cast.getForwardFilters(ai.getSubject());
						for(CastFilter filter:filters) {
							stop &= filter.contentForwarding(content);
						}
						
						return !stop;
				    } 
					else return true;
					
				} else {
					return true;
				}

			}

			public void contentDelivery(CastContent content) {
				//System.out.println("CONTAINER DELIVER : "+content);
				// System.out.println("TEST DELIVER COUNT :
				// "+content.getNumOfHops());
				logger.debug("deliver : "+content);

				if (content instanceof AspectInvokation) {
					AspectInvokation ai = (AspectInvokation) content;
					invokationArrive(ai);				
				} else if (content instanceof AspectActivation) {
					activationArrive((AspectActivation) content);
				}
			}

			@Override
			public void hostUpdate(NodeHandle nh, boolean joined) {
				//System.out.println("NODE UPDATE : "+nh+" join : "+joined);
				logger.info("NODE UPDATE : "+nh+" join : "+joined);
				Collection<AspectRemoting> ars = thisEndPoint.getAllInstances();
				for (AspectRemoting ar : ars) {
				  new UpdateThread(ar,nh,joined).start();
				}
				
			}

		});

		groups.add(url);
		cast.subscribe(url);
	}
	
	class UpdateThread extends Thread {
		
		private AspectRemoting ar;
		private NodeHandle nh;
		private boolean joined;
		
		public UpdateThread(AspectRemoting ar, NodeHandle nh, boolean joined) {
			this.ar = ar;
			this.nh = nh;
			this.joined = joined;
			
		}
		public void run() {
			ar.update(nh,joined);		
		}
		
	}

	public void unregister(String url) {

		cast.unsubscribe(url);
		groups.remove(url);
		cast.removeDeliverListener(url);
	}

	public void addFilter(String url, String name, CastFilter filter) {
		cast.addForwardFilter(url, name, filter);		
	}

	public void removeFilter(String url, String name, CastFilter filter) {
		cast.removeForwardFiter(url, name);
	}

	private void send(Abstractions abstraction, CastContent content)
			throws RegistryException, DHTException {
		
		//System.out.println("AspectContainer : send -> "+content+"");
		logger.debug("sending : "+content);
		
		switch (abstraction) {
		
		case DIRECT:
			Collection<NodeHandle> nhs = getGroupMembers(content.getSubject());
			for (NodeHandle nh : nhs)
				cast.sendDirect(nh, content);
			break;
		case HOPPED:
			cast.sendHopped(content.getKey(), content);
			break;
		case ANY:
			cast.sendAnycast(content.getSubject(), content);
			break;
		case MANY:
			cast.sendManycast(content.getSubject(), content, content.getNum());
			break;
		case MULTI:			
			cast.sendMulticast(content.getSubject(), content);
			break;
		}

	}

	public void activate(NodeHandle nh, AspectActivation activation) {
		cast.sendDirect(nh, activation);
	}

	public void activate(Abstractions abstraction, AspectActivation activation)
			throws RegistryException, DHTException {
		send(abstraction, activation);
	}
	
	public void activateLocally(String aspectName, boolean isXML, MetaData md)
	throws Exception {
		Class aspectClass = null;
		
		try {
		  if (md.getClassLoader()==null) aspectClass = ClassLoader.getSystemClassLoader().loadClass(aspectName);
		  else aspectClass = md.getClassLoader().loadClass(aspectName);
		} catch(Exception e) {
			aspectClass = storage.retrieve(aspectName);	
		}
		activateLocally(aspectName, aspectClass, isXML, md);
	}

	private void activateLocally(String aspectName, Class aspectClass, boolean isXML, MetaData md)
			throws Exception {

		MetaData metadata = md;		
		 
		
		if (isXML) {
			byte[] bytes = storage.retrieveXML(aspectName);
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			metadata = XMLParser.parse(bais);
			aspectName = metadata.getName();
		}		
		
		if (metadata == null) {
			metadata = AnnotationParser.parse(aspectClass);
		}
		
		String url = metadata.getGroup();			
        
		if (thisEndPoint.isLocalHostInThisGroup(url) && !thisEndPoint.checkSingleton(url, aspectName, metadata.isSingleton())) {

			thisEndPoint.registerSharedAspect(aspectName, url);
			
			//System.out.println("deployer : "+deployer);			
			//System.out.println("aspectName : "+aspectName);			
			//System.out.println("aspectClass : "+aspectClass);
			//System.out.println("metadata CL : "+metadata.getClassLoader());

			String aspectId = deployer.deploy(aspectName, aspectClass, metadata.getClassLoader());

			thisEndPoint.enableAspectRemoting(aspectName, aspectClass, metadata);

			aspectIds.put(aspectName, aspectId);
			
			//startingPoint();
			
			/*
			
			if (metadata.isProxy()) {
				System.out.println("starting point 1 : AspectContainer : "+aspectName);
				try {
				Thread.currentThread().getContextClassLoader().getResources("proxy");
				} catch (Exception e) {}
			}
			else System.out.println(aspectName+" no proxy");
			*/
			
		}

	}
	
	///**
	// * Start Hook for AspectRemoting
	// */
	//public void startingPoint() {
	//	System.out.println("(0,0)");
	//}
	

	private boolean checkRequirements(String aspectName, boolean isXML) {

		boolean res = true;

		if (!isXML) {

			try {

				Class aspectClass = storage.retrieve(aspectName);
				Object aspect = aspectClass.newInstance();
									
				// TODO : sort by requirement priority
				Vector<Object[]> requirements = new Vector<Object[]>();
				Method[] methods = aspectClass.getMethods();
				for (Method m : methods) {
					DamonRequirement annotation = (DamonRequirement) m.getAnnotation(DamonRequirement.class);
					if (annotation != null) {
						Integer priority = new Integer(annotation.priority());
						requirements.add(new Object[] { m, priority });
					}
				}

				Collections.sort(requirements, new Comparator<Object[]>() {
					public int compare(Object[] pair1, Object[] pair2) {
						int p1 = ((Integer) pair1[1]).intValue();
						int p2 = ((Integer) pair2[1]).intValue();
						return p2 - p1;
					}
				});

				for (Object[] pair : requirements) {
					Method m = (Method) pair[0];
					Boolean b = (Boolean) m.invoke(aspect, new Object[] {});
					res &= b.booleanValue();
				}
				
			} catch (Exception ex) {
				// if some problem, it won't check any requirement
			}

		}

		else {

		}

		return res;
	}

	private boolean checkConditions(String url, String id, Object[] args) {

		boolean stayHere = true;
		
		Collection<String> aspectNames = thisEndPoint.getAspectNames(url);
		if (aspectNames!=null) {
		  for (String aspectName : aspectNames) {
		    Set<Method> conditions = thisEndPoint.getConditions(aspectName, id);
		    
			Vector<AspectRemoting> aspects = thisEndPoint.getInstances(aspectName);

			for(AspectRemoting aspect : aspects) {
							
		      for (Method condition : conditions) {
			    //System.out.println("CHECKING : "+condition);
			    try {
				  Class aspectClass = thisEndPoint.getClass(aspectName);
				  //Object aspect = aspectClass.newInstance();
				  //System.out.println("checkConditions : "+aspectName+" + "+id+" --> "+condition.getName()+"("+args.length+")");
				  stayHere &= (Boolean) condition.invoke(aspect, args);
		   	    } catch (Exception ex) {
				  ex.printStackTrace();
			    }
		      }
			}
		  }
		}  
		else return false;
		
		return stayHere;
	}

	public void passivate(NodeHandle nh, AspectActivation activation) {
		cast.sendDirect(nh, activation);
	}

	public void passivate(Abstractions abstraction, AspectActivation activation)
			throws RegistryException, DHTException {
		send(abstraction, activation);
	}

	public void passivateLocally(String url, String aspectName) {

		if (aspectName != null) {
			deployer.undeploy(aspectName);
			aspectIds.remove(aspectName);
			thisEndPoint.disableAspectRemoting(aspectName);
			thisEndPoint.unregisterAspect(url, aspectName);
		}
		// else error

	}

	public void activationArrive(AspectActivation activation) {
		if (activation.isActivate()) {
			try {
				activateLocally(activation.getName(), activation.isXML(),
						activation.getMetaData());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { //isPassivate
			passivateLocally(activation.getSubject(), activation.getName());
		}
	}
	
	/*
	 * ASPECT INVOKER
	 */

	public Object invoke(NodeHandle nh, AspectInvokation ai) throws RegistryException, DHTException {
		return invoke(Abstractions.DIRECT,nh,ai); 		
	}
	
	public Object invoke(Abstractions abstraction, AspectInvokation ai) throws RegistryException, DHTException {
		return invoke(abstraction,null,ai);
	}
	
	public Object invoke(Abstractions abstraction, NodeHandle nh, AspectInvokation ai) throws RegistryException, DHTException {
 		
		//System.out.println("AspectContainer : invoke -> "+ai+"");
		logger.debug("invoking : "+ai);
		
		//local & synchronous invocation?
		if (abstraction.equals(Abstractions.LOCAL)) {
			invokationArrive(ai);
			return null;
		}		
		
		if (ai.isSynchronous()) {
			//System.out.println("AspectContainer : Synchronous Invoke : "+ai);
			
			Object value = null;
			if (ai.isLazy()) {
			  
			  //waiting begin
			  synchronized (this) {
				if (!mutex.isWaiting(ai.getCode())) {					
					value = synchroInvokCache.getValue(ai.getCode());
					//no value? --> first time
					if (value==null)	{
						send(abstraction, ai);
						value = mutex.wait(ai.getCode(), Context.DELAY, Context.TIMEOUT);
						if (value==null) value = synchroInvokCache.getValue(ai.getCode());
						return value;
					}
				    else return value;							
				}
			  }
			  //waiting end
			  return synchroInvokCache.waitValue(ai.getCode(), Context.DELAY, Context.TIMEOUT);			  					
			}			
			
			//Greedy Invocation 
			else { 
		      if (nh!=null) cast.sendDirect(nh, ai);
		      else send(abstraction, ai);
		      
		      //System.out.println("AspectContainer : Waiting for "+ai);
		      value = mutex.wait(ai.getCode(), Context.DELAY, Context.TIMEOUT);
		      //System.out.println("AspectContainer : Value returned : "+value);
			}  
            return value;
		
		} 
		else {
		//System.out.println("AspectContainer : Asynchronous Invoke : "+ai);
		  if (ai.isLazy()) {			  
			  synchronized (this) {
				//System.out.println("AspectContainer : test : "+ai);
			    if (!asynchroInvokCache.hasValue(ai.getCode())) {			    	
			    	if (nh!=null) cast.sendDirect(nh, ai);
			        else {
			        	//System.out.println("AspectContainer : sending : "+ai);
			        	send(abstraction, ai);		
			        }
			    	asynchroInvokCache.putValue(ai.getCode(), true);			    	
			    }			    
			  }
		  }
		//Greedy Invocation 
		  else {
			//  System.out.println("AspectContainer : Sending Invoke : "+ai);
		    if (nh!=null) cast.sendDirect(nh, ai);
	        else send(abstraction, ai);		    
		  }  
		//  System.out.println("AspectContainer : Returning expected NULL");
		  return null;
		}  
		
	}

	// AOP Hook
	public void invokationArrive(AspectInvokation ai) {
		
		//System.out.println("invokationArrive ("+ai+"): "+ai.append);
        logger.debug("invokation arrive : "+ai);
		
		if (ai.isAck()) {
			mutex.notify(ai.getCode(), ai.getResult());
			synchroInvokCache.putValue(ai.getCode(), ai.getResult());
		}
		
		else if (publisher!=null) {
		  try {
			publisher.publish(ai);
		  } catch (IOException e) {			
			e.printStackTrace();
		  }
		}  
	}
	
	/******************************************************************************/
	
	public void allowPublisher(int port) throws UnknownHostException, IOException {
		publisher = new DamonPublisher(port);
		publisher.start();
	}
	
	public void allowProxy(String cl, String url, int port) throws UnknownHostException, IOException {
		//System.out.println("AspectContainer : allowProxy : "+cl);
		DamonProxy proxy = new DamonProxy(port);
		proxy.start();
		proxies.put(cl,new Object[]{url,proxy});
	}
	
	public void closeProxies(String cl) throws UnknownHostException, IOException {
		Set<Object[]> set = proxies.get(cl);
		for(Object[] pair : set) {
		  DamonProxy proxy = (DamonProxy) pair[1];
		  if (proxy!=null) proxy.close();
		}  
	}	
	
	public void useProxies(String cl, AspectRemoting ar) {
		Set<Object[]> set = proxies.get(cl);
		for(Object[] pair : set) {
		  String url = (String) pair[0];		
		  DamonProxy proxy = (DamonProxy) pair[1];		  
		  if (proxy!=null) proxy.add(url,ar);
		}  
	    		
	}
	
	/******************************************************************************/

	/*
	 * ASPECT REFLECTION
	 */

	public String getInstanceId(String aspectName) {
		return aspectIds.get(aspectName);
	}

	public Set<String> getAspects() {
		return aspectIds.keySet();
	}
	
	public String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName ();
	}

	public NodeHandle getNodeHandle() {
		return conn.getNode().getLocalNodeHandle();
	}
	
	public boolean isAlive(NodeHandle nh) {
		return conn.isAlive(nh);
	}
	
	public IdRange getRange(NodeHandle handle, int rank, Id lkey) {
		return cast.getRange(handle,rank,lkey);
	}

	public Collection<NodeHandle> getNeighbours(boolean ordered) {
		
		Collection<NodeHandle> nhs = cast.getNeighbours(numMaxNeightbours + 1, ordered);
		nhs.remove(conn.getNode().getLocalNodeHandle()); // remove local
		// nodehandle
		return nhs;
	}
	
	public Collection<NodeHandle> getReplicaSet(Id id, int num) {
		
		Collection<NodeHandle> nhs = cast.getReplicaSet(id, num);
		//nhs.remove(conn.getNode().getLocalNodeHandle()); //		
		return nhs;
	}

	public Collection<NodeHandle> getGroupMembers(String url)
			throws RegistryException, DHTException {
		return Registry.getWriters(url);
	}

	public boolean isGroupRoot(String url) {
		return cast.isRoot(url);
	}

	public NodeHandle getGroupParent(String url) {
		return cast.getParent(url);
	}

	public Collection<NodeHandle> getGroupChildren(String url) {		
		return cast.getChildren(url);
	}

	public long getAverageLatency(NodeHandle neighbour) {

		long acum = 0;
		int hits = 0;

		for (int i = 0; i < 5; i++) {

			long t1 = System.currentTimeMillis();
			boolean res = ((rice.pastry.NodeHandle) neighbour).ping();
			long t2 = System.currentTimeMillis();
			// System.out.println("Time "+i+" : "+(t2-t1)+" res : "+res);
			if (res && (t2 > t1)) {
				acum += (t2 - t1);
				hits++;
			}
		}

		if (hits > 0)
			return (acum / hits);
		else
			return 0;

	}

	public long getFreeMem() {

		// System.out.println("Runtime freeMem
		// :"+Runtime.getRuntime().freeMemory());

		long freeMem = mxbean.getFreePhysicalMemorySize()
				+ mxbean.getFreeSwapSpaceSize();
		return freeMem;

	}

	public long getTotalMem() {

		// System.out.println("Runtime totalMem
		// :"+Runtime.getRuntime().totalMemory());

		long totalMem = mxbean.getTotalPhysicalMemorySize()
				+ mxbean.getTotalSwapSpaceSize();
		return totalMem;
	}

	public long getFreeSpace() {
		// Java 6 methods
		File f = new File(".");
		return f.getFreeSpace();
	}

	public long getTotalSpace() {
		// Java 6 methods
		File f = new File(".");
		return f.getTotalSpace();
	}
	
	public double getSystemLoadAverage() {
		// Java 6 method
		return mxbean.getSystemLoadAverage();
	}

	public int getAvailableProcessors() {
		// Java 6 method
		return mxbean.getAvailableProcessors();
	}

	public long getProcessCpuTime() {
		// Java 6 method
		return mxbean.getProcessCpuTime();
	}

	public int getNumOfThreads() {
		// Java 6 method
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		//return threads.getPeakThreadCount();
		return threads.getThreadCount() - threads.getDaemonThreadCount();
		//return threads.getThreadCount();	
	}

	

}
