package ca.mcgill.ecse211.Test;


import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.project.ColorDetection;
import ca.mcgill.ecse211.project.FinalProject;
import ca.mcgill.ecse211.project.Navigation;
import ca.mcgill.ecse211.project.RingGrabbing;
import ca.mcgill.ecse211.project.UltrasonicPoller;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import ca.mcgill.ecse211.odometer.OdometerExceptions;

/**
 *We used this test for grabbing a ring.
 *The robot move forward until it reaches 
 *the tree. Upon reaching the tree, the collecting
 *arm grabs the ring securely onto the robot.
 * @author Ashish
 *
 */
 class GrabingTest {
public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	private static Odometer odometer ; 
	public static Navigation navigation=new Navigation(2.1, 14.75, leftMotor, rightMotor, odometer);
	public static int SensorAngle=50;
	public static void main (String [] args) throws InterruptedException, OdometerExceptions {
		odometer = Odometer.getOdometer(leftMotor, rightMotor, 14.75, 2.1); 
		RingGrabbing grabber=new RingGrabbing(navigation, leftMotor, rightMotor);
		grabber.attempt();
		
	}
}
