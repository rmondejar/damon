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
package damon.aspectwerkz;

import java.util.Hashtable;

import org.codehaus.aspectwerkz.transform.inlining.deployer.Deployer;
import org.codehaus.aspectwerkz.transform.inlining.deployer.DeploymentHandle;

import damon.core.AspectHotDeployer;

public class HotDeployer implements AspectHotDeployer {
	
  private ClassLoader classLoader;
	
  private Hashtable<String, Object[]> handles = new Hashtable<String, Object[]>();
	
  public String deploy(String aspectName, Class aspectClass) { 
	
	DeploymentHandle dh;	
	AnnotationInfo info = AnnotationExtractor.extract(aspectClass);	
	String xmlDef = DocumentCreator.createXML(aspectClass, info);	
	if (classLoader==null) dh = Deployer.deploy(aspectClass, xmlDef, ClassLoader.getSystemClassLoader());
	else  dh = Deployer.deploy(aspectClass, xmlDef, classLoader);	
	handles.put(aspectName,new Object[]{aspectClass,classLoader});
	//System.out.println("load "+aspectName+" --> "+classLoader);
	return dh.toString();
  }
  
  public String deploy(String aspectName, Class aspectClass, ClassLoader cl)  { 
		
	    if (cl==null) return deploy(aspectName,aspectClass);
	    	    
		DeploymentHandle dh;		
		try {
			aspectClass = cl.loadClass(aspectName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
			
		AnnotationInfo info = AnnotationExtractor.extract(aspectClass, cl);	
		String xmlDef = DocumentCreator.createXML(aspectClass, info);	
		if (cl==null) dh = Deployer.deploy(aspectClass, xmlDef, cl);
		else  dh = Deployer.deploy(aspectClass, xmlDef, cl);	
		handles.put(aspectName,new Object[]{aspectClass,cl});
		//System.out.println("load "+aspectName+" --> "+classLoader);
		return dh.toString();
	  }
  
  
	
  public boolean isDeployed(String aspectName) {
	return handles.containsKey(aspectName); 	
  }
		
  public void undeploy(String aspectName) {
	  
	if (handles.containsKey(aspectName)) {  
	  Object[] handle = handles.remove(aspectName);
	  //DeploymentHandle dh = (DeploymentHandle) handle[0];
	  Class aspectClass = (Class) handle[0];
	  ClassLoader classLoader = (ClassLoader) handle[1];
	  //System.out.println("unload "+aspectName+" --> "+classLoader);
	  if (aspectClass!=null) {
		if (classLoader!=null) Deployer.undeploy(aspectClass,classLoader);
		else Deployer.undeploy(aspectClass, ClassLoader.getSystemClassLoader());
	  }
	}  
  }


  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }  

}
