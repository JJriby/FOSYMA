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
    private String rdv = "";
    private Map<String, Boolean> liste_rdv;
    private boolean premier_tour = true;
    private List<String> shortestPath;
    private Map<String, List<Integer>> list_gold; // on stocke dans liste gold, pour chaque localisation, on récupère quantité, lock, strength
    private Map<String, List<Integer>> list_diamond;
    private Map<String, Boolean> agents_fin;
    
    private int exitValue = 0;

    public ExploCoopBehaviour2(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, Map<String, List<Integer>> list_gold, Map<String, List<Integer>> list_diamond) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.nodesToTransmit = new HashMap<>();
        this.liste_rdv = new HashMap<>();
        this.rdv = null;
        this.list_gold = list_gold;
        this.list_diamond = list_diamond;
        this.agents_fin = new HashMap<>();
        
        
        // faire partie où on attend que tout le monde ait fini pour vraiment finir la communication
        for (String agentName : list_agentNames) {
            this.agents_fin.put(agentName, false);
        }
        
        // pour initialiser la liste des agents qui ont bien reçu le rdv
        for (String agentName : list_agentNames) {
        	this.liste_rdv.put(agentName, false);
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
        
        if (premier_tour && this.myAgent.getLocalName().equals("Elsa")) {
        	this.rdv = myPosition.getLocationId();
        	System.out.println("Lieu de rdv défini par " + this.myAgent.getLocalName() + " : " + rdv);
        	this.premier_tour = false;
        }

        // 1) Observer l'environnement
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

        try { this.myAgent.doWait(1000); } catch (Exception e) { e.printStackTrace(); }

        // 2) Marquer le nœud actuel comme visité
        this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

        // 3) Ajouter le nœud actuel aux sous-graphes des agents voisins
        /*for (String agentName : list_agentNames) {
            this.nodesToTransmit.putIfAbsent(agentName, new SerializableSimpleGraph<>());
            this.nodesToTransmit.get(agentName).addNode(myPosition.getLocationId(), MapAttribute.closed);
        }*/
        

        // 4) Explorer les nœuds accessibles et ajouter les nouvelles connexions
        String nextNodeId = null;
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location accessibleNode = obs.getLeft();
            List<Couple<Observation, String>> details = obs.getRight();
            
            boolean isNewNode = this.myMap.addNewNode(accessibleNode.getLocationId());
            
          
            // Vérifie que le nœud observé (accessibleNode) n'est pas la position actuelle (myPosition).
            if (!myPosition.getLocationId().equals(accessibleNode.getLocationId())) {
                this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());                
                
                for (String agentName : list_agentNames) {
                    this.nodesToTransmit.putIfAbsent(agentName, new SerializableSimpleGraph<>());
                    SerializableSimpleGraph<String, MapAttribute> g = this.nodesToTransmit.get(agentName);
                    g.addNode(myPosition.getLocationId(), MapAttribute.closed);
                    g.addNode(accessibleNode.getLocationId(), MapAttribute.open);
                    g.addEdge("", myPosition.getLocationId(), accessibleNode.getLocationId());
                }
                
                // Si nextNodeId n’a pas encore été défini ET que le nœud observé est un nouveau nœud, alors nextNodeId prend l'identifiant du premier nœud nouvellement découvert. (sélection du prochain noeud à explorer)
                if (nextNodeId == null && isNewNode) nextNodeId = accessibleNode.getLocationId();
            }
            
            List<Integer> data = new ArrayList<Integer>(); 
        	boolean gold = false;
        	boolean diamond = false;

            // 5) Détecter les agents voisins et leur envoyer les nouveaux nœuds
            for (Couple<Observation, String> detail : details) {
            	
                if (detail.getLeft() == Observation.AGENTNAME) {
                    String agentName = detail.getRight();
                    stop = true;
                    
                    
                    // Récupérer le sous-graphe à envoyer à cet agent
                    SerializableSimpleGraph<String, MapAttribute> partialGraph = this.nodesToTransmit.get(agentName);
                    System.out.println(partialGraph);
                    if (partialGraph != null && !partialGraph.getAllNodes().isEmpty()) {
                    	System.out.println(this.myAgent.getLocalName() + " envoie une partie de sa carte à " + agentName);  
                    	// AJOUTER LA TRANSMISSION DES LISTES DE TRESORS
                    	this.myAgent.addBehaviour(new ShareMapBehaviour2(this.myAgent, partialGraph, agentName));
                        this.nodesToTransmit.put(agentName, new SerializableSimpleGraph<>()); // Reset après envoi
                        
                        // METTRE UN TEMPS D'ATTENTE LE TEMPS QUE L'AUTRE RECEPTIONNE SA CARTE  
                        // FAIRE UN CAS Où LORSQU'ON COMMUNIQUE AVEC QLQ QUI A FINI DE NE PAS ATTENDRE CAR SINON PB
                        // FAIRE UN TICKER POUR PAS TACHATTER NON STOP AVEC LE MEME (JSP SI VRAIMENT UTILE VU QU'ON FAIT DES LISTES PAR AGENTS)
                        
                        
                        // répétition avec le 8), pb -> essayer d'utiliser un boolean
                        // pour bloquer l'agent pour attendre la carte de l'autre agent
                        
                    }
                    
                    /*MapRepresentation map_to_share = new MapRepresentation();

                    SerializableSimpleGraph<String, MapAttribute> subgraph = this.nodesToTransmit.get(agentName);
                    if (subgraph != null && !subgraph.getAllNodes().isEmpty()) {
                        // Ajouter les nœuds et leurs attributs
                        for (SerializableNode<String, MapAttribute> node : subgraph.getAllNodes()) {
                            map_to_share.addNode(node.getNodeId(), node.getNodeContent());
                        }

                        // Ajouter les arêtes
                        for (SerializableNode<String, MapAttribute> node : subgraph.getAllNodes()) {
                        	System.out.println("tartiflette : " + subgraph.getEdges(node.getNodeId()));
                            for (String neighborId : subgraph.getEdges(node.getNodeId())) {
                                map_to_share.addEdge(node.getNodeId(), neighborId);
                            }
                        }
                        
                        System.out.println(this.myAgent.getLocalName() + " envoie une partie de sa carte à " + agentName);   
                    	this.myAgent.addBehaviour(new ShareMapBehaviour2(this.myAgent, map_to_share, agentName));
                        this.nodesToTransmit.put(agentName, new SerializableSimpleGraph<>());
                    
                    }*/
                    
                    // Réceptionner le sous-graphe
                    MessageTemplate msgTemplate = MessageTemplate.and(
                    		MessageTemplate.MatchProtocol("SHARE-NEW-NODES"),
                    		MessageTemplate.MatchPerformative(ACLMessage.INFORM)
                    );
                        
                    ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
                       
                        
                    if (msgReceived == null) {
                    	msgReceived = this.myAgent.blockingReceive(msgTemplate, 3000);
                    }
                        
                    if (msgReceived != null) {
                    	System.out.println(this.myAgent.getLocalName() + " a reçu une carte de " + agentName + " en retour !");
                        try {
                        	SerializableSimpleGraph<String, MapAttribute> receivedMap = 
                        			(SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();
                            this.myMap.mergeMap(receivedMap);
                            // a revoir
                            this.nodesToTransmit.put(agentName, new SerializableSimpleGraph<>()); // Nettoyer après fusion
                            stop = false;
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    } else {
                    	System.out.println(this.myAgent.getLocalName() + " n'a pas reçu de réponse de " + agentName);
                    }
               
                    
	                // Envoi du lieu de rdv à l'agent rencontré si celui-ci ne le connait pas
	                if (!this.liste_rdv.get(agentName) && this.rdv != null) {
                        System.out.println(this.myAgent.getLocalName() + " envoie le rdv " + this.rdv + " à " + agentName);
                        
                        ACLMessage rdvMsg = new ACLMessage(ACLMessage.INFORM);
                        rdvMsg.setProtocol("SHARE-RDV");
                        rdvMsg.setSender(this.myAgent.getAID());
                        rdvMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
                        rdvMsg.setContent(this.rdv); // Envoi du lieu de RDV sous forme de String
                        ((AbstractDedaleAgent)this.myAgent).sendMessage(rdvMsg);
	                }
	                
	                
	                // Réception du lieu de rdv
	                if (this.rdv != "" && !this.liste_rdv.get(agentName)) {
		                MessageTemplate msgTemplate2 = MessageTemplate.and(
	                    		MessageTemplate.MatchProtocol("SHARE-RDV"),
	                    		MessageTemplate.MatchPerformative(ACLMessage.INFORM)
	                    );
	                        
		                ACLMessage msgReceived2 = null;
		                int maxAttempts = 3;
		                while (msgReceived2 == null && maxAttempts > 0) {
		                    msgReceived2 = this.myAgent.blockingReceive(msgTemplate2, 3000); // 🔵 Attente max de 3 sec
		                    maxAttempts--;
		                }
		                
		                
	                    //ACLMessage msgReceived2 = this.myAgent.receive(msgTemplate2);
	                        
	                    if (msgReceived2 != null) {
                        	//System.out.println(msgReceived2.getContentObject());
                        	this.rdv = msgReceived2.getContent();
                        	this.liste_rdv.put(agentName, true);
                        	System.out.println(this.myAgent.getLocalName() + " a reçu le rdv en " + this.rdv + " de  " + agentName);
	                    } else {
	                    	System.out.println(this.myAgent.getLocalName() + " n'a pas reçu de rdv de " + agentName + " car il l'a déjà : " + this.rdv);
	                    }
	                } 
                }
                
                // VOIR POUR AUSSI TRANSMETTTRE CETTE LISTE DE FACON OPTI
                // POTENTIELLEMENT FAIRE UN TICKER POUR LES MAJ AU CAS OU MODIFICATION TRESOR PAR GOLEM ETC, POUR ENVOI MESS DU CHGT LE PLUS RECENT
                
                // Quand on atterit sur un trésor contenant de l'or
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
                	//System.out.println("liste : " + detail.getLeft());
                	gold = true;
                	data.add(Integer.parseInt(detail.getRight()));
                }
                
                // Quand on atterit sur un trésor contenant des diamands
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

        // 6) Vérifier si l'exploration est terminée
        if (!this.myMap.hasOpenNode()) {
            System.out.println(this.myAgent.getLocalName() + " - Exploration terminée !");
            System.out.println("liste des trésors or : " + list_gold);
            System.out.println("liste des trésors diamant : " + list_diamond);
            /*this.shortestPath = this.myMap.getShortestPath(myPosition.getLocationId(), this.rdv);
            System.out.println("Carte " + this.myAgent.getLocalName() + " : " + this.shortestPath);*/
            
            Set<String> treasureNodes = new HashSet<>();
            treasureNodes.addAll(list_gold.keySet());
            treasureNodes.addAll(list_diamond.keySet());
            
            
            String obj = this.calculerBarycentreTopologique(treasureNodes);
            System.out.println("obj :"+ obj);
            
            this.shortestPath = this.myMap.getShortestPath(myPosition.getLocationId(), obj);
        	//this.myAgent.addBehaviour(new GoToRdvBehaviour((AbstractDedaleAgent) this.myAgent, this.shortestPath));
        	
        	//((GlobalBehaviour)this.getParent()).transitionTo("GoToRDV");
            ((GlobalBehaviour)this.getParent()).setShortestPath(this.shortestPath);
        	this.exitValue = 1;
        	finished = true;
        	return;
        }

        // 7) Déterminer le prochain déplacement
        if (nextNodeId == null) {
            nextNodeId = this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);
        }

        // 8) Vérifier si un autre agent a partagé une carte
        /*MessageTemplate msgTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-NEW-NODES"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);

        if (msgReceived != null) {
            try {
            	// on récupère la carte reçue
                SerializableSimpleGraph<String, MapAttribute> sgreceived = 
                    (SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();
                AID id_sender = msgReceived.getSender();
	            if(sgreceived != null) {
	            	System.out.println(this.myAgent.getLocalName() + " a reçu une carte de " + id_sender.getLocalName());
	            	this.myMap.mergeMap(sgreceived);
	            	
	                SerializableSimpleGraph<String, MapAttribute> partialGraph2 = nodesToTransmit.get(id_sender.getLocalName());
	                if (partialGraph2 != null && !partialGraph2.getAllNodes().isEmpty()) {
	                	//System.out.println(this.myAgent.getLocalName() + " renvoie sa carte à " + id_sender.getLocalName());
	                    this.myAgent.addBehaviour(new ShareMapBehaviour2(this.myAgent, myMap, id_sender.getLocalName(), partialGraph2));
	                    nodesToTransmit.put(id_sender.getLocalName(), new SerializableSimpleGraph<>()); // Reset après envoi
	                }
	                stop = false;
	
	            }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }*/

        // 9) Se déplacer vers le prochain nœud
        if (!stop) {
        	((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(nextNodeId));
        }
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
