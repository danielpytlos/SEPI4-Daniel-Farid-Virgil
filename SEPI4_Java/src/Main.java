import dk.thibaut.serial.SerialPort;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String[] args) {

		String buf = "";
		String buf2 = "";
		String[][] trackData;

		List<String> availablePorts = SerialPort.getAvailablePortsNames();

		for (String availablePort : availablePorts) {
			System.out.println(availablePort + " is available");
		}

		System.out.println("-------------------------------------");

		SerialConnection serialConnection = new SerialConnection("COM4");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
					"viacardump.txt"));
			out.write("First " + "\n");
			out.newLine();
			System.out.println("Runing");
			// serialConnection.write("A".getBytes());
			// serialConnection.read();
			out.write(new String(serialConnection.read()) + "\n");
			serialConnection.write("D".getBytes());
			serialConnection.read();
			// out.write(new String(serialConnection.read()) + "\n");
			// serialConnection.write("f".getBytes());
			for (int i = 0; i < 100; i++) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				serialConnection.write("F".getBytes());
				// System.out.println(new String(serialConnection.read()));
				// System.out.println(new String(serialConnection.read()));
				buf2 = new String(serialConnection.read());
				System.out.println(buf2);
				buf += buf2;
				// cardata.add("Accel:"+ new String(serialConnection.read()));
				// out.write(new String(serialConnection.read()) + "\n");
				// out.newLine();

				// serialConnection.write("G".getBytes());
				// buf += new String(serialConnection.read());
				// cardata.add(buf);
				// out.write(new String(serialConnection.read()) + "\n");
				// out.newLine();

			}
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
			}
			buf2 = new String(serialConnection.read());
			System.out.println(buf2);
			buf += buf2;
			serialConnection.write("d".getBytes());
			serialConnection.read();
			out.write(new String(serialConnection.read()) + "\n");
			serialConnection.write("a".getBytes());
			out.write(new String(serialConnection.read()) + "\n");
			serialConnection.close();

			System.out.println("-------------------------------------");
			int l = 0;
			String[] bufArr = buf.split("[xyzrqt]");
			trackData = new String[((bufArr.length) / 6)][6];
			System.out.println(bufArr.length);
			System.out.println(buf);
			System.out.println(Arrays.toString(bufArr));
			int no=1;
			for (int r = 0; r < trackData.length; r++) {
				for (int c = 0; c < trackData[0].length; c++) {
					try {
						l = Integer.parseInt(bufArr[no] .replaceAll("\\s+", "").trim());
						System.out.println(String.format("%.2f", l));
						trackData[r][c] = l + "";
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
							System.out.println(temp);
							trackData[r][c] = Integer.parseInt(temp.replaceAll("\\s+", "").trim()) + "";
						}
						no++;
					}
				}
			}
			System.out.println(Arrays.deepToString(trackData));
			out.close();
		} catch (IOException e) {
		}

		// SerialConnection serialConnectionShortBuffer = new
		// SerialConnection("COM6", 2);
		// serialConnectionShortBuffer.write("Test!".getBytes());

		/*
		 * System.out.println("Result was: " + new
		 * String(serialConnectionShortBuffer.read()));
		 * System.out.println("Result was: " + new
		 * String(serialConnectionShortBuffer.read()));
		 * System.out.println("Result was: " + new
		 * String(serialConnectionShortBuffer.read()));
		 * serialConnectionShortBuffer.close();
		 */
	}
}
