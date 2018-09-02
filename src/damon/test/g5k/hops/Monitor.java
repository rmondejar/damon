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
package damon.test.g5k.hops;


import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.annotation.*;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.util.UID;

/**
 * Distributed Aspect using multi abstraction and remote services
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://test.urv.cat")
public class Monitor extends AspectRemoting {
	
	@RemotePointcut(id = "hopped", abstraction = Abstractions.HOPPED, synchro=true)
	@SourceHook(source = "damon.test.g5k.hops.SourceHooks", method = "getHostNameMonitor")        
	public void hopped(JoinPoint joinPoint) throws Throwable {
    	
		String key =  UID.getUID();				
		long t1 = System.currentTimeMillis();		
		Object res = invoke("hopped", joinPoint, key, new Object[]{});
		if (res!=null) {
		  int hops = (Integer) res;
		  long t2 = System.currentTimeMillis();		
		  MonitorHost.updateData("hopped", t2-t1, hops);
		}  
    	
	}
	
		
	@RemotePointcut(id = "any", abstraction = Abstractions.ANY, synchro=true)	
    @SourceHook(source = "damon.test.g5k.hops.SourceHooks", method = "getHostNameMonitor")    
	public void any(JoinPoint joinPoint) throws Throwable {
    					
		long t1 = System.currentTimeMillis();
		Object res = (Integer) invoke("any", joinPoint, new Object[]{});
		if (res!=null) {
		  int hops = (Integer) res;
		  long t2 = System.currentTimeMillis();
		  MonitorHost.updateData("any", t2-t1, hops);
		}  
	}
	
	
	
	@RemotePointcut(id = "many", abstraction = Abstractions.MANY, num=2, synchro=true)	
    @SourceHook(source = "damon.test.g5k.hops.SourceHooks", method = "getHostNameMonitor")
	public void many(JoinPoint joinPoint) throws Throwable {
    
		long t1 = System.currentTimeMillis();
		Object res = (Integer) invoke("many", joinPoint, new Object[]{});
		if (res!=null) {
		  int hops = (Integer) res;
		  long t2 = System.currentTimeMillis();
		  MonitorHost.updateData("many", t2-t1, hops);
		}  
	}
	
}	