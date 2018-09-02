package damon.util.collections;


import java.util.Hashtable;

public class HashHashtable<K,N,V> extends Hashtable<K, Hashtable<N,V>> {
	
	private static final long serialVersionUID = -1633487045054321656L;

	public synchronized void put(K key, N name, V value) {
		 
	    Hashtable<N,V> htn = super.get(key);
		if (htn==null) {		  		  
		  htn = new Hashtable<N,V>();	      	
		}
		htn.put(name,value);
		super.put(key, htn);
	}
	
	public V get(K key, N name) {
		 
	    Hashtable<N,V> htn = super.get(key);
		if (htn!=null) {		  		  
		  return htn.get(name);	      	
		}
		else return null;	  	
	}
	
	public V remove(K key, N name) {
		
	    Hashtable<N,V> htn = super.get(key);
		if (htn!=null) {		  		  
		  return htn.remove(name);	      	
		}
		else return null;	  
	}
	
}
