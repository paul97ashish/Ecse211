package ca.mcgill.ecse211.wallfollowing;


import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {

  private final int bandCenter;
  private final int bandwidth;
  private final int motorLow;
  private final int motorHigh;
  private int distance;
  private int error;
  private static final int FILTER_OUT = 10;
  private int filterControl;
  public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
    // Default Constructor
    this.bandCenter = bandCenter;
    this.bandwidth = bandwidth;
    this.motorLow = motorLow;
    this.motorHigh = motorHigh;
    this.filterControl=0;
    WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving forward
    WallFollowingLab.rightMotor.setSpeed(motorHigh);
    WallFollowingLab.leftMotor.forward();
    WallFollowingLab.rightMotor.forward();
  }

  @Override
  public void processUSData(int distance)   {
	  if (distance >= 255 && filterControl < FILTER_OUT) {
	      // bad value, do not set the distance var, however do increment the
	      // filter value
	      filterControl++;
	    } else if (distance >= 255) {
	      // We have repeated large values, so there must actually be nothing
	      // there: leave the distance alone
	      this.distance = distance;
	    } else {
	      // distance went below 255: reset filter and leave
	      // distance alone.
	      filterControl = 0;
	      this.distance = distance;
	    }
	 
	 //  this.distance = distance;
    this.error=this.bandCenter-this.distance; 
    if (Math.abs(error)<=this.bandwidth) {
    	// the robot is within the limit
    	// continue straight
    	 WallFollowingLab.leftMotor.setSpeed(motorHigh); 
    	    WallFollowingLab.rightMotor.setSpeed(motorHigh);
    	    WallFollowingLab.leftMotor.forward();
    	    WallFollowingLab.rightMotor.forward();
    }else if (this.distance<15) {
    	// too close to the wall
    	// Immediately turn right
    	int speed=2*motorHigh;
    	WallFollowingLab.leftMotor.setSpeed(speed); 
 	    WallFollowingLab.rightMotor.setSpeed(motorLow);
 	   WallFollowingLab.leftMotor.forward();
 	    WallFollowingLab.rightMotor.backward();
    }  else if (this.error>0) {
    	// close to the wall 
    	// does a normal right turn
    	WallFollowingLab.leftMotor.setSpeed(2*motorHigh+motorLow); 
	    WallFollowingLab.rightMotor.setSpeed(motorLow);
	    WallFollowingLab.leftMotor.forward();
	    WallFollowingLab.rightMotor.forward();
    }else if (this.distance>40){
    	WallFollowingLab.leftMotor.setSpeed(150); 
 	    WallFollowingLab.rightMotor.setSpeed(270);
 	    WallFollowingLab.leftMotor.forward();
 	    WallFollowingLab.rightMotor.forward();
    	
    }else {
    	 // far from the wall
    	// turns left 
    	WallFollowingLab.leftMotor.setSpeed(motorLow); 
	    WallFollowingLab.rightMotor.setSpeed(motorHigh);
	    WallFollowingLab.leftMotor.forward();
	    WallFollowingLab.rightMotor.forward();
    }
 
    // TODO: process a movement based on the us distance passed in (BANG-BANG style)
  }

  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
