package damon.timer;

import java.util.Timer;
import java.util.TimerTask;

public class DamonTimer {

	private Timer timer = new Timer();
	
	private int pulse;
	
	public void start() {
		timer.schedule(new TimerTask() {
			
			public void run() 	{
			  seconds1();
		      if (pulse%2==0) seconds2();
		      if (pulse%5==0) seconds5();
		      if (pulse%11==0) seconds11();
		      if (pulse%19==0) seconds19();
			
			  pulse= (pulse+1)%20;			  
			}
			
		},0,1000);	
	}
	
	public void seconds1() {}
	
	public void seconds2() {}
	
	public void seconds5() {}
	
	public void seconds11() {}
	
	public void seconds19() {}

}
