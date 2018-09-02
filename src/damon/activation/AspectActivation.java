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
package damon.activation;

import damon.reflection.MetaData;
import easypastry.cast.CastContent;

public class AspectActivation extends CastContent {
  
	private static final long serialVersionUID = 7026985200951527206L;
		
	private String name;
	private boolean activate = true;
	private boolean xml = false;
	private MetaData metadata = null;
	
	public AspectActivation (String url, String name, boolean activate) {
	    super(url);
		this.name = name;
		this.activate = activate;
		this.xml = false;	
	}

	public AspectActivation (String url, String name, int num, boolean activate) {
		super(url);
		this.name = name;
		super.num = num;
		this.activate = activate;
		this.xml = false;	
	}
	
	public AspectActivation (String url, String name, boolean activate, boolean xml) {
		super(url);
		this.name = name;
		this.activate = activate;
		this.xml = xml;	
	}
		
	public AspectActivation (String url, String name, int num, boolean activate, boolean xml) {
		super(url);
		this.name = name;
		super.num = num;
		this.activate = activate;
		this.xml = xml;	
	}

	public AspectActivation (String url, String name, boolean activate, MetaData metadata) {
		super(url);
		this.name = name;
		this.activate = activate;
		this.xml = false;	
		this.metadata = metadata;
	}

	public AspectActivation (String url, String name, int num, boolean activate, MetaData metadata) {
		super(url);
		this.name = name;
		super.num = num;
		this.activate = activate;
		this.xml = false;
		this.metadata = metadata;
	}

	public String getName() {
		return name;
	}
	
	public boolean isActivate() {
		return activate;
	}
	
	public boolean isXML() {
		return xml;
	}
	
	public boolean isMetaData() {
		return metadata!=null;
	}
	
	public MetaData getMetaData() {
		return metadata;
	}
	
	public String toString() {
		return "AspectActivation of "+name+" in : "+super.subject;
	}
	
	


}
