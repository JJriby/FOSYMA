package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import jade.core.behaviours.Behaviour;

public class FinExploBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 7565689731496787661L;
	private boolean finished = false;
	private int exitValue = -1;
	
	private Set<String> already_com = new HashSet<>();
	private Set<String> last_com = new HashSet<>();
	
    private int pour_debugger = 0;
		
	public FinExploBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
	}

	@Override
	public void action() {
		
		if(pour_debugger == 0) {
			System.out.println(myAgent.getLocalName() + " est dans fin d'explo");
			pour_debugger++;
		}
				
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    Map<String, Boolean> list_fin_explo = myAgent.getListFinExplo();
	    		
		// si tous les agents connaissent les caractéristiques de tout le monde, 
		// alors on arrête la communication d'expertise et on part préparer le plan d'attaque
		boolean all_validation = true;
		for(Map.Entry<String, Boolean> elt : list_fin_explo.entrySet()) {
	    	if (elt.getValue() == false){
	    		all_validation = false;
	    		break;
	    	}
	    }
		

	    if(!all_validation) {
		    List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();	
	
		    for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
	            List<Couple<Observation, String>> details = obs.getRight();
			
	            for (Couple<Observation, String> detail : details) {
	            	if (detail.getLeft() == Observation.AGENTNAME) {
	            		
	                    String agentName = detail.getRight();	                    
	                    
	                    //if(!alreadyExchanged.contains(agentName) && !myAgent.getCurrentlyExchanging().contains(agentName)) {
	                    if(!this.already_com.contains(agentName)) {
	                    	this.already_com.add(agentName);
	                    	//this.voisins.add(agentName);
	                    	myAgent.setReceiverName(agentName);
	                    	//myAgent.getCurrentlyExchanging().add(agentName);
	                    	myAgent.setMsgRetour(4);
	                    	
	                    	if (myAgent.getLocalName().compareTo(agentName) < 0) {
	                        	System.out.println(myAgent.getLocalName() + " doit aller dans ping");
	                        	myAgent.setTypeMsg(5);
	                        	this.exitValue = 3;     
	                        } else {
	                        	System.out.println(myAgent.getLocalName() + " doit aller dans pong");
	                            this.exitValue = 4;
	                        }                    	
			                this.finished = true;
			                return;
	                    }
	            	}    
	        	}
	        }
		    this.already_com.clear();
	    } else {
	    	List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();	
	    	
		    for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
	            List<Couple<Observation, String>> details = obs.getRight();
			
	            for (Couple<Observation, String> detail : details) {
	            	if (detail.getLeft() == Observation.AGENTNAME) {
	            		
	                    String agentName = detail.getRight();	                    
	                    
	                    //if(!alreadyExchanged.contains(agentName) && !myAgent.getCurrentlyExchanging().contains(agentName)) {
	                    if(!this.last_com.contains(agentName)) {
	                    	this.last_com.add(agentName);
	                    	//this.voisins.add(agentName);
	                    	myAgent.setReceiverName(agentName);
	                    	//myAgent.getCurrentlyExchanging().add(agentName);
	                    	myAgent.setMsgRetour(4);
	                    	
	                    	if (myAgent.getLocalName().compareTo(agentName) < 0) {
	                        	System.out.println(myAgent.getLocalName() + " doit aller dans ping");
	                        	myAgent.setTypeMsg(5);
	                        	this.exitValue = 3;     
	                        } else {
	                        	System.out.println(myAgent.getLocalName() + " doit aller dans pong");
	                            this.exitValue = 4;
	                        }                    	
			                this.finished = true;
			                return;
	                    }
	            	}    
	        	}
	        }
		    this.already_com.clear();
		    this.last_com.clear();
	    	
	    	this.finished = true;
	    	this.exitValue = 2;
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
