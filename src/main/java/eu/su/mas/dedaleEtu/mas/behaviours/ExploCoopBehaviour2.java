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
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
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
    private boolean stop = false;
    //private Map<String, Boolean> agents_fin;    
    private Set<String> alreadyPinged = new HashSet<>();
    private String lastPos = "";
    private int cpt_block = 0;  
    private int exitValue = 0;
    
    private MapRepresentation myMap;
    
    private Map<String, Integer> historique_com = new HashMap<>();

    public ExploCoopBehaviour2(final ExploreCoopAgent2 myagent) {
        super(myagent);
        //this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
        /*this.list_agentNames = agentNames;
        this.nodesToTransmit = new HashMap<>();
        this.list_gold = list_gold;
        this.list_diamond = list_diamond;
        this.agents_fin = new HashMap<>();
        
        
        // faire partie où on attend que tout le monde ait fini pour vraiment finir la communication
        for (String agentName : agentNames) {
            this.agents_fin.put(agentName, false);
        }*/
        
        for(String n : ((ExploreCoopAgent2) this.myAgent).getAgentNames()) {
        	this.historique_com.put(n, 0);
        }
                
    }

    @Override
    public void action() {
    	
    	// NE PAS OUBLIER DE MODIFIER LE ALREADYSEE POUR METTRE UNE ESPECE DE COMPTEUR
    	// CAR ICI POUR L'INSTANT, JE LES OBLIGE A PARTAGER QU'UNE SEULE FOIS LEUR CARTE MAX
    	
    	this.finished = false;
    	this.exitValue = 0;    	
    	
    	// variables récupérées de l'agent
    	ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;

    	//MapRepresentation myMap = myAgent.getMyMap();
    	List<String> agentNames = myAgent.getAgentNames();
    	Map<String, Map<Observation, String>> list_gold = myAgent.getListGold();
    	Map<String, Map<Observation, String>> list_diamond = myAgent.getListDiamond();
    	Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
    	Set<String> currentlyExchanging = myAgent.getCurrentlyExchanging();
    	Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit = myAgent.getNodesToTransmit();
    	
    	this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
    	
        if (this.myMap == null) {
        	myMap = new MapRepresentation();
        	((GlobalBehaviour) this.getParent()).setMyMap(myMap);
        }
        
        for (String n : historique_com.keySet()) {
            int cpt = historique_com.get(n);
            if (cpt > 0) {
                historique_com.put(n, cpt - 1);
            }
        }
        
        

        // 0) Récupérer la position actuelle de l'agent
        Location myPosition = ((AbstractDedaleAgent) myAgent).getCurrentPosition();
        if (myPosition == null) return;
        
        try { myAgent.doWait(100); } catch (Exception e) {}
        

        // 1) Observer l'environnement
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();

        try { myAgent.doWait(1000); } catch (Exception e) { e.printStackTrace(); }

        // 2) Marquer le nœud actuel comme visité
        myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);
        
        // Détection si inter-blocage et si c'est le cas on part chercher une solution
        if(this.lastPos == myPosition.getLocationId() && currentlyExchanging.isEmpty()) {
        	this.cpt_block++;
        } else {
        	this.cpt_block = 0;
        }
        
        if(this.cpt_block == 5) {        	
        	this.finished = true;
        	this.exitValue = 2;
        	return;
        }
        
        
        // 3) Explorer les nœuds accessibles et ajouter les nouvelles connexions
        String nextNodeId = null;
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location accessibleNode = obs.getLeft();
            List<Couple<Observation, String>> details = obs.getRight();
            
            boolean isNewNode = myMap.addNewNode(accessibleNode.getLocationId());
            
            // Vérifie que le nœud observé (accessibleNode) n'est pas la position actuelle (myPosition).
            if (!myPosition.getLocationId().equals(accessibleNode.getLocationId())) {
                myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());                
                
                // On ajoute les noeuds et arcs découverts dans les listes à partager
                if(isNewNode) {
	                for (String agentName : agentNames) {
	                    nodesToTransmit.putIfAbsent(agentName, new SerializableSimpleGraph<>());
	                    SerializableSimpleGraph<String, MapAttribute> g = nodesToTransmit.get(agentName);
	                    g.addNode(myPosition.getLocationId(), MapAttribute.closed);
	                    g.addNode(accessibleNode.getLocationId(), MapAttribute.open);
	                    g.addEdge("", myPosition.getLocationId(), accessibleNode.getLocationId());
	                }
                }
                
                // nextNodeId devient le noeud nouvellement découvert (s'il y en a un) à visiter à la prochaine itération
                if (nextNodeId == null && isNewNode) nextNodeId = accessibleNode.getLocationId();
            }
            
            // Initialisation des listes de trésors à transmettre
            Map<Observation, String> data = new HashMap<>(); 
        	boolean gold = false;
        	boolean diamond = false;
        	

            // 4) Observation
            for (Couple<Observation, String> detail : details) {
            	
            	// Détecter les agents voisins et leur envoyer les nouveaux nœuds
                if (detail.getLeft() == Observation.AGENTNAME) {
                    String agentName = detail.getRight();
                    stop = true;
                    
                       
                    SerializableSimpleGraph<String, MapAttribute> partialGraph = nodesToTransmit.get(agentName);

                    if (partialGraph != null && !partialGraph.getAllNodes().isEmpty()) {
                        //System.out.println("⛏ Comparaison stricte avec alreadyExchanged: " + alreadyExchanged);
                        
                        //if (!alreadyExchanged.contains(agentName) && !currentlyExchanging.contains(agentName)) {
                    	if (this.historique_com.get(agentName) == 0 && !currentlyExchanging.contains(agentName)) {
                            //System.out.println("📡 Démarrage d’un échange avec " + agentName);
                            currentlyExchanging.add(agentName);
                            this.historique_com.put(agentName, 10);

                            myAgent.setReceiverName(agentName);
                            myAgent.setMapToSend(partialGraph);

                            myAgent.setMsgRetour(0);
                            if (myAgent.getLocalName().compareTo(agentName) < 0) {
                            	myAgent.setTypeMsg(1);
                            	this.exitValue = 3;
                                
                            } else {
                            	System.out.println(myAgent.getLocalName() + " doit aller dans pong");
                                this.exitValue = 4;
                            }

                            this.finished = true;
                            return;
                        } /*else {
                            System.out.println("Échange déjà fait ou en cours avec " + agentName + ", on passe. Avancée du compteur : " + this.cpt_block);
                        }*/
                    }
                    
                }
                
                // VOIR POUR AUSSI TRANSMETTTRE CETTE LISTE DE FACON OPTI
                // POTENTIELLEMENT FAIRE UN TICKER POUR LES MAJ AU CAS OU MODIFICATION TRESOR PAR GOLEM ETC, POUR ENVOI MESS DU CHGT LE PLUS RECENT
                
                
                // Ajout des informations des trésors
                if (detail.getLeft() == Observation.LOCKPICKING) {
                	//data.add(Integer.parseInt(detail.getRight()));
                	data.put(detail.getLeft(), detail.getRight());
            	}
                
                if (detail.getLeft() == Observation.STRENGH) {
                	//data.add(Integer.parseInt(detail.getRight()));
                	data.put(detail.getLeft(), detail.getRight());
            	}
                
                if (detail.getLeft() == Observation.LOCKSTATUS) {
                	//data.add(Integer.parseInt(detail.getRight()));
                	data.put(detail.getLeft(), detail.getRight());
            	}
                
                if (detail.getLeft() == Observation.GOLD) {
                	gold = true;
                	//data.add(Integer.parseInt(detail.getRight()));
                	data.put(detail.getLeft(), detail.getRight());
                }
                
                if (detail.getLeft() == Observation.DIAMOND) {
                	diamond = true;
                	//data.add(Integer.parseInt(detail.getRight()));
                	data.put(detail.getLeft(), detail.getRight());
                }
            }
            
            if(gold) {
            	list_gold.putIfAbsent(myPosition.getLocationId(), data);
            }
            
            if(diamond) {
            	list_diamond.putIfAbsent(myPosition.getLocationId(), data);
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
            List<String> shortestPath = myMap.getShortestPath(myPosition.getLocationId(), obj);
            myAgent.setShortestPath(shortestPath);
            
            //((GlobalBehaviour)this.getParent()).setShortestPath(shortestPath);
            myAgent.setTypeMsg(2);
            
            alreadyExchanged.clear();
            
        	this.exitValue = 1;
        	finished = true;
        	return;
        }
        
        // 6) Déterminer le prochain déplacement
        if (nextNodeId == null) {
            nextNodeId = myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);
        }
        

        // 7) Se déplacer vers le prochain nœud
        if (currentlyExchanging.isEmpty()) {
        	((AbstractDedaleAgent) myAgent).moveTo(new GsLocation(nextNodeId));
        }
        
        // on garde en mémoire la position actuelle
        this.lastPos = myPosition.getLocationId();
        
        /*if(this.cpt_block == 4) {
        	System.out.println("id Node : " + nextNodeId);
        }*/
    }
    
    
    private String calculerBarycentreTopologique(Set<String> treasureNodes) {
        String bestNode = null;
        int minTotalDistance = Integer.MAX_VALUE;
      
        for (SerializableNode<String, MapAttribute> node : myMap.getSerializableGraph().getAllNodes()) {
        	String candidate = node.getNodeId();
        	int totalDistance = 0;
            boolean reachable = true;

            for (String treasure : treasureNodes) {
                List<String> path = myMap.getShortestPath(candidate, treasure);
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
        return this.finished;
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }
    
}
