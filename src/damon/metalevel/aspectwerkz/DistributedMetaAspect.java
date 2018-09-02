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
package damon.metalevel.aspectwerkz;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Hashtable;

import damon.annotation.Abstractions;
import damon.annotation.Type;
import damon.invokation.AspectInvokation;
import damon.invokation.AspectInvoker;
import damon.invokation.InvokationException;
import damon.invokation.RemoteJoinPoint;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.metalevel.AspectMetaInvokation;
import damon.reflection.thisEndPoint;
import damon.util.Context;

/**
 * Distributed Meta-Aspects allows remote meta-pointcuts y remote meta-advices.
 */
public abstract class DistributedMetaAspect extends AspectRemoting {

	/**
	 * Supplantation is used to communicate Meta-Level with Distributed Aspects, 
	 * in order to avoid the MetaPointcuts and activate Remote Advices/Methods.
	 * @param String method name
	 * @param RemoteJoinPoint rjp
	 * @param Object ... args
	 * @return invokation result if it is synchronous, otherwise return null
	 */
	public Object supplantation(String methodName, RemoteJoinPoint rjp, Object ... args) {	

		Hashtable<String, Object> supplantation = thisEndPoint.getRemoteMetaAdvice(thisAspectName, methodName);

		if (supplantation.isEmpty()
				|| !supplantation.containsKey(Context.TARGET)
				|| !supplantation.containsKey(Context.ABSTRACTION)) {

			return null;
		}

		String url = (String) supplantation.get(Context.TARGET);
		Abstractions abstraction = (Abstractions) supplantation.get(Context.ABSTRACTION);
		String id = (String) supplantation.get(Context.ID);
		boolean synchro = (Boolean) supplantation.get(Context.SYNCHRO);
		boolean lazy = (Boolean) supplantation.get(Context.LAZY);
		AspectInvoker invoker = thisEndPoint.getInvoker();
		AspectInvokation ai = null;

		switch (abstraction) {

		case HOPPED:
			ai = new AspectInvokation(thisAspectName, url, id, args);
			//ai.setKey(key);
			break;
		case LOCAL:
		case DIRECT:
		case MULTI:		
		case ANY:			
			ai = new AspectInvokation(thisAspectName, url, id, args);
			break;
		case MANY:
			int numNodes = ((Integer) supplantation.get(Context.NUM_NODES)).intValue();
			ai = new AspectInvokation(thisAspectName, url, id, args, numNodes);
			break;
		}

		ai.setSynchronism(synchro);
		ai.setLazy(lazy);
		
		//ai.setKey(rjp.getKey());
		ai.setSource(rjp.getOriginator());
		ai.setFake(true);
		
		try {
			
			if (abstraction==Abstractions.DIRECT) {
				Object result = invoker.invoke(ai.getSource(), ai);
				return result;
			}
			else {		
				Object result = invoker.invoke(abstraction, ai);
				return result;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Checks MetaPointcuts when an AspectMetaInvokation arrive, returning a cancel response
	 * @param AspectMetaInvokation ami
	 * @return boolean cancel, if true, accept, if false
	 */
	public boolean checkMetaPointcuts(AspectMetaInvokation ami) throws InvokationException {
	
		//System.out.println("--- "+thisAspectName+" has "+ami);
		
		boolean cancel = false;
	
			AspectInvokation ai = ami.getAspectInvokation();
			//ai.append += " DMA2";
	    	//System.out.println("DMA2  ("+ai+") : "+ai.append);
			Type type = ami.getType();

			Collection<Object[]> rmpcs = thisEndPoint.getRemoteMetaPointcuts(thisAspectName, ai.getId(), type);
			//System.out.println("--- "+thisAspectName+" has "+rmpcs.size()+" metapointcuts");			
			RemoteJoinPoint rjp = new RemoteJoinPoint(ai.getSource());
			rjp.setNum(ai.getNum());
			rjp.setId(ai.getId());
			rjp.setPath(ai.getPath());
			rjp.setSubject(ai.getSubject());
			rjp.setArgs(ai.getArgs());
			rjp.setName(ai.getName());
			rjp.setCode(ai.getCode());
			rjp.setResult(ai.getResult());
						
			Object[] args = null;
			int length = 1;
		    if (ai.getArgs()!=null) length += ai.getArgs().length;
			if (ai.isAck()) length++; 
			args = new Object[length];
			args[0] = rjp;	 
			if (ai.getArgs()!=null) {
		      for (int i = 0; i < ai.getArgs().length; i++)
				args[i+1] = ai.getArgs()[i];
			}
		    if (ai.isAck()) args[length-1] = ai.getResult();
			
			
			for (Object[] rmpc : rmpcs) {
				Method method = (Method) rmpc[0];
				//String target = (String) rmpc[1];
				boolean ack  = (Boolean) rmpc[2];
				
				if (ack == ai.isAck()) {			
				
				try {			
					
					rjp.setCancel(false);					
					method.invoke(this, args);
					//System.out.println("--- "+thisAspectName+" invoking : "+method);
					
					if (rjp.isCancel()) cancel = true;				
					
					ami.modify(rjp.getArgs(),rjp.getResult());					
										
					
				} catch (IllegalArgumentException iae) {
					//String argsText = "";
					//if (args!=null) { for (Object arg : args) argsText += arg.getClass().getName()+","; 
					//                  if (argsText.length()>0) argsText = argsText.substring(0,argsText.length()-2);      }  
					System.err.println("Bad remote metapointcut argument(s) {"+args.length+"} for the definition of : "+method);
					iae.printStackTrace();
					throw new InvokationException(iae);
				} catch (Exception e) {
					e.printStackTrace();
					throw new InvokationException(e);

				}
				}
				//else {
				//	System.out.println("--- "+thisAspectName+" ACK def("+ack+") && ai("+ai.isAck()+") for "+method);
				//}
				
			}
		//}
	  return cancel;
	}
	
}
