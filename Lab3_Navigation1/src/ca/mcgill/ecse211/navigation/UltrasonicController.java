package ca.mcgill.ecse211.navigation;

public interface UltrasonicController {

	  public void processUSData(int distance) ;

	  public int readUSDistance();
	}
