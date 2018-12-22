package ca.mcgill.ecse211.Test;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.project.LightPoller;
import ca.mcgill.ecse211.project.Localization;
import ca.mcgill.ecse211.project.Navigation;
import ca.mcgill.ecse211.project.UltrasonicPoller;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
/**
 * We used this test to check for the hardware stability of the robot.
 * The robot uses the move method and travel to method to check it if 
 * it accurately navigate with/without rings on the robot.
 * @author Ashish
 *
 */

public class HardwareStability {

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

		Navigation navigation = new Navigation(WHEEL_RAD, TRACK, leftMotor, rightMotor, odometer);

		lcd.clear();

		lcd.drawString("Waiting ", 0, 0);
		lcd.drawString("For", 0, 1);
		lcd.drawString("Any  ", 0, 2);
		lcd.drawString("Press", 0, 3);
		
		Button.waitForAnyPress(); 
		navigation.move(5*TILE_SIZE);
		Button.waitForAnyPress(); 
		navigation.travelTo(2.4, 2);
		System.exit(0);
	}
	
}
