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
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
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
    private boolean finished = false;
    private int exitValue;
    
    Set<String> currentlyExchanging;
    String receiverName;
    
    private MapRepresentation myMap;
    
    public PongBehaviour(final ExploreCoopAgent2 myagent, MapRepresentation myMap2) {
        super(myagent);
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
        Map<String, List<Integer>> list_gold = myAgent.getListGold();
        Map<String, List<Integer>> list_diamond = myAgent.getListDiamond();
        
    	this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
    	    	

        MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchProtocol("PING"),
                MessageTemplate.MatchProtocol("SHARE-NEW-NODES")
        	);

        // réception des messages en laissant 3 secondes d'attente et si y a rien on part
        ACLMessage msg = myAgent.blockingReceive(mt, 3000);
        
        if (msg == null) {
            System.out.println(myAgent.getLocalName() + " n'a reçu aucun message, on passe.");
            this.exitValue = 0; // ou autre, selon ce que tu veux faire
            this.finished = true;
            return;
        }
            
        // ici on traite qu'une fois la boîte aux lettres, à voir comment faire
        while (msg != null) {
        	
            String proto = msg.getProtocol();

            switch (proto) {
                case "PING":
                    // Répondre au PING avec un PONG
                	
                    ACLMessage pong = msg.createReply();
                    pong.setPerformative(ACLMessage.INFORM);
                    pong.setProtocol("PONG");
                    pong.setSender(myAgent.getAID());
                    pong.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
                    pong.setContent("Je suis bien dispo !");
                    myAgent.sendMessage(pong);
                    System.out.println(myAgent.getLocalName() + " → PONG envoyé à " + msg.getSender().getLocalName());
                    break;

                case "SHARE-NEW-NODES":
                    try {
                    	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>> received = 
                            (Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>>) msg.getContentObject();
                    	
                    	SerializableSimpleGraph<String, MapAttribute> receivedMap = received.getLeft();
                        myMap.mergeMap(receivedMap);
                        System.out.println(myAgent.getLocalName() + " carte reçue et fusionnée de " + msg.getSender().getLocalName());
                    
                        if (mapToSend != null && !mapToSend.getAllNodes().isEmpty()) {
                            ACLMessage returnMap = new ACLMessage(ACLMessage.INFORM);
                            returnMap.setProtocol("SHARE-NEW-NODES-RETURN");
                            returnMap.setSender(myAgent.getAID());
                            returnMap.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
                            
                            try {		
                            	Couple<Map<String, List<Integer>>,Map<String, List<Integer>>> tresors = new Couple<>(list_gold, list_diamond); 
                            	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>> a_envoyer = new Couple<>(mapToSend, tresors);
                            	returnMap.setContentObject(a_envoyer);
                    		} catch (IOException e) {
                    			e.printStackTrace();
                    		}
                            
                            myAgent.sendMessage(returnMap);
                            System.out.println(myAgent.getLocalName() + " 🔁 carte en retour envoyée à " + msg.getSender().getLocalName());
                            nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());
                        }
                        
                        // fusion des cartes de trésors 
                        Couple<Map<String, List<Integer>>,Map<String, List<Integer>>> tresors = received.getRight();
                        Map<String, List<Integer>> golds = tresors.getLeft();
                        Map<String, List<Integer>> diamonds = tresors.getRight();
                        
                        for(Map.Entry<String, List<Integer>> g : golds.entrySet()) {
                        	String g_key = g.getKey();
                        	List<Integer> g_value = g.getValue();
                        	if(!list_gold.containsKey(g_key)) {
                        		list_gold.put(g_key, g_value);
                        	}
                        }
                        
                        for(Map.Entry<String, List<Integer>> d : diamonds.entrySet()) {
                        	String d_key = d.getKey();
                        	List<Integer> d_value = d.getValue();
                        	if(!list_diamond.containsKey(d_key)) {
                        		list_diamond.put(d_key, d_value);
                        	}
                        }
                        
                        
                        alreadyExchanged.add(receiverName);
                        System.out.println("PONG : " + myAgent.getLocalName() + " ✅ a marqué " + receiverName + " comme déjà échangé");

                        
                        finished = true;
                        this.exitValue = 0;
                        return;
                        
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
            } 
            this.exitValue = 0;
            this.finished = true;
            return;
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
