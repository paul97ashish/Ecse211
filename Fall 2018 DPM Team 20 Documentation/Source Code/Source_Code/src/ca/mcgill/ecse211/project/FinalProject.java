/**
 * This is the main class It is used to create all the thread and calling the
 * appropriate methods.
 * <p>
 * It is responsible for localizing, traversing the tunnel, grabbing the rings,
 * and coming back to the starting corner.
 * 
 * @author Zakaria Essadaoui
 * @author Carl ElKhoury
 *
 */
package ca.mcgill.ecse211.project;

import java.util.Map;

import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;


public class FinalProject {
	// Motor Objects, and Robot related parameters
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	public static final double WHEEL_RAD = 2.1;
	public static final double TRACK = 14.85;

	// Declaring instances of classes used.
	public static Localization localization;
	public static UltrasonicPoller usPoller;
	public static LightPoller LeftLightPoller;
	public static LightPoller RightLightPoller;
	public static RingGrabbing grabber;
	public static Navigation navigation;
	private static Odometer odometer;
	// Grids related parameters
	public static final double TILE_SIZE = 30.48;
	private static final int max_X = 15;
	private static final int max_Y = 9;
	private static final double[][] COORDONATES = { { 1, 1, 0 }, { max_X - 1, 1, 270 }, { max_X - 1, max_Y - 1, 180 },
			{ 1, max_Y - 1, 90 } };

	// Server IP field
	private static final String SERVER_IP = "192.168.2.2";
	// Game related parameters
	private static int Corner = 1;
	private static int Start_LL_y = 0;
	private static int Start_LL_x = 12;
	private static int Start_UR_y = 9;
	private static int Start_UR_x = 15;
	private static int Island_LL_y = 0;
	private static int Island_LL_x = 7;
	private static int Island_UR_y = 9;
	private static int Island_UR_x = 11;
	private static int TN_LL_y = 5;
	private static int TN_LL_x = 11;
	private static int TN_UR_y = 6;
	private static int TN_UR_x = 12;
	private static int TR_y = 8;
	private static int TR_x = 9;

	// Tunnel and tree related parameters
	private static int[][] Tunnel_Corners = new int[4][2];
	private static int[] corners_checked = { 0, 0, 0, 0 };
	private static boolean[] orientation = new boolean[2];
	// speed offset between the two motors used when traversing the tunnel
	private static final int offset_speed = 5;
	// Boolean to that holds whether the tree obstruct the trajectory of the robot when going back to the tunnel.
	private static boolean tree_obstruct = false;

