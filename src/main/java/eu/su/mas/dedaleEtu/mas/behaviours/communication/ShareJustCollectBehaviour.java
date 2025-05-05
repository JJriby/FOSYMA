package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareJustCollectBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8591689931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    private int pour_debugger = 0;
    
    private MapRepresentation myMap;
    
    public ShareJustCollectBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }


	@Override
	public void action() {
		
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    String receiverName = myAgent.getReceiverName();
	    
		if(pour_debugger == 0) {
			if(myAgent.getLocalName() != "Silo") {
				System.out.println("Phase collecte : " + this.myAgent.getLocalName() + " ma localisation : " + ((AbstractDedaleAgent) myAgent).getCurrentPosition() + " mais but : " + myAgent.getListObjectifs().get(myAgent.getLocalName()));
			} else {
				System.out.println("Phase collecte : " + this.myAgent.getLocalName() + " ma localisation : " + ((AbstractDedaleAgent) myAgent).getCurrentPosition() + " mais but : " + myAgent.getPosSilo());
			}
			pour_debugger++;
		}

	    
	    boolean empty = ((AbstractDedaleAgent) myAgent).emptyMyBackPack(myAgent.getAgentSilo());
    	
	    if(empty) {
	    	myAgent.setCollectedTreasureValue();
	    	System.out.println("après empty true : " + myAgent.getCollectedTreasureValue());
	    } else {
	    	System.out.println("après empty false : " + myAgent.getCollectedTreasureValue());
	    }
	    
	}

	@Override
	public boolean done() {
		return this.finished;
	}
	
	@Override
    public int onEnd() {
        return this.exitValue;
    }

}
