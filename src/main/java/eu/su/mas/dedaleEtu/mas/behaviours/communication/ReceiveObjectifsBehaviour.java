package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.util.List;
import java.util.Map;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveObjectifsBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 6593689931496737661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    public ReceiveObjectifsBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		this.finished = false;
		this.exitValue = -1;
		
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    String receiverName = myAgent.getReceiverName();

		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();

	    // Réception de la liste validation
        MessageTemplate returnObjTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-OBJECTIFS"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
       
        ACLMessage returnObj = myAgent.blockingReceive(returnObjTemp, 3000);
        
        if (returnObj != null) {
            try {
            	
            	Couple<Map<String, String>,String> objectifs_received =
            			(Couple<Map<String, String>,String>) returnObj.getContentObject();
            	
            	myAgent.setListObjectifs(objectifs_received.getLeft());
            	myAgent.setPosSilo(objectifs_received.getRight());
            	    
            	String but = myAgent.getListObjectifs().get(myAgent.getLocalName());
            	myAgent.setShortestPath(myMap.getShortestPath(((AbstractDedaleAgent) myAgent).getCurrentPosition().getLocationId(), but));
            	myAgent.setGoalNode(but);
            	
                System.out.println(this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
        		System.out.println(this.myAgent.getLocalName() + " liste objectifs : " + myAgent.getListObjectifs());
        		System.out.println(this.myAgent.getLocalName() + " a pour objectif : " + but + " et devra déposer en : " + myAgent.getPosSilo());
        	    
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
            
            this.exitValue = myAgent.getMsgRetour();
            
        } else {
            System.out.println(myAgent.getLocalName() + " n’a pas reçu de liste de validation en retour de " + receiverName);
            myAgent.setSent(false);
            myAgent.setReceived(false);
            this.exitValue = myAgent.getMsgRetour();
        }
        
        this.finished = true;
	        
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
