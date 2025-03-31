package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareMapBehaviour3 extends Behaviour {

	private static final long serialVersionUID = 12L;
	private String receiverName;
    private SerializableSimpleGraph<String, MapAttribute> mapToSend;
    private MapRepresentation myMap;
    private boolean finished = false;
    private Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit;
    private Set<String> alreadyExchanged;
    private Set<String> currentlyExchanging;
    private int exitValue;
    private Map<String, List<Integer>> list_gold;
    private Map<String, List<Integer>> list_diamond;

    public ShareMapBehaviour3(AbstractDedaleAgent a, String receiverName, SerializableSimpleGraph<String, MapAttribute> mapToSend, MapRepresentation myMap, Map<String, SerializableSimpleGraph<String, MapAttribute>> nodesToTransmit, Set<String> alreadyExchanged, Set<String> currentlyExchanging,     Map<String, List<Integer>> list_gold, Map<String, List<Integer>> list_diamond) {

        super(a);
        this.receiverName = receiverName;
        this.mapToSend = mapToSend;
        this.myMap = myMap;
        this.nodesToTransmit = nodesToTransmit;
        this.alreadyExchanged = alreadyExchanged;
        this.currentlyExchanging = currentlyExchanging;
        this.list_gold = list_gold;
        this.list_diamond = list_diamond;
    }
    


    @Override
    public void action() {
    	    	
    	// 3. Envoi de la carte        
        try {
        	
        	Couple<Map<String, List<Integer>>,Map<String, List<Integer>>> tresors = new Couple<>(this.list_gold, this.list_diamond); 
        	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>> a_envoyer = new Couple<>(mapToSend, tresors);
        	
            ACLMessage mapMsg = new ACLMessage(ACLMessage.INFORM);
            mapMsg.setProtocol("SHARE-NEW-NODES");
            mapMsg.setSender(this.myAgent.getAID());
            mapMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
            mapMsg.setContentObject(a_envoyer);
            ((AbstractDedaleAgent)myAgent).sendMessage(mapMsg);
            System.out.println(myAgent.getLocalName() + " carte et trésors envoyés à " + receiverName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 4. Attente de l’ACK - carte en retour
        MessageTemplate returnMapTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-NEW-NODES-RETURN"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnMap = this.myAgent.blockingReceive(returnMapTemp, 3000);
        
        if (returnMap != null) {
            try {
            	Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>> received = 
                        (Couple<SerializableSimpleGraph<String, MapAttribute>,Couple<Map<String, List<Integer>>,Map<String, List<Integer>>>>) returnMap.getContentObject();
                	
                SerializableSimpleGraph<String, MapAttribute> receivedMap = received.getLeft();
                this.myMap.mergeMap(receivedMap);
                System.out.println(this.myAgent.getLocalName() + " a reçu et fusionné la carte en retour de " + this.receiverName);
                
                this.nodesToTransmit.put(receiverName, new SerializableSimpleGraph<>());
                
                // fusion des cartes de trésors 
                Couple<Map<String, List<Integer>>,Map<String, List<Integer>>> tresors = received.getRight();
                Map<String, List<Integer>> golds = tresors.getLeft();
                Map<String, List<Integer>> diamonds = tresors.getRight();
                
                for(Map.Entry<String, List<Integer>> g : golds.entrySet()) {
                	String g_key = g.getKey();
                	List<Integer> g_value = g.getValue();
                	if(!this.list_gold.containsKey(g_key)) {
                		this.list_gold.put(g_key, g_value);
                	}
                }
                
                for(Map.Entry<String, List<Integer>> d : diamonds.entrySet()) {
                	String d_key = d.getKey();
                	List<Integer> d_value = d.getValue();
                	if(!this.list_diamond.containsKey(d_key)) {
                		this.list_diamond.put(d_key, d_value);
                	}
                }

                /*// Envoi de l’ACK final
                ACLMessage ackMsg = new ACLMessage(ACLMessage.CONFIRM);
                ackMsg.setProtocol("ACK-SHARE");
                ackMsg.setContent("Merci pour ta carte !");
                ackMsg.addReceiver(returnMap.getSender());
                this.myAgent.send(ackMsg);
                System.out.println(this.myAgent.getLocalName() + " ✅ a envoyé un ACK final à " + returnMap.getSender().getLocalName());
				*/
                
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            
            this.alreadyExchanged.add(receiverName);
            System.out.println("PING : " + this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
            
        } else {
            System.out.println(this.myAgent.getLocalName() + " n’a pas reçu de carte en retour de " + receiverName);
        }
        
        this.exitValue = -1;
        this.finished = true;
    }

    @Override
    public boolean done() {
    	this.currentlyExchanging.remove(receiverName);
        return finished;
    }
    
    @Override
    public int onEnd() {
        return this.exitValue;
    }

}
