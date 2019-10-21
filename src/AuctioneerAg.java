//package com.aiad;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AuctioneerAg extends Agent{
    // The GUI by means of which the user can start an auction
    private CreateAuctionGui myGui;
    // The list of known BIDDER agents
    private AID[] bidderAgents;
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
        System.out.println("Auctioneer-agent "+getAID().getName()+" terminating.");
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
                System.out.println("Started the auction of the item --> "+itemName+" <-- ");
                System.out.println("\nStarting price is --> "+startingPrice+" $");
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
                    System.out.println("Found the following bidder agents:");
                    // Identifies all bidder agents
                    bidderAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        bidderAgents[i] = result[i].getName();
                        System.out.println(bidderAgents[i].getName());
                    }
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < bidderAgents.length; ++i)
                        cfp.addReceiver(bidderAgents[i]);
                    cfp.setContent(itemName+"-"+startingPrice);
                    cfp.setConversationId("Auction");
                    cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);

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

}
