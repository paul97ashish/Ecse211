/**
 * This class is meant as a skeleton for the odometer class to be used.
 * 
 * @author Rodrigo Silva
 * @author Dirk Dubois
 * @author Derek Yu
 * @author Karim El-Baba
 * @author Michael Smith
 * 
 * @modified Carl ElKhoury
 * @modified Zakaria Essadaoui
 */


package ca.mcgill.ecse211.odometer;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends OdometerData implements Runnable {

	private OdometerData odoData;
	private static Odometer odo = null; // Returned as singleton

	// Motors and related variables
	private int leftMotorTachoCount;
	private int rightMotorTachoCount;
	private int lastLeftTacho = 0;
	private int lastRightTacho = 0;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	private final double TRACK;
	private final double WHEEL_RAD;

	private static final long ODOMETER_PERIOD = 25; // odometer update period in ms

	/**
	 * This is the default constructor of this class. It initiates all motors and
	 * variables once.It cannot be accessed externally.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param TRACK 
	 * @param WHEEL_RAD
	 * @throws OdometerExceptions
	 */
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, final double TRACK,
			final double WHEEL_RAD) throws OdometerExceptions {
		odoData = OdometerData.getOdometerData(); // Allows access to x,y,z
													// manipulation methods
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

		// Reset the values of x, y and z to 0
		odoData.setXYT(0, 0, 0);

		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;

		this.TRACK = TRACK;
		this.WHEEL_RAD = WHEEL_RAD;

	}

	/**
	 * This method is meant to ensure only one instance of the odometer is used
	 * throughout the code.
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @return new or existing Odometer Object
	 * @throws OdometerExceptions
	 */
	public synchronized static Odometer getOdometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			final double TRACK, final double WHEEL_RAD) throws OdometerExceptions {
		if (odo != null) { // Return existing object
			return odo;
		} else { // create object and return it
			odo = new Odometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
			return odo;
		}
	}

	/**
	 * This class is meant to return the existing Odometer Object. It is meant to be
	 * used only if an odometer object has been created
	 * 
	 * @return error if no previous odometer exists
	 */
	public synchronized static Odometer getOdometer() throws OdometerExceptions {

		if (odo == null) {
			throw new OdometerExceptions("No previous Odometer exits.");

		}
		return odo;
	}

	/**
	 * This method calculates the change in X, Y and Theta and updates them.
	 * This is the same method used in the slides with some changes to the angles units
	 * 
	 */
	// run method (required for Thread)

	public void run() {
		long updateStart, updateEnd;
		double distL, distR, deltaD, deltaT, dX, dY, Theta;
		while (true) {
			updateStart = System.currentTimeMillis();
			// Get new Tacho Count for each motor
			
			leftMotorTachoCount = leftMotor.getTachoCount();
			rightMotorTachoCount = rightMotor.getTachoCount();

			// Calculate the displacement of each wheel
			distL = Math.PI * WHEEL_RAD * (this.leftMotorTachoCount - this.lastLeftTacho) / 180;
			distR = Math.PI * WHEEL_RAD * (this.rightMotorTachoCount - this.lastRightTacho) / 180;

			this.lastLeftTacho = this.leftMotorTachoCount;
			this.lastRightTacho = this.rightMotorTachoCount;
			// Calculate the change in Theta
			deltaD = 0.5 * (distL + distR);
			deltaT = (distL - distR) / TRACK;
			// Calculate the change in X and Y coordinates
			Theta = odo.getXYT()[2] * Math.PI / 180.0;
			Theta += deltaT;
			dX = deltaD * Math.sin(Theta);
			dY = deltaD * Math.cos(Theta);

			// Update the coordinates
			odo.update(dX, dY, deltaT * 180.0 / Math.PI);
			
			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done
				}
			}
		}
	}

}
