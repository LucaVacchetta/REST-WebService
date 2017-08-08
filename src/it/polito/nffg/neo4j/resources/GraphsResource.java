
package it.polito.nffg.neo4j.resources;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.sun.media.jfxmedia.Media;

import it.polito.nffg.neo4j.jaxb.*;
import it.polito.nffg.neo4j.jaxb.Graphs;
import it.polito.nffg.neo4j.jaxb.Neighbour;
import it.polito.nffg.neo4j.jaxb.Node;
import it.polito.nffg.neo4j.exceptions.DuplicateNodeException;
import it.polito.nffg.neo4j.exceptions.MyInvalidDirectionException;
import it.polito.nffg.neo4j.exceptions.MyInvalidIdException;
import it.polito.nffg.neo4j.exceptions.MyInvalidObjectException;
import it.polito.nffg.neo4j.exceptions.MyNotFoundException;
import it.polito.nffg.neo4j.manager.Neo4jLibrary;
import it.polito.nffg.neo4j.service.Service;

/**
 * This class defines the methods that are mapped to HTTP request at path '/graphs'.
 *
 * @see <a href="https://jersey.java.net/nonav/apidocs/latest/jersey/javax/ws/rs/Path.html">@Path</a>
 */
@Path("graphs")
public class GraphsResource 
{
	static Neo4jLibrary lib = Neo4jLibrary.getNeo4jLibrary();
	static Service service = new Service();
	static ObjectFactory obFactory = new ObjectFactory();
	
