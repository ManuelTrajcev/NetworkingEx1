package shared;

import shared.Worker;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SharedResourceServer extends Thread {
    private int port;
    private String csvFile;
    private String counterFile;

    public SharedResourceServer(int port, String csvFile, String counterFile) {
        this.port = port;
        this.csvFile = csvFile;
        this.counterFile = counterFile;
    }

    @Override
    public void run() {
        System.out.println("Shared Resource Server: starting...");
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Shared Resource Server: started");
        System.out.println("Shared Resource Server: waiting for connections...");

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Shared Resource Server: new client accepted!");
            new Worker(socket, new File(csvFile), new File(counterFile));
        }
    }

    public static void main(String[] args) {
        String serverPort = System.getenv("SERVER_PORT");
        if (serverPort == null || serverPort.isEmpty()) {
            throw new RuntimeException("Please add env variable with port number.");
        }
        SharedResourceServer server = new SharedResourceServer(Integer.parseInt(serverPort), System.getenv("logFile"), System.getenv("counterFile"));
    }
}
