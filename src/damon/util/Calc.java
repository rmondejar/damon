package damon.util;

public class Calc {

	  public static int[] max(int[][] data, int size, int types) {
		  
		  int[] maxs = new int[types];
		  for(int i=0;i<types;i++) maxs[i]=Integer.MIN_VALUE;
		  for(int i=0;i<size;i++) {
			  for(int j=0;j<types;j++) {
				  if (maxs[j]<data[i][j]) maxs[j] = data[i][j];  
			  }
		  }
		  return maxs;
	  }
	  
	  public static int[] min(int[][] data, int size, int types) {
		  
		  int[] mins = new int[types];
		  for(int i=0;i<types;i++) mins[i]=Integer.MAX_VALUE;
		  for(int i=0;i<size;i++) {
			  for(int j=0;j<types;j++) {
				  if (mins[j]>data[i][j]) mins[j] = data[i][j];  
			  }
		  }
		  return mins;
	  }
	  
	  public static int[] avg(int[][] data, int size, int types) {
		  
		  long[] avgs = new long[types];		  
		  for(int i=0;i<types;i++) avgs[i]=0;
		  for(int i=0;i<size;i++) {
			  for(int j=0;j<types;j++) {
				  avgs[j] += data[i][j];  
			  }
		  }
		  int[] iavgs = new int[types];
		  for (int i = 0;i<types;i++) iavgs[i] = (int) avgs[i]/size;
		  return iavgs;
	  }
	  
	  public static float[] max(float[][] data, int size, int types) {
		  
		  float[] maxs = new float[types];
		  for(int i=0;i<types;i++) maxs[i]=Integer.MIN_VALUE;
		  for(int i=0;i<size;i++) {
			  for(int j=0;j<types;j++) {
				  if (maxs[j]<data[i][j]) maxs[j] = data[i][j];  
			  }
		  }
		  return maxs;
	  }
	  
	  public static float[] min(float[][] data, int size, int types) {
		  
		  float[] mins = new float[types];
		  for(int i=0;i<types;i++) mins[i]=Integer.MAX_VALUE;
		  for(int i=0;i<size;i++) {
			  for(int j=0;j<types;j++) {
				  if (mins[j]>data[i][j]) mins[j] = data[i][j];  
			  }
		  }
		  return mins;
	  }
	  
	  public static float[] avg(float[][] data, int size, int types) {
		  
		  double[] avgs = new double[types];		  
		  for(int i=0;i<types;i++) avgs[i]=0;
		  for(int i=0;i<size;i++) {
			  for(int j=0;j<types;j++) {
				  avgs[j] += data[i][j];  
			  }
		  }
		  float[] iavgs = new float[types];
		  for (int i = 0;i<types;i++) iavgs[i] = (float) avgs[i]/size;
		  return iavgs;
	  }
}
