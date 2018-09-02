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
package damon.test.g5k.deploy;

import java.util.Collection;
import java.util.Vector;

import damon.activation.AspectControl;
import damon.core.AspectStorage;
import damon.core.DamonCore;
import damon.test.AbstractTest;
import damon.util.UID;

/**
 * This class encapsulates the Sensor Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class SensorHost extends AbstractTest {
	
  private String hostname;
  
  public SensorHost() {
	 this.hostname = DamonCore.getHostName();  
  }
  
  public String getHostName() throws InterruptedException {
	Thread.sleep(5000);
    return hostname;
    
  }
  
private int activationTestbed(Collection<String> keys) {
	  
	  int fails = 0;
	  AspectControl control = DamonCore.getControl();
	  
	  System.out.println("Activating Dumpers ... ");
	  for(String key : keys) {
		  try { 
	  		  control.activate(key);	  		  
	  		} catch (Exception e){
	  			fails++;
	  		}
	  }	   	
	  return fails;
  }

  private Collection<String> deployTestbed() {
	
	AspectStorage storage = DamonCore.getStorage();  
	 
	String aspectName = "damon.test.g5k.fault.Dump";
	 System.out.println("Deploying Dumpers ... ");
	 Vector<String> names = new Vector<String>(); 
 	 for (int i=0;i<1000;) {  
 		try { 
 		  //generating random name
 		  String randomName =  UID.getUID();
 		  storage.deploy(randomName,aspectName);
 		  names.add(randomName);
 		  i++;
 		} catch(Exception e) {}  
    }	
	return names;
}

  public void test(Object... params) throws Exception {
	  
	 System.out.println("Starting Test ... ");
	 System.out.println("Deploying Dumpers ... ");	
	 Collection<String> keys = deployTestbed();
	 System.out.println("Activating Dumpers ... ");
     activationTestbed(keys);
  	 System.out.println("Obtaining information ... ");  
  	 for(;;) {
  		getHostName();
  	 }
  }
  
  public static void main (String[] args) {  
	
    try {
            
      if (args.length>0) DamonCore.init(args[0], "damon-config.xml");
      else DamonCore.init("damon-config.xml");
      
      DamonCore.registerGroup("p2p://test.urv.cat");
      
      SensorHost host = new SensorHost();  
      host.deploy("damon.test.g5k.multi.Sensor");
      host.test();
      
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}