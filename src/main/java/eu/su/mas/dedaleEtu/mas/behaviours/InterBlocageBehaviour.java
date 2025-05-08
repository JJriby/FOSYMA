package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.*;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

public class InterBlocageBehaviour extends Behaviour {

    private static final long serialVersionUID = 1L;
    private boolean finished = false;
    private int exitValue = -1;
    private List<String> nodesToAvoid = new ArrayList<>();

    private MapRepresentation myMap;

    public InterBlocageBehaviour(final ExploreCoopAgent2 myAgent) {
        super(myAgent);
    }

    @Override
    public void action() {
    	
    	
        this.finished = false;
        this.exitValue = -1;
        ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
        this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
        
        this.nodesToAvoid.clear();
        
        System.out.println("blocage " + myAgent.getLocalName());
        
        /*boolean pong_interblocage = myAgent.checkMessagesInterBlocage();
        if (pong_interblocage) {
			System.out.println("pong provient de interblock");
			//myAgent.setMsgRetour(GlobalBehaviour.TO_GO_TO_RDV);
		    
			//this.exitValue = GlobalBehaviour.TO_SHARE_INFOS_INTERBLOCAGE;
		    //this.finished = true;
		    //return;
			myAgent.setTransmissionInterblocage(true);
		    myAgent.setDecalage(false);
		} else {
			myAgent.setTransmissionInterblocage(false);
		}*/
        
        myAgent.setReceptionInterblocage(false);
        
        if(myAgent.checkDemandeDecalage()) {
        	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
        	this.finished = true;
        	return;
        }
        

        Location myPosition = ((AbstractDedaleAgent) myAgent).getCurrentPosition();
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();

        // 1. Collect nodes to avoid (occupied by other agents)
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location loc = obs.getLeft();
            for (Couple<Observation, String> o : obs.getRight()) {
                if (o.getLeft().equals(Observation.AGENTNAME)) {
                	System.out.println("ici : " + loc.getLocationId() + " à cause de " + o.getRight());
                    nodesToAvoid.add(loc.getLocationId());
                    if(loc.getLocationId().equals(myAgent.getNoeudBloque())) {
                    	myAgent.setBlockingAgent(o.getRight());
                    }
                }
            }
        }
        
        if(nodesToAvoid.isEmpty()) {
        	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
            this.finished = true;
            return;
        }

        // 2. Try normal open node path avoiding occupied nodes
        List<String> path = myMap.getShortestPathToClosestOpenNode2(myPosition.getLocationId(), nodesToAvoid);
        if (path != null && !path.isEmpty()) {
            myAgent.setShortestPath(path);
            //myAgent.setTypeMsg(1);
            this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
            this.finished = true;
            return;
        }

