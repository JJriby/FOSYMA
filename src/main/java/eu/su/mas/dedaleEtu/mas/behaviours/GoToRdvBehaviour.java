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
	private int exitValue = -1;
	private int cpt = 0;
		
	public GoToRdvBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
	}
	
	@Override
	public void action() {
		
		this.finished = false;
		this.exitValue = -1;
		
		// traiter un cas d'erreur où shortestPath serait égal à null ?
		
		// vérifier si lors de ce chemin aussi y a pas interblocage ?
		
		
		ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
		List<String> shortestPath = myAgent.getShortestPath(); 
		
		if(cpt < shortestPath.size()) {
			boolean moved = ((AbstractDedaleAgent) this.myAgent).moveTo(new GsLocation(shortestPath.get(cpt)));
			
			// dans le cas où on doit s'arrêter avant obj car embouteillage 
			// pour le rdv après exploration faudra faire une condition au cas où 
			// y en a un qu'est coincé alors qu'il pense être au rdv
			if(!moved) {
				this.cpt = 0;
				this.exitValue = myAgent.getTypeMsg();
				this.finished = true;
				return;
			} else {
				cpt++;
			}
		}
		else {
			this.cpt = 0;
			myAgent.setShortestPath(new ArrayList<>());
			this.exitValue = myAgent.getTypeMsg();
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
