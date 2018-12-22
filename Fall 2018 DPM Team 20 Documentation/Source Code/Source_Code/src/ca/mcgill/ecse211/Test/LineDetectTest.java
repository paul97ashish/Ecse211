package ca.mcgill.ecse211.Test;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.project.LightPoller;
import ca.mcgill.ecse211.project.Localization;
import ca.mcgill.ecse211.project.Navigation;
import ca.mcgill.ecse211.project.UltrasonicPoller;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * We used this test to check whether line detect works properly.
 * The robot moves forward and tries to detects the lines using 2 light sensors.
 * @author Ashish
 *
 */
 class LineDetectTest {

	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RAD = 2.1;
	public static final double TRACK = 14.7;
	public static LightPoller LeftLightPoller;
	public static LightPoller RightLightPoller;
	public static final double TILE_SIZE = 30.48;
	public static Navigation navigation;

	public static Localization localization;
	public static UltrasonicPoller usPoller;

	public static void main(String[] args) throws OdometerExceptions {
    
		int buttonChoice;

		// Odometer related objects
		Odometer odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);

		Navigation navigation = new Navigation(WHEEL_RAD,TRACK,leftMotor, rightMotor,odometer);
		Thread odoThread = new Thread(odometer);
		odoThread.start();

		usPoller = new UltrasonicPoller();
		
		LeftLightPoller=new LightPoller(LocalEV3.get().getPort("S1"));
		RightLightPoller=new LightPoller(LocalEV3.get().getPort("S3"));
		// start our localization routine
		localization = new Localization(odometer, navigation, leftMotor, rightMotor, usPoller, LeftLightPoller, RightLightPoller );
		lcd.clear();
		localization.angle=30;
	
		Button.waitForAnyPress(); // Record choice (left or right press)
		while(true) {
		localization.linedetect_move();
		localization.linedetect_move();
		localization.linedetect_move();
		localization.linedetect_move();
		localization.linedetect_move();
		localization.linedetect_move();
		localization.linedetect_move();
		}

	}
	/**
	 * This rounding method get the values from the odometer and return the closest
	 * grid intersection and orientation This is done after performing light
	 * localization
	 * 
	 * @param x
	 * @param y
	 * @param theta
	 * @return real coordonates of the robot
	 */
	public static double[] round(double x, double y, double theta) {
		double X = x / TILE_SIZE;
		double Y = y / TILE_SIZE;
		// check for closest X
		double errorx = X - (int) X;
		if (errorx >= 0.5) {
			X = (int) X + 1;
		} else {
			X = (int) X;
		}
		// checks for closest Y
		double errory = Y - (int) Y;
		if (errory >= 0.5) {
			Y = (int) Y + 1;
		} else {
			Y = (int) Y;
		}
		// check for closest theta
		double ret = 0;
		if (theta > 225 && theta < 315) { // goind on the first side
			ret = 270;
		} else if (theta < 45 || theta > 315) {// second side
			ret = 0;
		} else if (theta > 45 && theta < 135) {// third side
			ret = 90;
		} else if (theta > 135 && theta < 225) {// fourth side
			ret = 180;
		}
		return new double[] { X * TILE_SIZE, Y * TILE_SIZE, ret };

	}
}
