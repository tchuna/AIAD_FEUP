//package com.aiad;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class BidderAgent extends Agent{
    //If the item is not in the bidders insterest, and the current bid exceed 5% of his budget e quits bidding
    private static final float BUDGET_PERCENT_NOINTEREST = 0.05f;
    //Bidders with  no interest in product highest bidding percentage --> can only bid 3% of its budget
    private static final float HIGHEST_BIDDING_PERCENTAGE_NOINTEREST_AGRESS_2_3 = 0.03f;
    //% of budget with interest agress ==1
    private static final float HIGHEST_BIDDING_PERCENTAGE_WITHINTEREST_AGRESS_1 = 0.5f;
    //% of budget with interest agress ==2
    private static final float HIGHEST_BIDDING_PERCENTAGE_WITHINTEREST_AGRESS_2 = 0.7f;

    // Ammount of money available to spend in this auction
    private int budget;
    // How aggressive the bidder is - [0-5]
    private int agressivenessLevel;

    private boolean isInterestedInItem=false;

    private int currentRound;
    private AuctionState auctionState;
    private ArrayList<String> interests = new ArrayList<String>();

    // Put agent initializations here
    protected void setup() {
        // Get the bidder's characteristics
        Object[] args = getArguments();

        if (args != null && args.length > 0) {

            printInTerminal("Args: "+ Arrays.toString(args));
            budget=Integer.parseInt((String)args[0]);
            agressivenessLevel=Integer.parseInt((String)args[1]);
           if(args.length >2) {
                for (int i = 2; i < args.length; i++) {
                  interests.add((String) args[i]);
               }
           }

        }

//        String initalValeus = "budget= "+budget+", and my interest are: ";
//        for(int i =0; i<interests.size();i++){
//            initalValeus+= interests.get(i)+", ";
//        }
//
//        printInTerminal(initalValeus);

        // Register the BIDDER service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("bidding-agent");
        sd.setName("Best-Auctions");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST) );
        addBehaviour(new AchieveREResponderBidder(this, template));
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        printInTerminal("Bidder-agent "+getAID().getName()+" terminating.");
    }

    //------------------------------------------------------------------------------------------------------------------
    // Accept or Refuse entering the Auction.
    // AKA: FIPA-Request-Protocol
    //------------------------------------------------------------------------------------------------------------------
    private class AchieveREResponderBidder extends AchieveREResponder {
        public AchieveREResponderBidder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
            printInTerminal(": REQUEST to enter auction received from "+request.getSender().getName()+". Item is "+request.getContent());

            //---------------------
            auctionState = new AuctionState();
            String[] splits = request.getContent().split("-");
            auctionState.setItemBeingAutioned(new Item(splits[0], splits[2], Integer.parseInt(splits[1])));

            //--------------------
            //Update item initial price from ACLMessage

            if (isParticipatingInAuction(Integer.parseInt(request.getContent().split("-")[1]))) {
                printInTerminal(": I will send AGREE to participate.");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);

                //Creates AuctionState
//                auctionState = new AuctionState();
//                String[] splits = request.getContent().split("-");
//                auctionState.setItemBeingAutioned(new Item(splits[0], splits[2], Integer.parseInt(splits[1])));

                return agree;
            } else {
                printInTerminal(": I will send REFUSE to participate.");
                ACLMessage refuse = request.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }
        }

        /**
         * This method is called after the execution of the handleRequest() method if
         * no response was sent or the response was an AGREE message.
         */
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {

            //Create message template for contract net and adding the new behaviuor to the Bidder.
            MessageTemplate template = MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP) );
            myAgent.addBehaviour(new AuctionRoundBidder(myAgent, template));

            //If Bidder Agreed, send INFORM msg to Auctioneer, acording to FIPA-REQUEST-PROTOCOL
            printInTerminal(": I will send INFORM Auctioneear acording to FIPA-REQUEST rules.");
            ACLMessage inform = request.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            return inform;
        }

        //TODO: Decide if bidder ir entering the auction or not.
        private boolean isParticipatingInAuction(int price){
            isInterestedInItem = interests.contains(auctionState.getItemBeingAutioned().getCategory());
            //se nao for interesse e agressivvidade =1 nao vai
            if(!isInterestedInItem && agressivenessLevel <=1)
                return false;
            //----------- se nao for interesse so vai se custar menos de 5% do budget
            if(!isInterestedInItem && auctionState.getItemBeingAutioned().getStartingPrice() > budget*BUDGET_PERCENT_NOINTEREST)
                return false;
            return (price<=budget); //atm the bidder always participates if his/her budget is higher than the item price
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // Play the Auction bidding rounds
    // AKA: FIPA-Contract-Net
    //------------------------------------------------------------------------------------------------------------------
    private class AuctionRoundBidder extends ContractNetResponder{
        public AuctionRoundBidder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            printInTerminal(" CFP received from "+cfp.getSender().getName()+". "+cfp.getContent());
            updateCurrentRound(cfp.getContent());

            int proposal = prepareBid();
            if (proposal!=0) {
                // We provide a proposal
                printInTerminal(" (PROPOSE) Item price is "+auctionState.getCurrentPrice()+"$, in "+cfp.getContent()+" I'm bidding "+proposal+"$.");
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf(proposal));
                return propose;
            }
            else {
                // We refuse to provide a proposal
                printInTerminal(" (REFUSE) Item price is "+auctionState.getCurrentPrice()+"$, in "+cfp.getContent()+" I'm refusing to bid and leaving the Auction.");
                ACLMessage refuse = cfp.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
            printInTerminal(" (Recieved ACCEPT_PROPOSAL)I had the highest biddder this round.");

            if(accept!=null)
                updateAuctionStateFromMsg(accept.getContent());

            if (true) {
                printInTerminal(" (SENDING INFORM) I'm sending inform_done since I had the highest bid this round )");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            }
            else {
                printInTerminal(": Action execution failed");
                throw new FailureException("unexpected-error");
            }
        }

        @Override
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            printInTerminal(" (Recieved REJECT_PROPOSAL) My bid was not the highest this round.");
            if(reject.getContent()!=null){
                updateAuctionStateFromMsg(reject.getContent());
            }
            super.handleRejectProposal(cfp, propose, reject);
        }

        //returns value to bid OR zero if the bidder does not want to bid
        private int prepareBid() {
            if(auctionState.getCurrentPrice()>=budget)
                return 0;

            //If the item is not in the bidders insterest, and the current bid axceed 5% of his budget e quits bidding
            if(!isInterestedInItem && auctionState.getCurrentPrice()>=(budget*BUDGET_PERCENT_NOINTEREST)){
                return 0;
            }

            Random r = new Random();
            float percentageOfBudget; //the percentage value that the current price represents related to the bidder's budget
            percentageOfBudget = (float)auctionState.getCurrentPrice()/budget;

            int bid=0;

            float maxBidPercentage;
            float minBidPercentage;
            int maxBid;
            int minBid;
            int moneyAvailableforBidding;

            if(!isInterestedInItem){
                // the maximum of bid the bidder can offer [0-40]
                maxBidPercentage=(float)(agressivenessLevel-1)/10;
                // the minuimum of bid the bidder can offer [0-40]
                minBidPercentage=(float)(agressivenessLevel-2)/10;
                maxBid=(int)(auctionState.getItemBeingAutioned().getMaxRaise()*maxBidPercentage);
                minBid=(int)(auctionState.getItemBeingAutioned().getMaxRaise()*minBidPercentage);
                if(agressivenessLevel<=3){ // 2-3 agress
                    // calculate how much money he can bid (according to percentage of budget)
                    moneyAvailableforBidding=(int)(HIGHEST_BIDDING_PERCENTAGE_NOINTEREST_AGRESS_2_3*budget)-auctionState.getCurrentPrice();
                }
                else{ // 4-5 agress
                    // calculate how much money he can bid (according to percentage of budget)
                    moneyAvailableforBidding=(int)(BUDGET_PERCENT_NOINTEREST*budget)-auctionState.getCurrentPrice();
                }

               // System.out.println("**************\nMONEY AVAILABLE -> "+moneyAvailableforBidding+" / MINBID -> "+minBid+" / MAXBID -> "+maxBid +"  FOR AGENT "+getLocalName());
                //////////////////
                // if money available for bidding bigger than minBid, bids
                if(moneyAvailableforBidding>=minBid){
                    // random between minbid and moneyAvailableforBidding
                    if(moneyAvailableforBidding<maxBid){
                        bid = r.nextInt(moneyAvailableforBidding-minBid+1)+minBid;
                    }
                    // random between minbid and maxbid
                    else{
                        bid = r.nextInt(maxBid-minBid+1)+minBid;
                    }
                }
                else{
                    if(moneyAvailableforBidding>0)
                        bid = moneyAvailableforBidding;
                }
                //////////////////
            }

            //HAS INTEREST
            else{
                // the maximum of bid the bidder can offer [30-100]
                minBidPercentage=(float)(agressivenessLevel*14)/100;
                // the minuimum of bid the bidder can offer
                if(agressivenessLevel == 5){
                    maxBidPercentage=1;
                }else{
                    maxBidPercentage=minBidPercentage+0.14f;
                }

                maxBid=(int)(auctionState.getItemBeingAutioned().getMaxRaise()*maxBidPercentage);
                minBid=(int)(auctionState.getItemBeingAutioned().getMaxRaise()*minBidPercentage);

                float raisePercentage_lastRound = auctionState.calculatePercentageOfRaise_lastTwoRounds();
                // if agressiveness is 1, checks if the price has risen more than 170%
                if(agressivenessLevel<=1) { // 0-1 agress
                    if (raisePercentage_lastRound>=(0.85f)){//price has risen 170% or more
                        printInTerminal("PRICE has gone up too quickly. I'm out");
                        return 0;
                    }
                    moneyAvailableforBidding = (int)(HIGHEST_BIDDING_PERCENTAGE_WITHINTEREST_AGRESS_2*budget)-auctionState.getCurrentPrice();
                }
                else if( agressivenessLevel<=2){ //2 agress
                    if (raisePercentage_lastRound>=(0.95f)) { //price has risen 190% or more
                        printInTerminal("PRICE has gone up too quickly. I'm out");
                        return 0;
                    }
                    moneyAvailableforBidding = (int)(HIGHEST_BIDDING_PERCENTAGE_WITHINTEREST_AGRESS_2*budget)-auctionState.getCurrentPrice();
                }
                else{ // agress >= 3 ->> can bet whole budget
                    moneyAvailableforBidding=budget-auctionState.getCurrentPrice();
                }

                //System.out.println("**************\nMONEY AVAILABLE -> "+moneyAvailableforBidding+" / MINBID -> "+minBid+" / MAXBID -> "+maxBid +"  FOR AGENT "+getLocalName());
                //////////////////
                // if money available for bidding bigger than minBid, bids
                if(moneyAvailableforBidding>=minBid){
                    // random between minbid and moneyAvailableforBidding
                    if(moneyAvailableforBidding<maxBid+auctionState.getCurrentPrice()){
                        bid = r.nextInt(moneyAvailableforBidding-minBid+1)+minBid;
                     //   printInTerminal("moneyavaila= "+ moneyAvailableforBidding+"<maxbid+curent= "+((int)maxBid+ (int) auctionState.getCurrentPrice()));
                       // printInTerminal("RANDOM BID 2=" +bid);
                    }
                    // random between minbid and maxbid
                    else{
                        bid = r.nextInt(maxBid-minBid+1)+minBid;
                       // printInTerminal("RANDOM BID 1=" +bid);
                    }
                }
                else{

                    if(moneyAvailableforBidding>0)
                        bid = moneyAvailableforBidding;
                }
                //////////////////
            }


//            if( auctionState.getRound()>0 ){
//
//            }

//            Random r = new Random();
//            int bid = r.nextInt(budget-auctionState.getCurrentPrice())+auctionState.getCurrentPrice()+1; //randomizing between currprice and budget -- this is stupid
            return bid+auctionState.getCurrentPrice();
        }
    }

    private void updateAuctionStateFromMsg(String msg){
        String [] splits = msg.split("-");
        RoundState rState = new RoundState(this.currentRound, splits[0],  Integer.parseInt(splits[1]));
        auctionState.updateRoundHistory(rState);
    }

    private void updateCurrentRound(String msg){
        String [] splits = msg.split("-");
        if(splits[0].equals("Round"))
            this.currentRound=Integer.parseInt(splits[1]);
        else
            printInTerminal("AN ERROR HAS OCCURED WHILE RECEIVING CFP");
    }

    private void printInTerminal(String msg ){
        System.out.println("BIDDER "+getLocalName()+" -> " +msg);
    }
}
