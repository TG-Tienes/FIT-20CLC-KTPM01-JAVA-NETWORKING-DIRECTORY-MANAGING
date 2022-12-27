package Server;

import ClientHandler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    ServerSocket sv;
    int port;
    int id = 0;

    Server(int port){
        try{
            sv = new ServerSocket(port);
            this.port = port;
//            this.clientList = new HashMap<>();
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

    public static void main(String[] args) throws IOException {
        Server sv = new Server(3000);
        sv.start();
    }
}
