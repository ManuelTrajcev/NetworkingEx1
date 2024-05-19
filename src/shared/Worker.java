package shared;

import java.io.*;
import java.net.Socket;

public class Worker extends Thread {
    private Socket socket;
    private File logFile;
    private File clientsCountFile;

    public Worker(Socket socket, File logFile, File clientsCountFile) {
        this.socket = socket;
        this.logFile = logFile;
        this.clientsCountFile = clientsCountFile;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        RandomAccessFile clientCounterRaf = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.logFile)));
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            clientCounterRaf = new RandomAccessFile(this.clientsCountFile, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();;
        }
        Integer currentClientCounter = null;
        try {
            currentClientCounter = clientCounterRaf.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (currentClientCounter == null) {
            currentClientCounter = 0;
        }
        String line;

        try {
            while ((line=reader.readLine()) != null) {
                writer.append(line + "\n");
            }
            currentClientCounter ++;
            clientCounterRaf.seek(0);
            clientCounterRaf.writeInt(currentClientCounter);
            System.out.printf("Total Number of Clients until now: %d.\n",currentClientCounter);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.flush();
                writer.close();
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
