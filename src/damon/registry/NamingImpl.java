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
package damon.registry;

import java.io.*;
import java.util.*;

import easypastry.dht.DHTException;
import easypastry.dht.DHTHandler;

import rice.p2p.commonapi.*;

/**
 * This class implements the naming typical methods using t
 * 
 * @author Ruben Mondejar <ruben.mondejar@urv.cat>
 * @version 2.0
 */
public class NamingImpl implements Naming {

	private DHTHandler dht;
	private NodeHandle nh; //local nodehandle

	public NamingImpl(DHTHandler dht, NodeHandle localNH) {
		this.dht = dht;		
		this.nh = localNH;
	}
	
	@Override
	public void bind(String url, Serializable value) throws RegistryException, DHTException {

		// starts with 'p2p://'
		if (!url.startsWith("p2p://")) throw new RegistryException("Invalid url, it do not starts with 'p2p://'");
		
		url = url.substring("p2p://".length());
		String parent = "p2p://";
		String ancient = "";
		
		//splitting
		StringTokenizer st = new StringTokenizer(url,"/");
		
		while(st.hasMoreTokens()) {
            
			String token = st.nextToken();
			if (parent.charAt(parent.length()-1)=='/') token = parent + token;
			else token = parent + '/' + token;
			
			RegistryRecord rrp = null;
			
			//parent exists?					
			try {
			rrp = (RegistryRecord) dht.get(parent);
			} catch(Exception e){}
			
			if (rrp==null) {
				// inserts	
				rrp = new RegistryRecord();
				rrp.setValue("void", nh);
				rrp.setUrl(parent);
				rrp.setParent(ancient);		
			}		
			
			rrp.addChild(token);
			dht.put(parent, rrp);
			
			// inserts
			if (!st.hasMoreTokens()) {
			  RegistryRecord rr = new RegistryRecord();
			  rr.setValue(value, nh);
			  rr.setUrl(url);
			  rr.setParent(parent);
			  dht.put(token, rr);		
			}
			
			//update
			ancient = parent;
			parent = token;			
		}
	}

	
	@Override
	public Collection<String> list(String root) throws DHTException {
		RegistryRecord rr = (RegistryRecord) dht.get(root);
		return rr.getChildren();
	}

	@Override
	public Serializable lookup(String url) throws DHTException {
		
		RegistryRecord rr = (RegistryRecord) dht.get(url);
		if (rr==null) throw new DHTException("Object <"+url+"> is not bound");
		return rr.getValue();
	}


	@Override
	public void rebind(String url, Serializable value) throws DHTException {
		RegistryRecord rr = (RegistryRecord) dht.get(url);
		rr.setValue(value, nh);
		dht.put(url, rr);
	}

	@Override
	public void unbind(String url) throws DHTException {
		RegistryRecord rr = (RegistryRecord) dht.get(url);

		// remove child from parent
		String parent = rr.getParent();
		RegistryRecord rrp = (RegistryRecord) dht.get(parent);
		rrp.removeChild(url);
		dht.put(parent, rrp);

		dht.remove(url);
	}

	@Override
	public Collection<NodeHandle> getWriters(String url)
			throws DHTException {
		
		RegistryRecord rr = (RegistryRecord) dht.get(url);
		if (rr==null) throw new DHTException("Object <"+url+"> is not bound");
		return rr.getWriters();
		
	}

	

}
