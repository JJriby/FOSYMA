package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class SuitePlanDAttaqueBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597682931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    private List<String> voisins = new ArrayList<>();
    private Set<String> already_paroles_passees = new HashSet<>();
    private Set<String> already_com = new HashSet<>();
    private Set<String> last_com = new HashSet<>();
    
    Map<String, String> list_objectifs;
    Map<String, Integer> list_lock_agents = new HashMap<>();
    Map<String, Integer> list_strength_agents = new HashMap<>();
    
    private int pour_debugger = 0;
    
    public SuitePlanDAttaqueBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		if(pour_debugger == 0) {
			System.out.println(myAgent.getLocalName() + " est en suite");
			pour_debugger++;
		}
		
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
		
		this.list_objectifs = myAgent.getListObjectifs();
				
		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
		
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
		
		// On trie la liste des trésors de diamants
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
		for(Map.Entry<String, Set<Couple<Observation,Integer>>> elt : list_expertise.entrySet()) {
			String agentName = elt.getKey();
    		Set<Couple<Observation,Integer>> details_agent = elt.getValue();
			
			for(Couple<Observation,Integer> detail_agent : details_agent) {
				if(detail_agent.getLeft() == Observation.STRENGH) {
					this.list_strength_agents.put(agentName,detail_agent.getRight());
				}
				if(detail_agent.getLeft() == Observation.LOCKPICKING) {
					this.list_lock_agents.put(agentName,detail_agent.getRight());
				}
			}			
		}
			
	 
		// on génère toutes les coalitions possibles
		List<List<String>> list_coalitions_gold = new ArrayList<>();
		generationCoalitions(tri_capacite_gold, 0, new ArrayList<>(), list_coalitions_gold);
		
		List<List<String>> list_coalitions_diamond = new ArrayList<>();
		generationCoalitions(tri_capacite_diamond, 0, new ArrayList<>(), list_coalitions_diamond);
		
		attributionObjectifs(tri_tresors_gold, tri_capacite_gold, list_coalitions_gold, Observation.GOLD);
		attributionObjectifs(tri_tresors_diamond, tri_capacite_diamond, list_coalitions_diamond, Observation.DIAMOND);

		
		// faire une condition si on n'a pas attribué une localisation à tout le monde (mettre localisation aléatoire ou jsp)
		// ou sinon l'ajouter à l'une des coalitions pour qu'il récupère quand même des trésors
		
		
		// on calcule un nouveau barycentre en fonction des trésors sélectionnés
		Set<String> pos_tresors = new HashSet<>();
		for(Map.Entry<String,String> t : this.list_objectifs.entrySet()) {
			pos_tresors.add(t.getValue());
		}
		
		myAgent.setPosSilo(this.myMap.calculBarycentre(pos_tresors));	
		myAgent.setShortestPath(this.myMap.getShortestPath(((AbstractDedaleAgent) myAgent).getCurrentPosition().getLocationId(), myAgent.getPosSilo()));
		
		System.out.println("attribution des objectifs : " + myAgent.getListObjectifs());
		System.out.println("position du silo : " + myAgent.getPosSilo());
		
	}
	
	
	public int getObservationBackPackAgent(List<Couple<Observation, Integer>> list, Observation obs) {
	    for (Couple<Observation, Integer> couple : list) {
	        if (couple.getLeft() == obs) {
	            return couple.getRight();
	        }
	    }
	    return 0;
	}
	
	
	public void generationCoalitions(List<Map.Entry<String, List<Couple<Observation, Integer>>>> agents, int cpt, List<String> coalition, List<List<String>> list_coalitions) {
		if(!coalition.isEmpty()) {
			list_coalitions.add(new ArrayList<>(coalition));
		}
		for(int i=cpt; i < agents.size(); i++) {
			coalition.add(agents.get(i).getKey());
			generationCoalitions(agents, i+1, coalition, list_coalitions);
			coalition.remove(coalition.size()-1);
		}
	}
	
	
	public void attributionObjectifs(List<Map.Entry<String, Map<Observation, String>>> tri_tresors, List<Map.Entry<String, List<Couple<Observation, Integer>>>> tri_capacite, List<List<String>> list_coalitions, Observation obs) {
		// l'agent avec la plus grande capacité du trésor en question choisit le plus gros trésor
		for(Map.Entry<String, List<Couple<Observation, Integer>>> elt1 : tri_capacite) {
			String agentName = elt1.getKey();
			if(this.list_objectifs.containsKey(agentName)) {
				continue;
			}
			
			// on garde que les coalitions de l'agent avec le plus gros back pack
			List<List<String>> list_coalitions_agent = new ArrayList<>();
			for(List<String> l : list_coalitions) {
				if(l.contains(agentName)) {
					boolean pb = false;
					for(String a : l) {
						if(this.list_objectifs.containsKey(a)) {
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
			for(Map.Entry<String, Map<Observation, String>> elt2 : tri_tresors) {
				String localisation = elt2.getKey();
				Map<Observation, String> details = elt2.getValue();
				
				if(this.list_objectifs.containsValue(localisation)) {
					continue;
				}
					    				
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
	    					int strength_agent = this.list_strength_agents.get(a);
	    					int lock_agent = this.list_lock_agents.get(a);		
	    					
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
	    					int strength_agent = this.list_strength_agents.get(a);
	    					
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
						for(Map.Entry<String, List<Couple<Observation, Integer>>> a_tresor : tri_capacite) {
			    			String agentNameTresor = a_tresor.getKey();
			    			if(agentNameTresor.equals(a)) {
			    				int back_pack = getObservationBackPackAgent(a_tresor.getValue(), obs);
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
				
				if(!coalition_finale.isEmpty()) {
    				// on met à jour les objectifs des membres de la coalition
    				for(String a : coalition_finale) {
    					this.list_objectifs.put(a, localisation);
    				}	
    				break;
				}
			}
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
