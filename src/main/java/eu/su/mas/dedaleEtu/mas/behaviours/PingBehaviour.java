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
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class PingBehaviour extends Behaviour {

	private static final long serialVersionUID = 12L;
	private String receiverName;
    private SerializableSimpleGraph<String, MapAttribute> mapToSend;
    private MapRepresentation myMap;
    private boolean finished = false;
    private Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit;
    private Set<String> alreadyExchanged;
    private Set<String> currentlyExchanging;

    public PingBehaviour(AbstractDedaleAgent a, String receiverName, SerializableSimpleGraph<String, MapAttribute> mapToSend, MapRepresentation myMap, Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit, Set<String> alreadyExchanged, Set<String> currentlyExchanging) {
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
    	
    	System.out.println("ici : " + this.alreadyExchanged);
    	
        // 1. Envoi du PING
        ACLMessage ping = new ACLMessage(ACLMessage.QUERY_IF);
        ping.setProtocol("PING");
        ping.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
        ping.setContent("Tu es dispo ?");
        ping.setSender(this.myAgent.getAID());
        ((AbstractDedaleAgent)myAgent).sendMessage(ping);
        System.out.println(myAgent.getLocalName() + " → PING envoyé à " + receiverName);
        
       
        // 2. Attente du PONG
        MessageTemplate pongTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol("PONG"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage pong = this.myAgent.blockingReceive(pongTemplate, 3000);
        if (pong == null) {
            System.out.println(myAgent.getLocalName() + " ❌ Pas de PONG de " + receiverName);
            this.finished = true;
            return;
        }
        System.out.println(myAgent.getLocalName() + " ✅ PONG reçu de " + receiverName);

        // 3. Envoi de la carte        
        try {
            ACLMessage mapMsg = new ACLMessage(ACLMessage.INFORM);
            mapMsg.setProtocol("SHARE-NEW-NODES");
            mapMsg.setSender(this.myAgent.getAID());
            mapMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
            mapMsg.setContentObject(mapToSend);
            ((AbstractDedaleAgent)myAgent).sendMessage(mapMsg);
            System.out.println(myAgent.getLocalName() + " → carte envoyée à " + receiverName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 4. Attente de l’ACK - carte en retour
        MessageTemplate returnMapTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-NEW-NODES-RETURN"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnMap = this.myAgent.blockingReceive(returnMapTemp, 3000);
        
        if (returnMap != null) {
            try {
                SerializableSimpleGraph<String, MapAttribute> receivedMap =
                    (SerializableSimpleGraph<String, MapAttribute>) returnMap.getContentObject();

                this.myMap.mergeMap(receivedMap);
                System.out.println(this.myAgent.getLocalName() + " 🔁 a reçu et fusionné la carte en retour de " + this.receiverName);
                
                this.nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());

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
            
            this.alreadyExchanged.add(receiverName);
            System.out.println("PING : " + this.myAgent.getLocalName() + " ✅ échange terminé avec " + receiverName);
            
        } else {
            System.out.println(this.myAgent.getLocalName() + " ❌ n’a pas reçu de carte en retour de " + receiverName);
        }
        
        finished = true;
    }

    @Override
    public boolean done() {
    	this.currentlyExchanging.remove(receiverName);
        return finished;
    }
}