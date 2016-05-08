package com.crossover.trial.weather.model;

import java.util.Arrays;
import java.util.List;

import com.crossover.trial.weather.exceptions.WeatherException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.AccessLevel;

// Getters, Setters, ToString and equals/hashcode autogenerated
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class AirportData 
{
	private static final List<String> DST = Arrays.asList("E", "A", "S", "O", "Z", "N", "U");

    private static final Double latMin = -90.0;
    private static final Double latMax = 90.0;
    private static final Double lonMin = -180.0;
    private static final Double lonMax = 180.0;
    
    /** earth radius in KM */
    private static final double R = 6372.8;

	private String name;
    private String city;
    private String country;
    private String iata;
    private String icao;
    private double lat;
    private double lon;
    private double alt;
    private double timezone;
    private String dst;
    
    @Setter(AccessLevel.NONE)
    private Integer requestFrequency = 0;
    
    @Setter(AccessLevel.NONE)
    private AtmosphericInformation atmosphericInformation;
    
    public AirportData() 
    {
        atmosphericInformation = new AtmosphericInformation();
        requestFrequency = 0;
	}
    
    public synchronized void incrementRequestFrecuency()
    {
    	requestFrequency++;
    }
    
    public synchronized void copyFrom(AirportData d)
    {
    	setName(d.getName());
    	setCity(d.getCity());
    	setCountry(d.getCountry());
    	setIata(d.getIata());
    	setIcao(d.getIcao());
    	setAlt(d.getAlt());
    	setLat(d.getLat());
    	setLon(d.getLon());
    	setDst(d.getDst());
    	requestFrequency = d.getRequestFrequency();
    	atmosphericInformation = d.getAtmosphericInformation().clone();
    }
    
    public AirportData clone()
    {
    	AirportData ad = new AirportData();
    	ad.setName(name);
    	ad.setCity(city);
    	ad.setCountry(country);
    	ad.setIata(iata);
    	ad.setIcao(icao);
    	ad.setAlt(alt);
    	ad.setLat(lat);
    	ad.setLon(lon);
    	ad.setTimezone(timezone);
    	ad.setDst(dst);
    	ad.requestFrequency = requestFrequency;
    	ad.atmosphericInformation = atmosphericInformation.clone();
    	
    	return ad;
    }
    
    /**
     * Haversine distance between two airports.
     *
     * @param ad airport 1
     * @return the distance in KM
     */
    public double calculateDistanceTo(AirportData ad) 
    {
        double deltaLat = Math.toRadians(ad.getLat() - lat);
        double deltaLon = Math.toRadians(ad.getLon() - lon);
        double a =  Math.pow(Math.sin(deltaLat / 2), 2) + Math.pow(Math.sin(deltaLon / 2), 2)
                	* Math.cos(lat) * Math.cos(ad.getLat());
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
    
    /**
     * Validate the fields of an object airport
     * 
     * @param ad the Airport to validate
     */
    public static void validateData(AirportData ad)
    {
		if(ad.getIata() != null && ad.getIata().trim().length() > 3)
			throw new WeatherException("IATA/FAA code not valid : " + ad.getIata());
		else if(ad.getIcao() != null && ad.getIcao().trim().length() > 4)
			throw new WeatherException("ICAO code not valid : " + ad.getIcao());
		else if(ad.getDst() != null && !DST.contains(ad.getDst().toUpperCase()))
			throw new WeatherException("DST code not valid : " + ad.getDst());
		else if(ad.getLat() < latMin || ad.getLat() > latMax)
			throw new WeatherException("Latitude error.: " + ad.getLat());
    	else if(ad.getLon() < lonMin || ad.getLon() > lonMax)
			throw new WeatherException("Longitude error.: " + ad.getLon());
    }
}
