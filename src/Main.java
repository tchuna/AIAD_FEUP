import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.Boot;



public class Main {

    public static void main(String[] args) {

        String[] param = new String[2];
        param[ 0 ] = "-gui";
        param[ 1 ] = "Auctionner:AuctioneerAg;" +
                        "Ana:BidderAgent(1000,1, Art, Furniture, Cars);" +
                        "Pedro:BidderAgent(500,1, Art, Furniture, Cars);" +
                        "Jorge:BidderAgent(3000,2, Art, Furniture, Cars);" +
                        "Hugo:BidderAgent(500,3, Art, Furniture, Cars);" +
                        "Maria:BidderAgent(1000,3, Art, Furniture, Cars);" +
                        "Maria:BidderAgent(200,5, Art, Furniture, Cars);" +
                        "Maria:BidderAgent(700,5, BD);" +
                        "Maria:BidderAgent(7000,2, BD);" +
                        "Maria:BidderAgent(700,5, Houses);" +
                        "Maria:BidderAgent(7000,2, Houses);" +
                        "Maria:BidderAgent(1000,1);" +
                        "Maria:BidderAgent(500,1);" +
                        "Maria:BidderAgent(3000,2);" +
                        "Maria:BidderAgent(500,3);" +
                        "Maria:BidderAgent(1000,3);" +
                        "Maria:BidderAgent(200,5);" +
                        "Maria:BidderAgent(1000,5);" +
                        "Tchuna:BidderAgent(1000,3, Art, Furniture, Cars)";
        Boot.main( param );
    }
}