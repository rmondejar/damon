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
package damon.metalevel.aspectwerkz;

import java.util.Vector;

import org.codehaus.aspectwerkz.annotation.*;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import rice.p2p.commonapi.NodeHandle;

import damon.annotation.Abstractions;
import damon.annotation.Type;
import damon.core.DamonCore;
import damon.invokation.AspectInvokation;
import damon.invokation.InvokationException;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.metalevel.AspectMetaInvokation;
import damon.reflection.MetaData;
import damon.reflection.thisEndPoint;
import damon.util.AnnotationParser;
import easypastry.cast.CastContent;
import easypastry.cast.CastFilter;

/**
 * MetaPointcut capture for remote services
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class MetaPointcutObserver implements CastFilter {
	
	@Around ("execution(public damon.metalevel.aspectwerkz.DistributedMetaAspect.new(..))")
	public Object startingAdvice(JoinPoint joinPoint) throws Throwable {
		
		Object res = joinPoint.proceed();		
		DistributedMetaAspect dma = (DistributedMetaAspect) joinPoint.getThis();
		MetaData metadata = AnnotationParser.parse(dma.getClass());
		
        //System.out.println("Distributed Aspect ("+ar+") is registered");		
		//thisEndPoint.setInstance(ar.thisAspectName,ar);
		
		//System.out.println("STARTING DISTRIBUTED META-ASPECT : "+dma.getName()+" in "+metadata.getGroup());
		//thisEndPoint.getRemoteMetaPointcuts(dma.getName(), id, Type.AROUND);
		
		//System.out.println("ADDING RMP : "+method+" type : "+type);
		//if (type.equals(Type.AROUND)) {
		/**
		 * TODO : Name != ClassName -> Problems with multiple instances
		 */
		DamonCore.getContainer().addFilter(metadata.getGroup(), dma.getName(), this);
		//}
		
		return res;
	}
	
	

	
	
	/*
     * Before Remote Service
     * After Ack Remote Service for Synchronous Invocation with Null Result
     */
    @Around ("call(* damon.invokation.AspectInvoker.invoke(..)) AND args(nh, ai)")
    public Object beforeMetaDirectInvoke(JoinPoint joinPoint, NodeHandle nh, AspectInvokation ai) throws Throwable {
    	//System.out.println("METAPOINTCUT BEFORE 1 : "+ai);
    	boolean doIt = metaInvokationArrive(ai, Type.BEFORE);
    	if (doIt) {
    	  Object result = joinPoint.proceed(); 
    	  if (ai.isSynchronous() && result==null) {    		
    		AspectInvokation ai2 = new AspectInvokation(ai.getName(), ai.getSubject(), ai.getId(), ai.getCode(), ai.getArgs(),result);
    		metaInvokationArrive(ai2, Type.AFTER);
    	  }
    	  return result;
    	}   	  
    	else return null;
	}
    
    /*
     * Before Remote Service
     * After Ack Remote Service for Synchronous Invocation with Null Result
     */
    @Around ("call(* damon.invokation.AspectInvoker.invoke(..)) AND args(abs, ai)")    
    public Object beforeMetaInvoke(JoinPoint joinPoint, Abstractions abs, AspectInvokation ai) throws Throwable {
    	//System.out.println("METAPOINTCUT BEFORE 2 : "+ai);
    	boolean doIt = metaInvokationArrive(ai, Type.BEFORE);
    	if (doIt) {
    	  Object result = joinPoint.proceed();
    	  //System.out.println("Result : "+result+" for "+ai+" is syncho? : "+ai.isSynchronous());    	
    	  if (ai.isSynchronous() && result==null) {
    		//System.out.println("NULL ACK ARRIVE : args num : "+ai.getArgs().length);
    		  
    		//if null ACK --> Fake MetaInvocation  
    		AspectInvokation ai2 = new AspectInvokation(ai.getName(), ai.getSubject(), ai.getId(), ai.getCode(), ai.getArgs(),result);
    		metaInvokationArrive(ai2, Type.AFTER);
    	  }    	  
    	  return result;
    	}
    	else return null;
	}
    
    /*
     * After Remote Service
     */
    //@Around ("call(* damon.invokation.AspectInvoker.invokationArrive(..)) AND args(ai)")
    @Around ("call(* damon.core.AspectContainer.invokationArrive(..)) AND args(ai)")
    public Object afterMetaInvoke(JoinPoint joinPoint, AspectInvokation ai) throws Throwable {  
      //System.out.println("METAPOINTCUT AFTER  ("+ai+") : ");
      boolean doIt = metaInvokationArrive(ai, Type.AFTER);
      if (doIt) return joinPoint.proceed();
      else return null;
	}
   
    
    /*
     * Around Remote Service
     */	
	public boolean contentForwarding(CastContent content) {
		
		if (content instanceof AspectInvokation) {
			
	      AspectInvokation ai = (AspectInvokation) content;
	      //System.out.println("METAPOINTCUT AROUND : "+ai);
	      return metaInvokationArrive(ai, Type.AROUND);
		}  
		
		return true;
	}
       
    private boolean metaInvokationArrive(AspectInvokation ai, Type type) {
    	
    	boolean accept = true;    	
    	
    	if (!ai.isFake()) {
    	  
     	  AspectMetaInvokation ami = new AspectMetaInvokation(ai, type);
    	  Vector<String> names = thisEndPoint.getAspectNames(ai.getSubject());    	  
    	  if (names!=null) {
    		
	        for(String name : names) {
	        	//System.out.println("MPO names : "+name);
	        	Vector<AspectRemoting> instances = thisEndPoint.getInstances(name);
	        	//System.out.println("MPO instances : "+instances);
	        	for(AspectRemoting ar : instances) {
	        		if (ar instanceof DistributedMetaAspect) {
	        			
	        			DistributedMetaAspect dma = (DistributedMetaAspect) ar;
	        			//System.out.println("MPO >>> "+dma);
	        			try {
							accept &= !dma.checkMetaPointcuts(ami);
							//System.out.println("MPO >>> ACCEPT? "+accept);
						} catch (InvokationException e) {							
							e.printStackTrace();
						}
	        		}
	        	}        	
	        }	        
    	  }    	  
    	}  
		
    	return accept;
	}  	
}	