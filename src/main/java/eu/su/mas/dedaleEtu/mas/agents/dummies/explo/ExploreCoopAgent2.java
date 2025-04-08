package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
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
	private List<String> list_agentNames = new ArrayList<String>();
	
	// params à transmettre aux comportements
	
	private Map<String, Map<Observation, String>> list_gold = new HashMap<>();
	private Map<String, Map<Observation, String>> list_diamond = new HashMap<>();
	private List<String> shortestPath = new ArrayList<>();
	private int type_msg = -1;
	private int msg_retour = -1;
	private String receiverName = "";
	private SerializableSimpleGraph<String, MapAttribute> mapToSend = new SerializableSimpleGraph<>();
	private Map<String,SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit = new HashMap<>();
	// voir si c'est vraiment utile ces deux trucs mtn qu'on est en fsm
	private Set<String> alreadyExchanged = new HashSet<>(); 
	private Set<String> currentlyExchanging = new HashSet<>();
	
	private Map<String,Observation> list_treasure_type = new HashMap<>();
	private Map<String, Set<Couple<Observation, Integer>>> list_expertise = new HashMap<>();
	private Map<String, List<Couple<Observation,Integer>>> list_back_free_space = new HashMap<>();
	private Map<String,Boolean> list_validation = new HashMap<>();
	
	//private List<Couple<Location, List<Couple<Observation, String>>>> lastObs = new ArrayList<>();
	//private List<String> objectif = new ArrayList<>();
	

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
		
		//list_agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				this.list_agentNames.add((String)args[i]);
				i++;
			}
		}
		
		// on initialise les listes avec les noms des agents pour les remplir ensuite
		/*this.list_treasure_type.put(this.getLocalName(), null);
		this.list_expertise.put(this.getLocalName(), new HashSet<>());
		this.list_back_free_space.put(this.getLocalName(), new ArrayList<>());
		*/
		
		this.list_validation.put(this.getLocalName(),  false);
		
		for(String a : this.list_agentNames) {
			/*this.list_treasure_type.put(a, null);
			this.list_expertise.put(a, new HashSet<>());
			this.list_back_free_space.put(a, new ArrayList<>());
			*/
			this.list_validation.put(a, false);
		}
		

		List<Behaviour> lb=new ArrayList<Behaviour>();
		
		/************************************************
		 * 
		 * ADD the behaviours of the Dummy Moving Agent
		 * 
		 ************************************************/
		
		//lb.add(new ExploCoopBehaviour2(this,this.myMap,list_agentNames));

		lb.add(new GlobalBehaviour(this, this.myMap));
		
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */
		
		
		addBehaviour(new StartMyBehaviours(this,lb));
		
		System.out.println("the  agent "+this.getLocalName()+ " is started");

	}
	
	
	public List<String> getAgentNames(){
		return this.list_agentNames;
	}
	
	public Map<String, Map<Observation, String>> getListGold(){
		return this.list_gold;
	}
	
	public Map<String, Map<Observation, String>> getListDiamond(){
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
	
	public int getMsgRetour() {
		return this.msg_retour;
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
	
	public Map<String,Observation> getListTreasureType(){
		return this.list_treasure_type;
	}
	
	public Map<String, Set<Couple<Observation, Integer>>> getListExpertise(){
		return this.list_expertise;
	}
	
	public Map<String, List<Couple<Observation,Integer>>> getListBackFreeSpace(){
		return this.list_back_free_space;
	}
	
	public Map<String, Boolean> getListValidation(){
		return this.list_validation;
	}
	
	/*public List<Couple<Location, List<Couple<Observation, String>>>> getLastObservation(){
		return this.lastObs;
	}
	
	public List<String> getObjectif(){
		return this.objectif;
	}*/
	
	
	
	public void setAgentNames(List<String> agentNames) {
		this.list_agentNames = agentNames;
	}
	
	public void setGold(Map<String, Map<Observation, String>> list_gold) {
		this.list_gold = list_gold;
	}
	
	public void setDiamond(Map<String, Map<Observation, String>> list_diamond) {
		this.list_diamond = list_diamond;
	}
	
	public void setShortestPath(List<String> shortestPath) {
		this.shortestPath = shortestPath;
	}
	
	public void setTypeMsg(int type_msg) {
		this.type_msg = type_msg;
	}
	
	public void setMsgRetour(int msg_retour) {
		this.msg_retour = msg_retour;
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
	
	public void setListTreasureType(Map<String,Observation> list_treasure_type) {
		this.list_treasure_type = list_treasure_type;
	}
	
	public void setListExpertise(Map<String, Set<Couple<Observation, Integer>>> list_expertise) {
		this.list_expertise = list_expertise;
	}
	
	public void setListBackFreeSpace(Map<String, List<Couple<Observation,Integer>>> list_back_free_space) {
		this.list_back_free_space = list_back_free_space;
	}
	
	public void setListValidation(Map<String,Boolean> list_validation) {
		this.list_validation = list_validation;
	}
	
	/*public void setLastObservation(List<Couple<Location, List<Couple<Observation, String>>>> lastObs) {
		this.lastObs = lastObs;
	}
	
	public void setObjectif(List<String> objectif) {
		this.objectif = objectif;
	}*/
	
	
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
