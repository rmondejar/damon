package damon.aspectwerkz;

import java.util.Hashtable;
import java.util.Vector;

public class AnnotationInfo {
	
	//pointcutData = {method,pointcut}
	private Hashtable<String,String> befores = new Hashtable<String,String>();
	private Hashtable<String,String> afters = new Hashtable<String,String>();
	private Hashtable<String,String> arounds = new Hashtable<String,String>();
	
	//timer method = seconds
	private Hashtable<String,Integer> pulses = new Hashtable<String,Integer>();
	
	private void insertAdvice(Hashtable<String,Vector<String[]>> advices, String method, String[] pair) {
		Vector<String[]> pairs = advices.get(method);
		if (pairs==null) pairs = new Vector<String[]>();
		pairs.add(pair);
		advices.put(method, pairs);
	}

	public Hashtable<String, Vector<String[]>> getAdvices() {
		
		Hashtable<String,Vector<String[]>> advices = new Hashtable<String,Vector<String[]>>();    	
       		    								
      	for (String methodName : befores.keySet()) {      		
      		String pointcut = befores.get(methodName);
       		String[] pair = new String[]{"before",pointcut};       		
			insertAdvice(advices,methodName,pair);	
		}
      	
      	for (String methodName : afters.keySet()) {
      		String pointcut = afters.get(methodName);
       		String[] pair = new String[]{"after",pointcut};       		
			insertAdvice(advices,methodName,pair);	
		}
      	
      	for (String methodName : arounds.keySet()) {      		
      		String pointcut = arounds.get(methodName);
       		String[] pair = new String[]{"around",pointcut};       		
			insertAdvice(advices,methodName,pair);	
		}
      	
      	for (String methodName : pulses.keySet()) {      		
      		int seconds = pulses.get(methodName);
      		String pointcut = "execution(* damon.timer.DamonTimer.seconds"+seconds+"(..))";
       		String[] pair = new String[]{"before",pointcut};       		
			insertAdvice(advices,methodName,pair);	
		}


        return advices;		
	}

	public void putBefore(String method, String pointcut) {
		befores.put(method,pointcut);		
	}

	public void putAfter(String method, String pointcut) {
		afters.put(method,pointcut);		
	}

	public void putAround(String method, String pointcut) {
		arounds.put(method,pointcut);		
	}

	public void putPulse(String method, int seconds) {
		pulses.put(method,seconds);
		
	}
	

}
