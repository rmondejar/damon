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

import rice.p2p.commonapi.NodeHandle;

public class RegistryRecord implements Serializable, Cloneable {

	private static final long serialVersionUID = -8572303747750229301L;

	private Serializable value;	
	private String url;
	private String parent;
	private Set<NodeHandle> writers = new HashSet<NodeHandle>();
	private Set<String> children = new HashSet<String>();
	
	public String toString() {
		return "RegistryRecord <"+url+","+value+">, children : "+children;
	}

	public void setValue(Serializable value, NodeHandle writer) {
		this.value = value;
		if (writer!=null) writers.add(writer);
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public void addChild(String url) {
		children.add(url);
	}

	public void removeChild(String url) {
		children.remove(url);
	}

	public Serializable getValue() {
		return value;
	}

	public String getUrl() {
		return url;
	}

	public String getParent() {
		return parent;
	}

	public void setWriters(Set<NodeHandle> writers) {
		this.writers = writers;
	}
	
	public Set<NodeHandle> getWriters() {
		return writers;
	}

	public Collection<String> getChildren() {
		return children;
	}
	
	public void setChildren(Set<String> children) {
		this.children = children;
	}

	protected Object clone() {
		RegistryRecord copy = new RegistryRecord();
		copy.setParent(parent);
		copy.setUrl(url);
		copy.setValue(value, null);
		copy.setWriters(writers);
		copy.setChildren(children);
		return copy;
	}
	
	public void merge(RegistryRecord rr) {
        
		//System.out.println("MERGING 1 : "+this);
		//System.out.println("MERGING 2 : "+rr);
		
		if (value instanceof Collection &&
            rr.getValue() instanceof Collection) {
        	
        }
		writers.addAll(rr.getWriters());
		children.addAll(rr.getChildren());
		//System.out.println("MERGING 3 : "+this);
	}

}