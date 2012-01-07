package com.starredsolutions.utils;

import java.util.HashMap;

public class MyTimer {
	// WARNING: HashMap is not thread-safe (not syncronized)
	static private HashMap<String, MyTimer> timers = new HashMap<String, MyTimer>();
	
	private String name = null;

	private long startedAt;
	private long elapsedTimeInMS = 0;
	
	
	static public MyTimer get(String name) {
		MyTimer timer;
		if (timers.containsKey(name)) {
			timer = timers.get(name);
		} else {
			timer = new MyTimer(name);
			timers.put(name, timer);
		}
		return timer;
	}
	
	
	/**
	 * If given a name the timer will be save in the list
	 * @param name
	 * @return
	 */
	static public MyTimer start(String name) {
		MyTimer timer = get(name);
		timer.start();
		return timer;
	}
	
	static public MyTimer resume(String name) {
		MyTimer timer = get(name);
		timer.resume();
		return timer;
	}
	
	static public MyTimer stop(String name) {
		MyTimer timer = get(name);
		timer.stop();
		return timer;
	}
	
	static public MyTimer remove(String name) {
		return timers.remove(name);
	}

	
	// Private constructor prevents instantiation
	private MyTimer(String name) {
		this.name = name;
	}
	
	public void start() {
		this.elapsedTimeInMS = 0;
		this.startedAt = System.currentTimeMillis();
	}
	
	public void resume() {
		this.startedAt = System.currentTimeMillis();
	}
	
	/**
	 * @return elapsedTimeInMS
	 */
	public long stop() {
		this.elapsedTimeInMS += (System.currentTimeMillis() - this.startedAt);
		return this.elapsedTimeInMS;
	}
	
	public long ms() {
		return this.elapsedTimeInMS;
	}
	
	public float seconds() {
		return this.elapsedTimeInMS / 1000F;
	}
	
	public String name() { return this.name; }
	
	/**
	 * Adds the value of another timer to this one
	 * @param otherTimer
	 */
	public MyTimer add(MyTimer otherTimer) {
		this.elapsedTimeInMS += otherTimer.ms();
		return this;
	}
	
	public MyTimer add(String name) {
		this.elapsedTimeInMS += get(name).ms();
		return this;
	}
	
	public String toString() {
		return Float.toString(seconds()) + "s";
	}
}
