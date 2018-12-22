package ca.mcgill.ecse211.navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Test {
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	private static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
	// this instance
	private static float[] usData = new float[usDistance.sampleSize()];
	private static int distance;
	public static final EV3MediumRegulatedMotor sensorMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("D"));
	
	public static void main(String args[]) {
		int angle=10;
		while (true) {
		//	sensorMotor.setAcceleration(100);
			sensorMotor.setSpeed(50);
			sensorMotor.rotate(angle);
			usSensor.fetchSample(usData, 0); // acquire data
			distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
			if(distance>30) {
				sensorMotor.stop();
				angle=-angle;
			}
			System.out.println(distance);
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			} // Poor man's timed sampling

		}

	}
}
