package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.Collections;
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
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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
	private String agent_cible = "";
	private int cpt_ordre = 0;
	private List<String> list_ordre = new ArrayList<>();
	private String parole = "";
	private Map<String, Boolean> list_fin_explo = new HashMap<>();
	
	private boolean sent = false;
	private boolean received = false;
	private int type_msg_init = -1;
	
	private Map<String, String> list_objectifs = new HashMap<>();
	private String pos_silo = "";
	
	private int type_interblocage = -1;
	private String agent_silo = "";
	private Map<Observation, Integer> stockage = new HashMap<>(); 
	private String noeud_bloque = "";
	
	private String mode = "explo";
    private Map<String, Integer> historique_com_map = new HashMap<>();
	
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
		this.list_fin_explo.put(this.getLocalName(),  false);
		
		for(String a : this.list_agentNames) {
			/*this.list_treasure_type.put(a, null);
			this.list_expertise.put(a, new HashSet<>());
			this.list_back_free_space.put(a, new ArrayList<>());
			*/
			this.list_validation.put(a, false);
			this.list_fin_explo.put(a, false);
		}
		
		
		/*this.list_ordre.add(this.getLocalName());
		this.list_ordre.addAll(this.list_agentNames);
		Collections.sort(this.list_ordre);
		
		this.setParole(this.list_ordre.get(0));*/
		
		this.stockage.put(Observation.GOLD, 0);
		this.stockage.put(Observation.DIAMOND, 0);
		

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
	
	public String getAgentCible(){
		return this.agent_cible;
	}
	
	public int getCptOrdre(){
		return this.cpt_ordre;
	}
	
	public List<String> getListOrdre(){
		return this.list_ordre;
	}
	
	public String getParole() {
		return this.parole;
	}
	
	public Map<String, Boolean> getListFinExplo(){
		return this.list_fin_explo;
	}
	
	public Boolean getSent() {
		return this.sent;
	}
	
	public Boolean getReceived() {
		return this.received;
	}
	
	public int getTypeMsgInit() {
		return this.type_msg_init;
	}
	
	public Map<String, String> getListObjectifs(){
		return this.list_objectifs;
	}
	
	public String getPosSilo() {
		return this.pos_silo;
	}
	
	public int getTypeInterblocage() {
		return this.type_interblocage;
	}
	
	public String getAgentSilo() {
		return this.agent_silo;
	}
	
	public Map<Observation,Integer> getStockage(){
		return this.stockage;
	}
	
	public String getNoeudBloque() {
		return this.noeud_bloque;
	}
	
	public String getMode() {
		return this.mode;
	}
	
	public Map<String, Integer> getHistoriqueComMap(){
		return this.historique_com_map;
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
	
	public void setAgentCible(String nom) {
		this.agent_cible = nom;
	}
	
	public void setCptOrdre(int cpt) {
		this.cpt_ordre = cpt;
	}
	
	public void setListOrdre(List<String> list_ordre) {
		this.list_ordre = list_ordre;
	}
	
	public void setParole(String nom) {
		this.parole = nom;
	}
	
	public void setListFinExplo(Map<String,Boolean> list_fin_explo) {
		this.list_fin_explo = list_fin_explo;
	}
	
	public void setSent(boolean sent) {
		this.sent = sent;
	}
	
	public void setReceived(boolean received) {
		this.received = received;
	}
	
	public void setTypeMsgInit(int type_msg_init) {
		this.type_msg_init = type_msg_init;
	}
	
	public void setListObjectifs(Map<String, String> l) {
		this.list_objectifs = l;
	}
	
	public void setPosSilo(String pos) {
		this.pos_silo = pos;
	}
	
	public void setTypeInterblocage(int interbloc) {
		this.type_interblocage = interbloc;
	}
	
	public void setAgentSilo(String nom_a) {
		this.agent_silo = nom_a;
	}
	
	public void setStockage(Map<Observation,Integer> stockage) {
		this.stockage = stockage;
	}
	
	public void setNoeudBloque(String noeud) {
		this.noeud_bloque = noeud;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public void setHistoriqueComMap(Map<String, Integer> history_map) {
		this.historique_com_map = history_map;
	}

	
	/*public void setLastObservation(List<Couple<Location, List<Couple<Observation, String>>>> lastObs) {
		this.lastObs = lastObs;
	}
	
	public void setObjectif(List<String> objectif) {
		this.objectif = objectif;
	}*/
	
	
	
	public boolean checkMessagesInterBlocage() {
		// 1. Réception du Ping
        MessageTemplate pingTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol("PING"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage ping = this.receive(pingTemplate);
        
        if (ping == null) {
            System.out.println(this.getLocalName() + " Pas de PING factice de " + receiverName + "retour : " + this.getMsgRetour());
            return false;
        }
        
        // on se dirige à la réception du partage adéquat
        int val_ping = Integer.parseInt(ping.getContent());
        
        if(val_ping == 20) {
	        this.setTypeMsg(val_ping);
	        
	    	System.out.println("pong factice : " + this.getLocalName() + " msg retour : " + this.getMsgRetour() + " msg autre : " + this.getTypeMsg());
	        
	        // 2. Envoi du Pong
	        ACLMessage pong = ping.createReply();
	        pong.setProtocol("PONG");
	        pong.setSender(this.getAID());
	        //pong.addReceiver(new AID(this.receiverName, AID.ISLOCALNAME));
	        pong.setContent("Je suis bien dispo !");
	        this.sendMessage(pong);
	        System.out.println(this.getLocalName() + " → PONG factice envoyé à " + receiverName);
	        return true;
        } else {
        	return false;
        }
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
	
	
	
	//ADDED FOR INTERBLOCAGE 
	
	private int collectedTreasureValue = 0;

	public int getCollectedTreasureValue() {
	    return this.collectedTreasureValue;
	}

	public void addCollectedTreasure(int amount) {
	    this.collectedTreasureValue += amount;
	}
	
	public void setCollectedTreasureValue() {
		this.collectedTreasureValue = 0;
	}
	
	private int equityCounter = 0;

	public int getEquityCounter() {
	    return this.equityCounter;
	}

	public void incrementEquityCounter() {
	    this.equityCounter++;
	}
	
	private String goalNode = null;

	public String getGoalNode() {
	    return this.goalNode;
	}

	public void setGoalNode(String nodeId) {
	    this.goalNode = nodeId;
	}
	
	private List<String> pathToAvoid = new ArrayList<>();

	public List<String> getPathToAvoid() {
	    return new ArrayList<>(this.pathToAvoid);
	}

	public void setPathToAvoid(List<String> path) {
	    this.pathToAvoid = new ArrayList<>(path);
	}
	
	public List<String> getFarAwayOpenNodes() {
	    return this.myMap.getFarAwayOpenNodes(getCurrentPosition().getLocationId());
	}
	
	private String blockingAgent = null;

	public void setBlockingAgent(String name) {
	    this.blockingAgent = name;
	}

	public String getBlockingAgent() {
	    return this.blockingAgent;
	}
	
	private Map<String, Integer> knownTreasureValues = new HashMap<>();
	private Map<String, Integer> knownEquityCounters = new HashMap<>();
	

	public void updateKnownTreasureValue(String agentName, int value) {
	    knownTreasureValues.put(agentName, value);
	}

	public int getKnownTreasureValue(String agentName) {
	    return knownTreasureValues.getOrDefault(agentName, 0);
	}

	public void updateKnownEquityCounter(String agentName, int counter) {
	    knownEquityCounters.put(agentName, counter);
	}

	public int getKnownEquityCounter(String agentName) {
	    return knownEquityCounters.getOrDefault(agentName, 0);
	}
	

	
	

}
