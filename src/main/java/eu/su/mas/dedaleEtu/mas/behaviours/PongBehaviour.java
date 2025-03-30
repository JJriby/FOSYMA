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
    
    public PongBehaviour(AbstractDedaleAgent a, String receiverName, SerializableSimpleGraph<String, MapAttribute> mapToSend, MapRepresentation myMap, Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit, Set<String> alreadyExchanged, Set<String> currentlyExchanging) {
        super(a);
        this.receiverName = receiverName;
        this.mapToSend = mapToSend;
        this.myMap = myMap;
        this.nodesToTransmit = nodesToTransmit;
        this.alreadyExchanged = alreadyExchanged;
        this.currentlyExchanging = currentlyExchanging;
    }

    @Override
    public void action() {
    	
    	System.out.println("là : " + this.alreadyExchanged);
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
                        SerializableSimpleGraph<String, MapAttribute> received = 
                            (SerializableSimpleGraph<String, MapAttribute>) msg.getContentObject();
                        this.myMap.mergeMap(received);
                        System.out.println(myAgent.getLocalName() + " 🧠 carte reçue et fusionnée de " + msg.getSender().getLocalName());

                                               
                        if (this.mapToSend != null && !this.mapToSend.getAllNodes().isEmpty()) {
                            ACLMessage returnMap = new ACLMessage(ACLMessage.INFORM);
                            returnMap.setProtocol("SHARE-NEW-NODES-RETURN");
                            returnMap.setSender(this.myAgent.getAID());
                            returnMap.addReceiver(new AID(this.receiverName, AID.ISLOCALNAME));
                            
                            try {			
                            	returnMap.setContentObject(this.mapToSend);
                    		} catch (IOException e) {
                    			e.printStackTrace();
                    		}
                            
                            ((AbstractDedaleAgent)this.myAgent).sendMessage(returnMap);
                            System.out.println(myAgent.getLocalName() + " 🔁 carte en retour envoyée à " + msg.getSender().getLocalName());
                            this.nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());
                        }
                        
                        this.alreadyExchanged.add(receiverName);
                        System.out.println("PONG : " + myAgent.getLocalName() + " ✅ a marqué " + receiverName + " comme déjà échangé");
                        
                        finished = true;

                        
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
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        } else {
            finished = true;
        }
    }
    
    @Override
    public boolean done() {
    	this.currentlyExchanging.remove(receiverName);
        return finished;
    }
}
