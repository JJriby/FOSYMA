package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CollectSiloBehaviour extends Behaviour {
	
	private static final long serialVersionUID = 8597682931496287661L;
    private boolean finished = false;
    private int exitValue = -1;
    
    private MapRepresentation myMap;
    
    private int pour_debugger = 0;
    private String node_priority = "";
        
    
    public CollectSiloBehaviour(final ExploreCoopAgent2 myagent) {
        super(myagent);
    }

	@Override
	public void action() {
		
		// voir pour modifier et faire un pong en cas d'interblocage, à voir

		this.finished = false;
	    this.exitValue = -1;
	    
	    ExploreCoopAgent2 myAgent = (ExploreCoopAgent2) this.myAgent;
	    
	    myAgent.setMsgRetour(GlobalBehaviour.TO_COLLECT_SILO);
	    
	    List<String> agentNames = myAgent.getAgentNames();
	    
	    if (myAgent.checkMessagesInterBlocage()) {
			System.out.println("pong provient de collect silo");
			//myAgent.setMsgRetour(GlobalBehaviour.TO_GO_TO_RDV);
		    this.exitValue = GlobalBehaviour.TO_SHARE_INFOS_INTERBLOCAGE;
		    this.finished = true;
		    return;
		}
	    
	    Map<Observation, Integer> stockage = myAgent.getStockage();

	    
	    // Réception du sac à dos de l'un des agents
        MessageTemplate backPackTemp = MessageTemplate.and(
            MessageTemplate.MatchProtocol("ProtocolTanker"),
            MessageTemplate.MatchPerformative(ACLMessage.REQUEST)
        );
        
        ACLMessage returnBackPack = myAgent.blockingReceive(backPackTemp, 3000);
        
        if (returnBackPack != null) {
            try {
            	
            	// réception et ajout des trésors supplémentaires
            	List<Couple<Observation, Integer>> back_pack = (List<Couple<Observation, Integer>>) returnBackPack.getContentObject();
            	    
            	for(Couple<Observation, Integer> bp : back_pack) {
            		int qte_avant = stockage.get(bp.getLeft());
            		stockage.put(bp.getLeft(), qte_avant + bp.getRight());
            	}
            	
            	
            	// envoi de l'accusé de réception
            	ACLMessage reponse = returnBackPack.createReply();
            	reponse.setSender(myAgent.getAID());
            	reponse.setProtocol("ProtocolTanker");
            	reponse.setPerformative(ACLMessage.AGREE);
            	reponse.setContent("Bien reçu et ajouté !");
            	
            	myAgent.sendMessage(reponse);        	
            	
            	if(pour_debugger == 0) {
            		System.out.println(myAgent.getLocalName() + " possède désormais comme stockage : " + stockage + " grâce à l'ajout de " + returnBackPack.getSender().getLocalName());
            		pour_debugger++;
            	}
            } catch (UnreadableException e) {
                e.printStackTrace();
            }           
                    
        } else {
        	System.out.println(myAgent.getLocalName() + " n’a pas reçu de back_pack");
        }
        
        /* en travaux (pas fini)
        // Réception de la possible disparition du coffre et dans ce cas faudra le retirer de la liste théorique 
        /// pour la redistribution des trésors. S'il reste des agents ne pouvant pas faire parti de coalitions,
        /// on les envoie en repérage sur les prochains coffres non attribués pour vérifier qu'il sont toujours là
        /// et avertir le tanker s'ils ont disparu pour éviter d'envoyer les coalitions sur des trésors qui ne sont plus présents.
        /// S'il ne reste plus de coffres, ils partent au hasard sur la map pour fouiller et voir s'ils repèrent des coffres.
        /// Et ainsi de suite.
        ///
   
        MessageTemplate returnInfosCoffreTemp = MessageTemplate.and(
                MessageTemplate.MatchProtocol("SHARE-INFOS-COFFRE"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
            );
           
            ACLMessage returnInfosCoffre = myAgent.blockingReceive(returnInfosCoffreTemp, 3000);
            
            if (returnInfosCoffre != null) {
            	try {
                	
            	Couple<String,String> infos_coffre_received =
            			(Couple<String,String>) returnInfosCoffre.getContentObject();
            	
            	String dest = infos_coffre_received.getLeft();
            	String info_coffre = infos_coffre_received.getRight();
            	// phase de calcul
                Couple<List<Map.Entry<String, Map<Observation, String>>>, List<Map.Entry<String, Map<Observation, String>>>> list_theorique = myAgent.getListTheorique();
                Observation obs = null;
                String transmetteur = returnInfosCoffre.getSender().getLocalName();
                
                List<Map.Entry<String, Map<Observation, String>>> liste_tresor = null;
                if(myAgent.getListGold().containsKey(dest)) {
                	liste_tresor = list_theorique.getLeft();
                	obs = Observation.GOLD;
                } else {
                	liste_tresor = list_theorique.getRight();
                	obs = Observation.DIAMOND;
                }
                
                // on récupère le trésor maximal
                String bestNode = null;
                int max_tresor = -1;
                for(Map.Entry<String, Map<Observation, String>> l : liste_tresor) {
                	String nodeId = l.getKey();
                    Map<Observation, String> caracteristiques = l.getValue();

                    int qte = Integer.parseInt(caracteristiques.get(obs));

                    // Comparaison
                    if (qte > max_tresor) {
                        max_tresor = qte;
                        bestNode = nodeId;
                    }
                }
                
                if(bestNode != null) {
                	List<Couple<Observation, Integer>> back_pack = myAgent.getListBackFreeSpace().get(transmetteur);
                	Set<Couple<Observation,Integer>> expertises = myAgent.getListExpertise().get(transmetteur);
                	
                    
                    int tresor_capacite = 0;
                    int strength = 0;
                    int lockpicking = 0;

                    for (Couple<Observation, Integer> o : back_pack) {
                        if (o.getLeft() == obs) {
                            tresor_capacite = o.getRight();
                        }
                    }

                    for (Couple<Observation, Integer> exp : expertises) {
                        if (exp.getLeft() == Observation.STRENGH)
                            strength = exp.getRight();
                        else if (exp.getLeft() == Observation.LOCKPICKING)
                            lockpicking = exp.getRight();
                    }
                    
                    
                    // on rappelle la fonction pour former les coalitions et on repartage la nouvelle liste des objectifs obtenue
                	
                }
                
                
                
	            } catch (UnreadableException e) {
	                e.printStackTrace();
	            }    
                          
            }                
            
        */
        	    
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
