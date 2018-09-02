/*******************************************************************************
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
 ******************************************************************************/
package damon.samples.dht.replication.nbs;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.metalevel.aspectwerkz.DistributedMetaAspect;
import damon.reflection.ReflectionHandler;
import damon.reflection.thisEndPoint;
import damon.samples.dht.distribution.Storage;
import damon.util.Utilities;
import damon.util.collections.SetHashtable;

/**
 * Distributed Meta-Aspect Replicator
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://dht.urv.cat")
public class Replicator extends DistributedMetaAspect {
	
	private final int R_FACTOR = 4;
	private ReflectionHandler rh = thisEndPoint.getReflectionHandler();
	
	private static SetHashtable<NodeHandle,Id> replicators = new SetHashtable<NodeHandle,Id>();
	private static SetHashtable<Id,NodeHandle> replicas = new SetHashtable<Id,NodeHandle>();
	
	@RemoteMetaPointcut(id = "put", type = Type.AFTER)    
	public void replicate(RemoteJoinPoint rjp, String key, Object value) {
		Id id =  Utilities.generateHash(key);		
		System.out.println("Replicator [replicate] : "+id);		
		if (Storage.containsKey(id)) redoReplicas(id,value);
		checkNumReplicas(id,value);
	}
	
	@RemotePointcut(id = "replicate", abstraction = Abstractions.DIRECT, synchro = true)
	public void redoReplicas(Id id, Object value) {			
		    
		    Object oldValue = Storage.getValue(id);
		    
		    if (!oldValue.equals(value)) {
		    	
			  Set<NodeHandle> replicatorsById = replicas.get(id);
			  Set<NodeHandle> blacklist = new HashSet<NodeHandle>();
		      for(NodeHandle nh : replicatorsById) {				  
				  Object res = super.invoke("redoReplicas",null,nh, new Object[]{id,value});
				  if (res==null) {					  
					  blacklist.add(nh); //cancel replicator (indirect)
				  }
			  }		
		    
		      for (NodeHandle nh : blacklist) {
		        replicators.remove(nh,id);		
			    replicas.remove(id, nh);
		      }
		    }  
	}
	
	@RemotePointcut(id = "replicate", abstraction = Abstractions.DIRECT, synchro = true)
	public void checkNumReplicas(Id id, Object value) {
					    
		    ReflectionHandler reflection = thisEndPoint.getReflectionHandler();
		
			Set<NodeHandle> replicatorsById = replicas.get(id);
			int num = replicatorsById.size();
		    		    
			
			if (num < R_FACTOR) {
				
		      Set<NodeHandle> newers = new HashSet<NodeHandle>(rh.getNeighbours(false));
		      newers.removeAll(replicatorsById);
		    
			  Iterator<NodeHandle> it = newers.iterator();				
			  while(num < R_FACTOR && it.hasNext()) {
			    NodeHandle nh = it.next();
			    //System.out.println("Replica to : "+nh);			    
			    Object res = null;
			    while(res==null && reflection.isAlive(nh)) {			    	
			    	System.out.println("New replica : "+nh);
			    	res = super.invoke("checkNumReplicas",null,nh, new Object[]{id,value});
			    	if (res==null) try {Thread.sleep(2000);} catch(Exception e) {}
			    }
			    if (res!=null) {
			      System.out.println("Replica stored in : "+nh);
			      replicators.put(nh,id);		
				  replicas.put(id, nh);
			      num++;
			    }  
			  }			    
			}	
	}
    
	/*********************************************************************/

	@RemoteUpdate
	public void replicaCheck(NodeHandle handle, boolean joined) {
		
		ReflectionHandler reflection = thisEndPoint.getReflectionHandler();
		System.out.println("Replicator [replicaCheck]");
		
		if (joined) {	
			try {Thread.sleep(15000);} catch(Exception e) {}
			//check that replicators still are neigbours			
            for(NodeHandle nh : replicators.keySet()) {
				
				if (!reflection.isAlive(nh)) {
				  Set<Id> ids = replicators.remove(nh);
				  if (ids!=null) {
				  for(Id id : ids) {
					  replicas.remove(id,nh);
					  
				  }
				  }
				}  
			}					
		}
		else {
			 //delete dead replicator			
		     Set<Id> ids = replicators.remove(handle);
		     if (ids!=null) {
		       for(Id id : ids) replicas.remove(id,handle);
		     }  
        }
	    
		for(Id id : replicas.keySet()) {
          checkNumReplicas(id,null);
		}
	}
	
	/**
	 * Forces Update (Crash Case)
	 */
	@RemotePointcut(id = "replicaPing", abstraction = Abstractions.DIRECT, synchro = true)
	@DamonPulse(seconds=11)	
	public void ping() {
		for (NodeHandle nh : replicators.keySet()) {
			super.invoke("ping",null,nh,new Object[]{});
		}
	}
	
	@RemoteAdvice(id = "replicaPing") 
	public void pong(RemoteJoinPoint rjp) {		
	  rjp.proceed(true);
	}
	
	
}	