	@GET
	@Produces({ MediaType.APPLICATION_XML })
	public Graphs getGraphs()
	{
		Graphs graphs;
		graphs = lib.getGraphs();
		
		return graphs;
	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_XML })
	public Response createGraph(Graph graph, @Context UriInfo uriInfo) throws BadRequestException,Exception
	{
		Graph graphReturned;
		try{
			graphReturned = lib.createGraph(graph);			
		}
		catch(MyNotFoundException e){
			throw new BadRequestException();
		}
		
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    	URI u = builder.path(Long.toString(graphReturned.getId())).build();
    	return Response.created(u).status(Status.CREATED).entity(graphReturned).build();
	}
	
	
	@GET
	@Path("{ graphId }")
	@Produces({ MediaType.APPLICATION_XML })
	public Graph getGraph(@PathParam("graphId") String id) throws NotFoundException, ForbiddenException
	{
		Graph graph;
		long graphId;
		
		try{
			graphId = service.convertStringId(id);
			graph=lib.getGraph(graphId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id");
		}
		return graph;
	}
	
	@PUT
	@Path("{ graphId }")
	@Consumes({ MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_XML })
	public Graph updateGraph(@PathParam("graphId") String id, Graph graph) throws NotFoundException,ForbiddenException,BadRequestException{
		Graph graphReturned;
		long graphId;
		
		try{
			graphId = service.convertStringId(id);
			graphReturned = lib.updateGraph(graph, graphId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id");
		}
		catch(DuplicateNodeException e3){
			throw new BadRequestException(e3.getMessage());
		}
		catch(MyInvalidObjectException e4){
			throw new BadRequestException(e4.getMessage());
		}
		return graphReturned;
	}
	
	@DELETE
	@Path("{ graphId }")
	public void deleteGraph(@PathParam("graphId") String id){
		long graphId;
		
		try{
			graphId = service.convertStringId(id);
			lib.deleteGraph(graphId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id");
		}
		return;
	}
	
	@POST
	@Path("{graphId}/nodes")
	@Consumes({ MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_XML })
	public Response createNode(Node node, @PathParam("graphId") String id, @Context UriInfo uriInfo) throws NotFoundException, BadRequestException, ForbiddenException
	{
		Node nodeReturned;
		long graphId;
		
		try
		{
			graphId = service.convertStringId(id);
			nodeReturned = lib.createNode(node, graphId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id");
		}
		catch(DuplicateNodeException e3){
			throw new BadRequestException();
		}
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    	URI u = builder.path(Long.toString(nodeReturned.getId())).build();
    	return Response.created(u).status(Status.CREATED).entity(nodeReturned).build();
	}
	
	@GET
	@Path("{graphId}/nodes")
	@Produces({MediaType.APPLICATION_XML})
	public Set<Node> getNodes(@PathParam("graphId") String id) throws NotFoundException, ForbiddenException{
		Set<Node> set;
		long graphId;
		
		try
		{
			graphId = service.convertStringId(id);
			set = lib.getNodes(graphId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id");
		}
		return set;
	}
	
	@GET
	@Path("{graphId}/nodes/{nodeId}")
	@Produces({MediaType.APPLICATION_XML})
	public Node getNode(@PathParam("graphId") String graphIdS, @PathParam("nodeId") String nodeIdS) throws NotFoundException, ForbiddenException{
		Node node;
		long graphId, nodeId;
		
		try{
			graphId = service.convertStringId(graphIdS);
			nodeId = service.convertStringId(nodeIdS);
			node = lib.getNode(graphId, nodeId);
		}
		catch(MyInvalidIdException e1){
			throw new ForbiddenException("Invalid graph Id and/or node Id");
		}
		catch(MyNotFoundException e2){
			throw new NotFoundException();
		}
		return node;
	}
	
	@PUT
	@Path("{graphId}/nodes/{nodeId}")
	@Consumes({MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_XML})
	public Node updateNode(@PathParam("graphId") String graphIdS, @PathParam("nodeId") String nodeIdS, Node node) throws NotFoundException,ForbiddenException{
		Node nodeReturned;
		long graphId, nodeId;
		
		try{
			graphId = service.convertStringId(graphIdS);
			nodeId = service.convertStringId(nodeIdS);
			nodeReturned = lib.updateNode(node, graphId, nodeId);
		}
		catch(MyInvalidIdException e1){
			throw new ForbiddenException("Invalid graph Id and/or node Id");
		}
		catch(MyNotFoundException e2){
			throw new NotFoundException();
		}
		catch(MyInvalidObjectException e3){
			throw new BadRequestException(e3.getMessage());
		}
		return nodeReturned;
	}
	
	@DELETE
	@Path("{graphId}/nodes/{nodeId}")
	public void deleteNode(@PathParam("graphId") String graphIdS, @PathParam("nodeId") String nodeIdS) throws ForbiddenException,NotFoundException{
		long graphId, nodeId;
		
		try{
			graphId = service.convertStringId(graphIdS);
			nodeId = service.convertStringId(nodeIdS);
			lib.deleteNode(graphId, nodeId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException("graph or node not found");
		}catch(MyInvalidIdException e){
			throw new ForbiddenException("Invalid graph Id and/or node Id");
		}
		return;
	}
	
	@POST
	@Path("{graphId}/nodes/{nodeId}/neighbours")
	@Consumes({MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_XML})
	public Response createNeighbour(@PathParam("graphId") String graphIdS, @PathParam("nodeId") String nodeIdS, @Context UriInfo uriInfo, Neighbour neighbour) throws NotFoundException, ForbiddenException{
		Neighbour neighReturned;
		long graphId, nodeId;
		
		try{
			graphId = service.convertStringId(graphIdS);
			nodeId = service.convertStringId(nodeIdS);
			neighReturned = lib.createNeighbour(neighbour, graphId, nodeId);
		}
		catch(MyInvalidIdException e1){
			throw new ForbiddenException("Invalid graph Id and/or node Id");
		}
		catch(MyNotFoundException e2){
			throw new NotFoundException();
		}
		UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    	URI u = builder.path(Long.toString(neighReturned.getId())).build();
    	return Response.created(u).status(Status.CREATED).entity(neighReturned).build();
	}

	@GET
	@Path("{graphId}/nodes/{nodeId}/neighbours")
	@Produces({MediaType.APPLICATION_XML})
	public Set<Neighbour> getNeighbours(@PathParam("graphId") String graphIdS, @PathParam("nodeId") String nodeIdS) throws NotFoundException, ForbiddenException{
		Set<Neighbour> set;
		long graphId,nodeId;
		
		try
		{
			graphId = service.convertStringId(graphIdS);
			nodeId = service.convertStringId(nodeIdS);
			set = lib.getNeighbours(graphId, nodeId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id and/or node Id");
		}
		return set;
	}
	
	@GET
	@Path("{graphId}/nodes/{nodeId}/neighbours/{neighbourId}")
	@Produces({MediaType.APPLICATION_XML})
	public Neighbour getNeighbour(@PathParam("graphId") String graphIdS, @PathParam("nodeId") String nodeIdS, @PathParam("neighbourId") String neighbourIdS ) throws NotFoundException, ForbiddenException{
		Neighbour neighbour;
		long graphId, nodeId, neighbourId;
		
		try{
			graphId = service.convertStringId(graphIdS);
			nodeId = service.convertStringId(nodeIdS);
			neighbourId = service.convertStringId(neighbourIdS);
			neighbour = lib.getNeighbour(graphId, nodeId, neighbourId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id and/or node Id and/or neighbour Id");
		}
		return neighbour;
	}
	
	@PUT
	@Path("{graphId}/nodes/{nodeId}/neighbours/{neighbourId}")
	@Consumes({MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_XML})
	public Node updateNeighbour(@PathParam("graphId") String graphIdS, @PathParam("nodeId") String nodeIdS, @PathParam("neighbourId") String neighbourIdS, Neighbour neighbour) throws NotFoundException, BadRequestException, ForbiddenException{
		Node nodeReturned;
		long graphId, nodeId, neighbourId;
		
		try{
			graphId = service.convertStringId(graphIdS);
			nodeId = service.convertStringId(nodeIdS);
			neighbourId = service.convertStringId(neighbourIdS);
			nodeReturned = lib.updateNeighbour(neighbour, graphId, nodeId, neighbourId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id and/or node Id and/or neighbour Id");
		}
		catch(MyInvalidObjectException e3){
			throw new BadRequestException(e3.getMessage());
		}
		return nodeReturned;
	}
	
	@DELETE
	@Path("{graphId}/nodes/{nodeId}/neighbours/{neighbourId}")
	public void deleteNeighbour(@PathParam("graphId") String graphIdS, @PathParam("nodeId") String nodeIdS, @PathParam("neighbourId") String neighbourIdS) throws NotFoundException, ForbiddenException{
		long graphId, nodeId, neighbourId;
		
		try{
			graphId = service.convertStringId(graphIdS);
			nodeId = service.convertStringId(nodeIdS);
			neighbourId = service.convertStringId(neighbourIdS);
			lib.deleteNeighbour(graphId, nodeId, neighbourId);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new ForbiddenException("Invalid graph Id and/or node Id and/or neighbour Id");
		}
		return;
	}
	
	@GET
	@Path("{graphId}/reachability")
	@Produces({MediaType.APPLICATION_XML})
	public Reachability checkReachability(@PathParam("graphId") String graphIdS, @Context UriInfo uriInfo, @QueryParam("src") String srcName, @QueryParam("dst") String dstName, @QueryParam("dir") String direction) throws NotFoundException, BadRequestException{
		long graphId;
		Reachability result;

		try{
			graphId = service.convertStringId(graphIdS);
			service.checkValidDirection(direction);
			result = lib.checkReachability(graphId, srcName, dstName,direction);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new BadRequestException("Invalid graph Id ");
		}
		catch(MyInvalidDirectionException e3){
			throw new BadRequestException(e3.getMessage());
		}

		return result;
	}
	
	@GET
	@Path("{graphId}/paths")
	@Produces({MediaType.APPLICATION_XML})
	public it.polito.nffg.neo4j.jaxb.Paths getAllPaths(@PathParam("graphId") String graphIdS, @QueryParam("src") String srcName, @QueryParam("dst") String dstName, @QueryParam("dir") String direction) throws NotFoundException, BadRequestException{
		long graphId;
		it.polito.nffg.neo4j.jaxb.Paths paths; // = obFactory.createPaths();
		try{
			graphId = service.convertStringId(graphIdS);
			if(srcName == null || dstName == null || direction == null)
				throw new MyNotFoundException("Missing query parameters");
			service.checkValidDirection(direction);
			paths =  lib.findAllPathsBetweenTwoNodes(graphId, srcName, dstName,direction);
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new BadRequestException("Invalid graph Id ");
		}
		catch(MyInvalidDirectionException e3){
			throw new BadRequestException(e3.getMessage());
		}
		
		return paths;
	}

	@GET
	@Path("{graphId}/policy")
	@Produces({MediaType.APPLICATION_XML})
	public Policy checkPolicy(@PathParam("graphId") String graphIdS, @QueryParam("source") String srcName, @QueryParam("destination") String dstName, @QueryParam("middlebox") String middlebox, @QueryParam("type") String type) throws NotFoundException, BadRequestException{
		long graphId;
		boolean result;
		
		try{
			graphId = service.convertStringId(graphIdS);
			if(type.toLowerCase().compareTo("reachability") == 0)
				result = lib.checkReachability(graphId, srcName, dstName, "OUTGOING").isResult();
			else if(type.toLowerCase().compareTo("traversal") == 0){
				if(lib.checkReachability(graphId, srcName, middlebox, "OUTGOING").isResult())
					result = lib.checkReachability(graphId, middlebox, dstName, "OUTGOING").isResult();
				else
					result = false;
			}
			else if(type.toLowerCase().compareTo("isolation") == 0){
				if(lib.checkReachability(graphId, srcName, middlebox, "OUTGOING").isResult())
					result = !(lib.checkReachability(graphId, middlebox, dstName, "OUTGOING")).isResult();
				else
					result = true;
			}
			else
				throw new BadRequestException("Invalid type");
		}
		catch(MyNotFoundException e1){
			throw new NotFoundException();
		}
		catch(MyInvalidIdException e2){
			throw new BadRequestException("Invalid graph Id ");
		}
		
		Policy policy = obFactory.createPolicy();
		policy.setSource(srcName);
		policy.setDestination(dstName);
		policy.setType(PolicyTypes.valueOf(type.toUpperCase()));
		policy.setResult(result);
		if(type.compareTo("reachability") != 0)
			policy.setMiddlebox(middlebox);
		
		return policy;
	}
}