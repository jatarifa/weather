package com.crossover.trial.weather.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

/**
 * encapsulates sensor information for a particular location
 */
@ToString
@EqualsAndHashCode
public class AtmosphericInformation 
{
	/** milliseconds in a day */
    private static final long DAY_MILLIS = 86400000;

    /** temperature in degrees celsius */
    private Optional<DataPoint> temperature;

    /** wind speed in km/h */
    private Optional<DataPoint> wind;

    /** humidity in percent */
    private Optional<DataPoint> humidity;

    /** precipitation in cm */
    private Optional<DataPoint> precipitation;

    /** pressure in mmHg */
    private Optional<DataPoint> pressure;

    /** cloud cover percent from 0 - 100 (integer) */
    private Optional<DataPoint> cloudCover;

    /** the last time this data was updated, in milliseconds since UTC epoch */
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private long lastUpdateTime = 0;
    
    public AtmosphericInformation()
    {
    	this(null, null, null, null, null, null);
    }
    
    public AtmosphericInformation(DataPoint temperature, DataPoint wind, DataPoint humidity, DataPoint precipitation, DataPoint pressure, DataPoint cloudCover) 
    {
        this.temperature = Optional.ofNullable(temperature);
        this.wind = Optional.ofNullable(wind);
        this.humidity = Optional.ofNullable(humidity);
        this.precipitation = Optional.ofNullable(precipitation);
        this.pressure = Optional.ofNullable(pressure);
        this.cloudCover = Optional.ofNullable(cloudCover);
        
        if(notEmpty())
        	touch();
    }
    
	public DataPoint getTemperature() 
	{
		return temperature.orElse(null);
	}

	public DataPoint getWind() 
	{
		return wind.orElse(null);
	}

	public DataPoint getHumidity() 
	{
		return humidity.orElse(null);
	}

	public DataPoint getPrecipitation() 
	{
		return precipitation.orElse(null);
	}

	public DataPoint getPressure() 
	{
		return pressure.orElse(null);
	}

	public DataPoint getCloudCover() 
	{
		return cloudCover.orElse(null);
	}

	public long getLastUpdateTime() 
	{
		return lastUpdateTime;
	}
        
	public void setWind(DataPoint wind) 
	{
		this.wind = Optional.ofNullable(wind);
	}
	
	public void setCloudCover(DataPoint cloudCover) 
	{
		this.cloudCover = Optional.ofNullable(cloudCover);
	}
	
	public void setHumidity(DataPoint humidity) 
	{
		this.humidity = Optional.ofNullable(humidity);
	}
	
	public void setPrecipitation(DataPoint precipitation) 
	{
		this.precipitation = Optional.ofNullable(precipitation);
	}
	
	public void setPressure(DataPoint pressure) 
	{
		this.pressure = Optional.ofNullable(pressure);
	}
	
	public void setTemperature(DataPoint temperature) 
	{
		this.temperature = Optional.ofNullable(temperature);
	}
	
    public boolean notEmpty()
    {
    	return cloudCover.isPresent() || 
    		   humidity.isPresent() ||
               pressure.isPresent() ||
               precipitation.isPresent() ||
               temperature.isPresent() ||
               wind.isPresent();
    }
    
    public boolean recentReaded()
    {
    	return notEmpty() && lastUpdateTime > System.currentTimeMillis() - DAY_MILLIS;
    }

    public AtmosphericInformation clone()
    {
    	return new AtmosphericInformation(temperature.map(a -> a.clone()).orElse(null), 
    									  wind.map(a -> a.clone()).orElse(null), 
    									  humidity.map(a -> a.clone()).orElse(null), 
    									  precipitation.map(a -> a.clone()).orElse(null), 
    									  pressure.map(a -> a.clone()).orElse(null), 
    									  cloudCover.map(a -> a.clone()).orElse(null));
    }
    
    /**
     * update atmospheric information with the given data point for the given point type
     *
     * @param pointType the data point type as a string
     * @param dp the actual data point
     */
    public void updateAtmosphericInformation(DataPointType pointType, DataPoint point)
    {
    	Optional<DataPoint> dp = Optional.ofNullable(point.clone());

    	switch(pointType)
    	{
    		case WIND:
    	    	dp.filter(d -> d.getMean() >= 0).ifPresent(d -> wind = dp);
                break;
    		case TEMPERATURE:
    	    	dp.filter(d -> d.getMean() >= -50 && d.getMean() < 100).ifPresent(d -> temperature = dp);
                break;
    		case HUMIDITY:
    	    	dp.filter(d -> d.getMean() >= 0 && d.getMean() < 100).ifPresent(d -> humidity = dp);
                break;
    		case PRESSURE:
    	    	dp.filter(d -> d.getMean() >= 650 && d.getMean() < 800).ifPresent(d -> pressure = dp);
                break;
    		case CLOUDCOVER:
    	    	dp.filter(d -> d.getMean() >= 0 && d.getMean() < 100).ifPresent(d -> cloudCover = dp);
                break;
    		case PRECIPITATION:
    	    	dp.filter(d -> d.getMean() >=0 && d.getMean() < 100).ifPresent(d -> precipitation = dp);
                break;
    	}
    	
    	touch();
    }  
    
    private void touch()
    {
    	lastUpdateTime = System.currentTimeMillis();
    }
}
