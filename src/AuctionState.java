import java.util.ArrayList;

public class AuctionState {
    private Item itemBeingAutioned;
    private ArrayList<RoundState> roundHistory;

    public AuctionState(Item item){
        this.itemBeingAutioned = item;
        roundHistory = new ArrayList<RoundState>();
    }

    public AuctionState(){
        roundHistory = new ArrayList<RoundState>();
    }

    public Item getItemBeingAutioned() {
        return itemBeingAutioned;
    }

    public void setItemBeingAutioned(Item itemBeingAutioned) {
        this.itemBeingAutioned = itemBeingAutioned;
    }

    public ArrayList<RoundState> getRoundHistory() {
        return roundHistory;
    }

    public void setRoundHistory(ArrayList<RoundState> roundHistory) {
        this.roundHistory = roundHistory;
    }

    public void updateRoundHistory(RoundState roundState){
        this.roundHistory.add(roundState);
    }

    public RoundState getRound(){
        if (roundHistory.size() == 0) return null;
        return this.roundHistory.get(this.roundHistory.size()-1);
    }

    public int getCurrentPrice() {
        if (roundHistory.size() == 0) return itemBeingAutioned.getStartingPrice();
        return roundHistory.get(this.roundHistory.size()-1).getCurrentPrice();
    }

    public String getLastAcceptedProposal_Bidder(){
        return roundHistory.get(this.roundHistory.size()-1).getCurrentHigestBidder();
    }
    public int getLastAcceptedProposal_Value(){
        return roundHistory.get(this.roundHistory.size()-1).getCurrentPrice();
    }

    /**
     * Calculates the percentage of raise --> how much the price has risen in the last two
     * ROUNDS in relation to max raise value
     * @return
     */
    public float calculatePercentageOfRaise_lastTwoRounds(){
        float percentage = 0f;
        if(getCurrentRoundNumber()>0){
            if(getCurrentRoundNumber()==1){
                percentage = (float)(getCurrentPrice() - itemBeingAutioned.getStartingPrice())/itemBeingAutioned.getMaxRaise();
            }
            else{
                percentage = (float)(getCurrentPrice() - roundHistory.get(this.roundHistory.size()-2).getCurrentPrice())/itemBeingAutioned.getMaxRaise();
            }
        }
        return percentage;
    }

    public Integer getCurrentRoundNumber(){
        return ((getRound() == null)?0: getRound().getRoundNr()+1);
    }
}
