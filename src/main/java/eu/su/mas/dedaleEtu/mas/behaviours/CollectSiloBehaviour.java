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
		
		// voir pour modifier et faire un pong en cas d'interblocage, à voir

		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    
	    if (myAgent.checkMessagesInterBlocage()) {
	    	myAgent.setMsgRetour(22);
		    this.exitValue = myAgent.getTypeMsg();
		    this.finished = true;
		    return;
		}
	    
	    Map<Observation, Integer> stockage = myAgent.getStockage();

	    
	    // Réception du sac à dos de l'un des agents
        MessageTemplate backPackTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("ProtocolTanker"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
        );
        
        ACLMessage returnBackPack = myAgent.blockingReceive(backPackTemp, 3000);
        
        if (returnBackPack != null) {
            try {
            	
            	// réception et ajout des trésors supplémentaires
            	List<Couple<Observation, Integer>> back_pack = (List<Couple<Observation, Integer>>) returnBackPack.getContentObject();
            	    
            	for(Couple<Observation, Integer> bp : back_pack) {
            		int qte_avant = stockage.get(bp.getLeft());
            		stockage.put(bp.getLeft(), qte_avant + bp.getRight());
            	}
            	
            	
            	// envoi de l'accusé de réception
            	ACLMessage reponse = returnBackPack.createReply();
            	reponse.setSender(myAgent.getAID());
            	reponse.setProtocol("ProtocolTanker");
            	reponse.setPerformative(ACLMessage.AGREE);
            	reponse.setContent("Bien reçu et ajouté !");
            	
            	myAgent.sendMessage(reponse);        	
            	
            	
            	System.out.println(myAgent.getLocalName() + " possède désormais comme stockage : " + stockage + " grâce à l'ajout de " + returnBackPack.getSender().getLocalName());
            	
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
                    
        } else {
            System.out.println(myAgent.getLocalName() + " n’a pas reçu de back_pack");
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
