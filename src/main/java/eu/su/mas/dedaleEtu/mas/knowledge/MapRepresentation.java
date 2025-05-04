package eu.su.mas.dedaleEtu.mas.knowledge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeRejectedException;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.view.Viewer;

import dataStructures.serializableGraph.*;
import dataStructures.tuple.Couple;
import javafx.application.Platform;

/**
 * This simple topology representation only deals with the graph, not its content.</br>
 * The knowledge representation is not well written (at all), it is just given as a minimal example.</br>
 * The viewer methods are not independent of the data structure, and the dijkstra is recomputed every-time.
 * 
 * @author hc
 */
public class MapRepresentation implements Serializable {

	/**
	 * A node is open, closed, or agent
	 * @author hc
	 *
	 */

	public enum MapAttribute {	
		agent,open,closed;

	}

	private static final long serialVersionUID = -1333959882640838272L;

	/*********************************
	 * Parameters for graph rendering
	 ********************************/

	private String defaultNodeStyle= "node {"+"fill-color: black;"+" size-mode:fit;text-alignment:under; text-size:14;text-color:white;text-background-mode:rounded-box;text-background-color:black;}";
	private String nodeStyle_open = "node.agent {"+"fill-color: forestgreen;"+"}";
	private String nodeStyle_agent = "node.open {"+"fill-color: blue;"+"}";
	private String nodeStyle=defaultNodeStyle+nodeStyle_agent+nodeStyle_open;

	private Graph g; //data structure non serializable
	private Viewer viewer; //ref to the display,  non serializable
	private Integer nbEdges;//used to generate the edges ids

	private SerializableSimpleGraph<String, MapAttribute> sg;//used as a temporary dataStructure during migration


	public MapRepresentation() {
		//System.setProperty("org.graphstream.ui.renderer","org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		System.setProperty("org.graphstream.ui", "javafx");
		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		Platform.runLater(() -> {
			openGui();
		});
		//this.viewer = this.g.display();

		this.nbEdges=0;
	}

	/**
	 * Add or replace a node and its attribute 
	 * @param id unique identifier of the node
	 * @param mapAttribute attribute to process
	 */
	public synchronized void addNode(String id,MapAttribute mapAttribute){
		Node n;
		if (this.g.getNode(id)==null){
			n=this.g.addNode(id);
		}else{
			n=this.g.getNode(id);
		}
		n.clearAttributes();
		n.setAttribute("ui.class", mapAttribute.toString());
		n.setAttribute("ui.label",id);
	}

	/**
	 * Add a node to the graph. Do nothing if the node already exists.
	 * If new, it is labeled as open (non-visited)
	 * @param id id of the node
	 * @return true if added
	 */
	public synchronized boolean addNewNode(String id) {
		Node existing = this.g.getNode(id);
		if (existing == null){
			addNode(id, MapAttribute.open);
			return true;
		}
		return false;
	}

	/**
	 * Add an undirect edge if not already existing.
	 * @param idNode1 unique identifier of node1
	 * @param idNode2 unique identifier of node2
	 */
	public synchronized void addEdge(String idNode1,String idNode2){
		this.nbEdges++;
		try {
			this.g.addEdge(this.nbEdges.toString(), idNode1, idNode2);
		}catch (IdAlreadyInUseException e1) {
			System.err.println("ID existing");
			System.exit(1);
		}catch (EdgeRejectedException e2) {
			this.nbEdges--;
		} catch(ElementNotFoundException e3){

		}
	}

