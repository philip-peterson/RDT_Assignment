import java.net.*;
import java.io.*;
import java.nio.charset.*;

public class Receiver extends GenericClient {

   /* private vars */
   private String host;

   public Receiver(String host, int port) {
      super(host, port);
   }

   public void run() {
      super.run();

      try {
         out.write(ProgramCodes.RECEIVER);
         out.flush();
      }
      catch (IOException e) {
         ExitCodes.ExitWithMessage(ExitCodes.SOCKIO);
      }

      try {
         while(true) {
            Packet rcvpkt;
            System.out.println("waiting for packet");
            rcvpkt = rdt_rcv();
            System.out.println("Got packet"+rcvpkt.seq+" (id="+rcvpkt.id+", checksum="+rcvpkt.checksum+", msg="+rcvpkt.content+")!!");
         }
      }
      catch (IOException e) {
         ioError();
      }

   }

   private int _curSeq = 0;
   Packet rdt_rcv() throws IOException {
      Packet pkt;
      while(true) {
         System.out.println("Waiting in rdt_rcv...");
         int wantsExit = Util.unsignedToSigned(in.read());
         System.out.println(wantsExit);
         if (wantsExit == -1) {
            System.out.println("Exiting successfully.");
            System.exit(0);
         }
         System.out.println("About to receive packet");
         pkt = Packet.readFromStream(in);
         System.out.println("got it");
         if (pkt.isCorrupt()) {
            System.out.println("Received corrupt Packet (seq="+_curSeq+",id="+pkt.id+",msg="+pkt.content+"). Sending other ack.");
            Ack ack = new Ack((byte)(1 - _curSeq));
            ack.writeToStreamAndFlush(out);
            continue;
         }
         else {
            Ack ack = new Ack(pkt.seq);
            _curSeq = 1 - pkt.seq;
            // don't bother with sending the "QUIT" flag; the receiver never sends these, only the sender
            ack.writeToStreamAndFlush(out);
            break;
         }
      }
      return pkt;
   }

   public static void main(String[] args) {
      if (args.length != 2) {
         System.err.println("Usage: receiver HOST PORT");
         System.exit(ExitCodes.USAGE);
      }
      int port = Integer.parseInt(args[1]);
      String host = args[0];
      Receiver r = new Receiver(host, port);
      r.run();
   }
}
