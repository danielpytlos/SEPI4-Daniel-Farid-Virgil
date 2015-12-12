import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.google.common.base.Stopwatch;

public class Main {

	public static void main(String[] args) {

		String bluetoothDataString = "";
		String tempBluetoothString = "";
		boolean flag = true;
		// double[][] trackData = {{}};
		Stopwatch stopwatch = Stopwatch.createUnstarted();
		int[] raceDataPlan;

		System.out.println("Setting-up the connection");
		SerialConnection serialConnection = new SerialConnection("COM6");

		System.out.println("Learning the track");
		serialConnection.write("L".getBytes());
		serialConnection.read();
		stopwatch.start();

		/* Wait for signal('!') that car stopped sampling the track */
		while (flag) {
			tempBluetoothString = new String(serialConnection.read());
			// System.out.print(tempBluetoothString.replaceAll("\\s+",
			// "").trim());
			if (tempBluetoothString.charAt(0) == '!') {
				flag = false;
			}
		}
		stopwatch.stop();
		System.out.println("\n Learning time: "
				+ stopwatch.elapsed(TimeUnit.MILLISECONDS));
		System.out.println("Receiving the data");
		flag = true;

		/* Reading sampling data until 15 empty readings */
		while (flag) {
			tempBluetoothString = new String(serialConnection.read());
			System.out.println(tempBluetoothString);
			bluetoothDataString += tempBluetoothString;
			if (tempBluetoothString.contains("!")) {
				flag = false;
			}
			/*
			 * if (tempBluetoothString.replaceAll("\\s+", "").trim().equals(""))
			 * { emptyCount++; if (emptyCount > 15) { start = false; } } else {
			 * emptyCount = 0; }
			 */
		}

		/* Splitting the string into single readings */
		String[] bufArr = bluetoothDataString.split("[yzt]");

		System.out.println("Number of readings: " + (bufArr.length - 1)
				+ " \nSampling points: " + (bufArr.length - 1) / 3);

		/* Filtering the received data and inputing it into 2D array */
		final double[][] trackData = new double[((bufArr.length) / 3)][3];
		int parsedInteger = 0;
		int no = 1;
		for (int r = 0; r < trackData.length; r++) {
			for (int c = 0; c < trackData[0].length; c++) {
				try {
					parsedInteger = Integer.parseInt(bufArr[no].replaceAll(
							"[\\s+!]", "").trim());
					trackData[r][c] = (double) parsedInteger;
					no++;
				} catch (Exception e) {
					String temp = "";
					for (int j = 0; j < bufArr[no].length(); j++) {
						if ((int) bufArr[no].charAt(j) != 0) {
							temp += bufArr[no].charAt(j);
						}
					}
					if (!temp.equals("")) {
						trackData[r][c] = (double) Integer.parseInt(temp
								.replaceAll("[\\s+!]", "").trim());
					}
					no++;
				}
			}
		}

		/* Converting from raw data to actual readings */
		for (int i = 0; i < trackData.length; i++) {
			trackData[i][0] = trackData[i][0] / 16384;
			trackData[i][1] = trackData[i][1] / 65.5;
		}

		/* Write data to .csv file for easier analysis in MS Excel */
		writeDataToFile(trackData);

		/* Analyze the data- detect the straights, curves and make a race plan */
		raceDataPlan = calculateRacePlan(trackData);

		try {
			Thread.sleep(500);
		} catch (Exception e) {
		}

		/* Give signal for sending the data */
		serialConnection.write("S".getBytes());

		/* Send the race data plan */
		System.out.println("Sending data");
		for (int i = 0; i < raceDataPlan.length; i++) {
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}
			serialConnection.write((raceDataPlan[i] + "!").getBytes());
		}

