import java.util.ArrayList;

public class AuctionState {
    private Item itemBeingAutioned;
    private ArrayList<RoundState> roundHistory;

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

    private void updateRoundHistory(RoundState roundState){
        this.roundHistory.add(roundState);
    }

    private int getRound(){
        return this.roundHistory.get(this.roundHistory.size()-1).getRoundNr();
    }
}
