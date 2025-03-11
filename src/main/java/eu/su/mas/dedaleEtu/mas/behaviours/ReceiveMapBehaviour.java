package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * Behaviour qui écoute les messages contenant une carte et fusionne les données reçues avec la carte locale.
 */
public class ReceiveMapBehaviour extends CyclicBehaviour {
    
    private MapRepresentation myMap;

    public ReceiveMapBehaviour(MapRepresentation myMap) {
        this.myMap = myMap;
    }

    @Override
    public void action() {
        MessageTemplate msgTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol("SHARE-TOPO"),
            MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        ACLMessage msgReceived = myAgent.receive(msgTemplate);
        if (msgReceived != null) {
            try {
                SerializableSimpleGraph<String, MapAttribute> receivedGraph =
                    (SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();

                System.out.println(myAgent.getLocalName() + " received a map from " + msgReceived.getSender().getLocalName());

                // Fusionner la carte reçue avec la carte locale
                myMap.mergeMap(receivedGraph);
                System.out.println(myAgent.getLocalName() + " has merged the received map.");
                
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        } else {
            block();
        }
    }
}
