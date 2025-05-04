package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Node;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.Behaviour;

public class CollectBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597689731496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    private int pour_debugger = 0; 

    
    public CollectBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		this.finished = false;
		this.exitValue = -1;
		
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    String receiverName = myAgent.getReceiverName();

		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
		
		if (myAgent.checkMessagesInterBlocage()) {
			myAgent.setMsgRetour(21);
		    this.exitValue = myAgent.getTypeMsg();
		    this.finished = true;
		    return;
		}
		
		if(pour_debugger == 0) {
			if(myAgent.getLocalName() != "Silo") {
				System.out.println("Phase collecte : " + this.myAgent.getLocalName() + " ma localisation : " + ((AbstractDedaleAgent) myAgent).getCurrentPosition() + " mais but : " + myAgent.getListObjectifs().get(myAgent.getLocalName()));
			} else {
				System.out.println("Phase collecte : " + this.myAgent.getLocalName() + " ma localisation : " + ((AbstractDedaleAgent) myAgent).getCurrentPosition() + " mais but : " + myAgent.getPosSilo());
			}
			pour_debugger++;
		}
		
		
        Location myPosition = ((AbstractDedaleAgent) myAgent).getCurrentPosition();
        
        
        // on ouvre le trésor adéquat si nécessaire
        boolean ouvert = false;
        
        if(myAgent.getListGold().containsKey(myPosition.getLocationId())) {
        	String statut_lock = myAgent.getListGold().get(myPosition.getLocationId()).get(Observation.LOCKSTATUS);
        	if(statut_lock.equals("0")) {
        		ouvert = ((AbstractDedaleAgent) myAgent).openLock(Observation.GOLD);
        	} else {
        		ouvert = true;
        	}
    	}
        
        if(myAgent.getListDiamond().containsKey(myPosition.getLocationId())) {
        	String statut_lock = myAgent.getListDiamond().get(myPosition.getLocationId()).get(Observation.LOCKSTATUS);
        	if(statut_lock.equals("0")) {
        		ouvert = ((AbstractDedaleAgent) myAgent).openLock(Observation.DIAMOND);
        	} else {
        		ouvert = true;
        	}
    	}
        
        
        // si le coffre est bien ouvert, on le récolte
        if(ouvert) {
        	int qte = ((AbstractDedaleAgent) myAgent).pick();
        	
        	// si y a rien, faut voir comment faire pour avertir le silo que le coffre a disparu
        	if(qte == 0) {
        		System.out.println("il n'y a rien ici !");
        	} else {
        		myAgent.addCollectedTreasure(qte);
        		System.out.println("quantité récupérée : " + myAgent.getCollectedTreasureValue());
        	}
        	
        }
        
        
        // on regarde si on a récupéré tout le coffre
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();

        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
	        Location pos = obs.getLeft();
	        List<Couple<Observation, String>> details = obs.getRight();
	    	
	        // si on observe le noeud où on se trouve
			if(pos.getLocationId().equals(myPosition.getLocationId())) {
				
		        for (Couple<Observation, String> detail : details) {
		        	
		            if (detail.getLeft() == Observation.GOLD || detail.getLeft() == Observation.DIAMOND) {
		                String qte_restante = detail.getRight();
		                
		                if(qte_restante.equals("0")) {
		                	System.out.println("On a récolté tout le trésor !");
		                }
		                
		            }
		        }  
			}
        }

  
        // on retourne au silo
        myAgent.setShortestPath(this.myMap.getShortestPath(myPosition.getLocationId(), myAgent.getPosSilo()));
        this.exitValue = 15;
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
