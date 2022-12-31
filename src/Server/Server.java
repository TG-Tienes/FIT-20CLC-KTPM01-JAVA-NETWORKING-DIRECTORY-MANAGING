package Server;

import ClientHandler.ClientHandler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Objects;

public class Server {
    ServerSocket sv;
    int id = 0;

    public static JFrame window;
    public static int port;
    public static JTable jTable, clientTable;
    public static int currentSelectedUser = -1;
    public static JFileChooser fileChooser;
    public static String chooserDir;
    public static boolean changeFilePath = false, disconnectChoice = false;

    public static JScrollPane dataScrollPane;
    public static JComboBox jComboID;
    public static JButton filterNameButton;
    public static JTextField inputSearchNameTextField;

    Server(int port) {
        try {
            sv = new ServerSocket(port);
            Server.port = port;

            // JFrame
            window = new JFrame("SERVER");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLayout(null);
            window.setBounds(300, 200, 1000, 800);

            window.getContentPane().setBackground(new Color(240, 240, 240));

            // Label
            JLabel uiLabel = new JLabel("SERVER");
            uiLabel.setBounds(450, 15, 200, 30);
            uiLabel.setFont(new Font("Serif", Font.PLAIN, 40));
            window.add(uiLabel);

            // tables


            JLabel clientTableLabel = new JLabel("Click on row to select and change user's directory");
            clientTableLabel.setBounds(200, 50, 400, 30);
            clientTableLabel.setFont(new Font("Serif", Font.PLAIN, 18));
            window.add(clientTableLabel);

            // create table
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER);

            DefaultTableModel jTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Time", "Action", "File Path"}, 0);
            jTable = new JTable(jTableModel);

            DefaultTableModel clientTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Time", "Current Path"}, 0);

            clientTable = new JTable(clientTableModel);

            final TableRowSorter<TableModel> idSorter = new TableRowSorter<>(jTableModel);
            jTable.setRowSorter(idSorter);

            clientTable.setCursor(new Cursor(Cursor.HAND_CURSOR));
            jTable.setCursor(new Cursor(Cursor.HAND_CURSOR));

            jTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
            jTable.getTableHeader().setOpaque(false);
            jTable.getTableHeader().setForeground(new Color(255, 255, 255));
            jTable.getTableHeader().setBackground(new Color(0, 0, 0));

            clientTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
            clientTable.getTableHeader().setOpaque(false);
            clientTable.getTableHeader().setForeground(new Color(255, 255, 255));
            clientTable.getTableHeader().setBackground(new Color(0, 0, 0));

            clientTable.setBounds(200, 80, 600, 150);
            jTable.setBounds(20, 300, 950, 400);

            clientTable.setDefaultEditor(Object.class, null);
            jTable.setDefaultEditor(Object.class, null);

            jTable.setRowHeight(30);
            jTable.getColumnModel().getColumn(0).setMinWidth(10);
            jTable.getColumnModel().getColumn(0).setPreferredWidth(10);
            jTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            jTable.getColumnModel().getColumn(2).setPreferredWidth(40);
            jTable.getColumnModel().getColumn(3).setPreferredWidth(130);
            jTable.getColumnModel().getColumn(4).setPreferredWidth(600);

            clientTable.setRowHeight(30);
            clientTable.getColumnModel().getColumn(0).setMinWidth(10);
            clientTable.getColumnModel().getColumn(0).setPreferredWidth(10);
            clientTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            clientTable.getColumnModel().getColumn(2).setPreferredWidth(40);
            clientTable.getColumnModel().getColumn(3).setPreferredWidth(360);

            for (int i = 0; i < 4; ++i) {
                jTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

            for (int i = 0; i < 3; ++i) {
                clientTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }

//            DefaultTableModel m = (DefaultTableModel) jTable.getModel();

            // scroll pane
            dataScrollPane = new JScrollPane(jTable);
            JScrollPane jTableClientScroll = new JScrollPane(clientTable);

            dataScrollPane.setBounds(20, 300, 950, 400);
            jTableClientScroll.setBounds(200, 80, 600, 150);

            JScrollBar sb = dataScrollPane.getVerticalScrollBar();
            sb.setValue(sb.getMaximum());

            dataScrollPane.getViewport().setBackground(new Color(255, 255, 255));
            jTableClientScroll.getViewport().setBackground(new Color(255, 255, 255));

            window.add(dataScrollPane);
            window.add(jTableClientScroll);

            Server.clientTable.getSelectionModel().addListSelectionListener(event -> {
                if (Server.clientTable.getSelectedRow() > -1) {
                    System.out.println(Server.clientTable.getValueAt(Server.clientTable.getSelectedRow(), 0).toString());
                    Server.currentSelectedUser = (int) Server.clientTable.getValueAt(Server.clientTable.getSelectedRow(), 0);

                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                    fileChooser.setDialogTitle(chooserDir);
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.setAcceptAllFileFilterUsed(false);

//                    JOptionPane.showInputDialog(fileChooser, JOptionPane.CANCEL_OPTION);
                    int val = fileChooser.showOpenDialog(window);
                    chooserDir = String.valueOf((fileChooser.getSelectedFile()));

                    if (isValidPath(chooserDir) && val == JFileChooser.APPROVE_OPTION) {
                        changeFilePath = true;
                        clientTable.setValueAt(chooserDir, Server.clientTable.getSelectedRow(), 3);
                    }
                    Server.clientTable.clearSelection();
                    System.out.println(fileChooser.getSelectedFile());
                }
            });

            // COMBOBOX
            // combobox label
            JLabel comboboxLabel = new JLabel("Filter by ID");
            comboboxLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            comboboxLabel.setBounds(20, 230, 200, 30);
            window.add(comboboxLabel);

            // ID combobox
            String[] comboIDList = {"All"};
            jComboID = new JComboBox(comboIDList);
            jComboID.setBounds(20, 260, 300, 30);
            jComboID.setBackground(new Color(255, 255, 255));

            jComboID.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String filterData = jComboID.getItemAt(jComboID.getSelectedIndex()).toString();
                    idFilter(jTableModel, idSorter, filterData.split("-")[0]);
                }
            });
            window.add(jComboID);

            // SEARCH NAME
            // input search label
            JLabel searchNameLabel = new JLabel("Filter by NAME");
            searchNameLabel.setBounds(400, 230, 200, 30);
            searchNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            window.add(searchNameLabel);

            // input
            inputSearchNameTextField = new JTextField();
            inputSearchNameTextField.setBounds(400, 260, 420, 30);

            inputSearchNameTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    change();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    change();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    change();
                }

                public void change() {
                    filterNameButton.setEnabled(inputSearchNameTextField.getText() != null && !inputSearchNameTextField.getText().equals(""));
                }
            });

            window.add(inputSearchNameTextField);

            // filter button
            filterNameButton = new JButton("Filter");
            filterNameButton.setBounds(830, 260, 62, 30);
            filterNameButton.setBackground(new Color(132, 255, 132));
            filterNameButton.setEnabled(false);
            window.add(filterNameButton);

            filterNameButton.addActionListener(e -> {
                nameFilter(jTableModel, idSorter, inputSearchNameTextField.getText());
            });

            // reset button
            JButton resetFilterButton = new JButton("Reset");
            resetFilterButton.setBounds(900, 260, 68, 30);
            resetFilterButton.setBackground(new Color(255, 61, 61));
            window.add(resetFilterButton);

            resetFilterButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    idSorter.setRowFilter(null);
                }
            });

            window.setVisible(true);
        } catch (IOException e) {
            System.out.println("Server constructor exception: " + e);
        }
    }

    private void idFilter(DefaultTableModel m, TableRowSorter<TableModel> sorter, String data) {
        RowFilter<TableModel, Object> rowFilter = null;
        //If current expression doesn't parse, don't update.
        if (!Objects.equals(data, "All")) {
            try {
                rowFilter = RowFilter.regexFilter(data, 0);
            } catch (java.util.regex.PatternSyntaxException e) {
                System.out.println(e);
                return;
            }
        }
        sorter.setRowFilter(rowFilter);
    }

    private void nameFilter(DefaultTableModel m, TableRowSorter<TableModel> sorter, String nameData) {
        RowFilter<TableModel, Object> rowFilter = null;
        try {
            rowFilter = RowFilter.regexFilter(nameData, 1);
        } catch (java.util.regex.PatternSyntaxException e) {
            System.out.println(e);
            return;
        }

        sorter.setRowFilter(rowFilter);

        // Truong hop key khong ton tai, reset lai table ve hien thi all
        if(sorter.getViewRowCount() == 0){
            JOptionPane.showMessageDialog(window, "Searched Key doesn't exist, table will be reset to ALL");
            sorter.setRowFilter(null);
        }
    }

    public void start() {
        try {
            do {
                System.out.println("Waiting for new client");
                Socket s = sv.accept();

                ClientHandler cl = new ClientHandler(s, id);
                id++;

                Thread t = new Thread(cl);

                t.start();
            } while (!sv.isClosed());
        } catch (IOException e) {
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