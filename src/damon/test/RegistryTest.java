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
package damon.test;

import junit.framework.*;

import java.util.*;

import damon.core.DamonCore;
import damon.registry.Registry;

public class RegistryTest extends TestCase {

  public RegistryTest (String name) {
    super (name);
  }

  protected void setUp() {
    System.out.println ("[RegistryTest] - Setting >>>>>>>>> UP <<<<<<<<<<<");
       	
      try {
		DamonCore.init("damon-config-test.xml");
	} catch (Exception e) {		
		e.printStackTrace();
	} 
      
  }

  protected void tearDown() {
    System.out.println ("[RegistryTest] - Tearing >>>>>>>>> DOWN <<<<<<<<<<\n");    
  }

  public static Test suite() {
    return new TestSuite (RegistryTest.class);
  }

  public void testRegistry() throws Exception {
	  
	   	
      Registry.bind ("p2p://simple", "value1");
      Registry.bind ("p2p://simple2", "value1");
      Registry.bind ("p2p://simple3", "value1");
      Registry.bind ("p2p://simple/object", "value2");
      Registry.bind ("p2p://simple/object/ser", "value3");
        	
	  String value1 = (String) Registry.lookup("p2p://simple");
      assertEquals (value1, "value1");
      String value2 = (String) Registry.lookup("p2p://simple/object");
      assertEquals (value2, "value2");
      Registry.rebind ("p2p://simple/object", "value3");
      String value3 = (String) Registry.lookup("p2p://simple/object");
      assertEquals (value3, "value3");

      Collection<String> list = Registry.list ("p2p://");
      System.out.println("LIST : "+list);
      int size = list.size();
      assertEquals (4, size); //simple, simple2, simple3, damon.distributed.aspects
  }

  public void testNotBound() throws Exception {
    try {
      Registry.lookup("p2p://simple4");
      fail ("Looked up unexistent value!");
    } catch (Exception ex) {
      assertEquals (true, true);
    }
  }
}
