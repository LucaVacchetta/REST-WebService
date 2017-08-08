package it.polito.nffg.neo4j.tests;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.junit.Assert.*;

import javax.print.attribute.standard.Media;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import org.junit.BeforeClass;
import org.junit.Test;

import it.polito.nffg.neo4j.jaxb.FunctionalTypes;
import it.polito.nffg.neo4j.jaxb.Graph;
import it.polito.nffg.neo4j.jaxb.Graphs;
import it.polito.nffg.neo4j.jaxb.Neighbour;
import it.polito.nffg.neo4j.jaxb.Node;
import it.polito.nffg.neo4j.jaxb.ObjectFactory;
import it.polito.nffg.neo4j.jaxb.Paths;
import it.polito.nffg.neo4j.jaxb.Policy;
import it.polito.nffg.neo4j.jaxb.Reachability;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.File;
import java.net.URI;
import java.util.List;

public class GraphTest {

	private static WebTarget target;
	private static ObjectFactory objF;
	
	@BeforeClass
	public static void setUpClient(){
		Client client = ClientBuilder.newClient(); 
		// create a web target for the intended URI
		target = client.target(getBaseURI());
		
		//initialize the object factory
		objF = new ObjectFactory();
	}
	
	private static URI getBaseURI() {
	    return UriBuilder.fromUri("http://localhost:8080/neo4jmanager/rest/").build();
	}
	
	
	/* 
	 * Check the oriented reachability between node "sap1" to "sap2"
	 * and the result must be TRUE
	 */
	@Test
	public void testReachabilityAndGets() {
		String filename = "testcases/test1.xml";
		Graph graph = unmarshalFile(filename);
	
		Graph graphReturned = postGraph(graph);
		testReachability(graphReturned);
		testGetGraphs(graphReturned);
		testGetGraph(graphReturned);
		testGetNodes(graphReturned);
		testGetNode(graphReturned);
		testGetNeighbours(graphReturned);
		testGetNeighbour(graphReturned);
		deleteGraph(graphReturned);
	}

	private void testGetNeighbour(Graph graphReturned) {
		long nodeId=-1;
		long neighId=-1;
		for(Node n: graphReturned.getNode()){
			if(!n.getNeighbour().isEmpty()){
				nodeId = n.getId();
				for(Neighbour neigh : n.getNeighbour()){
					neighId=neigh.getId();
					break;
				}
				break;
			}
		}
		assertTrue(nodeId!=-1 && neighId!=-1);
		Response respGet = target.path("graphs")
									.path(String.valueOf(graphReturned.getId()))
									.path("nodes")
									.path(String.valueOf(nodeId))
									.path("neighbours")
									.path(String.valueOf(neighId))
									.request()
									.accept(MediaType.APPLICATION_XML)
									.get();
		assertTrue("Unable to perform a get neighbour query", respGet.getStatus() == Status.OK.getStatusCode());
		Neighbour neigh = respGet.readEntity(Neighbour.class);
		assertNotNull("Null response is returned", neigh);
		assertTrue("Unable to retrieve a correct neighbour", neigh.getId()==neighId);
	}
	
	private void testGetNeighbours(Graph graphReturned) {
		long nodeId=-1;
		for(Node n: graphReturned.getNode()){
			nodeId = n.getId();
			break;
		}
		assertTrue(nodeId!=-1);
		Response respGet = target.path("graphs")
									.path(String.valueOf(graphReturned.getId()))
									.path("nodes")
									.path(String.valueOf(nodeId))
									.path("neighbours")
									.request()
									.accept(MediaType.APPLICATION_XML)
									.get();
		assertTrue("Unable to perform a get neighbours query", respGet.getStatus() == Status.OK.getStatusCode());
		List<Neighbour> neighs = respGet.readEntity(new GenericType<List<Neighbour>>(){});
		assertNotNull("Null response is returned", neighs);
		Node node=null;
		for(Node n: graphReturned.getNode()){
			if(n.getId()==nodeId){
				node=n;
				break;
			}
		}
		assertNotNull(node);
		assertTrue("Unable to retrieve a correct neighbours", neighs.size() == node.getNeighbour().size());
	}
	
