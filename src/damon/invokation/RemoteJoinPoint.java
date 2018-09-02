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
package damon.invokation;

import java.util.Vector;

import damon.core.DamonCore;
import damon.registry.RegistryException;
import easypastry.dht.DHTException;

import rice.p2p.commonapi.NodeHandle;

public class RemoteJoinPoint {

	private NodeHandle originator;
	private String name;	
	private String id;
	private int num;
	private Vector<NodeHandle> path;
	private String subject;
	private Object[] args;
	private Object result;
	private String code;
	private boolean cancel;

	public RemoteJoinPoint(NodeHandle originator) {
		this.originator = originator;
	}

	public NodeHandle getOriginator() {
		return originator;
	}


	public void setId(String id) {
		this.id = id;

	}
	
	public void setCode(String code) {
		this.code = code;
	}

	public void setPath(Vector<NodeHandle> path) {
		this.path = path;

	}
	
	public void setNum(int num)  {
		this.num = num;
	}
	
	public int getNum()  {
		return num;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getNumOfHops() {
		return path.size();
	}

	public String getId() {
		return id;
	}

	public Vector<NodeHandle> getPath() {
		return path;
	}

	public String getSubject() {
		return subject;
	}
	
	public void setName(String name) {
		this.name = name;		
	}

	public void setOriginator(NodeHandle originator) {
		this.originator = originator;
	}
	

	public void setResult(Object result) {
		this.result = result;		
	}
	

	public Object getResult() {
		return result;		
	}

	public void setArgs(Object[] args) {
		this.args = args;
		
	}
	
	public Object[] getArgs() {
		return args;
	}
	
	public void proceed() {
		proceed(null);
	}
	
	public void proceed(Object result) {
       AspectInvoker invoker =  DamonCore.getInvoker();       
       AspectInvokation ai = new AspectInvokation(name,subject,id,code,args,result);       
       try {
		invoker.invoke(originator, ai);
	} catch (RegistryException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (DHTException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
	
		
	public void cancel() {
		this.cancel = true;
	}
	
	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;		
	}


	

}
