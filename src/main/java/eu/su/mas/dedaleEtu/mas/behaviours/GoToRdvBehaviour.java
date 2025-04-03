package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;

public class GoToRdvBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 7567689731496787661L;
	private boolean finished = false;
	private int exitValue = 0;
	private int cpt = 0;
	
	private MapRepresentation myMap;
	
	public GoToRdvBehaviour(final ExploreCoopAgent2 myagent, MapRepresentation myMap) {
        super(myagent);
        this.myMap = myMap;
	}
	
	@Override
	public void action() {
		
		// traiter un cas d'erreur où shortestPath serait égal à null ?
		
		ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
		List<String> shortestPath = myAgent.getShortestPath(); 
		
		if(cpt < shortestPath.size()) {
			((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(shortestPath.get(cpt)));
			cpt++;
		}
		else {
			myAgent.setShortestPath(new ArrayList<>());
			this.exitValue = 1;
			this.finished = true;
			return;
		}

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