        // 3. If no normal path, apply advanced interblockage resolution
        // Compare with other agent's info (mocked logic for now)
        
        
        String otherAgent = myAgent.getBlockingAgent();
        System.out.println(myAgent.getLocalName() + " a comme bloquant " + otherAgent + " car pos pb : " + myAgent.getNoeudBloque());
        if (otherAgent != null) {
        	if(!myAgent.getTransmissionInterblocage() && !myAgent.getReceptionInterblocage()) {
        		        		
	        	// 1. PING : on envoit un message à l'agent qui nous bloque
	        	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	        	msg.setProtocol("PING-INTERBLOCAGE");
	        	msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
	        	msg.setContent("Je suis bloqué, peux-tu te décaler ?");
	        	msg.setSender(myAgent.getAID());
	        	System.out.println(myAgent.getLocalName() + " → PING inter envoyé à " + otherAgent);
	        	myAgent.sendMessage(msg);
	        	
	        	// 2. Attente du PONG
	            MessageTemplate pongTemplate = MessageTemplate.and(
	                MessageTemplate.MatchProtocol("PONG-INTERBLOCAGE"),
	                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
	            );
	
	            ACLMessage pong = myAgent.receive(pongTemplate);

	            
	            if (pong != null) {
	            	
	            	myAgent.setTransmissionInterblocage(true);
	            	myAgent.setDecalage(false);
	                this.exitValue = GlobalBehaviour.TO_SHARE_INFOS_INTERBLOCAGE;
	                this.finished = true;
	                return;
	            }
        	}
            
            
            int myTreasure = myAgent.getCollectedTreasureValue();
            int otherTreasure = myAgent.getKnownTreasureValue(otherAgent);
            int myCounter = myAgent.getEquityCounter();
            int otherCounter = myAgent.getKnownEquityCounter(otherAgent);     
            
            List<String> path_found = null;
            
            Set<String> nodesToAvoid2 = new HashSet<>(nodesToAvoid);
            /*if (myAgent.getPathToAvoid() != null) {
                all_to_avoid.addAll(myAgent.getPathToAvoid());
            }*/
            
            List<String> avoidPath = myAgent.getPathToAvoid();

            
        	if(myAgent.getLocalName().equals(myAgent.getAgentSilo())) {
            	myAgent.setGoalNode(myAgent.getPosSilo());
            	
                //List<String> avoidPath = myAgent.getPathToAvoid();
                String currentNode = myPosition.getLocationId();
                
                System.out.println("avoid : " + avoidPath);

                path_found = findSafeNodeToMove(currentNode, avoidPath, nodesToAvoid2);

                if (path_found != null && !path_found.isEmpty()) {
                    myAgent.setShortestPath(path_found);
                    myAgent.setDecalage(false);
                    myAgent.setHasNewPath("hasNewPath");
                    System.out.println(myAgent.getLocalName() + " se décale vers : " + path_found);
                    
                    myAgent.setHasNewPath("hasNewPath");
                    myAgent.setShortestPath(path_found);
                    myAgent.setMode("blocking");
                    this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                    this.finished = true;
                    return;
                } else {
                	System.out.println("aucun noeud sûr trouvé pour se décaler.");
                    myAgent.setHasNewPath("hasNotNewPath");
                	/*myAgent.setDecalage(false);
            		myAgent.setTransmissionInterblocage(false);
            		myAgent.setPriorityNow(true);
                    // Envoi d’un message à l'agent bloqué pour lui dire de se décaler
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setProtocol("DEMANDE-DECALAGE");
                    msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
                    msg.setContent("Je ne peux pas me décaler, c'est à toi de bouger.");
                    myAgent.sendMessage(msg);
                    return;*/
                }
            	
        	} else {
        		myAgent.setGoalNode(myAgent.getListObjectifs().get(myAgent.getLocalName()));
        	}
        	
            boolean iHavePriority = false;
            if(otherAgent.equals(myAgent.getAgentSilo())) {
            	iHavePriority = true;
            } else if (myTreasure > 1.5 * otherTreasure) {
                iHavePriority = true;
            } else if (myTreasure == otherTreasure) {
            	if(myCounter < otherCounter) {
            		iHavePriority = true;
            	} else if(myCounter == otherCounter) {
            		iHavePriority = myAgent.getLocalName().compareTo(otherAgent) < 0;
            	}
            }

            // si je suis prioritaire et que je n'ai pas à bouger mon chemin
            if ((iHavePriority && !myAgent.getDecalage()) || myAgent.getPriorityNow()) {
            	path_found = myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), myAgent.getGoalNode());
            	
