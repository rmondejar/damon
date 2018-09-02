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

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import rice.p2p.commonapi.Id;

import damon.annotation.*;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.reflection.thisEndPoint;
import damon.util.Utilities;

/**
 * Distributed Aspect Locator
 * @author Ruben Mondejar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://dht.urv.cat")
public class Locator extends AspectRemoting {
			
	@RemotePointcut(id = "put", abstraction = Abstractions.HOPPED, synchro = true)		
	@SourceHook(source = "damon.samples.dht.SourceHooks", method = "put", type = Type.AROUND)	
	public Object putMethod(JoinPoint joinPoint) throws Throwable {	
		System.out.println("Locator [put]");		
		Object[] params = thisEndPoint.getParams(joinPoint);
		Object key = params[0];		 
		Object obj = params[1];
		if (key!=null && obj!=null) {
		  Id id =  Utilities.generateHash(key);
    	  Object res = null;
		  while (res==null) {
    	    res = super.invoke("putMethod", joinPoint, id, new Object[]{key,obj});
		  }		  
		}  
    	return joinPoint.proceed();
    	  
    }
	
	@RemotePointcut(id = "get", abstraction = Abstractions.HOPPED, synchro = true)
	@SourceHook(source = "damon.samples.dht.SourceHooks", method = "get", type = Type.AROUND)
	public Object getMethod(JoinPoint joinPoint) throws Throwable {
		System.out.println("Locator [get]");
		Object obj = joinPoint.proceed();
		Object key = thisEndPoint.getParams(joinPoint)[0];
		if (key!=null) {		
		  Id id =  Utilities.generateHash(key);
		  obj = super.invoke("getMethod", joinPoint, id, new Object[]{key});
		}  
    	return obj;
    }
	
	@RemotePointcut(id = "remove", abstraction = Abstractions.HOPPED, synchro = true)
	@SourceHook(source = "damon.samples.dht.SourceHooks", method = "remove", type = Type.AROUND)
	public Object removeMethod(JoinPoint joinPoint) throws Throwable {
		System.out.println("Locator [remove]");
		Object obj = joinPoint.proceed();
		Object key = thisEndPoint.getParams(joinPoint)[0];
		if (key!=null) {		
		  Id id =  Utilities.generateHash(key);
		  obj = super.invoke("removeMethod", joinPoint, id, new Object[]{key});
		}  
		return obj;
	}	
    	
	
	
}	