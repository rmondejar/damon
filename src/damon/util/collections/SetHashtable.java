package damon.util.collections;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class SetHashtable<K,V> extends Hashtable<K, Set<V>> implements Iterable<V> {
	
	private static final long serialVersionUID = -8267523378140660869L;

	public synchronized void put(K key, V value) {
		 
	    Set<V> set = super.get(key);
		if (set==null) {		  		  
		  set = new HashSet<V>();	      	
		}
		set.add(value);
		super.put(key, set);
	}
	
	public Set<V> get(K key) {		 
	    if (super.containsKey(key)) return super.get(key);	
	    else return new HashSet<V>();
	}
	
	public Set<V> remove(K key) {		 
	    return super.remove(key);			  	
	}

	public void remove(K key, V value) {
		
		Set<V> set = super.get(key);
		if (set!=null) {		  		  
		  set.remove(value);	      	
		}				
	}

	@Override
	public Iterator<V> iterator() {
		Vector<V> v = new Vector<V>();
		for (Set<V> set : super.values()) {
			v.addAll(set);
		}
		return v.iterator();
	}
	
}
