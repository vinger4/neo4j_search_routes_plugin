package ru.vinger4.neo4j.plugins;

import java.util.*;

public class Utils
{
	public Utils()
	{
	}

	public static <T> Set<T> intersect(Set<? extends T> a, Set<? extends T> b)
	{
		Set<T> result = new HashSet<>();

		for (T t : a)
		{
			if (b.remove(t))
			{
				result.add(t);
			}
		}

		return result;
	}
}