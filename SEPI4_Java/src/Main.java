import dk.thibaut.serial.SerialPort;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class Main {

	public static void main(String[] args) {

		String buf = "";
		String buf2 = "";
		Double[][] trackData;
		ArrayList<Double> xArr = new ArrayList<Double>();
		ArrayList<Double> yArr = new ArrayList<Double>();
		Stopwatch stopwatch = Stopwatch.createUnstarted();
		int[] testData;

		List<String> availablePorts = SerialPort.getAvailablePortsNames();

		for (String availablePort : availablePorts) {
			System.out.println(availablePort + " is available");
		}

		System.out.println("-------------------------------------");
		testData = analyzeData();
		/*SerialConnection serialConnection = new SerialConnection("COM6");
		for (int i = 0; i < testData.length; i++) {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
			serialConnection.write((testData[i] +"!").getBytes());
			//serialConnection.write("N".getBytes());
			//System.out.println(new String(serialConnection.read()));
		}
		System.out.println("START!");
		serialConnection.write("P".getBytes());
		serialConnection.read();
		serialConnection.close();*/
		/*try {
			System.out.println("Runing");
			serialConnection.write("D".getBytes());
			serialConnection.read();
			stopwatch.start();
			for (int i = 0; i < 145; i++) {
				try {
					//Thread.sleep(100);
				} catch (Exception e) {
				}
				serialConnection.write("F".getBytes());
				buf2 = new String(serialConnection.read());
				System.out.println(buf2);
				buf += buf2;

				buf2 = new String(serialConnection.read());
				System.out.println(buf2);
				buf += buf2;

			}
			buf2 = new String(serialConnection.read());
			System.out.println(buf2);
			buf += buf2;
			stopwatch.stop();
			serialConnection.write("d".getBytes());
			serialConnection.read();
			serialConnection.write("N".getBytes());
			System.out.println("nextVal: " + new String(serialConnection.read()));
			serialConnection.close();

			System.out.println("-------------------------------------");
			int l = 0;
			String[] bufArr = buf.split("[xyzrqt]");
			trackData = new Double[((bufArr.length) / 6)][6];
			System.out.println(bufArr.length);
			System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS));
			//System.out.println(Arrays.toString(bufArr));
			int no=1;
			for (int r = 0; r < trackData.length; r++) {
				for (int c = 0; c < trackData[0].length; c++) {
					try {
						l = Integer.parseInt(bufArr[no] .replaceAll("\\s+", "").trim());
						//System.out.println(String.format("%.2f", l));
						trackData[r][c] = (double) l;
						no++;
					} catch (Exception e) {
						//System.out.println("WRONG VALUE: ");
						String temp = "";
						for (int j = 0; j < bufArr[no].length(); j++) {
							if ((int) bufArr[no].charAt(j) != 0) {
								temp += bufArr[no].charAt(j);
							}
						}
						if (!temp.equals("")) {
							//System.out.println(temp);
							trackData[r][c] = (double) Integer.parseInt(temp.replaceAll("\\s+", "").trim());
						}
						no++;
					}
				}
			}
			for(int i = 0; i < trackData.length; i++){
				trackData[i][0] = trackData[i][0]/16384;
				trackData[i][1] = trackData[i][1]/16384;
				//trackData[i][2] = trackData[i][2]/16384;
				trackData[i][3] = trackData[i][3]/65.5;
				trackData[i][4] = trackData[i][4]/65.5;
			}
			//System.out.println(Arrays.deepToString(trackData));
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"viacardump.csv"));
			for (int row = 0; row < trackData.length; row++) {
				for (int col = 0; col < trackData[0].length; col++) {
					out.write(trackData[row][col] + ",");
				}
				out.newLine();
			}
			out.write("First " + "\n");
			out.newLine();
			out.close();
			
			double theta,xnew,ynew;
			for(int iter = 27; iter <= 98; iter++){
				theta = Math.atan2(trackData[iter][1], trackData[iter][4]);
				xnew = (trackData[iter][4]*50*0.006) * Math.cos(theta) - (trackData[iter][1]*50) * Math.sin(theta);
				xArr.add(xnew);
				ynew = (trackData[iter][4]*50*0.006) * Math.sin(theta) + (trackData[iter][1]*50) * Math.cos(theta);
				yArr.add(ynew);
			}
			
			for (int x = 0; x < xArr.size(); x++) {
				System.out.print(xArr.get(x) + ", ");
			}
			System.out.println("\n");
			for (int x = 0; x < yArr.size(); x++) {
				System.out.print(yArr.get(x) + ", ");
			}
			
		} catch (IOException e) {
		}*/

	}
	
	public static int[] analyzeData() {
		int raceCounter = 0;
		int[] raceData= new int[260];
		double[] accY = {-0.01025390625, 0.041748047, 0.219970703, 0.255859375, -0.379150391, -0.657714844 -0.506347656, 0.037841797, 0.594482422, 0.439697266, 0.414794922, 0.552001953, -0.225341797, 0.314697266, -0.455322266, 0.404785156, -0.009765625, 0.080566406, 0.044433594, 0.034667969, -0.047119141, -0.153320313, 0.167724609, -0.067138672, 0.2734375, 0.500976563, 1.016113281, 0.508544922, 0.671386719, 0.184570313, -0.066650391, -0.155273438, 0.396240234, -1.211669922, -0.611083984, 0.380615234, -0.004882813, -0.055419922, 0.834472656, 0.782958984, 0.833251953, 0.537597656, 0.0703125, 0.118896484, -0.169921875, -0.151367188, 0.099365234, 0.143066406, 0.998291016, -0.153076172, -0.210693359, -0.004882813, 0.220947266, 0.150390625, -0.554931641, -0.320556641, -0.539794922, -0.728027344, 0.071533203, 0.043701172, 0.065917969, 0.289306641, -1.342773438, 0.534667969, 0.042236328, -0.036621094, 0.536132813, 0.335693359, 0.191650391, 0.280761719, 0.067138672, 0.054199219, 0.500732422, 0.978515625, 0.374511719, 0.418701172, -0.177490234, -0.740478516, -0.683349609, -0.655029297, 0.508544922, 0.647949219, 0.410400391, 0.437744141, -0.245605469, 0.297851563, -0.416015625, 0.299804688, 0.055664063, 0.188720703, 0.125488281, 0.046630859, 0.041748047, -0.120361328, 0.635009766, 0.1171875, 0.077880859, 0.103271484, 1.155029297, 0.561035156, 0.757324219, 0.014648438, 0.185302734, 0.133056641, -0.860107422, -0.651611328, -0.807128906, -0.204101563, -0.022216797, 0.514404297, 0.618652344, 0.649902344, 0.666748047, 0.299560547, 0.242431641, 0.104003906, 0.027099609, 0.042480469, -0.055419922, 1.104736328, 0.612304688, 0.181884766, 0.010009766, 0.001220703, 0.059082031, -0.659179688, -0.587158203, 0.146240234, -0.487792969, -0.577636719, 0.131103516, -0.06640625, 0.025634766, -1.334960938, 0.674804688, 0.686035156, -0.268554688, 0.050048828, 0.480224609, 0.397216797, 0.053955078, 0.198974609, -0.015136719, 0.012207031, 0.607666016,};
		int[] tachoArr ={3, 11, 18, 31, 46, 61, 76, 91, 107, 118, 134, 150, 166, 182, 198, 210, 225, 239, 255, 273, 293, 314, 338, 365, 386, 414, 441, 464, 483, 497, 516, 535, 557, 579, 598, 614, 624, 641, 660, 678, 694, 711, 727, 745, 760, 788, 806, 831, 854, 875, 895, 911, 934, 958, 983, 1003, 1029, 1039, 1056, 1071, 1091, 1113, 1137, 1158, 1177, 1190, 1207, 1221, 1235, 1248, 1265, 1284, 1305, 1320, 1339, 1357, 1373, 1390, 1402, 1419, 1434, 1450, 1466, 1482, 1494, 1511, 1527, 1543, 1558, 1572, 1589, 1603, 1623, 1645, 1670, 1697, 1724, 1753, 1780, 1796, 1816, 1834, 1854, 1875, 1897, 1919, 1932, 1949, 1964, 1984, 2003, 2020, 2034, 2049, 2067, 2086, 2107, 2131, 2156, 2175, 2202, 2218, 2239, 2262, 2286, 2311, 2333, 2351, 2365, 2384, 2403, 2423, 2445, 2464, 2486, 2505, 2523, 2540, 2556, 2566, 2580, 2597, 2616, 2636, 2658};
		//int cumulativeTacho = 0;
		int speed = 0;
		for (int i = 1; i < accY.length; i++) {
			accY[i] = (accY[i-1]+(0.8*(accY[i]-accY[i-1])))+2;
			//accY[i]=accY[i]+2;
			//System.out.println(accY[i]);
		}
		for (int r = 1; r < accY.length; r++) {
			//cumulativeTacho = cumulativeTacho + trackData[r][5].intValue();
			if(accY[r]<1.5||accY[r]>2.5) {
				speed = 0;
				raceData[raceCounter] = tachoArr[r];
				raceData[raceCounter+1] = speed;
				raceCounter = raceCounter+2;
			}
		}
		for (int item : raceData) {   
		    System.out.println(item + ", ");
		}
		return raceData;
	}
}
