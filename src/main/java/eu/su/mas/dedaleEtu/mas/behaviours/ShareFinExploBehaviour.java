package eu.su.mas.dedaleEtu.mas.behaviours;

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
				
		
		// Attente de l’ACK - liste validation en retour
        MessageTemplate returnListeTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-FIN-EXPLO-RETURN"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnListe = myAgent.blockingReceive(returnListeTemp, 3000);
        
        if (returnListe != null) {
            try {
            	
            	Map<String, Boolean> list2_fin_explo =
            			(Map<String, Boolean>) returnListe.getContentObject();
            	    
        	    // on fusionne la liste de validation
        	    for(Map.Entry<String, Boolean> elt : list2_fin_explo.entrySet()) {
        	    	if (elt.getValue() == true){
        	    		list_fin_explo.put(elt.getKey(), elt.getValue());
        	    	}
        	    } 
        	    
        	    // si l'agent avec qui on vient de communiquer n'a pas fini son explo, on va dans le behaviour shareMap pour lui partager sa map
        	    if(list_fin_explo.get(receiverName) == false) {
        	    	this.finished = true;
        	    	this.exitValue = 2;
        	    	return;
        	    }
        	    
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
            
            System.out.println("PING : " + this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
    		System.out.println("liste fin explo : " + myAgent.getListFinExplo());
            
        } else {
            System.out.println(myAgent.getLocalName() + " n’a pas reçu de liste de validation en retour de " + receiverName);
        }
	    
	    this.finished = true;
	    this.exitValue = 1;
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
