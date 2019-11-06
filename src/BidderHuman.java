import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class BidderHuman extends Agent{
  private CreateHumanBidderGUI bidderGui;

  private int  currentBudget;

  protected void setup(){
    currentBudget=100;

    bidderGui = new CreateHumanBidderGUI(this);
		bidderGui.showGui();
 

  }


  protected int getcurrentBudget(){

    return this.currentBudget;
  }

}
