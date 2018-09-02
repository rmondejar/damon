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
package damon.reflection;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import damon.annotation.Abstractions;
import damon.annotation.Type;
import damon.core.DamonCore;
import damon.invokation.AspectInvoker;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.util.AnnotationParser;
import damon.util.Context;
import damon.util.collections.QueueHashtable;
import damon.util.collections.SetHashtable;
import easypastry.dht.DHTException;
import easypastry.dht.DHTHandler;
import rice.p2p.commonapi.NodeHandle;

public class thisEndPoint {

	private static QueueHashtable<String, AspectRemoting> aspects = new QueueHashtable<String,AspectRemoting>();
	
	private static QueueHashtable<String, String> namesByURL = new QueueHashtable<String, String>();
	
	private static Hashtable<String, String> contexts = new Hashtable<String,String>();
	
	// aspect remoting
	private static Hashtable<String, Hashtable<String, Vector<Method>>> remoteAdvicesAndMethods = new Hashtable<String, Hashtable<String, Vector<Method>>>(); 	// aspectName,
																																								// rpcId,
																																								// methods
	private static Hashtable<String, Hashtable<String, Set<Method>>> remoteConditions = new Hashtable<String, Hashtable<String, Set<Method>>>(); 	// aspectName,
																																						// rpcId,
																																						// methods
	private static Hashtable<String, Hashtable<String, Hashtable<String, Object>>> remotePointcutsAndInvocations = new Hashtable<String, Hashtable<String, Hashtable<String, Object>>>(); // aspectName,
																																													// rpcId,
																																															// fields

	// distributed meta-aspect
	private static RemoteMetaPointcuts remoteMetaPointcuts = new RemoteMetaPointcuts();																																														private static Hashtable<String, Hashtable<String, Hashtable<String, Object>>> remoteMetaAdvices = new Hashtable<String, Hashtable<String, Hashtable<String, Object>>>();
	
	private static SetHashtable<String, Method> remoteUpdates = new SetHashtable<String, Method>(); 	// aspectName, // methods

	
	// to address the args problem...
	private static Hashtable<Object, Object[]> params = new Hashtable<Object, Object[]>();
	
	private static Hashtable<String, Class>  classes = new Hashtable<String, Class>();
	
	private static int activatedAspects;

	public static void addParams(Object key, Object ... args) {
		params.put(key, args);
	}
	
	public static Object[] getParams(Object key) {
		return params.get(key);
	}

	public static AspectInvoker getInvoker() {
		return DamonCore.getInvoker();
	}

	public static ReflectionHandler getReflectionHandler() {
		return DamonCore.getReflection();
	}

	public static DHTHandler getPersistenceHandler(String context)
			throws DHTException {
		return DamonCore.getDHTHandler(context);
	}
	
	public static void setInstance(String aspectName, AspectRemoting ar) {
		aspects.put(aspectName,ar);		
	}
	
	public static Collection<AspectRemoting> getAllInstances() {
		return aspects.getAll();
	}
	
	public static Vector<AspectRemoting> getInstances(String aspectName) {
		return aspects.get(aspectName);
	}	

	public static String getInstanceId(String aspectName) {
		return DamonCore.getContainer().getInstanceId(aspectName);
	}

	public static NodeHandle getLocalNodeHandle() {
		ReflectionHandler reflection = getReflectionHandler();
		return reflection.getNodeHandle();
	}

	public static Collection<NodeHandle> getClosestGroupMembers(String url) {

		ReflectionHandler reflection = getReflectionHandler();
		Collection<NodeHandle> c = new Vector<NodeHandle>();
		c.addAll(reflection.getGroupChildren(url));
		c.add(reflection.getGroupParent(url));
		c.remove(reflection.getNodeHandle());
		return c;
	}

	public static boolean isLocalHostInThisGroup(String url) {
		
		//System.out.println("isLocalHostInThisGroup "+url+" ? --> "+DamonCore.getContainer().isRegistered(url));

		return DamonCore.getContainer().isRegistered(url);
	}