	/**
	 * Compute the shortest Path from idFrom to IdTo. The computation is currently not very efficient
	 * 
	 * 
	 * @param idFrom id of the origin node
	 * @param idTo id of the destination node
	 * @return the list of nodes to follow, null if the targeted node is not currently reachable
	 */
	public synchronized List<String> getShortestPath(String idFrom,String idTo){
		List<String> shortestPath=new ArrayList<String>();

		Dijkstra dijkstra = new Dijkstra();//number of edge
		dijkstra.init(g);
		dijkstra.setSource(g.getNode(idFrom));
		dijkstra.compute();//compute the distance to all nodes from idFrom
		List<Node> path=dijkstra.getPath(g.getNode(idTo)).getNodePath(); //the shortest path from idFrom to idTo
		Iterator<Node> iter=path.iterator();
		while (iter.hasNext()){
			shortestPath.add(iter.next().getId());
		}
		dijkstra.clear();
		if (shortestPath.isEmpty()) {//The openNode is not currently reachable
			return null;
		}else {
			shortestPath.remove(0);//remove the current position
		}
		return shortestPath;
	}

	public List<String> getShortestPathToClosestOpenNode(String myPosition) {
		//1) Get all openNodes
		List<String> opennodes=getOpenNodes();

		//2) select the closest one
		List<Couple<String,Integer>> lc=
				opennodes.stream()
				.map(on -> (getShortestPath(myPosition,on)!=null)? new Couple<String, Integer>(on,getShortestPath(myPosition,on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
				.collect(Collectors.toList());

		Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
		//3) Compute shorterPath

		return getShortestPath(myPosition,closest.get().getLeft());
	}



	public List<String> getOpenNodes(){
		return this.g.nodes()
				.filter(x ->x .getAttribute("ui.class")==MapAttribute.open.toString()) 
				.map(Node::getId)
				.collect(Collectors.toList());
	}


	/**
	 * Before the migration we kill all non serializable components and store their data in a serializable form
	 */
	public void prepareMigration(){
		serializeGraphTopology();

		closeGui();

		this.g=null;
	}

	/**
	 * Before sending the agent knowledge of the map it should be serialized.
	 */
	private void serializeGraphTopology() {
		this.sg= new SerializableSimpleGraph<String,MapAttribute>();
		Iterator<Node> iter=this.g.iterator();
		while(iter.hasNext()){
			Node n=iter.next();
			sg.addNode(n.getId(),MapAttribute.valueOf((String)n.getAttribute("ui.class")));
		}
		Iterator<Edge> iterE=this.g.edges().iterator();
		while (iterE.hasNext()){
			Edge e=iterE.next();
			Node sn=e.getSourceNode();
			Node tn=e.getTargetNode();
			sg.addEdge(e.getId(), sn.getId(), tn.getId());
		}	
	}

	public synchronized SerializableSimpleGraph<String,MapAttribute> getSerializableGraph(){
		serializeGraphTopology();
		return this.sg;
	}
	public Graph getGraph() {
	    return this.g;
	}

	/**
	 * After migration we load the serialized data and recreate the non serializable components (Gui,..)
	 */
	public synchronized void loadSavedData(){

		this.g= new SingleGraph("My world vision");
		this.g.setAttribute("ui.stylesheet",nodeStyle);

		openGui();

		Integer nbEd=0;
		for (SerializableNode<String, MapAttribute> n: this.sg.getAllNodes()){
			this.g.addNode(n.getNodeId()).setAttribute("ui.class", n.getNodeContent().toString());
			for(String s:this.sg.getEdges(n.getNodeId())){
				this.g.addEdge(nbEd.toString(),n.getNodeId(),s);
				nbEd++;
			}
		}
		System.out.println("Loading done");
	}

	/**
	 * Method called before migration to kill all non serializable graphStream components
	 */
	private synchronized void closeGui() {
		//once the graph is saved, clear non serializable components
		if (this.viewer!=null){
			//Platform.runLater(() -> {
			try{
				this.viewer.close();
			}catch(NullPointerException e){
				System.err.println("Bug graphstream viewer.close() work-around - https://github.com/graphstream/gs-core/issues/150");
			}
			//});
			this.viewer=null;
		}
	}

	/**
	 * Method called after a migration to reopen GUI components
	 */
	private synchronized void openGui() {
		this.viewer =new FxViewer(this.g, FxViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);//GRAPH_IN_GUI_THREAD)
		viewer.enableAutoLayout();
		viewer.setCloseFramePolicy(FxViewer.CloseFramePolicy.CLOSE_VIEWER);
		viewer.addDefaultView(true);

		g.display();
	}

	public void mergeMap(SerializableSimpleGraph<String, MapAttribute> sgreceived) {
	    for (SerializableNode<String, MapAttribute> n : sgreceived.getAllNodes()) {
	        boolean alreadyIn = false;
	        Node newnode = null;

	        try {
	            newnode = this.g.addNode(n.getNodeId());
	            newnode.setAttribute("ui.label", newnode.getId());
	            newnode.setAttribute("ui.class", n.getNodeContent().toString());
	        } catch (IdAlreadyInUseException e) {
	            alreadyIn = true;
	        }

	        if (alreadyIn) {
	            newnode = this.g.getNode(n.getNodeId());
	            String receivedAttr = n.getNodeContent().toString();
	            String currentAttr = newnode.getAttribute("ui.class").toString();

	            // ALWAYS prefer "closed" over "open"
	            if ("open".equals(currentAttr) && "closed".equals(receivedAttr)) {
	                newnode.setAttribute("ui.class", "closed");
	            }
	        }
	    }

	    // Ajout des arêtes
	    for (SerializableNode<String, MapAttribute> n : sgreceived.getAllNodes()) {
	        for (String neighbor : sgreceived.getEdges(n.getNodeId())) {
	            addEdge(n.getNodeId(), neighbor);
	        }
	    }
	}

	/**
	 * 
	 * @return true if there exist at least one openNode on the graph 
	 */
	public boolean hasOpenNode() {
		return (this.g.nodes()
				.filter(n -> n.getAttribute("ui.class")==MapAttribute.open.toString())
				.findAny()).isPresent();
	}

	
	// Fonctions ajoutées
	
	public synchronized List<String> getShortestPath2(String idFrom,String idTo,List<String> noeudsInterdits){
		List<String> shortestPath = new ArrayList<>();

	    Graph tempG = new SingleGraph("tempCopy");

	    // on copie les noeuds et leurs attributs
	    this.g.nodes().forEach(node -> {
	        Node newNode = tempG.addNode(node.getId());
	        node.attributeKeys().forEach(attrKey -> {
	            newNode.setAttribute(attrKey, node.getAttribute(attrKey));
	        });
	    });

	    // on copie les arêtes et leurs attributs
	    this.g.edges().forEach(edge -> {
	        String id = edge.getId();
	        String src = edge.getSourceNode().getId();
	        String tgt = edge.getTargetNode().getId();
	        boolean directed = edge.isDirected();

	        if (tempG.getNode(src) != null && tempG.getNode(tgt) != null) {
	            Edge newEdge = tempG.addEdge(id, src, tgt, directed);
	            edge.attributeKeys().forEach(attrKey -> {
	                newEdge.setAttribute(attrKey, edge.getAttribute(attrKey));
	            });
	        }
	    });

	    for (String n_id : noeudsInterdits) {
	        if (tempG.getNode(n_id) != null) {
	            tempG.removeNode(n_id);
	        }
	    }

	    Node source = tempG.getNode(idFrom);
	    Node target = tempG.getNode(idTo);

	    if (source == null || target == null) {
	        noeudsInterdits.add(idTo);  // On ajoute le noeud cible à éviter
	        return null;
	    }

	    Dijkstra dijkstra = new Dijkstra();
	    dijkstra.init(tempG);
	    dijkstra.setSource(source);
	    dijkstra.compute();

	    List<Node> path = dijkstra.getPath(target).getNodePath();

	    for (Node n : path) {
	        shortestPath.add(n.getId());
	    }

	    dijkstra.clear();

	    if (shortestPath.isEmpty()) {
	        noeudsInterdits.add(idTo);
	        return null;
	    }

	    // On enlève la position actuelle du chemin s’il est en tête
	    if (shortestPath.get(0).equals(idFrom)) {
	        shortestPath.remove(0);
	    }

	    return shortestPath;
	}

	
	public List<String> getShortestPathToClosestOpenNode2(String myPosition, List<String> noeudsInterdits) {
		//1) Get all openNodes
		
		List<String> opennodes=getOpenNodes();
		opennodes.removeAll(noeudsInterdits);

		//2) select the closest one
		List<Couple<String,Integer>> lc=
				opennodes.stream()
				.map(on -> (getShortestPath2(myPosition,on,noeudsInterdits)!=null)? new Couple<String, Integer>(on,getShortestPath(myPosition,on).size()): new Couple<String, Integer>(on,Integer.MAX_VALUE))//some nodes my be unreachable if the agents do not share at least one common node.
				.collect(Collectors.toList());

		Optional<Couple<String,Integer>> closest=lc.stream().min(Comparator.comparing(Couple::getRight));
		
		if (closest.isEmpty() || closest.get().getRight() == Integer.MAX_VALUE) {
	        System.out.println("Aucun chemin atteignable depuis " + myPosition);
	        return null;
	    }
		
		//3) Compute shorterPath

		return getShortestPath2(myPosition,closest.get().getLeft(), noeudsInterdits);
	}
	
    public String calculBarycentre(Set<String> treasureNodes) {
        String bestNode = null;
        int min_dist = Integer.MAX_VALUE;
      
        for (SerializableNode<String, MapAttribute> node : this.getSerializableGraph().getAllNodes()) {
        	String candidat = node.getNodeId();
        	int tot_dist = 0;
            boolean atteint = true;

            for (String t : treasureNodes) {
                List<String> path = this.getShortestPath(candidat, t);
                if (path == null || path.isEmpty()) {
                    atteint = false;
                    break;
                }
                tot_dist += path.size(); // nombre de transitions
            }

            if (atteint && tot_dist < min_dist) {
                min_dist = tot_dist;
                bestNode = candidat;
            }
        }

        return bestNode;
    }
	
	
	////ADDED FOR INTERBLOCAGE PERPOSE
	/// 
	
	public List<String> getFarAwayOpenNodes(String fromNode) {
	    List<String> result = new ArrayList<>();

	    if (this.g == null || !this.g.iterator().hasNext()) {
	        return result;
	    }

	    Dijkstra dijkstra = new Dijkstra();
	    dijkstra.init(this.g);
	    dijkstra.setSource(this.g.getNode(fromNode));
	    dijkstra.compute();

	    for (Node n : this.g) {
	        if (n.hasAttribute("ui.class") && n.getAttribute("ui.class").equals(MapAttribute.open.toString())) {
	            double dist = dijkstra.getPathLength(n);
	            if (dist > 10) { // seuil de distance arbitraire
	                result.add(n.getId());
	            }
	        }
	    }

	    return result;
	}
	
	public List<String> getAlternativePath(String from, String to, List<String> avoid) {
	    Graph graph = this.getGraph();
	    Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");

	    // Supprimer temporairement les noeuds à éviter
	    List<Node> removed = new ArrayList<>();
	    for (String id : avoid) {
	        Node n = graph.getNode(id);
	        if (n != null) {
	            removed.add(n);
	            n.setAttribute("removed", true); // Marque pour restauration
	            n.removeAttribute("ui.class"); // Cache si besoin
	        }
	    }

	    dijkstra.init(graph);
	    dijkstra.setSource(graph.getNode(from));
	    dijkstra.compute();

	    List<String> path = new ArrayList<>();
	    if (dijkstra.getPath(graph.getNode(to)) != null) {
	        for (Node node : dijkstra.getPath(graph.getNode(to)).getNodePath()) {
	            path.add(node.getId());
	        }
	    }

	    // Restaurer les noeuds supprimés
	    for (Node n : removed) {
	        n.setAttribute("ui.class", MapAttribute.open.toString());
	        n.removeAttribute("removed");
	    }

	    return path.isEmpty() ? null : path;
	}


}