package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.FSMBehaviour;

public class GlobalBehaviour extends FSMBehaviour {
	
	private static final String Explore = "Explore";
	private static final String GoToRDV = "GoToRDV";
	private static final String PlanDAttaque = "PlanDAttaque";
	private static final String Collect = "Collect";
	private static final String InterBlocage = "InterBlocage";
	private static final String Ping = "Ping";
	private static final String Pong = "Pong";
	private static final String ShareMap = "ShareMap";
	
	private List<String> shortestPath;
	private GoToRdvBehaviour goToRdvBehaviour;
	private PlanDAttaqueBehaviour planDAttaqueBehaviour;
	private CollectBehaviour collectBehaviour;
	private InterBlocageBehaviour interBlocageBehaviour;
	private PingBehaviour pingBehaviour;
	private PongBehaviour pongBehaviour;
	private ShareMapBehaviour3 shareMapBehaviour;
	private ExploCoopBehaviour2 exploBehaviour;
	
	private MapRepresentation myMap;
	private List<String> agentNames;
	private Map<String, List<Integer>> list_gold;
	private Map<String, List<Integer>> list_diamond;
	
	private List<Couple<Location, List<Couple<Observation, String>>>> lastObservation;

	
	public GlobalBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
		
		this.myMap = myMap;
		this.agentNames = agentNames;

		this.list_gold = new HashMap<>();
		this.list_diamond = new HashMap<>();
		
		// comportements
		this.exploBehaviour = new ExploCoopBehaviour2(myagent, myMap, agentNames, list_gold, list_diamond);
        this.registerFirstState(this.exploBehaviour, Explore);
        this.goToRdvBehaviour = new GoToRdvBehaviour(myagent, null);
        this.registerState(goToRdvBehaviour, GoToRDV);
        this.interBlocageBehaviour = new InterBlocageBehaviour(myagent, this.myMap);
        this.registerState(interBlocageBehaviour, InterBlocage);
                
        
        // tester la technique où on les retire des constructeurs et on fait juste des fonctions
        this.planDAttaqueBehaviour = new PlanDAttaqueBehaviour(myagent, myMap, agentNames, list_gold, list_diamond);
        this.registerLastState(planDAttaqueBehaviour, PlanDAttaque);

        // transitions
        this.registerTransition(Explore, GoToRDV, 1);
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
	
	public void setShareMapParams(String receiverName, SerializableSimpleGraph<String, MapAttribute> mapToSend, MapRepresentation myMap, Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit, Set<String> alreadyExchanged, Set<String> currentlyExchanging, Map<String, List<Integer>> list_gold, Map<String, List<Integer>> list_diamond) {
		this.deregisterState(ShareMap);
		this.shareMapBehaviour = new ShareMapBehaviour3((AbstractDedaleAgent) this.myAgent, receiverName, mapToSend, myMap, nodesToTransmit, alreadyExchanged, currentlyExchanging, list_gold, list_diamond);
        this.registerState(shareMapBehaviour, ShareMap);
        
        this.registerTransition(Ping, ShareMap, 1);
        this.registerTransition(ShareMap, Explore, 0);
        
	}
	
	public void setPingParams(int type_msg, String receiverName){
		System.out.println("execution");
		this.deregisterState(Ping);
		this.pingBehaviour = new PingBehaviour((AbstractDedaleAgent) this.myAgent, type_msg, receiverName);
		this.registerState(pingBehaviour, Ping);
		
		this.registerTransition(Explore, Ping, 3);
		this.registerTransition(Ping, Explore, 0);
		this.registerTransition(Ping, ShareMap, 1);
	}
	
	public void setPongParams(String receiverName, SerializableSimpleGraph<String, MapAttribute> mapToSend, MapRepresentation myMap, Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit, Set<String> alreadyExchanged, Set<String> currentlyExchanging, Map<String, List<Integer>> list_gold, Map<String, List<Integer>> list_diamond) {
		this.deregisterState(Pong);
		this.pongBehaviour = new PongBehaviour((AbstractDedaleAgent) this.myAgent, receiverName, mapToSend, myMap, nodesToTransmit, alreadyExchanged, currentlyExchanging, list_gold, list_diamond);
        this.registerState(pongBehaviour, Pong);
        
        this.registerTransition(Explore, Pong, 4);
        this.registerTransition(Pong, Explore, 0);
	}
}
