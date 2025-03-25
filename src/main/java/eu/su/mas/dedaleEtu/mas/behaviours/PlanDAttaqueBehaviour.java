package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class PlanDAttaqueBehaviour extends Behaviour {
	
	private int exitValue = 0;
	private static final long serialVersionUID = 8597689931496787661L;
    private boolean finished = false;
    
    private MapRepresentation myMap;
    private List<String> list_agentNames;
    private Map<String, List<Integer>> list_gold;
    private Map<String, List<Integer>> list_diamond;
    
    private Map<String, String> list_obj;
    
    public PlanDAttaqueBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, Map<String, List<Integer>> list_gold, Map<String, List<Integer>> list_diamond) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.list_gold = list_gold;
        this.list_diamond = list_diamond;
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
