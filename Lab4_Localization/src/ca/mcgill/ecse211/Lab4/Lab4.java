package ca.mcgill.ecse211.Lab4;


import lejos.hardware.Button;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
/**
 * This our main class. 
 * We use it to ask the user what kind of Localization he wan't to chose 
 * and also start the odometer thread.
 * @author Group 21
 *
 */
public class Lab4 {

	// Motor Objects, and Robot related parameters
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	public static final double WHEEL_RAD = 2.2;
	public static final double TRACK = 15.6;
	
	public static Odometer odometer;
	
	
	public static void main(String[] args) throws OdometerExceptions, InterruptedException {

		int buttonChoice;

		// Odometer related objects
		odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); // TODO Complete implementation
		// create the display
		
		// ask the user to chose the localization type and wait for the input
		do {
			// clear the display
			lcd.clear();

		      lcd.drawString("< Left | Right >", 0, 0);
		      lcd.drawString("       |        ", 0, 1);
		      lcd.drawString("Falling|Rissing ", 0, 2);
		      lcd.drawString("Edge   | Edge   ", 0, 3);
		      lcd.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress(); // Record choice (left or right press)
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
		// create odometer thread
		Thread odoThread = new Thread(odometer);
		odoThread.start();
		// create an Ultrasonic Localizer
		UltrasonicLocalizer ultra=new UltrasonicLocalizer(leftMotor, rightMotor, odometer, WHEEL_RAD, TRACK);
		lcd.clear();
		// Run a type of localization depending on the user input
		if (buttonChoice==Button.ID_LEFT) {
			ultra.FallingEdge();
		}else {
			ultra.RisingEdge();
		}
		// wait for the user to click on a button before starting light localization
		buttonChoice = Button.waitForAnyPress();
		odometer.setTheta(0);
		// start light localization
		LightLocalizer light=new LightLocalizer(leftMotor, rightMotor, odometer, WHEEL_RAD, TRACK);
		light.Localize();
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}

