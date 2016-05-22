package com.crossover.trial.weather.repo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;

@Repository
public class WeatherRepository implements InitializingBean
{    
	// Immutable and Thread-safe HashMap of airports
	private HashPMap<AirportData, Integer> airportData = HashTreePMap.empty();

	// Immutable and Thread-safe HashMap of frecuencies
    private HashPMap<Double, Integer> radiusFreq = HashTreePMap.empty();

    /**
     * Gets all the airports data.
     *
     * @return set of airports
     */
    public Set<Map.Entry<AirportData, Integer>> getAirportData() 
    {
    	return airportData.entrySet();
	}

    /**
     * Gets all the airports.
     *
     * @return set of airports
     */
    public Set<AirportData> getAirports() 
    {
    	return airportData.keySet();
	}

    /**
     * Gets the radius frequency.
     *
     * @return set of frecuencies
     */
    public Map<Double, Integer> getRadiusFreq() 
    {
    	return radiusFreq;
	}
    
    /**
     * Gets all the iata codes from the airports.
     *
     * @return set of iata codes
     */
    public Set<String> getAirportCodes()
    {
    	return airportData.keySet().stream().map(a -> a.iata()).collect(Collectors.toSet());
    }
    
    /**
     * Given an iataCode find the airport
     *
     * @param iataCode as a string
     * @return airport or empty if not found
     */
    public Optional<AirportData> findAirport(String iataCode) 
    {
    	return findAirportData(iataCode).map(k -> k.getKey());
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
    	Optional<Map.Entry<AirportData, Integer>> data = findAirportData(iataCode);
    	
    	data.flatMap(a -> a.getKey()
    				       .withDataPointToAtmosphereInformation(pointType, dp))
    		.ifPresent(a -> airportData = airportData.plus(a, data.get().getValue()));
    }

    /**
     * Add a new known airport to our list.
     *
     * @param airport
     */
    public void addAirport(AirportData ad) 
    {
    	airportData = findAirportData(ad.iata())
    						.map(a -> airportData.plus(ad, a.getValue()))
    						.orElse(airportData.plus(ad, 0));
    }   

    /**
     * Delete a new known airport to our list.
     *
     * @param iataCode
     *
     */
    public void deleteAirport(String iataCode)
    {
    	findAirportData(iataCode).ifPresent(a -> airportData = airportData.minus(a.getKey()));
    }
    
    /**
     * Records information about how often requests are made
     *
     * @param iata an iata code
     * @param radius query radius
     */
    public void updateRequestFrequency(String iata, Double radius) 
    {
        findAirportData(iata).ifPresent(d -> {
        	airportData = airportData.plus(d.getKey(), d.getValue() + 1);
            radiusFreq = radiusFreq.plus(radius, radiusFreq.getOrDefault(radius, 0) + 1);
        });
    }
    
    /**
     * Given an iataCode find the airport data (airport and frequency)
     *
     * @param iataCode as a string
     * @return airport data or empty if not found
     */
    protected Optional<Entry<AirportData, Integer>> findAirportData(String iataCode) 
    {
        return airportData.entrySet()
        				  .stream()
        				  .filter(ap -> ap.getKey()
        						   		  .iata()
        						   		  .equalsIgnoreCase(iataCode))
        				  .findFirst();
    }
   
    /**
     * When server starts, init the mock airports;
     */
    @Override
    public void afterPropertiesSet() throws Exception 
    {
    	init();
    }
    
    public void init() 
    {
        airportData = HashTreePMap.empty();
        radiusFreq = HashTreePMap.empty();
		
        addAirport(buildData("BOS", 42.364347, -71.005181));
        addAirport(buildData("EWR", 40.6925,   -74.168667));
        addAirport(buildData("JFK", 40.639751, -73.778925));
        addAirport(buildData("LGA", 40.777245, -73.872608));
        addAirport(buildData("MMU", 40.79935,  -74.4148747));
    }
    
    protected AirportData buildData(String iata, double lat, double lon)
    {
		return AirportData.builder().iata(iata).lat(lat).lon(lon).build();
    }
}
