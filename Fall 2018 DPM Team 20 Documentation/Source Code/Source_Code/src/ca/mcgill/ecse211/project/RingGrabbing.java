package ca.mcgill.ecse211.project;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

/**
 * This class controls the grabbing mechanism and perform the operations needed
 * for grabbing.
 * @author Zakaria Essadaoui
 *
 */

public class RingGrabbing {

	private Navigation navigation;
	public  static EV3MediumRegulatedMotor SensorMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("D"));

	public static EV3MediumRegulatedMotor CollectingMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static ColorDetection detect;
	private static int SensorAngle=60;
	/**
	 * Constructor for the RingGrabbing class. Takes the parameter passed to it and initiate all the fields required.
	 * @param nav : instance of navigation
	 * @param left : left motor of the robot
	 * @param right: right motor of the robot
	 */
	public RingGrabbing(Navigation nav, EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right) {
		navigation = nav;
		leftMotor = left;
		rightMotor = right;

	}

	/**
	 * This method checks for a ring in the side that the robot is facing. 
	 * <p>
	 * It makes the robot go close to the tree, then starts scanning for a ring while making the sensor move 
	 * up and down. If a ring is detected, the robots grab it and beeps according to the ring's color and backs of. 
	 * Otherwise, the robot just backs of. 
	 * @return boolean: true when a ring is detected and grabbed, false otherwise.
	 */
	public boolean attempt() {
		CollectingMotor.setSpeed(100);
		SensorMotor.setSpeed(50);
		detect = new ColorDetection();
		CollectingMotor.rotate(270);
		navigation.move(12);// make the robot go close to the tree.
		int[] colors= {0,0,0,0,0};
		int ring = 4;
		int counter=0;
		// move the sensor up and down and record the colors detected
		while(counter<4){
			if (!SensorMotor.isMoving()) {
				SensorAngle = -SensorAngle;
				SensorMotor.rotate(SensorAngle, true);
				counter++;
			}
			ring = detect.detect();
			colors[ring]++;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// checks for the colors detected, beep accordingly and grab them.
		ring=-1;
		int ring2=-1;
		int index1=-1;
		int index2=-1;
		for (int i=0; i<4; i++) {
			if((colors[i]>=1) &&(colors[i])>ring) {
				ring=colors[i];
				index1=i;
			}else if((colors[i]>=1) &&(colors[i]<=ring) && (colors[i] >ring2)){
				ring2=colors[i];
				index2=i;
			}
			System.out.println(i+ "   "+ colors[i]);
		}
		if(ring==-1)index1=4;
		if (index1!=4) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// navigation.move(2);
			leftMotor.stop(true);
			rightMotor.stop();
			CollectingMotor.rotate(-270);
			for (int i = 0; i <= index1; i++) {
				Sound.beep();

			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i <= index2; i++) {
				Sound.beep();

			}
			navigation.move(-12);// move back to the initial position
			return true;
		}
		CollectingMotor.rotate(-270);
		navigation.move(-12); // move back to the initial position
	
		return false;
	}
}
