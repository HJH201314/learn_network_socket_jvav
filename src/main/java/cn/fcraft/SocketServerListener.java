package cn.fcraft;

import java.net.Socket;

/**
 * @author HJH201314
 */
public interface SocketServerListener {
    void onReceive(Socket socket, String message);
    void onFileRequest(Socket socket, String fileName, long fileSize);
    void onFileReceive(Socket socket, String fileName);
    void onConnected(Socket socket);
    void onDisconnected(String info);
}
