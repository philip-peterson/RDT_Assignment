import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.charset.*;

public class NetworkServer {
   /* Static vars */

      public static int SOCKET_NR = 65003;

   /* End static vars */

   public Thread receiverThread = null;
   public Thread senderThread = null;

   private static ServerSocket makeSocketOrDie(int port) {
      ServerSocket s = null;
      try {
         s = new ServerSocket(port);
      }
      catch (IOException|SecurityException e) {
         System.err.println(String.format("Error: Could not make socket on port %d", port));
         System.exit(ExitCodes.SOCKALLOC);
      }
      return s;
   }

   private Socket acceptConnectionOrDie() {
      Socket s = null;
      try {
         s = serverSock.accept();
      }
      catch (
            IOException
               |SecurityException
               |IllegalBlockingModeException
            e
         ) {
         System.err.println("Error: Could not accept connection: " + e.getMessage());
         System.exit(ExitCodes.SOCKACCEPT);
      }
      return s;
   }

   public ServerSocket serverSock;

   public NetworkServer(ServerSocket serverSock) {
      this.serverSock = serverSock;
   }

   public void ioError(IOException e) {
      System.err.println("Error: I/O -- " + e.getMessage());
      System.exit(ExitCodes.SOCKIO);
   }

   public void run() {
      doAwaitConnection(true);
      doAwaitConnection(false);
   }

   /**
    * @param boolean isReceiver false if sender, true if receiver
    */
   public void doAwaitConnection(boolean isReceiver) {
      String clientName = isReceiver ? "receiver" : "sender";

      System.out.println(String.format("Waiting for connection from %s...", clientName));
      Socket sock = this.acceptConnectionOrDie();

      OutputStream out = null;
      InputStream in = null;

      try {
         out = sock.getOutputStream();
         in = sock.getInputStream();

         int ident = in.read();
         if (ident == (isReceiver ? 1 : 0)) {
            System.err.println(
               String.format("Expected %s to connect, but something else happened. Quitting.", clientName)
            );
            System.exit(ExitCodes.WRONG_CLIENT);
         }
      }
      catch (IOException e) {
         this.ioError(e);
      }


      Thread thread;
      if (isReceiver) {
         thread = new NetworkServerReceiverThread(sock, in, out, this);
      }
      else {
         thread = new NetworkServerSenderThread(sock, in, out, this);
      }
      thread.start();
   }

   public static void main(String[] args ) {
      ServerSocket ss = makeSocketOrDie(SOCKET_NR);
      NetworkServer ns = new NetworkServer(ss);
      ns.run();
   }
}