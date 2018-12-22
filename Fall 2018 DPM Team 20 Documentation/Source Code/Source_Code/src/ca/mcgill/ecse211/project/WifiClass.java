package ca.mcgill.ecse211.project;

import java.util.Map;

import ca.mcgill.ecse211.WiFiClient.WifiConnection;

/**
 * This class communicates with the server to get the run's parameter. 
 * @author Zakaria Essadaoui
 *
 */
public class WifiClass {

		  // Enable/disable printing of debug info from the WiFi class
		  private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;

		  @SuppressWarnings("rawtypes")
		  /**
		   * This method return get data from the server and returns it.
		   * @param SERVER_IP : IP address of the server
		   * @param TEAM_NUMBER 
		   * @return Map: contains all the parameters of the run with their name as key
		   */
		  public static Map getMap(String SERVER_IP, int TEAM_NUMBER) {

		    System.out.println("Running..");

		    // Initialize WifiConnection class
		    WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);

		    // Connect to server and get the data, catching any errors that might occur
		    try {
		      /*
		       * getData() will connect to the server and wait until the user/TA presses the "Start" button
		       * in the GUI on their laptop with the data filled in. Once it's waiting, you can kill it by
		       * pressing the upper left hand corner button (back/escape) on the EV3. getData() will throw
		       * exceptions if it can't connect to the server (e.g. wrong IP address, server not running on
		       * laptop, not connected to WiFi router, etc.). It will also throw an exception if it connects
		       * but receives corrupted data or a message from the server saying something went wrong. For
		       * example, if TEAM_NUMBER is set to 1 above but the server expects teams 17 and 5, this robot
		       * will receive a message saying an invalid team number was specified and getData() will throw
		       * an exception letting you know.
		       */
		      Map data = conn.getData();
		      return data;

		    } catch (Exception e) {
		      System.err.println("Error: " + e.getMessage());
		    }
		    return null;
		  
	}
	

}
