package eu.su.mas.dedaleEtu.mas.behaviours.communication;

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
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
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
    
    
    public PongBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }
    
    @Override
    public void action() {
    	
    	// voir pour potentiellement modifier receiverName par plutôt le nom de l'envoyeur pour être sûr
    	    	    	    	    	
    	this.finished = false;
    	this.exitValue = -1;
    	
    	ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
    	this.receiverName = myAgent.getReceiverName();
	    
	    
	    // 1. Réception du Ping
        MessageTemplate pingTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol("PING"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage ping = this.myAgent.blockingReceive(pingTemplate, 3000);
        
        if (ping == null) {
            //System.out.println(myAgent.getLocalName() + " Pas de PING de " + receiverName + "retour : " + myAgent.getMsgRetour());
            this.exitValue = myAgent.getMsgRetour();
            this.finished = true;
            return;
        }
        //System.out.println(myAgent.getLocalName() + " PING reçu de " + receiverName);
        
        // on se dirige à la réception du partage adéquat
        int type_transmission = Integer.parseInt(ping.getContent());
        
        int type_reception = ((GlobalBehaviour) this.getParent()).getTypeReception(type_transmission);
        myAgent.setTypeMsg(type_reception);

        
    	//System.out.println("pong : " + myAgent.getLocalName() + " msg retour : " + myAgent.getMsgRetour() + " msg autre : " + myAgent.getTypeMsg());
        
        // 2. Envoi du Pong
        ACLMessage pong = ping.createReply();
        pong.setProtocol("PONG");
        pong.setSender(myAgent.getAID());
        pong.addReceiver(new AID(this.receiverName, AID.ISLOCALNAME));
        pong.setContent("Je suis bien dispo !");
        myAgent.sendMessage(pong);
        //System.out.println(myAgent.getLocalName() + " → PONG envoyé à " + receiverName);

        this.exitValue = myAgent.getTypeMsg();
	    this.finished = true;   
    }
    
    @Override
    public boolean done() {
    	/*if(this.currentlyExchanging != null) {
    		this.currentlyExchanging.remove(receiverName);
    	}*/
        return finished;
    }
    
    @Override
    public int onEnd() {
        return this.exitValue;
    }
}
