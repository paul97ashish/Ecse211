package ca.mcgill.ecse211.wallfollowing;

public interface UltrasonicController {

  public void processUSData(int distance) throws InterruptedException;

  public int readUSDistance();
}
