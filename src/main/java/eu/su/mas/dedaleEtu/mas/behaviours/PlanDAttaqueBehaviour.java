package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class PlanDAttaqueBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597689931496787661L;
    private boolean finished = false;
    private int exitValue = 0;
    
    private MapRepresentation myMap;
    
    public PlanDAttaqueBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		System.out.println("Plan d'attaque en marche !");
		
		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
		
		/*for(String a : list_agentNames) {
			
		}*/
		
		// System.out.println("xp : " + ((AbstractDedaleAgent) this.myAgent).getMyExpertise());
		
		
		// l'agent avec la plus grande capacité du trésor en question choisit le plus gros trésor 
		// il vérifie s'il peut l'ouvrir seul : lockingExpertise + Strengh et s'il peut alors il y va seul
		// sinon, il génère plusieurs coalitions possibles en s'assurant que les autres n'ont pas un backpack de capacité nulle pour le trésor en question
		// il choisit le groupe d'agents qui évite le plus la perte des 10% (donc j'imagine on veut le moins de personnes possibles)
		
		// ensuite, une fois les trésors attribués aux agents, on informe le silot des trésors sélectionnés
		// il calcule ensuite un nouveau barycentre en fonction des trésors sélectionnés
		
		
		
		// on verra la partie de l'adaptation de la place du silot où l'un des coffres est vide plus tard

	}

	@Override
	public boolean done() {
		return finished;
	}
	
	@Override
    public int onEnd() {
        return this.exitValue;
    }

}
