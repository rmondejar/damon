package damon.metalevel;

import damon.annotation.Type;
import damon.invokation.AspectInvokation;

public class AspectMetaInvokation {
	
	private AspectInvokation ai;
	private Type type;

	public AspectMetaInvokation(AspectInvokation ai, Type type) {
		this.ai = ai;
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public AspectInvokation getAspectInvokation() {
		return ai;
	}

	public void modifyArgs() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Modification of arguments and/or result. The first
	 * arg is the RemoteJoinPoint, and if it is ack, the
	 * last is the result.
	 * @param args
	 */
	public void modify(Object[] args, Object result) {
		int length = args.length;// - 1;		
		for (int i=0;i<length;i++) {
			ai.setArg(i, args[i]);
		}
		if (ai.isAck()) {			
			ai.setResult(result);			
		}
		
		
	}

}
