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

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

public class InterBlocageBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 7567689731436787661L;
	private boolean finished = false;
	private int exitValue = 0;
	
	private Graph g;
	
	public InterBlocageBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
        super(myagent);
	}

	@Override
	public void action() { 
		// on récupère les observations de l'agent bloqué
		List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((GlobalBehaviour)this.getParent()).getLastObservation();
		
		
		// faire directement une condition avec getShortestpath si c'est null alors coincé donc l'autre bouge
		/*
		boolean coince = true;
		for (Couple<Location, List<Couple<Observation, String>>> observation : lobs) {
			for (Couple<Observation, String> o : observation.getRight()) {
				if (o.getLeft() == null) {
					System.out.println("ici : " + this.myAgent.getLocalName());
				}
			}
		}
		*/
		
		
		
		// 1) si qtte trésor ramassée pareil ou égal à 0 --> communication chemin vers objectif
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
