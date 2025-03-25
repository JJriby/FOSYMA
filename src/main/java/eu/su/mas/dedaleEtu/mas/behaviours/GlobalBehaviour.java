package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.FSMBehaviour;

public class GlobalBehaviour extends FSMBehaviour {
	
	private static final String Explore = "Explore";
	private static final String GoToRDV = "GoToRDV";
	private static final String Collect = "Collect";
	
	private List<String> shortestPath;
	private GoToRdvBehaviour goToRdvBehaviour;
	private CollectBehaviour collectBehaviour;
	
	private MapRepresentation myMap;
	private List<String> agentNames;
	private Map<String, List<Integer>> list_gold;
	private Map<String, List<Integer>> list_diamond;

	
	public GlobalBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<String> agentNames) {
		super(myagent);
		
		this.myMap = myMap;
		this.agentNames = agentNames;

		this.list_gold = new HashMap<>();
		this.list_diamond = new HashMap<>();
		
		// comportements
        this.registerFirstState(new ExploCoopBehaviour2(myagent, myMap, agentNames, list_gold, list_diamond), Explore);
        this.goToRdvBehaviour = new GoToRdvBehaviour(myagent, null);
        this.registerState(goToRdvBehaviour, GoToRDV);
        
        // tester la technique où on les retire des constructeurs et on fait juste des fonctions
        this.collectBehaviour = new CollectBehaviour(myagent, myMap, agentNames, list_gold, list_diamond);
        this.registerLastState(collectBehaviour, Collect);

        // transitions
        this.registerTransition(Explore, GoToRDV, 1); // quand exploration finie
        this.registerTransition(GoToRDV, Collect, 1);
	}
	
	public void setShortestPath(List<String> path) {
	    this.shortestPath = path;
	    this.goToRdvBehaviour.setPath(path);
	}

}
