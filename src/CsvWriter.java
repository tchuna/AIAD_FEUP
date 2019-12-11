import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CsvWriter {
    public static CsvWriter getInstance() throws IOException {
        if( instance==null )
            instance = new CsvWriter();
        return instance;
    }

    public static CsvWriter newInstance() throws IOException {
        instance = new CsvWriter();
        return instance;
    }
    static private CsvWriter instance;
    // category of item
//    private String itemCategory;
    // start price of item
    private int itemStartingPrice;
    // Ammount of money available of the analysed bidder
    private int budget;
    // How aggressive the bidder is - [0-5]
    private int agressivenessLevel;
    // Is the bidder interested in the product in auction?
    private boolean isInterestedInItem;
    // bidders in the auction ==> <AGRESSLEVEL-ISINTERESTED-BUDGET>
    private List<HashMap> biddersInAuction = new ArrayList<>();

    // Has the bidder won the auction
    private boolean hasWon;
    // Value paid by bidder who won
    private int paidValue;

    private float [] percentageAgresssive ;
    private float percentageInterested;
    // percentage of bidders with budget bigger than bidder analysed
    private float percentageBudgetBigger;
    // write in csv
    FileWriter writer;


    private CsvWriter() throws IOException {
        writer = new FileWriter("src/auctionsDataRapidMiner.csv", true);
        percentageAgresssive= new float [6];
    }

    public void setItemInAuction_startPrice(int itemInAuction) {
        this.itemStartingPrice = itemInAuction;
    }

    public void setBudget(int budget) {
        this.budget = budget;
    }

    public void setAgressivenessLevel(int agressivenessLevel) {
        this.agressivenessLevel = agressivenessLevel;
    }

    public void setInterestedInItem(boolean interestedInItem) {
        isInterestedInItem = interestedInItem;
    }

    public void addBiddersToAuction(Integer agress, Boolean isInterested, Integer budget) {
        HashMap b = new HashMap();
        b.put("agressiveness_level",agress);
        b.put("is_interested_in_product",isInterested);
        b.put("budget",budget);
        this.biddersInAuction.add(b);
    }

    public void setHasWon(String bidderWon) {
        this.hasWon = (bidderWon.equals("Bidder0"));
    }

    public void setPaidValue(int paidValue) {
        this.paidValue = paidValue;
    }


    //Info produto (starting price)  | Dados de bidder a prever -- agr,inter,bud | no opositores | %agressivos-nivel1 | %agressivos-nivel2 .....| %agressivos-nivel5 | %interessados | %budget>meu | GANHA/PERDE | VALOR PAGO
    public void writeCSV(){
        calculatePercentages();
        try {
            System.out.println(itemStartingPrice+";"+
            agressivenessLevel+";"+
            isInterestedInItem+";"+
            budget+";"+
            biddersInAuction.size()+";"+
            percentageAgresssive[0] + ";"+
            percentageAgresssive[1] + ";"+
            percentageAgresssive[2] + ";"+
            percentageAgresssive[3] + ";"+
            percentageAgresssive[4] + ";"+
            percentageAgresssive[5] + ";"+
            percentageInterested+";"+
            percentageBudgetBigger+";"+
            hasWon+";"+
            paidValue+"\n");


            // dados do produto
            writer.write(itemStartingPrice+";");


            // dados do bidder a prever
            writer.write(agressivenessLevel+";");
            writer.write(isInterestedInItem+";");
            writer.write(budget+";");


            // dados de opositores
            writer.write(biddersInAuction.size()+";");
            for(int i = 0; i<percentageAgresssive.length;i++) {
                writer.write(percentageAgresssive[i] + ";");
            }
            writer.write(percentageInterested+";");
            writer.write(percentageBudgetBigger+";");


            // info de auction
            writer.write(hasWon+";");
            writer.write(paidValue+"\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        writer.close();
    }

    private void calculatePercentages(){
        int [] noAgressives = {0,0,0,0,0,0};
        int interested=0;
        int moreBudget = 0;
        int noBidders = biddersInAuction.size();
        for (int i=0 ; i<noBidders; i++ ){
            noAgressives[(Integer)biddersInAuction.get(i).get("agressiveness_level")] ++;
            if ((Boolean)biddersInAuction.get(i).get("is_interested_in_product")) interested++;
            if ((Integer)biddersInAuction.get(i).get("budget")>budget) moreBudget++;
        }

        for (int i=0; i<noAgressives.length; i++){
            percentageAgresssive[i]=(float)noAgressives[i]/noBidders;
        }
        percentageBudgetBigger = (float)moreBudget/noBidders;
        percentageInterested = (float)interested/noBidders;
    }

}
