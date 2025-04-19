package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.Behaviour;

import java.io.IOException;
import java.io.Serializable;
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
    private boolean attente = false;
    private int cpt = 0;
    
    public PongBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }
    
    @Override
    public void action() {
    	    	    	    	    	
    	this.finished = false;
    	this.exitValue = -1;
    	
    	ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
    	    	
    	// pour shareMap
    	this.receiverName = myAgent.getReceiverName();
        SerializableSimpleGraph<String, MapAttribute> mapToSend = myAgent.getMapToSend();
        //MapRepresentation myMap = myAgent.getMyMap();
        Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit = myAgent.getNodesToTransmit();
        Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
        this.currentlyExchanging = myAgent.getCurrentlyExchanging();
        Map<String, Map<Observation, String>> list_gold = myAgent.getListGold();
        Map<String, Map<Observation, String>> list_diamond = myAgent.getListDiamond();
    	this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
    	
    	// pour shareExpertise
    	List<String> agentNames = myAgent.getAgentNames();
	    Map<String, Observation> list_treasure_type = myAgent.getListTreasureType();
	    Map<String, Set<Couple<Observation,Integer>>> list_expertise = myAgent.getListExpertise();
	    Map<String, List<Couple<Observation,Integer>>> list_back_free_space = myAgent.getListBackFreeSpace();
	    Map<String, Boolean> list_validation = myAgent.getListValidation();    
	    
	    Map<String, Boolean> list_fin_explo = myAgent.getListFinExplo();
	    
	    System.out.println(myAgent.getLocalName() + " communique avec " + receiverName);
	    
	    if(this.cpt <= 5) {
	    	
	        MessageTemplate mt = MessageTemplate.or(
	        		MessageTemplate.or(
		        		MessageTemplate.or(
		        				MessageTemplate.MatchProtocol("SHARE-FIN-EXPLO"), 
		        				MessageTemplate.MatchProtocol("PING")),
		                MessageTemplate.MatchProtocol("SHARE-NEW-NODES")),
	        		MessageTemplate.or(
		        		MessageTemplate.MatchProtocol("SHARE-EXPERTISE"),
		        		MessageTemplate.MatchProtocol("CHGT-PAROLE"))
	        	);
	
	        // réception des messages en laissant 3 secondes d'attente et si y a rien on part
	        ACLMessage msg = myAgent.receive(mt);
	            
	        // ici on traite qu'une fois la boîte aux lettres, à voir comment faire
	        if (msg != null) {
	        	
	            String proto = msg.getProtocol();
	
	            switch (proto) {
	                case "PING":
	                    // Répondre au PING avec un PONG
	                	
	                    ACLMessage pong = msg.createReply();
	                    pong.setPerformative(ACLMessage.INFORM);
	                    pong.setProtocol("PONG");
	                    pong.setSender(myAgent.getAID());
	                    pong.addReceiver(new AID(this.receiverName, AID.ISLOCALNAME));
	                    pong.setContent("Je suis bien dispo !");
	                    myAgent.sendMessage(pong);
	                    System.out.println(myAgent.getLocalName() + " → PONG envoyé à " + msg.getSender().getLocalName());
	                    break;
	
	                case "SHARE-NEW-NODES":
	                    try {
	                    	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>> received = 
	                            (Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>>) msg.getContentObject();
	                    	
	                    	SerializableSimpleGraph<String, MapAttribute> receivedMap = received.getLeft();
	                        myMap.mergeMap(receivedMap);
	                        System.out.println(myAgent.getLocalName() + " carte reçue et fusionnée de " + msg.getSender().getLocalName());
	                    
	                        if (mapToSend != null && !mapToSend.getAllNodes().isEmpty()) {
	                            ACLMessage returnMap = new ACLMessage(ACLMessage.INFORM);
	                            returnMap.setProtocol("SHARE-NEW-NODES-RETURN");
	                            returnMap.setSender(myAgent.getAID());
	                            returnMap.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
	                            
	                            try {		
	                            	Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>> tresors = new Couple<>(list_gold, list_diamond); 
	                            	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>> a_envoyer = new Couple<>(mapToSend, tresors);
	                            	returnMap.setContentObject(a_envoyer);
	                    		} catch (IOException e) {
	                    			e.printStackTrace();
	                    		}
	                            
	                            myAgent.sendMessage(returnMap);
	                            System.out.println(myAgent.getLocalName() + " carte en retour envoyée à " + msg.getSender().getLocalName());
	                            nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());
	                        }
	                        
	                        // fusion des cartes de trésors 
	                        Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>> tresors = received.getRight();
	                        Map<String, Map<Observation, String>> golds = tresors.getLeft();
	                        Map<String, Map<Observation, String>> diamonds = tresors.getRight();
	                        
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
	                        
	                        
	                        alreadyExchanged.add(receiverName);
	                        System.out.println("PONG : " + myAgent.getLocalName() + " a marqué " + receiverName + " comme déjà échangé");
	
	                        this.cpt = 0;
	                        this.finished = true;
	                        this.exitValue = myAgent.getMsgRetour();
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
	                        	                        
	                    } catch (UnreadableException e) {
	                        e.printStackTrace();
	                    }
	                    break;
	                    
	                case "SHARE-EXPERTISE": 
	                	try {
	                		
	                		// réception + fusion
	                		Couple<Couple<Map<String, Observation>, Map<String, Set<Couple<Observation,Integer>>>>, Couple<Map<String, List<Couple<Observation,Integer>>>, Map<String, Boolean>>> received =
	                    			(Couple<Couple<Map<String, Observation>, Map<String, Set<Couple<Observation,Integer>>>>, Couple<Map<String, List<Couple<Observation,Integer>>>, Map<String, Boolean>>>) msg.getContentObject();
	                		
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
	                	    
	                	    if(list_treasure_type.size() == agentNames.size()+1 && list_expertise.size() == agentNames.size()+1 && list_back_free_space.size() == agentNames.size()+1) {
	                	    	list_validation.put(myAgent.getLocalName(), true);
	                	    }
	                	       
	                	    
	                	    // envoi en retour
	                	    try {
	                        	
	                			Couple<Map<String, Observation>, Map<String, Set<Couple<Observation,Integer>>>> partie1 = new Couple<>(list_treasure_type, list_expertise);
	                			Couple<Map<String, List<Couple<Observation,Integer>>>, Map<String, Boolean>> partie2 = new Couple<>(list_back_free_space, list_validation);
	                			
	                			Couple<Couple<Map<String, Observation>, Map<String, Set<Couple<Observation,Integer>>>>, Couple<Map<String, List<Couple<Observation,Integer>>>, Map<String, Boolean>>> a_envoyer = new Couple<>(partie1, partie2);
	                        	
	                            ACLMessage expMsg = new ACLMessage(ACLMessage.INFORM);
	                            expMsg.setProtocol("SHARE-EXPERTISE-RETURN");
	                            expMsg.setSender(myAgent.getAID());
	                            expMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
	                            expMsg.setContentObject(a_envoyer);
	                            myAgent.sendMessage(expMsg);
	                            System.out.println(myAgent.getLocalName() + " exp envoyés à " + receiverName);
	                        } catch (IOException e) {
	                            e.printStackTrace();
	                        }
	                		
	                		System.out.println("PONG : " + myAgent.getLocalName() + " a marqué " + receiverName + " comme déjà échangé");
	                		System.out.println("liste trésors types : " + myAgent.getListTreasureType());
	                		System.out.println("liste expertise : " + myAgent.getListExpertise());
	                		System.out.println("liste back pack : " + myAgent.getListBackFreeSpace());
	                		System.out.println("liste validation : " + myAgent.getListValidation());
	                        
	                		this.cpt = 0;
	                        this.finished = true;
	                        this.exitValue = myAgent.getMsgRetour();
	                        return;
	                		
	                	} catch (UnreadableException e) {
	                        e.printStackTrace();
	                    }
	                    break;
	                    
	                /*case "CHGT-PAROLE":
	                	System.out.println("changement");
	                	String msg_received = msg.getContent();
	                	myAgent.setParole(msg_received);
	                	this.exitValue = myAgent.getMsgRetour();
	                    this.finished = true;
	                    return;
	                */
	                case "SHARE-FIN-EXPLO":
	                	try {
	                		System.out.println("pong fin explo "+myAgent.getLocalName());
	                		// réception + fusion
	                		Map<String, Boolean> list2_fin_explo =
	                    			(Map<String, Boolean>) msg.getContentObject();
	                	    
	                	    
	                	    // on fusionne la liste de validation
	                	    for(Map.Entry<String, Boolean> elt : list2_fin_explo.entrySet()) {
	                	    	if (elt.getValue() == true){
	                	    		list_fin_explo.put(elt.getKey(), elt.getValue());
	                	    	}
	                	    }  
	                	    
	                	    // envoi en retour
	                	    try {
	                        	
	                            ACLMessage finExploMsg = new ACLMessage(ACLMessage.INFORM);
	                            finExploMsg.setProtocol("SHARE-FIN-EXPLO-RETURN");
	                            finExploMsg.setSender(myAgent.getAID());
	                            finExploMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
	                            finExploMsg.setContentObject((Serializable)list_fin_explo);
	                            myAgent.sendMessage(finExploMsg);
	                            System.out.println(myAgent.getLocalName() + " liste fin explo envoyée en retour à " + receiverName);
	                        } catch (IOException e) {
	                            e.printStackTrace();
	                        }
	                		
	                		System.out.println("PONG : " + myAgent.getLocalName() + " a marqué " + receiverName + " comme déjà échangé");
	                		System.out.println("liste fin explo : " + myAgent.getListFinExplo());
	                		
	                		// si je n'ai pas fini d'explorer la carte alors que mon voisin a fini, j'attends que mon voisin m'envoie sa carte
	                		if(list_fin_explo.get(myAgent.getLocalName()) == false) {
	                			this.attente = true;
	                		} else {
	                			this.attente = false;
	                		}
	                        
	                		if(!attente) {
		                		this.cpt = 0;
		                        this.finished = true;
		                        this.exitValue = myAgent.getMsgRetour();
		                        return;
	                		}
	                		
	                	} catch (UnreadableException e) {
	                        e.printStackTrace();
	                    }
	                    break;
	                    
	            } 
	        }
	        this.cpt++;
	        myAgent.doWait(500);
	    } else {
	    	this.cpt = 0;
	    	System.out.println(myAgent.getLocalName() + " n'a reçu aucun message, on passe. cpt : " + this.cpt);
            this.exitValue = myAgent.getMsgRetour();
            this.finished = true;
            return;
	    }
	    
    }
    
    @Override
    public boolean done() {
    	this.currentlyExchanging.remove(this.receiverName);
        return finished;
    }
    
    @Override
    public int onEnd() {
        return this.exitValue;
    }
}
