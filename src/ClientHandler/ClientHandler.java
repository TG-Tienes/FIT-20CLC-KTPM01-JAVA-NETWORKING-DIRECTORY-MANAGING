package ClientHandler;

import Server.Server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler extends JPanel implements Runnable {
    Thread t;
    Socket s;
    public String name = "", dir;
    DataInputStream dInput;
    DataOutputStream dOutput;
    public static HashMap<Integer, ClientHandler> clientList = new HashMap<>();
    int id;
    public static int prevID = 0, countTime = 0;

    public ClientHandler(Socket sk, int id) {
        try {

            this.s = sk;
            dInput = new DataInputStream(new DataInputStream(sk.getInputStream()));
            dOutput = new DataOutputStream(new DataOutputStream(sk.getOutputStream()));
            this.name = dInput.readUTF();
            clientList.put(id, this);
            this.id = id;

            String time = java.time.LocalTime.now().toString();
            String[] tempTime = time.split(":");
            DefaultTableModel model = (DefaultTableModel) Server.clientTable.getModel();

            time = tempTime[0] + ":" + tempTime[1];
            model.addRow(new Object[]{this.id, this.name, time, "Not selected"});
            Server.jComboID.addItem(this.id + "-" + this.name);

        } catch (Exception e) {
            System.out.println("Handler constructor exception: " + e);
        }
    }

    @Override
    public void run() {
        try {
            new Thread(() -> {
                String msg;
                while (true) {
                    try {
                        DefaultTableModel model = (DefaultTableModel) Server.jTable.getModel();
                        msg = dInput.readUTF();

                        String time = java.time.LocalTime.now().toString();
                        String[] tempTime = time.split(":");
                        time = tempTime[0] + ":" + tempTime[1];

                        if (msg.equals("Change observe")) {
                            model.addRow(new Object[]{this.id, this.name, time, "CHANGE DIRECTORY", Server.chooserDir});
                        } else {
                            String[] splitMSG = msg.split("<"), action;
                            action = splitMSG[0].split("_");

                            model.addRow(new Object[]{this.id, this.name, time, action[1], splitMSG[1]});
                        }

                        JScrollBar sb = Server.dataScrollPane.getVerticalScrollBar();
                        sb.setValue(sb.getMaximum());

                        System.out.println("Received from " + this.name + ": " + msg);

                    } catch (Exception e) {
                        System.out.println("Server ex1: " + e);
                        DefaultTableModel model = (DefaultTableModel) Server.clientTable.getModel();

                        // xoa trong bang Client
                        for (int i = 0; i < model.getRowCount(); ++i) {
                            if (Objects.equals(model.getValueAt(i, 0), id)) {
                                model.removeRow(i);
                                JOptionPane.showMessageDialog(Server.jFrame, "Client \"id: " + id + "- name: " + name + "\" has disconnected");
                                break;
                            }
                        }

                        try {
                            this.s.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

//                        Server.jComboID.removeItem(this.id + "-" + this.name);
                        clientList.remove(id);
                        break;
                    }
                }
            }).start();
        } catch (Exception e) {
            try {
                this.s.close();
            } catch (Exception ee) {
                System.out.println("Close socket exception: " + e);
            }
            System.out.println("Handler run exception: " + e);
        }

        while (s.isConnected()) {
            Scanner sc = new Scanner(System.in);
            if (!Server.changeFilePath || this.id != Server.currentSelectedUser) {
                continue;
            } else {
                try {
                    System.out.println("currentuser: " + Server.currentSelectedUser + " - name: " + this.name);

                    ClientHandler temp = clientList.get(Server.currentSelectedUser);

                    String sendMSG = Server.chooserDir;
                    try {
                        temp.dOutput.writeUTF(sendMSG);
                    } catch (Exception e) {
                        System.out.println("Exception: " + e);
                    }
                    System.out.println("Sent msg: " + this.id + "-" + sendMSG);

                    Server.changeFilePath = false;
                } catch (Exception e) {
                    System.out.println("Exception 2: " + e);
                }
            }
        }

    }
}

