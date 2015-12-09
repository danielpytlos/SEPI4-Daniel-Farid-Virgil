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
		boolean start = true;
		double[][] trackData = null;
		ArrayList<Double> xArr = new ArrayList<Double>();
		ArrayList<Double> yArr = new ArrayList<Double>();
		Stopwatch stopwatch = Stopwatch.createUnstarted();
		int[] testData;

		List<String> availablePorts = SerialPort.getAvailablePortsNames();

		for (String availablePort : availablePorts) {
			System.out.println(availablePort + " is available");
		}

		System.out.println("-------------------------------------");
		//analyzeData(trackData);
		SerialConnection serialConnection = new SerialConnection("COM6");

		try {
			System.out.println("Runing");
			//serialConnection.write("D".getBytes());
			//serialConnection.read();
			stopwatch.start();
			serialConnection.write("L".getBytes());
			serialConnection.read();
			while(start) {
				buf2 = new String(serialConnection.read());
				System.out.print(buf2.replaceAll("\\s+", "").trim());
				if(buf2.charAt(0) == '!') {
					start = false;
				}
			}
			stopwatch.stop();
			System.out.println("\n" + stopwatch.elapsed(TimeUnit.MILLISECONDS));
			System.out.println("Getting Data");
			int emptyCount=0;
			start = true;
			while(start) {
				buf2 = new String(serialConnection.read());
				System.out.println(buf2);
				buf += buf2;
				if(buf2.replaceAll("\\s+", "").trim().equals("")) {
					emptyCount++;
					if(emptyCount > 20) {
						start = false;
					}
				}else {
					emptyCount=0;
				}
			}

			System.out.println("-------------------------------------");
			int l = 0;
			String[] bufArr = buf.split("[xyzrqt]");
			trackData = new double[((bufArr.length) / 6)][6];
			System.out.println(bufArr.length);
			int no=1;
			for (int r = 0; r < trackData.length; r++) {
				for (int c = 0; c < trackData[0].length; c++) {
					try {
						l = Integer.parseInt(bufArr[no] .replaceAll("\\s+", "").trim());
						trackData[r][c] = (double) l;
						no++;
					} catch (Exception e) {
						String temp = "";
						for (int j = 0; j < bufArr[no].length(); j++) {
							if ((int) bufArr[no].charAt(j) != 0) {
								temp += bufArr[no].charAt(j);
							}
						}
						if (!temp.equals("")) {
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
			System.out.println(Arrays.deepToString(trackData));
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
			
			/*double theta,xnew,ynew;
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
			}*/

			testData = analyzeData(trackData);
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			System.out.println("Sending data");
			for (int i = 0; i < testData.length; i++) {
				try {
					Thread.sleep(20);
				} catch (Exception e) {
				}
				serialConnection.write((testData[i] +"!").getBytes());
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
			serialConnection.read();
			System.out.println("START!");
			serialConnection.write("B".getBytes());
			serialConnection.write("R".getBytes());
			serialConnection.close();
		} catch (IOException e) {
			System.out.println("Writing exception");
		}
	}
	
	public static int[] analyzeData(double[][] trackData) {
		int raceCounter = 0;
		int[] raceData;;
		double[] accY = new double[trackData.length];
		int[] tachoArr = new int[trackData.length];
		//double[] accY = {0.052001953, 0.055664063, 0.081542969, 0.044189453, -0.113037109, 0.059814453, -0.002929688, 0.084716797, 0.343261719, -0.030761719, -0.193359375, -0.019287109, 0.220214844, 0.056884766, 0.075927734, 0.036621094, 0.235595703, 0.500488281, 0.424560547, 0.681396484, 0.641113281, 0.561035156, 0.657714844, 0.677734375, 0.654541016, 0.339111328, 0.378173828, 0.291259766, 0.650146484, 0.383300781, 0.510253906, 0.6171875, 0.677978516, -0.019042969, -0.164550781, -0.511474609, -0.631591797, -0.567626953, -0.576904297, -0.362792969, -0.658447266, -0.594238281, -0.668701172, -0.582763672, -0.539794922, -0.475830078, -0.263671875, 0.527587891, 0.527099609, 0.661376953, 0.516845703, 0.209960938, 0.571533203, -0.049316406, 0.324707031, 0.421142578, 0.834228516, 0.418701172, 0.780517578, 0.276611328, 0.572509766, 0.549072266, 0.486572266, 0.468505859, -0.229248047, -0.557617188, -0.383300781, 0.210205078, 0.45703125, 0.272216797, 0.555908203, -0.285888672, -0.203369141, -0.491943359, 0.008544922, -0.024169922, 0.590332031, 0.339599609, 0.262695313, 0.322753906, 0.228027344, 0.065185547, 0.035888672, -0.054443359, 0.199951172, 0.063720703, 0.071533203, -0.043701172, 0.090087891, 0.052001953, -0.137207031, 0.138183594, 0.139404297, 0.332275391, -0.141845703, -0.171142578, 0.167724609, 0.098388672, -0.015625, 0.163085938, 0.044677734, 0.034912109, 0.216796875, 0.232421875, -0.167480469, -0.105957031, 0.076416016, 0.236572266, -0.021972656, -0.206787109, -0.029541016, -0.017578125, -0.028076172, 0.003417969, 0.041503906, -0.006835938, -0.102050781, 0.188964844, 0.288574219, 1.178466797, 1.511962891, 0.547119141, 0.572021484, 0.755615234, 0.631347656, 0.850830078, 0.808105469, 0.973876953, 0.533447266, 0.393066406, 0.232177734, -0.140380859, -0.178710938, 0.059082031, -0.021728516, 0.152587891, 0.500488281, -0.054931641, -0.298828125, 0.004394531, 0.226806641, 0.03515625, 0.023193359, 0.038330078, -0.841552734, -0.550292969, -1.135253906, -1.250244141, -0.617919922, -0.836669922, -0.671142578, -0.803710938, -0.803466797, -0.827636719, -0.673583984, -0.243896484, 0.173339844, 0.099853516, -0.004638672, 0.067138672, 0.069091797, 0.285644531, -0.050292969, 0.342529297, 0.75, 0.303222656, 0.528564453, 0.652587891, 0.754150391, 0.565185547, 0.332275391, 0.35546875, 0.679443359, 0.440673828, 0.579833984, 0.503662109, 0.534912109, 0.338623047, 0.532714844, 0.426269531, 0.479492188, 0.648925781, 0.204345703, 0.111816406, 0.125, 0.22265625, 0.147949219, -0.237060547, 0.132568359, -0.013671875, 0.099365234, -0.095947266, 0.029052734, 0.026855469, -0.086181641, 0.131103516, -0.069824219, 0.059326172, 0.096435547, -0.150878906, 0.362060547, 0.154541016, 0.060058594, -0.049560547, 1.102539063, 1.478759766, 1.269287109, 0.71875, 0.763671875, 0.796142578, 0.382080078, 0.092041016, 0.11328125, 0.04296875, -0.089599609, -0.105224609, -0.201416016, -0.106689453, -0.135498047, -0.380615234, -0.055419922, 0.141113281, 0.002685547, 0.170898438, 0.050292969, 0.073974609, 0.001953125, 0.144775391, -0.086914063, -0.270507813, -0.909667969, -1.35546875, -0.927246094, -0.871582031, -0.090332031, -1.161621094, -0.008544922, 0.140625, -0.077636719, -0.196289063, -0.986816406, -0.442871094, -0.812011719, -0.948242188, -0.746582031, -0.172363281, -0.077880859, -0.12109375, -0.102783203, 0.091552734, 0.063232422, -0.044433594, 0.09375, -0.201660156, -0.09375, 0.035888672, 0.099121094, -0.080078125, -0.711914063, -1.153808594, -0.867675781, -1.354736328, -1.023925781, 0.728759766, 0.774658203, 1.002441406, 0.896240234, 0.493408203, 0.551269531, 0.64453125, -0.411621094, -0.454833984, -0.323242188, 0.405029297, 1.05078125, 0.55859375, 0.489257813, 0.239990234, 0.721191406, 0.570556641, 0.658691406, 0.244628906, 0.401367188, 0.434326172, 0.361816406, 0.2890625, 0.028808594, -0.119140625, -0.399414063, 0.131591797, 0.194580078, 0.106201172, 0.020263672, -0.174804688, -0.024902344, -0.061767578, -0.045410156, -0.165771484, 0.114501953, 0.347900391, 0.689453125, 1.192138672, 1.079101563, 0.739013672, 0.201904297, 0.528564453, 0.861328125, 0.674316406, 0.562255859, 0.433837891, 0.610351563, 0.722412109, 0.668945313, 0.152587891, -0.458007813, -0.586669922, -0.588867188, -0.801513672, -0.627441406, -0.671142578, -0.447265625, -0.65625, -0.606445313, -0.478515625, -0.426025391, -0.609375, -0.048339844, -0.076416016, 0.393554688, 0.599609375, 0.204101563, 0.649902344, 0.544677734, 0.479248047, 0.551025391, 0.543457031, 0.305908203, 0.551269531, 0.513671875, 0.621582031, 0.465820313, 0.489746094, 0.468994141, 0.511230469, -0.039306641, -0.210693359, -0.291259766, 0.217041016, 0.335449219, 0.192138672};	
		//int[] tachoArr ={0, 1, 1, 2, 4, 5, 7, 10, 12, 15, 19, 22, 26, 29, 33, 38, 42, 46, 51, 56, 60, 65, 69, 74, 78, 82, 86, 90, 95, 99, 103, 107, 111, 115, 119, 123, 127, 131, 135, 138, 142, 146, 150, 154, 158, 161, 165, 169, 173, 176, 180, 184, 188, 192, 196, 200, 204, 208, 212, 216, 220, 224, 228, 232, 236, 240, 244, 248, 252, 256, 260, 264, 268, 272, 276, 279, 283, 287, 290, 294, 297, 301, 304, 308, 311, 315, 319, 323, 327, 331, 336, 340, 345, 350, 355, 360, 365, 370, 375, 381, 386, 392, 398, 404, 410, 416, 423, 429, 436, 443, 450, 456, 463, 470, 477, 484, 491, 498, 505, 511, 518, 524, 529, 534, 539, 545, 549, 554, 559, 563, 567, 571, 576, 580, 584, 589, 594, 598, 603, 609, 614, 619, 624, 630, 636, 641, 647, 652, 656, 661, 666, 670, 674, 679, 683, 686, 690, 693, 697, 701, 704, 709, 713, 717, 722, 727, 731, 736, 740, 745, 749, 753, 757, 761, 765, 769, 773, 777, 781, 785, 789, 793, 797, 801, 805, 809, 814, 819, 824, 829, 834, 839, 845, 850, 855, 861, 866, 872, 878, 884, 889, 895, 901, 907, 913, 919, 925, 930, 935, 940, 945, 950, 954, 959, 964, 969, 975, 980, 985, 991, 997, 1002, 1008, 1014, 1020, 1026, 1032, 1038, 1045, 1051, 1057, 1063, 1068, 1073, 1077, 1082, 1086, 1090, 1094, 1099, 1104, 1109, 1114, 1118, 1123, 1127, 1131, 1136, 1140, 1145, 1149, 1154, 1159, 1164, 1169, 1175, 1180, 1186, 1192, 1199, 1205, 1211, 1216, 1222, 1227, 1232, 1237, 1242, 1246, 1251, 1255, 1260, 1264, 1268, 1272, 1277, 1281, 1285, 1289, 1293, 1297, 1301, 1305, 1309, 1313, 1316, 1320, 1324, 1328, 1333, 1337, 1342, 1346, 1351, 1356, 1361, 1367, 1372, 1377, 1383, 1388, 1394, 1399, 1404, 1408, 1413, 1417, 1422, 1426, 1430, 1434, 1439, 1443, 1447, 1451, 1455, 1459, 1464, 1468, 1472, 1476, 1480, 1484, 1487, 1491, 1495, 1499, 1502, 1506, 1510, 1514, 1518, 1522, 1526, 1530, 1534, 1537, 1541, 1545, 1549, 1553, 1557, 1561, 1565, 1569, 1573, 1577, 1580, 1584, 1588};
		double[] speedArr= new double[350];
		double[] speedArr2= new double[350];
		int cumulativeTacho = 0;
		for (int i = 0; i < trackData.length; i++) {
			accY[i] = trackData[i][1];
			cumulativeTacho += trackData[i][5];
			tachoArr[i] = cumulativeTacho;
		}
		tachoArr[0]=0;
		speedArr[0]=accY[0];
		for (int i = 1; i < accY.length; i++) {
			speedArr[i] = speedArr[i-1]+0.4*(accY[i]-speedArr[i-1]);
			//System.out.println(speedArr[i]);
		}
		for (int i = 0; i < speedArr.length; i++) {
			speedArr[i] = 1-Math.abs(speedArr[i]);
			//System.out.println(speedArr[i]);
		}
		speedArr2[0]=1;
		for (int i = 1; i < speedArr2.length; i++) {
			speedArr2[i] = speedArr2[i-1]+0.2*(speedArr[i]-speedArr2[i-1]);
			//System.out.println(speedArr2[i]);
		}
		raceData = new int[tachoArr.length*2];
		raceData[0] = 0;
		for (int i = 0; i < tachoArr.length; i++) {
			raceData[raceCounter+1]=(int) ((Math.pow(2, (speedArr2[i]/0.976))-1)*90);
			//System.out.println(raceData[raceCounter+1]);
			raceData[raceCounter]= tachoArr[i];
			raceCounter = raceCounter+2;
		}
		
		for (int i = 1; i < raceData.length; i+=2) {
			if(raceData[i]<50){
				raceData[i]=60;
			}
			if(raceData[i]>100){
				raceData[i]=100;
			}
		}
		int straightLength = 0;
		int[] straightStart = {0,0};
		int[] straightEnd = {0,0};
		int breakPoint = 0;
		boolean straight = false;
		
		for (int i = 21; i < raceData.length; i+=2) {
			if(raceData[i] >= 85 && !straight) {
				straightStart[0] = raceData[i-1];
				straightStart[1] = i;
				straight = true;
			}
			if(straight && raceData[i] < 85) {
				straightEnd[0] = raceData[i-3];
				straightEnd[1] = i;
				straight = false;
				straightLength = straightEnd[0] - straightStart[0];
				breakPoint = (int) (straightStart[0] + (straightLength*0.55));
				
				if(straightLength>10) {
					System.out.println("Straight: " + straightStart[0] + " to " + straightEnd[0] + " Length: " 
							+ straightLength + " Break point: "  + breakPoint);
					for (int j = straightStart[1]; j <= straightEnd[1]; j+=2) {
						if(raceData[j-1]>breakPoint) {
							raceData[j-2] = 200;
						}
					}
				}
				straightLength = 0;
				straightEnd[0] = 0;
				straightEnd[1] = 0;
				straightStart[0] = 0;
				straightStart[1] = 0;
			}
		}
		
		//raceData[raceCounter] = 0;
		//raceData[raceCounter+1] = 70;
		//raceCounter = raceCounter+2;
		/*for (int r = 0; r < accY.length; r++) {
			//cumulativeTacho = cumulativeTacho + trackData[r][5].intValue();
			if(accY[r]>0.4||accY[r]<-0.4) {
				distanceBetween = tachoArr[r] - tachoArr[tempR];
				if(distanceBetween < 50) {
					speed = 65;
					raceData[raceCounter] = tachoArr[tempR];
					raceData[raceCounter+1] = speed;
					raceCounter = raceCounter+2;
				}else if(distanceBetween >= 50 && distanceBetween < 100) {
					speed = 75;
					raceData[raceCounter] = tachoArr[tempR];
					raceData[raceCounter+1] = speed;
					raceCounter = raceCounter+2;
					speed = 65;
					raceData[raceCounter] = tachoArr[tempR] + 20+10;
					raceData[raceCounter+1] = speed;
					raceCounter = raceCounter+2;
				} else {
					speed = 83;
					raceData[raceCounter] = tachoArr[tempR];
					raceData[raceCounter+1] = speed;
					raceCounter = raceCounter+2;
					speed = 180;
					raceData[raceCounter] = (int) (tachoArr[tempR] + (distanceBetween*0.65));
					raceData[raceCounter+1] = speed;
					raceCounter = raceCounter+2;
					speed = 65;
					raceData[raceCounter] = tachoArr[tempR] + distanceBetween;
					raceData[raceCounter+1] = speed;
					raceCounter = raceCounter+2;
				}
				tempR = r;
			}
		}*/
		for (int item : raceData) {   
		    System.out.println(item + ", ");
		}
		return raceData;
	}
}