	/**
	 * This is the main method for the project. It takes care of the calling all the
	 * appropriate methods to make the robot performs all the tasks required.
	 * 
	 * @param args
	 * @throws OdometerExceptions
	 */
	public static void main(String[] args) throws OdometerExceptions {

		// Odometer object
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
		// Initiates localization object
		localization = new Localization(odometer, navigation, leftMotor, rightMotor, usPoller, LeftLightPoller,
				RightLightPoller);
		Button.waitForAnyPress();
		// wait for wifi server and get wifi data
		getWifiData();
		// start our localization routine
		localization.InitialLocalization();
		Sound.beep();
		Sound.beep();
		Sound.beep();
		usPoller.close();
		// reduce the speed of the motors
		Navigation.FORWARD_SPEED = 400;
		// setting the robot's coordinates depending of the starting corner
		odometer.setXYT(COORDONATES[Corner][0] * TILE_SIZE, COORDONATES[Corner][1] * TILE_SIZE, COORDONATES[Corner][2]);
		orientation = getTunnelOrientation();
		navigation.speed_offset = 3;
		TraverseTunnel();
		goToTree();
		ColorDetection.colorSensor.close();
		Navigation.FORWARD_SPEED = 400;
		orientation[1] = !orientation[1];
		TraverseTunnel();
		navigation.speed_offset = 5;
		// Go back to the starting corner
		navigation.travelTo(COORDONATES[Corner][0], COORDONATES[Corner][1]);
		// Ensures that we are within the starting tile
		if (Corner == 0) {
			navigation.turnTo(225 * Math.PI / 180);
			navigation.move(15);
		} else if (Corner == 1) {
			navigation.turnTo(135 * Math.PI / 180);
			navigation.move(15);
		} else if (Corner == 2) {
			navigation.turnTo(45 * Math.PI / 180);
			navigation.move(15);
		} else if (Corner == 3) {
			navigation.turnTo(315 * Math.PI / 180);
			navigation.move(15);
		}
		// unload the rings
		RingGrabbing.CollectingMotor.setSpeed(300);
		RingGrabbing.CollectingMotor.rotate(360 * 4);
		Sound.beep();
		Sound.beep();
		Sound.beep();
		Sound.beep();
		Sound.beep();
		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);

	}

	/**
	 * This method gets data from the WIFI class and initiate all the appropriate
	 * fields.
	 * <p>
	 * First, it checks which team we are (Red or Green) and initiate the the
	 * Islands and Tree parameters accordingly.
	 * 
	 * @return void
	 */
	public static void getWifiData() {
		@SuppressWarnings("rawtypes")
		Map temp = WifiClass.getMap(SERVER_IP, 20);
		int RedTeam = ((Long) temp.get("RedTeam")).intValue();
		int GreenTeam = ((Long) temp.get("GreenTeam")).intValue();
		if (GreenTeam == 20) {
			Corner = ((Long) temp.get("GreenCorner")).intValue();
			Start_LL_y = ((Long) temp.get("Green_LL_y")).intValue();
			Start_LL_x = ((Long) temp.get("Green_LL_x")).intValue();
			Start_UR_y = ((Long) temp.get("Green_UR_y")).intValue();
			Start_UR_x = ((Long) temp.get("Green_UR_x")).intValue();
			Island_LL_y = ((Long) temp.get("Island_LL_y")).intValue();
			Island_LL_x = ((Long) temp.get("Island_LL_x")).intValue();
			Island_UR_y = ((Long) temp.get("Island_UR_y")).intValue();
			Island_UR_x = ((Long) temp.get("Island_UR_x")).intValue();
			TN_LL_y = ((Long) temp.get("TNG_LL_y")).intValue();
			TN_LL_x = ((Long) temp.get("TNG_LL_x")).intValue();
			TN_UR_y = ((Long) temp.get("TNG_UR_y")).intValue();
			TN_UR_x = ((Long) temp.get("TNG_UR_x")).intValue();
			TR_y = ((Long) temp.get("TG_y")).intValue();
			TR_x = ((Long) temp.get("TG_x")).intValue();
		} else if (RedTeam == 20) {
			Corner = ((Long) temp.get("RedCorner")).intValue();
			Start_LL_y = ((Long) temp.get("Red_LL_y")).intValue();
			Start_LL_x = ((Long) temp.get("Red_LL_x")).intValue();
			Start_UR_y = ((Long) temp.get("Red_UR_y")).intValue();
			Start_UR_x = ((Long) temp.get("Red_UR_x")).intValue();
			Island_LL_y = ((Long) temp.get("Island_LL_y")).intValue();
			Island_LL_x = ((Long) temp.get("Island_LL_x")).intValue();
			Island_UR_y = ((Long) temp.get("Island_UR_y")).intValue();
			Island_UR_x = ((Long) temp.get("Island_UR_x")).intValue();
			TN_LL_y = ((Long) temp.get("TNR_LL_y")).intValue();
			TN_LL_x = ((Long) temp.get("TNR_LL_x")).intValue();
			TN_UR_y = ((Long) temp.get("TNR_UR_y")).intValue();
			TN_UR_x = ((Long) temp.get("TNR_UR_x")).intValue();
			TR_y = ((Long) temp.get("TR_y")).intValue();
			TR_x = ((Long) temp.get("TR_x")).intValue();
		}
	}

	/**
	 * This method makes the robot go to closest point next to the tree first. Then
	 * checks for rings in all the sides of the tree that are accessible (not next
	 * to a wall or the water). At each corner, it calls the attempt method from the
	 * RingGrabbing class.
	 * 
	 * @return void
	 */
	public static void goToTree() {
		int[][] points = { { TR_x - 1, TR_y, 90 }, { TR_x, TR_y + 1, 180 }, { TR_x + 1, TR_y, 270 },
				{ TR_x, TR_y - 1, 0 } };

		// Start the ring grabbing routine
		int index = 0;
		getClosestPoint();
		for (int i = 0; i < 4; i++) {
			if (corners_checked[i] == 1)
				index = i;
		}
		int[] FirstPoint = points[index];
		int firstindex = index;
		int counter = 0;
		navigation.travelTo(FirstPoint[0], FirstPoint[1]);
		navigation.turnTo(FirstPoint[2] * Math.PI / 180);
		navigation.speed_offset = 0;
		Sound.beep();
		Sound.beep();
		Sound.beep();
		Navigation.FORWARD_SPEED = 300;
		sleep();
		navigation.turnBy(-90);
		localization.linedetect_move2();
		sleep();
		navigation.turnBy(90);
		localization.linedetect_move2();
		sleep();
		grabber = new RingGrabbing(navigation, leftMotor, rightMotor);
		grabber.attempt();
		counter++;
		while (counter < 4) {
			boolean left = false;
			boolean right = false;
			if ((index % 2) == 0) {
				if (isAvailable(points[index][0], points[index][1] - 1 + index)) {
					right = true;
				}
				if (isAvailable(points[index][0], points[index][1] + 1 - index)) {
					left = true;
				}
			} else {
				if (isAvailable(points[index][0] - 2 + index, points[index][1])) {
					right = true;
				}
				if (isAvailable(points[index][0] + 2 - index, points[index][1])) {
					left = true;
				}
			}
			boolean precedence_right = false;
			if (isAvailable(points[(index + 3) % 4][0], points[(index + 3) % 4][1]) && right
					&& corners_checked[(index + 3) % 4] != 1) {
				precedence_right = true;
			} else if (isAvailable(points[(index + 1) % 4][0], points[(index + 1) % 4][1]) && left
					&& corners_checked[(index + 1) % 4] != 1) {
				precedence_right = false;
			}
			if (precedence_right) {
				if (isAvailable(points[(index + 3) % 4][0], points[(index + 3) % 4][1]) && right) {
					index = (index + 3) % 4;
					navigation.turnBy(90);
					navigation.move(TILE_SIZE * 0.7);
					localization.linedetect_move();
					navigation.turnBy(-90);
					navigation.move(TILE_SIZE * 0.7);
					localization.linedetect_move();
					navigation.turnBy(-90);
					odometer.setXYT(points[index][0] * TILE_SIZE, points[index][1] * TILE_SIZE, points[index][2]);
					if (corners_checked[index] != 1) {
						grabber.attempt();
						corners_checked[index] = 1;
					}
				} else if (isAvailable(points[(index + 1) % 4][0], points[(index + 1) % 4][1]) && left) {
					index = (index + 1) % 4;
					navigation.turnBy(-90);
					navigation.move(TILE_SIZE * 0.7);
					localization.linedetect_move();
					navigation.turnBy(90);
					navigation.move(TILE_SIZE * 0.7);
					localization.linedetect_move();
					navigation.turnBy(90);
					odometer.setXYT(points[index][0] * TILE_SIZE, points[index][1] * TILE_SIZE, points[index][2]);
					if (corners_checked[index] != 1) {
						grabber.attempt();
						corners_checked[index] = 1;
					}
				}
			} else {
				if (isAvailable(points[(index + 1) % 4][0], points[(index + 1) % 4][1]) && left) {
					index = (index + 1) % 4;
					navigation.turnBy(-90);
					navigation.move(TILE_SIZE * 0.7);
					localization.linedetect_move();
					navigation.turnBy(90);
					navigation.move(TILE_SIZE * 0.7);
					localization.linedetect_move();
					navigation.turnBy(90);
					odometer.setXYT(points[index][0] * TILE_SIZE, points[index][1] * TILE_SIZE, points[index][2]);
					if (corners_checked[index] != 1) {
						grabber.attempt();
						corners_checked[index] = 1;
					}
				} else if (isAvailable(points[(index + 3) % 4][0], points[(index + 3) % 4][1]) && right) {
					index = (index + 3) % 4;
					navigation.turnBy(90);
					navigation.move(TILE_SIZE * 0.7);
					localization.linedetect_move();
					navigation.turnBy(-90);
					navigation.move(TILE_SIZE * 0.7);
					localization.linedetect_move();
					navigation.turnBy(-90);
					odometer.setXYT(points[index][0] * TILE_SIZE, points[index][1] * TILE_SIZE, points[index][2]);
					if (corners_checked[index] != 1) {
						grabber.attempt();
						corners_checked[index] = 1;
					}
				}

			}
			counter++;
		}
		RingGrabbing.SensorMotor.close();
		if (!tree_obstruct) {
			if (Math.abs(firstindex - index) != 2) {
				sleep();
				navigation.travelTo(points[firstindex][0], points[firstindex][1]);
			} else if ((index % 2) == 0) {
				if (isAvailable(TR_x, TR_y + 1)) {
					sleep();
					navigation.travelTo(TR_x, TR_y + 1);
					navigation.travelTo(points[firstindex][0], points[firstindex][1]);
				} else if (isAvailable(TR_x, TR_y - 1)) {
					sleep();
					navigation.travelTo(TR_x, TR_y - 1);
					navigation.travelTo(points[firstindex][0], points[firstindex][1]);
				}
			} else {
				if (isAvailable(TR_x - 1, TR_y)) {
					sleep();
					navigation.travelTo(TR_x - 1, TR_y);
					navigation.travelTo(points[firstindex][0], points[firstindex][1]);
				} else if (isAvailable(TR_x + 1, TR_y)) {
					sleep();
					navigation.travelTo(TR_x + 1, TR_y);
					navigation.travelTo(points[firstindex][0], points[firstindex][1]);
				}
			}
		}
	}

	/**
	 * This method checks if a point is available and returns a boolean variable
	 * accordingly.
	 * <p>
	 * A point is considered available if it is in the Island without being a wall
	 * nor a point in the tunnel. Points on the water boundary are considered as
	 * being available.
	 * 
	 * @param x:
	 *            x coordinate of the point.
	 * @param y:
	 *            y coordinate of the point.
	 * @return Boolean: true if the point is available, false otherwise.
	 */
	public static boolean isAvailable(int x, int y) {
		return withinIsland(x, y) && !isInWall(x, y) && !isInTunnel(x, y);
	}

	/**
	 * This method checks if a point is in a wall and returns a boolean variable
	 * accordingly.
	 * 
	 * @param x:
	 *            x coordinate of the point.
	 * @param y:
	 *            y coordinate of the point.
	 * @return Boolean: true if the point in part of the wall, false otherwise.
	 */
	public static boolean isInWall(int x, int y) {
		return (x == 0) || (y == 0) || (x == max_X) || (y == max_Y);
	}

	/**
	 * This method checks if a point is within a tunnel of its sides and returns a
	 * boolean accordingly.
	 * 
	 * @param x:
	 *            x coordinate of the point.
	 * @param y:
	 *            y coordinate of the point.
	 * @return Boolean: true if the point in part of the tunnel, false otherwise.
	 * 
	 */
	public static boolean isInTunnel(int x, int y) {
		return (x <= TN_UR_x) && (x >= TN_LL_x) && (y <= TN_UR_y) && (y >= TN_LL_y);
	}

	/**
	 * This method makes the robot sleep for 1000ms.
	 */
	public static void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * When called, this method checks for the closest corner of the tree from the
	 * robot's position. Then it fills the corners_checked[corner] with 1.
	 * <P>
	 * It only checks for available corners, i.e. corners that are nor a wall nor in
	 * water.
	 */
	public static void getClosestPoint() {
		double closestX = 0, closestY = 0;
		double smallestDist = 0;
		boolean initialized = false;
		if (withinIslandBoundary(TR_x - 1, TR_y)) {
			initialized = true;
			closestX = TR_x - 1;
			closestY = TR_y;
			smallestDist = getDist(closestX * TILE_SIZE - odometer.getXYT()[0],
					closestY * TILE_SIZE - odometer.getXYT()[1]);
			corners_checked[0] = 1;
		}
		if (withinIslandBoundary(TR_x, TR_y + 1)) {
			if (!initialized) {
				initialized = true;
				closestX = TR_x;
				closestY = TR_y + 1;
				smallestDist = getDist(closestX * TILE_SIZE - odometer.getXYT()[0],
						closestY * TILE_SIZE - odometer.getXYT()[1]);
				corners_checked[1] = 1;
			} else {
				double temp = getDist(TR_x * TILE_SIZE - odometer.getXYT()[0],
						(TR_y + 1) * TILE_SIZE - odometer.getXYT()[1]);
				if (temp < smallestDist) {
					smallestDist = temp;
					corners_checked[1] = 1;
					corners_checked[0] = 0;
					corners_checked[2] = 0;
					corners_checked[3] = 0;
				}
			}
		}
		if (withinIslandBoundary(TR_x + 1, TR_y)) {
			if (!initialized) {
				initialized = true;
				corners_checked[2] = 1;
				closestX = TR_x + 1;
				closestY = TR_y;
				smallestDist = getDist(closestX * TILE_SIZE - odometer.getXYT()[0],
						closestY * TILE_SIZE - odometer.getXYT()[1]);
			} else {
				double temp = getDist((TR_x + 1) * TILE_SIZE - odometer.getXYT()[0],
						(TR_y) * TILE_SIZE - odometer.getXYT()[1]);
				if (temp < smallestDist) {
					corners_checked[2] = 1;
					corners_checked[1] = 0;
					corners_checked[0] = 0;
					corners_checked[3] = 0;
				}
			}
		}
		if (withinIslandBoundary(TR_x, TR_y - 1)) {
			if (!initialized) {
				closestX = TR_x;
				closestY = TR_y - 1;
				initialized = true;
				smallestDist = getDist(closestX * TILE_SIZE - odometer.getXYT()[0],
						closestY * TILE_SIZE - odometer.getXYT()[1]);
				corners_checked[3] = 1;
			} else {
				double temp = getDist(TR_x * TILE_SIZE - odometer.getXYT()[0],
						(TR_y - 1) * TILE_SIZE - odometer.getXYT()[1]);
				if (temp < smallestDist) {
					closestX = TR_x;
					closestY = TR_y - 1;
					smallestDist = temp;
					corners_checked[3] = 1;
					corners_checked[2] = 0;
					corners_checked[1] = 0;
					corners_checked[0] = 0;
				}
			}
		}
	}

	/**
	 * This method make the robot go close to the tunnel, traverse it and localize
	 * at the other end of the tunnel.
	 * <p>
	 * We are using the array "orientation" to get both the orientation of the
	 * tunnel and the direction of the traversal.
	 * <p>
	 * orientation[0] gives the orientation of the tunnel (true means vertical and
	 * false means horizontal).
	 * <p>
	 * orientation[1] gives the direction of the tunnel.For the vertical case: true
	 * is UP and false is DOWN. For the horizontal case: true is LEFT and false is
	 * RIGHT.
	 */
	public static void TraverseTunnel() {
		if (orientation[0]) {// case where the tunnel is vertical
			if (orientation[1]) {// case where we need to go UP
				double distLeft = getDist((Tunnel_Corners[0][0] * TILE_SIZE - odometer.getXYT()[0]),
						(Tunnel_Corners[0][1] * TILE_SIZE - odometer.getXYT()[1]));
				double distright = getDist((Tunnel_Corners[1][0] * TILE_SIZE - odometer.getXYT()[0]),
						(Tunnel_Corners[1][1] * TILE_SIZE - odometer.getXYT()[1]));
				if (distLeft < distright) {
					navigation.travelTo(Tunnel_Corners[0][0] - 0.5, Tunnel_Corners[0][1] - 0.5);
					navigation.turnTo(90 * Math.PI / 180);
					localization.linedetect_move();
					odometer.setXYT(Tunnel_Corners[0][0] * TILE_SIZE, (Tunnel_Corners[0][1] - 0.5) * TILE_SIZE, 90);
					navigation.travelTo(Tunnel_Corners[0][0] + 0.5, Tunnel_Corners[0][1] - 0.5);
					navigation.turnBy(-93);
				} else {
					navigation.travelTo(Tunnel_Corners[1][0] + 0.5, Tunnel_Corners[1][1] - 0.5);
					navigation.turnTo(270 * Math.PI / 180);
					localization.linedetect_move();
					odometer.setXYT(Tunnel_Corners[1][0] * TILE_SIZE, (Tunnel_Corners[1][1] - 0.7) * TILE_SIZE, 270);
					navigation.travelTo(Tunnel_Corners[1][0] - 0.5, Tunnel_Corners[1][1] - 0.7);
					navigation.turnBy(87);
				}
				localization.linedetect_move();
				odometer.setXYT((Tunnel_Corners[0][0] + 0.5) * TILE_SIZE, (Tunnel_Corners[0][1]) * TILE_SIZE, 0);
				navigation.speed_offset = offset_speed;
				navigation.travelTo(Tunnel_Corners[2][0] + 0.5, Tunnel_Corners[2][1] + 0.5);
				navigation.speed_offset = 0;

				if ((withinIslandBoundary(Tunnel_Corners[2][0], Tunnel_Corners[2][1] + 1)
						|| isInStartIsland(new int[] { Tunnel_Corners[2][0], Tunnel_Corners[2][1] + 1 }))
						&& !IsTree(Tunnel_Corners[2][0], Tunnel_Corners[2][1] + 1)) {
					localization.linedetect_move();
					navigation.turnBy(-90);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[2][0]) * TILE_SIZE, (Tunnel_Corners[2][1] + 1) * TILE_SIZE, 270);
				} else if ((withinIslandBoundary(Tunnel_Corners[3][0], Tunnel_Corners[3][1] + 1)
						|| isInStartIsland(new int[] { Tunnel_Corners[3][0], Tunnel_Corners[3][1] + 1 }))
						&& !IsTree(Tunnel_Corners[3][0], Tunnel_Corners[3][1] + 1)) {
					localization.linedetect_move();
					navigation.turnBy(90);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[3][0]) * TILE_SIZE, (Tunnel_Corners[3][1] + 1) * TILE_SIZE, 90);
				} else {

					if ((withinIslandBoundary(Tunnel_Corners[2][0], Tunnel_Corners[2][1] + 2)
							|| isInStartIsland(new int[] { Tunnel_Corners[2][0], Tunnel_Corners[2][1] + 2 }))
							&& !IsTree(Tunnel_Corners[2][0], Tunnel_Corners[2][1] + 2)) {
						localization.linedetect_move();
						localization.linedetect_move();
						navigation.turnBy(-90);
						localization.linedetect_move();
						odometer.setXYT((Tunnel_Corners[2][0]) * TILE_SIZE, (Tunnel_Corners[2][1] + 2) * TILE_SIZE,
								270);
					} else if ((withinIslandBoundary(Tunnel_Corners[3][0], Tunnel_Corners[3][1] + 2)
							|| isInStartIsland(new int[] { Tunnel_Corners[3][0], Tunnel_Corners[3][1] + 2 }))
							&& !IsTree(Tunnel_Corners[3][0], Tunnel_Corners[3][1] + 2)) {
						localization.linedetect_move();
						localization.linedetect_move();
						navigation.turnBy(90);
						localization.linedetect_move();
						odometer.setXYT((Tunnel_Corners[3][0]) * TILE_SIZE, (Tunnel_Corners[3][1] + 2) * TILE_SIZE, 90);
					} else {
						odometer.setXYT(Tunnel_Corners[2][0] + 0.5, Tunnel_Corners[2][1] + 0.5, 0);
						tree_obstruct = true;
					}
				}
			} else {// case where we need to go DOWN
				double distLeft = getDist((Tunnel_Corners[2][0] * TILE_SIZE - odometer.getXYT()[0]),
						(Tunnel_Corners[2][1] * TILE_SIZE - odometer.getXYT()[1]));
				double distright = getDist((Tunnel_Corners[3][0] * TILE_SIZE - odometer.getXYT()[0]),
						(Tunnel_Corners[3][1] * TILE_SIZE - odometer.getXYT()[1]));
				if (distLeft < distright) {
					navigation.travelTo(Tunnel_Corners[2][0] - 0.5, Tunnel_Corners[2][1] + 0.5);
					navigation.turnTo(90 * Math.PI / 180);
					localization.linedetect_move();
					odometer.setXYT(Tunnel_Corners[2][0] * TILE_SIZE, (Tunnel_Corners[2][1] - 0.5) * TILE_SIZE, 90);
					navigation.travelTo(Tunnel_Corners[2][0] + 0.5, Tunnel_Corners[2][1] - 0.5);
					navigation.turnBy(87);
				} else {
					navigation.travelTo(Tunnel_Corners[3][0] + 0.5, Tunnel_Corners[3][1] + 0.5);
					navigation.turnTo(270 * Math.PI / 180);
					localization.linedetect_move();
					odometer.setXYT(Tunnel_Corners[3][0] * TILE_SIZE, (Tunnel_Corners[3][1] + 0.5) * TILE_SIZE, 270);
					navigation.travelTo(Tunnel_Corners[3][0] - 0.5, Tunnel_Corners[3][1] + 0.5);
					navigation.turnBy(-93);
				}
				localization.linedetect_move();
				odometer.setXYT((Tunnel_Corners[2][0] + 0.5) * TILE_SIZE, (Tunnel_Corners[2][1]) * TILE_SIZE, 180);
				navigation.speed_offset = offset_speed;
				navigation.travelTo(Tunnel_Corners[0][0] + 0.5, Tunnel_Corners[0][1] - 0.5);
				navigation.speed_offset = 0;

				if ((withinIslandBoundary(Tunnel_Corners[1][0], Tunnel_Corners[1][1] - 1)
						|| isInStartIsland(new int[] { Tunnel_Corners[1][0], Tunnel_Corners[1][1] - 1 }))
						&& !IsTree(Tunnel_Corners[1][0], Tunnel_Corners[1][1] - 1)) {
					localization.linedetect_move();
					navigation.turnBy(-90);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[1][0]) * TILE_SIZE, (Tunnel_Corners[1][1] - 1) * TILE_SIZE, 90);
				} else if ((withinIslandBoundary(Tunnel_Corners[0][0], Tunnel_Corners[0][1] - 1)
						|| isInStartIsland(new int[] { Tunnel_Corners[0][0], Tunnel_Corners[0][1] - 1 }))
						&& !IsTree(Tunnel_Corners[0][0], Tunnel_Corners[0][1] - 1)) {
					localization.linedetect_move();
					navigation.turnBy(90);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[0][0]) * TILE_SIZE, (Tunnel_Corners[0][1] - 1) * TILE_SIZE, 270);
				} else {
					if ((withinIslandBoundary(Tunnel_Corners[1][0], Tunnel_Corners[1][1] - 2)
							|| isInStartIsland(new int[] { Tunnel_Corners[1][0], Tunnel_Corners[1][1] - 2 }))
							&& !IsTree(Tunnel_Corners[1][0], Tunnel_Corners[1][1] - 2)) {
						localization.linedetect_move();
						localization.linedetect_move();
						navigation.turnBy(-90);
						localization.linedetect_move();
						odometer.setXYT((Tunnel_Corners[1][0]) * TILE_SIZE, (Tunnel_Corners[1][1] - 2) * TILE_SIZE, 90);
					} else if ((withinIslandBoundary(Tunnel_Corners[0][0], Tunnel_Corners[0][1] - 2)
							|| isInStartIsland(new int[] { Tunnel_Corners[0][0], Tunnel_Corners[0][1] - 2 }))
							&& !IsTree(Tunnel_Corners[0][0], Tunnel_Corners[0][1] - 2)) {
						localization.linedetect_move();
						localization.linedetect_move();
						navigation.turnBy(90);
						localization.linedetect_move();
						odometer.setXYT((Tunnel_Corners[0][0]) * TILE_SIZE, (Tunnel_Corners[0][1] - 2) * TILE_SIZE,
								270);
					} else {
						odometer.setXYT(Tunnel_Corners[0][0] + 0.5, Tunnel_Corners[0][1] - 0.5, 180);
						tree_obstruct = true;
					}
				}

			}
		} else {// case where the tunnel is horizontal
			if (orientation[1]) {// case where we need to go right
				double distUp = getDist((Tunnel_Corners[2][0] * TILE_SIZE - odometer.getXYT()[0]),
						(Tunnel_Corners[2][1] * TILE_SIZE - odometer.getXYT()[1]));
				double distDown = getDist((Tunnel_Corners[0][0] * TILE_SIZE - odometer.getXYT()[0]),
						(Tunnel_Corners[0][1] * TILE_SIZE - odometer.getXYT()[1]));
				if (distUp < distDown) {
					navigation.travelTo(Tunnel_Corners[2][0] - 0.5, Tunnel_Corners[2][1] + 0.5);
					navigation.turnTo(180 * Math.PI / 180);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[2][0] - 0.5) * TILE_SIZE, (Tunnel_Corners[2][1]) * TILE_SIZE, 180);
					navigation.travelTo(Tunnel_Corners[2][0] - 0.5, Tunnel_Corners[2][1] - 0.5);
					navigation.turnBy(-93);
				} else {
					navigation.travelTo(Tunnel_Corners[0][0] - 0.5, Tunnel_Corners[0][1] - 0.5);
					navigation.turnTo(0 * Math.PI / 180);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[0][0] - 0.7) * TILE_SIZE, (Tunnel_Corners[0][1]) * TILE_SIZE, 0);
					navigation.travelTo(Tunnel_Corners[0][0] - 0.7, Tunnel_Corners[0][1] + 0.5);
					navigation.turnBy(87);
				}
				localization.linedetect_move();
				odometer.setXYT((Tunnel_Corners[2][0]) * TILE_SIZE, (Tunnel_Corners[2][1] - 0.5) * TILE_SIZE, 90);
				navigation.speed_offset = offset_speed;
				navigation.travelTo(Tunnel_Corners[3][0] + 0.5, Tunnel_Corners[3][1] - 0.5);
				navigation.speed_offset = 0;

				if ((withinIslandBoundary(Tunnel_Corners[1][0] + 1, Tunnel_Corners[1][1])
						|| isInStartIsland(new int[] { Tunnel_Corners[1][0] + 1, Tunnel_Corners[1][1] }))
						&& !IsTree(Tunnel_Corners[1][0] + 1, Tunnel_Corners[1][1])) {
					localization.linedetect_move();
					navigation.turnBy(90);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[1][0] + 1) * TILE_SIZE, (Tunnel_Corners[1][1]) * TILE_SIZE, 180);
				} else if ((withinIslandBoundary(Tunnel_Corners[3][0] + 1, Tunnel_Corners[3][1])
						|| isInStartIsland(new int[] { Tunnel_Corners[3][0] + 1, Tunnel_Corners[3][1] }))
						&& !IsTree(Tunnel_Corners[3][0] + 1, Tunnel_Corners[3][1])) {
					localization.linedetect_move();
					navigation.turnBy(-90);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[3][0] + 1) * TILE_SIZE, (Tunnel_Corners[3][1]) * TILE_SIZE, 0);
				} else {
					if ((withinIslandBoundary(Tunnel_Corners[1][0] + 2, Tunnel_Corners[1][1])
							|| isInStartIsland(new int[] { Tunnel_Corners[1][0] + 2, Tunnel_Corners[1][1] }))
							&& !IsTree(Tunnel_Corners[1][0] + 2, Tunnel_Corners[1][1])) {
						localization.linedetect_move();
						localization.linedetect_move();
						navigation.turnBy(90);
						localization.linedetect_move();
						odometer.setXYT((Tunnel_Corners[1][0] + 2) * TILE_SIZE, (Tunnel_Corners[1][1]) * TILE_SIZE,
								180);
					} else if ((withinIslandBoundary(Tunnel_Corners[3][0] + 1, Tunnel_Corners[3][1])
							|| isInStartIsland(new int[] { Tunnel_Corners[3][0] + 1, Tunnel_Corners[3][1] }))
							&& !IsTree(Tunnel_Corners[3][0] + 2, Tunnel_Corners[3][1])) {
						localization.linedetect_move();
						localization.linedetect_move();
						navigation.turnBy(-90);
						localization.linedetect_move();
						odometer.setXYT((Tunnel_Corners[3][0] + 2) * TILE_SIZE, (Tunnel_Corners[3][1]) * TILE_SIZE, 0);
					} else {
						odometer.setXYT(Tunnel_Corners[3][0] + 0.5, Tunnel_Corners[3][1] - 0.5, 90);
						tree_obstruct = true;
					}
				}
			} else {// case where we need to go left.
				double distUp = getDist((Tunnel_Corners[3][0] * TILE_SIZE - odometer.getXYT()[0]),
						(Tunnel_Corners[3][1] * TILE_SIZE - odometer.getXYT()[1]));
				double distDown = getDist((Tunnel_Corners[1][0] * TILE_SIZE - odometer.getXYT()[0]),
						(Tunnel_Corners[1][1] * TILE_SIZE - odometer.getXYT()[1]));
				if (distUp < distDown) {
					navigation.travelTo(Tunnel_Corners[3][0] + 0.5, Tunnel_Corners[3][1] + 0.5);
					navigation.turnTo(180 * Math.PI / 180);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[3][0] + 0.5) * TILE_SIZE, (Tunnel_Corners[3][1]) * TILE_SIZE, 180);
					navigation.travelTo(Tunnel_Corners[3][0] + 0.5, Tunnel_Corners[3][1] - 0.5);
					navigation.turnBy(87);
				} else {
					navigation.travelTo(Tunnel_Corners[1][0] + 0.5, Tunnel_Corners[1][1] - 0.5);
					navigation.turnTo(0);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[1][0] + 0.5) * TILE_SIZE, (Tunnel_Corners[1][1]) * TILE_SIZE, 0);
					navigation.travelTo(Tunnel_Corners[1][0] + 0.5, Tunnel_Corners[1][1] + 0.5);
					navigation.turnBy(-93);
				}
				localization.linedetect_move();
				odometer.setXYT((Tunnel_Corners[1][0]) * TILE_SIZE, (Tunnel_Corners[1][1] + 0.5) * TILE_SIZE, 270);
				navigation.speed_offset = offset_speed;
				navigation.speed_offset = 0;
				navigation.travelTo(Tunnel_Corners[2][0] - 0.5, Tunnel_Corners[2][1] - 0.5);

				if ((withinIslandBoundary(Tunnel_Corners[2][0] - 1, Tunnel_Corners[2][1])
						|| isInStartIsland(new int[] { Tunnel_Corners[2][0] - 1, Tunnel_Corners[2][1] }))
						&& !IsTree(Tunnel_Corners[2][0] - 1, Tunnel_Corners[2][1])) {
					localization.linedetect_move();
					navigation.turnBy(90);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[2][0] - 1) * TILE_SIZE, (Tunnel_Corners[2][1]) * TILE_SIZE, 0);
				} else if ((withinIslandBoundary(Tunnel_Corners[0][0] - 1, Tunnel_Corners[0][1])
						|| isInStartIsland(new int[] { Tunnel_Corners[0][0] - 1, Tunnel_Corners[0][1] }))
						&& !IsTree(Tunnel_Corners[0][0] - 1, Tunnel_Corners[0][1])) {
					localization.linedetect_move();
					navigation.turnBy(-90);
					localization.linedetect_move();
					odometer.setXYT((Tunnel_Corners[0][0] - 1) * TILE_SIZE, (Tunnel_Corners[0][1]) * TILE_SIZE, 180);
				} else {
					if ((withinIslandBoundary(Tunnel_Corners[2][0] - 2, Tunnel_Corners[2][1])
							|| isInStartIsland(new int[] { Tunnel_Corners[2][0] - 2, Tunnel_Corners[2][1] }))
							&& !IsTree(Tunnel_Corners[2][0] - 2, Tunnel_Corners[2][1])) {
						localization.linedetect_move();
						localization.linedetect_move();
						navigation.turnBy(90);
						localization.linedetect_move();
						odometer.setXYT((Tunnel_Corners[2][0] - 2) * TILE_SIZE, (Tunnel_Corners[2][1]) * TILE_SIZE, 0);
					} else if ((withinIslandBoundary(Tunnel_Corners[0][0] - 2, Tunnel_Corners[0][1])
							|| isInStartIsland(new int[] { Tunnel_Corners[0][0] - 2, Tunnel_Corners[0][1] }))
							&& !IsTree(Tunnel_Corners[0][0] - 2, Tunnel_Corners[0][1])) {
						localization.linedetect_move();
						localization.linedetect_move();
						navigation.turnBy(-90);
						localization.linedetect_move();
						odometer.setXYT((Tunnel_Corners[0][0] - 2) * TILE_SIZE, (Tunnel_Corners[0][1]) * TILE_SIZE,
								180);
					} else {
						odometer.setXYT(Tunnel_Corners[2][0] - 0.5, Tunnel_Corners[2][1] - 0.5, 270);
						tree_obstruct = true;
					}
				}
			}
		}
	}

	/**
	 * This method compute the euclidean distance between two points
	 * 
	 * @param deltaX:
	 *            Difference between the two X coordinates
	 * @param deltaY:
	 *            Difference between the two Y coordinates
	 * @return the distance between the two coordinates
	 */
	public static double getDist(double deltaX, double deltaY) {
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}

	/**
	 * This method is checks if a point with coordinates (x,y) is within the start
	 * island (island in which the robot is positioned at the beginning).
	 * 
	 * @param XY:
	 *            array of two elements that contains the coordinates of the point.
	 * @return boolean: True if the point is within the starting island, false
	 *         otherwise.
	 */
	public static boolean isWithinStartIsland(int XY[]) {
		return (XY[0] <= Start_UR_x) && (XY[0] >= Start_LL_x) && (XY[1] <= Start_UR_y) && (XY[1] >= Start_LL_y);
	}

	/**
	 * This method is checks if a point with coordinates (x,y) is in the starting
	 * island (island in which the robot is positioned at the beginning), excluding
	 * the boundaries.
	 * 
	 * @param XY:
	 *            array of two elements that contains the coordinates of the point.
	 * @return boolean: True if the point is in the starting island, false
	 *         otherwise.
	 */
	public static boolean isInStartIsland(int XY[]) {
		return (XY[0] < Start_UR_x) && (XY[0] > Start_LL_x) && (XY[1] < Start_UR_y) && (XY[1] > Start_LL_y);
	}

	/**
	 * This method check if a point is is in the island. A point is not considered
	 * in the island if it is located in its boudaries.
	 * 
	 * @param x:
	 *            X coordinates of the point
	 * @param y:
	 *            Y coordinates of the point
	 * @return Boolean: true if the point is in the Island, false otherwise
	 */
	public static boolean withinIslandBoundary(double x, double y) {
		return (x <= Island_UR_x - 1) && (x >= Island_LL_x + 1) && (y <= Island_UR_y - 1) && (y >= Island_LL_y + 1)
				&& !IsTree((int) x, (int) y);
	}

	/**
	 * This method checks if a point is in the Island or its boundary and returns a
	 * boolean accordingly.
	 * 
	 * @param x:
	 *            X coordinates of the point.
	 * @param y:
	 *            Y coordinates of the point.
	 * @return Boolean: true if a point is within the Island and its boundaries,
	 *         false otherwise.
	 */
	public static boolean withinIsland(int x, int y) {
		return (x <= Island_UR_x) && (x >= Island_LL_x) && (y <= Island_UR_y) && (y >= Island_LL_y);
	}

	/**
	 * This method checks if a point is the tree and return a boolean accordingly.
	 * 
	 * @param x:
	 *            X coordinates of the point.
	 * @param y:
	 *            Y coordinates of the point.
	 * @return Boolean: true if a point is the tree, false otherwise.
	 */
	public static boolean IsTree(int x, int y) {
		return (x == TR_x) && (y == TR_y);
	}

	/**
	 * This method checks for the tunnels orientation (horizontal or vertical) and
	 * for the direction at which we want to cross it (UP OR DOWN OR LEFT OR RIGHT).
	 * 
	 * @return boolean[]: array that contains two boolean, the first one defines the
	 *         orientation and the second the direction.
	 */
	public static boolean[] getTunnelOrientation() {
		boolean[] ret = new boolean[2];
		Tunnel_Corners[0][0] = TN_LL_x;
		Tunnel_Corners[0][1] = TN_LL_y;
		Tunnel_Corners[1][0] = TN_UR_x;
		Tunnel_Corners[1][1] = TN_LL_y;
		Tunnel_Corners[2][0] = TN_LL_x;
		Tunnel_Corners[2][1] = TN_UR_y;
		Tunnel_Corners[3][0] = TN_UR_x;
		Tunnel_Corners[3][1] = TN_UR_y;
		if (isWithinStartIsland(Tunnel_Corners[0]) && isWithinStartIsland(Tunnel_Corners[1])) { // case tunnel is
																								// vertical and up
			ret[0] = true;
			ret[1] = true;
		} else if (isWithinStartIsland(Tunnel_Corners[2]) && isWithinStartIsland(Tunnel_Corners[3])) {// case tunnel is
																										// vertical and
																										// down
			ret[0] = true;
			ret[1] = false;
		} else if (isWithinStartIsland(Tunnel_Corners[0]) && isWithinStartIsland(Tunnel_Corners[2])) {// case tunnel is
			// horizontal and up
			ret[0] = false;
			ret[1] = true;
		} else if (isWithinStartIsland(Tunnel_Corners[3]) && isWithinStartIsland(Tunnel_Corners[1])) {// case tunnel is
																										// horizontal
																										// and down
			ret[0] = false;
			ret[1] = false;
		}
		return ret;
	}

}
