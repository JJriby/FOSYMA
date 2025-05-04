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

import org.graphstream.graph.Node;

public class ExploCoopBehaviour2 extends Behaviour {

    private static final long serialVersionUID = 8567689731496787661L;
    private boolean finished = false;        
    private String lastPos = "";
    private int cpt_block = 0;  
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    private Map<String, Integer> historique_com = new HashMap<>();

    public ExploCoopBehaviour2(final ExploreCoopAgent2 myagent) {
        super(myagent);
        
        for(String n : ((ExploreCoopAgent2) this.myAgent).getAgentNames()) {
        	this.historique_com.put(n, 0);
        }
                
    }

    @Override
    public void action() {
    	    	
    	this.finished = false;
    	this.exitValue = -1;    	
    	
    	// variables récupérées de l'agent
    	ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;

    	//MapRepresentation myMap = myAgent.getMyMap();
    	List<String> agentNames = myAgent.getAgentNames();
    	Map<String, Map<Observation, String>> list_gold = myAgent.getListGold();
    	Map<String, Map<Observation, String>> list_diamond = myAgent.getListDiamond();
    	Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
    	//Set<String> currentlyExchanging = myAgent.getCurrentlyExchanging();
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
        
        
        
     // Réception des listes fin d'explo envoyées par d'autres agents
        MessageTemplate template = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-FIN-EXPLO"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage msg = myAgent.receive(template);
        if (msg != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Boolean> receivedFinExplo = (Map<String, Boolean>) msg.getContentObject();
                for (Map.Entry<String, Boolean> entry : receivedFinExplo.entrySet()) {
                    if (entry.getValue()) {
                        myAgent.getListFinExplo().put(entry.getKey(), true);
                    }
                }
                System.out.println("[DEBUG] " + myAgent.getLocalName() + " a reçu une listFinExplo de " + msg.getSender().getLocalName());
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }

        // 0) Récupérer la position actuelle de l'agent
        Location myPosition = ((AbstractDedaleAgent) myAgent).getCurrentPosition();
        if (myPosition == null) return;
        
        try { myAgent.doWait(100); } catch (Exception e) {}
        

        // 1) Observer l'environnement
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();

        try { myAgent.doWait(1000); } catch (Exception e) { e.printStackTrace(); }

        
        // si le noeud où on se trouve était précédemment ouvert, on l'ajoute dans les noeuds à partager
        for(String a : agentNames) {
            nodesToTransmit.putIfAbsent(a, new SerializableSimpleGraph<>());
            SerializableSimpleGraph<String, MapAttribute> g = nodesToTransmit.get(a);

            Node currentNode = myMap.getGraph().getNode(myPosition.getLocationId());
            MapAttribute currentAttribute = (currentNode != null && currentNode.getAttribute("ui.class").equals(MapAttribute.closed.toString())) 
                ? MapAttribute.closed 
                : MapAttribute.open;

            SerializableNode<String, MapAttribute> node = g.getNode(myPosition.getLocationId());
            if(node == null || node.getNodeContent() != currentAttribute) {
                g.addNode(myPosition.getLocationId(), currentAttribute);
            }
        }
        
        
        // 2) Marquer le nœud actuel comme visité
        myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);
        //System.out.println("[DEBUG] " + myAgent.getLocalName() + " marked current node " + myPosition.getLocationId() + " as CLOSED");
        
        // Détection si inter-blocage et si c'est le cas on part chercher une solution
        if(this.lastPos == myPosition.getLocationId()) {
        	this.cpt_block++;
        	myAgent.doWait(500);
        } else {
        	this.cpt_block = 0;
        }
        
        if(this.cpt_block == 5) { 
        	myAgent.setTypeMsg(1);
        	this.finished = true;
        	this.exitValue = 2;
        	return;
        }
        
        
     // 3) Explorer les nœuds accessibles et ajouter les nouvelles connexions
        String nextNodeId = null;
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location accessibleNode = obs.getLeft();
            List<Couple<Observation, String>> details = obs.getRight();

            boolean isNewNode = false;
            Node existing = myMap.getGraph().getNode(accessibleNode.getLocationId());
            if (existing == null) {
            	myMap.addNode(accessibleNode.getLocationId(), MapAttribute.open);
            	isNewNode = true;
            }

            // Vérifie que le nœud observé (accessibleNode) n'est pas la position actuelle (myPosition).
            if (!myPosition.getLocationId().equals(accessibleNode.getLocationId())) {
                myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());

                // On récupère l'état réel du noeud depuis myMap
                Node graphNode = myMap.getGraph().getNode(accessibleNode.getLocationId());
                MapAttribute currentAttr = (graphNode != null && graphNode.getAttribute("ui.class").equals(MapAttribute.closed.toString()))
                    ? MapAttribute.closed
                    : MapAttribute.open;

                // Mise à jour correcte des nodesToTransmit avec l'attribut réel
                for (String agentName : agentNames) {
                    nodesToTransmit.putIfAbsent(agentName, new SerializableSimpleGraph<>());
                    SerializableSimpleGraph<String, MapAttribute> g = nodesToTransmit.get(agentName);

                    SerializableNode<String, MapAttribute> existingNode = g.getNode(accessibleNode.getLocationId());
                    if (existingNode == null || existingNode.getNodeContent() != currentAttr) {
                        g.addNode(accessibleNode.getLocationId(), currentAttr);
                    }

                    // ajout des arcs
                    Set<String> neighbors = g.getEdges(myPosition.getLocationId());
                    if (neighbors == null || !neighbors.contains(accessibleNode.getLocationId())) {
                        g.addEdge("", myPosition.getLocationId(), accessibleNode.getLocationId());
                    }
                }

