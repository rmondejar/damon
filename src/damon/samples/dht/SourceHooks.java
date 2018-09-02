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
package damon.samples.dht;

import org.codehaus.aspectwerkz.annotation.*;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.reflection.thisEndPoint;


/**
 * Source Hook definition for SimpleHost
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class SourceHooks {
	   
	
	@Before("call(* java.util.Hashtable.put(..)) AND args(key,value) AND within(damon.samples.dht.*)")   
	public void put(JoinPoint joinPoint, Object key, Object value) throws Throwable {
		//System.out.println("SourceHook : put("+key+","+value+")-> "+joinPoint);		
		thisEndPoint.addParams(joinPoint, key, value);
	}
	
	@Before("call(* java.util.Hashtable.get(..)) AND args(key) AND within(damon.samples.dht.*)")   
	public void get(JoinPoint joinPoint, Object key) throws Throwable {
		//System.out.println("SourceHook : get");
		thisEndPoint.addParams(joinPoint, key);
	}
	
	@Before("call(* java.util.Hashtable.remove(..)) AND args(key) AND within(damon.samples.dht.*)")   
	public void remove(JoinPoint joinPoint, Object key) throws Throwable { 
		thisEndPoint.addParams(joinPoint, key);
	}

}	