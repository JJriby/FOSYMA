package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class ShareObjectifsBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 6593689931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    public ShareObjectifsBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		this.finished = false;
		this.exitValue = -1;
		
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    List<String> agentNames = myAgent.getAgentNames();
	    String receiverName = myAgent.getReceiverName();
	    
	    Map<String,String> list_obj = myAgent.getListObjectifs();
	    String pos_silo = myAgent.getPosSilo();
	    
	    // à voir pour plutôt utiliser ça 
	    Set<String> alreadyExchanged = myAgent.getAlreadyExchanged();
	    
		
		
	    // envoi de la liste des objectifs et de la future position du silo
		try {
        	
			Couple<String,String> silo = new Couple<>(myAgent.getAgentSilo(), myAgent.getPosSilo());
			Couple<Map<String,String>,Couple<String,String>> a_envoyer = new Couple<>(list_obj, silo);
			
            ACLMessage objMsg = new ACLMessage(ACLMessage.INFORM);
            objMsg.setProtocol("SHARE-OBJECTIFS");
            objMsg.setSender(myAgent.getAID());
            objMsg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
            objMsg.setContentObject(a_envoyer);
            myAgent.sendMessage(objMsg);
            //System.out.println(myAgent.getLocalName() + " objectifs envoyés à " + receiverName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		this.exitValue = myAgent.getMsgRetour();
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
