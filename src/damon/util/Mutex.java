package damon.util;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Mutex {

	private Hashtable<String,Object> values;
	private Set<String> unblocked, waiting;
	//private Cache<String,Object> cache;
	
	public Mutex() {
		
		values = new Hashtable<String,Object>();
		unblocked = new HashSet<String>();
		waiting = new HashSet<String>();	
		//cache = new Cache<String,Object>();
				
	}
	
	public synchronized boolean isWaiting(String code) {
	   return waiting.contains(code);	
	}
	
	public void notify(String code, Object object) {
		//System.out.println("notify ("+code+","+object+")");
		 
		unblocked.add(code);
		if (object!=null) {
			values.put(code,object);
			//cache.putValue(code,object);
		}
	}

	public synchronized Object wait(String code, int timeout, int retry) {
				
		unblocked.remove(code);		
		waiting.add(code);
		boolean found = false;
		while((!found) && retry>0) {
			found = unblocked.contains(code); //|| cache.hasValue(code);
			if (!found) {
			  try {
				Thread.sleep(timeout);
			  } catch (InterruptedException e) {}	
			  retry--;
			}			
		}
		
		unblocked.remove(code);
		waiting.remove(code);
		Object value = values.remove(code);
		//System.out.println("wait ("+code+","+value+")");
		if (value==null) {
			//value = cache.getValue(code);
			//System.out.println("cache ("+code+","+value+")");
		}
		
		return value;
	}
	
	
	

}
