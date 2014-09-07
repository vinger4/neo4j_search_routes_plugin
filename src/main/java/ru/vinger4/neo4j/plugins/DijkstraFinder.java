package ru.vinger4.neo4j.plugins;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.kernel.impl.util.SingleNodePath;

import java.util.*;

public class DijkstraFinder
{
	PathExpander expander;
	PathExpander expanderRevert;
	RouterCostEvaluator costEvaluator;
	Double maxCost;
	double softTimeout;
	boolean sign_one;

	public DijkstraFinder(PathExpander expander, PathExpander expanderRevert, RouterCostEvaluator costEvaluator,
	                      double softTimeout, Double maxCost)
	{
		this.costEvaluator = costEvaluator;
		this.expander = expander;
		this.expanderRevert = expanderRevert;
		this.softTimeout = softTimeout;
		this.maxCost = maxCost;
	}
	
	public Node getNextNode(TreeMap<Double, Node> nodeByCost, Map<Node, Double> currentNodesCosts)
	{
		if (nodeByCost.isEmpty())
		{
			return null;
		}
        Node nextNode = nodeByCost.firstEntry().getValue();
        while (!currentNodesCosts.containsKey(nextNode))
        {
            nodeByCost.remove(nodeByCost.firstEntry().getKey());
            if (nodeByCost.isEmpty())
            {
                return null;
            }
            nextNode = nodeByCost.firstEntry().getValue();
        }
        return nextNode;
	}

	/**
	 * Создание ключа для хранения стоимостей
	 * @param cost стоимость
	 * @param id id ноды
	 * @return ключ
	 */
	private double getKeyByCostAndID(Double cost, long id)
	{
		return cost * 1e10 + id;
	}
	
	public boolean nextStep(Map<Node, Node> prevNodes, Map<Node, Double> currentNodesCosts,
	                        TreeMap<Double, Node> nodeByCost, Set<Node> frontDirect,
                            Set<Node> frontOpposite, Map<Node, Double> nodesCosts, PathExpander expander,
                            Map<Node, Node> prevNodesOpposite, boolean onlyOptWay)
	{
		Node nextNode = getNextNode(nodeByCost, currentNodesCosts);
		
		// если дальше "некуда идти", то выход
		if (nextNode == null)
		{
			return false;
		}
		
		// условие завершение поиска: найденный след. узел входит в массив узлов противоположного направления
		if (!onlyOptWay)
		{
			if (frontOpposite.contains(nextNode))
			{
				return false;
			}
		}
		else
		{
			if (prevNodesOpposite.containsValue(nextNode))
			{
				prevNodes.put(null, nextNode);
				return false;
			}
		}

		Double currentCost = currentNodesCosts.get(nextNode);
		
		// если превышена максимально допустимая стоимость, то выход
		if (currentCost > maxCost)
		{
			currentNodesCosts.clear();
			return false;
		}
		Node otherNode;
		Double nextCost;
		// переход к следующим узлам
        frontDirect.remove(nextNode);
		for (Relationship rel: (Iterable<Relationship>) expander.expand(new SingleNodePath(nextNode), BranchState.NO_STATE))
		{
			otherNode = rel.getOtherNode(nextNode);
            nextCost = currentCost + this.costEvaluator.getCost(rel);
			// условие: данный путь дешевле того, что уже существует (или он является первым)
			if (!nodesCosts.containsKey(otherNode) || nodesCosts.containsKey(otherNode) && nextCost < nodesCosts.get(otherNode))
			{
				// здесь определяется стоимость перемещения по дуге
				currentNodesCosts.put(otherNode, nextCost);
				nodeByCost.put(this.getKeyByCostAndID(nextCost, otherNode.getId()), otherNode);
				nodesCosts.put(otherNode, nextCost);
				prevNodes.put(otherNode, nextNode);
                frontDirect.add(otherNode);
			}
		}
		nodeByCost.remove(this.getKeyByCostAndID(currentNodesCosts.get(nextNode), nextNode.getId()));
		currentNodesCosts.remove(nextNode);
		return true;
	}
	
	public List<List<Node>> findPathsAsNodes(Node nodeFrom, Node nodeTo, boolean onlyOptWay)
	{
		List<List<Node>> result = new ArrayList<>();

		Map<Node, Node> prevNodeDirect = new HashMap<>();
		Map<Node, Node> prevNodeRevert = new HashMap<>();
		Set<Node> frontDirect = new HashSet<>();
		Set<Node> frontRevert = new HashSet<>();
        frontDirect.add(nodeFrom);
        frontRevert.add(nodeTo);
		Map<Node, Double> currentNodesCostsDirect = new HashMap<>();
		Map<Node, Double> currentNodesCostsRevert = new HashMap<>();
		TreeMap<Double, Node> nodeByCostDirect = new TreeMap<>();
		TreeMap<Double, Node> nodeByCostRevert = new TreeMap<>();
		Map<Node, Double> nodesCostsDirect = new HashMap<>();
		Map<Node, Double> nodesCostsRevert = new HashMap<>();
		currentNodesCostsDirect.put(nodeFrom, 0.0);
		currentNodesCostsRevert.put(nodeTo, 0.0);
		nodeByCostDirect.put(this.getKeyByCostAndID(0.0, nodeFrom.getId()), nodeFrom);
		nodeByCostRevert.put(this.getKeyByCostAndID(0.0, nodeTo.getId()), nodeTo);
		nodesCostsDirect.put(nodeFrom, 0.0);
		nodesCostsRevert.put(nodeTo, 0.0);
		boolean finding1 = true;
		boolean finding2 = true;
		long t0 = System.currentTimeMillis();
		while (finding1 && finding2 && (System.currentTimeMillis() - t0 < this.softTimeout))
		{
			// forward step
			finding1 = nextStep(prevNodeDirect, currentNodesCostsDirect, nodeByCostDirect,
                    frontDirect, frontRevert, nodesCostsDirect, this.expander, prevNodeRevert, onlyOptWay);
            if (this.sign_one && ! finding1)
			{
				break;
			}
			// backward step
			finding2 = nextStep(prevNodeRevert, currentNodesCostsRevert, nodeByCostRevert,
                    frontRevert, frontDirect, nodesCostsRevert, this.expanderRevert, prevNodeDirect, onlyOptWay);
			if (this.sign_one && ! finding2)
			{
				break;
			}
		}

		Set<Node> intersection;
		if (onlyOptWay)
		{
			intersection = Utils.intersect(new HashSet<>(prevNodeDirect.values()), new HashSet<>(prevNodeRevert.values()));
		}
		else
		{
	        intersection = Utils.intersect(frontDirect, frontRevert);
		}
		if (!intersection.isEmpty())
		{
			for (Node node: intersection)
			{
				List<Node> path = new ArrayList<>();
				Node keyNodeDirect = node;
				while (keyNodeDirect != null)
				{
					path.add(0, keyNodeDirect);
					keyNodeDirect = prevNodeDirect.get(keyNodeDirect);
				}
				Node keyNodeRevert = node;
				while (true)
				{
					keyNodeRevert = prevNodeRevert.get(keyNodeRevert);
					if (keyNodeRevert == null)
					{
						break;
					}
					path.add(keyNodeRevert);
				}
				result.add(path);
			}
		}
		return result;
	}
}
