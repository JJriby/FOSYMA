package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class CollectBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597689731496787661L;
    private boolean finished = false;
    private int exitValue = 0;
    
    private MapRepresentation myMap;
    /*private List<String> list_agentNames;
    
    private Map<String, List<String>> list_obj;*/
    
    public CollectBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
        /*this.list_agentNames = agentNames;
        this.list_obj = list_obj;*/
    }

	@Override
	public void action() {
		
		System.out.println("Phase collecte : " + this.myAgent.getLocalName());
		
		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
		
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