            	myAgent.setPriorityNow(false);
                myAgent.incrementEquityCounter();
                myAgent.setShortestPath(path_found);
                System.out.println("Chemin inchangé : " + myAgent.getShortestPath());
                // Keep my path
            	myAgent.setTransmissionInterblocage(false);
                myAgent.setHasNewPath("hasNotNewPath");
                /*this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                this.finished = true;
                return;*/
            } else {
            	if(myAgent.getDecalage()) {
                	System.out.println(myAgent.getLocalName() + " FORCÉ de se décaler à cause d'une demande explicite.");
            	}
                // Try to find alternate DFS path avoiding other's path
                //List<String> avoidPath = myAgent.getPathToAvoid();
                String goal = myAgent.getGoalNode();
                
                Set<String> totalToAvoid = new HashSet<>();
                if (avoidPath != null) {
                    totalToAvoid.addAll(avoidPath);
                }
                if (nodesToAvoid != null) {
                    totalToAvoid.addAll(nodesToAvoid);
                }
                List<String> totAvoid = new ArrayList<>(totalToAvoid);
                
                path_found = myMap.getAlternativePath(myPosition.getLocationId(), goal, totAvoid);

                if (path_found != null && !path_found.isEmpty()) {
                    myAgent.setShortestPath(path_found);
                    System.out.println("nouveau chemin court : " + myAgent.getShortestPath() + " et nodes to avoid : " + avoidPath);
                    //myAgent.setTypeMsg(1);
	            	myAgent.setTransmissionInterblocage(false);
	            	myAgent.setDecalage(false);
                    myAgent.setHasNewPath("hasNewPath");
                    /*this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                    this.finished = true;
                    return;*/
                } else {
                	
                	// on tente d'aller sur un noeud ne gênant pas l'autre agent
                    String currentNode = myPosition.getLocationId();
                    path_found = findSafeNodeToMove(currentNode, avoidPath, nodesToAvoid2);

                    if (path_found != null && !path_found.isEmpty()) {
                        myAgent.setShortestPath(path_found);
                        myAgent.setDecalage(false);
                        myAgent.setHasNewPath("hasNewPath");
                        myAgent.setMode("blocking");
                        /*this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                        this.finished = true;
                        return;*/
                    }
                    
                	/*// No alternative, move far away
                    List<String> farNodes = myMap.getFarAwayOpenNodes(myPosition.getLocationId());
                    if (!farNodes.isEmpty()) {
                        String randomFar = farNodes.get(new Random().nextInt(farNodes.size()));
                        List<String> escape = myMap.getShortestPath(myPosition.getLocationId(), randomFar);
                        if (escape != null && !escape.isEmpty()) {
                            myAgent.setShortestPath(escape);
                            System.out.println("nouveau chemin alternatif : " + myAgent.getShortestPath());
                            //myAgent.setTypeMsg(1);
        	            	myAgent.setTransmissionInterblocage(false);
                            this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                            this.finished = true;
                            return;
                        }
                    }*/
                }
            }
            
            if (path_found != null && !path_found.isEmpty()) {
            	myAgent.setHasNewPath("hasNewPath");
                myAgent.setShortestPath(path_found);
                this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                this.finished = true;
                /*this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                this.finished = true;
                return;*/
            } else {
            	System.out.println(this.myAgent.getLocalName() + " n'a trouvé aucun noeud sûr pour se décaler.");
        		myAgent.setDecalage(false);
        		myAgent.setTransmissionInterblocage(false);
        		myAgent.setPriorityNow(true);
                myAgent.setHasNewPath("hasNotNewPath");
        		
                // Envoi d’un message à l'agent bloqué pour lui dire de se décaler
                /*ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setProtocol("DEMANDE-DECALAGE");
                msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
                msg.setContent("Je ne peux pas me décaler, c'est à toi de bouger.");
                myAgent.sendMessage(msg);
                return;*/
            }
            
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.setProtocol("DEMANDE-DECALAGE");
            msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
            msg.setContent(myAgent.getHasNewPath());
            msg.setSender(myAgent.getAID());
            myAgent.sendMessage(msg);
            return;
            
        }
        
        
        // on tente de trouver un autre chemin jusqu'à l'objectif (au cas où golem)
        List<String> newPath = myMap.getAlternativePath(myPosition.getLocationId(), myAgent.getGoalNode(), Collections.singletonList(myAgent.getNoeudBloque()));
        if(newPath != null) {
        	System.out.println("newPath");
        	myAgent.setShortestPath(newPath);
            myAgent.setMode("blocking");
        	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
            this.finished = true;
            return;
        }

        // 4. As last fallback, move randomly to unoccupied neighbor
        List<String> freeNeighbors = new ArrayList<>();
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
        	
            String nodeId = obs.getLeft().getLocationId();
            boolean isFree = true;
            for (Couple<Observation, String> o : obs.getRight()) {
                if (o.getLeft().equals(Observation.AGENTNAME)) {
                    isFree = false;
                    break;
                }
            }
            if (isFree && !nodeId.equals(myPosition.getLocationId())) {
                freeNeighbors.add(nodeId);
            }
        }
        
        System.out.println("free neighbors ");

        if (!freeNeighbors.isEmpty()) {
            System.out.println("Troisième cas");
            String randomMove = freeNeighbors.get(new Random().nextInt(freeNeighbors.size()));
            myAgent.setShortestPath(List.of(randomMove));
            //myAgent.setTypeMsg(1);
        	myAgent.setTransmissionInterblocage(false);
        	myAgent.setDecalage(false);
            myAgent.setMode("blocking");
            this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
            this.finished = true;
        } else {
        	// si on lui a déjà dit au préalable de bouger alors qu'il avait la priorité, alors RIP
        	if(myAgent.getDecalage()) {
        		myAgent.setDecalage(false);
                System.out.println(myAgent.getLocalName() + " a reçu une demande de décalage mais n'a trouvé aucun chemin.");
                block(500);
                return;
        	} else { // s'il n'avait pas la priorité de base
        		System.out.println(this.myAgent + " n'a trouvé aucun noeud sûr pour se décaler.");
        	}
            
        }        
    }
    
    
    private List<String> findSafeNodeToMove(String fromNodeId, List<String> avoidPath, Set<String> nodesToAvoid) {
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        visited.add(fromNodeId);
        queue.add(fromNodeId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            List<String> neighbors = this.getNeighbors(current, nodesToAvoid);

            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);

                    // Si ce voisin est un nœud sûr et différent de notre position actuelle
                    if (!avoidPath.contains(neighbor) && !neighbor.equals(fromNodeId)) {
                        List<String> path = myMap.getShortestPath(fromNodeId, neighbor);
                        if (path != null && path.size() > 1) { // on veut au moins un déplacement réel
                            return path;
                        }
                    }

                }
            }
        }

        return null;
    }
    
    public List<String> getNeighbors(String nodeId, Set<String> nodesToAvoid) {
        List<String> neighbors = new ArrayList<>();
        Graph graph = this.myMap.getGraph();
        Node node = graph.getNode(nodeId);
        if (node != null) {
        	node.neighborNodes().forEach(n -> {
                if (!nodesToAvoid.contains(n.getId())) {
                    neighbors.add(n.getId());
                }
            });
        }
        return neighbors;
    }
    

    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public int onEnd() {
        return exitValue;
    }
}
