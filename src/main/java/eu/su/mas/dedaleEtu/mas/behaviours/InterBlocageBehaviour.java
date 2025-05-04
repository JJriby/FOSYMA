package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.*;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

public class InterBlocageBehaviour extends Behaviour {

    private static final long serialVersionUID = 1L;
    private boolean finished = false;
    private int exitValue = 0;
    private List<String> nodesToAvoid = new ArrayList<>();

    private MapRepresentation myMap;

    public InterBlocageBehaviour(final ExploreCoopAgent2 myAgent) {
        super(myAgent);
    }

    @Override
    public void action() {
        this.finished = false;
        this.exitValue = 0;
        ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
        this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
        
        System.out.println(myAgent.getLocalName() + " en blocage ");

        Location myPosition = ((AbstractDedaleAgent) myAgent).getCurrentPosition();
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();

        // 1. Collect nodes to avoid (occupied by other agents)
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location loc = obs.getLeft();
            for (Couple<Observation, String> o : obs.getRight()) {
                if (o.getLeft().equals(Observation.AGENTNAME)) {
                    nodesToAvoid.add(loc.getLocationId());
                }
            }
        }

        // 2. Try normal open node path avoiding occupied nodes
        List<String> path = myMap.getShortestPathToClosestOpenNode2(myPosition.getLocationId(), nodesToAvoid);
        if (path != null && !path.isEmpty()) {
            myAgent.setShortestPath(path);
            //myAgent.setTypeMsg(1);
            this.exitValue = 1;
            this.finished = true;
            return;
        }

        // 3. If no normal path, apply advanced interblockage resolution
        // Compare with other agent's info (mocked logic for now)
        String otherAgent = myAgent.getBlockingAgent();
        if (otherAgent != null) {
        	
        	System.out.println("Tiramisu, moi : " + myAgent.getLocalName() + " le pb : " + otherAgent);
            int myTreasure = myAgent.getCollectedTreasureValue();
            int otherTreasure = myAgent.getKnownTreasureValue(otherAgent);
            int myCounter = myAgent.getEquityCounter();
            int otherCounter = myAgent.getKnownEquityCounter(otherAgent);

            boolean iHavePriority = false;
            if (myTreasure > 1.5 * otherTreasure) {
                iHavePriority = true;
            } else if (myTreasure == otherTreasure) {
                iHavePriority = myCounter <= otherCounter;
            }

            if (iHavePriority) {
                myAgent.incrementEquityCounter();
                System.out.println("Chemin inchangé : " + myAgent.getShortestPath());
                // Keep my path
                this.exitValue = 1;
                this.finished = true;
                return;
            } else {
                // Try to find alternate DFS path avoiding other's path
                List<String> avoidPath = myAgent.getPathToAvoid();
                String goal = myAgent.getGoalNode();
                List<String> newPath = myMap.getAlternativePath(myPosition.getLocationId(), goal, avoidPath);

                if (newPath != null && !newPath.isEmpty()) {
                    myAgent.setShortestPath(newPath);
                    System.out.println("nouveau chemin court : " + myAgent.getShortestPath());
                    //myAgent.setTypeMsg(1);
                    this.exitValue = 1;
                    this.finished = true;
                    return;
                } else {
                    // No alternative, move far away
                    List<String> farNodes = myMap.getFarAwayOpenNodes(myPosition.getLocationId());
                    if (!farNodes.isEmpty()) {
                        String randomFar = farNodes.get(new Random().nextInt(farNodes.size()));
                        List<String> escape = myMap.getShortestPath(myPosition.getLocationId(), randomFar);
                        if (escape != null && !escape.isEmpty()) {
                            myAgent.setShortestPath(escape);
                            System.out.println("nouveau chemin alternative : " + myAgent.getShortestPath());
                            //myAgent.setTypeMsg(1);
                            this.exitValue = 1;
                            this.finished = true;
                            return;
                        }
                    }
                }
            }
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

        if (!freeNeighbors.isEmpty()) {
            String randomMove = freeNeighbors.get(new Random().nextInt(freeNeighbors.size()));
            myAgent.setShortestPath(List.of(randomMove));
            //myAgent.setTypeMsg(1);
            this.exitValue = 1;
            this.finished = true;
        } else {
            block(500);
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
