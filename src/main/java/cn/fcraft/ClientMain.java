package cn.fcraft;

import com.formdev.flatlaf.FlatLightLaf;

/**
 * @author HJH201314
 */
public class ClientMain {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        new SocketClientGUI().creatFrame();
    }
}
