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
        }

        @Override
        protected void handleAllResultNotifications(Vector resultNotifications) {
            super.handleAllResultNotifications(resultNotifications);
        }
    }


    private void printInTerminal(String msg ){
        System.out.println("---------------");
        System.out.println("AUCTIONEER "+getName());
        System.out.println("---------------");
        System.out.println(msg+"\n\n");
    }
}
