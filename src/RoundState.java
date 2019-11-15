public class RoundState {

    private int roundNr;
    private String currentHigestBidder;
    private int currentPrice;

    public RoundState(int roundNr, String currentHigestBidder, int currentPrice) {
        this.roundNr = roundNr;
        this.currentHigestBidder = currentHigestBidder;
        this.currentPrice = currentPrice;
    }

    public int getRoundNr() {
        return roundNr;
    }

    public void setRoundNr(int roundNr) {
        this.roundNr = roundNr;
    }

    public String getCurrentHigestBidder() {
        return currentHigestBidder;
    }

    public void setCurrentHigestBidder(String currentHigestBidder) {
        this.currentHigestBidder = currentHigestBidder;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }
}
