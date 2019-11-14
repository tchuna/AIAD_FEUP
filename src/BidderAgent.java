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

import java.util.Arrays;
import java.util.Random;

public class BidderAgent extends Agent{
    // Ammount of money available to spend in this auction
    private int budget;
    // How aggressive the bidder is
    private int agressivenessLevel;

    private int currentItemPrice;

    // Put agent initializations here
    protected void setup() {
        // Get the bidder's characteristics
        Object[] args = getArguments();
        budget=100;

        if (args != null && args.length > 0) {
            //
            printInTerminal("Args: "+ Arrays.toString(args));
//            budget=100;
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
            printInTerminal(": REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
            printInTerminal("CONTENT OF REQUEST IS: "+request.getContent());
            updatePriceFromMsg(request.getContent());

            if (isParticipatingInAuction()) {
                // We agree to perform the action. Note that in the FIPA-Request
                // protocol the AGREE message is optional. Return null if you
                // don't want to send it.
                printInTerminal(": Agree");
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            }

            else {
                ACLMessage refuse = request.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }

//            else {
//                // We refuse to perform the action
//                printInTerminal(": Refuse");
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
//                printInTerminal(": Action successfully performed");
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                MessageTemplate template = MessageTemplate.and(
  		            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                      MessageTemplate.MatchPerformative(ACLMessage.CFP) );
                myAgent.addBehaviour(new AuctionRoundBidder(myAgent, template));
                return inform;
//            }
//            else {
//                printInTerminal(": Action failed");
//                throw new FailureException("unexpected-error");
//            }
        }
        private boolean isParticipatingInAuction(){
            if(currentItemPrice>=budget)
                return false;
            return true;
        }
    }

    private class AuctionRoundBidder extends ContractNetResponder{
        public AuctionRoundBidder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            printInTerminal(": CFP received from "+cfp.getSender().getName()+". Action is "+cfp.getContent());
            int proposal = prepareBid();
            if (proposal!=0) {
                // We provide a proposal
                printInTerminal(": Proposing "+proposal);
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf(proposal));
                return propose;
            }
            else {
                // We refuse to provide a proposal
                printInTerminal(": Refuse");
                //throw new RefuseException("evaluation-failed");
                ACLMessage refuse = cfp.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
               // refuse.setContent(String.valueOf(proposal));
                return refuse;
            }
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
            printInTerminal(": Proposal accepted");

            if(accept!=null)
                updatePriceFromMsg(accept.getContent());

            if (true) {
                printInTerminal(": Action successfully performed");
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
            printInTerminal(": Proposal rejected");
            if(reject.getContent()!=null){
//
//                printInTerminal(": cfp  ---> "+ cfp.getContent());
//                printInTerminal(": propose  ---> "+ propose.getContent());
//                printInTerminal(": reject  ---> "+ reject.getContent());

                updatePriceFromMsg(reject.getContent());
                printInTerminal(":");
                printInTerminal(": Current value is  ---> "+ currentItemPrice);
            }
            super.handleRejectProposal(cfp, propose, reject);
        }

        private int prepareBid() {
            if(currentItemPrice>=budget)
                return 0;

            //TODO: Evaluate agressiveness here

            Random r = new Random();
            int bid = r.nextInt(budget-currentItemPrice)+currentItemPrice+1; //randomizing between currprice and budget -- this is stupid
            return bid;
        }
    }

    private void updatePriceFromMsg(String msg){
        String [] splits = msg.split("-");
        this.currentItemPrice=Integer.parseInt(splits[1]);
    }


    private void printInTerminal(String msg ){
        System.out.println("BIDDER "+getLocalName()+" -> " +msg);
    }


}
