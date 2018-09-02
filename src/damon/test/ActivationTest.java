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
import damon.activation.AspectActivation;
import damon.activation.AspectControl;
import damon.annotation.Abstractions;
import damon.core.AspectContainer;
import damon.core.DamonCore;

public class ActivationTest extends TestCase {

	private AspectContainer[] containers;
	private AspectControl[] controls;
	private final int MAX = 4;
	private final String url = "p2p://test";
	private int num;
	private AspectActivation activation;
	private String aspectName = "damom.test.TestAspect";

	public ActivationTest(String name) {
		super(name);
		containers = new AspectContainer[MAX];
		controls = new AspectControl[MAX];	
	}

	protected void setUp() {
		System.out.println("[ActivationTest] - Setting >>>>>>>>> UP <<<<<<<<<<<");
		try {			
			
			for (int i = 0; i < MAX; i++) {
				DamonCore.init("damon-config.xml");				
				containers[i] = DamonCore.getContainer();
				controls[i] = DamonCore.getControl();
				containers[i].register("p2p://test");				
			}
			DamonCore.getStorage().deploy(aspectName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void tearDown() {
		System.out.println("[ActivationTest] - Tearing >>>>>>>>> DOWN <<<<<<<<<<\n");
		for (int i = 0; i < MAX; i++) {
			try {
				containers[i].close();
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Test suite() {
		return new TestSuite(ActivationTest.class);
	}

	public void testActivate() throws Exception {
	 //direct
				
		activation = new AspectActivation(url,	aspectName, true);		
		containers[0].activate(containers[MAX-1].getNodeHandle(), activation);
		Thread.sleep(3000);
		
		num = containers[MAX-1].getAspects().size();
		assertEquals (1, num);
		
	 //hopped
		
		num = 0;
		activation = new AspectActivation(url,	aspectName, true);
		
		containers[0].activate(Abstractions.HOPPED, activation);
		Thread.sleep(3000);
		for (int i=0;i<MAX;i++) {
			  num+= containers[i].getAspects().size();	
		}			
		assertEquals (1, num);
		
	 //any
		num = 0;
		activation = new AspectActivation(url,	aspectName, true);
		
		containers[0].activate(Abstractions.ANY, activation);
		Thread.sleep(3000);
		for (int i=0;i<MAX;i++) {
			  num+= containers[i].getAspects().size();	
		}			
		assertEquals (1, num);
	//many
		
		num = 0;
		activation = new AspectActivation(url,	aspectName, 2, true);
		
		containers[0].activate(Abstractions.MANY, activation);		
		Thread.sleep(6000);
		for (int i=0;i<MAX;i++) {
			  num+= containers[i].getAspects().size();	
		}
			
		assertEquals (2, num);
		
	   //multi
		num = 0;
		activation = new AspectActivation(url,	aspectName, true);
		
		containers[0].activate(Abstractions.MULTI, activation);		
		Thread.sleep(2000*MAX);
		for (int i=0;i<MAX;i++) {
			  num+= containers[i].getAspects().size();	
	    }			
		assertEquals (MAX, num);
	}
}
