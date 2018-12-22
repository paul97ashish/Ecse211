package ca.mcgill.ecse211.project;

import lejos.hardware.Device;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * This class is used to get data from the ultrasonic sensor.
 * 
 * @author Zakaria Essadaoui
 */
public class UltrasonicPoller {
	// intiate the ultrasonic sensor and the sample provider
	private static final Port usPort = LocalEV3.get().getPort("S2");
	private static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	private static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
	// this instance
	private static float[] usData = new float[usDistance.sampleSize()];
	private static final int FILTER_OUT = 20;
	private int filterControl;

	private int distance;

	/**
	 * This method return the distance from the ultrasonic sensor.
	 * The range of distances is [0,255].
	 * 
	 * @return distance
	 */

	public int getDistance() {
		int distance;
		usDistance.fetchSample(usData, 0); // acquire data
		distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
		while (process(distance) == 300) {
			usDistance.fetchSample(usData, 0); // acquire data
			distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
		}
		
		return this.distance;
	}
	public void close() {
		((Device) usSensor).close();
	}

	/**
	 * This is our filter for making sure the distance detected is bigger than 255,
	 * i.e. no object in front of the sensor.
	 * 
	 * @param distance:
	 *            distance to process
	 * @return integer: processed distance
	 */
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
