package Server;

import ClientHandler.ClientHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Server {
    ServerSocket sv;
    int id = 0;

    public static JFrame window;
    public static int port;
    public static JTable jTable;
    public static JTable clientTable;
    public static int currentSelectedUser = -1;
    public static JFileChooser fileChooser;
    public static String chooserDir;
    public static boolean changeFilePath = false, disconnectChoice = false;

    Server(int port){
        try{
            sv = new ServerSocket(port);
            Server.port = port;

            window = new JFrame("SERVER");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLayout(null);
            window.setBounds(300, 200, 1000, 800);

            window.getContentPane().setBackground(new Color(240,240,240));

            JLabel uiLabel = new JLabel("SERVER");
            uiLabel.setBounds(450, 15, 200, 30);
            uiLabel.setFont(new Font("Serif", Font.PLAIN, 40));
            window.add(uiLabel);

            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment( JLabel.CENTER );

            JLabel clientTableLabel = new JLabel("Click on row to select and change user's directory");
            clientTableLabel.setBounds(200, 50, 400, 30);
            clientTableLabel.setFont(new Font("Serif", Font.PLAIN, 18));
            window.add(clientTableLabel);

            jTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "Action" ,"File Path"}, 0));
            clientTable = new JTable(new DefaultTableModel(new Object[]{"ID", "Name", "Time","Current Path"}, 0));


            clientTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
            jTable.setCursor(new Cursor(Cursor.HAND_CURSOR));

            jTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
            jTable.getTableHeader().setOpaque(false);
            jTable.getTableHeader().setForeground(new Color(255,255,255));
            jTable.getTableHeader().setBackground(new Color(0, 0, 0));

            clientTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
            clientTable.getTableHeader().setOpaque(false);
            clientTable.getTableHeader().setForeground(new Color(255,255,255));
            clientTable.getTableHeader().setBackground(new Color(0, 0, 0));

            clientTable.setBounds(200, 80, 600, 150);
            jTable.setBounds(20,240,950,400);

            clientTable.setDefaultEditor(Object.class, null);
            jTable.setDefaultEditor(Object.class, null);

            jTable.setRowHeight(30);
            jTable.getColumnModel().getColumn(0).setMinWidth(10);
            jTable.getColumnModel().getColumn(0).setPreferredWidth(10);
            jTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            jTable.getColumnModel().getColumn(2).setPreferredWidth(130);
            jTable.getColumnModel().getColumn(3).setPreferredWidth(600);

            clientTable.setRowHeight(30);
            clientTable.getColumnModel().getColumn(0).setMinWidth(10);
            clientTable.getColumnModel().getColumn(0).setPreferredWidth(10);
            clientTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            clientTable.getColumnModel().getColumn(2).setPreferredWidth(40);
            clientTable.getColumnModel().getColumn(3).setPreferredWidth(360);

            for(int i = 0; i < 3; ++i){
                jTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            for(int i = 0; i < 2; ++i){
                clientTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

//            DefaultTableModel m = (DefaultTableModel) jTable.getModel();

            // scroll pane
            JScrollPane dataScrollPane = new JScrollPane(jTable);
            JScrollPane jTableClientScroll = new JScrollPane(clientTable);

            dataScrollPane.setBounds(20,240,950,400);
            jTableClientScroll.setBounds(200, 80, 600, 150);

            JScrollBar sb = dataScrollPane.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());

            Rectangle  rect = new Rectangle(0, (int) jTable.getPreferredSize().getHeight(), 0 ,0);
            jTable.scrollRectToVisible(rect);

            Rectangle  rectClient = new Rectangle(0, (int) clientTable.getPreferredSize().getHeight(), 0 ,0);
            clientTable.scrollRectToVisible(rectClient);

            dataScrollPane.getViewport().setBackground(new Color(255,255,255));
            jTableClientScroll.getViewport().setBackground(new Color(255,255,255));

            window.add(dataScrollPane);
            window.add(jTableClientScroll);

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

                    if(isValidPath(chooserDir) && val == JFileChooser.APPROVE_OPTION){
                        changeFilePath = true;
                        clientTable.setValueAt(chooserDir,Server.clientTable.getSelectedRow(), 2) ;
                    }
                    Server.clientTable.clearSelection();
                    System.out.println(fileChooser.getSelectedFile());
                }

//                if(Server.clientTable.getSelectedRow() > -1){
//                    Server.currentSelectedUser = (int) Server.clientTable.getValueAt(Server.clientTable.getSelectedRow(), 0);
//                    Server.disconnectChoice = true;
//                }
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

                Thread t = new Thread(cl);

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
    public static void main(String[] args) {
        Server sv = new Server(2222);
        sv.start();
    }
}