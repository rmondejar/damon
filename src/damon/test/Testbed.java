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

import damon.core.DamonCore;

/**
 * Damon's main testcase
 * @author Ruben Mondejar <ruben.mondejar@urv.cat>
 * @version 2.0
 */
public class Testbed extends TestCase {

  public Testbed(String name) {
    super (name);
  }

  /**
   * Method executed at the beginning of the testcase
   */
  protected void setUp() {
    System.out.println ("[Testbed] - Setting >>>>>>>>> UP <<<<<<<<<<<");
    try {
      DamonCore.init("damon-config.xml");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Method executed at the end of the testcase
   */
  protected void tearDown() {
    System.out.println ("[Testbed] - Tearing >>>>>>>>> DOWN <<<<<<<<<<\n");
    try {
      DamonCore.close();
      Thread.sleep (1000);
   } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Test suite() {
    TestSuite t = new TestSuite();
    t.addTestSuite (RegistryTest.class);    
    t.addTestSuite (ActivationTest.class);
    return t;
  }
  
}
