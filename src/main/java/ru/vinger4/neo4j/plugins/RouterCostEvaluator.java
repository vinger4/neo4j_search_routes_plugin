package ru.vinger4.neo4j.plugins;

import org.neo4j.graphdb.Relationship;

import java.util.Map;

public class RouterCostEvaluator
{
    Map<String, Double> rel_type_costs;

    public RouterCostEvaluator(Map<String, Double> cost)
    {
        rel_type_costs = cost;
    }

    public Double getCost(Relationship relationship)
    {
        return rel_type_costs.get(relationship.getType().name());
    }
}