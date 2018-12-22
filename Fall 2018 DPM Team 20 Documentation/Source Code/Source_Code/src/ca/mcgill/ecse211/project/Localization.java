package ca.mcgill.ecse211.project;

import ca.mcgill.ecse211.odometer.Odometer;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class is responsible for the ultrasonic and light Localizations. It uses
 * the poller classes to fetch data.
 * 
 * @author Max Brodeur
 * @author Carl ElKhoury
 * @author Zakaria Essadaoui
 *
 */
public class Localization {
	// setting fiels and variables that will be needed
	public static Navigation navigation;
	public static boolean reached = false;
	public static final double distancethr = 35;
	public static double alpha;
	public static double beta;
	private static boolean stopped = false;
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	boolean nextStep = false;
	// setting up the right and left light sensors

	private LightPoller leftLight;
	private LightPoller rightLight;

	private UltrasonicPoller usPoller;
	Odometer odometer;

	public int angle = 45;

	/**
	 * Constructor of the localization class. It initiate all the appropriate fields
	 * with the parameters passed to it.
	 * 
	 * @param odo:
	 *            odometer object
	 * @param nav
	 *            : navigation object
	 * @param left
	 *            : left motor object
	 * @param right
	 *            : right motor object
	 * @param uspol
	 *            : UsPoller object
	 * @param leftLight
	 *            : left light poller object
	 * @param rightLight
	 *            : right light poller object
	 */
	public Localization(Odometer odo, Navigation nav, EV3LargeRegulatedMotor left, EV3LargeRegulatedMotor right,
			UltrasonicPoller uspol, LightPoller leftLight, LightPoller rightLight) {

		this.odometer = odo;
		navigation = nav;
		leftMotor = left;
		rightMotor = right;
		usPoller = uspol;
		this.leftLight = leftLight;
		this.rightLight = rightLight;

	}

	/**
	 * This method performs the initial localization. It first calls the ultrasonic
	 * localization method, then it calls the light localization method and returns.
	 */
	public void InitialLocalization() {

		USLocalization();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		odometer.setTheta(0);
		lightLocalization();
		navigation.turnBy(-90);
	}

	/**
	 * This method is used to perform our light localization. It Makes the robot
	 * move forward until both sensors detect the line turn by 90degress and does
	 * the same in that direction.
	 * 
	 * @return void
	 */
	public void lightLocalization() {

		linedetect_move();
		navigation.turnBy(90);
		linedetect_move();

	}

	/**
	 * This method make the robot detect a line (using line detect) then move
	 * forward by the wheel to sensors offset in order to make the wheels exactly at
	 * the line.
	 * 
	 * @return void
	 */
	public void linedetect_move() {
		linedetect();
		navigation.move(4.8);
	}

