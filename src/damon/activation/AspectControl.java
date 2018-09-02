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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Vector;

import org.jdom.JDOMException;

import damon.annotation.Abstractions;
import damon.annotation.DistributedAspect;
import damon.core.AspectStorage;
import damon.core.DamonCore;
import damon.reflection.MetaData;
import damon.reflection.thisEndPoint;
import damon.registry.RegistryException;
import damon.util.AnnotationParser;
import damon.util.XMLParser;
import easypastry.dht.DHTException;

/**
 * Damon AspectControl class
 * 
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */

public class AspectControl {

	private AspectStorage storage;

	public AspectControl(AspectStorage storage) {
		this.storage = storage;
	}

	/**
	 * Activate an aspect remotely
	 * 
	 * @param aspectName
	 * @throws Exception 
	 */

	public void activate(String aspectName) throws Exception {
		
		Class aspectClass = storage.retrieve(aspectName);
		Annotation[] as = aspectClass.getAnnotations();
		DistributedAspect da = (DistributedAspect) as[0];		

		Abstractions abstraction = da.abstraction();		
		String url = da.target();
		int num = da.num();
		
		activate(aspectName, url, abstraction, num);

	}
	
	public void activate(String aspectName, String url) throws Exception {
		
		Class aspectClass = storage.retrieve(aspectName);
		Annotation[] as = aspectClass.getAnnotations();
		DistributedAspect da = (DistributedAspect) as[0];

		Abstractions abstraction = da.abstraction();		
		int num = da.num();
		
		activate(aspectName, url, abstraction, num);

	}

	public void activate(String aspectName, String url, Abstractions abstraction) throws Exception {
		
		Class aspectClass = storage.retrieve(aspectName);
		Annotation[] as = aspectClass.getAnnotations();
		DistributedAspect da = (DistributedAspect) as[0];
		
		int num = da.num();
		
		activate(aspectName, url, abstraction, num);

	}
	
	public void activate(String aspectName, String url, Abstractions abstraction, int num) throws Exception {
		
		AspectActivator activator = DamonCore.getContainer();
		
		switch (abstraction) {

		case LOCAL:
			activator.activateLocally(aspectName, false, null);
			break;
		case HOPPED: case COORD: case DIRECT: case MULTI: case ANY:
			activator.activate(abstraction, new AspectActivation(url, aspectName, true));
			break;
		case MANY:
			
			activator.activate(abstraction, new AspectActivation(url, aspectName, num, true));
			break;
		}
	}
		
	public void activateMetaData(MetaData md) throws Exception {
		
		AspectActivator activator = DamonCore.getContainer();		
		
		switch (md.getAbstraction()) {

		case LOCAL:
			activator.activateLocally(md.getName(), false, md);
			break;
		case HOPPED: case COORD: case DIRECT: case MULTI: case ANY:
			activator.activate(md.getAbstraction(), new AspectActivation(md.getGroup(), md.getName(), true, md));
			break;
		case MANY:
			
			activator.activate(md.getAbstraction(), new AspectActivation(md.getGroup(), md.getName(), md.getNumNodes(), true, md));
			break;
		}
	}
	
	public void activateXML(String xmlName) throws Exception {
		
		byte[] bytes = storage.retrieveXML(xmlName);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);		
		MetaData md = XMLParser.parse(bais);
				
		//String aspectName = md.getName();
		Abstractions abstraction = md.getAbstraction();
		AspectActivator activator = DamonCore.getContainer();
		String url = md.getGroup();

