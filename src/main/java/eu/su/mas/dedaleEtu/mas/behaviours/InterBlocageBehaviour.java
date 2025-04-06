package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

public class InterBlocageBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 7567689731436787661L;
	private boolean finished = false;
	private int exitValue = 0;
	
	private MapRepresentation myMap;
		
	public InterBlocageBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
	}

	@Override
	public void action() { 
				
		this.finished = false;
    	this.exitValue = 0; 
		
    	ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
		
		
		// on stocke tous les noeuds où y a des agents pour les éviter
		List<String> noeudsAEviter = new ArrayList<>();
		List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();
		for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location nodeId = obs.getLeft();
            
            List<Couple<Observation, String>> details = obs.getRight();
            for (Couple<Observation, String> detail : details) {
                if (detail.getLeft() == Observation.AGENTNAME) {
                    noeudsAEviter.add(nodeId.getLocationId());
                }
            }
		}
		
		if (noeudsAEviter.contains(myAgent.getCurrentPosition().getLocationId())) {
			System.out.println("PB");
		}
				
		// on cherche le chemin le plus court menant à un noeud ouvert en évitant les noeuds où se trouvent les agents
		Location myPosition = ((AbstractDedaleAgent) myAgent).getCurrentPosition();
		List<String> shortestPath = this.myMap.getShortestPathToClosestOpenNode2(myPosition.getLocationId(), noeudsAEviter);
		
		
		//System.out.println("chemin : " + shortestPath);
		// si on en a trouvé un, on y va, puis on retourne dans la phase d'exploration
		if(shortestPath != null) {
			myAgent.setShortestPath(shortestPath);
			myAgent.setTypeMsg(1);
			this.finished = true;
			this.exitValue = 1;
			return;
		} 
		
		// dans le cas inverse, on réexécute le comportement jusqu'à ce qu'on trouve un chemin le temps que les agents blocants bougent
		
		
		
		// 1) si qtte trésor ramassée pareille ou égale à 0 --> communication chemin vers objectif
		// 2) l'agent avec chemin le plus petit garde sa route
		// tandis que les autres recalculent un chemin vers leur objectif sans les noeuds nécessaires à l'autre
		// et si un tel chemin n'existe pas on l'envoie dans un noeud random au loin
		// 3) chaque agent a un compteur qu'on incrémente s'il garde son chemin (donc a eu la priorité)
		// lors d'un blocus, on compare d'abord qtte trésor (A1 > beta*A2 alors A1 gagne)
		// puis les compteurs et celui ayant le plus petit garde son chemin (équité)
		
		
		// va falloir faire autrement pour exploration car pas d'objectif concret, 
		// j'imagine comparaison cpt en premier et si égal alors random dans le choix
		
		
		// va aussi falloir gérer les cas où celui qui doit bouger car pas prioritaire
		// est coincé dans un coin donc prendre en compte aussi si celui qui doit bouger PEUT bouger
		
		
		
		// pourquoi pas faire une sorte de liste de cas qui peuvent arriver genre :
		// file indienne
		// celui qui doit bouger par ordre de priorité est coincé dans un coin 
		// 5-6 agents en cercle
		// ... tous les cas de figures improbables et chiants mais possibles
		
		
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
