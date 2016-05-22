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
	abstract int first();
	abstract int second();
	abstract int third();
	abstract double mean();
	abstract int count();
}
