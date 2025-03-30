package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.FSMBehaviour;

public class GlobalBehaviour extends FSMBehaviour {
	
	private static final String Explore = "Explore";
	private static final String GoToRDV = "GoToRDV";
	private static final String PlanDAttaque = "PlanDAttaque";
	private static final String Collect = "Collect";
	private static final String InterBlocage = "InterBlocage";
	
	private List<String> shortestPath;
	private GoToRdvBehaviour goToRdvBehaviour;
	private PlanDAttaqueBehaviour planDAttaqueBehaviour;
	private CollectBehaviour collectBehaviour;
	private InterBlocageBehaviour interBlocageBehaviour;
	
	private MapRepresentation myMap;
	private List<String> agentNames;
	private Map<String, List<Integer>> list_gold;
	private Map<String, List<Integer>> list_diamond;
	
	private List<Couple<Location, List<Couple<Observation, String>>>> lastObservation;

	
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
        this.interBlocageBehaviour = new InterBlocageBehaviour(myagent);
        this.registerState(interBlocageBehaviour, InterBlocage);
        
        // tester la technique où on les retire des constructeurs et on fait juste des fonctions
        this.planDAttaqueBehaviour = new PlanDAttaqueBehaviour(myagent, myMap, agentNames, list_gold, list_diamond);
        this.registerLastState(planDAttaqueBehaviour, PlanDAttaque);

        // transitions
        this.registerTransition(Explore, GoToRDV, 1); // quand exploration finie
        this.registerTransition(GoToRDV, Collect, 1);
        this.registerTransition(Explore, InterBlocage, 2);
        this.registerTransition(InterBlocage, Explore, 2);
	}
	
	public void setShortestPath(List<String> path) {
	    this.shortestPath = path;
	    this.goToRdvBehaviour.setPath(path);
	}
	
	public void setLastObservation(List<Couple<Location, List<Couple<Observation, String>>>> obs) {
	    this.lastObservation = obs;
	}

	public List<Couple<Location, List<Couple<Observation, String>>>> getLastObservation() {
	    return this.lastObservation;
	}

}
