import dk.thibaut.serial.SerialConfig;
import dk.thibaut.serial.SerialPort;
import dk.thibaut.serial.enums.BaudRate;
import dk.thibaut.serial.enums.DataBits;
import dk.thibaut.serial.enums.Parity;
import dk.thibaut.serial.enums.StopBits;

import java.io.IOException;

/**
 * This class is made for communicating on the same port (which you properly wouldn't do in a real world situation)
 * To communicate on same port, shortcut pin 2 and 3.
 *
 * If you're having issues make sure correct drivers are installed and working, adjust timeout and check COM settings
 */
public class SerialConnection {

    private SerialPort serialPort;
    private int readBufferSize = 40;
    private static int TIMEOUT = 100;

    //Check settings (on Windows) in Device Manager -> Properties -> Port Settings if you have any issues.
    private static final SerialConfig DEFAULT_CONFIG = new SerialConfig(
            BaudRate.B9600, Parity.NONE, StopBits.ONE, DataBits.D8);

    //region Constructors
    public SerialConnection(String port) {
        openPort(port);
    }

    public SerialConnection(String port, int readBufferSize) {
        this.readBufferSize = readBufferSize;
        openPort(port);
    }
    //endregion

    public void write(byte[] bytes) {
        try {
            //System.out.println("Writing: " + bytes.length + " bytes to stream");
            serialPort.getOutputStream().write(bytes);
            serialPort.getOutputStream().flush();

        } catch (IOException e) {
            System.err.println("Failed writing output stream on " + serialPort.getName());
            e.printStackTrace();

        }
    }

    public byte[] read() {

        byte[] readBuffer = new byte[readBufferSize];
        /*try {
            int numberOfBytes = serialPort.getInputStream().read(readBuffer);
            System.out.println("read number of bytes: " + numberOfBytes);
        } catch (IOException e) {
            System.err.println("Failed getting or reading input stream from " + serialPort.getName());
            e.printStackTrace();
        }*/
        return readBuffer;
    }

    public void close() {
        try {
            serialPort.close();
        } catch (IOException e) {
            System.err.println("Could not close " + serialPort.getName());
            e.printStackTrace();
        }
    }

    //region private helper methods
    private void openPort(String port) {
        try {
            serialPort = SerialPort.open(port);
            serialPort.setConfig(DEFAULT_CONFIG);
            serialPort.setTimeout(TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //endregion
}
