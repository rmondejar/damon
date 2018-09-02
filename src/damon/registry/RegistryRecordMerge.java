package damon.registry;

import bunshin.listeners.BunshinMergeClient;

public class RegistryRecordMerge implements BunshinMergeClient {

	public Object merge(Object oldObj, Object newObj) {
			
		if (oldObj instanceof RegistryRecord && newObj instanceof RegistryRecord) {
			
			RegistryRecord oldRR = (RegistryRecord) oldObj;			
			RegistryRecord newRR = (RegistryRecord) newObj;			
			
			RegistryRecord finalRR = (RegistryRecord) newRR.clone();
			finalRR.merge(oldRR);			
			return finalRR;
			
		}
	       
		return newObj;
		
	}

}
