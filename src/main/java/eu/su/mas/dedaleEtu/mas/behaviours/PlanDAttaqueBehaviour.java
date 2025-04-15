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
	    	
	    	// si l'agent est le silo, alors il établit la stratégie
	    	String stratege = "";
	    	
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
	    	
	    	
	    	if(stratege.equals(myAgent.getLocalName())){
	    		/*
	    		 * liste trésors types : {Tim=Gold, Silo=Any}
				 * liste expertise : {Tim=[<Strength, 1>, <LockPicking, 3>], Silo=[<Strength, -1>, <LockPicking, -1>]}
				 * liste back pack : {Tim=[<Gold, 15>, <Diamond, 0>], Silo=[<Gold, 10000>, <Diamond, 10000>]}
				 * liste validation : {Elsa=false, Tim=false, Silo=false}
	    		 */
	    		
	    		/*
	    		 * golds : {14={LockIsOpen=0, Strength=0, LockPicking=1, Gold=24}, 82={LockIsOpen=0, Strength=0, LockPicking=0, Gold=95}, 96={LockIsOpen=0, Strength=1, LockPicking=2, Gold=24}}
				 * diamonds : {66={LockIsOpen=0, Strength=1, LockPicking=0, Diamond=46}, 112={LockIsOpen=0, Strength=0, LockPicking=0, Diamond=10}, 19={LockIsOpen=0, Strength=1, LockPicking=0, Diamond=41}}
	    		 */
	    		
	    		
	    		// On trie la liste des trésors d'or
	    		List<Map.Entry<String, Map<Observation, String>>> tri_tresors_gold = new ArrayList<>(list_gold.entrySet());

	    		tri_tresors_gold.sort((elt1, elt2) -> {
	    			    int val1 = Integer.parseInt(elt1.getValue().getOrDefault(Observation.GOLD, "0"));
	    			    int val2 = Integer.parseInt(elt2.getValue().getOrDefault(Observation.GOLD, "0"));
	    			    return Integer.compare(val2, val1);
	    		});
	    		
	    		// On trie la liste des trésors de diamand
	    		List<Map.Entry<String, Map<Observation, String>>> tri_tresors_diamond = new ArrayList<>(list_diamond.entrySet());

	    		tri_tresors_diamond.sort((elt1, elt2) -> {
	    			    int val1 = Integer.parseInt(elt1.getValue().getOrDefault(Observation.DIAMOND, "0"));
	    			    int val2 = Integer.parseInt(elt2.getValue().getOrDefault(Observation.DIAMOND, "0"));
	    			    return Integer.compare(val2, val1);
	    		});
	    		
	    		
	    		// On trie la liste des capacités libres des sac à dos en or
	    		List<Map.Entry<String, List<Couple<Observation, Integer>>>> tri_capacite_gold = new ArrayList<>(list_back_free_space.entrySet());

	    		tri_capacite_gold.sort((elt1, elt2) -> {
	    			    int val1 = getObservationBackPackAgent(elt1.getValue(), Observation.GOLD);
	    			    int val2 = getObservationBackPackAgent(elt2.getValue(), Observation.GOLD);
	    			    return Integer.compare(val2, val1);
	    		});
	    		
	    		// On trie la liste des capacités libres des sac à dos en diamand
	    		List<Map.Entry<String, List<Couple<Observation, Integer>>>> tri_capacite_diamond = new ArrayList<>(list_back_free_space.entrySet());

	    		tri_capacite_diamond.sort((elt1, elt2) -> {
	    			    int val1 = getObservationBackPackAgent(elt1.getValue(), Observation.GOLD);
	    			    int val2 = getObservationBackPackAgent(elt2.getValue(), Observation.GOLD);
	    			    return Integer.compare(val2, val1);
	    		});
	    		
	    		// on retire les caractéristiques du Silo (il part pas en expédition lui)
	    		tri_capacite_gold.removeIf(elt -> elt.getKey().equals(myAgent.getLocalName()));
	    		tri_capacite_diamond.removeIf(elt -> elt.getKey().equals(myAgent.getLocalName()));
	    		
	    		
	    		// on dissocie l'expertise car ce sera plus simple 
	    		Map<String, Integer> list_lock_agents = new HashMap<>();
	    		Map<String, Integer> list_strength_agents = new HashMap<>();
	    		for(Map.Entry<String, Set<Couple<Observation,Integer>>> elt : list_expertise.entrySet()) {
	    			String agentName = elt.getKey();
		    		Set<Couple<Observation,Integer>> details_agent = elt.getValue();
					
					for(Couple<Observation,Integer> detail_agent : details_agent) {
						if(detail_agent.getLeft() == Observation.STRENGH) {
							list_strength_agents.put(agentName,detail_agent.getRight());
						}
						if(detail_agent.getLeft() == Observation.LOCKPICKING) {
							list_lock_agents.put(agentName,detail_agent.getRight());
						}
					}			
	    		}
	    			
	   	    		
	    		Map<String, String> list_objectifs = new HashMap<>();
	    		
	    		
	    		// on génère toutes les coalitions possibles
	    		List<List<String>> list_coalitions = new ArrayList<>();
	    		generateCoalitions(agentNames, 0, new ArrayList<>(), list_coalitions);
	    		
	    		
	    		// l'agent avec la plus grande capacité du trésor en question choisit le plus gros trésor
	    		for(Map.Entry<String, List<Couple<Observation, Integer>>> elt1 : tri_capacite_gold) {
	    			String agentName = elt1.getKey();
	    			if(list_objectifs.containsKey(agentName)) {
	    				continue;
	    			}
	    			
	    			// on garde que les coalitions de l'agent avec le plus gros back pack
	    			List<List<String>> list_coalitions_agent = new ArrayList<>();
	    			for(List<String> l : list_coalitions) {
	    				if(l.contains(agentName)) {
	    					boolean pb = false;
	    					for(String a : l) {
	    						if(list_objectifs.containsKey(a)) {
	    							pb = true;
	    							break;
	    						}
	    					}
	    					if(!pb) {
	    						list_coalitions_agent.add(l);
	    					}
	    				}
	    			}
	    			
	    			// on parcourt les trésors ayant la plus grosse quantité à disposition
	    			for(Map.Entry<String, Map<Observation, String>> elt2 : tri_tresors_gold) {
	    				String localisation = elt2.getKey();
	    				Map<Observation, String> details = elt2.getValue();
	    					    				
	    				String is_open = details.get(Observation.LOCKSTATUS);
	    					    				
	    				// si le trésor est fermé
	    				if(is_open.equals("0")) {
	    					// on récupère les caractéristiques du trésor en question
		    				int strength_tresor = Integer.parseInt(details.get(Observation.STRENGH));
		    				int lock_tresor = Integer.parseInt(details.get(Observation.LOCKPICKING));
	    					
		    				// on parcourt toutes les coalitions
		    				for(List<String> l : list_coalitions_agent) {
		    					
	    						int strength_tot = 0;
	    						int lock_tot = 0;
		    					
	    						// on parcourt tous les agents de la coalition
		    					for(String a : l) {
		    						
			    					// on récupère les caractériqtiques de l'agent adéquat
			    					int strength_agent = list_strength_agents.get(a);
			    					int lock_agent = list_lock_agents.get(a);		
			    					
			    					strength_tot += strength_agent;
			    					lock_tot += lock_agent;
		    					}
			    					
		    					// si la coalition ne respecte pas les conditions, on la supprime
		    					if(lock_tot < lock_tresor || strength_tot < strength_tresor) {
		    						list_coalitions_agent.remove(l);
		    					}

	    					}
	    				} else { // si le trésor est déjà ouvert, on se préoccupe que de la force
	    					// on récupère les caractéristiques du trésor en question
		    				int strength_tresor = Integer.parseInt(details.get(Observation.STRENGH));
	    					
		    				// on parcourt toutes les coalitions
		    				for(List<String> l : list_coalitions_agent) {
	
	    						int strength_tot = 0;
		    					
	    						// on parcourt tous les agents de la coalition
		    					for(String a : l) {
		    						
			    					// on récupère les caractériqtiques de l'agent adéquat
			    					int strength_agent = list_strength_agents.get(a);
			    					
			    					strength_tot += strength_agent;
		    					}
			    					
		    					// si la coalition ne respecte pas les conditions, on la supprime
		    					if(strength_tot < strength_tresor) {
		    						list_coalitions_agent.remove(l);
		    					}

	    					}
	    				}
	    				
	    				// on choisit quelle coalition prendre (évite le plus la perte des 10%)
	    				double gain = 0;
	    				List<String> coalition_finale = new ArrayList<>();
	    				for(List<String> l : list_coalitions_agent) {
	    					int back_pack_tot = 0;
	    					for(String a : l) {
	    						for(Map.Entry<String, List<Couple<Observation, Integer>>> a_gold : tri_capacite_gold) {
	    			    			String agentNameGold = a_gold.getKey();
	    			    			if(agentNameGold.equals(a)) {
	    			    				int back_pack = getObservationBackPackAgent(a_gold.getValue(), Observation.GOLD);
	    			    				back_pack_tot += back_pack;
	    			    				break;
	    			    			}
	    						}
	    					}
	    					double gain_coalition = back_pack_tot * (1 - 0.1 * l.size()); // pas sûre de la formule, à revoir
	    					if(gain_coalition > gain) {
	    						gain = gain_coalition;
	    						coalition_finale = l;
	    					}
	    				}
	    				
	    				// on met à jour les objectifs des membres de la coalition
	    				for(String a : coalition_finale) {
	    					list_objectifs.put(a, localisation);
	    				}		
	    			}
	    		}
	    		
	    		// faire une condition si on n'a pas attribué une localisation à tout le monde (mettre localisation aléatoire ou jsp)
	    		
	    	}
	    }
		
		
		// ensuite, une fois les trésors attribués aux agents, on informe le silot des trésors sélectionnés
		// il calcule ensuite un nouveau barycentre en fonction des trésors sélectionnés
		
		
		
		// on verra la partie de l'adaptation de la place du silot où l'un des coffres est vide plus tard

	}
	
	
	public int getObservationBackPackAgent(List<Couple<Observation, Integer>> list, Observation obs) {
	    for (Couple<Observation, Integer> couple : list) {
	        if (couple.getLeft() == obs) {
	            return couple.getRight();
	        }
	    }
	    return 0;  // valeur par défaut si l'observation n'est pas trouvée
	}
	
	
	public void generateCoalitions(List<String> agents, int cpt, List<String> coalition, List<List<String>> list_coalitions) {
		if(!coalition.isEmpty()) {
			list_coalitions.add(new ArrayList<>(coalition));
		}
		for(int i=cpt; i < agents.size(); i++) {
			coalition.add(agents.get(i));
			generateCoalitions(agents, i+1, coalition, list_coalitions);
			coalition.remove(coalition.size()-1);
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
