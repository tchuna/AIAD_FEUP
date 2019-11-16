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
                     "Ana:BidderAgent(1000,1);" +
                     "Pedro:BidderAgent(500,4);" +
                     "Jorge:BidderAgent(300,5);" +
                     "Hugo:BidderAgent(800,3);" +
                     "Maria:BidderAgent(150,4);" +
                     "Tchuna:BidderAgent(620,2)";
        Boot.main( param );
    }
}