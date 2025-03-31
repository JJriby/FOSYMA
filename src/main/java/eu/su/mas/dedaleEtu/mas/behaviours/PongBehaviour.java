package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.Behaviour;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class PongBehaviour extends Behaviour {

    private static final long serialVersionUID = 1L;
    private String receiverName;
    private SerializableSimpleGraph<String, MapAttribute> mapToSend;
    private MapRepresentation myMap;
    private boolean finished = false;
    private Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit;
    private Set<String> alreadyExchanged;
    private Set<String> currentlyExchanging;
    private int exitValue;
    private Map<String, List<Integer>> list_gold;
    private Map<String, List<Integer>> list_diamond;
    
    public PongBehaviour(AbstractDedaleAgent a, String receiverName, SerializableSimpleGraph<String, MapAttribute> mapToSend, MapRepresentation myMap, Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit, Set<String> alreadyExchanged, Set<String> currentlyExchanging, Map<String, List<Integer>> list_gold, Map<String, List<Integer>> list_diamond) {
        super(a);
        this.receiverName = receiverName;
        this.mapToSend = mapToSend;
        this.myMap = myMap;
        this.nodesToTransmit = nodesToTransmit;
        this.alreadyExchanged = alreadyExchanged;
        this.currentlyExchanging = currentlyExchanging;
        this.list_gold = list_gold;
        this.list_diamond = list_diamond;
    }

    @Override
    public void action() {
    	
        ACLMessage msg = myAgent.receive();

        if (msg != null) {
            String proto = msg.getProtocol();

            switch (proto) {
                case "PING":
                    // Répondre au PING avec un PONG
                	
                    ACLMessage pong = msg.createReply();
                    pong.setPerformative(ACLMessage.INFORM);
                    pong.setProtocol("PONG");
                    pong.setSender(this.myAgent.getAID());
                    pong.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
                    pong.setContent("Je suis bien dispo !");
                    ((AbstractDedaleAgent)this.myAgent).sendMessage(pong);
                    System.out.println(myAgent.getLocalName() + " → PONG envoyé à " + msg.getSender().getLocalName());
                    break;

                case "SHARE-NEW-NODES":
                    try {
                    	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>> received = 
                            (Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>>) msg.getContentObject();
                    	
                    	SerializableSimpleGraph<String, MapAttribute> receivedMap = received.getLeft();
                        this.myMap.mergeMap(receivedMap);
                        System.out.println(myAgent.getLocalName() + " carte reçue et fusionnée de " + msg.getSender().getLocalName());

                                               
                        if (this.mapToSend != null && !this.mapToSend.getAllNodes().isEmpty()) {
                            ACLMessage returnMap = new ACLMessage(ACLMessage.INFORM);
                            returnMap.setProtocol("SHARE-NEW-NODES-RETURN");
                            returnMap.setSender(this.myAgent.getAID());
                            returnMap.addReceiver(new AID(this.receiverName, AID.ISLOCALNAME));
                            
                            try {		
                            	Couple<Map<String, List<Integer>>,Map<String, List<Integer>>> tresors = new Couple<>(this.list_gold, this.list_diamond); 
                            	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>> a_envoyer = new Couple<>(mapToSend, tresors);
                            	returnMap.setContentObject(a_envoyer);
                    		} catch (IOException e) {
                    			e.printStackTrace();
                    		}
                            
                            ((AbstractDedaleAgent)this.myAgent).sendMessage(returnMap);
                            System.out.println(myAgent.getLocalName() + " 🔁 carte en retour envoyée à " + msg.getSender().getLocalName());
                            this.nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());
                        }
                        
                        // fusion des cartes de trésors 
                        Couple<Map<String, List<Integer>>,Map<String, List<Integer>>> tresors = received.getRight();
                        Map<String, List<Integer>> golds = tresors.getLeft();
                        Map<String, List<Integer>> diamonds = tresors.getRight();
                        
                        for(Map.Entry<String, List<Integer>> g : golds.entrySet()) {
                        	String g_key = g.getKey();
                        	List<Integer> g_value = g.getValue();
                        	if(!this.list_gold.containsKey(g_key)) {
                        		this.list_gold.put(g_key, g_value);
                        	}
                        }
                        
                        for(Map.Entry<String, List<Integer>> d : diamonds.entrySet()) {
                        	String d_key = d.getKey();
                        	List<Integer> d_value = d.getValue();
                        	if(!this.list_diamond.containsKey(d_key)) {
                        		this.list_diamond.put(d_key, d_value);
                        	}
                        }
                        
                        
                        
                        
                        this.alreadyExchanged.add(receiverName);
                        System.out.println("PONG : " + myAgent.getLocalName() + " ✅ a marqué " + receiverName + " comme déjà échangé");
                        
                        finished = true;
                        this.exitValue = -1;

                        
                        /*// Envoyer un ACK
                        ACLMessage ack = msg.createReply();
                        ack.setPerformative(ACLMessage.CONFIRM);
                        ack.setProtocol("ACK-SHARE");
                        ack.setSender(this.myAgent.getAID());
                        ack.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
                        ack.setContent("Map reçue !");
                        ((AbstractDedaleAgent)this.myAgent).sendMessage(ack);
                        System.out.println(myAgent.getLocalName() + " ✅ ACK envoyé à " + msg.getSender().getLocalName());
						*/
                        
                        
                        // partage qtte sac à dos + objectif
                        
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        } else {
            finished = true;
            this.exitValue = -1;
        }
    }
    
    @Override
    public boolean done() {
    	this.currentlyExchanging.remove(receiverName);
        return finished;
    }
    
    @Override
    public int onEnd() {
        return this.exitValue;
    }
}
