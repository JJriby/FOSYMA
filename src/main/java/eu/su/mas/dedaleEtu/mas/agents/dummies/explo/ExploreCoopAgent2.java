package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.platformManagment.*;

import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour2;
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.Behaviour;

/**
 * <pre>
 * ExploreCoop agent. 
 * Basic example of how to "collaboratively" explore the map
 *  - It explore the map using a DFS algorithm and blindly tries to share the topology with the agents within reach.
 *  - The shortestPath computation is not optimized
 *  - Agents do not coordinate themselves on the node(s) to visit, thus progressively creating a single file. It's bad.
 *  - The agent sends all its map, periodically, forever. Its bad x3.
 *  - You should give him the list of agents'name to send its map to in parameter when creating the agent.
 *   Object [] entityParameters={"Name1","Name2};
 *   ag=createNewDedaleAgent(c, agentName, ExploreCoopAgent.class.getName(), entityParameters);
 *  
 * It stops when all nodes have been visited.
 * 
 * 
 *  </pre>
 *  
 * @author hc
 *
 */


public class ExploreCoopAgent2 extends AbstractDedaleAgent {

	private static final long serialVersionUID = -7969469610241668140L;
	private MapRepresentation myMap;
	private List<String> list_agentNames;
	
	// params à transmettre aux comportements
	
	private Map<String, List<Integer>> list_gold = new HashMap<>();
	private Map<String, List<Integer>> list_diamond = new HashMap<>();
	private List<String> shortestPath = new ArrayList<>();
	private int type_msg = -1;
	private String receiverName = "";
	private SerializableSimpleGraph<String, MapAttribute> mapToSend = new SerializableSimpleGraph<>();
	private Map<String,SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit = new HashMap<>();
	// voir si c'est vraiment utile ces deux trucs mtn qu'on est en fsm
	private Set<String> alreadyExchanged = new HashSet<>(); 
	private Set<String> currentlyExchanging = new HashSet<>();
	

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		//get the parameters added to the agent at creation (if any)
		final Object[] args = getArguments();
		
		list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				list_agentNames.add((String)args[i]);
				i++;
			}
		}

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		
		//lb.add(new ExploCoopBehaviour2(this,this.myMap,list_agentNames));

		lb.add(new GlobalBehaviour(this, this.myMap, list_agentNames));
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new StartMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	// pas utile vu qu'on les passe en paramètre finalement (à changer)
	/*public MapRepresentation getMyMap() {
		return this.myMap;
	}*/
	
	// pas utile vu qu'on les passe en paramètre finalement (à changer)
	public List<String> getAgentNames(){
		return this.list_agentNames;
	}
	
	public Map<String, List<Integer>> getListGold(){
		return this.list_gold;
	}
	
	public Map<String, List<Integer>> getListDiamond(){
		return this.list_diamond;
	}
	
	public Set<String> getAlreadyExchanged(){
		return this.alreadyExchanged;
	}
	
	public Set<String> getCurrentlyExchanging(){
		return this.currentlyExchanging;
	}
		
	public List<String> getShortestPath(){
		return this.shortestPath;
	}
	
	public int getTypeMsg() {
		return this.type_msg;
	}
	
	public String getReceiverName() {
		return this.receiverName;
	}
	
	public SerializableSimpleGraph<String, MapAttribute> getMapToSend(){
		return this.mapToSend;
	}
	
	public Map<String,SerializableSimpleGraph<String, MapAttribute>> getNodesToTransmit(){
		return this.nodesToTransmit;
	}
	
	
	/*public void setMyMap(MapRepresentation myMap) {
		this.myMap = myMap;
	}*/
	
	
	public void setAgentNames(List<String> agentNames) {
		this.list_agentNames = agentNames;
	}
	
	public void setGold(Map<String, List<Integer>> list_gold) {
		this.list_gold = list_gold;
	}
	
	public void setDiamond(Map<String, List<Integer>> list_diamond) {
		this.list_diamond = list_diamond;
	}
	
	public void setShortestPath(List<String> shortestPath) {
		this.shortestPath = shortestPath;
	}
	
	public void setTypeMsg(int type_msg) {
		this.type_msg = type_msg;
	}
	
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}
	
	public void setMapToSend(SerializableSimpleGraph<String, MapAttribute> mapToSend) {
		this.mapToSend = mapToSend;
	}
	
	public void setNodesToTransmit(Map<String,SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit) {
		this.nodesToTransmit = nodesToTransmit;
	}
	
	public void setAlreadyExchanged(Set<String> alreadyExchanged) {
		this.alreadyExchanged = alreadyExchanged;
	}

	public void setCurrentlyExchanging(Set<String> currentlyExchanging) {
		this.currentlyExchanging = currentlyExchanging;
	}
	
	
	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){
		super.takeDown();
	}

	protected void beforeMove(){
		super.beforeMove();
		//System.out.println("I migrate");
	}

	protected void afterMove(){
		super.afterMove();
		//System.out.println("I migrated");
	}

}
