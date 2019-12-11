//package com.aiad;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;

import java.io.IOError;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class AuctioneerAg extends Agent{
    // The GUI by means of which the user can start an auction
    private CreateAuctionGui myGui;
    // The list of known BIDDER agents
    private List<AID> bidderAgents;
    // Name of item being auctioned
   // private String itemName;
    // Starting price of the item
    //private int startingPrice;

    private AuctionState auctionState;

    private Item itemFurniture = new Item("Furniture", "Furniture", 10);
    private Item itemCar = new Item("Car", "Cars", 600);
    private Item itemHouse = new Item("House", "Houses", 800);
    private Item itemBD = new Item("BD", "BD", 3);
    private Item itemNoInterest = new Item("Manuel Curral", "Manuel Curral", 60);


    // Put agent initializations here
    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String name = (String) args[0];
            String category = (String) args[1];
            Integer startingPice = Integer.parseInt((String) args[2]);
            setAuctionState(new AuctionState(new Item(name, category, startingPice)));
            doWait(3000);
            startAuction();
        }
        else{
            // Create and show the GUI
            myGui = new CreateAuctionGui(this);
            myGui.showGui();
        }
        // auctionState = new AuctionState(itemHouse);
       // startAuction();

    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Close the GUI
//        myGui.dispose();
        // Printout a dismissal message
        printInTerminal("Auctioneer-agent "+getAID().getName()+" terminating.");
    }

    public void setAuctionState(AuctionState as){
        this.auctionState = as;

    }

    /**
     ** Called by the GUI when it's time to start the auction
     */
    public void startAuction() {
        // Behaviour used for setting local variables and printing the start of Auction
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                // Sets the variables values defined in the GUI
                //itemName=name;
                //startingPrice=price;

                printInTerminal("Started the auction of the item --> "+auctionState.getItemBeingAutioned().getName()+" <-- ");
                printInTerminal("\nStarting price is --> "+auctionState.getItemBeingAutioned().getStartingPrice()+" $");
            }
        } );

        // Behaviour used for sending the REQUEST to participate in the auction
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("bidding-agent");
                template.addServices(sd);
                try {
                    doWait(500);
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    printInTerminal("Found the following bidder agents:");
                    // Identifies all bidder agents
                    bidderAgents = new ArrayList<>();
                    for (int i = 0; i < result.length; ++i) {
                        bidderAgents.add(result[i].getName()) ;
                        printInTerminal(bidderAgents.get(i).getName());
                    }
                    // Send the REQUEST to all bidders
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    for (int i = 0; i < bidderAgents.size(); ++i)
                        request.addReceiver(bidderAgents.get(i));
                    request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    request.setContent(auctionState.getItemBeingAutioned().getName()+"-"+auctionState.getItemBeingAutioned().getStartingPrice()+"-"+auctionState.getItemBeingAutioned().getCategory());
//                  request.setConversationId("Auction");
                    request.setReplyWith("req"+System.currentTimeMillis()); // Unique value

                    addBehaviour(new AchieveREInitiatorAuctioneer(myAgent, request));

                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

    }

    //------------------------------------------------------------------------------------------------------------------
    // Send REQUEST message to the bidders found
    // AKA: FIPA-Request-Protocol
    //------------------------------------------------------------------------------------------------------------------
    private class AchieveREInitiatorAuctioneer extends AchieveREInitiator {
        public AchieveREInitiatorAuctioneer(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            super.handleAgree(agree);
            printInTerminal("received AGREE from "+agree.getSender().getName());
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            super.handleRefuse(refuse);
            printInTerminal("received REFUSE from "+refuse.getSender().getName());
            // if the answer is REFUSE, the agent is removed from bidderAgents List
            bidderAgents.remove(refuse.getSender());
            printInTerminal("\nBIDDERS ARE:\n");
            for (int i = 0; i < bidderAgents.size(); ++i)
                System.out.println("1 - "+(bidderAgents.get(i).getName()));
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            super.handleInform(inform);
            printInTerminal("received INFORM from "+inform.getSender().getName());
        }

        @Override
        protected void handleAllResponses(Vector responses) {
            super.handleAllResponses(responses);
            int acceptances =0;
            for(int i=0;i<responses.size();i++){ if(((ACLMessage)responses.get(i)).getPerformative()==ACLMessage.AGREE) acceptances++;}
            if(acceptances==bidderAgents.size()){
                // Fill the CFP message
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                for (AID bidderAgent : bidderAgents) {
                    msg.addReceiver(bidderAgent);
                }
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                msg.setContent("Round-"+ auctionState.getCurrentRoundNumber());
                // Launch first ROUND
                System.out.println("-----------***********THE AUCTION WILL BEGIN***********----------------");
                myAgent.addBehaviour(new AuctionRound(myAgent, msg));
            }
            else{
                printInTerminal("ERROR Gathering all responses in request protocol");
            }
        }

        @Override
        protected void handleAllResultNotifications(Vector resultNotifications) {
            super.handleAllResultNotifications(resultNotifications);
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // Play the Auction bidding rounds
    // AKA: FIPA-Contract-Net
    //------------------------------------------------------------------------------------------------------------------
    private class AuctionRound extends ContractNetInitiator{
        public AuctionRound(Agent a, ACLMessage cfp) {
            super(a, cfp);

        }

        /**
         * This method is called every time a propose message is received,
         * which is not out-of-sequence according to the protocol rules.
         * This default implementation does nothing; programmers might wish
         * to override the method in case they need to react to this event.
         */
        @Override
        protected void handlePropose(ACLMessage propose, Vector v) {
            printInTerminal(" (RECIEVED PROPOSE) From "+propose.getSender().getName()+" to round "+ auctionState.getCurrentRoundNumber()+" with "+propose.getContent()+"$.");
        }

        /**
         * This method is called every time a refuse message is received,
         * which is not out-of-sequence according to the protocol rules.
         * This default implementation does nothing; programmers might wish
         * to override the method in case they need to react to this event.
         */
        @Override
        protected void handleRefuse(ACLMessage refuse) {
            printInTerminal(" (RECEIVED REFUSE) From "+refuse.getSender().getName()+" to round "+ auctionState.getCurrentRoundNumber());
            // if the answer is REFUSE, the agent is removed from bidderAgents List
            bidderAgents.remove(refuse.getSender());
        }

        /**
         * This method is called every time a failure message is received,
         * which is not out-of-sequence according to the protocol rules.
         * This default implementation does nothing; programmers might wish
         * to override the method in case they need to react to this event.
         */
        @Override
        protected void handleFailure(ACLMessage failure) {
//            if (failure.getSender().equals(myAgent.getAMS())) {
//                // FAILURE notification from the JADE runtime: the receiver
//                // does not exist
//                printInTerminal("Responder does not exist");
//            }
//            else {
//                printInTerminal("Agent "+failure.getSender().getName()+" failed");
//            }
        }

        /**
         * This method is called when all the responses have been collected
         * or when the timeout is expired. The used timeout is the minimum value
         * of the slot replyBy of all the sent messages. By response message we
         * intend here all the propose, not-understood, refuse received messages,
         * which are not not out-of-sequence according to the protocol rules.
         * This default implementation does nothing; programmers might wish to
         * override the method in case they need to react to this event by analysing
         * all the messages in just one call.
         */
        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            if(checkWinner(responses))
                return;

            // Evaluate bids proposals vars
            int bestBidProposal = -1;
            AID bestBidderProposer = null;
            ACLMessage accept = null;
            // Determines the best bid and creates the replies to the proposals with default value REJECT_PROPOSAL,
            // which will be altered to ACCEPT only for the Highest Bidder
            for( int i = 0; i< responses.size(); i++){
                ACLMessage response = (ACLMessage) responses.get(i);
                if (response.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = response.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    int bidValue = Integer.parseInt(response.getContent());
                    // In case of draw, the first bid with that value is considered
                    if (bidValue > bestBidProposal) {
                        bestBidProposal = bidValue;
                        bestBidderProposer = response.getSender();
                        accept = reply;
                    }
                }
            }

            // Highest bidder's message is ACCEPT_PROPOSAL
            if (accept != null) {
                printInTerminal("(SENDING ACCEPT_PROPOSAL) To "+bestBidderProposer.getName() +" from bid "+ bestBidProposal+"$.");
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                auctionState.updateRoundHistory(new RoundState( auctionState.getCurrentRoundNumber(),bestBidderProposer.getLocalName(), bestBidProposal));
            }
            // Content of the message is the same for everyone: <HIGHEST_BIDDER_NAME>-<VALUE>
            for(int i = 0; i< acceptances.size(); i++) {
                ((ACLMessage) acceptances.get(i)).setContent(bestBidderProposer.getLocalName()+"-"+bestBidProposal);
            }

        }

        @Override
        protected void handleInform(ACLMessage inform) {
            printInTerminal("Agent "+inform.getSender().getName()+" successfully performed the requested action");
        }

        @Override
        protected void handleAllResultNotifications(Vector resultNotifications) {
            ACLMessage newIterationCFP = new ACLMessage(ACLMessage.CFP);
            for (int i = 0; i < bidderAgents.size(); ++i)
                newIterationCFP.addReceiver(bidderAgents.get(i));
            newIterationCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            newIterationCFP.setContent("Round-"+ auctionState.getCurrentRoundNumber());

//            //CREATES NEW ITERATION
//            roundCounter++;

            System.out.println("NEW ROUND -> round #"+  auctionState.getCurrentRoundNumber()+" #####################################################################");
            System.out.println("BIDDERS ARE:");
            for (int i = 0; i < bidderAgents.size(); ++i)
                System.out.println(i+" - "+(bidderAgents.get(i).getName()));

            myAgent.addBehaviour(new AuctionRound(myAgent, newIterationCFP));
        }

        public boolean checkWinner( Vector responses){
            try{
                // Handles the winner of the auction
                if(responses.size()==1){
                    ACLMessage response = (ACLMessage) responses.get(0);
                    //check if winner is last round's winner
                    System.out.println("REPSONSE "+response.getSender().getLocalName() +"/ last accepted "+auctionState.getLastAcceptedProposal_Bidder());
                    if(response.getSender().getLocalName().equals(auctionState.getLastAcceptedProposal_Bidder())){
                        printInTerminal("BIDDER "+auctionState.getLastAcceptedProposal_Bidder()+" WON THE AUCTION At Round "+ auctionState.getCurrentRoundNumber()+ ".\nHE PAID "+auctionState.getLastAcceptedProposal_Value()+"$");
                        CsvWriter.getInstance().setHasWon(auctionState.getLastAcceptedProposal_Bidder());
                        CsvWriter.getInstance().setPaidValue(auctionState.getLastAcceptedProposal_Value());
                        CsvWriter.getInstance().writeCSV();
                        CsvWriter.getInstance().close();
                    }
                    else {
                        printInTerminal("BIDDER " + response.getSender().getLocalName() + " WON THE AUCTION At Round " + auctionState.getCurrentRoundNumber() + ".\nHE PAID " + response.getContent() + "$");
                        CsvWriter.getInstance().setHasWon(response.getSender().getLocalName());
                        CsvWriter.getInstance().setPaidValue(Integer.valueOf(response.getContent()));
                        CsvWriter.getInstance().writeCSV();
                        CsvWriter.getInstance().close();
                    }
                    return true;
                }

                else if (bidderAgents.size()==0){
                    printInTerminal("BIDDER "+auctionState.getLastAcceptedProposal_Bidder()+" WON THE AUCTION At Round "+ auctionState.getCurrentRoundNumber()+ ".\nHE PAID "+auctionState.getLastAcceptedProposal_Value()+"$");
                    CsvWriter.getInstance().setHasWon(auctionState.getLastAcceptedProposal_Bidder());
                    CsvWriter.getInstance().setPaidValue(auctionState.getLastAcceptedProposal_Value());
                    CsvWriter.getInstance().writeCSV();
                    CsvWriter.getInstance().close();
                    return true;
                }
            }
            catch (IOException e){

            }
            return false;
        }
    }

    private void printInTerminal(String msg ){
        System.out.println("AUCTIONEER "+getLocalName()+ "-> " + msg);
    }


}
