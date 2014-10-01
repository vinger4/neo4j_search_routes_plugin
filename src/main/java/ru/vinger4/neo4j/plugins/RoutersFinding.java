package ru.vinger4.neo4j.plugins;

import org.neo4j.graphdb.*;

import java.util.*;

public class RoutersFinding
{
	GraphDatabaseService graphDb;

	public RoutersFinding(GraphDatabaseService graphDb)
	{
		this.graphDb = graphDb;
	}

	public List<List<Node>> getShortestPaths(Node nodeFrom, Node nodeTo, List<String> relationshipTypes, int softTimeout,
                                    Map<String, Double> relationshipCosts, boolean onlyOneRoute, double maxCost)
	{
		Transaction tx = graphDb.beginTx();
		PathExpanderBuilder relExpanderBuilder = PathExpanderBuilder.empty();
		for (String relType: relationshipTypes)
		{
			relExpanderBuilder = relExpanderBuilder.add(DynamicRelationshipType.withName(relType), Direction.OUTGOING);
		}
		PathExpander relExpander = relExpanderBuilder.build();

		DijkstraFinder pathFinder = new DijkstraFinder(relExpander, relExpander.reverse(),
				new RouterCostEvaluator(relationshipCosts), softTimeout, maxCost);
		List<List<Node>> paths = pathFinder.findPathsAsNodes(nodeFrom, nodeTo, onlyOneRoute);

		tx.close();
		return paths;
	}
}