	private void testGetNode(Graph graphReturned) {
		long nodeId=-1;
		for(Node n: graphReturned.getNode()){
			nodeId = n.getId();
			break;
		}
		assertTrue(nodeId!=-1);
		Response respGet = target.path("graphs")
									.path(String.valueOf(graphReturned.getId()))
									.path("nodes")
									.path(String.valueOf(nodeId))
									.request()
									.accept(MediaType.APPLICATION_XML)
									.get();
		assertTrue("Unable to perform a get node query", respGet.getStatus() == Status.OK.getStatusCode());
		Node node = respGet.readEntity(Node.class);
		assertNotNull("Null response is returned", node);
		assertTrue("Unable to retrieve a correct node", node.getId()==nodeId);
	}

	private void testGetNodes(Graph graphReturned) {
		Response respGet = target.path("graphs")
									.path(String.valueOf(graphReturned.getId()))
									.path("nodes")
									.request()
									.accept(MediaType.APPLICATION_XML)
									.get();
		assertTrue("Unable to perform a get nodes query", respGet.getStatus() == Status.OK.getStatusCode());
		List<Node> nodes = respGet.readEntity(new GenericType<List<Node>>(){});
		assertNotNull("Null response is returned", nodes);
		assertTrue("Unable to retrieve a collection of nodes", nodes.size()==graphReturned.getNode().size());
	}

	private void testGetGraph(Graph graphReturned) {
		Response respGet = target.path("graphs")
									.path(String.valueOf(graphReturned.getId()))
									.request()
									.accept(MediaType.APPLICATION_XML)
									.get();
		assertTrue("Unable to perform a get graph query", respGet.getStatus() == Status.OK.getStatusCode());
		Graph graph = respGet.readEntity(Graph.class);
		assertNotNull("Null response is returned", graph);
		assertTrue("Unable to retrieve a correct graph", graph.getId()==graphReturned.getId());
	}

	private void testGetGraphs(Graph graphReturned) {
		Response respGet = target.path("graphs")
									.request()
									.accept(MediaType.APPLICATION_XML)
									.get();
		assertTrue("Unable to perform a get graphs query", respGet.getStatus() == Status.OK.getStatusCode());
		Graphs graphs = respGet.readEntity(Graphs.class);
		assertNotNull("Null response is returned", graphs);
		boolean found = false;
		for(Graph g: graphs.getGraph()){
			if(g.getId() == graphReturned.getId()){
				found=true;
				break;
			}
		}
		if(!found)
			fail("Unable to read graph from graphs");
	}

	private void testReachability(Graph graphReturned){
		Response responseReachability = target.path("graphs")
				.path(String.valueOf(graphReturned.getId()))
				.path("reachability")
				.queryParam("src", "sap1")
				.queryParam("dst", "sap2")
				.queryParam("dir", "outgoing")
				.request()
				.accept(MediaType.APPLICATION_XML)
				.get();
		
		assertTrue("Unable to perform a reachability query", responseReachability.getStatus() == Status.OK.getStatusCode());
		
		Reachability reach = responseReachability.readEntity(Reachability.class);
		assertNotNull("Null response is returned", reach);
		assertTrue("Wrong sap1 -> sap2 is defined", reach.isResult());	
	}
	
	@Test
	public void testUpdateAddAndPath(){
		String filename = "testcases/test2.xml";
		Graph graph = unmarshalFile(filename);

		Graph graphReturned = postGraph(graph);
		
		testAddNode(graphReturned);
		testPutNode(graphReturned);
		testPath(graphReturned);
		deleteGraph(graphReturned);
	}
	
	private void testPath(Graph graphReturned) {
		Response responsePath = target.path("graphs")
				.path(String.valueOf(graphReturned.getId()))
				.path("paths")
				.queryParam("src", "webserver")
				.queryParam("dst", "UtenteUno")
				.queryParam("dir", "outgoing")
				.request()
				.accept(MediaType.APPLICATION_XML)
				.get();
		assertTrue("Unable to perform path query", responsePath.getStatus() == Status.OK.getStatusCode());
		Paths paths = responsePath.readEntity(Paths.class);
		assertNotNull("Null response is returned", paths);		
		assertTrue("Error occurs in path query operation", paths.getMessage().compareTo("No available paths") == 0);		
	}

