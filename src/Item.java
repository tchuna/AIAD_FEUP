public class Item {

    private String name;
    private String category;
    private int startingPrice;
    private int maxRaise;

    public Item(String name, String category, int startingPrice) {
        this.name = name;
        this.category = category;
        this.startingPrice = startingPrice;
        this.maxRaise=2*startingPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(int startingPrice) {
        this.startingPrice = startingPrice;
    }

    public int getMaxRaise() {
        return maxRaise;
    }

}
