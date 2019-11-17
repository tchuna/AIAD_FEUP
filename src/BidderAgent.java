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
    // Ammount of money available to spend in this auction
    private int budget;
    // How aggressive the bidder is - [0-5]
    private int agressivenessLevel;

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
            //se nao for interesse e agressivvidade =1 nao vai
            if(!interests.contains(auctionState.getItemBeingAutioned().getCategory()) && agressivenessLevel <=1)
                return false;
            //-----------
            //----------- se nao for interesse so vai se custar menos de 10% do budget
            if(!interests.contains(auctionState.getItemBeingAutioned().getCategory()) && auctionState.getItemBeingAutioned().getStartingPrice() > budget*0.1)
                return false;
            //----------
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

            //If the item is not in the bidders insterest, and the current bid axceed 10% of his budget e quits bidding

            if(!interests.contains(auctionState.getItemBeingAutioned().getCategory()) && auctionState.getCurrentPrice()>=(budget*0.1)){
                return 0;
            }
            //TODO: Evaluate agressiveness here

            float percentageOfBudget; //the percentage value that the current price represents related to the bidder's budget
            percentageOfBudget = (float)auctionState.getCurrentPrice()/budget;
//            if( auctionState.getRound()>0 ){
//
//            }

            Random r = new Random();
            int bid = r.nextInt(budget-auctionState.getCurrentPrice())+auctionState.getCurrentPrice()+1; //randomizing between currprice and budget -- this is stupid
            return bid;
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
