package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class CollectBehaviour extends Behaviour {
	
	private int exitValue = 0;
	private static final long serialVersionUID = 8597689731496787661L;
    private boolean finished = false;
    
    private MapRepresentation myMap;
    private List<String> list_agentNames;
    private Map<String, List<Integer>> list_gold;
    private Map<String, List<Integer>> list_diamond;
    
    private Map<String, String> list_obj;
    
    public CollectBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames, Map<String, List<Integer>> list_gold, Map<String, List<Integer>> list_diamond) {
        super(myagent);
        this.myMap = myMap;
        this.list_agentNames = agentNames;
        this.list_gold = list_gold;
        this.list_diamond = list_diamond;
        this.list_obj = list_obj;
    }

	@Override
	public void action() {
		/*for(int i=0; i<this.list_agentNames.size(); i++) {
			this.list_obj.put(this.list_agentNames.get(i), this.list_gold.get());
		}*/
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
