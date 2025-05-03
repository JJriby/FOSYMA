package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Location;
import eu.su.mas.dedale.env.Observation;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent2;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.PingBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.PongBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ReceiveExpertiseBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ReceiveFinExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ReceiveMapBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ReceiveObjectifsBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ShareExpertise;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ShareFinExploBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ShareMapBehaviour3;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ShareObjectifsBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.poubelle.ParoleBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.FSMBehaviour;

public class GlobalBehaviour extends FSMBehaviour {
	
	private static final long serialVersionUID = 1L;
	private MapRepresentation myMap;
	
	private static final String Explore = "Explore";
	private static final String GoToRDV = "GoToRDV";
	private static final String PlanDAttaque = "PlanDAttaque";
	private static final String Collect = "Collect";
	private static final String InterBlocage = "InterBlocage";
	private static final String Ping = "Ping";
	private static final String Pong = "Pong";
	private static final String ShareMap = "ShareMap";
	private static final String ShareExpertise = "ShareExpertise";
	//private static final String Parole = "Parole"; // PENSER A SUPPRIMER TOUT CE QUI CONCERNE LA PAROLE SI ON ABANDONNE DEFINITIVEMENT L'IDEE
	private static final String FinExplo = "FinExplo";
	private static final String ShareFinExplo = "ShareFinExplo";
	
	private static final String ReceiveMap = "ReceiveMap";
	private static final String ReceiveExpertise = "ReceiveExpertise";
	private static final String ReceiveFinExplo = "ReceiveFinExplo";
	
	private static final String SuitePlanDAttaque = "SuitePlanDAttaque";
	private static final String Attente = "Attente";
	
	private static final String ShareObjectifs = "ShareObjectifs";
	private static final String ReceiveObjectifs = "ReceiveObjectifs";
	
	private static final String CollectSilo = "CollectSilo";
	
	public GlobalBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		
		this.myMap = myMap;
		
		// comportements
        this.registerFirstState(new ExploCoopBehaviour2((ExploreCoopAgent2) this.myAgent), Explore);
        this.registerState(new InterBlocageBehaviour((ExploreCoopAgent2) this.myAgent), InterBlocage);         
        this.registerState(new PingBehaviour((ExploreCoopAgent2) this.myAgent), Ping);
        this.registerState(new PongBehaviour((ExploreCoopAgent2) this.myAgent), Pong);
        this.registerState(new ShareMapBehaviour3((ExploreCoopAgent2) this.myAgent), ShareMap);
        
        this.registerState(new GoToRdvBehaviour((ExploreCoopAgent2) this.myAgent), GoToRDV);
        
        this.registerState(new CollectBehaviour((ExploreCoopAgent2) this.myAgent), Collect);
        this.registerState(new PlanDAttaqueBehaviour((ExploreCoopAgent2) this.myAgent), PlanDAttaque);
        this.registerState(new ShareExpertise((ExploreCoopAgent2) this.myAgent), ShareExpertise);
        //this.registerState(new ParoleBehaviour((ExploreCoopAgent2) this.myAgent), Parole);
        this.registerState(new FinExploBehaviour((ExploreCoopAgent2) this.myAgent), FinExplo);
        this.registerState(new ShareFinExploBehaviour((ExploreCoopAgent2) this.myAgent), ShareFinExplo);
        
        this.registerState(new ReceiveMapBehaviour((ExploreCoopAgent2) this.myAgent), ReceiveMap);
        this.registerState(new ReceiveExpertiseBehaviour((ExploreCoopAgent2) this.myAgent), ReceiveExpertise);
        this.registerState(new ReceiveFinExploBehaviour((ExploreCoopAgent2) this.myAgent), ReceiveFinExplo);
        
        this.registerState(new SuitePlanDAttaqueBehaviour((ExploreCoopAgent2) this.myAgent), SuitePlanDAttaque);
        this.registerState(new AttenteBehaviour((ExploreCoopAgent2) this.myAgent), Attente);
        
        this.registerState(new ShareObjectifsBehaviour((ExploreCoopAgent2) this.myAgent), ShareObjectifs);
        this.registerState(new ReceiveObjectifsBehaviour((ExploreCoopAgent2) this.myAgent), ReceiveObjectifs);

        this.registerState(new CollectSiloBehaviour((ExploreCoopAgent2) this.myAgent), CollectSilo);
       
        // transitions
        this.registerTransition(Explore, GoToRDV, 1);
        this.registerTransition(GoToRDV, FinExplo, 2);
        this.registerTransition(FinExplo, PlanDAttaque, 2);
        
        this.registerTransition(Explore, InterBlocage, 2);
        this.registerTransition(InterBlocage, GoToRDV, 1);
        this.registerTransition(GoToRDV, Explore, 1);
        
        // partage de map
        this.registerTransition(Explore, Ping, 3);
        this.registerTransition(Explore, Pong, 4);
        
        this.registerTransition(Ping, Explore, 0);
        this.registerTransition(Pong, Explore, 0);
        
        this.registerTransition(Ping, ShareMap, 1);
        this.registerTransition(Pong, ReceiveMap, 1);
        
        this.registerTransition(ShareMap, Explore, 0);
        this.registerTransition(ReceiveMap, Explore, 0);
        
        this.registerTransition(ShareMap, ReceiveMap, 1);
        this.registerTransition(ReceiveMap, ShareMap, 1);
        
