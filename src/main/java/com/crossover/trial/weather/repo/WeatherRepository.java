package com.crossover.trial.weather.repo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;

@Repository
public class WeatherRepository implements InitializingBean
{    
    /** all known airports */
    private MapPSet<AirportData> airportData = HashTreePSet.empty();

    private HashPMap<Double, Integer> radiusFreq = HashTreePMap.empty();

    public Set<AirportData> getAirportData() 
    {
    	return airportData;
	}
    
    public Map<Double, Integer> getRadiusFreq() 
    {
    	return radiusFreq;
	}
    
    public Set<String> getAirports()
    {
    	return airportData.stream().map(a -> a.getIata()).collect(Collectors.toSet());
    }
    
    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or null if not found
     */
    public Optional<AirportData> findAirportData(String iataCode) 
    {
        return airportData.stream().filter(ap -> ap.getIata().equals(iataCode.toUpperCase())).findFirst();
    }
    
    /**
     * Update the airports weather data with the collected data.
     *
     * @param iataCode the 3 letter IATA code
     * @param pointType the point type {@link DataPointType}
     * @param dp a datapoint object holding pointType data
     *
     */
    public void addDataPoint(String iataCode, DataPointType pointType, DataPoint dp) 
    {
    	findAirportData(iataCode).ifPresent(a -> a.getAtmosphericInformation().updateAtmosphericInformation(pointType, dp));
    }

    /**
     * Add a new known airport to our list.
     *
     * @param airport
     */
    public void addAirport(AirportData ad) 
    {
    	ad.setIata(ad.getIata().toUpperCase());
    	AirportData.validateData(ad);
    	
    	Optional<AirportData> old = findAirportData(ad.getIata());
    	if(old.isPresent())
    		old.get().copyFrom(ad);
    	else
    		airportData = airportData.plus(ad);
    }   
	
    /**
     * Delete a new known airport to our list.
     *
     * @param iataCode
     *
     * @return the deleted airport
     */
    public AirportData deleteAirport(String iataCode)
    {
    	AirportData a = findAirportData(iataCode).get();
    	airportData = airportData.minus(a);
    	
    	return a;
    }
    
    /**
     * Records information about how often requests are made
     *
     * @param iata an iata code
     * @param radius query radius
     */
    public void updateRequestFrequency(String iata, Double radius) 
    {
        findAirportData(iata).ifPresent(d -> d.incrementRequestFrecuency());
        radiusFreq = radiusFreq.plus(radius, radiusFreq.getOrDefault(radius, 0) + 1);    
    }
    
    public void init() 
    {
        airportData = HashTreePSet.empty();
        radiusFreq = HashTreePMap.empty();
        
        addAirport(getAirportDataMock("BOS", 42.364347, -71.005181));
        addAirport(getAirportDataMock("EWR", 40.6925, -74.168667));
        addAirport(getAirportDataMock("JFK", 40.639751, -73.778925));
        addAirport(getAirportDataMock("LGA", 40.777245, -73.872608));
        addAirport(getAirportDataMock("MMU", 40.79935, -74.4148747));
    }
    
    private AirportData getAirportDataMock(String iata, double lat, double lon)
    {
    	AirportData ad = new AirportData();
    	ad.setIata(iata);
    	ad.setLat(lat);
    	ad.setLon(lon);
    	
    	return ad;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception 
    {
    	init();
    }
}
