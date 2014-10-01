package ru.vinger4.neo4j.plugins;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class TestPluginSearchRoutesTempDb
{
	protected GraphDatabaseService graphDb;
    public Node n1 = null;
    public Node n2 = null;
    public Node n3 = null;

	@Before
	public void prepareTestDatabase()
	{
		try
		{
            File theDir = new File("test_neo4j_db/");
            if (!theDir.exists())
            {
                boolean result = theDir.mkdir();
                if (! result)
                {
                    throw new IOException();
                }
            }
            else
            {
                FileUtils.cleanDirectory(new File("test_neo4j_db/"));
            }
		}
		catch (IOException e)
		{
			Assert.assertTrue("IOException: " + e.getMessage(), false);
		}
		graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder("test_neo4j_db/")
				.setConfig(GraphDatabaseSettings.keep_logical_logs, "false")
				.setConfig(GraphDatabaseSettings.allow_store_upgrade, "true")
				.newGraphDatabase();
		Transaction txClearGraph = graphDb.beginTx();
		try
		{
			GlobalGraphOperations ggo = GlobalGraphOperations.at(graphDb);
			for (Node nToDelete: ggo.getAllNodes())
			{
				for (Relationship relToDelete: nToDelete.getRelationships())
				{
					relToDelete.delete();
				}
				nToDelete.delete();
			}
			txClearGraph.success();
		}
		catch (Exception e)
		{
			txClearGraph.failure();
			Assert.assertTrue("Error while clear embedded db (test db)", false);
		}
        finally
        {
            txClearGraph.close();
        }

		Transaction tx_create_graph = graphDb.beginTx();
		try
		{
			n1 = graphDb.createNode();
			n1.setProperty("name", "node1");
			n1.setProperty("type", "type1");
			n2 = graphDb.createNode();
			n2.setProperty("name", "node2");
			n2.setProperty("type", "type2");
			n3 = graphDb.createNode();
			n3.setProperty("name", "node3");
			n3.setProperty("type", "type3");
			n1.createRelationshipTo(n2, DynamicRelationshipType.withName("relType1"));
			n2.createRelationshipTo(n3, DynamicRelationshipType.withName("relType2"));
			n1.createRelationshipTo(n3, DynamicRelationshipType.withName("relType3"));
			tx_create_graph.success();
		}
		catch (Exception e)
		{
			tx_create_graph.failure();
			Assert.assertTrue("Error while create db (test db)", false);
		}
		tx_create_graph.close();
	}

	@After
	public void destroyTestDatabase()
	{
		if (graphDb != null)
		{
			graphDb.shutdown();
		}
	}

	@Test
	public void testFindShortestPaths()
	{
		if (n1 == null || n2 == null || n3 == null)
		{
			Assert.assertTrue("Nodes are not created for test db.", false);
		}
		FindShortestPath fsp = new FindShortestPath();
		fsp.getShortestPath(graphDb, n1, n3, Arrays.asList("relType1", "relType2", "relType3"),
				Arrays.asList(1.0, 1.0, 1.0), false, 5000, 1000.0);
		Map<String, Double> costs = new HashMap<>();
		costs.put("relType1", 1.0);
		costs.put("relType2", 1.0);
		costs.put("relType3", 1.0);
		RoutersFinding routersFinding = new RoutersFinding(graphDb);
		List<List<Node>> shortestPaths = routersFinding.getShortestPaths(n1, n3, Arrays.asList("relType1", "relType2", "relType3"),
				5000, costs, true, 1000.0);
		Assert.assertEquals("Result isn't true", shortestPaths, Arrays.asList(Arrays.asList(n1, n3)));
		costs.put("relType3", 3.0);
		shortestPaths = routersFinding.getShortestPaths(n1, n3, Arrays.asList("relType1", "relType2", "relType3"),
				5000, costs, true, 1000.0);
		Assert.assertEquals("Result isn't true", shortestPaths, Arrays.asList(Arrays.asList(n1, n2, n3)));
	}
}