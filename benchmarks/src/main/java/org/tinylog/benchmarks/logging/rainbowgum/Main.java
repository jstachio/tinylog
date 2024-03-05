package org.tinylog.benchmarks.logging.rainbowgum;

public class Main {
	
	public static void main(
			String[] args) {
		LifeCycle lc = new LifeCycle();
		lc.async = true;
		try {
			lc.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
