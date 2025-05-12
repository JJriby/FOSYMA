package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
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
	    List<String> agentNames = myAgent.getAgentNames();
	    String receiverName = myAgent.getReceiverName();
	    Map<String, Observation> list_treasure_type = myAgent.getListTreasureType();
	    Map<String, Set<Couple<Observation,Integer>>> list_expertise = myAgent.getListExpertise();
	    Map<String, List<Couple<Observation,Integer>>> list_back_free_space = myAgent.getListBackFreeSpace();
	    Map<String, Boolean> list_validation = myAgent.getListValidation();
	    Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
	    
	    //String parole = myAgent.getParole();
		
		
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
            //System.out.println(myAgent.getLocalName() + " exp envoyés à " + receiverName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		if(myAgent.getReceived()) {
			myAgent.setReceived(false);
			this.exitValue = myAgent.getMsgRetour();
		} else {
			myAgent.setSent(true);
            //myAgent.setTypeMsg(GlobalBehaviour.TO_RECEIVE_EXPERTISE);
			this.exitValue = GlobalBehaviour.TO_RECEIVE_EXPERTISE;
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
