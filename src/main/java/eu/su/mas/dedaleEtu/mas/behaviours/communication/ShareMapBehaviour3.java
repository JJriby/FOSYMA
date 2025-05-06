package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareMapBehaviour3 extends Behaviour {

	private static final long serialVersionUID = 12L;
    private boolean finished = false;
    private int exitValue;
	
    Set<String> currentlyExchanging;
    String receiverName;
    
    private MapRepresentation myMap;
    
    public ShareMapBehaviour3(final ExploreCoopAgent2 myagent) {
        super(myagent);
        //this.myMap = myMap;
    }
    


    @Override
    public void action() {
    	
    	ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
    	
        Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit = myAgent.getNodesToTransmit();
        this.receiverName = myAgent.getReceiverName();
        
    	
        SerializableSimpleGraph<String, MapAttribute> mapToSend = myAgent.getMapToSend();
        //MapRepresentation myMap = myAgent.getMyMap();
        Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
        this.currentlyExchanging = myAgent.getCurrentlyExchanging();
        Map<String, Map<Observation, String>> list_gold = myAgent.getListGold();
        Map<String, Map<Observation, String>> list_diamond = myAgent.getListDiamond();
        
        this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
        
        if(myAgent.getMode() != "finExplo") {
	        SerializableSimpleGraph<String, MapAttribute> partialGraph = nodesToTransmit.get(receiverName);
	        myAgent.setMapToSend(partialGraph);
	        
	        System.out.println(myAgent.getLocalName() + " nodes : "+ nodesToTransmit + " pour " + receiverName);	
        } else {
        	SerializableSimpleGraph<String, MapAttribute> freshGraph = myMap.getSerializableGraph();
            myAgent.setMapToSend(freshGraph);
        }
    	// Envoi de la carte        
        try {
        	
        	Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>> tresors = new Couple<>(list_gold, list_diamond); 
        	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>> a_envoyer = new Couple<>(mapToSend, tresors);
        	
        	System.out.println("a envoyer : " + a_envoyer);
        	
            ACLMessage mapMsg = new ACLMessage(ACLMessage.INFORM);
            mapMsg.setProtocol("SHARE-MAP");
            mapMsg.setSender(myAgent.getAID());
            mapMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
            mapMsg.setContentObject(a_envoyer);
            myAgent.sendMessage(mapMsg);
            System.out.println(myAgent.getLocalName() + " carte et trésors envoyés à " + receiverName + " envoi : " + mapToSend);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        myAgent.getHistoriqueComMap().put(receiverName, 10);
        nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());     
        
        // s'il a déjà reçu une map, alors il sort, sinon il va réceptionner la map
        if(myAgent.getReceived()) {
        	myAgent.setReceived(false);
        	this.exitValue = myAgent.getMsgRetour();
        } else {
            myAgent.setSent(true);
            //myAgent.setTypeMsg(GlobalBehaviour.TO_RECEIVE_MAP);
        	this.exitValue = GlobalBehaviour.TO_RECEIVE_MAP;
        }
        
        this.finished = true;
    }

    @Override
    public boolean done() {
    	/*if (currentlyExchanging != null && receiverName != null) {
    	    currentlyExchanging.remove(receiverName);
    	}*/
    	return finished;
    }
    
    @Override
    public int onEnd() {
        return this.exitValue;
    }

}
