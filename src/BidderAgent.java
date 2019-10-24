//package com.aiad;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.util.Arrays;

public class BidderAgent extends Agent{

    // Put agent initializations here
    protected void setup() {
        // Get the bidder's characteristics
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            //
            printInTerminal("Args: "+ Arrays.toString(args));
        }

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
//        addBehaviour(new CyclicBehaviour() {
//            @Override
//            public void action() {
//                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
//                ACLMessage msg = myAgent.receive(mt);
//                if (msg != null) {
//                    // CFP Message received. Process it
//                    String info = msg.getContent();
//                    printInTerminal(getAID().getName()+" has received new auction message --> "+info);
//                }
//                else {
//                    block();
//                }
//            }
//        });

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


    private class AchieveREResponderBidder extends AchieveREResponder {
        public AchieveREResponderBidder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }
        /**
         * Handles the request
         */
        protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
            printInTerminal("Agent "+getLocalName()+": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
            System.out.println("CONTENT OF REQUEST IS: "+request.getContent());
            System.out.println("Performative IS: "+request.getPerformative());

//            if (checkAction()) {
                // We agree to perform the action. Note that in the FIPA-Request
                // protocol the AGREE message is optional. Return null if you
                // don't want to send it.
                printInTerminal("Agent "+getLocalName()+": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(myAgent.getLocalName().equals("a")?ACLMessage.REFUSE:ACLMessage.AGREE);
                return agree;
//            }
//            else {
//                // We refuse to perform the action
//                printInTerminal("Agent "+getLocalName()+": Refuse");
//                throw new RefuseException("check-failed");
//            }
        }
        /**
         * This method is called after the execution of the handleRequest() method if
         * no response was sent or the response was an AGREE message.
         * This default implementation returns null which has the effect of sending no result notification.
         * Programmers should override the method in case they need to react to this event.
         */
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
//            if (performAction()) {
//                printInTerminal("Agent "+getLocalName()+": Action successfully performed");
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
//            }
//            else {
//                printInTerminal("Agent "+getLocalName()+": Action failed");
//                throw new FailureException("unexpected-error");
//            }
        }
    }


    private void printInTerminal(String msg ){
        System.out.println("---------------");
        System.out.println("BIDDER "+getName());
        System.out.println("---------------");
        System.out.println(msg+"\n\n");
    }
}
