package eu.su.mas.dedaleEtu.mas.behaviours.communication;

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

public class ReceiveExpertiseBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8593589931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    public ReceiveExpertiseBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    String receiverName = myAgent.getReceiverName();
	    Map<String, Observation> list_treasure_type = myAgent.getListTreasureType();
	    Map<String, Set<Couple<Observation,Integer>>> list_expertise = myAgent.getListExpertise();
	    Map<String, List<Couple<Observation,Integer>>> list_back_free_space = myAgent.getListBackFreeSpace();
	    Map<String, Boolean> list_validation = myAgent.getListValidation();
	    Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
	    
	    
	    // Réception des listes d'expertise
        MessageTemplate returnListesTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-EXPERTISE"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnListes = myAgent.blockingReceive(returnListesTemp, 3000);
        
        if (returnListes != null) {
            try {
            	
            	Couple<Couple<Map<String, Observation>, Map<String, Set<Couple<Observation,Integer>>>>, Couple<Map<String, List<Couple<Observation,Integer>>>, Map<String, Boolean>>> received =
            			(Couple<Couple<Map<String, Observation>, Map<String, Set<Couple<Observation,Integer>>>>, Couple<Map<String, List<Couple<Observation,Integer>>>, Map<String, Boolean>>>) returnListes.getContentObject();
            	
            	Map<String, Observation> list2_treasure_type = received.getLeft().getLeft();
        	    Map<String, Set<Couple<Observation,Integer>>> list2_expertise = received.getLeft().getRight();
        	    Map<String, List<Couple<Observation,Integer>>> list2_back_free_space = received.getRight().getLeft();
        	    Map<String, Boolean> list2_validation = received.getRight().getRight();
        	    
        	    // on fusionne la liste des types de trésors
        	    for(Map.Entry<String, Observation> elt : list2_treasure_type.entrySet()) {
        	    	list_treasure_type.putIfAbsent(elt.getKey(), elt.getValue());
        	    }
        	    
        	    // on fusionne la liste des expertises
        	    for(Map.Entry<String, Set<Couple<Observation,Integer>>> elt : list2_expertise.entrySet()) {
        	    	list_expertise.putIfAbsent(elt.getKey(), elt.getValue());
        	    }
        	    
        	    // on fusionne la liste de l'espace libre dans les sacs à dos
        	    for(Map.Entry<String, List<Couple<Observation,Integer>>> elt : list2_back_free_space.entrySet()) {
        	    	list_back_free_space.putIfAbsent(elt.getKey(), elt.getValue());
        	    }
        	    
        	    // on fusionne la liste de validation
        	    for(Map.Entry<String, Boolean> elt : list2_validation.entrySet()) {
        	    	if (elt.getValue() == true){
        	    		list_validation.put(elt.getKey(), elt.getValue());
        	    	}
        	    }
        	    
        	    // agentNames.size + 1 car agentNames ne contient pas l'agent courant
        	    if(list_treasure_type.size() == agentNames.size()+1 && list_expertise.size() == agentNames.size()+1 && list_back_free_space.size() == agentNames.size()+1) {
        	    	list_validation.put(myAgent.getLocalName(), true);
        	    }
                   
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
            
            //alreadyExchanged.add(receiverName);
            System.out.println(this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
            System.out.println("liste trésors types : " + myAgent.getListTreasureType());
    		System.out.println("liste expertise : " + myAgent.getListExpertise());
    		System.out.println("liste back pack : " + myAgent.getListBackFreeSpace());
    		System.out.println("liste validation : " + myAgent.getListValidation());
            
    		
    		if(myAgent.getSent()) {
            	myAgent.setSent(false);
            	this.exitValue = myAgent.getMsgRetour();
            } else {
                myAgent.setReceived(true);
            	this.exitValue = myAgent.getTypeMsg();
            }
    		
        } else {
            System.out.println(myAgent.getLocalName() + " n’a pas reçu d'exp de " + receiverName);
            myAgent.setSent(false);
            myAgent.setReceived(false);
            this.exitValue = myAgent.getMsgRetour();
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
