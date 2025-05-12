package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.io.Serializable;
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
        
        //System.out.println("blocage " + myAgent.getLocalName());
        
        
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
                	//System.out.println("ici : " + loc.getLocationId() + " à cause de " + o.getRight());
                    nodesToAvoid.add(loc.getLocationId());
                    if(loc.getLocationId().equals(myAgent.getNoeudBloque())) {
                    	myAgent.setBlockingAgent(o.getRight());
                    }
                }
            }
        }
        
        /*if(nodesToAvoid.isEmpty()) {
        	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
            this.finished = true;
            return;
        }*/
        
        
        if(myAgent.getMode().equals("explo")) {
        	List<String> noeuds_ouverts = this.myMap.getFarAwayOpenNodes(myAgent.getCurrentPosition().getLocationId());
        	List<String> chemin = null;
        	for(String noeud : noeuds_ouverts) {
        		chemin = this.myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), noeud);
        		if(chemin != null) {
        			myAgent.setShortestPath(chemin);
                	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                    this.finished = true;
        		}
        	}
        	
        	if(chemin == null || chemin.isEmpty()) {
        		List<String> all_nodes = new ArrayList<>();
                for (org.graphstream.graph.Node node : this.myMap.getGraph()) {
                    all_nodes.add(node.getId());
                }
                
                if(!all_nodes.isEmpty()) {
                	while(chemin == null) {
	                    Random rand = new Random();
	                    String random_noeud = all_nodes.get(rand.nextInt(all_nodes.size()));
	
	                    chemin = this.myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), random_noeud);
	                    if(chemin != null) {
	                    	myAgent.setShortestPath(chemin);
	                    	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
	                        this.finished = true;
	                    }
                	}
                }
        	}
        	
    		//System.out.println("chemin : " + myAgent.getShortestPath());
            return;
        }
        
        
        
        if(myAgent.getMode().equals("CartePleine")) {
        	
            ACLMessage ptRdvMsg = new ACLMessage(ACLMessage.INFORM);
            ptRdvMsg.setProtocol("SHARE-DEMANDE-LIEU-RDV");
            ptRdvMsg.setSender(myAgent.getAID());
            ptRdvMsg.addReceiver(new AID(myAgent.getBlockingAgent(), AID.ISLOCALNAME));
            ptRdvMsg.setContent("Est-ce le pt de rdv ?");
            myAgent.sendMessage(ptRdvMsg);
            
            // Réception de la liste validation
            MessageTemplate returnLieuTemp = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-RETOUR-LIEU-RDV"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
            
            ACLMessage returnLieu = myAgent.blockingReceive(returnLieuTemp, 3000);
            
            if (returnLieu != null && returnLieu.getContent().equals("true")) {
        		//System.out.println("on est au pt de rdv");
            	this.exitValue = GlobalBehaviour.TO_FIN_EXPLO;
                this.finished = true;
                return;
            }
            
            if (returnLieu != null && returnLieu.getContent().equals("false")) {
            	String goal = myAgent.getShortestPath().get(myAgent.getShortestPath().size() - 1);
            	myAgent.setGoalNode(goal);
            	
            	List<String> all_nodes = new ArrayList<>();
                for (org.graphstream.graph.Node node : this.myMap.getGraph()) {
                    all_nodes.add(node.getId());
                }

                //System.out.println("nodes fin explo : " + all_nodes);
                List<String> chemin = null;
                if(!all_nodes.isEmpty()) {
                	while(chemin == null) {
	                    Random rand = new Random();
	                    String random_noeud = all_nodes.get(rand.nextInt(all_nodes.size()));
	
	                    chemin = this.myMap.getShortestPath(myAgent.getCurrentPosition().getLocationId(), random_noeud);
	                    if(chemin != null) {
	                    	myAgent.setShortestPath(chemin);
	                    	myAgent.setPreviousMode("CartePleine");
	                    	myAgent.setMode("blocking");
	                    	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
	                        this.finished = true;
	                        return;
	                    }
                	}
                }
            }
            
            return;
             
        }
        
 
        
        // si c'est un autre agent qui le bloque
        String otherAgent = myAgent.getBlockingAgent();
        System.out.println(myAgent.getLocalName() + " a comme bloquant " + otherAgent + " car pos pb : " + myAgent.getNoeudBloque());
        if (otherAgent != null) {
        	
        	// on envoie une demande d'interblocage à l'agent qui nous bloque
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
	
	            ACLMessage pong = myAgent.blockingReceive(pongTemplate, 1000);

	            
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
            List<String> avoidPath = myAgent.getPathToAvoid();

            // si c'est le tanker, il n'a pas la priorité et doit bouger
        	if(myAgent.getLocalName().equals(myAgent.getAgentSilo())) {
            	myAgent.setGoalNode(myAgent.getPosSilo());
            	
                String currentNode = myPosition.getLocationId();
                
                System.out.println("avoid : " + avoidPath);

                path_found = myMap.findSafeNodeToMove(currentNode, avoidPath, nodesToAvoid2);

                if (path_found != null && !path_found.isEmpty()) {
                    myAgent.setShortestPath(path_found);
                    myAgent.setDecalage(false);
                    myAgent.setHasNewPath("hasNewPath");
                    System.out.println(myAgent.getLocalName() + " se décale vers : " + path_found);
                    
                    myAgent.setHasNewPath("hasNewPath");
                    myAgent.setMode("blocking");
                    this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                    this.finished = true;
                    return;
                } else {
                	System.out.println("aucun noeud sûr trouvé pour se décaler.");
                    myAgent.setHasNewPath("hasNotNewPath");
                }
            	
        	} else {
        		myAgent.setGoalNode(myAgent.getListObjectifs().get(myAgent.getLocalName()));
        	}
        	
        	// si le membre blocant n'est pas le silo, alors on voit qui a la priorité
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

                myAgent.setTransmissionInterblocage(false);
                myAgent.setHasNewPath("hasNotNewPath");

            } else {
            	if(myAgent.getDecalage()) {
                	System.out.println(myAgent.getLocalName() + " FORCÉ de se décaler à cause d'une demande explicite.");
            	}

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

                    myAgent.setTransmissionInterblocage(false);
	            	myAgent.setDecalage(false);
                    myAgent.setHasNewPath("hasNewPath");

                } else {
                	
                	// on tente d'aller sur un noeud ne gênant pas l'autre agent
                    String currentNode = myPosition.getLocationId();
                    path_found = myMap.findSafeNodeToMove(currentNode, avoidPath, nodesToAvoid2);

                    if (path_found != null && !path_found.isEmpty()) {
                        myAgent.setShortestPath(path_found);
                        myAgent.setDecalage(false);
                        myAgent.setHasNewPath("hasNewPath");
                        myAgent.setMode("blocking");

                    }
                }
            }
            
            if (path_found != null && !path_found.isEmpty()) {
            	myAgent.setHasNewPath("hasNewPath");
                myAgent.setShortestPath(path_found);
                this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
                this.finished = true;
                
            } else {
            	System.out.println(this.myAgent.getLocalName() + " n'a trouvé aucun noeud sûr pour se décaler.");
        		myAgent.setDecalage(false);
        		myAgent.setTransmissionInterblocage(false);
        		myAgent.setPriorityNow(true);
                myAgent.setHasNewPath("hasNotNewPath");
            }
            
            // si l'agent n'a pas réussi à trouver de nouveau chemin,alors il demande à l'autre de se décaler
            if ("hasNotNewPath".equals(myAgent.getHasNewPath())) {
            	System.out.println(myAgent.getLocalName() + " envoie une demande de décalage à " + myAgent.getBlockingAgent());
	            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	            msg.setProtocol("DEMANDE-DECALAGE");
	            msg.addReceiver(new AID(otherAgent, AID.ISLOCALNAME));
	            msg.setContent(myAgent.getHasNewPath());
	            msg.setSender(myAgent.getAID());
	            myAgent.sendMessage(msg);
            }
            
            return;
            
        }
        
        
        // on tente de trouver un autre chemin jusqu'à l'objectif (au cas où golem)
        List<String> newPath = myMap.getAlternativePath(myPosition.getLocationId(), myAgent.getGoalNode(), Collections.singletonList(myAgent.getNoeudBloque()));
        if(newPath != null) {
        	System.out.println("newPath");
        	myAgent.setShortestPath(newPath);
        	this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;
            this.finished = true;
            return;
        }      
        
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
