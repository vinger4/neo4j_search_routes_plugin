package ru.vinger4.neo4j.plugins;

import com.google.gson.Gson;
import org.neo4j.graphdb.*;
import org.neo4j.server.plugins.*;
import org.neo4j.server.rest.repr.Representation;
import org.neo4j.server.rest.repr.ValueRepresentation;

import java.util.*;

@Description("An extension to the Neo4j Server for find shortest path")
public class FindShortestPath extends ServerPlugin
{
	public FindShortestPath()
	{
	}

    @Name("get_shortest_path")
    @Description("Get shortest path by two ids, ")
    @PluginTarget(GraphDatabaseService.class)
    public Representation getShortestPath
            (
                    @Source GraphDatabaseService graphDb,
                    @Description("The id of the node from")
                    @Parameter(name = "node_from") Node nodeFrom,
                    @Description("The id of the node to")
                    @Parameter(name = "node_to") Node nodeTo,
                    @Description("Array of relationship types")
                    @Parameter(name = "relationship_types") List<String> relationshipTypes,
                    @Description("Array of costs for relationship types")
                    @Parameter(name = "relationship_costs") List<Double> relationshipCosts,
                    @Description("Sign for return only one route")
                    @Parameter(name = "only_one_route") boolean onlyOneRoute,
                    @Description("Soft timeout in milliseconds")
                    @Parameter(name = "soft_timeout") Integer softTimeout,
                    @Description("Max cost when search are stopped")
                    @Parameter(name = "max_cost") Double maxCost
            )
    {
        List<List<Node>> shortestPaths;
        RoutersFinding routersFinding = new RoutersFinding(graphDb);
	    Map<String, Double> costs = new HashMap<>();
	    for (int i = 0; i < relationshipTypes.size(); i++)
	    {
			costs.put(relationshipTypes.get(i), relationshipCosts.get(i));
	    }
	    shortestPaths = routersFinding.getShortestPaths(nodeFrom, nodeTo, relationshipTypes, softTimeout, costs,
			    onlyOneRoute, maxCost);

	    List<List<Map<String, Object>>> pathsForJson = makeJsonForEachNode(graphDb, shortestPaths);
	    Gson gson = new Gson();
        String resJSON = gson.toJson(pathsForJson, pathsForJson.getClass());
        return ValueRepresentation.string(resJSON);
    }

	public List<List<Map<String, Object>>> makeJsonForEachNode(GraphDatabaseService graphDb,
	                                                           List<List<Node>> shortestPaths)
	{
		Transaction txRead = graphDb.beginTx();
		Map<String, Object> nodeAsNode;
		List<List<Map<String, Object>>> pathsForJson = new ArrayList<>();
		List<Map<String, Object>> pathForJson;
		for (List<Node> path: shortestPaths)
		{
			pathForJson = new ArrayList<>();
			for (Node node: path)
			{
				nodeAsNode = new HashMap<>();
				for (String propertyKey: node.getPropertyKeys())
				{
					nodeAsNode.put(propertyKey, node.getProperty(propertyKey));
				}
				pathForJson.add(nodeAsNode);
			}
			pathsForJson.add(pathForJson);
		}
		txRead.close();
		return pathsForJson;
	}
}
