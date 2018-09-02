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

import java.io.Serializable;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import damon.invokation.AspectInvoker;
import damon.reflection.ReflectionHandler;
import damon.registry.Registry;
import damon.timer.DamonTimer;
import damon.util.Context;
import damon.activation.AspectActivator;
import damon.activation.AspectControl;
import easypastry.core.PastryKernel;
import easypastry.dht.DHTException;
import easypastry.dht.DHTHandler;

public class DamonCore extends PastryKernel {

	private static AspectStorage storage;
	private static AspectControl control;
	private static AspectContainer container;
	private static AspectHotDeployer deployer;	
	private static DamonTimer timer;
	
	public static void init(String configPath) throws Exception {
		init("",-1,configPath);
	}
	
	public static void init(String bhost, String configPath) throws Exception {
		init(bhost,-1,configPath);
	}
		
	public static void init(String bhost, int bport, String configPath) throws Exception  {
		
		PastryKernel.init(bhost,bport,configPath);
		
		// load config
		Properties config = loadProps(configPath);

		// init deployer
		String engine = (String) config.getProperty(Context.ASPECT_ENGINE);
		if (engine == null)
			throw new InstantiationException("Error reading config file <"
					+ configPath + "> : cannot find propertie : "
					+ Context.ASPECT_ENGINE);
		deployer = (AspectHotDeployer) Class.forName(
				"damon." + engine + ".HotDeployer").newInstance();

		
		DHTHandler dht = PastryKernel.getDHTHandler(Context.STORAGE);
		storage = new AspectStorage(dht);

		// init control
		control = new AspectControl(storage);
		
		// init container
		container = new AspectContainer(conn, cast, storage, deployer);
				
		//init network
		PastryKernel.getPastryConnection().bootNode();
		
		//init timer
		timer = new DamonTimer();
		timer.start();

	}
		
    public static void close() {
 	  container.close(); 	  	
	}

	public static AspectStorage getStorage() {
		return storage;
	}

	public static AspectControl getControl() {
		return control;
	}

	public static void registerGroup(String url) throws Exception {

		Collection<Object[]> aspects = null;
		try {
			aspects = (Collection<Object[]>) Registry.lookup(url);
			Registry.rebind(url, (Serializable) aspects);
		} catch (DHTException pe) {
			aspects = new Vector<Object[]>();
			Registry.bind(url, (Serializable) aspects);
		}

		control.activateLocallyAll(aspects);
		container.register(url);
	}

	public static AspectContainer getContainer() {
		return container;
	}

	public static AspectActivator getActivator() {
		return (AspectActivator) container;
	}

	public static AspectInvoker getInvoker() {
		return (AspectInvoker) container;
	}

	public static ReflectionHandler getReflection() {
		return (ReflectionHandler) container;
	}

	public static void setClassLoader(ClassLoader classLoader) {
		deployer.setClassLoader(classLoader);
	}


	
}
