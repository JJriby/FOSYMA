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
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ShareInfosInterBlocageBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ShareMapBehaviour3;
import eu.su.mas.dedaleEtu.mas.behaviours.communication.ShareObjectifsBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.poubelle.ParoleBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.FSMBehaviour;

public class GlobalBehaviour extends FSMBehaviour {
	
	private static final long serialVersionUID = 1L;
	private MapRepresentation myMap;
	
	
	public static final int TO_EXPLORE = 0;  
	public static final int TO_FIN_EXPLO = 1; 
	public static final int TO_PLAN_D_ATTAQUE = 2; 
	public static final int TO_SUITE_PLAN_D_ATTAQUE = 3;
	public static final int TO_ATTENTE = 4;
	public static final int TO_COLLECT_SILO = 5;
	public static final int TO_COLLECT = 6; 
	public static final int TO_SHARE_JUST_COLLECT = 7; 
	public static final int TO_BLOCAGE = 8;
	
	public static final int TO_GO_TO_RDV = 10;
	
	public static final int TO_INTERBLOCAGE = 11;
	public static final int TO_SHARE_INFOS_INTERBLOCAGE = 12;
	
	public static final int TO_PING = 13;
	public static final int TO_PONG = 14;
	
	public static final int TO_SHARE_MAP = 20;
	public static final int TO_SHARE_EXPERTISE = 21;
	public static final int TO_SHARE_FIN_EXPLO = 22;
	public static final int TO_SHARE_OBJECTIFS = 23;

	public static final int TO_RECEIVE_MAP = 30;
	public static final int TO_RECEIVE_EXPERTISE = 31;
	public static final int TO_RECEIVE_FIN_EXPLO = 32;
	public static final int TO_RECEIVE_OBJECTIFS = 33;
	
	
	
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
	private static final String ShareJustCollect = "ShareJustCollect";
	
	private static final String ShareInfosInterBlocage = "ShareInfosInterBlocage";
	
	private static final String Blocage = "Blocage";
	
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
        this.registerState(new ShareJustCollectBehaviour((ExploreCoopAgent2) this.myAgent), ShareJustCollect);
        
        this.registerState(new ShareInfosInterBlocageBehaviour((ExploreCoopAgent2) this.myAgent), ShareInfosInterBlocage);
       
