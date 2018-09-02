package damon.reflection;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import damon.annotation.Abstractions;
import damon.annotation.Type;
import damon.util.Context;

public class MetaData {
	
	private ClassLoader cl;
	
	private String name;
	private Abstractions abstraction;
	private String group;
	private int numNodes = 1;
	private boolean stateful = false;
	private boolean singleton = false;
	private boolean proxy = false;
	
	//RemotePointcutOrInvocation
	private Hashtable<String,Hashtable<String,Object>> rpcais = new Hashtable<String,Hashtable<String,Object>>();
	//RemoteAdviceOrMethod
	private Hashtable<String,Vector<String>> raams = new Hashtable<String,Vector<String>>();
	//DistributedMetaPointcut
	private Hashtable<String,Hashtable<String,Object>> rmpcs = new Hashtable<String,Hashtable<String,Object>>();
	//RemoteMetaAdvice
	private Hashtable<String,Hashtable<String,Object>> rmas = new Hashtable<String,Hashtable<String,Object>>();
	//RemoteCondition
	private Hashtable<String, Vector<String>> rcs = new Hashtable<String, Vector<String>>(); 
	//RemoteUpdates
	private Set<String> updates = new HashSet<String>();
	
	public String toString() {
		return "MetaData <"+name+"> activation : abstraction <"+abstraction+"> group <"+group+">"
		       +"\n offered : "+rpcais
		       +"\n conditions : "+rcs
		       +"\n required : "+raams
		       +"\n meta-pointcuts : "+rmpcs
		       +"\n meta-advices : "+rmas;
	}

	public void setScope(String scope) {
	  this.group = scope;	  
	  for (Hashtable<String,Object> rpcoi : rpcais.values()) {
		  rpcoi.put(Context.TARGET, scope);		  
	  }

	  for (Hashtable<String,Object> rma : rmas.values()) {
		  rma.put(Context.TARGET, scope);	  
	  }
	}
	
	public void addRemotePointcutOrInvocation(String methodName, String id, Abstractions abstraction, int numNodes, String target, boolean synchro, boolean lazy) {
				   
		  Hashtable<String,Object> rpcoi = new Hashtable<String,Object>();
		  rpcoi.put(Context.ID, id);
		  rpcoi.put(Context.ABSTRACTION, abstraction);
		  rpcoi.put(Context.TARGET, target);
		  rpcoi.put(Context.NUM_NODES, new Integer(numNodes)); //for MANY abstraction
		  rpcoi.put(Context.SYNCHRO, synchro);
		  rpcoi.put(Context.LAZY, lazy);
		  
		  rpcais.put(methodName, rpcoi);		  
	}
	
	public Hashtable<String,Object> getRemotePointcutOrInvocation(String methodName) {
		return rpcais.get(methodName);
	}
	
	public void addRemoteCondition(String methoName, String id) {
		  Vector<String> rc = null;			  
		  if (rcs.containsKey(methoName)) {
			  rc = rcs.get(methoName);		  
		  }
		  else {
			  rc = new Vector<String>();
		  }
		  rc.add(id);
		  rcs.put(methoName, rc);
		
	}
	
	public Vector<String> getRemoteConditions(String methodName) {
		
		Vector<String> res = rcs.get(methodName);
		if (res==null) res = new Vector<String>();
		return res;
		
	}
	
	public void addRemoteAdviceOrMethod(String methoName, String id) {
		  
		  Vector<String> raom = null;			  
		  if (raams.containsKey(methoName)) {
			  raom = raams.get(methoName);		  
		  }
		  else {
			  raom = new Vector<String>();
		  }
		  raom.add(id);
		  raams.put(methoName, raom);
	}
	
	public Vector<String> getRemoteAdviceOrMethod(String methodName) {
		Vector<String> res = raams.get(methodName);
		if (res==null) res = new Vector<String>();
		return res;
		
	}
	
	public void addRemoteMetaPointcut(String methodName, Type type, String id, String target, boolean ack) {
		   
		  Hashtable<String,Object> rmpc = new Hashtable<String,Object>();
		  rmpc.put(Context.ID, id);
		  rmpc.put(Context.TYPE, type);
		  rmpc.put(Context.TARGET, target);
		  rmpc.put(Context.ACK, ack);
		  
		  rmpcs.put(methodName, rmpc);	  
		  
	}
	
	public Hashtable<String,Object> getRemoteMetaPointcut(String methodName) {
		return rmpcs.get(methodName);
	}
	
	public void addRemoteMetaAdvice(String methodName, String id, Abstractions abstraction, int numNodes, String target, boolean synchro, boolean lazy) {
		   
		  Hashtable<String,Object> rma = new Hashtable<String,Object>();
		  rma.put(Context.ID, id);
		  rma.put(Context.ABSTRACTION, abstraction);
		  rma.put(Context.TARGET, target);
		  rma.put(Context.NUM_NODES, new Integer(numNodes)); //for MANY abstraction
		  rma.put(Context.SYNCHRO, synchro);
		  rma.put(Context.LAZY, lazy);
		  
		  rmas.put(methodName, rma);		  
	}
	
	public Hashtable<String,Object> getRemoteMetaAdvice(String methodName) {
		return rmas.get(methodName);
	}

	public void addRemoteUpdate(String methodName) {		  
		  updates.add(methodName);
	}
	
    public boolean isRemoteUpdate(String methodName) {
    	  return updates.contains(methodName);
    }
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Abstractions getAbstraction() {
		return abstraction;
	}

	public void setAbstraction(Abstractions abstraction) {
		this.abstraction = abstraction;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getNumNodes() {
		return numNodes;
	}

	public void setNumNodes(int numNodes) {
		this.numNodes = numNodes;
	}

	public boolean isStateful() {
		return stateful;
	}

	public void setStateful(boolean stateful) {
		this.stateful = stateful;
	}

	
	public boolean isSingleton() {
		return singleton;
	}
	
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	
	public boolean isProxy() {
		// TODO Auto-generated method stub
		return proxy;
	}

    public void setProxy(boolean proxy) {
    	this.proxy = proxy;
    }
    
	public ClassLoader getClassLoader() {		
		return cl;
	}
	
	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}

	
	


}
