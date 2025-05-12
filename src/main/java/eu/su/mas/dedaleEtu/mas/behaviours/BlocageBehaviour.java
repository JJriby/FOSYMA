package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class BlocageBehaviour extends Behaviour {
	
    private static final long serialVersionUID = 1L;
    private boolean finished = false;
    private int exitValue = -1;

    private MapRepresentation myMap;
    
    private boolean vu = false;

    public BlocageBehaviour(final ExploreCoopAgent2 myAgent) {
        super(myAgent);
    }

	@Override
	public void action() {
		this.finished = false;
        this.exitValue = -1;
        ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
        this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
        
        if(myAgent.getPreviousMode().equals("CartePleine")) {
        	System.out.println(myAgent.getLocalName() + " est bloqué et a pour obj " + myAgent.getGoalNode());
        	List<String> chemin = myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), myAgent.getGoalNode());
        	myAgent.setShortestPath(chemin);
        	System.out.println("nouveau chemin : " + myAgent.getShortestPath());
        	myAgent.setMode("CartePleine");
        	myAgent.setPreviousMode("");
        	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
	    	this.finished = true;
	    	return;
        }
        
        if(myAgent.getPreviousMode().equals("collecte")) {
        	System.out.println(myAgent.getLocalName() + " est bloqué et a pour obj " + myAgent.getGoalNode());
        	List<String> chemin = myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), myAgent.getGoalNode());
        	myAgent.setShortestPath(chemin);
        	System.out.println("nouveau chemin : " + myAgent.getShortestPath());
        	myAgent.setMode("collecte");
        	myAgent.setPreviousMode("");
        	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
	    	this.finished = true;
	    	return;
        }
        
        // une fois qu'on a croisé l'agent qu'on devait laisser passer, et qu'on ne le voit désormais plus, on récupère notre chemin vers la destination initiale
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();	
    	
        boolean vu_une_fois = false;
	    for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            List<Couple<Observation, String>> details = obs.getRight();
       
            for (Couple<Observation, String> detail : details) {
            	if (detail.getLeft() == Observation.AGENTNAME) {
            		
                    String agentName = detail.getRight();	                    
                    
                    if(agentName.equals(myAgent.getBlockingAgent())) {
                    	this.vu = true;
                    	vu_une_fois = true;
                    }
            	}    
        	}
	    }
	    
	    // donc si à destination on l'a déjà vu mais que mtn on ne le voit plus nul part alors la voie est libre
	    if(this.vu && !vu_une_fois) {
	    	List<String> new_path = new ArrayList<>();
	    	if(myAgent.getLocalName().equals(myAgent.getAgentSilo())) {
	    		new_path = myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), myAgent.getPosSilo());
	    	} else {
	    		new_path = myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), myAgent.getListObjectifs().get(myAgent.getLocalName()));
	    	}
	    	
	    	if(!new_path.isEmpty()) {
		    	myAgent.setShortestPath(new_path);
		    	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
		    	this.finished = true;
	    	}
	    }
        
	}

    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public int onEnd() {
        return exitValue;
    }

}