        // partage de fins d'explo
        this.registerTransition(FinExplo, Ping, 3);
        this.registerTransition(FinExplo, Pong, 4);
        
        this.registerTransition(Ping, FinExplo, 4);
        this.registerTransition(Pong, FinExplo, 4);
        
        this.registerTransition(Ping, ShareFinExplo, 5);
        this.registerTransition(Pong, ReceiveFinExplo, 5);
        
        this.registerTransition(ShareFinExplo, FinExplo, 4);
        this.registerTransition(ReceiveFinExplo, FinExplo, 4);
        
        this.registerTransition(ShareFinExplo, ReceiveFinExplo, 5);
        this.registerTransition(ReceiveFinExplo, ShareFinExplo, 5);
        
        this.registerTransition(ShareFinExplo, ReceiveMap, 7);
        this.registerTransition(ReceiveFinExplo, ShareMap, 7);
        
        this.registerTransition(ShareMap, FinExplo, 4);
        this.registerTransition(ReceiveMap, FinExplo, 4);
        
        // partage de fins d'explo et de map pendant le chemin jusqu'au rdv
        this.registerTransition(GoToRDV, Ping, 3);
        this.registerTransition(GoToRDV, Pong, 4);
        
        this.registerTransition(Ping, GoToRDV, 6);
        this.registerTransition(Pong, GoToRDV, 6);
        
        this.registerTransition(ShareFinExplo, GoToRDV, 6);
        this.registerTransition(ReceiveFinExplo, GoToRDV, 6);
        
        this.registerTransition(ShareMap, GoToRDV, 6);
        this.registerTransition(ReceiveMap, GoToRDV, 6);
                
        // partage d'expertises
        this.registerTransition(PlanDAttaque, Ping, 3);
        this.registerTransition(PlanDAttaque, Pong, 4);
        
        this.registerTransition(Ping, PlanDAttaque, 2);
        this.registerTransition(Pong, PlanDAttaque, 2);
        
        this.registerTransition(Ping, ShareExpertise, 3);
        this.registerTransition(Pong, ReceiveExpertise, 3);
        
        this.registerTransition(ShareExpertise, PlanDAttaque, 2);
        this.registerTransition(ReceiveExpertise, PlanDAttaque, 2);
        
        this.registerTransition(ShareExpertise, ReceiveExpertise, 3);
        this.registerTransition(ReceiveExpertise, ShareExpertise, 3);
        
        // au cas où y a encore transmission des listes de fin d'explo
        this.registerTransition(ShareFinExplo, PlanDAttaque, 2);
        this.registerTransition(ReceiveFinExplo, PlanDAttaque, 2);
        
        // plan d'attaque vers la suite du plan d'attaque si Silo sinon direction salle d'attente
        this.registerTransition(PlanDAttaque, SuitePlanDAttaque, 8);
        this.registerTransition(PlanDAttaque, Attente, 9);
        
        
        // partage des listes des objectifs (pdv Silo)
        this.registerTransition(SuitePlanDAttaque, Ping, 3);
        this.registerTransition(Ping, SuitePlanDAttaque, 10);
        
        this.registerTransition(Ping, ShareObjectifs, 11);
        this.registerTransition(ShareObjectifs, SuitePlanDAttaque, 10);
        
        this.registerTransition(SuitePlanDAttaque, GoToRDV, 12);
        
        
        // partage des listes des objectifs (pdv autres agents)
        this.registerTransition(Attente, Ping, 3);
        this.registerTransition(Attente, Pong, 4);
        
        this.registerTransition(Ping, Attente, 13);
        this.registerTransition(Pong, Attente, 13);
        
        this.registerTransition(Pong, ReceiveObjectifs, 11);
        
        this.registerTransition(ShareObjectifs, Attente, 13);
        this.registerTransition(ReceiveObjectifs, Attente, 13);
        
        this.registerTransition(Attente, GoToRDV, 12);
        
        
        // faire des temps d'attente quand on change de phase de comportements
        
        // au cas où qlq n'a pas fini de demander les expertises
        this.registerTransition(ShareExpertise, Attente, 13);  
        
        // au cas où qlq tjrs dans PlanDAttaque continue de partager ses expertises
        this.registerTransition(ReceiveObjectifs, PlanDAttaque, 2);
        
        // je ne sais plus à ce niveau là
        this.registerTransition(ShareExpertise, FinExplo, 4);
        this.registerTransition(ShareExpertise, GoToRDV, 6);
        this.registerTransition(ReceiveObjectifs, GoToRDV, 6);
        
        
        // une fois le partage des objectifs fini, chacun se dirige vers la destination attribuée pour la récolte
        this.registerTransition(GoToRDV, Collect, 14);
        this.registerTransition(GoToRDV, CollectSilo, 16);
        
        
        // pour l'interblocage lors du trajet jusqu'au point de rdv
        this.registerTransition(GoToRDV, InterBlocage, 5);
        
        
        
        // pour la collecte pdv agents
        this.registerTransition(Collect, GoToRDV, 15);
       
        
        // pour la récolte pdv Silo
        
        
        
        // autre
       
        //this.registerTransition(Ping, Parole, 4);
        //this.registerTransition(Parole, PlanDAttaque, 1);
        
	}
	
	public MapRepresentation getMyMap() {
        return this.myMap;
    }
	
	public void setMyMap(MapRepresentation myMap) {
        this.myMap = myMap;
    }
	
}
