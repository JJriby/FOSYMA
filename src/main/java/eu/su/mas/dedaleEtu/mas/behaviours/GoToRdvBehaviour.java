package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;

public class GoToRdvBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 7567689731496787661L;
	private boolean finished = false;
	private int exitValue = -1;
	private int cpt = 0;
	
	private Set<String> already_com = new HashSet<>();
	private int cpt_block;
		
	public GoToRdvBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
	}
	
	@Override
	public void action() {
				
		this.finished = false;
		this.exitValue = -1;
		
		// traiter un cas d'erreur où shortestPath serait égal à null ?
		
		// vérifier si lors de ce chemin aussi y a pas interblocage ?
		
		
		ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
		List<String> shortestPath = myAgent.getShortestPath(); 
		
        try { myAgent.doWait(1000); } catch (Exception e) { e.printStackTrace(); }
        
        
        
        if(myAgent.getListFinExplo().get(myAgent.getLocalName()) && myAgent.getPosSilo() == "") {
        	
        	if(myAgent.getTypeMsgInit() == -1) {
        		myAgent.setTypeMsgInit(myAgent.getTypeMsg());
        	}
        	
        	System.out.println("on est dans gotordv alors qu'il faut pas : " + myAgent.getMsgRetour() + " fin explo moi : " + myAgent.getListFinExplo().get(myAgent.getLocalName()) + " posSilo : " + myAgent.getPosSilo());
        	
	        // on guette si on croise un agent n'ayant pas fini sur le chemin et on lui envoie une map dans ce cas
	        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();	
	    	
		    for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
	            List<Couple<Observation, String>> details = obs.getRight();
			
	            for (Couple<Observation, String> detail : details) {
	            	if (detail.getLeft() == Observation.AGENTNAME) {
	            		
	                    String agentName = detail.getRight();	                    
	                    
	                    if(!this.already_com.contains(agentName)) {
	                    	this.already_com.add(agentName);
	                    	myAgent.setReceiverName(agentName);
	                    	myAgent.setMsgRetour(6);
	                    	
	                    	if (myAgent.getLocalName().compareTo(agentName) < 0) {
	                        	System.out.println(myAgent.getLocalName() + " doit aller dans ping goto");
	                        	myAgent.setTypeMsg(5);
	                        	this.exitValue = 3;     
	                        } else {
	                        	System.out.println(myAgent.getLocalName() + " doit aller dans pong goto");
	                            this.exitValue = 4;
	                        }                    	
			                this.finished = true;
			                return;
	                    }
	            	}    
	        	}
	        }
        }
		
		if(cpt < shortestPath.size()) {
			boolean moved = ((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(shortestPath.get(cpt)));
			
			// dans le cas où on doit s'arrêter avant obj car embouteillage 
			// pour le rdv après exploration faudra faire une condition au cas où 
			// y en a un qu'est coincé alors qu'il pense être au rdv
			if(!moved) {
				if(this.cpt_block < 5) {
					this.cpt_block ++;
				} else {
					this.cpt = 0;
					this.already_com.clear();
					if(myAgent.getTypeMsgInit() != -1) {
		        		this.exitValue = myAgent.getTypeMsgInit();
		        		myAgent.setTypeMsgInit(-1);
		        	} else {
		        		this.exitValue = myAgent.getTypeMsg();
		        	}
					//myAgent.setTypeInterblocage(2);
					//this.exitValue = 5;
					this.finished = true;
					return;
				}
			} else {
				this.cpt++;
				this.cpt_block = 0;
			}
		}
		else {
			this.cpt_block = 0;
			this.cpt = 0;
			this.already_com.clear();
			myAgent.setShortestPath(new ArrayList<>());
			if(myAgent.getTypeMsgInit() != -1) {
        		this.exitValue = myAgent.getTypeMsgInit();
        		myAgent.setTypeMsgInit(-1);
        	} else {
        		this.exitValue = myAgent.getTypeMsg();
        	}
			this.finished = true;
			return;
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
