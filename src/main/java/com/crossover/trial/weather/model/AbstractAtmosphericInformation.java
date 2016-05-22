package com.crossover.trial.weather.model;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@Value.Style(typeImmutable="")
@JsonSerialize(as = AtmosphericInformation.class)
@JsonDeserialize(as = AtmosphericInformation.class)
public abstract class AbstractAtmosphericInformation 
{
    static final long DAY_MILLIS = 86400000;

    @Nullable public abstract DataPoint temperature();
    @Nullable public abstract DataPoint wind();
    @Nullable public abstract DataPoint humidity();
    @Nullable public abstract DataPoint precipitation();
    @Nullable public abstract DataPoint pressure();
    @Nullable public abstract DataPoint cloudCover();

    @JsonIgnore
    @Value.Auxiliary
    @Value.Default
    public long lastUpdateTime()
    {
    	return System.currentTimeMillis();
    }
    
    public boolean notEmpty()
    {
    	return cloudCover() != null || 
    		   humidity() != null ||
               pressure() != null ||
               precipitation() != null ||
               temperature() != null ||
               wind() != null;
    }
    
    public boolean recentReaded()
    {
    	return notEmpty() && lastUpdateTime() > System.currentTimeMillis() - DAY_MILLIS;
    }
}
