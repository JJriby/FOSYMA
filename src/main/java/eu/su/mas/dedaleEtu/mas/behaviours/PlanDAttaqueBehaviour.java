package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class PlanDAttaqueBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597689931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    private List<String> voisins = new ArrayList<>();
    private Set<String> already_paroles_passees = new HashSet<>();
    private Set<String> already_com = new HashSet<>();
    private Set<String> last_com = new HashSet<>();
    
    private int pour_debugger = 0;
    
    public PlanDAttaqueBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		if(pour_debugger == 0) {
			System.out.println(myAgent.getLocalName() + " est dans plan d'attaque");
			pour_debugger++;
		}
		
		// faire une condition pour attendre que tout le monde soit là pour communiquer
		
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    
	    myAgent.setMsgRetour(GlobalBehaviour.TO_PLAN_D_ATTAQUE);
	    
	    List<String> agentNames = myAgent.getAgentNames();
	    Map<String, Observation> list_treasure_type = myAgent.getListTreasureType();
	    Map<String, Set<Couple<Observation,Integer>>> list_expertise = myAgent.getListExpertise();
	    Map<String, List<Couple<Observation,Integer>>> list_back_free_space = myAgent.getListBackFreeSpace();
	    Map<String, Boolean> list_validation = myAgent.getListValidation();
	    
	    // on récupère les caractéristiques des trésors
	    Map<String, Map<Observation, String>> list_gold = myAgent.getListGold();
		Map<String, Map<Observation, String>> list_diamond = myAgent.getListDiamond();
				
		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
		
		// faire condition pour le faire qu'une fois
		// on récupère les caratéristiques de l'agent
		list_treasure_type.put(myAgent.getLocalName(), ((AbstractDedaleAgent) myAgent).getMyTreasureType());
		list_expertise.put(myAgent.getLocalName(), ((AbstractDedaleAgent) myAgent).getMyExpertise());
		list_back_free_space.put(myAgent.getLocalName(), ((AbstractDedaleAgent) myAgent).getBackPackFreeSpace());
		
		
		// si tous les agents connaissent les caractéristiques de tout le monde, 
		// alors on arrête la communication d'expertise et on part préparer le plan d'attaque
		boolean all_validation = true;
		for(Map.Entry<String, Boolean> elt : list_validation.entrySet()) {
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
	            	if (detail.getLeft() == Observation.AGENTNAME && myAgent.getAgentNames().contains(detail.getRight())) {
	            		
	                    String agentName = detail.getRight();	                    
	                    
	                    //if(!alreadyExchanged.contains(agentName) && !myAgent.getCurrentlyExchanging().contains(agentName)) {
	                    if(!this.already_com.contains(agentName)) {
	                    	this.already_com.add(agentName);
	                    	//this.voisins.add(agentName);
	                    	myAgent.setReceiverName(agentName);
	                    	//myAgent.getCurrentlyExchanging().add(agentName);
	                    	
	                    	if (myAgent.getLocalName().compareTo(agentName) < 0) {
	                        	myAgent.setTypeMsg(GlobalBehaviour.TO_SHARE_EXPERTISE);
	                        	this.exitValue = GlobalBehaviour.TO_PING;
	                            
	                        } else {
	                        	//System.out.println(myAgent.getLocalName() + " doit aller dans pong");
	                            this.exitValue = GlobalBehaviour.TO_PONG;
	                        }
	                    	
			                this.finished = true;
		                    return;
	                    }
	            	}    
	        	}
	        }
		    this.already_com.clear();
	    } else {
	    	// pourquoi pas faire juste des ping pour envoyer à ceux qui le veulent ? plutôt que rester en mode ping-pong
	    	List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();	
	    	
		    for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
	            List<Couple<Observation, String>> details = obs.getRight();
			
	            for (Couple<Observation, String> detail : details) {
	            	if (detail.getLeft() == Observation.AGENTNAME && myAgent.getAgentNames().contains(detail.getRight())) {
	            		
	                    String agentName = detail.getRight();	                    
	                    
	                    //if(!alreadyExchanged.contains(agentName) && !myAgent.getCurrentlyExchanging().contains(agentName)) {
	                    if(!this.last_com.contains(agentName)) {
	                    	this.last_com.add(agentName);
	                    	System.out.println("ajout liste contactés : " + last_com);
	                    	//this.voisins.add(agentName);
	                    	myAgent.setReceiverName(agentName);
	                    	//myAgent.getCurrentlyExchanging().add(agentName);
	                    	
	                    	if (myAgent.getLocalName().compareTo(agentName) < 0) {
	                        	myAgent.setTypeMsg(GlobalBehaviour.TO_SHARE_EXPERTISE);
	                        	this.exitValue = GlobalBehaviour.TO_PING; 
	                        } else {
	                        	System.out.println(myAgent.getLocalName() + " doit aller dans pong");
	                            this.exitValue = GlobalBehaviour.TO_PONG;
	                        }                   	
			                this.finished = true;
			                return;
	                    }
	            	}    
	        	}
	        }
		    this.already_com.clear();
		    this.last_com.clear();
	    	
		    // si l'agent est le silo, alors il établit la stratégie
	    	/*String stratege = "";
	    	
	    	for (Map.Entry<String, Set<Couple<Observation, Integer>>> elt : list_expertise.entrySet()) {
	    	    String nom_agent = elt.getKey();
	    	    Set<Couple<Observation, Integer>> expertises = elt.getValue(); 
	    	    boolean all_neg = true;
	    	    for(Couple<Observation, Integer> exp : expertises) {
	    	    	if(exp.getRight() > 0) {
	    	    		all_neg = false;
	    	    		break;
	    	    	}
	    	    }
	    	    if(all_neg) {
	    	    	stratege = nom_agent;
	    	    }
	    	}
	    	
	    	
	    	// on le garde en mémoire pour la récolte
	    	myAgent.setAgentSilo(stratege);
	    	*/
	    	
		    if(myAgent.getTypeAgent().equals("agentTanker")){
		    	myAgent.setAgentSilo(myAgent.getLocalName());
	    		this.exitValue = GlobalBehaviour.TO_SUITE_PLAN_D_ATTAQUE;
	    	} else {
	    		this.exitValue = GlobalBehaviour.TO_ATTENTE;
	    	}
	    	
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
