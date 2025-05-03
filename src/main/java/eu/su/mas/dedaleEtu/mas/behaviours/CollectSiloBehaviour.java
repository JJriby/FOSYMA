package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CollectSiloBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597682931496287661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
        
    
    public CollectSiloBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {

		/*this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    
	    Map<Observation, Integer> stockage = myAgent.getStockage();

	    
	    // Réception du sac à dos de l'un des agents
        MessageTemplate backPackTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("ProtocolTanker"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
        );
        
        ACLMessage returnBackPack = myAgent.blockingReceive(backPackTemp, 3000);
        
        if (returnBackPack != null) {
            try {
            	
            	List<Couple<Observation, Integer>> back_pack = (List<Couple<Observation, Integer>>) returnBackPack.getContentObject();
            	    
            	for(Couple<Observation, Integer> bp : back_pack) {
            		int qte_avant = stockage.get(bp.getLeft());
            		stockage.put(bp.getLeft(), qte_avant + bp.getRight());
            	}
        	    
            	
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
            
            System.out.println(this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
    		System.out.println(this.myAgent.getLocalName() + " liste fin explo : " + myAgent.getListFinExplo());
    		
    		if(myAgent.getSent()) {
            	myAgent.setSent(false);
            	this.exitValue = myAgent.getMsgRetour();
            } else {
                myAgent.setReceived(true);
            	this.exitValue = myAgent.getTypeMsg();
            }
            
        } else {
            System.out.println(myAgent.getLocalName() + " n’a pas reçu de liste de validation en retour de " + receiverName);
            myAgent.setSent(false);
            myAgent.setReceived(false);
            this.exitValue = myAgent.getMsgRetour();
        }
        
        this.finished = true;*/
	    
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