	private void testPutNode(Graph graphReturned) {
		Node user1 = null;
		for(Node n : graphReturned.getNode()){
			if(n.getName().compareTo("user1") == 0){
				user1 = n;
				break;
			}
		}
		assertNotNull("Null response is returned", user1);
		user1.setName("UtenteUno");
		Neighbour newNeighbour = objF.createNeighbour();
		newNeighbour.setName("webClient");
		user1.getNeighbour().add(newNeighbour);
		Response respPutNode = target.path("graphs")
				.path(String.valueOf(graphReturned.getId()))
				.path("nodes")
				.path(String.valueOf(user1.getId()))
				.request(MediaType.APPLICATION_XML)
				.put(Entity.entity(user1, MediaType.APPLICATION_XML));
		assertTrue("Unable to perform a node PUT", respPutNode.getStatus() == Status.OK.getStatusCode());
		
		Response respGetNode = target.path("graphs")
				.path(String.valueOf(graphReturned.getId()))
				.path("nodes")
				.path(String.valueOf(user1.getId()))
				.request()
				.get();
		assertTrue("Unable to perform a node GET", respGetNode.getStatus() == Status.OK.getStatusCode());
		Node nodeReturned = respGetNode.readEntity(Node.class);
		assertNotNull("Null response is returned", nodeReturned);
		assertTrue("Update not performed well", nodeReturned.getName().compareTo("UtenteUno") == 0);
		for(Neighbour neigh : nodeReturned.getNeighbour()){
			assertTrue("Update not performed well (neighbour mistake)", neigh.getName().compareTo("webClient")==0 || neigh.getName().compareTo("nat")==0);
		}
	}

	private static void testAddNode(Graph graphReturned){
		Node newNode = objF.createNode();
		newNode.setFunctionalType(FunctionalTypes.WEBCLIENT);
		newNode.setName("webClient");
		Response respAddNode = target.path("graphs")
				.path(String.valueOf(graphReturned.getId()))
				.path("nodes")
				.request(MediaType.APPLICATION_XML)
				.post(Entity.entity(newNode, MediaType.APPLICATION_XML));
		
		assertTrue("Unable to perform a node POST", respAddNode.getStatus() == Status.CREATED.getStatusCode());
		assertNotNull("Null response is returned in add Node", respAddNode.getEntity());	
	}
	
	@Test
	public void testPolicyUpdateGraphAndNeighbour(){
		String filename = "testcases/test3.xml";
		Graph graph = unmarshalFile(filename);

		Graph graphReturned = postGraph(graph);
		testPolicy(graphReturned);
		testUpdateGraph(graphReturned);
		testUpdateNeighbour(graphReturned);
		deleteGraph(graphReturned);	
	}

	/*
	 * In this test the neighbour of user1 change from vpnaccess to nat
	 * so return the initial configuration
	 */
	private void testUpdateNeighbour(Graph graphReturned) {
		long user1Id = -1;
		long neighUser1Id = -1;
		for(Node n : graphReturned.getNode()){
			if(n.getName().compareTo("user1") == 0){
				user1Id = n.getId();
				for(Neighbour neigh : n.getNeighbour()){
					if(neigh.getName().compareTo("vpnaccess")==0){
						neighUser1Id = neigh.getId();
						break;
					}
				}
				break;
			}
		}
		Neighbour newNeigh = objF.createNeighbour();
		newNeigh.setName("nat");
		Response respNeighPut = target.path("graphs")
										.path(String.valueOf(graphReturned.getId()))
										.path("nodes")
										.path(String.valueOf(user1Id))
										.path("neighbours")
										.path(String.valueOf(neighUser1Id))
										.request(MediaType.APPLICATION_XML)
										.accept(MediaType.APPLICATION_XML)
										.put(Entity.entity(newNeigh, MediaType.APPLICATION_XML));
		assertTrue("Unable to perform neighbour PUT it return "+respNeighPut.getStatus(), respNeighPut.getStatus() == Status.OK.getStatusCode());
		Node nodeWithNeighbourUpdate = respNeighPut.readEntity(Node.class);
		assertNotNull("Null response is returned in testUpdateNeighbour", nodeWithNeighbourUpdate);
		for(Neighbour neigh : nodeWithNeighbourUpdate.getNeighbour()){
			if(neigh.getId() == neighUser1Id)
				assertTrue("Neihgbour PUT not performed well", neigh.getName().compareTo("nat") == 0);
		}
	}

