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
    
    public PlanDAttaqueBehaviour(final ExploreCoopAgent2 myagent, MapRepresentation myMap) {
        super(myagent);
        this.myMap = myMap;
    }

	@Override
	public void action() {
		/*for(String a : list_agentNames) {
			
		}*/
		
		System.out.println("xp : " + ((AbstractDedaleAgent) this.myAgent).getMyExpertise());

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
