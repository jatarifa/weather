package com.crossover.trial.weather.model;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

public interface DeepCloned extends Serializable
{
	@SuppressWarnings("unchecked")
	public default <T extends DeepCloned> T copy()
	{
		return SerializationUtils.clone((T)this);
	}
}
