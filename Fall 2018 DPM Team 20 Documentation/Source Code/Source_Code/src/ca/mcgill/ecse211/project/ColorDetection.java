package ca.mcgill.ecse211.project;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * This is the color detection that is used when we are searching for rings.
 * It is mainly used to return the value of ring scanned.
 * 
 * 
 * @author Team 20
 */
public class ColorDetection {
	// Defining our sensor and corresponding fields
	public static Port portColor = LocalEV3.get().getPort("S4");
	public static EV3ColorSensor colorSensor = new EV3ColorSensor(portColor);
	static float[] sampleColor;
	static SampleProvider colorValue;
	// Defining the normalized mean for RGB values for each ring in the following
	// order:
	// Blue, Green, Yellow, Orange
	private static final float[][] Meann = { { 0.186389331f, 0.73661063f, 0.650125985f },
			{ 0.417854802f, 0.8964036f, 0.147844344f }, { 0.84147529f, 0.522908317f, 0.135964066f },
			{ 0.953944461f, 0.282113056f, 0.10199112f } };

	/**
	 * This is the class constructor.
	 * It is used to initiate the color sensor and sample provider.
	 */
	public ColorDetection() {
		colorSensor.setFloodlight(lejos.robotics.Color.WHITE);
		colorValue = colorSensor.getMode("RGB");
		sampleColor = new float[colorValue.sampleSize()];

	}

	/**
	 * This method returns the color detected.
	 * <p>
	 * Blue= 0/ Green=1/ Yellow=2/ Orange=3/ No color detected=4.
	 * @return int: color detected 
	 */
	public int detect() {
		int color;
		color = findMatch(fetch());
		return color;
	}

	/**
	 * This method gets the data from the sample provider.
	 * @return sample color: array with the RGB values
	 */
	public static float[] fetch() {
		colorValue.fetchSample(sampleColor, 0);
		return sampleColor;
	}

	/**
	 * This method return the corresponding color for the RGB values that were
	 * passed to it. If the RGB doesn't correspond to any known colors, it returns 4.
	 * 
	 * @param array:
	 *            RGB values from the sensor
	 * @return index of the color detected
	 */

	public static int findMatch(float array[]) {
		// Standardizing our RGB values
		float euc = (float) Math.sqrt((Math.pow(array[0], 2) + Math.pow(array[1], 2) + Math.pow(array[2], 2)));
		float R = array[0] / euc;
		float G = array[1] / euc;
		float B = array[2] / euc;
		// checking if the RGB value correspond to any color
		float minDist = 20;
		int index = 4;
		// checks for the color with the minimal euclidean distance
		for (int i = 0; i < 4; i++) {

			float distance = (float) Math.sqrt((Math.pow(R - Meann[i][0], 2) + Math.pow(G - Meann[i][1], 2) + Math.pow(B - Meann[i][2], 2)));
			if (distance < minDist) {
				minDist = distance;
				index = i;
			}
		}
		if(index!=4) {
		float differenceR = Math.abs(R - Meann[index][0]) / 0.05f;
		float differenceG = Math.abs(G - Meann[index][1]) / 0.05f;
		float differenceB = Math.abs(B - Meann[index][2]) /0.05f;
		// if a match is found, return the index of the color
		if (differenceR < 1.0 && differenceG < 1.0 && differenceB < 1.0) {
			return index;
		}
		}
		return 4;
	}
}
