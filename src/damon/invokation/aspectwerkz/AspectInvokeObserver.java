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
package damon.invokation.aspectwerkz;

import java.util.Hashtable;
import java.util.Vector;

import org.codehaus.aspectwerkz.annotation.*;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.invokation.AspectInvokation;
import damon.reflection.thisEndPoint;

/**
 * Pointcut pre-definition for remote calls
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class AspectInvokeObserver {
	
	private static Hashtable<String, Vector<AspectInvokation>> queues = new Hashtable<String,Vector<AspectInvokation>>();

	@Around ("execution(public damon.invokation.aspectwerkz.AspectRemoting.new(..))")
	public Object startingAdvice(JoinPoint joinPoint) throws Throwable {		
		Object res = joinPoint.proceed();		
		AspectRemoting ar = (AspectRemoting) joinPoint.getThis();
        //System.out.println("Distributed Aspect ("+ar+") is registered");		
		thisEndPoint.setInstance(ar.thisAspectName,ar);
		return res;
	}
	
	/**********************************INNER PATCH*******************************/
	
	/*
	
	@Around ("get(* damon.invokation.aspectwerkz.AspectRemoting.self)")
	public Object getSelf(JoinPoint joinPoint) throws Throwable {		
		AspectRemoting ar = (AspectRemoting) joinPoint.proceed();
		//System.out.println("Get Self "+res);
		thisEndPoint.setInstance(ar.thisAspectName,ar);
		return ar;
	}
	
	@Before ("execution(* damon.core.AspectContainer.startPoint(..))")
	public void startPoint(JoinPoint joinPoint) throws Throwable {}
	
	
	@Around ("set(* damon.invokation.aspectwerkz.AspectRemoting.self)")
	public Object setSelf(JoinPoint joinPoint) throws Throwable {		
		MemberSignature signature = (MemberSignature)joinPoint.getSignature();
        System.out.println("ENTER: " + joinPoint.getTargetClass().getName() + "::" + signature.getName());
        return joinPoint.proceed();

	}
	
	@Before ("call(* java.lang.ClassLoader.getResources(..))")
	public void startingAdvice() {		
		System.out.println("starting point 2 : AspectInvokeObserver");
	}
	*/
	
	/**********************************************************************************/
	
    @Around ("execution(* damon.core.AspectContainer.invokationArrive(..)) AND args(ai)")
    public Object invokeAdvice(JoinPoint joinPoint, AspectInvokation ai) throws Throwable { 
      if (ai!=null && !ai.isAck()) {   	
    	//System.out.println("AIO  ("+ai+") : "+ai.append);	
        Vector<String> names = thisEndPoint.getAspectNames(ai.getSubject()); 
        if (names!=null) {
          for(String name : names) {
	        queue(name, ai);
          }
        } else System.out.println("WARNING : not aspects found for url : "+ai.getSubject());  
      }  
      
      return joinPoint.proceed();
	}
	
    private static synchronized void queue(String name, AspectInvokation ai) {
		  
		  Vector<AspectInvokation> queue = queues.get(name);	  
		  if (queue==null) queue = new Vector<AspectInvokation>();	
		  queue.add(ai);		  
		  queues.put(name, queue);	  
			
	}
	              
    public static synchronized AspectInvokation nextInvokation(String name) {
    	
	  AspectInvokation ai = null;
	  Vector<AspectInvokation> queue = queues.get(name);
	  if (queue!=null) {
	    if (!queue.isEmpty()) {
		  ai = queue.remove(0);		  
	    }
	    queues.put(name, queue);
	  }  
	  return ai;
		
	}
	

   	
}	