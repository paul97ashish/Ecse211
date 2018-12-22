package ca.mcgill.ecse211.navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class ObstacleAvoidance extends Thread {
	// creating the motor sensor
	public static final EV3MediumRegulatedMotor sensorMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("D"));
	// creating the sensor
	private static final Port usPort = LocalEV3.get().getPort("S1");
	private static SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
	private static SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
	// this instance
	private static float[] usData = new float[usDistance.sampleSize()];
	// varibales
	private static final int SENSOR_SPEED = 40;
	private static final int ROTATE_SPEED=100;
	private static final int ANGLE=5;
	private static final int FWDSPEED=250;
	private int distance;
	private int safeDistance;

	public ObstacleAvoidance(int safeDistance) {
		this.safeDistance = safeDistance;
	}

	public void run() {

		while (true) {
			usSensor.fetchSample(usData, 0); // acquire data
			distance = (int) (usData[0] * 100.0); // extract from buffer, cast to int
			if (this.distance < this.safeDistance) {
				Lab3.nav.suspend();
				Lab3.leftMotor.stop();
				Lab3.rightMotor.stop();
				Avoid();
				Lab3.nav.resume();
			}
			

			try {
				Thread.sleep(50);
			} catch (Exception e) {
			} // Poor man's timed sampling
		}
	}

	public void Avoid() {
		int rightAngle, leftAngle;
		sensorMotor.resetTachoCount();
		int Tacho=sensorMotor.getTachoCount();
		int currentDistance=0;
		sensorMotor.setSpeed(SENSOR_SPEED);
		while(currentDistance<30) {
			
			sensorMotor.rotate(ANGLE, false);
			usSensor.fetchSample(usData, 0); // acquire data
			currentDistance = (int) (usData[0] * 100.0);
		}
		sensorMotor.stop();
		rightAngle=sensorMotor.getTachoCount()-Tacho;
		
		sensorMotor.rotate(-rightAngle, false);
		usSensor.fetchSample(usData, 0); // acquire data
		currentDistance = (int) (usData[0] * 100.0);
		while(currentDistance<30) {
			sensorMotor.rotate(ANGLE, false);
			usSensor.fetchSample(usData, 0); // acquire data
			currentDistance = (int) (usData[0] * 100.0);
			
		}
		
		leftAngle=Tacho-rightAngle;
		double travel_dist=0.0;
		if(leftAngle<rightAngle) {
			travel_dist=this.distance/Math.cos(leftAngle);
			Lab3.leftMotor.setSpeed(ROTATE_SPEED);
			Lab3.rightMotor.setSpeed(ROTATE_SPEED);
			Lab3.leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, leftAngle), true);
			Lab3.rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, leftAngle), false);
		}else {
			travel_dist=this.distance/Math.cos(rightAngle);
			Lab3.leftMotor.setSpeed(ROTATE_SPEED);
			Lab3.rightMotor.setSpeed(ROTATE_SPEED);
			Lab3.leftMotor.rotate(convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, rightAngle), true);
			Lab3.rightMotor.rotate(-convertAngle(Lab3.WHEEL_RAD, Lab3.TRACK, rightAngle), false);
		}
		Lab3.leftMotor.setSpeed(FWDSPEED);
		Lab3.rightMotor.setSpeed(FWDSPEED);
		
		Lab3.leftMotor.rotate(convertDistance(Lab3.WHEEL_RAD,travel_dist),true);
		
		Lab3.rightMotor.rotate(convertDistance(Lab3.WHEEL_RAD,travel_dist),false);
	}
	
	
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
