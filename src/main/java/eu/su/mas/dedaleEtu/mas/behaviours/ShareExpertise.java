package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareExpertise extends Behaviour {
	
	private static final long serialVersionUID = 8593689931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    public ShareExpertise(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    String receiverName = myAgent.getReceiverName();
	    Map<String, Observation> list_treasure_type = myAgent.getListTreasureType();
	    Map<String, Set<Couple<Observation,Integer>>> list_expertise = myAgent.getListExpertise();
	    Map<String, List<Couple<Observation,Integer>>> list_back_free_space = myAgent.getListBackFreeSpace();
	    Map<String, Boolean> list_validation = myAgent.getListValidation();
		
		
	    // envoi des 4 listes
		try {
        	
			Couple<Map<String, Observation>, Map<String, Set<Couple<Observation,Integer>>>> partie1 = new Couple<>(list_treasure_type, list_expertise);
			Couple<Map<String, List<Couple<Observation,Integer>>>, Map<String, Boolean>> partie2 = new Couple<>(list_back_free_space, list_validation);
			
			Couple<Couple<Map<String, Observation>, Map<String, Set<Couple<Observation,Integer>>>>, Couple<Map<String, List<Couple<Observation,Integer>>>, Map<String, Boolean>>> a_envoyer = new Couple<>(partie1, partie2);
        	
            ACLMessage expMsg = new ACLMessage(ACLMessage.INFORM);
            expMsg.setProtocol("SHARE-EXPERTISE");
            expMsg.setSender(myAgent.getAID());
            expMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
            expMsg.setContentObject(a_envoyer);
            myAgent.sendMessage(expMsg);
            System.out.println(myAgent.getLocalName() + " exp envoyés à " + receiverName);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		
		
		// Attente de l’ACK - listes en retour
        MessageTemplate returnListesTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-EXPERTISE-RETURN"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnListes = myAgent.blockingReceive(returnListesTemp, 3000);
        
        if (returnListes != null) {
            try {
            	
            	//System.out.println("trésors or avant : " + list_gold);
            	//System.out.println("trésors diamand avant : " + list_diamond);
            	
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
        	    	list_validation.putIfAbsent(elt.getKey(), elt.getValue());
        	    }
                   
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
            
            //alreadyExchanged.add(receiverName);
            System.out.println("PING : " + this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
            System.out.println("liste trésors types : " + myAgent.getListTreasureType());
    		System.out.println("liste expertise : " + myAgent.getListExpertise());
    		System.out.println("liste back pack : " + myAgent.getListBackFreeSpace());
    		System.out.println("liste validation : " + myAgent.getListValidation());
            
        } else {
            System.out.println(myAgent.getLocalName() + " n’a pas reçu de carte en retour de " + receiverName);
        }
        
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
