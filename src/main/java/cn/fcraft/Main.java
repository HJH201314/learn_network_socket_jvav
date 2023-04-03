package cn.fcraft;

import com.formdev.flatlaf.FlatLightLaf;

/**
 * @author HJH201314
 */
public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        new SocketServerGUI().creatFrame();
        new SocketClientGUI().creatFrame();
    }
}