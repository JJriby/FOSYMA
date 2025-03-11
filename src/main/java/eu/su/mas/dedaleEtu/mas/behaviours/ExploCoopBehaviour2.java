package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ExploCoopBehaviour2 extends SimpleBehaviour {

    private static final long serialVersionUID = 8567689731496787661L;
    private boolean finished = false;
    private MapRepresentation myMap;
    private List<String> list_agentNames;
    
    // Stocke les sous-graphes des nœuds à transmettre pour chaque agent
    private Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit;
    
    private boolean stop = false;
    private Location rdv = null;
    private Map<String, Boolean> liste_rdv;

    public ExploCoopBehaviour2(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.nodesToTransmit = new HashMap<>();
        this.liste_rdv = new HashMap<>();
        if (this.myAgent.getLocalName().equals("Elsa")) {
        	this.rdv = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
        	System.out.println(this.myAgent.getLocalName() + " a défini le lieu de rencontre à " + this.rdv);
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

        // 1) Observer l'environnement
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

        try { this.myAgent.doWait(1000); } catch (Exception e) { e.printStackTrace(); }

        // 2) Marquer le nœud actuel comme visité
        this.myMap.addNode(myPosition.getLocationId(), MapAttribute.closed);

        // 3) Ajouter le nœud actuel aux sous-graphes des agents voisins
        for (String agentName : list_agentNames) {
            nodesToTransmit.putIfAbsent(agentName, new SerializableSimpleGraph<>());
            nodesToTransmit.get(agentName).addNode(myPosition.getLocationId(), MapAttribute.closed);
        }

        // 4) Explorer les nœuds accessibles et ajouter les nouvelles connexions
        String nextNodeId = null;
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location accessibleNode = obs.getLeft();
            List<Couple<Observation, String>> details = obs.getRight();

            boolean isNewNode = this.myMap.addNewNode(accessibleNode.getLocationId());
            // Vérifie que le nœud observé (accessibleNode) n'est pas la position actuelle (myPosition).
            if (!myPosition.getLocationId().equals(accessibleNode.getLocationId())) {
                this.myMap.addEdge(myPosition.getLocationId(), accessibleNode.getLocationId());
                // Si nextNodeId n’a pas encore été défini ET que le nœud observé est un nouveau nœud, alors nextNodeId prend l'identifiant du premier nœud nouvellement découvert. (sélection du prochain noeud à explorer)
                if (nextNodeId == null && isNewNode) nextNodeId = accessibleNode.getLocationId();
            }

            // 5) Détecter les agents voisins et leur envoyer les nouveaux nœuds
            for (Couple<Observation, String> detail : details) {
                if (detail.getLeft() == Observation.AGENTNAME) {
                    String agentName = detail.getRight();
                    stop = true;
                    // Récupérer le sous-graphe à envoyer à cet agent
                    SerializableSimpleGraph<String, MapAttribute> partialGraph = nodesToTransmit.get(agentName);
                    if (partialGraph != null && !partialGraph.getAllNodes().isEmpty()) {
                    	System.out.println(this.myAgent.getLocalName() + " envoie une partie de sa carte à " + agentName);   
                    	this.myAgent.addBehaviour(new ShareMapBehaviour2(this.myAgent, myMap, agentName, partialGraph));
                        nodesToTransmit.put(agentName, new SerializableSimpleGraph<>()); // Reset après envoi
                        
                        // METTRE UN TEMPS D'ATTENTE LE TEMPS QUE L'AUTRE RECEPTIONNE SA CARTE
                        
                        // répétition avec le 8), pb -> essayer d'utiliser un boolean
                        // pour bloquer l'agent pour attendre la carte de l'autre agent
                        /*MessageTemplate msgTemplate = MessageTemplate.and(
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
                                nodesToTransmit.put(agentName, new SerializableSimpleGraph<>()); // Nettoyer après fusion
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println(this.myAgent.getLocalName() + " n'a pas reçu de réponse de " + agentName);
                        }*/
                        
                        /*if (!liste_rdv.get(agentName)) {
                            System.out.println(this.myAgent.getLocalName() + " envoie le RDV à " + agentName);
                            ACLMessage rdvMsg = new ACLMessage(ACLMessage.INFORM);
                            rdvMsg.setProtocol("SHARE-RDV");
                            rdvMsg.setContent(rdv.getLocationId()); // Envoi du lieu de RDV sous forme de String
                            rdvMsg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
                            this.myAgent.send(rdvMsg);
                        }*/
                        
                    }
                }
            }
        }

        // 6) Vérifier si l'exploration est terminée
        if (!this.myMap.hasOpenNode()) {
            finished = true;
            System.out.println(this.myAgent.getLocalName() + " - Exploration terminée !");
            return;
        }

        // 7) Déterminer le prochain déplacement
        if (nextNodeId == null) {
            nextNodeId = this.myMap.getShortestPathToClosestOpenNode(myPosition.getLocationId()).get(0);
        }

        // 8) Vérifier si un autre agent a partagé une carte
        MessageTemplate msgTemplate = MessageTemplate.and(
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
	            	
	                /*SerializableSimpleGraph<String, MapAttribute> partialGraph2 = nodesToTransmit.get(id_sender.getLocalName());
	                if (partialGraph2 != null && !partialGraph2.getAllNodes().isEmpty()) {
	                	//System.out.println(this.myAgent.getLocalName() + " renvoie sa carte à " + id_sender.getLocalName());
	                    this.myAgent.addBehaviour(new ShareMapBehaviour2(this.myAgent, myMap, id_sender.getLocalName(), partialGraph2));
	                    nodesToTransmit.put(id_sender.getLocalName(), new SerializableSimpleGraph<>()); // Reset après envoi
	                }*/
	                stop = false;
	
	            }
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }

        // 9) Se déplacer vers le prochain nœud
        if (!stop) {
        	((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(nextNodeId));
        }
    }

    @Override
    public boolean done() {
        return finished;
    }
    
}
