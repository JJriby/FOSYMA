package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareFinExploBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597689931498787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    public ShareFinExploBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    String receiverName = myAgent.getReceiverName(); 
	    Map<String, Boolean> list_fin_explo = myAgent.getListFinExplo();
		
		
	    // envoi de la liste de validation
		try {
            ACLMessage finExploMsg = new ACLMessage(ACLMessage.INFORM);
            finExploMsg.setProtocol("SHARE-FIN-EXPLO");
            finExploMsg.setSender(myAgent.getAID());
            finExploMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
            finExploMsg.setContentObject((Serializable)list_fin_explo);
            myAgent.sendMessage(finExploMsg);
            System.out.println(myAgent.getLocalName() + " fin explo envoyée à " + receiverName);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		System.out.println(myAgent.getLocalName() + " envoie : " + list_fin_explo + " à " + receiverName);
	    
		if(list_fin_explo.get(myAgent.getLocalName()) == false) {
	    	myAgent.setSent(true); // comme ça on fera juste la réception de map et pas l'envoi
	    	myAgent.setReceived(false); 
	    	this.finished = true;
	    	this.exitValue = 7;
	    	return;
	    }
		
		if(myAgent.getReceived()) {
			myAgent.setReceived(false);
			this.exitValue = myAgent.getMsgRetour();
		} else {
			myAgent.setSent(true);
			this.exitValue = myAgent.getTypeMsg();
		}
		
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
