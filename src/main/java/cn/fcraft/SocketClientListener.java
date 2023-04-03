package cn.fcraft;

/**
 * @author HJH201314
 */
public interface SocketClientListener {
    void onReceive(String message);
    void onFileRequest(String fileName, long fileSize);
    void onFileSent(String fileName, boolean result);
    void onConnected();
    void onDisconnected(String info);
}