	public static void registerSharedAspect(String aspectName, String url) {

		contexts.put(aspectName, url.substring(6));
		
		if (isLocalHostInThisGroup(url)) { // if is null, then the host is not registered
			namesByURL.put(url, aspectName);
		}		
		activatedAspects++;
	}
	
	public static String getContext(String aspectName) {		
		return contexts.get(aspectName);
	}

	public static Vector<String> getAspectNames(String url) {
		return namesByURL.get(url);
	}

	public static void unregisterAspect(String url, String aspectName) {		
		namesByURL.remove(url,aspectName);
		activatedAspects--;
	}

	public static void disableAspectRemoting(String aspectName) {
		remotePointcutsAndInvocations.remove(aspectName);
		remoteAdvicesAndMethods.remove(aspectName);
		remoteMetaPointcuts.remove(aspectName);
		remoteConditions.remove(aspectName);
		remoteUpdates.remove(aspectName);
	}

	/**
	 * Read annotations from an aspect to subscribe these remote advices/methods
	 * and/or pointcuts/invocations
	 * 
	 * @param aspectName
	 * @param aspectClass
	 * @param url
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void enableAspectRemoting(String aspectName,
			Class aspectClass) throws IOException, ClassNotFoundException {

		MetaData md = AnnotationParser.parse(aspectClass);		
		enableAspectRemoting(aspectName, aspectClass, md);
		
	}

	public static void enableAspectRemoting(String aspectName, Class aspectClass, MetaData md) {
				
		classes.put(aspectName, aspectClass);
				
		Method[] methods = aspectClass.getMethods();

		for (Method method : methods) {

			if (md.isRemoteUpdate(method.getName())) {			
				addRemoteUpdate(aspectName, method);
			}
			
			Vector<String> ids2 = md.getRemoteConditions(method.getName());
			for (String id : ids2) {
				addRemoteCondition(aspectName, id, method);
			}
			
			Vector<String> ids3 = md.getRemoteAdviceOrMethod(method.getName());
			for (String id : ids3) {
				addRemoteAdviceOrMethod(aspectName, id, method);
			}
		
			
			Hashtable<String, Object> rpcoi = md.getRemotePointcutOrInvocation(method.getName());
			if (rpcoi != null) {
				String id = (String) rpcoi.get(Context.ID);
				Abstractions abstraction = (Abstractions) rpcoi
						.get(Context.ABSTRACTION);
				int numNodes = (Integer) rpcoi.get(Context.NUM_NODES); // MANY
																		// abstraction
				String target = (String) rpcoi.get(Context.TARGET);
				if (target.length() <= 0)
					target = md.getGroup(); // this group
				boolean synchro = false;
				if (rpcoi.containsKey(Context.SYNCHRO)) synchro = (Boolean) rpcoi.get(Context.SYNCHRO);
				boolean lazy = false;
				if (rpcoi.containsKey(Context.LAZY)) lazy = (Boolean) rpcoi.get(Context.LAZY);
				
				addRemotePointcutOrInvocation(aspectName, method.getName(), id,
						abstraction, numNodes, target,synchro, lazy);
			}
			
			Hashtable<String, Object> rmpc = md.getRemoteMetaPointcut(method.getName());
			if (rmpc != null) {
				String id = (String) rmpc.get(Context.ID);
				String target = (String) rmpc.get(Context.TARGET);
				Type type = (Type) rmpc.get(Context.TYPE);
				boolean ack = false;
				if (rmpc.containsKey(Context.ACK)) ack = (Boolean) rmpc.get(Context.ACK);
				addRemoteMetaPointcut(aspectName, method, id, target, type,ack);
			
			}
			
			Hashtable<String, Object> rma = md.getRemoteMetaAdvice(method.getName());
			if (rma != null) {
				String id = (String) rma.get(Context.ID);
				Abstractions abstraction = (Abstractions) rma.get(Context.ABSTRACTION);
				int numNodes = (Integer) rma.get(Context.NUM_NODES); // MANY
																		// abstraction
				String target = (String) rma.get(Context.TARGET);
				if (target.length() <= 0)
					target = md.getGroup(); // this group
				boolean synchro = false;
				if (rma.containsKey(Context.SYNCHRO)) synchro = (Boolean) rma.get(Context.SYNCHRO);
				boolean lazy = false;
				if (rma.containsKey(Context.LAZY)) lazy = (Boolean) rma.get(Context.LAZY);
				
				addRemoteMetaAdvice(aspectName, method.getName(), id,
						abstraction, numNodes, target,synchro, lazy);
			}
		}
	}

	private static void addRemotePointcutOrInvocation(String aspectName,
			String methodName, String id, Abstractions abstraction,
			int numNodes, String target, boolean synchro, boolean lazy) {

		Hashtable<String, Hashtable<String, Object>> rpcais = null;
		if (remotePointcutsAndInvocations.containsKey(aspectName)) {
			rpcais = remotePointcutsAndInvocations.get(aspectName);
		} else {
			rpcais = new Hashtable<String, Hashtable<String, Object>>();
		}

		Hashtable<String, Object> rpcoi = new Hashtable<String, Object>();
		rpcoi.put(Context.ID, id);
		rpcoi.put(Context.ABSTRACTION, abstraction);
		rpcoi.put(Context.TARGET, target);
		rpcoi.put(Context.NUM_NODES, new Integer(numNodes)); // for MANY abstraction
		rpcoi.put(Context.SYNCHRO, synchro);
		rpcoi.put(Context.LAZY, lazy);

		rpcais.put(methodName, rpcoi);
		remotePointcutsAndInvocations.put(aspectName, rpcais);
	}

	public static Hashtable<String, Object> getRemotePointcutOrInvocation(String aspectName, String methodName) {

		Hashtable<String, Object> rpoi = null;

		if (remotePointcutsAndInvocations.containsKey(aspectName)) {
			Hashtable<String, Hashtable<String, Object>> rpcais = remotePointcutsAndInvocations
					.get(aspectName);
			if (rpcais.containsKey(methodName)) {
				rpoi = rpcais.get(methodName);
			} else {
				rpoi = new Hashtable<String, Object>();
			}
		} else {
			rpoi = new Hashtable<String, Object>();
		}

		return rpoi;
	}

	private static void addRemoteAdviceOrMethod(String aspectName,
			String rpcID, Method advice) {

		Hashtable<String, Vector<Method>> raams = null;
		if (remoteAdvicesAndMethods.containsKey(aspectName)) {
			raams = remoteAdvicesAndMethods.get(aspectName);
		} else {
			raams = new Hashtable<String, Vector<Method>>();
		}

		Vector<Method> raom = null;
		if (raams.containsKey(rpcID)) {
			raom = raams.get(rpcID);
		} else {
			raom = new Vector<Method>();
		}
		raom.add(advice);
		raams.put(rpcID, raom);
		remoteAdvicesAndMethods.put(aspectName, raams);

	}

	public static Vector<Method> getRemoteAdvicesAndMethods(String aspectName,
			String pc) {
		
		Vector<Method> raom = null;
		//System.out.println("get "+remoteAdvicesAndMethods.get(aspectName));
		if (remoteAdvicesAndMethods.containsKey(aspectName)) {
			Hashtable<String, Vector<Method>> raams = remoteAdvicesAndMethods
					.get(aspectName);
			if (raams.containsKey(pc)) {
				raom = raams.get(pc);
			} else {
				raom = new Vector<Method>();
			}
		} else {
			raom = new Vector<Method>();
		}

		return raom;
	}
	
	private static void addRemoteCondition(String aspectName, String id, Method condition) {

		Hashtable<String, Set<Method>> rcs = null;
		if (remoteConditions.containsKey(aspectName)) {
			rcs = remoteConditions.get(aspectName);
		} else {
			rcs = new Hashtable<String, Set<Method>>();
		}

		Set<Method> rc = null;
		if (rcs.containsKey(id)) {
			rc = rcs.get(id);
		} else {
			rc = new HashSet<Method>();
		}
		rc.add(condition);
		rcs.put(id, rc);
		remoteConditions.put(aspectName, rcs);		

	}

	public static Set<Method> getConditions(String aspectName, String id) {

		//System.out.println("getConditions : "+aspectName+" + "+pc);
		//System.out.println("remoteConditions : "+remoteConditions);
		Set<Method> rc = null;		
		if (remoteConditions.containsKey(aspectName)) {
			Hashtable<String, Set<Method>> rcs = remoteConditions.get(aspectName);
			if (rcs.containsKey(id)) {
				rc = rcs.get(id);
			} else {
				rc = new HashSet<Method>();
			}
		} else {
			rc = new HashSet<Method>();
		}

		return rc;
	}

	public static void addRemoteMetaPointcut(String aspectName,	Method method, String id, String target, Type type, boolean ack) {
		remoteMetaPointcuts.add(aspectName,method,id,target,type,ack);
	}

	public static Collection<Object[]> getRemoteMetaPointcuts(String aspectName, String id, Type type) {
		return remoteMetaPointcuts.get(aspectName,id,type);	
	}
	
	private static void addRemoteMetaAdvice(String aspectName,
			String methodName, String id, Abstractions abstraction,
			int numNodes, String target, boolean synchro, boolean lazy) {

		Hashtable<String, Hashtable<String, Object>> rmas = null;
		if (remoteMetaAdvices.containsKey(aspectName)) {
			rmas = remoteMetaAdvices.get(aspectName);
		} else {
			rmas = new Hashtable<String, Hashtable<String, Object>>();
		}

		Hashtable<String, Object> rma = new Hashtable<String, Object>();
		rma.put(Context.ID, id);
		rma.put(Context.ABSTRACTION, abstraction);
		rma.put(Context.TARGET, target);
		rma.put(Context.NUM_NODES, new Integer(numNodes)); // for MANY abstraction
		rma.put(Context.SYNCHRO, synchro);
		rma.put(Context.LAZY, lazy);

		rmas.put(methodName, rma);
		remoteMetaAdvices.put(aspectName, rmas);
	}

	
	public static Hashtable<String, Object> getRemoteMetaAdvice(String aspectName, String methodName) {
		
		Hashtable<String, Object> rma = null;

		if (remoteMetaAdvices.containsKey(aspectName)) {
			
			Hashtable<String, Hashtable<String, Object>> rmas = 
				remoteMetaAdvices.get(aspectName);
			if (rmas.containsKey(methodName)) {
				rma = rmas.get(methodName);
			} else {
				rma = new Hashtable<String, Object>();
			}
			
		} else {
			rma = new Hashtable<String, Object>();
		}

		return rma;
	}
	
	public static void addRemoteUpdate(String aspectName,	Method method) {
		remoteUpdates.put(aspectName,method);
	}

	public static Collection<Method> getRemoteUpdates(String aspectName) {
		return remoteUpdates.get(aspectName);	
	}
	

	public static Class getClass(String aspectName) {		
		return classes.get(aspectName);
	}

	public static Hashtable<String, Integer> getPersistenceState() {
		return DamonCore.getStorage().getDHTHandler().getState();
	}
	
	public static int getActivatedAspects() {
		return activatedAspects;
	}

	public static boolean checkSingleton(String url, String aspectName, boolean singleton) {
				
		if (singleton) {
		  Vector<String> names = getAspectNames(url);  
		  return names.contains(aspectName);		  
		}  
		return false;
	}

	



}
