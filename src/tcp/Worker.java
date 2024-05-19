package tcp;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker extends Thread {
    private Socket socket;

    public Worker(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedWriter writer = null;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //HTTP
            WebRequest request = WebRequest.of(reader);
            System.out.println(request.command + " " + request.url);
            shareLog(socket.getInetAddress().getHostAddress(), request.command, request.url);

            writer.write("Hello " + request.headers.get("User-Agent") + "! <br/>");
            writer.write("You requested: " + request.command + " " + request.url + " by using HTTP version " + request.version + "\n");
            writer.write("\n");
            writer.flush();
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

    private void shareLog(String hostAddress, String command, String url) {
        String serverName = System.getenv("LOGGER_SERVERNAME");
        String serverPort = System.getenv("LOGGER_SERVERPORT");
        BufferedWriter writer = null;
        if (serverName == null) {
            throw new RuntimeException("Logger Server port is not specified {LOGGER_SERVERPORT}!");
        }
        Socket socket1 = null;
        String clientIPAddress = socket1.getInetAddress().getHostAddress();

        try {
            socket1 = new Socket(InetAddress.getByName(serverName), Integer.parseInt(serverPort));
            writer = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
            writer.write(String.format("[%s] %s: %s %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), clientIPAddress, command, url));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                writer.flush();
                writer.close();
                socket1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static class WebRequest {
        private String command;
        private String url;
        private String version;
        private Map<String, String> headers;

        public WebRequest(String command, String url, String version, Map<String, String> headers) {
            this.command = command;
            this.url = url;
            this.version = version;
            this.headers = headers;
        }

        public static WebRequest of(BufferedReader reader) throws IOException {
            List<String> input = new ArrayList<>();
            String line;
            while (!(line = reader.readLine()).equals("")) {
                input.add(line);
            }
            String[] args = input.get(0).split("\\s++");
            HashMap<String, String> headers = new HashMap<>();
            for (int i = 1; i < input.size(); i++) {
                String[] header = input.get(i).split(":");
                headers.put(header[0], header[1]);
            }

            return new WebRequest(args[0], args[1], args[2], headers);
        }
    }
}
