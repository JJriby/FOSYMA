package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

public class PlanDAttaqueBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597689931496787661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    public PlanDAttaqueBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    
	    // on récupère les caractéristiques des trésors
	    Map<String, Map<Observation, String>> list_gold = myAgent.getListGold();
		Map<String, Map<Observation, String>> list_diamond = myAgent.getListDiamond();
				
		this.myMap = ((GlobalBehaviour) this.getParent()).getMyMap();
		
		// on récupère les caratéristiques de l'agent
		myAgent.getListTreasureType().put(myAgent.getLocalName(), ((AbstractDedaleAgent) myAgent).getMyTreasureType());
		myAgent.getListExpertise().put(myAgent.getLocalName(), ((AbstractDedaleAgent) myAgent).getMyExpertise());
		myAgent.getListBackFreeSpace().put(myAgent.getLocalName(), ((AbstractDedaleAgent) myAgent).getBackPackFreeSpace());
		
		
		
        List<Couple<Location, List<Couple<Observation, String>>>> lobs = ((AbstractDedaleAgent) myAgent).observe();	
        for (Couple<Location, List<Couple<Observation, String>>> obs : lobs) {
            Location accessibleNode = obs.getLeft();
            List<Couple<Observation, String>> details = obs.getRight();
		
            for (Couple<Observation, String> detail : details) {
            	if (detail.getLeft() == Observation.AGENTNAME) {
                    String agentName = detail.getRight();
                    
                    if(!myAgent.getCurrentlyExchanging().contains(agentName)) {
                    
	                    myAgent.setMsgRetour(2);
	                    myAgent.setReceiverName(agentName);
	                    
	                    myAgent.getCurrentlyExchanging().add(agentName);
	                    
	                    //System.out.println("communication " + myAgent.getLocalName());
	                    
	                    if (myAgent.getLocalName().compareTo(agentName) < 0) {
	                    	// si ta case du this.list_validation n'est pas à true, sinon t'as rien à demander (faire des contains)
	                        myAgent.setTypeMsg(3); // PING initiateur
	                        this.exitValue = 3;
	                        
	                    } else {
	                    	// si this.list_validation y a pas que des true
	                        this.exitValue = 4; // PONG récepteur
	                    }
	
	                    this.finished = true;
	                    return;
                    }
                    
                    
                    
            	}
            }
        }
        
        // s'il reçoit une liste de validation différente de la sienne alors il transmet à tout le monde sauf à l'agent qui lui a transmis la nouvelle info
		
		
		
		// l'agent avec la plus grande capacité du trésor en question choisit le plus gros trésor 
		// il vérifie s'il peut l'ouvrir seul : lockingExpertise + Strengh et s'il peut alors il y va seul
		// sinon, il génère plusieurs coalitions possibles en s'assurant que les autres n'ont pas un backpack de capacité nulle pour le trésor en question
		// il choisit le groupe d'agents qui évite le plus la perte des 10% (donc j'imagine on veut le moins de personnes possibles)
		
		// ensuite, une fois les trésors attribués aux agents, on informe le silot des trésors sélectionnés
		// il calcule ensuite un nouveau barycentre en fonction des trésors sélectionnés
		
		
		
		// on verra la partie de l'adaptation de la place du silot où l'un des coffres est vide plus tard

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