	/**
	 * This method first makes the robot move backward, then calls line detection,
	 * then makes the robot move forward in order to make the wheel directly on the
	 * line. It used to make sure we detect a line when we might think that the
	 * sensors are already in front of the lines.
	 * 
	 * @return void
	 */
	public void linedetect_move2() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		navigation.move(-13);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		linedetect();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		navigation.move(4.8);
	}

	/**
	 * This method makes the robot move forward until one sensor detect a black
	 * line. At that point, only the wheel from the other side keep turning until
	 * the it detect a line. It also include mechanisms to deal with extreme cases
	 * where a line is missed.
	 * 
	 * @return void
	 */
	public void linedetect() {
		// make the robot move forward slowly
		leftMotor.setSpeed(150);
		rightMotor.setSpeed(150);
		leftMotor.forward();
		rightMotor.forward();
		// wait for any ring to detect the line
		boolean left_detected = leftLight.line_detected(true);
		boolean right_detected = rightLight.line_detected(true);
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			left_detected = leftLight.line_detected(false);
			right_detected = rightLight.line_detected(false);
			// case where both lines are detected at the same time
			if (left_detected && right_detected) {
				leftMotor.setSpeed(1);
				rightMotor.setSpeed(1);
				leftMotor.stop(true);
				rightMotor.stop();
				break;
			}
			// case where the left sensor detect a line first
			if (left_detected) {
				leftMotor.setSpeed(1);
				leftMotor.stop(true);
				rightMotor.setSpeed(50);
				rightMotor.forward();
				// checking for a line detection or an false reading
				double lastTheta = odometer.getXYT()[2];
				boolean wrap = false;
				if (lastTheta <= angle) {
					wrap = true;
				}
				double currentTheta = lastTheta;
				while (!right_detected && Math.abs(lastTheta - currentTheta) <= angle) {
					try {
						Thread.sleep(75);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					right_detected = rightLight.line_detected(false);
					currentTheta = odometer.getXYT()[2];
					if (wrap && currentTheta > (300 - angle)) {
						currentTheta -= 360;
					}
				}
				rightMotor.setSpeed(1);
				rightMotor.stop();
				// getting the difference between the two angles
				double dtheta = lastTheta - currentTheta;
				if (Math.abs(dtheta) >= angle) {
					if (Math.abs(dtheta) > 360) {
						dtheta %= 360;
					}
					if (dtheta > 180) {
						dtheta -= 360;
					} else if (dtheta < -180) {
						dtheta += 360;
					}
					navigation.turnBy(dtheta);
					navigation.move(-10);
					linedetect();
				}

				break;

			}
			// case where the right sesnor detects the line first
			if (right_detected) {

				rightMotor.setSpeed(1);
				rightMotor.stop(true);
				leftMotor.setSpeed(50);
				leftMotor.forward();
				double lastTheta = odometer.getXYT()[2];
				boolean wrap = false;
				if (lastTheta >= (360 - angle)) {
					wrap = true;
					lastTheta -= 360;
				}
				double currentTheta = lastTheta;
				while (!left_detected && Math.abs(lastTheta - currentTheta) <= angle) {
					try {
						Thread.sleep(75);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					left_detected = leftLight.line_detected(false);
					currentTheta = odometer.getXYT()[2];
					if (wrap && currentTheta >= (360 - angle)) {
						currentTheta -= 360;
					}
				}
				leftMotor.setSpeed(1);
				leftMotor.stop();
				double dtheta = lastTheta - currentTheta;
				if (Math.abs(dtheta) >= angle) {
					if (Math.abs(dtheta) > 360) {
						dtheta %= 360;
					}
					navigation.turnBy(dtheta);
					navigation.move(-8);
					linedetect();
				}
				break;
			}

		}
	}

	/**
	 * This method performs the Ultrasonic localization using a falling edge
	 * detection.
	 * <p>
	 * It makes the robot turn clockwise until a falling edge is detected, record
	 * the odometer's angle, switch direction then detects a second falling edge and
	 * record the odometer's angle. Then, it uses those two angles to turn to the 0°
	 * direction.
	 */

	public void USLocalization() {
		int lastDistance;
		int distance;
		navigation.turn360(true);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
		}
		distance = usPoller.getDistance();
		// unsure we are not facing a wall
		while (distance != 255) {
			distance = usPoller.getDistance();
		}
		while (!stopped) {
			if (!leftMotor.isMoving() && !rightMotor.isMoving() && !reached) {
				navigation.turn360(true);
			}
			if (!leftMotor.isMoving() && !rightMotor.isMoving() && reached) {
				navigation.turn360(false);
			}
			lastDistance = usPoller.getDistance();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			distance = usPoller.getDistance();
			if ((lastDistance - distance) > distancethr && distance < 60) { // triggered if falling edge is detected
				if (!reached) { // if its the first falling edge detected
					reached = true;
					alpha = odometer.getXYT()[2]; // store the first angle
					leftMotor.setSpeed(1); // slow the motors to reduce error when stopping
					rightMotor.setSpeed(1);
					leftMotor.stop();
					rightMotor.stop();
					Sound.beep();
					navigation.turn360(false);
					try {
						Thread.sleep(1000); // one second pause to stop sensor from returning false positive.
					} catch (InterruptedException e) {
					}
				} else {
					if (!leftMotor.isMoving() && !rightMotor.isMoving()) {
						navigation.turn360(false);
					}
					beta = odometer.getXYT()[2]; // if its not the first falling edge detected store the angle
					leftMotor.setSpeed(1);
					rightMotor.setSpeed(1);
					leftMotor.stop();
					rightMotor.stop();
					stopped = true; // set boolean to true to ensure to it doesnt run this sequence again.
					Sound.beep();
				}
			}
		}

		if (alpha > beta) {
			double current[] = odometer.getXYT();
			odometer.setTheta(45 - (alpha + beta) / 2 + current[2]); // sets the new corrected theta.

		} else {
			double current[] = odometer.getXYT();
			odometer.setTheta(225 - (alpha + beta) / 2 + current[2]); // also sets the new corrected if the first
																		// angle measured is less than the second.
		}

		navigation.turnTo(0); // turn back to 0 degrees/

	}

}
