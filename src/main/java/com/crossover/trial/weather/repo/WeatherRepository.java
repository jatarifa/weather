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
import com.crossover.trial.weather.model.AirportData.AirportDataBuilder;

@Repository
public class WeatherRepository implements InitializingBean
{    
	// Immutable and Thread-safe HashMap of airports
	private MapPSet<AirportData> airportData = HashTreePSet.empty();

	// Immutable and Thread-safe HashMap of frecuencies
    private HashPMap<Double, Integer> radiusFreq = HashTreePMap.empty();

    /**
     * Gets all the airports.
     *
     * @return set of airports
     */
    public Set<AirportData> getAirportData() 
    {
    	return airportData;
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
    public Set<String> getAirports()
    {
    	return airportData.stream().map(a -> a.getIata()).collect(Collectors.toSet());
    }
    
    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode as a string
     * @return airport data or empty if not found
     */
    public Optional<AirportData> findAirportData(String iataCode) 
    {
        return airportData.stream().filter(ap -> ap.getIata().equalsIgnoreCase(iataCode)).findFirst();
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
    	ad.validate();
    	airportData = findAirportData(ad.getIata())
    						.map(airportData::minus)
    						.orElse(airportData)
    						.plus(ad);
    }   

    /**
     * Delete a new known airport to our list.
     *
     * @param iataCode
     *
     */
    public void deleteAirport(String iataCode)
    {
    	findAirportData(iataCode).ifPresent(a -> airportData = airportData.minus(a));
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
    
    /**
     * Inits the mocked airports
     */
    public void init() 
    {
        airportData = HashTreePSet.empty();
        radiusFreq = HashTreePMap.empty();
		
        addAirport(buildData("BOS", 42.364347, -71.005181));
        addAirport(buildData("EWR", 40.6925,   -74.168667));
        addAirport(buildData("JFK", 40.639751, -73.778925));
        addAirport(buildData("LGA", 40.777245, -73.872608));
        addAirport(buildData("MMU", 40.79935,  -74.4148747));
    }
    
    private AirportData buildData(String iata, double lat, double lon)
    {
		return new AirportDataBuilder()
						.withIata(iata)
						.withLat(lat)
						.withLon(lon)
						.build();
    }
    
    /**
     * When server starts, init the mock airports;
     */
    @Override
    public void afterPropertiesSet() throws Exception 
    {
    	init();
    }
}
