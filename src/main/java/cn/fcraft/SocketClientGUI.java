package cn.fcraft;

import cn.fcraft.Component.HintTextField;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author HJH201314
 */
public class SocketClientGUI extends JFrame implements SocketClientListener {

    private MyClient client;

    private JTextArea textAreaLog;

    private final HashMap<String, JComponent> componentMap = new HashMap<>();

    public void creatFrame() {
        setTitle("Socket Client GUI");
        setMinimumSize(new Dimension(400, 350));
        setBounds(0,0,400, 350);
        // close the program when the window is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // show the frame in the center of the screen
        setLocationRelativeTo(null);

        Container container = getContentPane();
        container.setLayout(new MigLayout("insets 10", "[grow,50%][grow,50%]", "[][][][grow][]"));

        // Address 提示
        JLabel labelAddr = new JLabel("Address:");
        container.add(labelAddr, "split 2");
        componentMap.put("labelAddress", labelAddr);

        // Address 输入
        JTextField inputAddr = new JTextField("localhost");
        container.add(inputAddr, "growx");
        componentMap.put("inputAddress", inputAddr);

        // Port 提示
        JLabel labelPort = new JLabel("Port:");
        container.add(labelPort, "split 2");
        componentMap.put("labelPort", labelPort);

        // Port 输入
        JTextField textFieldPort = new JTextField("7891");
        container.add(textFieldPort, "growx,wrap");
        componentMap.put("inputPort", textFieldPort);

        // Start 按钮
        JButton buttonStart = new JButton("Connect");
        buttonStart.addActionListener((actionEvent) -> {
            JTextField addrField = (JTextField) componentMap.get("inputAddress");
            JTextField portField = (JTextField) componentMap.get("inputPort");
            if (!Objects.equals(portField.getText(), "")) {
                client = new MyClient(addrField.getText(), Integer.parseInt(portField.getText()));
                if (!client.start(this))
                    log("Connection failed.");
                updateStatus();
            }
        });
        container.add(buttonStart, "growx");
        componentMap.put("buttonStart", buttonStart);

        // Stop 按钮
        JButton buttonStop = new JButton("Disconnect");
        buttonStop.addActionListener((actionEvent) -> {
            if (client != null) {
                client.sendMsg("bye");
                //client.stop();
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
                    if (client == null) return;
                    String cmd = inputCmd.getInputText();
                    if (cmd.startsWith("send ")) {
                        cmd = cmd.replace("send ", "");
                        client.sendMsg(cmd);
                        log("[Client] " + cmd);
                        inputCmd.setText("");
                    } else if (cmd.equals("time")) {
                        log("[Client] Querying server time.");
                        client.sendMsg("time");
                    } else if (cmd.startsWith("exit")) {
                        client.sendMsg("bye");
                        //client.stop();
                    } else if (cmd.startsWith("file")) {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.showOpenDialog(container);
                        File file = fileChooser.getSelectedFile();
                        client.sendFile(file);
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
        setTitle("Socket Client GUI - " + (client != null && client.isConnected() ? "Connected" : "Disconnected"));
        JButton buttonStart = (JButton) componentMap.get("buttonStart");
        buttonStart.setEnabled(client == null || !client.isConnected());
        JButton buttonStop = (JButton) componentMap.get("buttonStop");
        buttonStop.setEnabled(client != null && client.isConnected());
        JTextField inputCmd = (JTextField) componentMap.get("inputCmd");
        inputCmd.setEnabled(client != null && client.isConnected());
    }

    @Override
    public void onReceive(String message) {
        if (Objects.equals(message, "bye")) {
            client.stop();
            log("Connection closed.");
        } else {
            log("[Server] " + message);
        }
    }

    @Override
    public void onConnected() {
        log("[Client] Connected.");
        updateStatus();
    }

    @Override
    public void onDisconnected(String info) {
        log("[Client] Disconnected. " + info);
        updateStatus();
    }

    @Override
    public void onFileRequest(String fileName, long fileSize) {
        log("[Client] Request file sending: " + fileName + "(" + fileSize + "bytes)");
    }

    @Override
    public void onFileSent(String fileName, boolean result) {
        if (result) log("[Client] File " + fileName + " sent successfully.");
        else log("[Client] File " + fileName + " sent failed.");
    }
}
