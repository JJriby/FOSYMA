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
    
    public PlanDAttaqueBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
				
		// faire une condition pour attendre que tout le monde soit là pour communiquer
		
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    Map<String, Observation> list_treasure_type = myAgent.getListTreasureType();
	    Map<String, Set<Couple<Observation,Integer>>> list_expertise = myAgent.getListExpertise();
	    Map<String, List<Couple<Observation,Integer>>> list_back_free_space = myAgent.getListBackFreeSpace();
	    Map<String, Boolean> list_validation = myAgent.getListValidation();
	    
	    Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
	    List<String> list_ordre = myAgent.getListOrdre();
	    String parole = myAgent.getParole();
	    
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
	            Location accessibleNode = obs.getLeft();
	            List<Couple<Observation, String>> details = obs.getRight();
			
	            for (Couple<Observation, String> detail : details) {
	            	if (detail.getLeft() == Observation.AGENTNAME) {
	            		
	                    String agentName = detail.getRight();	                    
	                    
	                    //if(!alreadyExchanged.contains(agentName) && !myAgent.getCurrentlyExchanging().contains(agentName)) {
	                    if(!this.already_com.contains(agentName) && !myAgent.getCurrentlyExchanging().contains(agentName)) {
	                    	this.already_com.add(agentName);
	                    	//this.voisins.add(agentName);
	                    	myAgent.setReceiverName(agentName);
	                    	myAgent.getCurrentlyExchanging().add(agentName);
	                    	myAgent.setMsgRetour(2);
	                    	
	                    	if (myAgent.getLocalName().compareTo(agentName) < 0) {
	                        	myAgent.setTypeMsg(3);
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
	    	System.out.println("Finito : Seconde phase");
	    }
		
		
        
        // A optimiser lors du share-expertise : s'il reçoit une liste de validation différente de la sienne alors il transmet à tout le monde sauf à l'agent qui lui a transmis la nouvelle info
		
		
		
		// l'agent avec la plus grande capacité du trésor en question choisit le plus gros trésor 
		// il vérifie s'il peut l'ouvrir seul : lockingExpertise + Strengh et s'il peut alors il y va seul
		// sinon, il génère plusieurs coalitions possibles en s'assurant que les autres n'ont pas un backpack de capacité nulle pour le trésor en question
		// il choisit le groupe d'agents qui évite le plus la perte des 10% (donc j'imagine on veut le moins de personnes possibles)
		
		// ensuite, une fois les trésors attribués aux agents, on informe le silot des trésors sélectionnés
		// il calcule ensuite un nouveau barycentre en fonction des trésors sélectionnés
		
		
		
		// on verra la partie de l'adaptation de la place du silot où l'un des coffres est vide plus tard

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
