package com.synsere.util;

import static java.util.Objects.requireNonNull;

public final class Nulls
{

	private Nulls()
	{

	}

	public static boolean allNull(Object... objects)
	{
		if(requireNonNull(objects).length == 0)
		{
			return true;
		}

		for(Object object : objects)
		{
			if(object != null)
			{
				return false;
			}
		}
		return true;
	}

	public static boolean anyNull(Object... objects)
	{
		if(requireNonNull(objects).length == 0)
		{
			return true;
		}

		for(Object object : objects)
		{
			if(object == null)
			{
				return true;
			}
		}
		return false;
	}
}
