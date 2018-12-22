package ca.mcgill.ecse211.Test;

import ca.mcgill.ecse211.project.ColorDetection;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

 class Test2 {
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static final EV3LargeRegulatedMotor sensorMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	
	
	public static void main(String[] args) throws InterruptedException {
		ColorDetection Color=new ColorDetection();
		Button.waitForAnyPress();
		sensorMotor.setSpeed(50);
		sensorMotor.forward();
		int detected=Color.detect();
		while (detected==5) {
			detected=Color.detect();
		}
		Sound.beep();
		Thread.sleep(20);
		
		leftMotor.setSpeed(200);
		rightMotor.setSpeed(200);
		leftMotor.backward();
		rightMotor.backward();
		Thread.sleep(1000);
		leftMotor.stop();
		rightMotor.stop();
		sensorMotor.setSpeed(200);
		sensorMotor.forward();
		Thread.sleep(500);
		sensorMotor.stop();
	}
	
}
