package ca.mcgill.ecse211.Test;

import java.util.Map;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.project.LightPoller;
import ca.mcgill.ecse211.project.Localization;
import ca.mcgill.ecse211.project.Navigation;
import ca.mcgill.ecse211.project.RingGrabbing;
import ca.mcgill.ecse211.project.UltrasonicPoller;
import ca.mcgill.ecse211.project.WifiClass;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

class BetaDemo {
	// Motor Objects, and Robot related parameters
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	public static final double WHEEL_RAD = 2.1;
	public static final double TRACK = 14.3;
	public static Navigation navigation;

	public static Localization localization;
	public static UltrasonicPoller usPoller;
	public static LightPoller LeftLightPoller;
	public static LightPoller RightLightPoller;
	public static RingGrabbing grabber;
	public static final double TILE_SIZE = 30.48;

	// coordonates of the robot after localization depending on the starting corner
	private static final double[][] COORDONATES = { { 1, 1, 0 }, { 7, 1, 270 }, { 7, 7, 180 }, { 1, 7, 90 } };
	private static Odometer odometer;

	private static final String SERVER_IP = "192.168.2.14";
	// defining all the parameters passed from the wifi class
	private static final int TEAM_NUM = 20;
	private static int GreenCorner = 0;
	private static int Green_LL_y = 0;
	private static int Green_LL_x = 0;
	private static int Green_UR_y = 0;
	private static int Green_UR_x = 0;
	private static int Island_LL_y = 0;
	private static int Island_LL_x = 0;
	private static int Island_UR_y = 0;
	private static int Island_UR_x = 0;
	private static int TNG_LL_y = 0;
	private static int TNG_LL_x = 0;
	private static int TNG_UR_y = 0;
	private static int TNG_UR_x = 0;
	private static int TG_y = 0;
	private static int TG_x = 0;
	private static int[] corners_checked= {0,0,0,0};
	public static int SensorAngle=-30;
	public static void main(String[] args) throws OdometerExceptions {

		// gets Odometer 
		odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD); 

		navigation = new Navigation(WHEEL_RAD, TRACK, leftMotor, rightMotor, odometer); // create an instance of the
																						// navigation class


