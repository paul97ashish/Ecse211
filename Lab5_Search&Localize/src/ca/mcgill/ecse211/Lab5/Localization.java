package ca.mcgill.ecse211.Lab5;

import ca.mcgill.ecse211.Lab5.Lab5;
import ca.mcgill.ecse211.Lab5.Navigation;
import ca.mcgill.ecse211.odometer.OdometerData;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.Gyroscope;
import lejos.robotics.SampleProvider;

public class Localization{
	public static Navigation navigation = Lab5.navigation;
	public static boolean reached=false;
	public static boolean fallingEdge;
	public static final double distancethr=35;
	public static double alpha;
	public static double beta;
	private static boolean stopped =false; 
	private static EV3LargeRegulatedMotor leftMotor = Lab5.getLeftMotor();
	private static EV3LargeRegulatedMotor rightMotor = Lab5.getRightMotor();
//	private static OdometerData odo;
	boolean nextStep=false;
	private static final EV3ColorSensor colorSensorR=new EV3ColorSensor(LocalEV3.get().getPort("S2"));
	SampleProvider lightSensorR=colorSensorR.getMode("Red");
	float [] lightDataR=new float [lightSensorR.sampleSize()];
	private static final EV3ColorSensor colorSensorL=new EV3ColorSensor(LocalEV3.get().getPort("S3"));
	SampleProvider lightSensorL=colorSensorL.getMode("Red");
	float [] lightDataL=new float [lightSensorL.sampleSize()];
	private double currentPosition[];
	private double radius = Lab5.getRadius();
	private double track = Lab5.getTrack();
	private double array[] = new double[4];
	
	private static final double colorthr=0.155;
	
	public Localization(boolean fallingEdge){
		this.fallingEdge=fallingEdge; //  records if the user inputed falling or rising edge detection
	}
	
	/**This method combines calculations from the odometry and navigation class with readings from an ultrasonic sensor and light sensor in order to correctly
	 * position itself at the relative grid origin no matter the start position. It first uses the US to orient itself facing 0 degrees and next moves forward 
	 * and performs a 360 degree turn while detecting four instances of grid lines and storing the current theta when detected. Those stored angles are then 
	 * used to accurately locate the current position of the robot and move to the origin.**/

	public void run(){
		int count =0;
		angleLocalization();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lightLoc();
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		leftMotor.rotate(-convertAngle(radius, track , 90),true);		//turns to the origin point	
		rightMotor.rotate(convertAngle(radius, track , 90),false);
		/*leftMotor.rotate(300,true);
		rightMotor.rotate(300,false);
		
		
		navigation.turn360(true);*/
//		while(nextStep) {
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			lightSensor.fetchSample(lightData, 0);
//
//			if(lightData[0]<0.30) {				//set the threshold for the line detection 
//				Sound.beep();
//				if(count !=4)
//					array[count++]=Lab5.odometer.getXYT()[2];	//storing the angle it was detected at.
//			}
//			
//			if(count == 4) {
//				try {
//					Thread.sleep(1500);
//				} catch (InterruptedException e) {
//				}
//				double deltaTx=array[1]-array[3];				//delta angles at second and fourth detected lines
//				double d= 14.0;
//				double y = -d*Math.cos(deltaTx*Math.PI/360);	//calculation of y position
//				double deltaTy=array[0]-array[2];				//delta angles at first and third detected lines
//				double x = -d*Math.cos(deltaTy*Math.PI/360);	//calculation of x position
//				double theta = Math.atan(x/y)*180/Math.PI;		
//				double distance = Math.sqrt(x*x+y*y);
//				double correctionAngle = array[0]-(deltaTy/2)-270;		
////				System.out.println("x= "+x+" y= "+y+" correction= "+(correctionAngle));
//				Sound.beep();
//				leftMotor.rotate(-convertAngle(radius, track , theta+correctionAngle),true);		//turns to the origin point	
//				rightMotor.rotate(convertAngle(radius, track , theta+correctionAngle),false);
//				leftMotor.rotate(convertDistance(radius, distance),true);							//moves to the point
//				rightMotor.rotate(convertDistance(radius, distance),false);	
//			//	Lab5.odometer.setTheta(theta);
//				leftMotor.rotate(-convertAngle(radius, track , theta+110),true);		//turns to the origin point	
//				rightMotor.rotate(convertAngle(radius, track , theta+110),false);
//			//	navigation.turnTo(0);																//turn back forward
//				count++;
//				gyro.reset();
//				break;
//				
//			}
			
					
		}
	
