package Client;

import ClientHandler.ClientHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client extends JPanel{
    Socket s;
    DataInputStream dInput;
    DataOutputStream dOutput;
    public String name, dir;
    WatchService watcher;
    Path direc;
    WatchKey key;

    public static JFrame window;
    public static int port;
    public static JTextField jtextport;
    public static JTextField jtextip;
    public static JTable jTable;

   Client(Socket s, String name, String dir){
       try  {
           this.s = s;
           dInput = new DataInputStream(new DataInputStream(s.getInputStream()));
           dOutput = new DataOutputStream(new DataOutputStream(s.getOutputStream()));
           this.name = name;
           this.dir = dir;

           watcher = FileSystems.getDefault().newWatchService();
           direc = Paths.get(this.dir);
           direc.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                   StandardWatchEventKinds.ENTRY_MODIFY);
           key = null;

           window = new JFrame("Client");
           window.setLayout(null);
           window.setBounds(300, 200, 330, 300);
//           window.setResizable(false);



           JLabel labelport = new JLabel("Port");
           labelport.setBounds(20, 120, 80, 30);
           labelport.setFont(new Font("Serif", Font.PLAIN, 24));
           window.add(labelport);
           jtextport = new JTextField("D:\\test");
           jtextport.setSize(new Dimension(100, 50));
           jtextport.setBounds(80, 120, 200, 30);
           window.add(jtextport);
           jTable = new JTable(new DefaultTableModel(new Object[]{"Action", "File Path"}, 0));

//           jTable = new JTable(data,ColumnName);
           jTable.setBounds(20,230,330,100);

           JScrollPane jScroll = new JScrollPane(jTable);
           jScroll.setBounds(20,230,330,100);
           window.add(jScroll);

           window.setVisible(true);
       }
       catch (Exception e){
            System.out.println(e);
       }
   }

   public void sendMessage() throws IOException {
       try{
           dOutput.writeUTF(name);

           Scanner sc = new Scanner(System.in);
           while (this.s.isConnected()){
//               String msg = sc.nextLine();

               watcher = FileSystems.getDefault().newWatchService();
               direc = Paths.get(this.dir);
               direc.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                       StandardWatchEventKinds.ENTRY_MODIFY);

               System.out.println("Watch Service registered for dir: " + direc.getFileName());
               key = null;
               System.out.println("DIR in send: " + this.dir);
               try {
                   // System.out.println("Waiting for key to be signalled...");
                   key = watcher.take();
               } catch (InterruptedException ex) {
                   System.out.println("InterruptedException: " + ex.getMessage());
                   return;
               }


               for (WatchEvent<?> event : key.pollEvents()) {
                   // Retrieve the type of event by using the kind() method.
                   WatchEvent.Kind<?> kind = event.kind();
                   WatchEvent<Path> ev = (WatchEvent<Path>) event;
                   DefaultTableModel model = (DefaultTableModel) jTable.getModel();

                   Path fileName = direc.resolve((Path) event.context());
                   if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                       if(!fileName.getFileName().toString().equals("pj_temp_new_file_java_01.txt")) {
                           dOutput.writeUTF("CREATE" + fileName);
//                           jtextport.setText("CREATE" + fileName);
                           System.out.printf("A new file %s was created.%n", fileName.getFileName());
                           model.addRow(new Object[]{"CREATE", fileName});
                       }
                   } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                       if(!fileName.getFileName().toString().equals("pj_temp_new_file_java_01.txt")) {
                           dOutput.writeUTF("MODIFY" + fileName);
                           System.out.printf("A file %s was modified.%n", fileName.getFileName());
//                           jtextport.setText("MODIFY" + fileName);
                           model.addRow(new Object[]{"MODIFY", fileName});
                       }
                   } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                       if(!fileName.getFileName().toString().equals("pj_temp_new_file_java_01.txt")) {
                           dOutput.writeUTF("DELETE" + fileName);
                           System.out.printf("A file %s was deleted.%n", fileName.getFileName());
//                           jtextport.setText("DELETE" + fileName);
                           model.addRow(new Object[]{"DELETE", fileName});
                       }
                   }
               }

               boolean valid = key.reset();
               if (!valid) {
                   break;
               }
//               System.out.println("Sent: " + msg);
           }
       }
       catch (Exception e){

       }
   }

   public void listenMessage(){
       new Thread(new Runnable() {
           @Override
           public void run() {
               String msg;
               while (s.isConnected()){
                   try {
                       msg = dInput.readUTF();
//                       if(isValidPath(msg)){
                           File file = new File( dir + "\\pj_temp_new_file_java_01.txt");
                           file.createNewFile();
                           file.delete();

                           dir = msg;
                           dOutput.writeUTF("Change observe" );
//                       }

                       System.out.println("Received: " + msg);
                   }
                   catch (Exception e){

                   }
               }
           }
       }).start();

//       while (true){
//           try {
//               watcher = FileSystems.getDefault().newWatchService();
//               direc = Paths.get(dir);
//               direc.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
//                       StandardWatchEventKinds.ENTRY_MODIFY);
//               key = null;
//
//               try {
//                   // System.out.println("Waiting for key to be signalled...");
//                   key = watcher.take();
//               } catch (InterruptedException ex) {
//                   System.out.println("InterruptedException: " + ex.getMessage());
//                   return;
//               }
//           }
//           catch (Exception e){
//
//           }
//       }
   }


   public static void main(String[] args) throws IOException {
       CountDownLatch startSignal = new CountDownLatch(1);
       CountDownLatch doneSignal = new CountDownLatch(100000);
       new GUI();


        while (GUI.sName == null || GUI.sName == "") {
            try {
                Thread.sleep(200);
            } catch(InterruptedException e) {
                System.out.println(e);
            }
        }

       try {

           Socket s = new Socket("localhost", 3000);

            Client cl = new Client(s, GUI.sName, GUI.sDir);
           cl.listenMessage();
           cl.sendMessage();
       }
       catch (Exception ex){
            System.out.println(ex);
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

}

class GUI extends JPanel{
    public static String sName = null, sDir = null;
    public static JFrame window;
    public static int port;
    public static JTextField jtextport;
    public static JTextField jtextip;
    public static JTextField jtextname;
    public static JButton jbutton;

    public static JFrame window2;
    public static JTable jTable;
    public static Client cll;

    public GUI() {
        window = new JFrame("Client");
        window.setLayout(null);
        window.setBounds(300, 200, 330, 300);
        window.setResizable(false);

        JLabel label = new JLabel("Client");
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

        JLabel labelname = new JLabel("Name");
        labelname.setBounds(20, 160, 80, 30);
        labelname.setFont(new Font("Serif", Font.PLAIN, 24));
        window.add(labelname);
        jtextname = new JTextField("tgt");
        jtextname.setSize(new Dimension(100, 50));
        jtextname.setBounds(80, 160, 200, 30);
        window.add(jtextname);

        jbutton = new JButton("Connect");
        jbutton.setBounds(100, 200, 100, 30);
        window.add(jbutton);

        window.setVisible(true);

        jbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sName = jtextname.getText();
                sDir = jtextport.getText();
                window.dispose();
            }
        });
//        jbutton.setActionCommand("nameSend");
    }
}

