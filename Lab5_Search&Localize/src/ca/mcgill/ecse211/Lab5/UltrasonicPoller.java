package ca.mcgill.ecse211.Lab5;

import lejos.robotics.SampleProvider;

/**
 * Control of the wall follower is applied periodically by the UltrasonicPoller thread. The while
 * loop at the bottom executes in a loop. Assuming that the us.fetchSample, and cont.processUSData
 * methods operate in about 20mS, and that the thread sleeps for 50 mS at the end of each loop, then
 * one cycle through the loop is approximately 70 mS. This corresponds to a sampling rate of 1/70mS
 * or about 14 Hz.
 */
public class UltrasonicPoller  {
  private SampleProvider us;
  private float[] usData;
  
  private static final int FILTER_OUT = 20;
  private int filterControl;
  
  private int distance;
  public UltrasonicPoller(SampleProvider us, float[] usData) {
    this.us = us;
  
    this.usData = usData;
  }

  /*
   * Sensors now return floats using a uniform protocol. Need to convert US result to an integer
   * [0,255] (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  
  public int getDistance() {
    int distance;
    us.fetchSample(usData, 0); // acquire data
     distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
     while (process(distance)==300) {
    	 us.fetchSample(usData, 0); // acquire data
         distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
     }
     return this.distance;
  }
  
  public int process(int distance) {
	  if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
			return 300;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = 255;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}
	  return this.distance;
  }
}
