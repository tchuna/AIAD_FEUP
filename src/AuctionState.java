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
}
