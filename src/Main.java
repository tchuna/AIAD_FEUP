import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.Boot;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class Main {
    private static ArrayList<String> getArrayListFromArray(String [] array) {
        if(array==null || array.length==0 )
            return null;
        ArrayList<String> availableInterestsList = new ArrayList<>();
        for (int i = 0; i < array.length; i++) availableInterestsList.add(array[i]);
        return availableInterestsList;
    }
    private static int getRandomNumberInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
    private static String[] getRandomBidderInterests(String [] availableInterests) {
        int noInterests = getRandomNumberInRange(0,categories.length-1);
        if (noInterests==0)
            return null;
        String [] interests = new String[noInterests];
        int cntr = 0;
        ArrayList<String> availableInterestsList = getArrayListFromArray(availableInterests);
        while ( noInterests>0 ){
            int interestToAdd = getRandomNumberInRange(0,availableInterestsList.size()-1);
            interests[cntr] = availableInterestsList.get(interestToAdd);
            availableInterestsList.remove(interestToAdd);
            cntr++;
            noInterests--;
        }
        return interests;
    }
    private static String stringifyInterests(String [] ints) {
        if(ints!=null){
            String ret = "";
            for (int i=0; i<ints.length ; i++){
                ret += ("," + ints[i]);
            }
            return ret;
        }
        return "";
    }

    public static String [] categories = {"Furniture", "Art", "Cars", "Tech"};
    public static void main(String[] args)  throws IOException{
            String paramsAgents = "";
            ///// Creates item randomly ////
            // random category
            String itemCategory = categories[getRandomNumberInRange(0, categories.length - 1)];
            // random starting price, from 10 to 1000
            int startingPrice = getRandomNumberInRange(10, 500);
            CsvWriter.newInstance().setItemInAuction_startPrice(startingPrice);

            ///// Creates bidders randomly ////
            // Num bidders is random between 2 - 50
            int noBidders = getRandomNumberInRange(2, 50);
            // create bidderAgents
            for (int i = 0; i < noBidders; i++) {
                paramsAgents += ";Bidder" + i + ":BidderAgent(";

                // Bidder's agrressiveness level
                int agressLevel = getRandomNumberInRange(0, 5);
                // Bidder's budget [750 - 100,000]
                int budget = getRandomNumberInRange(750, 100000);
                // Generate random bidder interests
                String[] categ = categories;
                String[] interests = getRandomBidderInterests(categ);
                // check interest
                ArrayList<String> interestsList = getArrayListFromArray(interests);
                boolean isBidderInterested = (interestsList != null && interestsList.contains(itemCategory));

                // Agent to which csv info is related to //
                if (i == 0) {
                    CsvWriter.getInstance().setAgressivenessLevel(agressLevel);
                    CsvWriter.getInstance().setBudget(budget);
                    CsvWriter.getInstance().setInterestedInItem(isBidderInterested);
                }
                // Other bidder agents
                else {
                    String bidderInAuction = "";
                    CsvWriter.getInstance().addBiddersToAuction(agressLevel, isBidderInterested, budget);
                }
                paramsAgents += budget + "," + agressLevel
                        + stringifyInterests(interests) + ")";
            }

        System.out.println(paramsAgents);
        String[] param = new String[2];

        param[ 0 ] = "-gui";
        param[ 1 ] = "Auctionner:AuctioneerAg" + "(item" + "," + itemCategory + "," +  startingPrice + ");" +
                        paramsAgents;


//        param[ 0 ] = "-gui";
//        param[ 1 ] = "Auctionner:AuctioneerAg;" +
//                        "A:BidderAgent(1000,1, Art, Furniture, Cars);" +
//                        "B:BidderAgent(500,1, Art, Furniture, Cars);" +
//                        "C:BidderAgent(3000,2, Art, Furniture, Cars);" +
//                        "D:BidderAgent(500,3, Art, Furniture, Cars);" +
//                        "E:BidderAgent(1000,3, Art, Furniture, Cars);" +
//                        "E1:BidderAgent(1000,4, Art, Furniture, Cars);" +
//                        "F:BidderAgent(200,5, Art, Furniture, Cars);" +
//                        "G:BidderAgent(700,5, BD);" +
//                        "H:BidderAgent(7000,2, BD,Tech);" +
//                        "I:BidderAgent(3500,5, Cosplay);" +
//                        "J:BidderAgent(7000,2, Cosplay,Tech);" +
//                        "K:BidderAgent(1000,1);" +
//                        "L:BidderAgent(500,1);" +
//                        "M:BidderAgent(3000,2);" +
//                        "N:BidderAgent(500,3, Music, Tech);" +
//                        "O:BidderAgent(1000,3);" +
//                        "P:BidderAgent(200,5, Tech);" +
//                        "Q:BidderAgent(1000,5);" +
//                        "R:BidderAgent(10000,5, Music)";
        Boot.main( param );
    }
}