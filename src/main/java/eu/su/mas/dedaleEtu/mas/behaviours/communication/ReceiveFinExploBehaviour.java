package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.util.Map;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveFinExploBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597289931498787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    public ReceiveFinExploBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }
    
	@Override
	public void action() {
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    String receiverName = myAgent.getReceiverName(); 
	    Map<String, Boolean> list_fin_explo = myAgent.getListFinExplo();
	    
	    
	    // Réception de la liste validation
        MessageTemplate returnListeTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-FIN-EXPLO"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnListe = myAgent.blockingReceive(returnListeTemp, 3000);
        
        if (returnListe != null) {
            try {
            	
            	Map<String, Boolean> list2_fin_explo =
            			(Map<String, Boolean>) returnListe.getContentObject();
            	    
        	    // on fusionne la liste de validation
        	    for(Map.Entry<String, Boolean> elt : list2_fin_explo.entrySet()) {
        	    	if (elt.getValue() == true){
        	    		list_fin_explo.put(elt.getKey(), elt.getValue());
        	    	}
        	    } 
        	    
        	    // si l'agent avec qui on vient de communiquer n'a pas fini son explo, on va dans le behaviour shareMap pour lui partager sa map
        	    if(list_fin_explo.get(receiverName) == false) {
        	    	myAgent.setSent(false);
        	    	myAgent.setReceived(true); // comme ça on fera juste l'envoi de map et pas la réception
        	    	this.finished = true;
        	    	this.exitValue = GlobalBehaviour.TO_SHARE_MAP;
        	    	return;
        	    }
        	    
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
            
            //System.out.println(this.myAgent.getLocalName() + " échange terminé avec " + receiverName);
    		//System.out.println(this.myAgent.getLocalName() + " liste fin explo : " + myAgent.getListFinExplo());
    		
    		if(myAgent.getSent()) {
            	myAgent.setSent(false);
            	this.exitValue = myAgent.getMsgRetour();
            } else {
                myAgent.setReceived(true);
                //myAgent.setTypeMsg(GlobalBehaviour.TO_SHARE_FIN_EXPLO);
            	this.exitValue = GlobalBehaviour.TO_SHARE_FIN_EXPLO;
            }
            
        } else {
            //System.out.println(myAgent.getLocalName() + " n’a pas reçu de liste de validation en retour de " + receiverName);
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
