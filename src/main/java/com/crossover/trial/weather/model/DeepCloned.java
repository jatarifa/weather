package com.crossover.trial.weather.model;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

public interface DeepCloned extends Serializable
{
	public static <T extends Serializable> T copy(T orig)
	{
		return SerializationUtils.clone(orig);
	}
}