	private void testUpdateGraph(Graph graphReturned) {
		for(Node n : graphReturned.getNode()){
			if(n.getName().compareTo("user1") == 0){
				//change neighbour of user1 from nat to vpnaccess
				for(Neighbour neigh : n.getNeighbour()){
					if(neigh.getName().compareTo("nat")==0){
						neigh.setName("vpnaccess");
						break;
					}
				}
				//change the functional type of user1 from END_HOST to WEBCLIENT
				n.setFunctionalType(FunctionalTypes.WEBCLIENT);
				break;
			}
		}
		//now the graph is modified
		Response respPutGraph = target.path("graphs")
										.path(String.valueOf(graphReturned.getId()))
										.request(MediaType.APPLICATION_XML)
										.accept(MediaType.APPLICATION_XML)
										.put(Entity.entity(graphReturned, MediaType.APPLICATION_XML));
		assertTrue("Unable to perform graph PUT", respPutGraph.getStatus() == Status.OK.getStatusCode());
		Graph graphUpdated = respPutGraph.readEntity(Graph.class);
		assertNotNull("Null response is returned", graphReturned);
		//check if updates are really done
		for(Node n : graphUpdated.getNode()){
			if(n.getName().compareTo("user1") == 0){
				assertTrue("Functional type not update", n.getFunctionalType().compareTo(FunctionalTypes.WEBCLIENT) == 0);
				for(Neighbour neigh : n.getNeighbour()){
					//user1 has only one neighbour
					assertTrue("Neighbour not update", neigh.getName().compareTo("vpnaccess") == 0);
				}
				break;
			}
		}
	}

	private void testPolicy(Graph graphReturned) {
		Response responseTraversal = target.path("graphs")
												.path(String.valueOf(graphReturned.getId()))
												.path("policy")
												.queryParam("source", "user1")
												.queryParam("destination", "webserver")
												.queryParam("type", "traversal")
												.queryParam("middlebox", "vpnaccess" )
												.request()
												.get();
		assertTrue("Unable to perform a policy GET", responseTraversal.getStatus() == Status.OK.getStatusCode());
		assertNotNull("Null response is returned in policy traversal test", responseTraversal.getEntity());	
		Policy policy = responseTraversal.readEntity(Policy.class);
		assertTrue("The policy traversal returns a wrong result", policy.isResult());		
	}

	@Test
	public void testCreateNeighbourDeleteNodeAndNeighbour(){
		String filename = "testcases/test4.xml";
		Graph graph = unmarshalFile(filename);

		Graph graphReturned = postGraph(graph);
		testPostNeighbour(graphReturned);
		testDeleteNode(graphReturned);
		testDeleteNeighbour(graphReturned);
		deleteGraph(graphReturned);
	}
	
	/*
	 * This test delete the neighbour "nat" to user1
	 */
	private void testDeleteNeighbour(Graph graphReturned) {
		long user1Id = -1;
		long natNeighId = -1;
		for(Node n : graphReturned.getNode()){
			if(n.getName().compareTo("user1") == 0){
				user1Id = n.getId();
				for(Neighbour neigh : n.getNeighbour()){
					if(neigh.getName().compareTo("nat") == 0){
						natNeighId = neigh.getId();
						break;
					}
				}
				break;
			}
		}
		
		Response respDeleteNeighbour = target.path("graphs")
												.path(String.valueOf(graphReturned.getId()))
												.path("nodes")
												.path(String.valueOf(user1Id))
												.path("neighbours")
												.path(String.valueOf(natNeighId))
												.request()
												.delete();
		assertTrue("Unable to perform neighbour DELETE", respDeleteNeighbour.getStatus() == Status.NO_CONTENT.getStatusCode());
		Response respGetNatNeigh = target.path("graphs")
											.path(String.valueOf(graphReturned.getId()))
											.path("nodes")
											.path(String.valueOf(user1Id))
											.path("neighbours")
											.path(String.valueOf(natNeighId))
											.request()
											.get();
		assertTrue("DELETE neighbour is not performed well", respGetNatNeigh.getStatus() == Status.NOT_FOUND.getStatusCode());
	}

