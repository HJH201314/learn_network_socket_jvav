package cn.fcraft;

import cn.fcraft.Component.HintTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author HJH201314
 */
public class SocketServerGUI extends JFrame implements SocketServerListener {

    private MyServer server;

    private JTextArea textAreaLog;

    private final HashMap<String, JComponent> componentMap = new HashMap<>();

    public void creatFrame() {
        setTitle("Socket Server GUI");
        setMinimumSize(new Dimension(400, 350));
        setBounds(0,0,400, 350);
        // close the program when the window is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // show the frame in the center of the screen
        setLocationRelativeTo(null);

        Container container = getContentPane();
        container.setLayout(new MigLayout("insets 10", "[grow,50%][grow,50%]", "[][][][grow][]"));

        // Port 提示
        JLabel labelPort = new JLabel("Port:");
        container.add(labelPort, "split 2");
        componentMap.put("labelPort", labelPort);

        // Port 输入
        JTextField textFieldPort = new JTextField("7891");
        container.add(textFieldPort, "growx");
        componentMap.put("inputPort", textFieldPort);

        // Clients 提示
        JLabel labelClientCount = new JLabel("Clients: 0");
        labelClientCount.setHorizontalAlignment(SwingConstants.RIGHT);
        container.add(labelClientCount, "split 2,growx");
        componentMap.put("labelClientCount", labelClientCount);

        // Clients 按钮
        JButton buttonManageClient = new JButton("Manage");
        container.add(buttonManageClient, "wrap,right");
        componentMap.put("buttonManageClient", buttonManageClient);

        // Start 按钮
        JButton buttonStart = new JButton("Start Server");
        buttonStart.addActionListener((actionEvent) -> {
            JTextField portField = (JTextField) componentMap.get("inputPort");
            if (!Objects.equals(portField.getText(), "")) {
                server = new MyServer(Integer.parseInt(portField.getText()));
                server.setListener(this);
                server.start();
                log("Started. Listening at port: " + portField.getText());
                updateStatus();
            }
        });
        container.add(buttonStart, "growx");
        componentMap.put("buttonStart", buttonStart);

        // Stop 按钮
        JButton buttonStop = new JButton("Stop Server");
        buttonStop.addActionListener((actionEvent) -> {
            if (server != null) {
                server.stop();
                log("Socket server stopped.");
                updateStatus();
            }
        });
        container.add(buttonStop , "growx,wrap");
        componentMap.put("buttonStop", buttonStop);

        // Log 提示
        JLabel labelLog = new JLabel("Log:");
        container.add(labelLog);
        componentMap.put("labelLog", labelLog);

        // Clear 按钮
        JButton buttonClear = new JButton("Clear");
        buttonClear.addActionListener((actionEvent) -> {
            clearLog();
        });
        container.add(buttonClear, "split 2,right,wrap");
        componentMap.put("buttonClear", buttonClear);

        // Log 文本框
        textAreaLog = new JTextArea();
        textAreaLog.setLineWrap(true);
        textAreaLog.setEditable(false);
        JScrollPane scroll = new JScrollPane (textAreaLog);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        container.add(scroll, "span 2,growx,growy,wrap");
        componentMap.put("textAreaLog", textAreaLog);

        // CMD 输入框
        HintTextField inputCmd = new HintTextField("Input command here");
        inputCmd.setToolTipText("Input command here");
        inputCmd.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (server == null) return;
                    String cmd = inputCmd.getText();
                    if (cmd.startsWith("broadcast ")) {
                        server.broadcast(cmd.substring(10));
                        log("Broadcast: " + cmd.substring(10));
                    } else {
                        log("Unknown command: " + cmd);
                    }
                    inputCmd.setText("");
                    JScrollBar scrollBar = scroll.getVerticalScrollBar();
                    scrollBar.setValue(scrollBar.getMaximum());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        container.add(inputCmd, "span 2,growx,wrap");
        componentMap.put("inputCmd", inputCmd);

        updateStatus();
        setVisible(true);
    }

    public void log(String log) {
        SwingUtilities.invokeLater(() -> {
            textAreaLog.append(log + "\n");
        });
    }

    public void clearLog() {
        textAreaLog.setText("");
    }

    public void updateStatus() {
        JLabel labelClientCount = (JLabel) componentMap.get("labelClientCount");
        if (server != null) {
            labelClientCount.setText("Clients: " + server.getClientCount());
            setTitle("Socket Server GUI - " + (server.isRunning() ? server.getClientCount() + " clients connected" : "Stopped"));
        }
        JButton buttonStart = (JButton) componentMap.get("buttonStart");
        buttonStart.setEnabled(server == null || !server.isRunning());
        JButton buttonStop = (JButton) componentMap.get("buttonStop");
        buttonStop.setEnabled(server != null && server.isRunning());
        JTextField inputCmd = (JTextField) componentMap.get("inputCmd");
        inputCmd.setEnabled(server != null && server.isRunning());
    }

    @Override
    public void onReceive(Socket socket, String message) {
        String base = "[Client %address%] %msg%";
        base = base.replace("%address%", socket.getRemoteSocketAddress().toString());
        base = base.replace("%msg%", message);
        log(base);
    }

    @Override
    public void onConnected(Socket socket) {
        updateStatus();
        log("Client connected: " + socket.getRemoteSocketAddress().toString());
    }

    @Override
    public void onDisconnected(String info) {
        updateStatus();
        log("Client disconnected: " + info);
    }

    @Override
    public void onFileReceive(Socket socket, String fileName) {
        log("Received file from " + socket.getRemoteSocketAddress().toString() + ": " + fileName);
    }

    @Override
    public void onFileRequest(Socket socket, String fileName, long fileSize) {
        log("File requested from " + socket.getRemoteSocketAddress().toString() + ": " + fileName + " (" + fileSize + " bytes)");
    }
}
