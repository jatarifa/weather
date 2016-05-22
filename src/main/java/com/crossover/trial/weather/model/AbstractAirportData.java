package com.crossover.trial.weather.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.crossover.trial.weather.exceptions.WeatherException;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@Value.Style(typeImmutable="")
@JsonSerialize(as = AirportData.class)
@JsonDeserialize(as = AirportData.class)
public abstract class AbstractAirportData 
{
	static final List<String> DST_LIST = Arrays.asList("E", "A", "S", "O", "Z", "N", "U");
    static final Double LAT_MIN = -90.0;
    static final Double LAT_MAX = 90.0;
    static final Double LON_MIN = -180.0;
    static final Double LON_MAX = 180.0;
    static final double R = 6372.8;

	public abstract String iata();
	public abstract double lat();
    public abstract double lon();
	@Nullable public abstract String icao();
    @Nullable public abstract String name();
    @Nullable public abstract String city();
    @Nullable public abstract String country();
    @Nullable public abstract Double alt();
    @Nullable public abstract Double timezone();
    @Nullable public abstract String dst();
    
    @Value.Auxiliary
    @Value.Default
    public AtmosphericInformation atmosphericInformation()
    {
    	return AtmosphericInformation.builder().build();
    }
 
    public Optional<AirportData> withDataPointToAtmosphereInformation(DataPointType pointType, DataPoint point)
    {
    	Optional<AtmosphericInformation> ai;
    	Optional<DataPoint> dp = Optional.ofNullable(point);
    	switch(pointType)
    	{
    		case WIND:
    	    	ai = dp.filter(d -> d.mean() >= 0)
    	    		   .map(atmosphericInformation()::withWind);
                break;
    		case TEMPERATURE:
    	    	ai = dp.filter(d -> d.mean() >= -50 && d.mean() < 100)
    	    	  	   .map(atmosphericInformation()::withTemperature);
                break;
    		case HUMIDITY:
    			ai = dp.filter(d -> d.mean() >= 0 && d.mean() < 100)
    	    	  	   .map(atmosphericInformation()::withHumidity);
                break;
    		case PRESSURE:
    			ai = dp.filter(d -> d.mean() >= 650 && d.mean() < 800)
    	    	  	   .map(atmosphericInformation()::withPressure);
                break;
    		case CLOUDCOVER:
    			ai = dp.filter(d -> d.mean() >= 0 && d.mean() < 100)
    	    	       .map(atmosphericInformation()::withCloudCover);
                break;
    		case PRECIPITATION:
    			ai = dp.filter(d -> d.mean() >=0 && d.mean() < 100)
    	    	       .map(atmosphericInformation()::withPrecipitation);
                break;
            default:
            	return null;
    	}
    	
    	return ai.map(a -> AirportData.builder()
    								  .from(this)
    								  .atmosphericInformation(a)
    								  .build());
    }
    
    public double calculateDistanceTo(AirportData ad) 
    {
        double deltaLat = Math.toRadians(ad.lat() - lat());
        double deltaLon = Math.toRadians(ad.lon() - lon());
        double a =  Math.pow(Math.sin(deltaLat / 2), 2) + Math.pow(Math.sin(deltaLon / 2), 2)
                	* Math.cos(lat()) * Math.cos(ad.lat());
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    @Value.Check
    protected void validate()
    {
    	checkIata();
    	checkIcao();
    	checkLatitude();
    	checkLongitude();
    	checkDst();
    }
    
    private void checkIata()
    {	
		if(iata().trim().length() > 3)
			throw new WeatherException("IATA/FAA code not valid : " + iata());
    }
    
    private void checkIcao()
    {
		if(icao() != null && icao().trim().length() > 4)
			throw new WeatherException("ICAO code not valid : " + icao());
    }
    
    private void checkDst()
    {
		if(dst() != null && !DST_LIST.contains(dst().toUpperCase()))
			throw new WeatherException("DST code not valid : " + dst());
    }
    
    private void checkLatitude()
    {
		if(Double.compare(lat(), LAT_MIN) < 0 || Double.compare(lat(), LAT_MAX) > 0)
			throw new WeatherException("Latitude error.: " + lat());	
    }
    
    private void checkLongitude()
    {
    	if(Double.compare(lon(), LON_MIN) < 0 || Double.compare(lon(), LON_MAX) > 0)
			throw new WeatherException("Longitude error.: " + lon());    	
    }
}
