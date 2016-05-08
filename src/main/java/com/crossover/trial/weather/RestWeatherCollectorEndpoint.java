package com.crossover.trial.weather;

import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.crossover.trial.weather.exceptions.WeatherException;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;
import com.crossover.trial.weather.repo.WeatherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * The interface shared to airport weather collection systems.
 *
 * @author code test administartor
 */

@Slf4j
@Component
@Path("/collect")
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint
{
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
    private ApplicationContext appContext;
 
    @Autowired
    private WeatherRepository repo;
    
    /**
     * A liveliness check for the collection endpoint.
     *
     * @return always 'ready'
     */
    @Override
    public Response ping() 
    {
    	return Response.status(Response.Status.OK).entity("ready").build();
    }

    /**
     * Update the airports atmospheric information for a particular pointType with
     * json formatted data point information.
     *
     * @param iataCode the 3 letter airport code
     * @param pointType the point type, {@link DataPointType} for a complete list
     * @param datapointJson a json dict containing mean, first, second, thrid and count keys
     *
     * @return HTTP Response code
     */
    @Override
	public Response updateWeather(@PathParam("iata") String iataCode, 
								  @PathParam("pointType") String pointType,
								  String dataPointJson) 
	{
    	try
    	{
	    	DataPointType type = DataPointType.valueOf(pointType.toUpperCase());
	    	if(type != null)
	    	{
				repo.addDataPoint(iataCode, type, mapper.readValue(dataPointJson, DataPoint.class));
				return Response.status(Response.Status.OK).build();
	    	}
	    	else
	    	{
	    		log.error("DataPoint Type doesn't exists.: {}", pointType);
	    		return Response.status(Response.Status.BAD_REQUEST).build();
	    	}
    	}
    	catch(Exception e)
    	{
    		log.error("UpdateWeather error: {}", e.getMessage());
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
	}
    
    /**
     * Return a list of known airports as a json formatted list
     *
     * @return HTTP Response code and a json formatted list of IATA codes
     */
    @Override
    public Response getAirports() 
    {
    	Set<String> aps = repo.getAirports();
		return Response.status(Response.Status.OK).entity(aps).build();
    }
    
    /**
     * Retrieve airport data, including latitude and longitude for a particular airport
     *
     * @param iata the 3 letter airport code
     * @return an HTTP Response with a json representation of {@link AirportData}
     */
    @Override
    public Response getAirport(@PathParam("iata") String iata) 
    {
    	AirportData ad = repo.findAirportData(iata).orElse(null);
		return Response.status(Response.Status.OK).entity(ad).build();
    }

    
    /**
     * Add a new airport to the known airport list.
     *
     * @param iata the 3 letter airport code of the new airport
     * @param latString the airport's latitude in degrees as a string [-90, 90]
     * @param longString the airport's longitude in degrees as a string [-180, 180]
     * @return HTTP Response code for the add operation
     */
    @Override
    public Response addAirport(@PathParam("iata") String iata,
    						   @PathParam("lat")  String latString,
    						   @PathParam("long") String longString)
    {
    	try
    	{
    		AirportData ad = new AirportData();
    		ad.setLat(Double.valueOf(latString));
    		ad.setLon(Double.valueOf(longString));
        	
    		repo.addAirport(ad);
    		return Response.status(Response.Status.OK).build();
    	}
    	catch(WeatherException e)
    	{
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    }

    /**
     * Add a new airport to the known airport list.
     *
     * @return HTTP Response code for the add operation
     */
    @Override
    public Response addAirport(String airportDataJson)
    {
    	try
    	{
    		AirportData data = mapper.readValue(airportDataJson, AirportData.class);
    		repo.addAirport(data);
    		return Response.status(Response.Status.OK).build();
    	}
    	catch(Exception e)
    	{
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    }
    
    /**
     * Remove an airport from the known airport list
     *
     * @param iata the 3 letter airport code
     * @return HTTP Response code for the delete operation
     */
    @Override
    public Response deleteAirport(@PathParam("iata") String iata) 
    {
    	repo.deleteAirport(iata);
		return Response.status(Response.Status.OK).build();
    }    

    @Override
    public Response exit() 
    {
    	log.info("Closing app.");
		SpringApplication.exit(appContext, () -> 0);
		
		return Response.noContent().build();
    }
}
