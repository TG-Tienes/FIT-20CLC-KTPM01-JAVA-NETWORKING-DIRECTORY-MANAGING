package Server;

import Client.Client;
import ClientHandler.ClientHandler;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    ServerSocket sv;
    int id = 0;

    public static JFrame window;
    public static int port;
    public static JTextField jtextport;
    public static JTextField jtextip;
    public static JTable jTable;
    public static JTable clientTable;
    public static int currentSelectedUser = -1;
    public static JFileChooser fileChooser;
    public static String chooserDir;
    public static boolean changeFilePath = false;
//    public static JButton jbutton;

    Server(int port){
        try{
            sv = new ServerSocket(port);
            Server.port = port;
//            this.clientList = new HashMap<>();


            window = new JFrame("Client");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLayout(null);
            window.setBounds(300, 200, 1000, 600);
//           window.setResizable(false);

            JLabel labelport = new JLabel("SERVER");
            labelport.setBounds(20, 120, 80, 30);
            labelport.setFont(new Font("Serif", Font.PLAIN, 24));
            window.add(labelport);
            jtextport = new JTextField("D:\\test");
            jtextport.setSize(new Dimension(100, 50));
            jtextport.setBounds(80, 120, 200, 30);
            window.add(jtextport);
            jTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "Action" ,"File Path"}, 0));
            clientTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Name","Current Path"}, 0));

            clientTable.setBounds(300, 230, 330, 100);
            jTable.setBounds(20,230,330,100);
            clientTable.setDefaultEditor(Object.class, null);

            JScrollPane dataScrollPane = new JScrollPane(jTable);
            JScrollPane jTableClientScroll = new JScrollPane(clientTable);
            dataScrollPane.setBounds(20,230,330,100);
            jTableClientScroll.setBounds(360, 230, 330, 100);
            window.add(dataScrollPane);
            window.add(jTableClientScroll);

            DefaultTableModel m = (DefaultTableModel) clientTable.getModel();

            Server.clientTable.getSelectionModel().addListSelectionListener(event -> {
                if (Server.clientTable.getSelectedRow() > -1) {
                    System.out.println(Server.clientTable.getValueAt(Server.clientTable.getSelectedRow(), 0).toString());
                    Server.currentSelectedUser = (int) Server.clientTable.getValueAt(Server.clientTable.getSelectedRow(), 0);

                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new java.io.File("."));
                    fileChooser.setDialogTitle(chooserDir);
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.setAcceptAllFileFilterUsed(false);

//                    JOptionPane.showInputDialog(fileChooser, JOptionPane.CANCEL_OPTION);
                    int val = fileChooser.showOpenDialog(window);
                    chooserDir = String.valueOf((fileChooser.getSelectedFile()));

                    if(isValidPath(chooserDir) && val == fileChooser.APPROVE_OPTION){
                        changeFilePath = true;
                    }
                    Server.clientTable.clearSelection();
                    System.out.println(fileChooser.getSelectedFile());
                }
            });
//            window.add(jbutton);
            window.setVisible(true);
        }catch (IOException e) {
            System.out.println("Server constructor exception: " + e);
        }
    }

    public void start(){
        try {
            do{
                System.out.println("Waiting for new client");
                Socket s = sv.accept();

                ClientHandler cl = new ClientHandler(s, id);
                id++;
//                clientList.put(id, cl);

                Thread t = new Thread(cl);

//                System.out.println("namasjdsa: "+ clientList.get(0).name);

                t.start();
            }while(!sv.isClosed());
        } catch (IOException e){
            System.out.println("Server start exception: " + e);
        }
    }

    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {

            return false;
        }
        return true;
    }
    public static void main(String[] args) throws IOException {
//        new serverGUI();
//
//        while (serverGUI.sPort == null || serverGUI.sPort == "") {
//            try {
//                Thread.sleep(200);
//            } catch(InterruptedException e) {
//                System.out.println(e);
//            }
//        }

//        Server sv = new Server(Integer.parseInt(serverGUI.sName));

        Server sv = new Server(2222);
        sv.start();
    }
}

class serverGUI extends JPanel {
    public static String sPort;
    public static JFrame window;
    public static int port;
    public static JTextField jtextport;
    public static JTextField jtextip;
    public static JTextField jtextname;
    public static JButton jbutton;

    public static JFrame window2;
    public static JTable jTable;
    public static Client cll;

    public serverGUI() {
        window = new JFrame("Server");
        window.setLayout(null);
        window.setBounds(300, 200, 330, 300);
        window.setResizable(false);

        JLabel label = new JLabel("Server");
        label.setBounds(125, 20, 80, 30);
        label.setFont(new Font("Serif", Font.PLAIN, 30));
        window.add(label);

        JLabel labelport = new JLabel("Port");
        labelport.setBounds(20, 120, 80, 30);
        labelport.setFont(new Font("Serif", Font.PLAIN, 24));
        window.add(labelport);
        jtextport = new JTextField("D:\\test");
        jtextport.setSize(new Dimension(100, 50));
        jtextport.setBounds(80, 120, 200, 30);
        window.add(jtextport);

        jbutton = new JButton("Connect");
        jbutton.setBounds(100, 200, 100, 30);
        window.add(jbutton);

        window.setVisible(true);

        jbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sPort = jtextport.getText();
                window.dispose();
            }
        });
//        jbutton.setActionCommand("nameSend");
    }


}

