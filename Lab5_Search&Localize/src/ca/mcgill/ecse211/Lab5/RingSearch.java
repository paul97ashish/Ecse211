package ca.mcgill.ecse211.Lab5;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class RingSearch {
	private static final EV3MediumRegulatedMotor SensorMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("D"));
	private static EV3LargeRegulatedMotor leftMotor = Lab5.getLeftMotor();
	private static EV3LargeRegulatedMotor rightMotor = Lab5.getRightMotor();
	public ColorDetection detect;
	private static int distance;

	private static UltrasonicPoller usPoller;
	private static double LastX, LastY;
	private static Navigation navigation;
	private static final int SPEED = 50;
	int ringValue = 5;
	private static double radius = Lab5.WHEEL_RAD;
	boolean notFound = true;

	public RingSearch(UltrasonicPoller poller, Navigation navig) {
		// System.out.println("RingSerch created");
		SensorMotor.setSpeed(100);
		SensorMotor.rotate(-120);
		detect = new ColorDetection();
		usPoller = poller;
		navigation = navig;
	}

	public void look() {
		/*
		 * Last = usPoller.getDistance(); System.out.println(Last);
		 */
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		distance = usPoller.getDistance();
		// System.out.println(distance);
		if (distance < Lab5.rangeDetection && checkRing()) {
			// Lab5.localization.linedetect();
			leftMotor.setSpeed(100);
			rightMotor.setSpeed(100);
			leftMotor.forward();
			rightMotor.forward();
			int newdistance = usPoller.getDistance();
			while ((newdistance - distance) <= 0) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				distance = newdistance;
				newdistance = usPoller.getDistance();
			}

			double TargX = navigation.x;
			double TargY = navigation.y;
			System.out.println("rechecked distance :" + newdistance);
			Lab5.localization.linedetect();
			turnBy(90);
			move(-7);
			Lab5.localization.linedetect();
			LastX = Lab5.odometer.getXYT()[0];
			LastY = Lab5.odometer.getXYT()[1];
		
			
			move(distance-20);
			
			leftMotor.setSpeed(SPEED);
			rightMotor.setSpeed(SPEED);
			ringValue = detect.detect();
			while (ringValue == 5 && Math.abs(LastX- Lab5.odometer.getXYT()[0])<Lab5.rangeDetection &&
					Math.abs(LastY- Lab5.odometer.getXYT()[1])<Lab5.rangeDetection) {
				leftMotor.forward();
				rightMotor.forward();
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ringValue = detect.detect();
			}
			if (ringValue == (Lab5.TR - 1)) {
				Sound.beep();
				navigation.travelTo(Lab5.UUX, Lab5.UUY);
				notFound = false;
			} else {
				Sound.beep();
				Sound.beep();
			}
			if(notFound) {
			leftMotor.setSpeed(200);
			rightMotor.setSpeed(200);
			leftMotor.backward();
			rightMotor.backward();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			leftMotor.setSpeed(1);
			rightMotor.setSpeed(1);
			leftMotor.stop(true);
			rightMotor.stop();
			navigation.travelTo(LastX / Lab5.TILE_SIZE, LastY / Lab5.TILE_SIZE);
			double theta=Lab5.odometer.getXYT()[2];
			// travelling close to the next tile without checking 
			if (theta>225 && theta< 315) { // goind on the first side
				navigation.travelTo((LastX / Lab5.TILE_SIZE), (LastY / Lab5.TILE_SIZE)+0.7);
			}else if(theta<45 || theta> 315) {// second side
				navigation.travelTo((LastX / Lab5.TILE_SIZE)+0.7, (LastY / Lab5.TILE_SIZE));
			}else if(theta > 45 && theta < 135) {// third side
				navigation.travelTo((LastX / Lab5.TILE_SIZE), (LastY / Lab5.TILE_SIZE)-0.7);
			}else if(theta >135 && theta< 225) {// fourth side
				navigation.travelTo((LastX / Lab5.TILE_SIZE)-0.7, (LastY / Lab5.TILE_SIZE));
			}
			navigation.travelTo(TargX, TargY, notFound);
			}

		}

	}
	private int swing() {
		SensorMotor.rotate(10);
		int distance1=usPoller.getDistance();
		SensorMotor.rotate(-10);
		int distance2=usPoller.getDistance();
		SensorMotor.rotate(-10);
		int distance3=usPoller.getDistance();
		SensorMotor.rotate(10);
		if (distance1<distance2 && distance1<distance3) {
			leftMotor.setSpeed(200);
			rightMotor.setSpeed(240);
			leftMotor.forward();
			rightMotor.forward();
			return distance1;
		}else if (distance3<distance2 && distance3<distance1) {
			leftMotor.setSpeed(240);
			rightMotor.setSpeed(200);
			leftMotor.forward();
			rightMotor.forward();
			return distance3;
		}else {
			leftMotor.setSpeed(200);
			rightMotor.setSpeed(200);
			leftMotor.forward();
			rightMotor.forward();
			return distance1;
		}
		
	}
	private void move(double distance) {
		leftMotor.setSpeed(200);
		rightMotor.setSpeed(200);
		leftMotor.rotate(convertDistance(radius, distance), true);
		rightMotor.rotate(convertDistance(radius, distance), false);
	}

	private void turnBy(double theta) {
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		leftMotor.rotate(convertAngle(radius, Lab5.TRACK, theta), true); // turns to the origin point
		rightMotor.rotate(-convertAngle(radius, Lab5.TRACK, theta), false);
	}

	public boolean checkRing() {
		int distance1 = usPoller.getDistance();
		int distance2 = usPoller.getDistance();
		int distance3 = usPoller.getDistance();
		return (distance1 < Lab5.rangeDetection) && (distance2 < Lab5.rangeDetection) && (distance3 < Lab5.rangeDetection);
	}

	public int ringValue() {
		return ringValue + 1;
	}

	private static int convertDistance(double radius, double distance) { // converts distance to wheel rotations
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) { // converts angle to radians for degree
																					// rotation
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
