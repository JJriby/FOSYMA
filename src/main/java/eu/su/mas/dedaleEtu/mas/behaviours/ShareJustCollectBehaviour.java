package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
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
	    
		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
	    

	    
	    boolean empty = ((AbstractDedaleAgent) myAgent).emptyMyBackPack(myAgent.getAgentSilo());
	    
	    if(pour_debugger == 0) {
		    if(empty) {
		    	myAgent.setCollectedTreasureValue();
		    	System.out.println("après empty true : " + myAgent.getCollectedTreasureValue());
		    } else {
		    	System.out.println("après empty false : " + myAgent.getCollectedTreasureValue());
		    }
		    pour_debugger++;
	    }
	    
	    
	    // en travaux pour la suite de la collecte
	    /*try {
		    // envoi du message si on a vu que le trésor n'y est plus
	        ACLMessage coffreMsg = new ACLMessage(ACLMessage.INFORM);
	        coffreMsg.setProtocol("SHARE-INFOS-COFFRE");
	        coffreMsg.setSender(myAgent.getAID());
	        coffreMsg.addReceiver(new AID(myAgent.getAgentSilo(), AID.ISLOCALNAME));
	        coffreMsg.setContentObject(new Couple<>(myAgent.getListObjectifs().get(myAgent.getLocalName()),myAgent.getCoffreDisparu()));
	        myAgent.sendMessage(coffreMsg);
	        System.out.println(myAgent.getLocalName() + " on informe que le coffre n'y est plus à " + myAgent.getAgentSilo());
	    } catch (IOException e) {
            e.printStackTrace();
        } 
        
        // Réception du prochaine objectif pour l'agent
        MessageTemplate returnDestTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-UPDATE-OBJECTIFS"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
       
        ACLMessage returnDest = myAgent.blockingReceive(returnDestTemp, 3000);
        boolean nouvelle_dest = false;
        if (returnDest != null) {
            try {
            	
            	nouvelle_dest = true;
            	
            	Map<String, String> update_objectif_received =
            			(Map<String, String>) returnDest.getContentObject();
            	
            	// on update la précédente liste des objectifs
            	myAgent.setListObjectifs(update_objectif_received);
            	    
            	String but = myAgent.getListObjectifs().get(myAgent.getLocalName());
            	myAgent.setShortestPath(myMap.getShortestPath(((AbstractDedaleAgent) myAgent).getCurrentPosition().getLocationId(), but));
            	myAgent.setGoalNode(but);
            	
                System.out.println(this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
        		System.out.println(this.myAgent.getLocalName() + " liste objectifs : " + myAgent.getListObjectifs());
        		System.out.println(this.myAgent.getLocalName() + " a pour objectif : " + but + ", donné par " + myAgent.getAgentSilo() + " et devra déposer en : " + myAgent.getPosSilo());
        	    
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
            
            this.exitValue = myAgent.getMsgRetour();
            
        }*/
        
	    // si on a bien vidé notre coffre alors on retourne à la récolte
	    if(empty) {
	        myAgent.setShortestPath(this.myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), myAgent.getListObjectifs().get(myAgent.getLocalName())));
	        myAgent.setTypeMsg(GlobalBehaviour.TO_COLLECT);
	        this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
	        this.finished = true;
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
