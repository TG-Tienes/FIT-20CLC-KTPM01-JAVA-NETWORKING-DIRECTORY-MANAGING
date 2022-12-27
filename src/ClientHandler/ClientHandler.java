package ClientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable{
    Thread t;
    Socket s;
    public String name = "";
    DataInputStream dInput;
    DataOutputStream dOutput;
    public static HashMap<Integer, ClientHandler> clientList = new HashMap<>();

    public ClientHandler(Socket sk, int id){
        try{
            this.s = sk;
            dInput = new DataInputStream(new DataInputStream(sk.getInputStream()));
            dOutput = new DataOutputStream(new DataOutputStream(sk.getOutputStream()));
            this.name = dInput.readUTF();
            clientList.put(id, this);

//            System.out.println("Name: " + this.name);
        } catch (Exception e){
            System.out.println("Handler constructor exception: " + e);
        }

    }

    @Override
    public void run(){
//        String msg;


        while (true){
            try{
                new Thread(() -> {
                    String msg;
                    while (true){
                        try {
                            msg = dInput.readUTF();
                            System.out.println("Received from " + this.name + ": " + msg);
                        }
                        catch (Exception e){
                            break;
                        }
                    }}).start();

                while (true) {
                    Scanner sc = new Scanner(System.in);
                    ClientHandler temp = clientList.get(0);

                    String sendMSG = "";
                    if (sc.hasNextLine()) {
                        sendMSG = sc.nextLine();
                    }
                    try {
                        temp.dOutput.writeUTF(sendMSG);
                        temp.dOutput.flush();
                    } catch (Exception e) {
                        System.out.println("Exception: " + e);
                    }
                    System.out.println("Sent msg: " + sendMSG);
                }
            }
            catch (Exception e){
                try{
                    this.s.close();
                } catch (Exception ee){
                    System.out.println("Close socket exception: " + e);
                }
                System.out.println("Handler run exception: " + e);
                break;
            }
        }


    }
}
