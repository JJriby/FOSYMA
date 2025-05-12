package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveMapBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 13L;
    private boolean finished = false;
    private int exitValue;
    
    private MapRepresentation myMap;
    
    Set<String> currentlyExchanging;
    String receiverName;

    public ReceiveMapBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
    	ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
    	
    	this.receiverName = myAgent.getReceiverName();
        SerializableSimpleGraph<String, MapAttribute> mapToSend = myAgent.getMapToSend();
        //MapRepresentation myMap = myAgent.getMyMap();
        Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit = myAgent.getNodesToTransmit();
        Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
        this.currentlyExchanging = myAgent.getCurrentlyExchanging();
        Map<String, Map<Observation, String>> list_gold = myAgent.getListGold();
        Map<String, Map<Observation, String>> list_diamond = myAgent.getListDiamond();
        
        this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
        
        //System.out.println(myAgent.getLocalName() + " est envoyé dans receive map par " + this.receiverName + " avec en retour : " + myAgent.getMsgRetour() + " et en maj : " + myAgent.getTypeMsg());
		
		// Réception de la carte et des trésors
        MessageTemplate returnMapTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-MAP"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnMap = myAgent.blockingReceive(returnMapTemp, 3000);
        
        if (returnMap != null) {
            try {  	
            	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>> received = 
                        (Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>>>) returnMap.getContentObject();
                	
                SerializableSimpleGraph<String, MapAttribute> receivedMap = received.getLeft();

                myMap.mergeMap(receivedMap);
                //System.out.println(myAgent.getLocalName() + " a reçu et fusionné la carte de " + receiverName);
                
                nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());
                
                // fusion des cartes de trésors 
                Couple<Map<String, Map<Observation, String>>,Map<String, Map<Observation, String>>> tresors = received.getRight();
                Map<String, Map<Observation, String>> golds = tresors.getLeft();
                Map<String, Map<Observation, String>> diamonds = tresors.getRight();
                
                
                // voir pour potentiellement faire le putIfAbsent
                for(Map.Entry<String, Map<Observation, String>> g : golds.entrySet()) {
                	String g_key = g.getKey();
                	Map<Observation, String> g_value = g.getValue();
                	list_gold.putIfAbsent(g_key, g_value);
                }
                
                for(Map.Entry<String, Map<Observation, String>> d : diamonds.entrySet()) {
                	String d_key = d.getKey();
                	Map<Observation, String> d_value = d.getValue();
                	list_diamond.putIfAbsent(d_key, d_value);
                }
                
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            
            //alreadyExchanged.add(receiverName);
            //System.out.println("Reception : " + this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
            
            if(myAgent.getSent()) {
            	myAgent.setSent(false);
            	this.exitValue = myAgent.getMsgRetour();
            	
            	if (!this.myMap.hasOpenNode() && myAgent.getMode().equals("explo")) {
        	    	//System.out.println(myAgent.getLocalName() + " est à " + myAgent.getCurrentPosition() + " et a comme fin : " + nodesToTransmit);
        	        
        	        System.out.println(this.myAgent.getLocalName() + " - Exploration terminée !");
        	        myAgent.getListFinExplo().put(myAgent.getLocalName(), true);
        	         
        	        Set<String> treasureNodes = new HashSet<>();
        	        treasureNodes.addAll(list_gold.keySet());
        	        treasureNodes.addAll(list_diamond.keySet());
        	        String obj = myMap.calculBarycentre(treasureNodes);
        	        System.out.println("RDV : "+ obj + " trésors : " + treasureNodes);
        	
        	        List<String> shortestPath = myMap.getShortestPath(((AbstractDedaleAgent) myAgent).getCurrentPosition().getLocationId(), obj);
        	        myAgent.setShortestPath(shortestPath);
        	        
        	        myAgent.setMode("CartePleine");
        	
        	        myAgent.setTypeMsg(GlobalBehaviour.TO_FIN_EXPLO);  // fin d'exploration
        	        alreadyExchanged.clear();
        	        
        	        //System.out.println("[OBJ] " + myAgent.getLocalName() + " est à " + myAgent.getCurrentPosition().getLocationId() + " et doit parcourir : " + myAgent.getShortestPath());
        	    	
        	        this.exitValue = GlobalBehaviour.TO_GO_TO_RDV;

                }
            	
            } else {
                myAgent.setReceived(true);
                //myAgent.setTypeMsg(GlobalBehaviour.TO_SHARE_MAP);
            	this.exitValue = GlobalBehaviour.TO_SHARE_MAP;
            }
            
        } else {
            //System.out.println(myAgent.getLocalName() + " n’a pas reçu de carte et de trésors de " + receiverName);
            myAgent.setSent(false);
            myAgent.setReceived(false);
            this.exitValue = myAgent.getMsgRetour();
        }
        
        this.finished = true;
	}

	@Override
    public boolean done() {
		/*if (currentlyExchanging != null && receiverName != null) {
		    currentlyExchanging.remove(receiverName);
		}*/
		return finished;
    }
    
    
    @Override
    public int onEnd() {
        return this.exitValue;
    }

}
