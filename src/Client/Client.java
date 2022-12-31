package Client;


import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Client extends JPanel {
    Socket s;
    DataInputStream dInput;
    DataOutputStream dOutput;
    public String name, dir;
    public static JFrame jFrame;
    public static JTable jTable;
    public static String sendingMSG = null;
    public static boolean dirChange = false;

    Client(Socket s, String name) {
        try {
            this.s = s;
            dInput = new DataInputStream(new DataInputStream(s.getInputStream()));
            dOutput = new DataOutputStream(new DataOutputStream(s.getOutputStream()));
            this.name = name;

            this.dir = "D:\\test";

            jFrame = new JFrame("Client - name: " + this.name);
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrame.setLayout(null);
            jFrame.setBounds(300, 200, 1000, 600);

            jFrame.getContentPane().setBackground(new Color(240, 240, 240));

            JLabel uiLabel = new JLabel("CLIENT");
            uiLabel.setBounds(420, 15, 200, 30);
            uiLabel.setFont(new Font("Serif", Font.PLAIN, 40));
            jFrame.add(uiLabel);

            JLabel nameLabel = new JLabel("Client Name: " + this.name);
            nameLabel.setBounds(390, 60, 1000, 30);
            nameLabel.setFont(new Font("Serif", Font.PLAIN, 30));
            jFrame.add(nameLabel);

            jTable = new JTable(new DefaultTableModel(new Object[]{"Action", "File Path"}, 0));

            jTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
            jTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
            jTable.getTableHeader().setOpaque(false);
            jTable.getTableHeader().setForeground(new Color(255,255,255));
            jTable.getTableHeader().setBackground(new Color(0, 0, 0));

            jTable.setRowHeight(30);
            jTable.getColumnModel().getColumn(0).setPreferredWidth(130);
            jTable.getColumnModel().getColumn(1).setPreferredWidth(600);

            jTable.setBounds(150, 120, 700, 400);

            JScrollPane jScroll = new JScrollPane(jTable);

            jScroll.getViewport().setBackground(new Color(255,255,255));
            jScroll.setBounds(150, 120, 700, 400);
            jFrame.add(jScroll);

            jFrame.setVisible(true);
        } catch (Exception e) {
            System.out.println("Ex4: " + e);

        }
    }

    public void sendMessage() {
        try {
            dOutput.writeUTF(name);
        } catch (Exception e) {
            System.out.println("Ex6: " + e);
        }

    }

    public void listenMessage() {
        new Thread(() -> {
            String msg;
            while (s.isConnected()) {
                try {
                    msg = dInput.readUTF();
                    if (isValidPath(msg)) {
//                        File file = new File(dir + "\\pj_temp_new_file_java_01.txt");
//                        if (file.createNewFile())
//                            System.out.println("JV created");
//                        if (file.delete())
//                            System.out.println("JV DELETED");

                        dir = msg;
                        DefaultTableModel model = (DefaultTableModel) jTable.getModel();
                        dirChange = true;
                        model.addRow(new Object[]{"CHANGE DIRECTORY", dir});
                        dOutput.writeUTF("Change observe");

                        Path recurDir = Paths.get(dir);
                        if(RecursiveWatchService.watcher != null){
                            Thread.interrupted();
                            RecursiveWatchService.watcher.close();
                        }
                        new Thread(new RecursiveWatchService(recurDir, s)).start();
                    }

                    System.out.println("Received: " + msg);
                } catch (Exception e) {
                    if (Objects.equals(e.toString(), "java.net.SocketException: Connection reset")) {
                        JOptionPane.showMessageDialog(jFrame, "Server closed, this program will be terminated");
                        System.exit(0);
                    }

                    System.out.println("Ex1: " + e);
                }
            }
        }).start();
    }


    public static void main(String[] args) {
        new GUI();


        while (GUI.sName == null || GUI.sName.equals("")) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("Ex2: " + e);
            }
        }

        try {
            Socket s = new Socket("localhost", 2222);
            Client cl = new Client(s, GUI.sName);

            cl.listenMessage();
            cl.sendMessage();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(jFrame, "!!! No server found !!!");
            System.out.println("Ex3: " + ex);

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

class GUI extends JPanel {
    public static String sName = null;
    public static JFrame jFrame;
    public static JTextField jInputName;
    public static JButton jConnectButton;

    public GUI() {
        jFrame = new JFrame("INPUT CLIENT");

        jFrame.setBounds(400, 250, 500, 250);
        jFrame.setLayout(null);
        jFrame.setResizable(false);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("INPUT CLIENT NAME");
        label.setBounds(40, 20, 1000, 30);
        label.setFont(new Font("SansSerif", Font.BOLD, 40));
        jFrame.add(label);

        JLabel labelName = new JLabel("Name");
        labelName.setBounds(20, 160, 80, 30);
        labelName.setFont(new Font("Serif", Font.PLAIN, 24));
        jFrame.add(labelName);
        jInputName = new JTextField("User 1");
        jInputName.setSize(new Dimension(100, 50));
        jInputName.setBounds(80, 160, 200, 30);
        jFrame.add(jInputName);

        jConnectButton = new JButton("Connect");
        jConnectButton.setBounds(100, 200, 100, 30);
        jFrame.add(jConnectButton);

        jFrame.setVisible(true);

        jInputName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }

            public void changed() {
                jConnectButton.setEnabled(!jInputName.getText().equals("") && jInputName.getText() != null);
            }
        });

        jConnectButton.addActionListener(e -> {
            sName = jInputName.getText();
            jFrame.dispose();
        });
    }
}

class RecursiveWatchService implements Runnable {

    public static WatchService watcher;
    private final Map<WatchKey, Path> keys;
    Socket s;
    Path direcPath;

    /**
     * Creates a WatchService and registers the given directory
     */
    RecursiveWatchService(Path dir, Socket s) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.s = s;
        this.direcPath = dir;
        walkAndRegisterDirectories(direcPath);
    }

    /**
     * Register the given directory with the WatchService; This function will be
     * called by FileVisitor
     */
    private void registerDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its subdirectories, with the
     * WatchService.
     */
    private void walkAndRegisterDirectories(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Process all events for keys queued to the watcher
     */
    @Override
    public void run() {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            if (watcher == null)
                continue;
            try {
                key = watcher.take();
            } catch (Exception x) {
                System.out.println("EX x: " + x);
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                Path name = ((WatchEvent<Path>) event).context();
                Path child = dir.resolve(name);

                // print out event
//                System.out.format("%s: %s\n", event.kind().name(), child);
                Client.sendingMSG = event.kind().name() + "<" + child;

//                if (child.getFileName().toString().equals("pj_temp_new_file_java_01"))
//                    continue;
                try {
                    DataOutputStream dOut = new DataOutputStream(new DataOutputStream(this.s.getOutputStream()));
                    dOut.writeUTF(Client.sendingMSG);
                    DefaultTableModel model = (DefaultTableModel) Client.jTable.getModel();
                    model.addRow(new Object[]{event.kind().name(), child});

//                    Thread.sleep(200);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // if directory is created, and watching recursively, then register it and its
                // sub-directories
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            walkAndRegisterDirectories(child);
                        }
                    } catch (IOException x) {
                        // do something useful
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}