package ClientHandler;

import Server.Server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    public ClientHandler(Socket sk, int id){
        try{

            this.s = sk;
            dInput = new DataInputStream(new DataInputStream(sk.getInputStream()));
            dOutput = new DataOutputStream(new DataOutputStream(sk.getOutputStream()));
            this.name = dInput.readUTF();
            clientList.put(id, this);
            this.id = id;

            String time = java.time.LocalTime.now().toString();
            String []tempTime = time.split(":");
            DefaultTableModel model = (DefaultTableModel) Server.clientTable.getModel();

            time = tempTime[0] + ":" + tempTime[1];
            model.addRow(new Object[]{this.id, this.name, time, "Not selected"});
        } catch (Exception e){
            System.out.println("Handler constructor exception: " + e);
        }
    }

    @Override
    public void run(){
//        while (s.isConnected()){
            try{
                new Thread(() -> {
                    String msg;
                    while (true){
                        try {
                            DefaultTableModel model = (DefaultTableModel) Server.jTable.getModel();
                            msg = dInput.readUTF();

                            if(msg.equals("Change observe")){
                                model.addRow(new Object[]{this.id, this.name, "CHANGE DIRECTORY", Server.chooserDir});
                            }
                            else{
                                String []splitMSG = msg.split("<");
                                model.addRow(new Object[]{this.id, this.name, splitMSG[0], splitMSG[1]});
                            }

                            System.out.println("Received from " + this.name + ": " + msg);

                        }
                        catch (Exception e){
                            System.out.println("Server ex1: " + e);
                            DefaultTableModel model = (DefaultTableModel) Server.clientTable.getModel();
                            for(int i = 0; i < model.getRowCount(); ++i){
                                if(Objects.equals(model.getValueAt(i, 0), id)){
                                    model.removeRow(i);
                                    JOptionPane.showMessageDialog(Server.window, "Client \"id: " + id + "- name: " + name + "\" has disconnected" );
                                    break;
                                }
                            }
                            clientList.remove(id);
                            break;
                        }
                    }}).start();
            }
            catch (Exception e){
                try{
                    this.s.close();
                } catch (Exception ee){
                    System.out.println("Close socket exception: " + e);
                }
                System.out.println("Handler run exception: " + e);
//                break;
            }
//        }

        while (s.isConnected()){
            Scanner sc = new Scanner(System.in);
//                    if(countTime == 0){
            if(!Server.changeFilePath || this.id != Server.currentSelectedUser){
                continue;
            }
            else {
                try {
                    System.out.println("currentuser: " + Server.currentSelectedUser + " - name: " + this.name);
//                    if(Server.disconnectChoice){
//                        Server.disconnectChoice = false;
//                        this.s.close();
//                    }

                    ClientHandler temp = clientList.get(Server.currentSelectedUser);

                    String sendMSG = Server.chooserDir;
//                Thread.interrupted();
                    try {
                        temp.dOutput.writeUTF(sendMSG);
                    } catch (Exception e) {
                        System.out.println("Exception: " + e);
                    }
                    System.out.println("Sent msg: " + this.id + "-" + sendMSG);
//                    prevID = Server.currentSelectedUser;
//                    countTime = 1;
                    Server.changeFilePath = false;
                } catch (Exception e) {
                    System.out.println("Exception 2: " + e);
                }
            }
//                    }
        }

    }
}

