import dk.thibaut.serial.SerialPort;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String[] args) {

		String buf = "";
		String buf2 = "";
		Double[][] trackData;

		List<String> availablePorts = SerialPort.getAvailablePortsNames();

		for (String availablePort : availablePorts) {
			System.out.println(availablePort + " is available");
		}

		System.out.println("-------------------------------------");

		SerialConnection serialConnection = new SerialConnection("COM6");
		try {
			System.out.println("Runing");
			serialConnection.write("A".getBytes());
			serialConnection.read();
			serialConnection.write("D".getBytes());
			serialConnection.read();
			for (int i = 0; i < 50; i++) {
				try {
					Thread.sleep(80);
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
			serialConnection.write("d".getBytes());
			serialConnection.read();
			serialConnection.write("a".getBytes());
			serialConnection.read();
			serialConnection.write("N".getBytes());
			System.out.println("nextVal: " + new String(serialConnection.read()));
			serialConnection.close();

			System.out.println("-------------------------------------");
			int l = 0;
			String[] bufArr = buf.split("[xyzrqt]");
			trackData = new Double[((bufArr.length) / 6)][6];
			System.out.println(bufArr.length);
			System.out.println(buf);
			System.out.println(Arrays.toString(bufArr));
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
				trackData[i][2] = trackData[i][2]/16384;
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
		} catch (IOException e) {
		}
	}
}