		/* Give signal that we stopped sending the data */
		serialConnection.write("s".getBytes());
		serialConnection.read();
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}

		/* Start racing */
		//serialConnection.write("R".getBytes());
		//serialConnection.read();
		System.out.println("START!");
		serialConnection.write("s".getBytes());
		serialConnection.read();

		serialConnection.close();
		System.out.println("Connection closed \nDisplaying track data");

		/* Display the graph with data */
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JChartGraph(trackData, raceDataPlan).setVisible(true);
			}
		});
	}

	public static void writeDataToFile(double[][] trackData) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("viacardump.csv"));
			for (int row = 0; row < trackData.length; row++) {
				for (int col = 0; col < trackData[0].length; col++) {
					writer.write(trackData[row][col] + ",");
				}
				writer.newLine();
			}
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Writing exception");
		}

	}

	public static int[] calculateRacePlan(double[][] trackData) {
		final double NORMAL_CURVE_BREAKING_POINT_CONSTANT = 0.8;
		final double SHARP_CURVE_BREAKING_POINT_CONSTANT = 0.65;

		int raceCounter = 0;
		int breakPointsCounter = 0;
		int cumulativeTacho = 0;
		int[] raceData;
		int[] breakPoints = new int[350];
		double[] accY = new double[trackData.length];
		double[] gyroZ = new double[trackData.length];
		int[] tacho = new int[trackData.length];

		/* Store data from 2D array into separate 1D arrays */
		for (int i = 0; i < trackData.length; i++) {
			accY[i] = trackData[i][0];
			gyroZ[i] = trackData[i][1];
			cumulativeTacho += trackData[i][2];
			tacho[i] = cumulativeTacho;
		}

		/* Make an array that stores race plan- tacho count and speed */
		raceData = new int[tacho.length * 2];
		raceData[0] = 0;
		tacho[0] = 0;

		/*
		 * Detect when the car is rotating(gyroscope reading > 100) and then set
		 * the motor speed of 65
		 */
		for (int i = 0; i < tacho.length; i++) {
			if (Math.abs(gyroZ[i]) < 100) {
				raceData[raceCounter + 1] = 85;
				raceData[raceCounter] = tacho[i];
			} else {
				raceData[raceCounter + 1] = 65;
				raceData[raceCounter] = tacho[i];
			}
			raceCounter = raceCounter + 2;
		}

		/* Initialize the variables for curve and straight detection */
		int[] straightStart = { 0, 0 };
		int[] straightEnd = { 0, 0 };
		int[] curveEnd = { 0, 0 };

		int straightLength = 0;
		int curveLength = 0;
		int breakPoint = 0;

		boolean isStraight = false;

		/* Algorithm for calculating the break points */
		for (int i = 31; i < raceData.length; i += 2) {
			if (raceData[i] >= 85 && !isStraight) {
				straightStart[0] = raceData[i - 3];
				straightStart[1] = i;
				isStraight = true;
			}
			if (isStraight && raceData[i] < 85) {
				straightEnd[0] = raceData[i - 3];
				straightEnd[1] = i;
				isStraight = false;

				straightLength = straightEnd[0] - straightStart[0];

				/*
				 * Calculate breaking point if straight is longer than 10 tacho
				 * count, otherwise ignore.
				 */
				if (straightLength > 10) {
					/* Detect end of the curve */
					for (int j = i; j < raceData.length; j += 2) {
						if (raceData[j] >= 85) {
							curveEnd[0] = raceData[j - 3];
							curveEnd[1] = j - 3;
							break;
						}
					}
					curveLength = curveEnd[0] - straightEnd[0];

					/*
					 * Calculate the point when to start breaking depending on
					 * the curve: If curve ahead is less than 40 tacho counts
					 * then curve is sharp, and we have to apply the break
					 * faster, otherwise apply the standard break constant.
					 * 
					 * TODO: Upgrade the algorithm to consider also the straight
					 * length
					 */
					if (curveLength < 40 && straightLength > 40) {
						breakPoint = (int) (straightStart[0] + (straightLength * SHARP_CURVE_BREAKING_POINT_CONSTANT));
					} else {
						breakPoint = (int) (straightStart[0] + (straightLength * NORMAL_CURVE_BREAKING_POINT_CONSTANT));
					}

					System.out.println("Straight: " + straightStart[0] + " to "
							+ straightEnd[0] + " Length: " + straightLength
							+ " Break point: " + breakPoint);
					System.out.println("Curve: " + straightEnd[0] + " to "
							+ curveEnd[0] + " Length: " + curveLength);

					/*
					 * Calculate breaking points from breakPoint to the middle
					 * of the curve and add them to array.
					 */
					for (int j = straightStart[1]; j <= straightEnd[1]
							+ ((curveEnd[1] - straightEnd[1]) / 2); j += 2) {
						if (raceData[j - 1] > breakPoint) {
							breakPoints[breakPointsCounter] = j;
							breakPointsCounter++;
						}
					}
				}

				/* Reset the variables */
				curveEnd[0] = 0;
				curveEnd[1] = 0;
				curveLength = 0;
				straightLength = 0;
				straightEnd[0] = 0;
				straightEnd[1] = 0;
				straightStart[0] = 0;
				straightStart[1] = 0;
			}
		}
		for (int k = 0; k < breakPoints.length; k++) {
			raceData[breakPoints[k]] = 200;
		}
		raceData[0] = 0;
		for (int item : raceData) {
			System.out.print(item + ", ");
		}
		System.out.println();
		for (int k = 0; k < breakPoints.length; k++) {
			System.out.print(raceData[breakPoints[k] + 1] + ", ");
		}
		return raceData;
	}
}

