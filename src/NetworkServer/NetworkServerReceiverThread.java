import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class NetworkServerReceiverThread extends NetworkServerThread {
   public NetworkServerReceiverThread(
         Socket sock,
         InputStream in,
         OutputStream out,
         NetworkServer ns
      ) {
      super(sock, in, out, ns);
   }

   private Random r = new Random();

   void run2() throws IOException {
      while(true) {
         Ack ack = Ack.readFromStream(in);
         double rand = r.nextDouble();
         if (true || rand < .5) {
            // PASS -- send it on through
            System.out.println("Will PASS");
            ns.ackQueue.add(ack);
         }
         else if (rand < .75) {
            // CORRUPT
            System.out.println("Will CORRUPT");
            ack.corruptify();
            ns.ackQueue.add(ack);
         }
         else {
            // DROP -- pretend it got lost (do nothing!)
            System.out.println("Will DROP");
         }
      }
   }

   public void run() {
      try {
         run2();
      }
      catch (SocketException e) {
         ExitCodes.ExitWithMessage(ExitCodes.SOCKBROKEN);
      }
      catch (IOException e) {
         ExitCodes.ExitWithMessage(ExitCodes.SOCKIO);
      }
   }
}
