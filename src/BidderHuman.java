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
  private String [] myItens;

  protected void setup(){
    currentBudget=100;
    String[] aux = { "BMW2020", "Rolex", "Book", "Rabbit"};
    myItens =aux;

    bidderGui = new CreateHumanBidderGUI(this);
		bidderGui.showGui();

    System.out.println("Hello! Bidder "+getAID().getName()+" i  have " + currentBudget +" $  in my Budget");


  }


  protected int getcurrentBudget(){

    return this.currentBudget;
  }


  protected String[] getmyItens(){

    return this.myItens;
  }

}
