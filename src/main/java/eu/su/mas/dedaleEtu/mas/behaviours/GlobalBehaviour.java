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
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.FSMBehaviour;

public class GlobalBehaviour extends FSMBehaviour {
	
	private static final long serialVersionUID = 1L;
	private MapRepresentation myMap;
	private List<String> agentNames;
	
	private static final String Explore = "Explore";
	private static final String GoToRDV = "GoToRDV";
	private static final String PlanDAttaque = "PlanDAttaque";
	private static final String Collect = "Collect";
	private static final String InterBlocage = "InterBlocage";
	private static final String Ping = "Ping";
	private static final String Pong = "Pong";
	private static final String ShareMap = "ShareMap";

	
	public GlobalBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		
		this.myMap = myMap;
		
		// comportements
        this.registerFirstState(new ExploCoopBehaviour2((ExploreCoopAgent2) this.myAgent), Explore);
        this.registerState(new InterBlocageBehaviour((ExploreCoopAgent2) this.myAgent), InterBlocage);         
        this.registerState(new PingBehaviour((ExploreCoopAgent2) this.myAgent), Ping);
        this.registerState(new PongBehaviour((ExploreCoopAgent2) this.myAgent), Pong);
        this.registerState(new ShareMapBehaviour3((ExploreCoopAgent2) this.myAgent), ShareMap);
        
        this.registerState(new GoToRdvBehaviour((ExploreCoopAgent2) this.myAgent), GoToRDV);
        
        this.registerState(new CollectBehaviour((ExploreCoopAgent2) this.myAgent), Collect);
        this.registerLastState(new PlanDAttaqueBehaviour((ExploreCoopAgent2) this.myAgent), PlanDAttaque);
        
       
        // transitions
        this.registerTransition(Explore, GoToRDV, 1);
        this.registerTransition(GoToRDV, PlanDAttaque, 2);
        
        this.registerTransition(Explore, InterBlocage, 2);
        this.registerTransition(InterBlocage, GoToRDV, 1);
        this.registerTransition(GoToRDV, Explore, 1);
        
        this.registerTransition(Explore, Ping, 3);
        this.registerTransition(Ping, Explore, 0);
        this.registerTransition(Ping, ShareMap, 1);
        this.registerTransition(ShareMap, Explore, 0);
        
        this.registerTransition(Explore, Pong, 4);
        this.registerTransition(Pong, Explore, 0);
       
	}
	
	public MapRepresentation getMyMap() {
        return this.myMap;
    }
	
	public void setMyMap(MapRepresentation myMap) {
        this.myMap = myMap;
    }
}
