package com.crossover.trial.weather.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@Value.Style(typeImmutable="")
@JsonSerialize(as = DataPoint.class)
@JsonDeserialize(as = DataPoint.class)
public interface AbstractDataPoint 
{
	abstract Integer first();
	abstract Integer second();
	abstract Integer third();
	abstract Double mean();
	abstract Integer count();
}
