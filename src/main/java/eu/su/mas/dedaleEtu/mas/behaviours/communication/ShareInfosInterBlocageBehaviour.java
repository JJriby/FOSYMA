package eu.su.mas.dedaleEtu.mas.behaviours.communication;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.behaviours.GlobalBehaviour;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ShareInfosInterBlocageBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 7567689731496787661L;
	private boolean finished = false;
	private int exitValue = -1;

		
	public ShareInfosInterBlocageBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
	}

	@Override
	public void action() {

		this.finished = false;
		this.exitValue = -1;
		
		ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
		List<String> shortestPath = myAgent.getShortestPath(); 
		
		System.out.println(myAgent.getLocalName() + " est dans shareInfos");
		
		// envoi des infos du parcours de l'agent
		
		// faire la boucle et comparer avec le noeud_bloque, maj blocking agent, et tranmission des autres trucs
		
        Location myPosition = ((AbstractDedaleAgent) myAgent).getCurrentPosition();
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();

        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location loc = obs.getLeft();
            if(loc.getLocationId().equals(myAgent.getNoeudBloque())) {
	            for (Couple<Observation, String> o : obs.getRight()) {
	                if (o.getLeft().equals(Observation.AGENTNAME)) {
	                	String agentName = o.getRight();
	                	myAgent.setBlockingAgent(agentName);
	                	
	                	// transmission des données de l'agent
	                	
	                	int qte_tresor = myAgent.getCollectedTreasureValue();
	                	int cpt_equite = myAgent.getEquityCounter();
	                	List<String> chemin = myAgent.getShortestPath();
	                	
		                Couple<Integer,Integer> infos = new Couple<>(qte_tresor, cpt_equite);
		                Couple<Couple<Integer,Integer>,List<String>> a_envoyer = new Couple<>(infos,chemin);
	                	
	                	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	                	msg.setSender(myAgent.getAID());
	                	msg.setProtocol("SHARE-INFOS-INTERBLOCAGE");
	                	msg.addReceiver(new AID(agentName, AID.ISLOCALNAME));
	                	
	                	try {
	                		msg.setContentObject(a_envoyer);
	                		myAgent.sendMessage(msg); 
	                	} catch (IOException e){
	                		System.out.println("problème lors de l'envoi");
	                	}
	                }
	            }
            }
        }
		
		
		// Réception des infos de l'agent
        MessageTemplate infosTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-INFOS-INTERBLOCAGE"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );
        
        ACLMessage returnInfos = myAgent.blockingReceive(infosTemp, 3000);
        
        if (returnInfos != null) {
            try {
            	
            	// réception et ajout des infos de l'agent bloquant
            	Couple<Couple<Integer,Integer>,List<String>> infos = (Couple<Couple<Integer,Integer>,List<String>>) returnInfos.getContentObject();
            	
            	myAgent.setBlockingAgent(returnInfos.getSender().getLocalName());
            	String nom_agent = myAgent.getBlockingAgent();
            	myAgent.updateKnownTreasureValue(nom_agent, infos.getLeft().getLeft());
            	myAgent.updateKnownEquityCounter(nom_agent, infos.getLeft().getRight());
            	myAgent.setPathToAvoid(infos.getRight());  	
            	
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
                    
        } else {
            System.out.println(myAgent.getLocalName() + " n’a pas reçu d'infos de l'agent " + myAgent.getBlockingAgent());
        }
        
        this.exitValue = GlobalBehaviour.TO_INTERBLOCAGE;
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
