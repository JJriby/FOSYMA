package eu.su.mas.dedaleEtu.mas.behaviours.poubelle;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ParoleBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8593789931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    public ParoleBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    String receiverName = myAgent.getReceiverName();  
	    String parole = myAgent.getParole();
	    
	    System.out.println(myAgent.getLocalName() + " donne la parole à " + receiverName);
		

	    // envoi de la prochaine parole
        ACLMessage paroleMsg = new ACLMessage(ACLMessage.INFORM);
        paroleMsg.setProtocol("CHGT-PAROLE");
        paroleMsg.setSender(myAgent.getAID());
        paroleMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
        paroleMsg.setContent(parole);
        myAgent.sendMessage(paroleMsg);
        System.out.println(myAgent.getLocalName() + " parole à : " + receiverName);
		
        // on retourne dans PlanDAttaque
        this.exitValue = 1;
        this.finished = true;        
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
