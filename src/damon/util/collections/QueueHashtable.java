package damon.util.collections;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * Hashtable of K and (FIFO + "Set") of V
 * @author Ruuben
 *
 * @param <K>
 * @param <V>
 */
public class QueueHashtable<K,V> extends Hashtable<K, Vector<V>> implements Iterable<V> {
	
	private static final long serialVersionUID = -8267523378140660869L;

	public synchronized void put(K key, V value) {
		 
	    Vector<V> set = super.get(key);
		if (set==null) {		  		  
		  set = new Vector<V>();	      	
		}
		if (!set.contains(value)) set.add(value);
		super.put(key, set);
	}
	
	public Vector<V> get(K key) {		 
	    if (super.containsKey(key)) return super.get(key);	
	    else return new Vector<V>();
	}
	
	public Vector<V> remove(K key) {		 
	    return super.remove(key);			  	
	}

	public void remove(K key, V value) {
		
		Vector<V> set = super.get(key);
		if (set!=null) {		  		  
		  set.remove(value);	      	
		}				
	}

	public Collection<V> getAll() {
		
		Vector<V> total = new Vector<V>();
		for(Vector<V> values : super.values()) {		  
		  total.addAll(values);
		}		
		return total;
	}
	
	@Override
	public Iterator<V> iterator() {
		Collection<V> s = getAll();
		return s.iterator();
	}
	
}
