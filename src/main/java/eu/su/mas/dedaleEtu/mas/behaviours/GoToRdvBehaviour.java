package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.env.gs.GsLocation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.SimpleBehaviour;

public class GoToRdvBehaviour extends SimpleBehaviour {
	
	private static final long serialVersionUID = 7567689731496787661L;
	private boolean finished = false;
	
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
			finished = true;
		}

	}

	@Override
	public boolean done() {
		return finished;
	}

}
