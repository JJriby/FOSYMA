package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class AttenteBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8547689931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    private List<String> voisins = new ArrayList<>();
    private Set<String> already_paroles_passees = new HashSet<>();
    private Set<String> already_com = new HashSet<>();
    private Set<String> last_com = new HashSet<>();
    
    private int pour_debugger = 0;
    
    public AttenteBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		if(pour_debugger == 0) {
			System.out.println(myAgent.getLocalName() + " est en attente");
			pour_debugger++;
		}
		
		this.finished = false;
	    this.exitValue = -1;
		
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    
		// si le plan d'attaque est fini, on le transmet à tous nos voisins
		if(myAgent.getPosSilo() != "") {
			
			System.out.println("chemin à parcourir par " + myAgent.getLocalName() + " : " + myAgent.getShortestPath());
			
			List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();	
	    	
		    for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
	            List<Couple<Observation, String>> details = obs.getRight();
			
	            for (Couple<Observation, String> detail : details) {
	            	if (detail.getLeft() == Observation.AGENTNAME) {
	            		
	                    String agentName = detail.getRight();	                    
	                    
	                    if(!this.already_com.contains(agentName)) {
	                    	this.already_com.add(agentName); // peut-être plutôt l'ajouter seulement une fois ShareObjectifs effectué
	                    	myAgent.setReceiverName(agentName);
	                    	myAgent.setMsgRetour(13);
	                    	
	                        System.out.println(myAgent.getLocalName() + " doit aller dans ping");
	                       	myAgent.setTypeMsg(11);
	                       	this.exitValue = 3;                               	
			                this.finished = true;
			                return;
	                    }
	            	}    
	        	}
	        }
		    this.already_com.clear();
		    myAgent.setTypeMsg(14);
		    this.finished = true;
		    this.exitValue = 12;
		    return;
		}
		
		// on va dans pong si on ne nous a rien transmis
		myAgent.setMsgRetour(13);
		this.exitValue = 4;
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
