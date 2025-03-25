package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;

public class GoToRdvBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 7567689731496787661L;
	private boolean finished = false;
	private int exitValue = 0;
	
	private List<String> shortestPath; 
	private int cpt = 0;
	
	public GoToRdvBehaviour(final AbstractDedaleAgent myagent, List<String> shortestPath) {
        super(myagent);
        this.shortestPath = shortestPath;
	}
	
	@Override
	public void action() {
		if(cpt < shortestPath.size()) {
			((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(shortestPath.get(cpt)));
			cpt++;
		}
		else {
			((GlobalBehaviour)this.getParent()).setShortestPath(this.shortestPath);
			this.exitValue = 1;
			this.finished = true;
			return;
		}

	}
	
	public void setPath(List<String> path) {
	    this.shortestPath = path;
	    this.cpt = 0; // reset au cas où
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