        this.registerState(new BlocageBehaviour((ExploreCoopAgent2) this.myAgent), Blocage);
        
        
        // transitions
        this.registerTransition(Explore, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(GoToRDV, FinExplo, TO_FIN_EXPLO);
        this.registerTransition(FinExplo, PlanDAttaque, TO_PLAN_D_ATTAQUE);
        
        //this.registerTransition(Explore, ShareInfosInterBlocage, 2);
        this.registerTransition(Explore, InterBlocage, TO_INTERBLOCAGE);
        
        this.registerTransition(InterBlocage, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(GoToRDV, Explore, TO_EXPLORE);
        
        // partage de map
        this.registerTransition(Explore, Ping, TO_PING);
        this.registerTransition(Explore, Pong, TO_PONG);
        
        this.registerTransition(Ping, Explore, TO_EXPLORE);
        this.registerTransition(Pong, Explore, TO_EXPLORE);
        
        this.registerTransition(Ping, ShareMap, TO_SHARE_MAP);
        this.registerTransition(Pong, ReceiveMap, TO_RECEIVE_MAP);
        
        this.registerTransition(ShareMap, Explore, TO_EXPLORE);
        this.registerTransition(ReceiveMap, Explore, TO_EXPLORE);
        
        this.registerTransition(ShareMap, ReceiveMap, TO_RECEIVE_MAP);
        this.registerTransition(ReceiveMap, ShareMap, TO_SHARE_MAP);
        
        // partage de fins d'explo
        this.registerTransition(FinExplo, Ping, TO_PING);
        this.registerTransition(FinExplo, Pong, TO_PONG);
        
        this.registerTransition(Ping, FinExplo, TO_FIN_EXPLO);
        this.registerTransition(Pong, FinExplo, TO_FIN_EXPLO);
        
        this.registerTransition(Ping, ShareFinExplo, TO_SHARE_FIN_EXPLO);
        this.registerTransition(Pong, ReceiveFinExplo, TO_RECEIVE_FIN_EXPLO);
        
        this.registerTransition(ShareFinExplo, FinExplo, TO_FIN_EXPLO);
        this.registerTransition(ReceiveFinExplo, FinExplo, TO_FIN_EXPLO);
        
        this.registerTransition(ShareFinExplo, ReceiveFinExplo, TO_RECEIVE_FIN_EXPLO);
        this.registerTransition(ReceiveFinExplo, ShareFinExplo, TO_SHARE_FIN_EXPLO);
        
        this.registerTransition(ShareFinExplo, ReceiveMap, TO_RECEIVE_MAP);
        this.registerTransition(ReceiveFinExplo, ShareMap, TO_SHARE_MAP);
        
        this.registerTransition(ShareMap, FinExplo, TO_FIN_EXPLO);
        this.registerTransition(ReceiveMap, FinExplo, TO_FIN_EXPLO);
        
        // partage de fins d'explo et de map pendant le chemin jusqu'au rdv
        this.registerTransition(GoToRDV, Ping, TO_PING);
        this.registerTransition(GoToRDV, Pong, TO_PONG);
        
        this.registerTransition(Ping, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(Pong, GoToRDV, TO_GO_TO_RDV);
        
        this.registerTransition(ShareFinExplo, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(ReceiveFinExplo, GoToRDV, TO_GO_TO_RDV);
        
        this.registerTransition(ShareMap, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(ReceiveMap, GoToRDV, TO_GO_TO_RDV);
                
        // partage d'expertises
        this.registerTransition(PlanDAttaque, Ping, TO_PING);
        this.registerTransition(PlanDAttaque, Pong, TO_PONG);
        
        this.registerTransition(Ping, PlanDAttaque, TO_PLAN_D_ATTAQUE);
        this.registerTransition(Pong, PlanDAttaque, TO_PLAN_D_ATTAQUE);
        
        this.registerTransition(Ping, ShareExpertise, TO_SHARE_EXPERTISE);
        this.registerTransition(Pong, ReceiveExpertise, TO_RECEIVE_EXPERTISE);
        
        this.registerTransition(ShareExpertise, PlanDAttaque, TO_PLAN_D_ATTAQUE);
        this.registerTransition(ReceiveExpertise, PlanDAttaque, TO_PLAN_D_ATTAQUE);
        
        this.registerTransition(ShareExpertise, ReceiveExpertise, TO_RECEIVE_EXPERTISE);
        this.registerTransition(ReceiveExpertise, ShareExpertise, TO_SHARE_EXPERTISE);
        
        // au cas où y a encore transmission des listes de fin d'explo
        this.registerTransition(ShareFinExplo, PlanDAttaque, TO_PLAN_D_ATTAQUE);
        this.registerTransition(ReceiveFinExplo, PlanDAttaque, TO_PLAN_D_ATTAQUE);
        
        // plan d'attaque vers la suite du plan d'attaque si Silo sinon direction salle d'attente
        this.registerTransition(PlanDAttaque, SuitePlanDAttaque, TO_SUITE_PLAN_D_ATTAQUE);
        this.registerTransition(PlanDAttaque, Attente, TO_ATTENTE);
        
        
        // partage des listes des objectifs (pdv Silo)
        this.registerTransition(SuitePlanDAttaque, Ping, TO_PING);
        this.registerTransition(Ping, SuitePlanDAttaque, TO_SUITE_PLAN_D_ATTAQUE);
        
        this.registerTransition(Ping, ShareObjectifs, TO_SHARE_OBJECTIFS);
        this.registerTransition(ShareObjectifs, SuitePlanDAttaque, TO_SUITE_PLAN_D_ATTAQUE);
        
        this.registerTransition(SuitePlanDAttaque, GoToRDV, TO_GO_TO_RDV);
        
        
        // partage des listes des objectifs (pdv autres agents)
        this.registerTransition(Attente, Ping, TO_PING);
        this.registerTransition(Attente, Pong, TO_PONG);
        
        this.registerTransition(Ping, Attente, TO_ATTENTE);
        this.registerTransition(Pong, Attente, TO_ATTENTE);
        
        this.registerTransition(Pong, ReceiveObjectifs, TO_RECEIVE_OBJECTIFS);
        
        this.registerTransition(ShareObjectifs, Attente, TO_ATTENTE);
        this.registerTransition(ReceiveObjectifs, Attente, TO_ATTENTE);
        
        this.registerTransition(Attente, GoToRDV, TO_GO_TO_RDV);
        
                
        // au cas où qlq n'a pas fini de demander les expertises
        this.registerTransition(ShareExpertise, Attente, TO_ATTENTE);  
        
        // au cas où qlq tjrs dans PlanDAttaque continue de partager ses expertises
        this.registerTransition(ReceiveObjectifs, PlanDAttaque, TO_PLAN_D_ATTAQUE);
        
        
        // dans d'autres cas
        this.registerTransition(ShareExpertise, FinExplo, TO_FIN_EXPLO);
        this.registerTransition(ShareExpertise, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(ReceiveObjectifs, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(ReceiveFinExplo, Explore, TO_EXPLORE);
        this.registerTransition(ReceiveExpertise, Attente, TO_ATTENTE); 
        this.registerTransition(ShareFinExplo, Explore, TO_EXPLORE);  
        this.registerTransition(ShareMap, PlanDAttaque, TO_PLAN_D_ATTAQUE);
		
        
        // une fois le partage des objectifs fini, chacun se dirige vers la destination attribuée pour la récolte
        this.registerTransition(GoToRDV, Collect, TO_COLLECT);
        this.registerTransition(GoToRDV, CollectSilo, TO_COLLECT_SILO);
        
        
        // pour l'interblocage lors du trajet jusqu'au point de rdv
        this.registerTransition(InterBlocage, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        this.registerTransition(ShareInfosInterBlocage, InterBlocage, TO_INTERBLOCAGE);
        
        this.registerTransition(GoToRDV, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        this.registerTransition(InterBlocage, GoToRDV, TO_GO_TO_RDV);
        
        this.registerTransition(GoToRDV, InterBlocage, TO_INTERBLOCAGE);
        
        this.registerTransition(CollectSilo, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        this.registerTransition(Collect, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        //this.registerTransition(GoToRDV, Ping, TO_PING);
        
        this.registerTransition(GoToRDV, Blocage, TO_BLOCAGE);
        this.registerTransition(Blocage, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(Blocage, InterBlocage, TO_INTERBLOCAGE);
        
        this.registerTransition(ReceiveFinExplo, Attente, TO_ATTENTE);
        /*this.registerTransition(Ping, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        
        this.registerTransition(ShareInfosInterBlocage, InterBlocage, TO_SHARE_INTERBLOCAGE);
        
        this.registerTransition(Ping, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        this.registerTransition(Pong, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE); 
        
        this.registerTransition(Collect, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        this.registerTransition(CollectSilo, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        this.registerTransition(GoToRDV, ShareInfosInterBlocage, TO_SHARE_INFOS_INTERBLOCAGE);
        
        this.registerTransition(ShareInfosInterBlocage, Collect, TO_COLLECT);
        this.registerTransition(ShareInfosInterBlocage, CollectSilo, TO_COLLECT_SILO);
        this.registerTransition(ShareInfosInterBlocage, GoToRDV, TO_GO_TO_RDV);

        this.registerTransition(ShareInfosInterBlocage, FinExplo, TO_FIN_EXPLO);
        */
        
        // pour la collecte
        this.registerTransition(Collect, GoToRDV, TO_GO_TO_RDV);
        this.registerTransition(GoToRDV, ShareJustCollect, TO_SHARE_JUST_COLLECT);
                
        this.registerTransition(ShareJustCollect, GoToRDV, TO_GO_TO_RDV);
        
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
	
	
	public int getTypeReception(int val) {
		switch (val) {
	        case GlobalBehaviour.TO_SHARE_MAP :
	        	return GlobalBehaviour.TO_RECEIVE_MAP;
	        case GlobalBehaviour.TO_SHARE_EXPERTISE :
	        	return GlobalBehaviour.TO_RECEIVE_EXPERTISE;
	        case GlobalBehaviour.TO_SHARE_FIN_EXPLO :
	        	return GlobalBehaviour.TO_RECEIVE_FIN_EXPLO;
	        case GlobalBehaviour.TO_SHARE_OBJECTIFS :
	        	return GlobalBehaviour.TO_RECEIVE_OBJECTIFS;
		}
		
		return -1;
	}
	
	
	public int getTypeTransmission(int val) {
		switch (val) {
	        case GlobalBehaviour.TO_RECEIVE_MAP :
	        	return GlobalBehaviour.TO_SHARE_MAP;
	        case GlobalBehaviour.TO_RECEIVE_EXPERTISE :
	        	return GlobalBehaviour.TO_SHARE_EXPERTISE;
	        case GlobalBehaviour.TO_RECEIVE_FIN_EXPLO :
	        	return GlobalBehaviour.TO_SHARE_FIN_EXPLO;
	        case GlobalBehaviour.TO_RECEIVE_OBJECTIFS :
	        	return GlobalBehaviour.TO_SHARE_OBJECTIFS;
		}
		
		return -1;
	}
	
}