	public void lightLoc() {
		linedetect();
		leftMotor.setSpeed(100);
		rightMotor.setSpeed(100);
		leftMotor.rotate(convertAngle(radius, track , 90),true);		//turns to the origin point	
		rightMotor.rotate(-convertAngle(radius, track , 90),false);
		
		linedetect();
		
		
		
	}
	public void linedetect() {
		leftMotor.setSpeed(50);
		rightMotor.setSpeed(50);
		leftMotor.forward();
		rightMotor.forward();
		while (true) {
			lightSensorL.fetchSample(lightDataL, 0);
			lightSensorR.fetchSample(lightDataR, 0);
			if(lightDataL[0]<colorthr) {
				Sound.beep();
				leftMotor.setSpeed(1);
				leftMotor.stop();
				lightSensorR.fetchSample(lightDataR, 0);
				double lastTheta=Lab5.odometer.getXYT()[2];
				double theta=Lab5.odometer.getXYT()[2];
				while(lightDataR[0]>colorthr && Math.abs(theta-lastTheta)<30) {
					lightSensorR.fetchSample(lightDataR, 0);
					theta=Lab5.odometer.getXYT()[2];
				}
				Sound.beep();
				rightMotor.setSpeed(1);
				rightMotor.stop();
				theta-=lastTheta;
//				if (Math.abs(theta)>30) {
//					turnBy(-(theta+90));
//					move(20);
//					turnBy(90);
//					move(-10);
//					linedetect();
//					
//				}
				
				break;
				
			}
			if(lightDataR[0]<colorthr) {
				Sound.beep();
				rightMotor.setSpeed(1);
				rightMotor.stop();
				lightSensorL.fetchSample(lightDataL, 0);
				double lastTheta=Lab5.odometer.getXYT()[2];
				double theta=Lab5.odometer.getXYT()[2];
				while(lightDataL[0]>colorthr && Math.abs(theta-lastTheta)<30) {
					lightSensorL.fetchSample(lightDataL, 0);
					theta=Lab5.odometer.getXYT()[2];
				}
				Sound.beep();
				leftMotor.setSpeed(1);
				leftMotor.stop();
				theta-=lastTheta;
				if (Math.abs(theta)>30) {
					turnBy(-(theta+90));
					move(10);
					turnBy(90);
					move(-10);
					linedetect();
					
				}
				
				break;
			}
		
		}
		move(-3);
	}
	public void move(double distance) {
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
	/**This method reads distance values from the ultrasonic sensor and detects rapid changes in the distances during movement. Depending
	 * on the direction of the change in value, it is considered a rising or falling edge. Two edges are detected during the sequence
	 * and the robots angle at the moment of each detection is used to calculate the angle needed to accurately orient the robot at 
	 * 0 degrees.**/
	
	private void angleLocalization() {
		int lastDistance;
		int distance;
		navigation.turn360(true);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
		}
		while(!stopped) {
			if(fallingEdge) {
				lastDistance=Lab5.usPoller.getDistance();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				 distance=Lab5.usPoller.getDistance();
				if((lastDistance-distance)>distancethr && distance<60) {	//triggered if falling edge is detected
					if(!reached) {			//if its the first falling edge detected
						reached =true;	
						alpha=Lab5.odometer.getXYT()[2];		//store the first angle
						leftMotor.setSpeed(1);						//slow the motors to reduce error when stopping
						rightMotor.setSpeed(1);
						leftMotor.stop();						
						rightMotor.stop();
						Sound.beep();
						navigation.turn360(false);
						try {
							Thread.sleep(1000);						//one second pause to stop sensor from returning false positive.
						} catch (InterruptedException e) {
						}
					}else {
						beta=Lab5.odometer.getXYT()[2];		//if its not the first falling edge detected store the angle
						leftMotor.setSpeed(1);
						rightMotor.setSpeed(1);
						leftMotor.stop();
						rightMotor.stop();
						stopped=true;								//set boolean to true to ensure to it doesnt run this sequence again.
						Sound.beep();
					}
				}
			}
		}
		if(alpha>beta) {
			double current[] =Lab5.odometer.getXYT();
			Lab5.odometer.setTheta(45-(alpha+beta)/2+current[2]);	//sets the new corrected theta.

		}else {
			double current[] =Lab5.odometer.getXYT();
			Lab5.odometer.setTheta(225-(alpha+beta)/2+current[2]);	//also sets the new corrected if the first angle measured is less than the second.
		}

		navigation.turnTo(0);													//turn back to 0 degrees/

		currentPosition=Lab5.odometer.getXYT();
	}
	private static int convertDistance(double radius, double distance) {	//converts distance to wheel rotations
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	private static int convertAngle(double radius, double width, double angle) {	//converts angle to radians for degree rotation
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
