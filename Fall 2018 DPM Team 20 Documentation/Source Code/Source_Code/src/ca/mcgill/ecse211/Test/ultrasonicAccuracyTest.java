package ca.mcgill.ecse211.Test;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * We used this class to test the accuracy of the ultrasonic sensors.
 * 
 * @author Group 21
 *
 */
 class ultrasonicAccuracyTest {

	private static final Port usPort = LocalEV3.get().getPort("S2");

	private static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	private static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provimdes samples from
	private static float[] usData = new float[usDistance.sampleSize()];
	private static final int Rotate_Speed = 100;
	private static Odometer odo;
	private static final double TRACK = 14.45;
	private static final double WHEEL_RAD = 2.2;
	private static final int FILTER_OUT = 20;

	public static void main(String[] args) {
		int distance = 0;
		while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
			Button.waitForAnyPress();
			usDistance.fetchSample(usData, 0); // acquire data
			distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
	          System.out.println(distance);
	          try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
