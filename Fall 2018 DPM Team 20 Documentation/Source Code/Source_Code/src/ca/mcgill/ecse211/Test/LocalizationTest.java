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
 * We used this test to verify whether the robot is 
 * able to localize properly using the localization methods, 
 * ultrasonic localization and light localization.
 * @author Ashish
 *
 */

 class LocalizationTest {

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

		lcd.drawString("Waiting ", 0, 0);
		lcd.drawString("For", 0, 1);
		lcd.drawString("Any  ", 0, 2);
		lcd.drawString("Press", 0, 3);
	
		Button.waitForAnyPress(); // Record choice (left or right press)
		

		// Start odometer and display threads
		
		localization.InitialLocalization();
		odometer.setXYT(7*TILE_SIZE, 1*TILE_SIZE, 270);
		Sound.beep();
		// stop the program after localization
	//	Button.waitForAnyPress();
		navigation.travelTo(3, 2); // traveling next to the bridge
		/*
		 * We make the robot go to the middle of the line in front of the ring
		 */
		navigation.turnTo(270*Math.PI/180);
		localization.linedetect();
		double[] temp=round(odometer.getXYT()[0],odometer.getXYT()[1],odometer.getXYT()[2]);

		System.out.println(odometer.getXYT()[0]+ "   " + odometer.getXYT()[1] + "   " +odometer.getXYT()[2]);
		System.out.println(temp[0]+ "   " + temp[1] + "   " +temp[2]);

//		navigation.move((TILE_SIZE / 2)-4.5);
		navigation.travelTo(2.4, 2);
		navigation.turnBy(90);
		localization.linedetect();
		
		localization.linedetect();
		odometer.setXYT(2.4*TILE_SIZE, 3*TILE_SIZE, 0);
		navigation.travelTo(2.4,5.8);
//		localization.linedetect();
//		Button.waitForAnyPress();
//		navigation.move(TILE_SIZE*3 -4);
		localization.lightLocalization();
		temp=round(odometer.getXYT()[0],odometer.getXYT()[1],odometer.getXYT()[2]);
		System.out.println(odometer.getXYT()[0]+ "   " + odometer.getXYT()[1] + "   " +odometer.getXYT()[2]);
		System.out.println(temp[0]+ "   " + temp[1] + "   " +temp[2]);
		lcd.clear();
		
		lcd.drawString("DONE ", 0, 0);
		Button.waitForAnyPress();
		System.exit(0);

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
