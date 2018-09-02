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
package damon.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;

import org.jdom.JDOMException;

import damon.annotation.DistributedAspect;
import damon.reflection.MetaData;
import damon.util.ByteClassLoader;
import damon.util.ByteWrapper;
import damon.util.Context;
import damon.util.XMLParser;
import easypastry.dht.DHTException;
import easypastry.dht.DHTHandler;

/**
 * Damon AspectContainer class
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */

public class AspectStorage {
	
	private DHTHandler dht;
	
	public AspectStorage(DHTHandler dht) {
		this.dht = dht;		
	}
	/**
	 * When the aspect name and the aspect file name are the same
	 * @param aspectName
	 * @throws ClassNotFoundException
	 * @throws PersistenceException 
	 * @throws IOException 
	 */	
	public void deploy(String aspectName) throws ClassNotFoundException, DHTException, IOException {
	  deploy(aspectName,aspectName);	
	}
	
	/**
	 * Deploy a named aspect in the decentralized container to be activated later
	 * @param name
	 * @param aspectFile
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws PersistenceException 
	 */
	public void deploy(String name, File aspectFile) throws IOException, ClassNotFoundException, DHTException {
		
	    String filename = aspectFile.getName();
	    String aspectName = filename.substring(0, filename.indexOf('.')-1);
	    FileInputStream fis = new FileInputStream(aspectFile);
        
	    //Load class
        int size = (int) aspectFile.length();
        byte[] bytes = new byte[size];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = fis.read (bytes, offset, bytes.length - offset)) >= 0) {
          offset += numRead;
        }
	    
        ByteClassLoader loader = new ByteClassLoader (bytes, Thread.currentThread().getContextClassLoader());
	    Class aspect = loader.loadClass (aspectName);
	    deploy(name,aspect);
	}
	
	/**
	 * Deploy a named aspect in the decentralized container to be activated later
	 * @param name
	 * @param aspectName
	 * @throws ClassNotFoundException
	 * @throws PersistenceException 
	 * @throws IOException 
	 */
	public void deploy(String name, String aspectName) throws ClassNotFoundException, DHTException, IOException {
	    Class aspect = ClassLoader.getSystemClassLoader().loadClass(aspectName);
	    deploy(name,aspect);
	}
	
	public void deploy(String name, Class aspectClass) throws DHTException, IOException {
		
		dht.put(Context.URL_BASE + '/' + name, ByteWrapper.wrap(aspectClass));
	}
	
	public Class retrieve(String name) throws DHTException, IOException, ClassNotFoundException {
		byte[] bytes = (byte[]) dht.get(Context.URL_BASE + '/' + name);
		if (bytes!=null) return (Class) ByteWrapper.unwrap(bytes);
		else return null;
		  
	}
	
	public void undeploy(String name) throws DHTException {		
		dht.remove(Context.URL_BASE + '/' + name);
	}
	
	public DistributedAspect getAspectAnnotation(String aspectName) throws DHTException, IOException, ClassNotFoundException {
		Class aspectClass = retrieve(aspectName);
		Annotation[] as = aspectClass.getAnnotations();
		DistributedAspect  da = (DistributedAspect) as[0];		
		return da;
	}
	
	/***** XML ******/
	
		/**
	 * When the aspect name and the aspect file name are the same
	 * @param aspectName 
	 * @throws PersistenceException 
	 * @throws IOException 
		 * @throws ClassNotFoundException 
		 * @throws JDOMException 
	 */	
	public void deployXML(String xmlName) throws DHTException, IOException, JDOMException, ClassNotFoundException {
	  deployXML(xmlName,xmlName);	
	}
	/**
	 * Deploy a descriptor and the associated aspect in the decentralized container to be activated later
	 * @param xml network name
	 * @param xml filename
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws PersistenceException 
	 * @throws JDOMException 
	 * @throws ClassNotFoundException 
	 */
	public void deployXML(String xmlName, String xmlFileName) throws DHTException, IOException, JDOMException, ClassNotFoundException {
		  deployXML(xmlName,new File(xmlFileName));	
	}
	
	/**
	 * Deploy a descriptor and the associated aspect in the decentralized container to be activated later
	 * @param xml network name
	 * @param xml file
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws PersistenceException 
	 * @throws JDOMException 
	 * @throws ClassNotFoundException 
	 */
	public void deployXML(String name, File xmlFile) throws IOException, DHTException, JDOMException, ClassNotFoundException {
				
	    //String filename = xmlFile.getName();
	    //String aspectName = filename.substring(0, filename.indexOf('.')-1);
		MetaData md = XMLParser.parse(xmlFile);
	    String aspectName = md.getName();
	    
	    FileInputStream fis = new FileInputStream(xmlFile);    
        
	    //Load file
        int size = (int) xmlFile.length();
        byte[] bytes = new byte[size];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = fis.read (bytes, offset, bytes.length - offset)) >= 0) {
          offset += numRead;
        }
	    
	    deployXML(name,bytes);
	    deploy(aspectName);
	}
		
	public void deployXML(String name, byte[] bytes) throws DHTException {
		
		dht.put(Context.URL_BASE + '/' + name, bytes);
	}
	
	public byte[] retrieveXML(String name) throws DHTException {
		
		return (byte[]) dht.get(Context.URL_BASE + '/' + name);	  	  
		  
	}
	
	public DHTHandler getDHTHandler() {
      return dht;		
	}
}
