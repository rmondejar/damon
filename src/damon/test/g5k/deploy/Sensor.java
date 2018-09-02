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
package damon.test.g5k.deploy;


import java.util.Hashtable;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.annotation.*;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.reflection.ReflectionHandler;
import damon.reflection.thisEndPoint;
import damon.util.Context;

/**
 * Distributed Aspect using multi abstraction and remote services
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://test.urv.cat")
public class Sensor extends AspectRemoting {
	
	private float[] ini = new float[3]; 
		
	@SourceHook(source = "damon.test.g5k.deploy.SourceHooks", method = "init")
	public void init(JoinPoint joinPoint) throws Throwable {	    						
			ini = obtainResources();	    	
	}
		
	@RemotePointcut(id = "information", abstraction = Abstractions.MULTI)
	@SourceHook(source = "damon.test.g5k.deploy.SourceHooks", method = "getHostNameSensor")      
	public Object getHostName(JoinPoint joinPoint) throws Throwable {
    	
		String name = (String) joinPoint.proceed();
		int[] data1 = obtainElements();
		float[] data2 = obtainResources();
		//float[] data2 = calculateResources(ini,current);
    	invoke("getHostName", joinPoint, new Object[]{name,data1,data2});    	
    	return name;
	}

	@RemotePointcut(id = "information", abstraction = Abstractions.MULTI)
    @SourceHook(source = "damon.test.g5k.deploy.SourceHooks", method = "getHostNameMonitor")
	public Object getHostName2(JoinPoint joinPoint) throws Throwable {
    	
		String name = (String) joinPoint.proceed();
		int[] data1 = obtainElements();
		float[] data2 = obtainResources();
		//float[] data2 = calculateResources(ini,current);
    	invoke("getHostName2", joinPoint, new Object[]{name,data1,data2});    	
    	return name;
	}
	
	private int[] obtainElements() {
		
		int[] elems = new int[3];
		Hashtable<String,Integer> state = thisEndPoint.getPersistenceState();
		if (state.containsKey(Context.STORAGE)) elems[0] = state.get(Context.STORAGE);
		if (state.containsKey(Context.REGISTRY))elems[1] = state.get(Context.REGISTRY);
		//System.out.println("State :"+state);
		elems[2] = thisEndPoint.getActivatedAspects()-1;
		return elems;
	}
	
	private float[] obtainResources() {
		
		float[] resources = new float[3];
		ReflectionHandler rh = thisEndPoint.getReflectionHandler();
		resources[0] = (float) rh.getSystemLoadAverage();
		resources[1] = (float) (rh.getTotalMem() - rh.getFreeMem()) / rh.getTotalMem() ; 
		resources[2] = (float) (rh.getTotalSpace() - rh.getFreeSpace()) / rh.getTotalSpace();
		return resources;
	}
	

	private float[] calculateResources(float[] res1, float[] res2) {
	  return new float[]{Math.abs(res1[0]-res2[0]),Math.abs(res1[1]-res2[1]),Math.abs(res1[2]-res2[2])};	
	}
    
}	