                // nextNodeId devient le noeud nouvellement découvert (s'il y en a un) à visiter à la prochaine itération
                if (nextNodeId == null && isNewNode)
                    nextNodeId = accessibleNode.getLocationId();
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

                    SerializableSimpleGraph<String, MapAttribute> partialGraph = nodesToTransmit.get(agentName);

                    if (this.historique_com.get(agentName) == 0) {

                        this.historique_com.put(agentName, 10);

                        myAgent.setReceiverName(agentName);
                        
                        
                     // Toujours forcer le nœud actuel à fermé juste avant envoi
                        myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

	                    SerializableSimpleGraph<String, MapAttribute> freshGraph = myMap.getSerializableGraph();
	                    myAgent.setMapToSend(freshGraph);
	
	                    Map<String, Boolean> finExplo = myAgent.getListFinExplo();
	                    ACLMessage finExploMsg = new ACLMessage(ACLMessage.INFORM);
	                    finExploMsg.setProtocol("SHARE-FIN-EXPLO");
	                    finExploMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
	                    finExploMsg.setSender(myAgent.getAID());
	                    try {
	                        finExploMsg.setContentObject((java.io.Serializable) new HashMap<>(finExplo));
	                        myAgent.sendMessage(finExploMsg);
	                        System.out.println("[DEBUG] " + myAgent.getLocalName() + " a envoyé sa listFinExplo à " + agentName);
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
	                    
	                    
                        myAgent.setMsgRetour(0);
                        if (myAgent.getLocalName().compareTo(agentName) < 0) {
                            myAgent.setTypeMsg(1);
                            this.exitValue = 3;
                        } else {
                            this.exitValue = 4;
                        }

                        this.finished = true;
                        return;
                    }
                }

                // Ajout des informations des trésors
                if (detail.getLeft() == Observation.LOCKPICKING ||
                    detail.getLeft() == Observation.STRENGH ||
                    detail.getLeft() == Observation.LOCKSTATUS ||
                    detail.getLeft() == Observation.GOLD ||
                    detail.getLeft() == Observation.DIAMOND) {

                    data.put(detail.getLeft(), detail.getRight());

                    if (detail.getLeft() == Observation.GOLD) gold = true;
                    if (detail.getLeft() == Observation.DIAMOND) diamond = true;
                }
            }

            if (gold) {
                list_gold.putIfAbsent(myPosition.getLocationId(), data);
            }

            if (diamond) {
                list_diamond.putIfAbsent(myPosition.getLocationId(), data);
            }
        }
        
        
        // 5) Vérifier si l'exploration est terminée
        if (!this.myMap.hasOpenNode()) {
        	System.out.println("[DEBUG] " + myAgent.getLocalName() + " - listFinExplo = " + myAgent.getListFinExplo());
            if (!myAgent.getListFinExplo().get(myAgent.getLocalName())) {
                System.out.println(this.myAgent.getLocalName() + " - Exploration terminée !");
                myAgent.getListFinExplo().put(myAgent.getLocalName(), true);
                
                
                //SEND END EXPLO
                ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
                msg1.setProtocol("SHARE-FIN-EXPLO");
                msg1.setSender(myAgent.getAID());
                for (String agent : myAgent.getAgentNames()) {
                	msg1.addReceiver(new AID(agent, AID.ISLOCALNAME));
                }
                try {
                	msg1.setContentObject((java.io.Serializable) new HashMap<>(myAgent.getListFinExplo()));
                    myAgent.sendMessage(msg1);
                    System.out.println("[DEBUG] " + myAgent.getLocalName() + " a envoyé sa listFinExplo");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                
                

                Set<String> treasureNodes = new HashSet<>();
                treasureNodes.addAll(list_gold.keySet());
                treasureNodes.addAll(list_diamond.keySet());
                String obj = myMap.calculBarycentre(treasureNodes);
                System.out.println("RDV : "+ obj + " trésors : " + treasureNodes);

                List<String> shortestPath = myMap.getShortestPath(myPosition.getLocationId(), obj);
                myAgent.setShortestPath(shortestPath);

                myAgent.setTypeMsg(2);  // fin d'exploration
                alreadyExchanged.clear();
            }
            
            // Continuer tant qu’un autre n’a pas fini
            if (myAgent.getListFinExplo().containsValue(false)) {
                return; // attendre les autres
            }

            // Tous ont fini → terminer vraiment
            this.exitValue = 1;
            finished = true;
            return;
        }
       
        // 6) Déterminer le prochain déplacement
        if (nextNodeId == null) {
            nextNodeId = myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);
        }
        
        
     
        
        
        
        

        // 7) Se déplacer vers le prochain nœud
       	((AbstractDedaleAgent) myAgent).moveTo(new GsLocation(nextNodeId));
        
        
        // on garde en mémoire la position actuelle
        this.lastPos = myPosition.getLocationId();
        myAgent.setNoeudBloque(nextNodeId);
       
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