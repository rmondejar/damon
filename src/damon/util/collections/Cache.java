package damon.util.collections;

import java.util.Hashtable;

import damon.util.Context;

/**
 * Cache Class
 * @author Ruben Mondejar <ruben.mondejar@urv.cat>
 *
 * @param <K> Key
 * @param <V> Value
 */
public class Cache<K,V> extends Hashtable<K, Object[]> {
	
	private static final long serialVersionUID = -1633487045054321656L;
	private int TTL;
	
	/**
	 * Cache Constructor 
	 * TTL is for default Context.CACHING_TTL
	 */
	public Cache() {
		TTL = Context.CACHING_TTL; 
	}
	
	/**
	 * Cache Constructor 
	 * @param TTL are the seconds of live for a cached value
	 */
	public Cache(int ttl) {
		TTL = ttl*1000;
	}
			
	public synchronized void putValue(K key, V value) {		
		super.put(key, new Object[]{value, System.currentTimeMillis()});
	}
	
	public synchronized V getValue(K key) {
		
		Object[] pair = super.get(key);
		if (pair!=null) {
		  V value = (V) pair[0];
		  long millis = (Long) pair[1];
		  if ((System.currentTimeMillis()-millis)<TTL) {
			return value;
		  }			
		  else {
			super.remove(key);
		    return null;	
		  }
		}
		else return null;
	}
		
	public boolean hasValue(K key) {
				
		Object[] pair = super.get(key);
		if (pair!=null) {
		  long millis = (Long) pair[1];
		  if ((System.currentTimeMillis()-millis)<TTL) {
			return true;
		  }			
		  else {
			super.remove(key);
		    return false;	
		  }
		}
		return false;
	}		

	public Object waitValue(K key, int timeout, int retry) {
		
		System.out.println("queue ("+key+")");
		
		if (hasValue(key)) {
			return getValue(key);
		}
				
		Object value = null;
		while((value==null) && retry>0) {
			if (hasValue(key)) {
			  value = getValue(key);
			}  
			else { 
			  try {
					Thread.sleep(timeout);
				  } catch (InterruptedException e) {}	
				  retry--;				  
			}						
			return value;
		}		
		return null;
	}

}