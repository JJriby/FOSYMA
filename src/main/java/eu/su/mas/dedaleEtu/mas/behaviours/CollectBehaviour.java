package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	    
	    myAgent.setMsgRetour(GlobalBehaviour.TO_COLLECT);
	    
	    if (myAgent.checkMessagesInterBlocage()) {
			System.out.println("pong provient de collect silo");
			//myAgent.setMsgRetour(GlobalBehaviour.TO_GO_TO_RDV);
		    this.exitValue = GlobalBehaviour.TO_SHARE_INFOS_INTERBLOCAGE;
		    this.finished = true;
		    return;
		}
	    
	    List<String> agentNames = myAgent.getAgentNames();
	    String receiverName = myAgent.getReceiverName();

		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
		
		/*if (myAgent.checkMessagesInterBlocage()) {
			myAgent.setMsgRetour(21);
		    this.exitValue = myAgent.getTypeMsg();
		    this.finished = true;
		    return;
		}*/
		
		if(pour_debugger == 0) {
			/*if(myAgent.getLocalName() != "Silo") {
				System.out.println("Phase collecte : " + this.myAgent.getLocalName() + " ma localisation : " + ((AbstractDedaleAgent) myAgent).getCurrentPosition() + " mais but : " + myAgent.getListObjectifs().get(myAgent.getLocalName()));
			} else {
				System.out.println("Phase collecte : " + this.myAgent.getLocalName() + " ma localisation : " + ((AbstractDedaleAgent) myAgent).getCurrentPosition() + " mais but : " + myAgent.getPosSilo());
			}*/
			pour_debugger++;
		}
		
		
        Location myPosition = ((AbstractDedaleAgent) myAgent).getCurrentPosition();
        
        
        // on ouvre le trésor adéquat si nécessaire
        boolean ouvert = false;
        Observation obs = null;
        int capacite = 0;
        
        if(myAgent.getListGold().containsKey(myPosition.getLocationId())) {
        	String statut_lock = myAgent.getListGold().get(myPosition.getLocationId()).get(Observation.LOCKSTATUS);
        	capacite += Integer.parseInt(myAgent.getListGold().get(myPosition.getLocationId()).get(Observation.GOLD));
        	obs = Observation.GOLD;
        	if(statut_lock.equals("0")) {
        		ouvert = ((AbstractDedaleAgent) myAgent).openLock(Observation.GOLD);
        		System.out.println("OUVERTURE REUSSIE : " + ouvert);
        	} else {
        		ouvert = true;
        	}
    	}
        
        if(myAgent.getListDiamond().containsKey(myPosition.getLocationId())) {
        	String statut_lock = myAgent.getListDiamond().get(myPosition.getLocationId()).get(Observation.LOCKSTATUS);
        	capacite += Integer.parseInt(myAgent.getListDiamond().get(myPosition.getLocationId()).get(Observation.DIAMOND));
        	obs = Observation.DIAMOND;
        	if(statut_lock.equals("0")) {
        		ouvert = ((AbstractDedaleAgent) myAgent).openLock(Observation.DIAMOND);
        	} else {
        		ouvert = true;
        	}
    	}
        
        
        // si le coffre est bien ouvert, on le récolte
        if (capacite > 0 && ouvert) {
        	boolean coffreTrouve = false;

        	if (obs == Observation.GOLD) {
        		for (Entry<String, Map<Observation, String>> o : myAgent.getListGold().entrySet()) {
        			if (o.getKey().equals(myPosition.getLocationId())) {
        				coffreTrouve = true;
        				break;
        			}
        		}
        	} else if (obs == Observation.DIAMOND) {
        		for (Entry<String, Map<Observation, String>> o : myAgent.getListDiamond().entrySet()) {
        			if (o.getKey().equals(myPosition.getLocationId())) {
        				coffreTrouve = true;
        				break;
        			}
        		}
        	}

        	if (coffreTrouve) {
        		int qte = ((AbstractDedaleAgent) myAgent).pick();

        		if (qte == 0) {
        			System.out.println("il n'y a rien ici !");
        			myAgent.setCoffreDisparu("true");
        		} else {
        			myAgent.addCollectedTreasure(qte);
        			System.out.println("quantité récupérée : " + myAgent.getCollectedTreasureValue());
        		}
          	} else {
        		// Sinon, on explore autour pour chercher le coffre
        		List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();

        		for (Couple<Location, List<Couple<Observation, String>>> obsLoc : lobs) {
        			String locId = obsLoc.getLeft().getLocationId();
        			for (Couple<Observation, String> o : obsLoc.getRight()) {
        				if (o.getLeft().equals(obs)) {
        					myAgent.setGoalNode(locId);
        					((AbstractDedaleAgent) myAgent).moveTo(obsLoc.getLeft());
        					return;
        				}
        			}
        		}

        	}
        }

        
        // on retourne au silo
        List<String> path = this.myMap.getShortestPath(myPosition.getLocationId(), myAgent.getPosSilo());
        if(path != null && !path.isEmpty()) {
        	path.remove(path.size()-1);
        	myAgent.setShortestPath(path);
        	myAgent.setTypeMsg(GlobalBehaviour.TO_SHARE_JUST_COLLECT);
            this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
            this.finished = true;
        }
        
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
