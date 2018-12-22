/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometer;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class OdometryCorrection implements Runnable {
	private static final long CORRECTION_PERIOD = 10;
	private Odometer odometer;
	// Value at which we now we crossed a black line
	private static final double THRESHOLD = 500.0;
	// Distance between the sensor and the center of the wheels
	private static final double offset=1.2;
	private double data;
	private static final double TILE_SIZE = 30.48;
	private static Port portColor = LocalEV3.get().getPort("S1");

	private static SensorModes myColor = new EV3ColorSensor(portColor);

	private static SampleProvider myColorSample = myColor.getMode("Red");

	// Need to allocate buffers for each sensor
	private static float[] sampleColor = new float[myColor.sampleSize()];

	/**
	 * This is the default class constructor. An existing instance of the odometer
	 * is used. This is to ensure thread safety.
	 * 
	 * @throws OdometerExceptions
	 */
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;

	}

	/**
	 * In here we verify if we crossed a black line
	 * Depending on our direction and the numbers of lines crossed
	 * We change the values of X and Y
	 * @throws OdometerExceptions
	 * @ author Group 21
	 */
	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		int Ylines = 0, Xlines = 0; // Counter for the number of lines crossed in each direction

		while (true) {
			correctionStart = System.currentTimeMillis();

			// TODO Trigger correction (When do I have information to correct?)
			myColorSample.fetchSample(sampleColor, 0);
			data = sampleColor[0] * 1000.0;
			// crossed a line
			if (data < THRESHOLD) {
				Sound.beep();
				double[] position = this.odometer.getXYT();

				if (position[2] < 10 || position[2] > 350) { // Going North

					this.odometer.setY(TILE_SIZE * Ylines - offset);
					Ylines++;

				} else if (position[2] > 80 && position[2] < 100) {// Going East
					this.odometer.setX(TILE_SIZE * Xlines - offset);
					Xlines++;

				} else if (position[2] > 170 && position[2] < 190) {// Going South
					Ylines--;
					this.odometer.setY(TILE_SIZE * Ylines + offset);

				} else if (position[2] > 260 && position[2] < 280) {// Going West
					Xlines--;
					this.odometer.setX(TILE_SIZE * Xlines + offset);

				}
			}

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here
				}
			}
		}
	}
}