/* Data used for testing without the need of running the car */
/*
 * double[] gyroZ = {0.41221374, 3.832061069, 7.312977099, 9.58778626,
 * 12.73282443, 1.328244275, 20.16793893, -3.358778626, 13.48091603,
 * -4.305343511, 2.870229008, 12.91603053, 2.213740458, -13.49618321,
 * -4.442748092, 13.8778626, 10.88549618, 1.175572519, 1.938931298,
 * -28.77862595, -152.5038168, -235.8015267, -273.7251908, -227.4198473,
 * -233.9847328, -227.0381679, -210.7328244, -187.9847328, -219.0076336,
 * -223.1145038, -216.0152672, -211.8931298, -223.4198473, -209.3435115,
 * -193.8320611, -137.740458, 8.717557252, 91.81679389, 143.221374, 181.129771,
 * 187.1908397, 200.5648855, 193.4503817, 225.1908397, 208.9312977, 186.1832061,
 * 180.5496183, 198.2748092, 195.8167939, 198.5954198, 186.870229, 208.9770992,
 * 202.3053435, 206.9618321, 121.3129771, -80.29007634, -180.5038168,
 * -255.7709924, -285.2366412, -240.7022901, -223.1145038, -199.2061069,
 * -220.9007634, -231.0687023, -221.4198473, -227.2671756, -228.2442748,
 * -229.6335878, -197.3129771, -206.0305344, -52.04580153, 64.96183206,
 * 112.3664122, 153.3740458, 180.5038168, 48.97709924, -111.5725191,
 * -207.648855, -218.9770992, -44.30534351, 72.70229008, 131.0076336,
 * 167.4351145, 178.5801527, -20.38167939, -126.7938931, -178, -206.7328244,
 * -95.8778626, -52.7480916, -16.58015267, -6.366412214, -11.49618321,
 * 0.671755725, 3.511450382, -8.671755725, -10.61068702, -4.732824427,
 * -0.702290076, -8.885496183, -9.847328244, 0.625954198, 2.977099237,
 * 8.839694656, 6.091603053, 7.358778626, 12.65648855, 13.96946565,
 * -1.740458015, -11.4351145, -7.694656489, -9.633587786, -3.465648855,
 * 9.06870229, 4.870229008, -5.145038168, -10.32061069, 9.832061069,
 * -8.610687023, -8.41221374, 6.763358779, -11.49618321, 4.13740458,
 * -1.267175573, 18.64122137, -11.49618321, 16.70229008, -3.389312977,
 * 13.3740458, -1.450381679, -7.526717557, -6.320610687, 7.526717557,
 * -1.755725191, -274.6412214, -452.7480916, -460.5343511, -311.5267176,
 * -217.2519084, -301.5419847, -326.2290076, -276.610687, -275.3282443,
 * -277.9847328, -251.1755725, -153.2061069, -63.90839695, -27.81679389,
 * -16.54961832, -6.045801527, -6.732824427, -10.01526718, -1.129770992,
 * -2.458015267, 2.885496183, 6.290076336, 9.969465649, 2.748091603,
 * -7.312977099, -13.4351145, -5.526717557, 165.2824427, 241.5114504,
 * 263.5572519, 253.648855, 246.9465649, 265.8167939, 253.4351145, 246.6564885,
 * 230.5343511, 230.0610687, 226.259542, 213.8015267, 194.1068702, 206.3358779,
 * 225.2366412, -44.41221374, -194.2900763, -267.1908397, -293.9694656,
 * -256.6717557, -243.3435115, -210.6259542, -242.7022901, -235.129771,
 * -232.9923664, -239.5572519, -246.7480916, -236.4580153, -223.0534351,
 * -122.3206107, -63.63358779, -31.51145038, -15.17557252, -24.77862595,
 * -21.86259542, -15.80152672, 2.519083969, 6.229007634, 4.091603053,
 * -0.152671756, -10.03053435, -6.503816794, -5.06870229, 6.213740458,
 * 6.366412214, 7.541984733, -6.13740458, -13.35877863, -0.885496183,
 * 16.6870229, -199.7709924, -336.1679389, -352.2137405, -289.3435115,
 * -254.351145, -230.4274809, -73.41984733, -29.03816794, -20.07633588,
 * -8.824427481, 10.61068702, 4.488549618, 24.45801527, 12.22900763,
 * 6.992366412, 2.305343511, -18.25954198, 0.564885496, 10.65648855,
 * 2.900763359, -9.847328244, -1.755725191, 16.50381679, -4.885496183,
 * -20.09160305, 11.32824427, 3.267175573, -11.14503817, 43.70992366,
 * 240.4122137, 298.1679389, 260.1526718, 260.6870229, 250.3664122, 241.4045802,
 * 162.5496183, 37.26717557, 24.27480916, -1.099236641, -20.67175573, -14,
 * 124.8091603, 214.5954198, 241.0534351, 231.221374, 241.4503817, 246.870229,
 * 219.3282443, 215.1755725, 77.52671756, 44.04580153, 41.34351145, 10.91603053,
 * 20.1221374, 4.091603053, -10.19847328, -9.389312977, 1.938931298,
 * 9.114503817, 6.717557252, -16.5648855, -5.633587786, 18.39694656,
 * 264.4732824, 332.8091603, 274.5801527, 291.5267176, 255.4045802, 232.4580153,
 * -142.3358779, -309.1450382, -375.1908397, -330.4122137, -246.8854962,
 * -139.4503817, 49.22137405, 144.0916031, 193.2824427, 190.0458015,
 * -84.16793893, -181.8473282, -259.9694656, -281.9847328, -233.4961832,
 * -218.870229, -242.0610687, -221.6946565, -213.9083969, -210.0305344,
 * -207.5419847, -107.9083969, -49.3129771, -21.40458015, -3.740458015,
 * 0.366412214, -7.358778626, -18.87022901, -12.6259542, -18.6870229,
 * -17.78625954, -0.534351145, -14.90076336, 5.358778626, 10.79389313,
 * 18.97709924, -2.198473282, -156.5954198, -270.5954198, -364.7022901,
 * -288.1679389, -244.1984733, -280.4427481, -241.2824427, -271.4656489,
 * -249.1908397, -256.1679389, -240.7022901, -238.4580153, -235.2671756,
 * -193.9389313, 2.473282443, 83.83206107, 152.778626, 184.4122137, 185.1755725,
 * 205.3587786, 189.6183206, 209.5877863, 205.5267176, 193.8473282, 202.519084,
 * 203.0687023, 210.1068702, 204.5343511, 210.7633588, 206.610687, 194.4427481,
 * 219.7862595, -33.3740458}; int[] tachoArr = {0, 0, 0, 1, 2, 4, 5, 7, 10, 12,
 * 15, 17, 20, 24, 27, 30, 34, 37, 41, 45, 48, 52, 56, 59, 63, 66, 69, 72, 76,
 * 79, 82, 85, 88, 91, 94, 97, 100, 103, 107, 110, 114, 117, 121, 124, 128, 131,
 * 135, 139, 142, 146, 150, 153, 157, 161, 164, 168, 172, 175, 179, 183, 186,
 * 189, 193, 196, 199, 203, 206, 209, 212, 215, 218, 221, 225, 228, 231, 234,
 * 238, 241, 244, 248, 251, 254, 258, 261, 264, 268, 271, 274, 277, 280, 283,
 * 285, 288, 291, 294, 296, 299, 302, 305, 308, 311, 315, 319, 322, 326, 330,
 * 334, 339, 343, 347, 352, 356, 361, 366, 371, 375, 381, 386, 391, 397, 403,
 * 409, 414, 421, 427, 433, 439, 445, 451, 457, 463, 469, 475, 481, 487, 493,
 * 498, 504, 508, 513, 517, 522, 526, 530, 534, 537, 541, 545, 548, 552, 556,
 * 560, 564, 569, 573, 577, 582, 586, 591, 596, 601, 605, 610, 615, 620, 624,
 * 629, 633, 638, 642, 646, 651, 655, 659, 663, 667, 671, 675, 679, 683, 687,
 * 691, 694, 698, 701, 705, 708, 712, 715, 719, 722, 725, 729, 732, 735, 739,
 * 743, 747, 751, 755, 759, 763, 768, 772, 777, 781, 786, 791, 796, 801, 805,
 * 810, 815, 820, 825, 829, 833, 837, 841, 845, 849, 853, 857, 862, 866, 870,
 * 875, 880, 885, 890, 894, 900, 905, 910, 915, 920, 926, 931, 936, 942, 947,
 * 952, 957, 962, 967, 971, 975, 979, 983, 987, 991, 995, 1000, 1004, 1008,
 * 1013, 1017, 1021, 1025, 1030, 1034, 1038, 1042, 1046, 1050, 1055, 1059, 1064,
 * 1069, 1074, 1079, 1084, 1089, 1095, 1101, 1106, 1112, 1117, 1122, 1127, 1132,
 * 1137, 1141, 1146, 1150, 1154, 1158, 1162, 1166, 1170, 1173, 1177, 1181, 1185,
 * 1189, 1192, 1196, 1199, 1202, 1205, 1208, 1212, 1215, 1218, 1221, 1225, 1228,
 * 1232, 1236, 1240, 1244, 1248, 1253, 1257, 1262, 1266, 1271, 1276, 1280, 1285,
 * 1290, 1294, 1298, 1301, 1305, 1309, 1313, 1316, 1320, 1323, 1326, 1330, 1333,
 * 1337, 1340, 1344, 1347, 1351, 1355, 1359, 1362, 1366, 1370, 1374, 1378, 1381,
 * 1385, 1389, 1393, 1397};
 */
