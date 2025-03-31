package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import dataStructures.serializableGraph.SerializableNode;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ExploCoopBehaviour2 extends Behaviour {

    private static final long serialVersionUID = 8567689731496787661L;
    private boolean finished = false;
    private MapRepresentation myMap;
    private List<String> list_agentNames;
    
    // Stocke les sous-graphes des nœuds à transmettre pour chaque agent
    // private Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit;
    private Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit;
    
    private boolean stop = false;
    private List<String> shortestPath;
    private Map<String, List<Integer>> list_gold; // on stocke dans liste gold, pour chaque localisation, on récupère quantité, lock, strength
    private Map<String, List<Integer>> list_diamond;
    private Map<String, Boolean> agents_fin;
    
    private Set<String> alreadyPinged = new HashSet<>();
    
    // voir si c'est vraiment utile et potentiellement voir pour un reset pour communiquer à nouveau avec un certain agent
    private Set<String> alreadyExchanged = new HashSet<>();
    private Set<String> currentlyExchanging = new HashSet<>(); 
    private String lastPos = "";
    private int cpt_block = 0;
    
    private int exitValue = 0;

    public ExploCoopBehaviour2(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, Map<String, List<Integer>> list_gold, Map<String, List<Integer>> list_diamond) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.nodesToTransmit = new HashMap<>();
        this.list_gold = list_gold;
        this.list_diamond = list_diamond;
        this.agents_fin = new HashMap<>();
        
        // faire partie où on attend que tout le monde ait fini pour vraiment finir la communication
        for (String agentName : list_agentNames) {
            this.agents_fin.put(agentName, false);
        }
        
    }

    @Override
    public void action() {
        if (this.myMap == null) {
            this.myMap = new MapRepresentation();
        }

        // 0) Récupérer la position actuelle de l'agent
        Location myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        if (myPosition == null) return;
        
        try { this.myAgent.doWait(100); } catch (Exception e) {}
        

        // 1) Observer l'environnement
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

        try { this.myAgent.doWait(1000); } catch (Exception e) { e.printStackTrace(); }

        // 2) Marquer le nœud actuel comme visité
        this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);
        
        // Détection si inter-blocage et si c'est le cas on part chercher une solution
        if(this.lastPos == myPosition.getLocationId() && this.currentlyExchanging.isEmpty()) {
        	this.cpt_block++;
        } else {
        	this.cpt_block = 0;
        }
        
        if(this.cpt_block == 5) {
        	((GlobalBehaviour)this.getParent()).setLastObservation(lobs);
        	this.finished = true;
        	this.exitValue = 2;
        	return;
        }
        
        
        // 3) Explorer les nœuds accessibles et ajouter les nouvelles connexions
        String nextNodeId = null;
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location accessibleNode = obs.getLeft();
            List<Couple<Observation, String>> details = obs.getRight();
            
            boolean isNewNode = this.myMap.addNewNode(accessibleNode.getLocationId());
            
            // Vérifie que le nœud observé (accessibleNode) n'est pas la position actuelle (myPosition).
            if (!myPosition.getLocationId().equals(accessibleNode.getLocationId())) {
                this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());                
                
                // On ajoute les noeuds et arcs découverts dans les listes à partager
                if(isNewNode) {
	                for (String agentName : list_agentNames) {
	                    this.nodesToTransmit.putIfAbsent(agentName, new SerializableSimpleGraph<>());
	                    SerializableSimpleGraph<String, MapAttribute> g = this.nodesToTransmit.get(agentName);
	                    g.addNode(myPosition.getLocationId(), MapAttribute.closed);
	                    g.addNode(accessibleNode.getLocationId(), MapAttribute.open);
	                    g.addEdge("", myPosition.getLocationId(), accessibleNode.getLocationId());
	                }
                }
                
                // nextNodeId devient le noeud nouvellement découvert (s'il y en a un) à visiter à la prochaine itération
                if (nextNodeId == null && isNewNode) nextNodeId = accessibleNode.getLocationId();
            }
            
            // Initialisation des listes de trésors à transmettre
            List<Integer> data = new ArrayList<Integer>(); 
        	boolean gold = false;
        	boolean diamond = false;
        	

            // 4) Observation
            for (Couple<Observation, String> detail : details) {
            	
            	// Détecter les agents voisins et leur envoyer les nouveaux nœuds
                if (detail.getLeft() == Observation.AGENTNAME) {
                    String agentName = detail.getRight();
                    stop = true;
                       
                    SerializableSimpleGraph<String, MapAttribute> partialGraph = this.nodesToTransmit.get(agentName);
                    if (partialGraph != null && !partialGraph.getAllNodes().isEmpty() && !alreadyExchanged.contains(agentName) && !currentlyExchanging.contains(agentName)) {
                    	// AJOUTER LA TRANSMISSION DES LISTES DE TRESORS
                        
                        // FAIRE UN CAS Où LORSQU'ON COMMUNIQUE AVEC QLQ QUI A FINI DE NE PAS ATTENDRE CAR SINON PB
                        // FAIRE UN TICKER POUR PAS TCHATTER NON STOP AVEC LE MEME (JSP SI VRAIMENT UTILE VU QU'ON FAIT DES LISTES PAR AGENTS)

                    	
                    	currentlyExchanging.add(agentName);
                    	
                    	if (this.myAgent.getLocalName().compareTo(agentName) < 0) {
                    		//this.myAgent.addBehaviour(new PingBehaviour((AbstractDedaleAgent)this.myAgent, agentName, partialGraph, this.myMap, this.nodesToTransmit, this.alreadyExchanged, this.currentlyExchanging, this.list_gold, this.list_diamond));
                    		((GlobalBehaviour)this.getParent()).setShareMapParams(agentName, partialGraph, this.myMap, this.nodesToTransmit, this.alreadyExchanged, this.currentlyExchanging, this.list_gold, this.list_diamond);
                    		((GlobalBehaviour)this.getParent()).setPingParams(1, agentName);
                    		this.exitValue = 3;
                    	} else {
                    		((GlobalBehaviour)this.getParent()).setPongParams(agentName, partialGraph, this.myMap, this.nodesToTransmit, this.alreadyExchanged, this.currentlyExchanging, this.list_gold, this.list_diamond);
                    		this.exitValue = 4;
                    	}
                    	return;
                    } 
                    
                }
                
                // VOIR POUR AUSSI TRANSMETTTRE CETTE LISTE DE FACON OPTI
                // POTENTIELLEMENT FAIRE UN TICKER POUR LES MAJ AU CAS OU MODIFICATION TRESOR PAR GOLEM ETC, POUR ENVOI MESS DU CHGT LE PLUS RECENT
                
                
                // Ajout des informations des trésors
                if (detail.getLeft() == Observation.LOCKPICKING) {
                	data.add(Integer.parseInt(detail.getRight()));
            	}
                
                if (detail.getLeft() == Observation.STRENGH) {
                	data.add(Integer.parseInt(detail.getRight()));
            	}
                
                if (detail.getLeft() == Observation.LOCKSTATUS) {
                	data.add(Integer.parseInt(detail.getRight()));
            	}
                
                if (detail.getLeft() == Observation.GOLD) {
                	gold = true;
                	data.add(Integer.parseInt(detail.getRight()));
                }
                
                if (detail.getLeft() == Observation.DIAMOND) {
                	diamond = true;
                	data.add(Integer.parseInt(detail.getRight()));
                }
            }
            
            if(gold) {
            	this.list_gold.putIfAbsent(myPosition.getLocationId(), data);
            }
            
            if(diamond) {
            	this.list_diamond.putIfAbsent(myPosition.getLocationId(), data);
            }
        }

        
        // 5) Vérifier si l'exploration est terminée
        if (!this.myMap.hasOpenNode()) {
            System.out.println(this.myAgent.getLocalName() + " - Exploration terminée !");
            
            // Calcul du barycentre des trésors
            Set<String> treasureNodes = new HashSet<>();
            treasureNodes.addAll(list_gold.keySet());
            treasureNodes.addAll(list_diamond.keySet());
            String obj = this.calculerBarycentreTopologique(treasureNodes);
            System.out.println("RDV : "+ obj);
            
            // En avant toute pour le barycentre !
            this.shortestPath = this.myMap.getShortestPath(myPosition.getLocationId(), obj);
            ((GlobalBehaviour)this.getParent()).setShortestPath(this.shortestPath);
        	this.exitValue = 1;
        	finished = true;
        	return;
        }

        // 6) Déterminer le prochain déplacement
        if (nextNodeId == null) {
            nextNodeId = this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);
        }
        

        // 7) Se déplacer vers le prochain nœud
        if (this.currentlyExchanging.isEmpty()) {
        	((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(nextNodeId));
        }
        
        // on garde en mémoire la position actuelle 
        this.lastPos = myPosition.getLocationId();
    }
    
    
    private String calculerBarycentreTopologique(Set<String> treasureNodes) {
    	System.out.println("Trésors : " + treasureNodes);
        String bestNode = null;
        int minTotalDistance = Integer.MAX_VALUE;

        for (SerializableNode<String, MapAttribute> node : this.myMap.getSerializableGraph().getAllNodes()) {
        	String candidate = node.getNodeId();
        	int totalDistance = 0;
            boolean reachable = true;

            for (String treasure : treasureNodes) {
                List<String> path = this.myMap.getShortestPath(candidate, treasure);
                if (path == null || path.isEmpty()) {
                    reachable = false;
                    break;
                }
                totalDistance += path.size(); // nombre de transitions
            }

            if (reachable && totalDistance < minTotalDistance) {
                minTotalDistance = totalDistance;
                bestNode = candidate;
            }
        }

        return bestNode;
    }
    
    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }
    
}