		switch (abstraction) {

		case LOCAL:
			activator.activateLocally(xmlName, true, null);
			break;
		case HOPPED: case COORD: case DIRECT: case MULTI: case ANY:
			activator.activate(abstraction, new AspectActivation(url,
					xmlName, true, true));
			break;
		case MANY:

			int num = md.getNumNodes();
			activator.activate(abstraction, new AspectActivation(url,
					xmlName, num, true, true));
			break;
		}
		
}
	
	public void activateLocally(String aspectName) throws Exception {
		AspectActivator activator = DamonCore.getContainer();
		activator.activateLocally(aspectName, false, null);		
	}
	
	public void activateLocallyAll(Collection<Object[]> aspects) throws Exception {
		AspectActivator activator = DamonCore.getContainer();
		
		for(Object aspect[] : aspects) {			
			activator.activateLocally((String)aspect[0], (Boolean) aspect[1], null);
		}
		
	}

	/**
	 * Passivate the aspect
	 * 
	 * @param aspectName
	 * @throws RemoteException
	 * @throws PersistenceException
	 * @throws RegistryException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public void passivate(String aspectName) throws DHTException, RegistryException, IOException, ClassNotFoundException {
		// read annotation from aspect

		Class aspectClass = storage.retrieve(aspectName);
		Annotation[] as = aspectClass.getAnnotations();
		DistributedAspect da = (DistributedAspect) as[0];

		Abstractions abstraction = da.abstraction();
		AspectActivator activator = DamonCore.getContainer();
		String url = da.target();

		switch (abstraction) {

		case LOCAL:
			activator.passivateLocally(url,aspectName);
			break;
		case HOPPED: case COORD: case DIRECT: case MULTI: case ANY:
			activator.passivate(abstraction, new AspectActivation(url, aspectName, false));
			break;
		case MANY:

			int num = da.num();
			activator.passivate(abstraction, new AspectActivation(url,
					aspectName, num, false));
			break;
		}
	}
	
	public void passivateMetaData(MetaData md) throws DHTException, RegistryException {
		// read annotation from aspect

		AspectActivator activator = DamonCore.getContainer();
		
		switch (md.getAbstraction()) {

		case LOCAL:
			activator.passivateLocally(md.getGroup(),md.getName());
			break;
		case HOPPED: case COORD: case DIRECT: case MULTI: case ANY:
			activator.passivate(md.getAbstraction(), new AspectActivation(md.getGroup(), md.getName(), false, md));
			break;
		case MANY:
   		    activator.passivate(md.getAbstraction(), new AspectActivation(md.getGroup(), md.getName(), md.getNumNodes(), false, md));
			break;
		}
	}


	public void passivateXML(String xmlName) throws DHTException, JDOMException, IOException, RegistryException {
		
		byte[] bytes = storage.retrieveXML(xmlName);
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);		
		MetaData md = XMLParser.parse(bais);
				
		//String aspectName = md.getName();
		Abstractions abstraction = md.getAbstraction();
		AspectActivator activator = DamonCore.getContainer();
		String url = md.getGroup();

		switch (abstraction) {

		case LOCAL:
			activator.passivateLocally(url, xmlName);
			break;
		case HOPPED:
		case COORD:
		case DIRECT:
		case MULTI:
		case ANY:
			activator.activate(abstraction, new AspectActivation(url,
					xmlName, false, true));
			break;
		case MANY:

			int num = md.getNumNodes();
			activator.activate(abstraction, new AspectActivation(url,
					xmlName, num, false, true));
			break;
		}	
	}
	
	public void passivateLocallyAll(String url) throws DHTException, JDOMException, IOException {
		passivateLocallyAll(url,thisEndPoint.getAspectNames(url));		
	}
	
	public void passivateLocallyAll(String url, Collection<String> c) throws DHTException, JDOMException, IOException {
		AspectActivator activator = DamonCore.getContainer();		
		Vector<String> aspects = new Vector<String>();
		if (c!=null) aspects.addAll(c);
		for(String aspectName : aspects) {	
			activator.passivateLocally(url, aspectName);			
		}		
	}

	/**
	 * Loads the class metadata using the Storage
	 * @param aspectName
	 * @return
	 * @throws PersistenceException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public MetaData getMetaData(String aspectName) throws DHTException, IOException, ClassNotFoundException {
		
		Class aspectClass = storage.retrieve(aspectName);		
		return AnnotationParser.parse(aspectClass);		
	}
	
	/**
	 * Loads the metadata class using the ClassLoader 
	 * @param String aspectName
	 * @param ClassLoader cl
	 * @return MetaData md
	 * @throws PersistenceException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public MetaData getMetaData(String aspectName, ClassLoader cl) throws DHTException, IOException, ClassNotFoundException {
		
		//Class aspectClass = storage.retrieve(aspectName);
		Class aspectClass = cl.loadClass(aspectName);
		return AnnotationParser.parse(aspectClass,cl);		
	}
		



}
