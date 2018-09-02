package damon.test;

import damon.activation.AspectControl;
import damon.core.AspectStorage;
import damon.core.DamonCore;

public abstract class AbstractTest {
	
	public void init(String bhost, String target) throws Exception {
		if (bhost==null) DamonCore.init("damon-config.xml");
		else DamonCore.init(bhost, "damon-config.xml");      
	    DamonCore.registerGroup(target);
	}
	
	public void init(String target) throws Exception {		
	    DamonCore.init("damon-config.xml");	      
	    DamonCore.registerGroup(target);
	}
	
	public void deploy(String aspectName) {
		  try {	      
		    	    
			AspectStorage storage = DamonCore.getStorage();
		    AspectControl control = DamonCore.getControl();   
		    
		    long t0 = System.currentTimeMillis();	    
		    
		    System.out.println ("Deploying "+aspectName+"...");
		    
		    storage.deploy(aspectName);	    
		    
		    long t1 = System.currentTimeMillis();
		    
		    System.out.println ("Activating "+aspectName+"...");

		    control.activate(aspectName);
		    
		    long t2 = System.currentTimeMillis();
		 
		    System.out.println ("Deploy time     : "+(t1-t0)+" ms.");
		    System.out.println ("Activation time : "+(t2-t1)+" ms.");
		    System.out.println ("Total time      : "+(t2-t0)+" ms.");
		    	    
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }

	public void activateLocally(String aspectName) {
		  try {	       
			
		    AspectControl control = DamonCore.getControl();   
		    		    
		    long t1 = System.currentTimeMillis();
		    
		    System.out.println ("Activating "+aspectName+"...");

		    control.activateLocally(aspectName);
		    		    
		    long t2 = System.currentTimeMillis();
		 		    
		    System.out.println ("Activation time : "+(t2-t1)+" ms.");		    
		    	    
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
	
	public void passivate(String aspectName) {
		  try {	        	    
			
		    AspectControl control = DamonCore.getControl();   
		    
		    long t1 = System.currentTimeMillis();
		    
		    System.out.println ("Passivating "+aspectName+"...");

		    control.passivate(aspectName);
		    
		    long t2 = System.currentTimeMillis();
		    
		    System.out.println ("Passivate time : "+(t2-t1)+" ms.");
		 
		    	    
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
	  }
    
	public abstract void test(Object ...params) throws Exception;

}