		// Start odometer thread
		Thread odoThread = new Thread(odometer);
		odoThread.start();
		// create an ultrasonic poler
		usPoller = new UltrasonicPoller();
		// creating the left and right light pollers
		LeftLightPoller = new LightPoller(LocalEV3.get().getPort("S1"));
		RightLightPoller = new LightPoller(LocalEV3.get().getPort("S3"));
		// start our localization routine
		localization = new Localization(odometer, navigation, leftMotor, rightMotor, usPoller, LeftLightPoller,
				RightLightPoller);
		getWifiData();
		localization.InitialLocalization();
		Sound.beep();
		Sound.beep();
		Sound.beep();
		usPoller=null;
		Green_Game();

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);

	}

	/**
	 * This method gets data from the wifi class and initiate all the appropriate
	 * fields.
	 */
	public static void getWifiData() {
		@SuppressWarnings("rawtypes")
		Map temp = WifiClass.getMap(SERVER_IP, TEAM_NUM);
		GreenCorner = ((Long) temp.get("GreenCorner")).intValue();
		Green_LL_y = ((Long) temp.get("Green_LL_y")).intValue();
		Green_LL_x = ((Long) temp.get("Green_LL_x")).intValue();
		Green_UR_y = ((Long) temp.get("Green_UR_y")).intValue();
		Green_UR_x = ((Long) temp.get("Green_UR_x")).intValue();
		Island_LL_y = ((Long) temp.get("Island_LL_y")).intValue();
		Island_LL_x = ((Long) temp.get("Island_LL_x")).intValue();
		Island_UR_y = ((Long) temp.get("Island_UR_y")).intValue();
		Island_UR_x = ((Long) temp.get("Island_UR_x")).intValue();
		TNG_LL_y = ((Long) temp.get("TNG_LL_y")).intValue();
		TNG_LL_x = ((Long) temp.get("TNG_LL_x")).intValue();
		TNG_UR_y = ((Long) temp.get("TNG_UR_y")).intValue();
		TNG_UR_x = ((Long) temp.get("TNG_UR_x")).intValue();
		TG_y = ((Long) temp.get("TG_y")).intValue();
		TG_x = ((Long) temp.get("TG_x")).intValue();
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
			theta = 270;
		} else if (theta < 45 || theta > 315) {// second side
			theta = 0;
		} else if (theta > 45 && theta < 135) {// third side
			theta = 90;
		} else if (theta > 135 && theta < 225) {// fourth side
			theta = 180;
		}
		return new double[] { X * TILE_SIZE, Y * TILE_SIZE, ret };

	}

	/**
	 * This method defines the game play in the case where our we are the green
	 * team. It controls all the robots movements and operations in order to
	 * complete all the tasks required in the project manual.
	 */
	public static void Green_Game() {
		// set the odometer coordonates according to our starting corner
		odometer.setXYT(COORDONATES[GreenCorner][0] * TILE_SIZE, COORDONATES[GreenCorner][1] * TILE_SIZE,
				COORDONATES[GreenCorner][2]);
		if(tunnelIsVertical()) {
		navigation.travelTo(TNG_LL_x+1.5, TNG_LL_y - 1.5); // traveling next to the bridge
		/*
		 * We make the robot go to the middle of the line in front of the ring
		 */
		navigation.turnTo(270*Math.PI/180);
		localization.lightLocalization();
		navigation.turnBy(-90);
		odometer.setXYT((TNG_LL_x+1)*TILE_SIZE,(TNG_LL_y - 1)*TILE_SIZE , 270);
		navigation.travelTo(((double)(TNG_LL_x + TNG_UR_x) / 2.0)-0.07, TNG_LL_y - 1);
		navigation.turnBy(90);
		localization.linedetect_move2();
		localization.linedetect_move();
		odometer.setXYT((((double)(TNG_LL_x + TNG_UR_x) / 2.0)-0.07)*TILE_SIZE, (TNG_LL_y)*TILE_SIZE, 0);
		// traveling across the bridge
		navigation.travelTo((double) (TNG_LL_x + TNG_UR_x) / 2 -0.07, TNG_UR_y + 0.7);
		localization.lightLocalization(); // localize after crossing the bridge
	//	double temp[] = round(odometer.getXYT()[0], odometer.getXYT()[1], odometer.getXYT()[2]);
		odometer.setXYT(TNG_UR_x*TILE_SIZE, (TNG_UR_y+1)*TILE_SIZE, 90); // rounding the odometer values
		System.out.println(odometer.getXYT()[0]+ "   " + odometer.getXYT()[1] + "   " +odometer.getXYT()[2]);
		}else {
			navigation.travelTo(TNG_UR_x+1.2, TNG_UR_y - 1.5); // traveling next to the bridge
			/*
			 * We make the robot go to the middle of the line in front of the ring
			 */
			navigation.turnTo(0);
			localization.linedetect_move();
			navigation.turnBy(-90);
			localization.linedetect_move();
			navigation.turnBy(90);
			odometer.setXYT((TNG_UR_x+1)*TILE_SIZE,(TNG_UR_y - 1)*TILE_SIZE , 0);
			navigation.travelTo(TNG_UR_x+1, (double)(TNG_LL_y + TNG_UR_y) / 2.0 +0.03);
			navigation.turnBy(-90);
			localization.linedetect_move2();
			localization.linedetect_move();
			odometer.setXYT((TNG_UR_x)*TILE_SIZE, ((double)(TNG_LL_y + TNG_UR_y) / 2.0 +0.03)*TILE_SIZE, 270);
			// traveling across the bridge
			navigation.travelTo(TNG_LL_x-0.07, (double)(TNG_LL_y + TNG_UR_y) / 2.0 +0.03);
			localization.lightLocalization(); // localize after crossing the bridge
		//	double temp[] = round(odometer.getXYT()[0], odometer.getXYT()[1], odometer.getXYT()[2]);
			odometer.setXYT((TNG_LL_x-1)*TILE_SIZE, (TNG_LL_y+1)*TILE_SIZE, 0); // rounding the odometer values
		}
		// getting the closest corner to the Tree
		double [][]points= {{TG_x - 1, TG_y, 90},
				{TG_x, TG_y + 1, 180},
				{TG_x + 1, TG_y, 270},
				{TG_x, TG_y - 1, 0}};
		
		// Start the ring grabbing routine
		int index=0;
		getClosestPoint();
		for(int i=0; i<4;i++) {
			if(corners_checked[i]==1)index=i;
		}
		double[] FirstPoint=points[index];
		int lastindex=index;
		
		boolean grabbed=false;
		int counter=0;
		for(int i=0; i<4;i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(Math.abs(lastindex-index)!=2) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				navigation.travelTo(FirstPoint[0], FirstPoint[1]);
			}else if((lastindex%2)==0) {
				if (withinIslandBoundary(TG_x, TG_y + 1)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					navigation.travelTo(TG_x, TG_y + 1);
					navigation.travelTo(FirstPoint[0], FirstPoint[1]);
				}else if(withinIslandBoundary(TG_x, TG_y - 1)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					navigation.travelTo(TG_x, TG_y - 1);
					navigation.travelTo(FirstPoint[0], FirstPoint[1]);
				}
			}else {
				if (withinIslandBoundary(TG_x - 1, TG_y)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					navigation.travelTo(TG_x - 1, TG_y);
					navigation.travelTo(FirstPoint[0], FirstPoint[1]);
				}else if(withinIslandBoundary(TG_x + 1, TG_y)) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					navigation.travelTo(TG_x + 1, TG_y);
					navigation.travelTo(FirstPoint[0], FirstPoint[1]);
				}
			}
			lastindex=index;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			navigation.turnTo(FirstPoint[2]*Math.PI/180);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			navigation.turnBy(-90);
			navigation.move(-5);
			localization.linedetect_move2();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			navigation.turnBy(90);
			navigation.move(-5);
			localization.linedetect_move2();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			grabber = new RingGrabbing(navigation, leftMotor, rightMotor);
			grabbed=grabber.attempt();
			odometer.setXYT(FirstPoint[0]*TILE_SIZE, FirstPoint[1]*TILE_SIZE, FirstPoint[2]);
			if(grabbed)break;
			counter++;
			index+=1;
			index%=4;
			if(counter==4)break;
			while(!withinIslandBoundary(points[index][0], points[index][1])) {
				index+=1;
				index%=4;
				counter++;
				if(counter==4)break;
			}
			FirstPoint=points[index];
			if(counter==4)break;
			grabber=null;
		}
		
	
		// Normally, now we are at the corner we started at
		

	}

	public static void getClosestPoint() {
		double closestX = 0, closestY = 0;
		double smallestDist=0;
		boolean initialized = false;
		if (withinIslandBoundary(TG_x - 1, TG_y)) {
			initialized = true;
			closestX=TG_x - 1;
			 closestY = TG_y ;
			smallestDist=getDist(closestX*TILE_SIZE-odometer.getXYT()[0],closestY*TILE_SIZE-odometer.getXYT()[1]);
			corners_checked[0]=1;
		}
		if (withinIslandBoundary(TG_x, TG_y + 1)) {
			if (!initialized) {
				initialized = true;
				closestX=TG_x ;
				 closestY = TG_y+1 ;
				smallestDist=getDist(closestX*TILE_SIZE-odometer.getXYT()[0],closestY*TILE_SIZE-odometer.getXYT()[1]);
				corners_checked[1]=1;
			} else {
				double temp=getDist(TG_x*TILE_SIZE-odometer.getXYT()[0],(TG_y + 1)*TILE_SIZE-odometer.getXYT()[1]);
				if(temp<smallestDist) {
					smallestDist=temp;
					corners_checked[1]=1;
					corners_checked[0]=0;
				}
			}
		}
		if (withinIslandBoundary(TG_x + 1, TG_y)) {
			if (!initialized) {
				initialized = true;
				corners_checked[2]=1;
				closestX=TG_x +1;
				 closestY = TG_y ;
				smallestDist=getDist(closestX*TILE_SIZE-odometer.getXYT()[0],closestY*TILE_SIZE-odometer.getXYT()[1]);
			} else {
				double temp=getDist((TG_x+1)*TILE_SIZE-odometer.getXYT()[0],(TG_y)*TILE_SIZE-odometer.getXYT()[1]);
				if(temp<smallestDist) {
					corners_checked[2]=1;
					corners_checked[1]=0;
				}
			}
		}
		if (withinIslandBoundary(TG_x, TG_y - 1)) {
			if (!initialized) {
				closestX = TG_x;
				closestY = TG_y - 1;
				initialized = true;
				smallestDist=getDist(closestX*TILE_SIZE-odometer.getXYT()[0],closestY*TILE_SIZE-odometer.getXYT()[1]);
				corners_checked[3]=1;
			} else {
				double temp=getDist(TG_x*TILE_SIZE-odometer.getXYT()[0],(TG_y - 1)*TILE_SIZE-odometer.getXYT()[1]);
				if(temp<smallestDist) {
					closestX = TG_x;
					closestY = TG_y - 1;
					smallestDist=temp;
					corners_checked[3]=1;
					corners_checked[2]=0;
				}
			}
			
			
		}
	}
	public static double getDist(double deltaX, double deltaY) {
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}
	public static boolean isWithinGreenIsland(double x, double y) {
		return (x < Green_UR_x) && (x > Green_LL_x) && (y < Green_UR_y) && (y > Green_LL_y);
	}

	public static boolean withinIslandBoundary(double x, double y) {
		return (x <= Island_UR_x - 1) && (x >= Island_LL_x + 1) && (y <= Island_UR_y - 1) && (y >= Island_LL_y + 1);
	}
	
	public static boolean tunnelIsVertical() {
		return (TNG_LL_x <= Green_UR_x) && (TNG_LL_x >= Green_LL_x ) && (TNG_LL_y <= Green_UR_y) && ( TNG_LL_y >= Green_LL_y);
	}
}
