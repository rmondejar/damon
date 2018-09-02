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
package damon.invokation.aspectwerkz;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.codehaus.aspectwerkz.annotation.After;
import org.codehaus.aspectwerkz.annotation.Before;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

import damon.annotation.Abstractions;
import damon.invokation.AspectInvokation;
import damon.invokation.AspectInvoker;
import damon.invokation.InvokationException;
import damon.invokation.RemoteJoinPoint;
import damon.reflection.thisEndPoint;
import damon.util.Context;
import damon.util.Utilities;

/*
 * Aspect Remoting allows invocations between distributed aspects
 */
public abstract class AspectRemoting {
	
	protected String thisAspectName = this.getClass().getName();
	protected String context = thisEndPoint.getContext(thisAspectName);	
		
	public AspectRemoting() {
		//System.out.println("AspectRemoting Created: "+this);		 
	}

	//force creation
	//@After ("execution(* damon.timer.DamonTimer.seconds1(..))")      
	//public void init() {}
	
	public String getName() {
		return thisAspectName;		
	}
	
	public String toString() {
		return "AspectRemoting["+thisAspectName+"]";
	}
	
	/*
	 * Normal invoke with null joinpoint
	 */
	public void invoke(String method, Object[] args) {
		invoke(method, null, args);
	}

	/*
	 * Normal invoke 
	 */
	public Object invoke(String methodName, JoinPoint joinpoint, Object[] args) {
		return invoke(methodName, joinpoint, null, null, 0, args);
	}

	/*
	 * Direct invoke with NodeHandle 
	 */
	public Object invoke(String methodName, JoinPoint joinpoint, NodeHandle nh,
			Object[] args) {
		return invoke(methodName, joinpoint, nh, null, 0, args);
	}
	
	/*
	 * Hopped invoke with String key
	 */
	public Object invoke(String methodName, JoinPoint joinpoint, Object key,
			Object[] args) {
		Id id = null;
		if (key!=null) id = Utilities.generateHash(key); 
		return invoke(methodName, joinpoint, null, id, 0, args);
	}
	
	/*
	 * Hopped invoke with Id key
	 */	
	public Object invoke(String methodName, JoinPoint joinpoint, Id key,
			Object[] args) {
		return invoke(methodName, joinpoint, null, key, 0, args);
	}
	
	/*
	 * Many invoke with Id key
	 */	
	public Object invoke(String methodName, JoinPoint joinpoint, int num,
			Object[] args) {
		return invoke(methodName, joinpoint, null, null, num, args);
	}
	
	
	private Object invoke(String methodName, JoinPoint joinpoint, NodeHandle nh, Id key,
			int num, Object[] args) {	
		
		Object result = null;

		Hashtable<String, Object> invocation = thisEndPoint
				.getRemotePointcutOrInvocation(thisAspectName, methodName);

		if (invocation.isEmpty()
				|| !invocation.containsKey(Context.TARGET)
				|| !invocation.containsKey(Context.ABSTRACTION)) {

			return result;
		}

		String url = (String) invocation.get(Context.TARGET);
		Abstractions abstraction = (Abstractions) invocation
				.get(Context.ABSTRACTION);
		String id = (String) invocation.get(Context.ID);
		boolean synchro = (Boolean) invocation.get(Context.SYNCHRO);
		boolean lazy = (Boolean) invocation.get(Context.LAZY);
		AspectInvoker invoker = thisEndPoint.getInvoker();
		AspectInvokation ai = null;

		switch (abstraction) {

		case HOPPED:
			ai = new AspectInvokation(thisAspectName, url, id, args);
			ai.setKey(key);
			break;
		case LOCAL :
		case DIRECT:
		case MULTI:		
		case ANY:			
			ai = new AspectInvokation(thisAspectName, url, id, args);
			break;
		case MANY:
			int numNodes = 0;
			if (num>0) numNodes = num;
			else num = ((Integer) invocation.get(Context.NUM_NODES)).intValue();
			ai = new AspectInvokation(thisAspectName, url, id, args, numNodes);
			break;
		}

		ai.setSynchronism(synchro);
		ai.setLazy(lazy);	
		
		try {
			
			if (nh!=null) {
				result = invoker.invoke(nh, ai);
			}
			else {		
				result = invoker.invoke(abstraction, ai);				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	}
	
	//@Before("execution(* damon.core.AspectContainer.invokationArrive(..))")
	@Before ("execution(* damon.timer.DamonTimer.seconds1(..))")
	public void pulse(JoinPoint joinPoint) throws Throwable {}

	@After("execution(* damon.core.AspectContainer.invokationArrive(..))")
	public void invokeAdvice(JoinPoint joinPoint) throws Throwable {
		//System.out.println("^^^ RemoteAspect invoke : "+thisAspectName);
		AspectInvokation ai = AspectInvokeObserver.nextInvokation(thisAspectName);
		if (ai!=null && !ai.isAck()) {
		  invokeLocally(ai);		
		}
	}

	@After("execution(* damon.core.DamonProxy.invokationArrive(..))")	
	public void invokeAdvice2(JoinPoint joinPoint) throws Throwable {
		//System.out.println("^^^ RemoteAspect PROXY : "+thisAspectName);
		AspectInvokation ai = AspectInvokeObserver.nextInvokation(thisAspectName);
		if (ai!=null && !ai.isAck()) {
		  invokeLocally(ai);		
		}
	}

	/**
	 * This method invokes the remote advices using Damon reflection to know the
	 * list of subscribed advices and Java reflection to invoke them
	 * @param ai 
	 * 
	 * @throws InvokationException
	 */
	public void invokeLocally(AspectInvokation ai) throws InvokationException {

			Vector<Method> methods = thisEndPoint.getRemoteAdvicesAndMethods(thisAspectName, ai.getId());

			RemoteJoinPoint rjp = new RemoteJoinPoint(ai.getSource());
			rjp.setNum(ai.getNum());
			rjp.setId(ai.getId());
			rjp.setPath(ai.getPath());
			rjp.setSubject(ai.getSubject());
			rjp.setName(ai.getName());
			rjp.setCode(ai.getCode());
			rjp.setArgs(ai.getArgs());

			Object[] args = new Object[ai.getArgs().length + 1];
			args[0] = rjp;
			for (int i = 1; i < args.length; i++)
				args[i] = ai.getArgs()[i - 1];		
			
			for (Method method : methods) {
				try {				
					//System.out.println("--- "+thisAspectName+" invoking [ack("+ai.isAck()+")] : "+method);
					method.invoke(this, args);
				} catch (IllegalArgumentException iae) {
					System.err.println("Bad remote method/advice argument definition in : "+method);
					iae.printStackTrace();
					throw new InvokationException(iae);
				} catch (Exception e) {
					e.printStackTrace();
					throw new InvokationException(e);

				}
			}		
	}

	public void update(NodeHandle handle, boolean joined) {
		
		Collection<Method> methods = thisEndPoint.getRemoteUpdates(thisAspectName);
		for (Method method : methods) { 
		  try {				
			//System.out.println("AR calling update method :"+method);
			method.invoke(this, handle, joined);
		  } catch (IllegalArgumentException iae) {
			System.err.println("Bad remote update arguments (must be a nodehandle and a boolean) definition in : "+method);
			iae.printStackTrace();			
		  } catch (Exception e) {
			e.printStackTrace();			
		  }
		}  
	}
	
	

}