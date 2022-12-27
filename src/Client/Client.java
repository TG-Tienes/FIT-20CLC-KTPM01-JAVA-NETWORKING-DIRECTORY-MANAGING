package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class Client {
   Socket s;
   DataInputStream dInput;
   DataOutputStream dOutput;
   String name, dir;

   Client(Socket s, String name, String dir){
       try  {
           this.s = s;
           dInput = new DataInputStream(new DataInputStream(s.getInputStream()));
           dOutput = new DataOutputStream(new DataOutputStream(s.getOutputStream()));
           this.name = name;
           this.dir = dir;
       }
       catch (Exception e){

       }
   }

   public void sendMessage() throws IOException {
       WatchService watcher = FileSystems.getDefault().newWatchService();
       Path dir = Paths.get(this.dir);
       dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
               StandardWatchEventKinds.ENTRY_MODIFY);

       System.out.println("Watch Service registered for dir: " + dir.getFileName());
       WatchKey key = null;

       try{
           dOutput.writeUTF(name);

           Scanner sc = new Scanner(System.in);
           while (this.s.isConnected()){
//               String msg = sc.nextLine();

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

                   Path fileName = dir.resolve((Path) event.context());
                   if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                       dOutput.writeUTF("CREATE" + fileName);
                       System.out.printf("A new file %s was created.%n", fileName.getFileName());
                   } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                       dOutput.writeUTF("MODIFY" + fileName);
                       System.out.printf("A file %s was modified.%n", fileName.getFileName());
                   } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                       dOutput.writeUTF("DELETE" + fileName);
                       System.out.printf("A file %s was deleted.%n", fileName.getFileName());
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
                       System.out.println("Received" + msg);
                   }
                   catch (Exception e){

                   }
               }
           }
       }).start();
   }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter name: ");
        String name = sc.nextLine(), dir;

        System.out.print("Enter path: ");
        dir = sc.nextLine();

        try {
            Socket s = new Socket("localhost", 3000);
            Client cl = new Client(s, name, dir);
            cl.listenMessage();
            cl.sendMessage();
        }
        catch (Exception e){

        }

    }
}
