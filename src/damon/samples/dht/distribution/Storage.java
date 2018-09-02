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
package damon.samples.dht.distribution;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.reflection.ReflectionHandler;
import damon.reflection.thisEndPoint;
import damon.util.Utilities;

/**
 * Distributed Aspect Storage
 * @author Ruben Mondejar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://dht.urv.cat")
public class Storage extends AspectRemoting {
			
	private ReflectionHandler rh = thisEndPoint.getReflectionHandler();
	
	private static SortedMap<Id, Object> data = new TreeMap<Id, Object>();
	
	public static synchronized void putValue(Id id, Object value) {
		data.put(id, value);
	}

	public static synchronized Object getValue(Id id) {
		return data.get(id);
	}
	
	public static synchronized boolean containsKey(Id id) {
		return data.containsKey(id);
	}
	
	public static synchronized Object removeValue(Id id) {
		return data.remove(id);
	}
	
	private Vector<Id> getValuesView() {
		Vector<Id> v = new Vector<Id>();
		v.addAll(data.keySet());
		return v;
	}
	
	@RemoteAdvice(id = "put")
	public void putArrive(RemoteJoinPoint rjp, String key, Object obj) {	
		Id id =  Utilities.generateHash(key);
		System.out.println("Storage [put] : "+id);
		putValue(id,obj);
		System.out.println("Pair ("+id+","+obj+") Stored ");					
		rjp.proceed(true);
	}
	
	@RemoteAdvice(id = "get")
	public void getArrive(RemoteJoinPoint rjp, String key) {
		Id id =  Utilities.generateHash(key);
		System.out.println("Storage [get] : "+id);		
		Object obj = getValue(id);		
		rjp.proceed(obj);
	}
	
	@RemoteAdvice(id = "remove")
	public void removeArrive(RemoteJoinPoint rjp, String key) {
		Id id =  Utilities.generateHash(key);
		System.out.println("Storage [remove] : "+id);		
		Object obj = removeValue(id);		
		rjp.proceed(obj);
	}
    
	/*********************************************************************/
	  	 
	@RemotePointcut(id = "put", abstraction = Abstractions.HOPPED, synchro = true)
	//@DamonPulse(seconds=11)
	//public void keyCheck() {
	@RemoteUpdate
	public void keyCheck(NodeHandle handle, boolean joined) {
		
		System.out.println("Storage [keyCheck]");
		
		if (joined) {
		  try {Thread.sleep(15000);} catch(Exception e) {}
		  
		  for(Id id : getValuesView()) {
			
			Collection<NodeHandle> c = rh.getReplicaSet(id,1);			
			if (c!=null && !c.isEmpty()) {
				NodeHandle nh = c.iterator().next(); 
				// me == first replica ???
				if (!nh.equals(rh.getNodeHandle())) {
				  Object value = getValue(id);
				  if (value!=null) {
				    Object res = null;
				    while (res==null) {					  
					  System.out.println("Moving :"+id);
					  res = super.invoke("keyCheck", null, id, new Object[]{id,value});
					  if (res==null) try {Thread.sleep(2000);} catch(Exception e) {}
				    }  
				    System.out.println("Moved :"+id);
				    removeValue(id);
				  }  
				}  
			}
		  }		
		}	
    }


}	