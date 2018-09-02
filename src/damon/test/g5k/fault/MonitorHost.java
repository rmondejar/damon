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

package damon.test.g5k.fault;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import damon.activation.AspectControl;
import damon.core.AspectStorage;
import damon.core.DamonCore;
import damon.test.AbstractTest;
import damon.util.UID;

/**
 * This class encapsulates the Monitor Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class MonitorHost extends AbstractTest {   
  
  private Collection<String> deployTestbed() {
	
	AspectStorage storage = DamonCore.getStorage();  
	 
	String aspectName = "damon.test.g5k.fault.Dump";
	 System.out.println("Deploying Dumpers ... ");
	 Vector<String> names = new Vector<String>(); 
 	 for (int i=0;i<100;) {  
 		try { 
 		  //generating random name
 		  String randomName =  UID.getUID();
 		  storage.deploy(randomName,aspectName);
 		  names.add(randomName);
 		  i++;
 		  System.out.println("--> "+i);
 		} catch(Exception e) {}  
    }	
	return names;
  }
  
  private int activationTestbed(Collection<String> keys) {
	  
	  Set<String> fails = new HashSet<String>();
	  int nfails = 0;
	  
	  AspectControl control = DamonCore.getControl();
	  
	  System.out.println("Activating Dumpers ... ");
	  for(String key : keys) {
		  try { 
	  		  control.activate(key);	  		  
	  		} catch (Exception e){
	  			fails.add(key);
	  			System.out.println("Activation Fail");
	  		}
	  }
	  try { Thread.sleep(10000); } catch(Exception e) {}
	  System.out.println("Activating Failures ... ");
	  for(String key : fails) {
		  try { 
	  		  control.activate(key);	  		  
	  		} catch (Exception e){
	  			nfails++;
	  			System.out.println("Activation re-Fail");
	  		}
	  }
	  return nfails;
  }

  
  //interactive test
  public void test(Object... params) throws Exception {
	  
	 BufferedReader dis = new BufferedReader(new InputStreamReader (System.in));
	 System.out.println("Press <Enter> key to start ... ");
	 dis.readLine(); 
	 
	 Collection<String> keys = deployTestbed();
	 	 
	 System.out.println("Press <Enter> key to continue ... ");
	 dis.readLine(); 
	 
	 int result = activationTestbed(keys);
	 System.out.println("Total : "+keys.size());
	 System.out.println("Failures : "+result);
	 System.out.println("Fraction : "+(result / keys.size()));
	 System.out.println("Final : "+(result*100 / keys.size())+" %");
  }
	
  

public static void main (String[] args) {  
	
    try {
         
      DamonCore.init("damon-config.xml");      
      DamonCore.registerGroup("p2p://test.urv.cat");
      
      MonitorHost host = new MonitorHost();  
      host.test();
      System.exit(0);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
