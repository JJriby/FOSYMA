package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
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
    	
    	this.receiverName = myAgent.getReceiverName();
        SerializableSimpleGraph<String, MapAttribute> mapToSend = myAgent.getMapToSend();
        //MapRepresentation myMap = myAgent.getMyMap();
        Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit = myAgent.getNodesToTransmit();
        Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
        this.currentlyExchanging = myAgent.getCurrentlyExchanging();
        Map<String, Map<Observation, String>> list_gold = myAgent.getListGold();
        Map<String, Map<Observation, String>> list_diamond = myAgent.getListDiamond();
        
        this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
    	    	
        
    	// Envoi de la carte        
        try {
        	
        	Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>> tresors = new Couple<>(list_gold, list_diamond); 
        	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>> a_envoyer = new Couple<>(mapToSend, tresors);
        	
            ACLMessage mapMsg = new ACLMessage(ACLMessage.INFORM);
            mapMsg.setProtocol("SHARE-NEW-NODES");
            mapMsg.setSender(myAgent.getAID());
            mapMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
            mapMsg.setContentObject(a_envoyer);
            myAgent.sendMessage(mapMsg);
            System.out.println(myAgent.getLocalName() + " carte et trésors envoyés à " + receiverName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Attente de l’ACK - carte en retour
        MessageTemplate returnMapTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-NEW-NODES-RETURN"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnMap = myAgent.blockingReceive(returnMapTemp, 3000);
        
        if (returnMap != null) {
            try {
            	
            	//System.out.println("trésors or avant : " + list_gold);
            	//System.out.println("trésors diamand avant : " + list_diamond);
            	
            	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>> received = 
                        (Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>>) returnMap.getContentObject();
                	
                SerializableSimpleGraph<String, MapAttribute> receivedMap = received.getLeft();
                myMap.mergeMap(receivedMap);
                System.out.println(myAgent.getLocalName() + " a reçu et fusionné la carte en retour de " + receiverName);
                
                nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());
                
                // fusion des cartes de trésors 
                Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>> tresors = received.getRight();
                Map<String, Map<Observation, String>> golds = tresors.getLeft();
                Map<String, Map<Observation, String>> diamonds = tresors.getRight();
                
                
                // voir pour potentiellement faire le putIfAbsent
                for(Map.Entry<String, Map<Observation, String>> g : golds.entrySet()) {
                	String g_key = g.getKey();
                	Map<Observation, String> g_value = g.getValue();
                	if(!list_gold.containsKey(g_key)) {
                		list_gold.put(g_key, g_value);
                	}
                }
                
                for(Map.Entry<String, Map<Observation, String>> d : diamonds.entrySet()) {
                	String d_key = d.getKey();
                	Map<Observation, String> d_value = d.getValue();
                	if(!list_diamond.containsKey(d_key)) {
                		list_diamond.put(d_key, d_value);
                	}
                }
                
                //System.out.println("trésors or après : " + list_gold);
            	//System.out.println("trésors diamand après : " + list_diamond);

                /*// Envoi de l’ACK final
                ACLMessage ackMsg = new ACLMessage(ACLMessage.CONFIRM);
                ackMsg.setProtocol("ACK-SHARE");
                ackMsg.setContent("Merci pour ta carte !");
                ackMsg.addReceiver(returnMap.getSender());
                this.myAgent.send(ackMsg);
                System.out.println(this.myAgent.getLocalName() + " ✅ a envoyé un ACK final à " + returnMap.getSender().getLocalName());
				*/
                
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            
            alreadyExchanged.add(receiverName);
            System.out.println("PING : " + this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
            
        } else {
            System.out.println(myAgent.getLocalName() + " n’a pas reçu de carte en retour de " + receiverName);
        }
        
        this.exitValue = 0;
        this.finished = true;
    }

    @Override
    public boolean done() {
    	this.currentlyExchanging.remove(receiverName);
        return finished;
    }
    
    @Override
    public int onEnd() {
    	this.currentlyExchanging.remove(this.receiverName);
        return this.exitValue;
    }

}
