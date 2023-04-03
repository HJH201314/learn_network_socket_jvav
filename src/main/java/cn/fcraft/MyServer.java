package cn.fcraft;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * @author HJH201314
 */
public class MyServer {

    private final int port;

    private Boolean isRunning;

    private ServerSocket serverSocket;

    private Thread clientListener;

    private final HashMap<String, ClientHandler> clients = new HashMap<>();

    private SocketServerListener listener;

    /**
     * Initialize the server using port.
     * @param port server port
     */
    public MyServer(int port) {
        this.port = port;
    }

    /**
     * Create a server socket and start listening.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running...");
            clientListener = new Thread(() -> {
                try {
                    while (!serverSocket.isClosed()) {
                        Socket socket = serverSocket.accept();
                        // Use a new thread to handle the connection
                        ClientHandler handler = new ClientHandler(socket, listener);
                        clients.put(socket.getRemoteSocketAddress().toString(), handler);
                        handler.start();
                        listener.onConnected(socket);
                    }
                } catch (IOException e) {
                    closeAll();
                    //e.printStackTrace();
                }
            });
            clientListener.start();
            this.isRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setListener(SocketServerListener listener) {
        this.listener = listener;
    }

    /**
     * Stop the server.
     */
    public void stop() {
        try {
            this.isRunning = false;
            closeAll();
            clientListener.interrupt();
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getClientCount() {
        return clients.size();
    }

    /**
     * Broadcast a message to all clients.
     * @param message a string message
     */
    public void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendText(message);
        }
    }

    public void send(String client, String message) {
        clients.get(client).sendText(message);
    }

    /**
     * Close all client connections.
     */
    public void closeAll() {
        for (ClientHandler client : clients.values()) {
            client.interrupt();
        }
        clients.clear();
    }

    /**
     * Close a client connection.
     * @param address client address
     */
    public void close(String address) {
        ClientHandler client = clients.get(address);
        clients.remove(address);
        client.interrupt();
    }

    /**
     * Handle client connection.
     */
    private class ClientHandler extends Thread {
        private String address;
        private final Socket socket;
        private InputStream inputStream = null;
        private OutputStream outputStream = null;
        private final SocketServerListener listener;

        public ClientHandler(Socket socket, SocketServerListener listener) {
            this.socket = socket;
            this.listener = listener;
            try {
                this.inputStream = socket.getInputStream();
                this.outputStream = socket.getOutputStream();
            } catch (IOException e) {
                this.interrupt();
                System.out.println("Failed to get input/output stream. Force to close socket.");
            }
        }

        @Override
        public void run() {
            this.address = socket.getRemoteSocketAddress().toString();
            try {
                while (!socket.isClosed()) {
                    if (inputStream.available() <= 0) continue;
                    byte[] buffer = new byte[1024];
                    int bytesRead = inputStream.read(buffer);
                    String message = new String(buffer, 0, bytesRead);
                    if (message.equals("time")) {
                        // get GMT time right now
                        ZonedDateTime gmtTime = ZonedDateTime.now(ZoneOffset.UTC);
                        // format the time
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        sendText(gmtTime.toString());
                    } else if (message.equals("bye")) {
                        close(address);
                    } else if (message.startsWith("FILE ")) {
                        receiveFile(message.replace("FILE ", "").split(" "));
                    }
                    listener.onReceive(socket, message);
                }
                listener.onDisconnected(this.address);
            } catch (IOException e) {
                listener.onDisconnected(this.address);
                //e.printStackTrace();
            }
        }

        @Override
        public void interrupt() {
            try {
                sendText("bye");
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            super.interrupt();
        }

        public Socket getSocket() {
            return socket;
        }

        public void sendText(String message) {
            try {
                if (outputStream != null) {
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void receiveFile(String[] args) {
            String[] fileNameSplit = args[0].split("\\.(?=[^.]*$)");
            listener.onFileRequest(socket, args[0], Long.parseLong(args[1]));
            String newFileName = fileNameSplit[0] + "_" + System.currentTimeMillis() + "." + fileNameSplit[1];
            long fileSize = Long.parseLong(args[1]);
            long bytesReceived = 0;

            try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.dir") + File.separator + newFileName)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while (bytesReceived < fileSize && (bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived))) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    bytesReceived += bytesRead;
                }
                fos.flush();
                System.out.println("File received: " + newFileName);
                listener.onFileReceive(socket, newFileName);
                sendText("FILE OK");
            } catch (IOException e) {
                return;
            }
        }

    }
}
