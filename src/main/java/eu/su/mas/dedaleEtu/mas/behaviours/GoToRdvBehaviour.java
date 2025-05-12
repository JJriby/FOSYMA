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
		
		
		ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
		
		//System.out.println(myAgent.getLocalName() + " est en blocage avec le mode : " + myAgent.getMode());
		
    	myAgent.setMsgRetour(GlobalBehaviour.TO_GO_TO_RDV);

		List<String> shortestPath = myAgent.getShortestPath(); 
		
		if (myAgent.checkMessagesInterBlocage()) {
			//myAgent.setMsgRetour(GlobalBehaviour.TO_GO_TO_RDV);
			this.cpt_block = 0;
			this.cpt = 0;
			this.already_com.clear();
			
			//System.out.println("pong provient de goto");
		    this.exitValue = GlobalBehaviour.TO_SHARE_INFOS_INTERBLOCAGE;
		    this.finished = true;
		    return;
		}
		
		
        try { myAgent.doWait(1000); } catch (Exception e) { e.printStackTrace(); }
        
        if(myAgent.getMode() == "collecte" && myAgent.getLocalName()=="Silo") {
        	System.out.println("Chemin final : " + myAgent.getShortestPath());
        }
        
        if(myAgent.getMode().equals("CartePleine")) {
        
        	
        	if(myAgent.getTypeMsgInit() == -1) {
        		myAgent.setTypeMsgInit(myAgent.getTypeMsg());
        	}
        	
        	
	        // on guette si on croise un agent n'ayant pas fini sur le chemin et on lui envoie une map dans ce cas
	        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();	
	    	
		    for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
	            List<Couple<Observation, String>> details = obs.getRight();
			
	            for (Couple<Observation, String> detail : details) {
	            	if (detail.getLeft() == Observation.AGENTNAME && myAgent.getAgentNames().contains(detail.getRight())) {
	            		
	                    String agentName = detail.getRight();	                    
	                    
	                    if(!this.already_com.contains(agentName)) {
	                    	this.already_com.add(agentName);
	                    	myAgent.setReceiverName(agentName);
	                    	
	                    	if (myAgent.getLocalName().compareTo(agentName) < 0) {
	                        	myAgent.setTypeMsg(GlobalBehaviour.TO_SHARE_FIN_EXPLO);
	                        	this.exitValue = GlobalBehaviour.TO_PING;     
	                        } else {
	                            this.exitValue = GlobalBehaviour.TO_PONG;
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
					myAgent.doWait(1000);
				} else {
					
					//System.out.println(myAgent.getLocalName() + " bloqué depuis 10 tours. Mode = " + myAgent.getMode());
					if(myAgent.getMode().equals("collecte")) {
						myAgent.setNoeudBloque(shortestPath.get(cpt));
						this.cpt_block = 0;
						this.cpt = 0;
						this.already_com.clear();
						this.exitValue = GlobalBehaviour.TO_INTERBLOCAGE;
						this.finished = true;
						return;
					}
					
					if(myAgent.getMode().equals("CartePleine")) {
						myAgent.setNoeudBloque(shortestPath.get(cpt));
						this.exitValue = GlobalBehaviour.TO_INTERBLOCAGE;
						
						/*if(myAgent.getTypeMsgInit() != -1) {
							myAgent.setTypeMsg(myAgent.getTypeMsgInit());
			        		myAgent.setTypeMsgInit(-1);
			        	} 
						
			        	this.exitValue = myAgent.getTypeMsg();
			        	*/
					}
					
					this.cpt_block = 0;
					this.cpt = 0;
					this.already_com.clear();
					
					if(myAgent.getTypeMsgInit() != -1) {
						myAgent.setTypeMsg(myAgent.getTypeMsgInit());
		        		myAgent.setTypeMsgInit(-1);
		        	}
					
		        	this.exitValue = myAgent.getTypeMsg();
					
					this.finished = true;
					return;
				}
			} else {
				this.cpt++;
				this.cpt_block = 0;
			}
		}
		else {
			
			if(myAgent.getMode()=="blocking") {
				this.cpt_block = 0;
				this.cpt = 0;
				this.already_com.clear();
				this.exitValue = GlobalBehaviour.TO_BLOCAGE;
				this.finished = true;
				return;
			}
			
			if(myAgent.getMode() == "cartePleine") {
				myAgent.setMode("finExplo");
			}
			
			this.cpt_block = 0;
			this.cpt = 0;
			this.already_com.clear();
			myAgent.setShortestPath(new ArrayList<>());
			if(myAgent.getTypeMsgInit() != -1) {
				myAgent.setTypeMsg(myAgent.getTypeMsgInit());
        		myAgent.setTypeMsgInit(-1);
        	}
        	this.exitValue = myAgent.getTypeMsg();
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
