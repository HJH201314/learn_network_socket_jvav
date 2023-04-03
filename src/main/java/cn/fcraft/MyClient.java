package cn.fcraft;

import java.io.*;
import java.net.*;

/**
 * @author HJH201314
 */
public class MyClient {

    private Socket socket;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;

    private Boolean isConnected = false;

    private final String host;

    private final int port;

    private Thread serverListener;

    private SocketClientListener listener;

    /**
     * Initialize the client using server host and port.
     * @param host server host
     * @param port server port
     */
    public MyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Create a socket and connect to the server.
     */
    public boolean start(SocketClientListener listener) {
        try {
            socket = new Socket(host, port);
            System.out.println("Connected to server: " + socket.getInetAddress().getHostAddress());
            this.listener = listener;
            this.isConnected = true;
            listener.onConnected();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            serverListener = new Thread(() -> {
                try {
                    if (inputStream == null) return;
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while (!socket.isClosed()) {
                        if (inputStream.available() <= 0) continue;
                        bytesRead = inputStream.read(buffer);
                        listener.onReceive(new String(buffer, 0, bytesRead));
                    }
                } catch (IOException ignored) {}
            });
            serverListener.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Send a message to the server.
     * @param message a String message
     * @return true if the message is sent successfully, otherwise false
     */
    public boolean sendMsg(String message) {
        try {
            if (outputStream != null) {
                outputStream.write(message.getBytes());
                return true;
            } else return false;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Send a message to the server.
     * @param file local file
     * @return true if the message is sent successfully, otherwise false
     */
    public boolean sendFile(File file) {
        // request file sending
        sendMsg("FILE " + file.getName() + " " + file.length());
        // send file
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (IOException e) {
            return false;
        }
        // receive response
        String response = receive();
        System.out.println("here:" + response);
        boolean result = response.equals("FILE OK");
        listener.onFileSent(file.getName(), result);
        return result;
    }

    /**
     * Receive a message from the server, wait until message come.
     * @return
     */
    public String receive() {
        try {
            if (inputStream == null) return "";
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            bytesRead = inputStream.read(buffer);
            return new String(buffer, 0, bytesRead);
        } catch (IOException ignored) {
            return "";
        }
    }

    /**
     * Close the socket.
     */
    public void stop() {
        try {
            this.isConnected = false;
            serverListener.interrupt();
            inputStream.close();
            outputStream.close();
            socket.close();
            listener.onDisconnected("");
        } catch (IOException ignored) {}
    }

    /**
     * Check if the socket is connected.
     * @return true if the socket is connected, otherwise false
     */
    public boolean isConnected() {
        return this.isConnected;
    }
}

