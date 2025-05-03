package eu.su.mas.dedaleEtu.mas.behaviours.communication;

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
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
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
    private boolean finished = false;
    private int exitValue;
    
    private MapRepresentation myMap;
    
    Set<String> currentlyExchanging;
    String receiverName;

    public PingBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

    @Override
    public void action() {
    	
    	
    	    	
    	this.finished = false;
        this.exitValue = -1;
    	
    	this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
    	this.currentlyExchanging = ((ExploreCoopAgent2) this.myAgent).getCurrentlyExchanging();
    	this.receiverName = ((ExploreCoopAgent2) this.myAgent).getReceiverName();	
    	ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
    	int type_msg = myAgent.getTypeMsg();
    	
    	System.out.println("ping : " + myAgent.getLocalName() + " msg retour : " + myAgent.getMsgRetour() + " msg autre : " + type_msg);
    	
        // 1. Envoi du PING
        ACLMessage ping = new ACLMessage(ACLMessage.INFORM);
        ping.setProtocol("PING");
        ping.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
        ping.setContent(Integer.toString(type_msg));
        ping.setSender(myAgent.getAID());
        myAgent.sendMessage(ping);
        System.out.println(myAgent.getLocalName() + " → PING envoyé à " + receiverName);
        
       
        // 2. Attente du PONG
        MessageTemplate pongTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol("PONG"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage pong = this.myAgent.blockingReceive(pongTemplate, 3000);
        if (pong == null) {
            System.out.println(myAgent.getLocalName() + " Pas de PONG de " + receiverName);
            this.exitValue = myAgent.getMsgRetour();
        } else {
	        System.out.println(myAgent.getLocalName() + " PONG reçu de " + receiverName);
	        this.exitValue = type_msg;
        }
        
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