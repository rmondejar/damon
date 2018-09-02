package damon.reflection;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import damon.annotation.Type;
import damon.util.collections.SetHashtable;

/**
 * Type -> AspectName -> Id -> Set (Method + Target + Ack)
 *  
 */
public class RemoteMetaPointcuts {
	
	private static final long serialVersionUID = 7573790554430089922L;
	
	private Hashtable<String, SetHashtable<String, Object[]>> befores = new Hashtable<String, SetHashtable<String, Object[]>>();
	private Hashtable<String, SetHashtable<String, Object[]>> afters = new Hashtable<String, SetHashtable<String, Object[]>>();
	private Hashtable<String, SetHashtable<String, Object[]>> arounds = new Hashtable<String, SetHashtable<String, Object[]>>();
	
	private Hashtable<String, SetHashtable<String, Object[]>> getRef(Type type) {
		
		switch(type) {
		  case BEFORE : return befores;
		  case AFTER : return afters;
		  case AROUND : return arounds;
		}
		return befores;		
	}
	
	public void add(String aspectName, Method method, String id, String target,	Type type, boolean ack) {
	
		Hashtable<String, SetHashtable<String, Object[]>> info = getRef(type);
		
		//AspectName -> Id -> Set (Method + Target + Ack)		
		SetHashtable<String, Object[]> rmpcs = null;
		if (info.containsKey(aspectName)) {
			//Id -> Set (Method + Target + Ack)
			rmpcs = info.get(aspectName);			
		}			
		else {
			rmpcs = new SetHashtable<String,Object[]>();
		}
		rmpcs.put(id,new Object[]{method,target,ack});
		info.put(aspectName, rmpcs); // name --> id --> type --> methods		
	}
	
	public Collection<Object[]> get(String aspectName, String id, Type type) {
				
		Hashtable<String, SetHashtable<String, Object[]>> info = getRef(type);
		
		//AspectName -> Id -> Set (Method + Target + Ack)		
		SetHashtable<String, Object[]> rmpcs = null;
		if (info.containsKey(aspectName)) {
			//Id -> Set (Method + Target + Ack)
			rmpcs = info.get(aspectName);
			Set<Object[]> set = rmpcs.get(id);
			return set;
		}		
		
		return new HashSet<Object[]>();
	}
	
	public void remove(String aspectName) {
		befores.remove(aspectName);
		afters.remove(aspectName);
		arounds.remove(aspectName);		
	}

	

}
