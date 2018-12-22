package ca.mcgill.ecse211.project;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
/**
 * This class is used to acquire values from the light sensor and detect lines.
 * @author Zakaria Essadaoui
 *
 */
public class LightPoller {
	
	private SensorModes myColor;
	private SampleProvider myColorSample;
	private float[] sampleColor;
	private int lastData;
	private int newData;
	private static final double THREESHOLD=-50;
	private static int baseCase;
	private static boolean First_Line=true;
	/**
	 * Constructor for this class. It initiates the sensor and sample provider.
	 * @param portColor: port of the light sensor
	 */
	public LightPoller(Port portColor) {
		 myColor = new EV3ColorSensor(portColor);
		 myColorSample = myColor.getMode("Red");
		 sampleColor = new float[myColor.sampleSize()];
		 
	}
	/**
	 * This method detects if a line was crossed 
	 * @param first: boolean that determines whether the method was called for the first time 
	 * @return boolean for whether the line was crossed
	 */
	public boolean line_detected(boolean first) {
		if(first) {
			lastData=fetch();
			
		}
		newData=fetch();
		
			if((newData-lastData)<THREESHOLD ) {
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(fetch()-lastData<THREESHOLD) {
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(fetch()-lastData<THREESHOLD) {
					lastData=newData;
					return true;
					}
				}else {
					lastData=newData;
					return false;
				}
					
					/*lastData=newData;
					double ratio=(double)(newData-baseCase)/baseCase;
					if(!First_Line && ratio <0.2 && ratio >-0.2) {
					return true;
					}*/
				}
		
		lastData=newData;
		return false;
	}
	
	/**
	 * This method is used to get data from the light sensor
	 * @return data from light sensor
	 * @throws InterruptedException
	 */
	private int fetch() {
		int sensor_data;
		myColorSample.fetchSample(sampleColor, 0);
		sensor_data=(int) (sampleColor[0] * 1000);
		
		return sensor_data;
	}
}