	/*
	 * This test delete the node vpnexit
	 */
	private void testDeleteNode(Graph graphReturned) {
		long vpnexitId = -1;
		for(Node n : graphReturned.getNode()){
			if(n.getName().compareTo("vpnexit") == 0){
				vpnexitId = n.getId();
				break;
			}
		}
		Response respDeleteNode = target.path("graphs")
											.path(String.valueOf(graphReturned.getId()))
											.path("nodes")
											.path(Long.toString(vpnexitId))
											.request()
											.delete();
		assertTrue("Unable to perform node DELETE it return "+respDeleteNode.getStatus(), respDeleteNode.getStatus() == Status.NO_CONTENT.getStatusCode());
		Response respGetNodeVpnexit =  target.path("graphs")
												.path(String.valueOf(graphReturned.getId()))
												.path("nodes")
												.path(String.valueOf(vpnexitId))
												.request()
												.get();
		assertTrue("DELETE node is not performed well", respGetNodeVpnexit.getStatus() == Status.NOT_FOUND.getStatusCode());
	}

	/*
	 * This test is to add a neighbour to user1, so after this also
	 * vpnaccess is directly reachable from user1
	 */
	private void testPostNeighbour(Graph graphReturned) {
		Neighbour newNeighbour = objF.createNeighbour();
		newNeighbour.setName("vpnaccess");
		long user1Id = -1;
		for(Node n : graphReturned.getNode()){
			if(n.getName().compareTo("user1") == 0){
				user1Id = n.getId();
				break;
			}
		}
		
		Response respPostNeighbour = target.path("graphs")
											.path(String.valueOf(graphReturned.getId()))
											.path("nodes")
											.path(String.valueOf(user1Id))
											.path("neighbours")
											.request(MediaType.APPLICATION_XML)
											.accept(MediaType.APPLICATION_XML)
											.post(Entity.entity(newNeighbour, MediaType.APPLICATION_XML));
		assertTrue("Post neighbour returns wrong status code", respPostNeighbour.getStatus() == Status.CREATED.getStatusCode());
		Neighbour neighReturned = respPostNeighbour.readEntity(Neighbour.class);
		assertNotNull("Null response is returned", neighReturned);
		assertTrue("POST neighbour is performed not well", neighReturned.getName().compareTo("vpnaccess") == 0);
	}

	private static Graph unmarshalFile(String filename){
		Graphs graphs = objF.createGraphs();

		try {
            // create a JAXBContext capable of handling classes generated into
            // the primer.po package
            JAXBContext jc = JAXBContext.newInstance( "it.polito.nffg.neo4j.jaxb" );
            
            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();

            SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            try {
                Schema schema = sf.newSchema(new File("schema/xml_components.xsd"));
                u.setSchema(schema);
                u.setEventHandler(
                    new ValidationEventHandler() {
                        // allow unmarshalling to continue even if there are errors
                        public boolean handleEvent(ValidationEvent ve) {
                            // ignore warnings
                            if (ve.getSeverity() != ValidationEvent.WARNING) {
                                ValidationEventLocator vel = ve.getLocator();
                                System.out.println("Line:Col[" + vel.getLineNumber() +
                                    ":" + vel.getColumnNumber() +
                                    "]:" + ve.getMessage());
                            }
                            return true;
                        }
                    }
                );
            } catch (org.xml.sax.SAXException se) {
                System.out.println("Unable to validate due to following error.");
                se.printStackTrace();
            }
            
            graphs = (Graphs) u.unmarshal(new File(filename));
        } catch( UnmarshalException ue ) {
            // The JAXB specification does not mandate how the JAXB provider
            // must behave when attempting to unmarshal invalid XML data.  In
            // those cases, the JAXB provider is allowed to terminate the 
            // call to unmarshal with an UnmarshalException.
            System.out.println( "Caught UnmarshalException" );
        } catch( JAXBException je ) {
            je.printStackTrace();
        }
		return graphs.getGraph().get(0);
	}
	
	private static void deleteGraph(Graph graphReturned){
		Response respDelete = target.path("graphs")
				.path(String.valueOf(graphReturned.getId()))
				.request()
				.delete();

		assertTrue("Unable to perform a delete graph", respDelete.getStatus() == Status.NO_CONTENT.getStatusCode());
	}
	
	private static Graph postGraph(Graph graph){
		Response response = target.path("graphs")
				.request(MediaType.APPLICATION_XML)
				.accept(MediaType.APPLICATION_XML)
				.post(Entity.entity(graph,MediaType.APPLICATION_XML));
		
		assertTrue("Unable to post a graph into the web server", response.getStatus() == Status.CREATED.getStatusCode());
		Graph graphReturned = response.readEntity(Graph.class);
		assertNotNull("Null response is returned",graphReturned);
		return graphReturned;
	}
}
