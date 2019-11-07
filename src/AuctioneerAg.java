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

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AuctioneerAg extends Agent{
    // The GUI by means of which the user can start an auction
    private CreateAuctionGui myGui;
    // The list of known BIDDER agents
    private List<AID> bidderAgents;
    // Name of item being auctioned
    private String itemName;
    // Starting price of the item
    private int startingPrice;

    private int roundCounter=0;

    // Put agent initializations here
    protected void setup() {
        // Create and show the GUI
        myGui = new CreateAuctionGui(this);
        myGui.showGui();

    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Close the GUI
        myGui.dispose();
        // Printout a dismissal message
        printInTerminal("Auctioneer-agent "+getAID().getName()+" terminating.");
    }

    /**
     ** Called by the GUI when it's time to start the auction
     */
    public void startAuction(final String name, final int price) {
        // Behaviour used for setting local variables and printing the start of Auction
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                // Sets the variables values defined in the GUI
                itemName=name;
                startingPrice=price;
                printInTerminal("Started the auction of the item --> "+itemName+" <-- ");
                printInTerminal("\nStarting price is --> "+startingPrice+" $");
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
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    printInTerminal("Found the following bidder agents:");
                    // Identifies all bidder agents
                    bidderAgents = new ArrayList<>();
                    for (int i = 0; i < result.length; ++i) {
                        bidderAgents.add(result[i].getName()) ;
                        System.out.println(bidderAgents.get(i).getName());
                    }
                    // Send the REQUEST to all sellers
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    for (int i = 0; i < bidderAgents.size(); ++i)
                        request.addReceiver(bidderAgents.get(i));
                    request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                    request.setContent(itemName+"-"+startingPrice);
//                    request.setConversationId("Auction");
                    request.setReplyWith("req"+System.currentTimeMillis()); // Unique value

                    addBehaviour(new AchieveREInitiatorAuctioneer(myAgent, request));
//                    // Prepare the template to get proposals
//                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
//                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                }
                catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

    }

    private class AchieveREInitiatorAuctioneer extends AchieveREInitiator {
        public AchieveREInitiatorAuctioneer(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            super.handleAgree(agree);
            printInTerminal("Auctioneer "+myAgent.getName()+" received AGREE from "+agree.getSender().getName());
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            super.handleRefuse(refuse);
            printInTerminal("Auctioneer "+myAgent.getName()+" received REFUSE from "+refuse.getSender().getName());
            // if the answer is REFUSE, the agent is removed from bidderAgents List
            bidderAgents.remove(refuse.getSender());
            System.out.println("\nBIDDERS ARE:\n");
            for (int i = 0; i < bidderAgents.size(); ++i)
                System.out.println("1 - "+(bidderAgents.get(i).getName()));
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            super.handleInform(inform);
            printInTerminal("Auctioneer "+myAgent.getName()+" received INFORM from "+inform.getSender().getName());
        }

        @Override
        protected void handleAllResponses(Vector responses) {
            super.handleAllResponses(responses);
            if(responses.size()==bidderAgents.size()){
                // Fill the CFP message
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                for (AID bidderAgent : bidderAgents) {
                    msg.addReceiver(bidderAgent);
                }
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                msg.setContent("Round-"+roundCounter);
                // Launch first ROUND
                myAgent.addBehaviour(new AuctionRound(myAgent, msg));
            }
            else{
                System.out.println("ERROR Gathering all responses");
            }
        }

        @Override
        protected void handleAllResultNotifications(Vector resultNotifications) {
            super.handleAllResultNotifications(resultNotifications);
        }
    }

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
            System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
        }

        /**
         * This method is called every time a refuse message is received,
         * which is not out-of-sequence according to the protocol rules.
         * This default implementation does nothing; programmers might wish
         * to override the method in case they need to react to this event.
         */
        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent "+refuse.getSender().getName()+" refused");
            printInTerminal("Auctioneer "+myAgent.getName()+" received REFUSE from "+refuse.getSender().getName()+" in Round no "+roundCounter);
            // if the answer is REFUSE, the agent is removed from bidderAgents List
            bidderAgents.remove(refuse.getSender());
            System.out.println("\nBIDDERS ARE:\n");
            for (int i = 0; i < bidderAgents.size(); ++i)
                System.out.println("1 - "+(bidderAgents.get(i).getName()));
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
//                System.out.println("Responder does not exist");
//            }
//            else {
//                System.out.println("Agent "+failure.getSender().getName()+" failed");
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
            System.out.println("DEBUG1");
            // Handles the winner of the auction
            if(responses.size()==1){
                ACLMessage response = (ACLMessage) responses.get(0);
                System.out.println("BIDDER "+response.getSender().getLocalName()+" WON THE AUCTION At Round "+roundCounter+ ".\nHE PAID "+response.getContent()+"$");
                return;
            }

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
            System.out.println("DEBUG2");

            // Highest bidder's message is ACCEPT_PROPOSAL
            if (accept != null) {
                System.out.println("Accepting proposal "+bestBidProposal+"$ from responder "+bestBidderProposer.getName());
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
            System.out.println("DEBUG3");
            // Content of the message is the same for everyone: <HIGHEST_BIDDER_NAME>-<VALUE>
            for(int i = 0; i< acceptances.size(); i++) {
                ((ACLMessage) responses.get(i)).setContent(bestBidderProposer.getLocalName()+"-"+bestBidProposal);
            }
            System.out.println("DEBUG4");
            //CREATES NEW ITERATION
            roundCounter++;
            ACLMessage newIterationCFP = new ACLMessage(ACLMessage.CFP);
            newIterationCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            newIterationCFP.setContent("Round-"+roundCounter);
            System.out.println("DEBUG5");
            Vector v =new Vector();
            v.add(newIterationCFP);
            //newIteration(v);
           // myAgent.addBehaviour(new AuctionRound(myAgent, newIterationCFP));

            myAgent.addBehaviour(new AuctionRound(myAgent, newIterationCFP));
            System.out.println("DEBUG6");
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
        }
    }

    private void printInTerminal(String msg ){
        System.out.println("---------------");
        System.out.println("AUCTIONEER "+getName());
        System.out.println("---------------");
        System.out.println(msg+"\n\n");
    }
}
