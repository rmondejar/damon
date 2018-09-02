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
package damon.test.g5k.multi;

import java.util.Collection;
import java.util.Hashtable;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import rice.p2p.commonapi.NodeHandle;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.reflection.ReflectionHandler;
import damon.reflection.thisEndPoint;

/**
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://test.urv.cat")
public class Sensor extends AspectRemoting {
		
	private Hashtable<String,NodeHandle> group = new Hashtable<String,NodeHandle>();
	private Hashtable<NodeHandle,Long> latencies = new Hashtable<NodeHandle,Long>();
	
	@RemoteAdvice(id = "information")
	public void storeTime(RemoteJoinPoint rjp, String name, NodeHandle nh, int[] data1, int[] data2) {
		if (name!=null && nh!=null) {
           MonitorHost.updateData(name, data1[0], data1[1], data1[2], data2[0], data2[1], data2[2]);
           group.put(name,nh);
           long lat = thisEndPoint.getReflectionHandler().getAverageLatency(nh);        
           latencies.put(nh,lat);
		}  
	}
	
	@RemotePointcut(id = "information", abstraction = Abstractions.MULTI)	
    @SourceHook(source = "damon.test.g5k.multi.SourceHooks", method = "getHostNameSensor")    
	public Object getHostName(JoinPoint joinPoint) throws Throwable {
    	
		String name = (String) joinPoint.proceed();
		NodeHandle nh = thisEndPoint.getLocalNodeHandle();
		int[] data1 = obtainHosts();
		int[] data2 = obtainLatencies();
    	invoke("getHostName", joinPoint, new Object[]{name,nh,data1,data2});    	
    	return name;
	}
	
	@RemotePointcut(id = "information", abstraction = Abstractions.MULTI)	
    @SourceHook(source = "damon.test.g5k.multi.SourceHooks", method = "getHostNameMonitor")    
	public Object getHostName2(JoinPoint joinPoint) throws Throwable {
    	
		String name = (String) joinPoint.proceed();
		NodeHandle nh = thisEndPoint.getLocalNodeHandle();
		int[] data1 = obtainHosts();
		int[] data2 = obtainLatencies();
    	invoke("getHostName2", joinPoint, new Object[]{name,nh,data1,data2});    	
    	return name;
	}
	
	
	private int[] obtainHosts() {
		
		int[] hosts = new int[3];		
		hosts[0] = group.size();
		ReflectionHandler rh = thisEndPoint.getReflectionHandler();
		hosts[1] = rh.getGroupChildren("p2p://test.urv.cat").size();
		if (rh.getGroupParent("p2p://test.urv.cat")!=null) hosts[1]++;
		hosts[2] = rh.getNeighbours(false).size();		
		return hosts;
	}
	
	private int[] obtainLatencies() {
		
		int[] latencies = new int[3];
		ReflectionHandler rh = thisEndPoint.getReflectionHandler();
		latencies[0] = getAverageLatency(group.values());
		latencies[1] = getAverageLatency(rh.getGroupChildren("p2p://test.urv.cat")); 
		latencies[2] = getAverageLatency(rh.getNeighbours(false));
		return latencies;
	}
    
	private int getAverageLatency(Collection<NodeHandle> hosts) {
		long avg = 0;
		int counter = 0;
		if (hosts!=null) {
		  for (NodeHandle nh : hosts) {
			if (!latencies.contains(nh)) {
			  long lat = thisEndPoint.getReflectionHandler().getAverageLatency(nh);
			  latencies.put(nh, lat);
			}
			avg+=latencies.get(nh);
			counter++;
		  }	
		}		
		int res = 0;
		if (counter>0) res = (int) (avg/counter);
		return res;
	}
	
	
}	