package ca.mcgill.ecse211.Test;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * We used this test for ring detection
 * The sensor detects the RGB values and tries to match it to a color of the ring.
 * If it finds a match it displays the number according to ring 
 * 0 - blue, 1 - green, 2 - yellow, 3 - orange
 * @author Ashish
 *
 */
 class ColorDetectionTest {
	static Port portColor = LocalEV3.get().getPort("S1");
	static EV3ColorSensor colorSensor = new EV3ColorSensor(portColor);
	static float[]sampleColor;
	static SampleProvider colorValue;
	private static final TextLCD lcd = LocalEV3.get().getTextLCD();
	// Blue, Green, Yellow, Orange
//	private static final float[] [] Meann= {{0.1385345f,0.680827f,0.7192239f},
//											{0.4176605f, 0.8939716f,0.1624025f},
//											{0.8397597f,0.5311634f,0.1125575f},
//											{ 0.9761509f,0.1892537f,0.1063603f}};
//	private static final float[] [] StdDevn= {{0.3956478f,0.758946f,0.5171691f},
//												{0.487909f,0.751976f,0.4432559f},
//												{0.7748833f,0.5722075f,0.2685785f},
//												{ 0.9628929f,0.2225449f,0.1526793f}};
//	private static final float[] [] Mean= {{0.1385345f,0.680827f,0.7192239f},
//											{0.4176605f, 0.8939716f,0.1624025f},
//											{0.8397597f,0.5311634f,0.1125575f},
//												{ 0.9761509f,0.1892537f,0.1063603f}};
//private static final float[] [] StdDev= {{0.3956478f,0.758946f,0.5171691f},
//				{0.487909f,0.751976f,0.4432559f},
//				{0.7748833f,0.5722075f,0.2685785f},
//				{ 0.9628929f,0.2225449f,0.1526793f}};
private static final float[] [] Meann= {{0.170390332f,0.767597595f,0.617868163f},
										{0.402231815f,0.906190081f,0.13049561f},
										{0.832694447f,0.538629888f,0.128443766f},
										{ 0.953786617f,0.290982684f,0.074967764f}};
//private static final float[] [] StdDevn= {{0.573214593f,0.606117356f,	0.551404371f},
//											{0.584991691f,0.583430374f,0.563377067f},
//											{0.584095753f,0.585842632f,0.561801176f},
//											{ 0.58910641f,0.579596568f,0.563046585f}};

	public static void main (String [] args) {
	colorSensor.setFloodlight(lejos.robotics.Color.WHITE);
		colorValue = colorSensor.getMode("RGB"); 
		sampleColor = new float[colorValue.sampleSize()];
		while(true) {
		Button.waitForAnyPress();
		lcd.clear();
		int status;
		
		do {
			status=findMatch( fetch());
		}while(status==5);
		
		String[] str= {"Blue", "Green", "Yellow", "Orange"};
		lcd.drawString("Object Detected ", 0, 0);
		lcd.drawString(str[status], 0, 1);
		
		}
	}

//	public ColorDetection() {
//		colorSensor.setFloodlight(lejos.robotics.Color.WHITE);
//		colorValue = colorSensor.getMode("RGB"); 
//		sampleColor = new float[colorValue.sampleSize()];
//		
//	}
	public int detect() {
		int color;
		do {
			color=findMatch(fetch());
			
		}while(color==5);
		return color;
	}
	public static float[] fetch() {
		colorValue.fetchSample(sampleColor, 0);
		return sampleColor;
	}
	public static int findMatch(float array[]) {
	//	System.out.println(array[0]+ "    "+array[1]+ "    "+array[2]+ "    ");
		float euc=(float)Math.sqrt((Math.pow(array[0], 2)+Math.pow(array[1], 2)+Math.pow(array[2], 2)));
		float R=array[0]/euc;
		float G=array[1]/euc;
		float B=array[2]/euc;
		for (int i=0; i<4; i++) {
			float differenceR=Math.abs(R-Meann[i][0])/0.1f;
			float differenceG=Math.abs(G-Meann[i][1])/0.1f;
			float differenceB=Math.abs(B-Meann[i][2])/0.1f;
			if (differenceR<1.0  &&differenceG<1.0 && differenceB<1.0) {
				return i;
			}
		}
		
		return 5;
	}
}
