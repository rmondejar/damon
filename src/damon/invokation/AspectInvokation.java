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

import damon.util.Utilities;
import easypastry.cast.CastContent;

public class AspectInvokation extends CastContent {

	private static final long serialVersionUID = -2787904668838171498L;
	
	public String append = "";
	
	private String name;
	private Object[] args;
	private String id;
	private String code;
	private boolean synchro = false;
	private Object result;
	private boolean lazy = false;
	private boolean ack = false;
	private boolean fake = false;	

	public AspectInvokation(String name, String url, String id, Object[] args) {
		
		super(url);
		this.name = name;
		this.id = id;
		this.args = args;
		String argsSum = "";
		for (int i = 0; i < args.length; i++) {
			//argsClass[i] = args[i].getClass();
			argsSum += args[i];
		}
		code = Utilities.generateStringHash(name+url+id+argsSum);
	}
	
	public AspectInvokation(String name, String url, String id, Object[] args, int num) {
		
		super(url);
		super.num = num;
		
		this.name = name;
		this.id = id;
		this.args = args;		
		String argsSum = "";
		for (int i = 0; i < args.length; i++) {			
			argsSum += args;
		}
		this.code = Utilities.generateStringHash(name+url+id+argsSum+num);
	}
	
	 public AspectInvokation(String name, String url, String id, String code, Object[] args, Object result) {
		 
		 	super(url);
			
			this.args = args;
			if (args==null) this.args = new Object[]{};					
			String argsSum = "";
			for (int i = 0; i < this.args.length; i++) {				
				argsSum += this.args;
			}
			
			this.name = name;
			this.id = id;
			this.result = result;
			this.ack = true;		
			this.code = code;
	}
	 
	public String getName() {
		return name;
    }

	public Object[] getArgs() {		
		return args;
	}
	
	public void setArgs(Object[] args) {		
		this.args = args;
	}
	
	public void setArg(int pos, Object arg) {		
		this.args[pos] = arg;
	}


	public String getId() {		
		return id;
	}

	public String toString() {
		String s = "AspectInvokation ["+id+"("+super.subject+")]";
		if (ack) s+= " ACK ";
		if (synchro) s+= " SYNCHRO ";
		//s+=" : "+code;
		String argsSum = "";
		for (int i = 0; i < args.length; i++) {
			//argsClass[i] = args[i].getClass();
			argsSum += args[i]+",";
		}
		s+= " ("+argsSum+")"; 
		return s;
	}

	public boolean isSynchronous() {
		return synchro;
	}
	
	public void setSynchronism(boolean synchro) {
		this.synchro = synchro;
	}

	public boolean isLazy() {
		return lazy;
	}

	
	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}
	
	public boolean isFake() {
		return fake;
	}

	
	public void setFake(boolean fake) {
		this.fake = fake;
	}
	


	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public String getCode() {		
		return code;
	}

	public Object getResult() {		
		return result;
	}	
	
	public void setResult(Object result) {
		this.result = result;
	}
}
