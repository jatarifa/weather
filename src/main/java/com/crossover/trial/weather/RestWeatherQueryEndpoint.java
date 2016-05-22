package com.crossover.trial.weather;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.repo.WeatherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * The query only API for the Weather Server App. This API is made available to the public internet.
 *
 * @author code test adminsitrator
 */

@Slf4j
@Component
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint
{
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private WeatherRepository repo;

    /**
     * Retrieve health and status information for the the query api. Returns information about how the number
     * of datapoints currently held in memory, the frequency of requests for each IATA code and the frequency of
     * requests for each radius.
     *
     * @return a JSON formatted dict with health information.
     */
    @Override
    @GET
    @Path("/ping")
	public String ping()
	{
		try
		{
	        Map<String, Object> retval = new HashMap<>();
	
	        long dataSize = repo.getAirportData().stream().filter(a -> a.getAtmosphericInformation().recentReaded()).count();
	        retval.put("datasize", dataSize);
	
	
	        int freqSize = repo.getAirportData().size();
	        Map<String, Double> freq = new HashMap<>();
	        if(freqSize != 0)
	        {
		        repo.getAirportData().stream().forEach(a -> {
		            double frac = (double)a.getRequestFrequency() / freqSize;
		            freq.put(a.getIata(), frac);        	
		        });
	        }
	        retval.put("iata_freq", freq);
	
	   
	        int m = repo.getRadiusFreq().keySet().stream().max(Double::compare).orElse(1000.0).intValue() + 1;
	        int[] hist = new int[m];
	        repo.getRadiusFreq().entrySet().stream().forEach(k -> {
	            int i = k.getKey().intValue() % 10;
	            hist[i] += k.getValue();
	        });        
	        retval.put("radius_freq", hist);
	        
	        return mapper.writeValueAsString(retval);
		}
		catch(Exception e)
		{
			log.error("Ping error.", e);
			return null;
		}
	}
	
    /**
     * Retrieve the most up to date atmospheric information from the given airport and other airports in the given
     * radius.
     *
     * @param iata the three letter airport code
     * @param radiusString the radius, in km, from which to collect weather data
     *
     * @return an HTTP Response and a list of {@link AtmosphericInformation} from the requested airport and
     * airports in the given radius
     */
    @Override
    @GET
    @Path("/weather/{iata}/{radius}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response weather(@PathParam("iata") String iata, 
    						@PathParam("radius") String radiusString)
	{
		Optional<AirportData> airport = repo.findAirportData(iata);
    	if(!airport.isPresent())
        	return Response.status(Response.Status.NOT_FOUND).build();

		double radius = Optional.ofNullable(radiusString).map(Double::valueOf).orElse(0.0);
		if(radius < 0)
        	return Response.status(Response.Status.NOT_FOUND).build();
		
        repo.updateRequestFrequency(iata, radius);

        List<AtmosphericInformation> retval;
        if (Double.compare(radius, 0.0) == 0) 
        	retval = Arrays.asList(airport.get().getAtmosphericInformation());
        else 
        {
        	retval = repo.getAirportData()
        				 .stream()
        				 .filter(a -> a.getAtmosphericInformation().notEmpty())
        				 .filter(a -> airport.get().calculateDistanceTo(a) <= radius)
        				 .map(a -> a.getAtmosphericInformation())
        				 .collect(Collectors.toList());
        	
        	if(retval.isEmpty())
        		retval = Arrays.asList(airport.get().getAtmosphericInformation());
        }
                
    	return Response.status(Response.Status.OK).entity(retval).build();
	}
}
