import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.Boot;

import java.util.Random;


public class Main {

    public static void main(String[] args) {
        Random r = new Random();
        int bid1 = r.nextInt(6-6 +1)+6  ;
        System.out.println(bid1);
//        int bid = r.nextInt(1)+6+1;
//        System.out.println(bid);
        String[] param = new String[2];
        param[ 0 ] = "-gui";
        param[ 1 ] = "Auctionner:AuctioneerAg;" +
                        "A:BidderAgent(1000,1, Art, Furniture, Cars);" +
                        "B:BidderAgent(500,1, Art, Furniture, Cars);" +
                        "C:BidderAgent(3000,2, Art, Furniture, Cars);" +
                        "D:BidderAgent(500,3, Art, Furniture, Cars);" +
                        "E:BidderAgent(1000,3, Art, Furniture, Cars);" +
                        "E1:BidderAgent(1000,4, Art, Furniture, Cars);" +
                        "F:BidderAgent(200,5, Art, Furniture, Cars);" +
                        "G:BidderAgent(700,5, BD);" +
                        "H:BidderAgent(7000,2, BD,Tech);" +
                        "I:BidderAgent(3500,5, Cosplay);" +
                        "J:BidderAgent(7000,2, Cosplay,Tech);" +
                        "K:BidderAgent(1000,1);" +
                        "L:BidderAgent(500,1);" +
                        "M:BidderAgent(3000,2);" +
                        "N:BidderAgent(500,3, Music, Tech);" +
                        "O:BidderAgent(1000,3);" +
                        "P:BidderAgent(200,5, Tech);" +
                        "Q:BidderAgent(1000,5);" +
                        "R:BidderAgent(10000,5, Music)";
        Boot.main( param );
    }
}