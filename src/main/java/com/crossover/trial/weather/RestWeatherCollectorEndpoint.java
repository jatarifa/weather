package com.crossover.trial.weather;

import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
	@Autowired private ObjectMapper mapper;

	@Autowired private ApplicationContext appContext;

	@Autowired private WeatherRepository repo;

	/**
	 * A liveliness check for the collection endpoint.
	 *
	 * @return always 'ready'
	 */
	@Override
	@GET
	@Path("/ping")
	public Response ping()
	{
		return Response.status(Response.Status.OK).entity("ready").build();
	}

	/**
	 * Update the airports atmospheric information for a particular pointType with json formatted data point
	 * information.
	 *
	 * @param iataCode the 3 letter airport code
	 * @param pointType the point type, {@link DataPointType} for a complete list
	 * @param datapointJson a json dict containing mean, first, second, thrid and count keys
	 *
	 * @return HTTP Response code
	 */
	@Override
	@POST
	@Path("/weather/{iata}/{pointType}")
	public Response updateWeather(@PathParam("iata") String iataCode, @PathParam("pointType") String pointType,
			String dataPointJson)
	{
		try
		{
			DataPointType type = DataPointType.valueOf(pointType.toUpperCase());

			if (!repo.findAirport(iataCode).isPresent())
				return Response.status(Response.Status.NOT_FOUND).build();
			else
			{
				repo.addDataPoint(iataCode, type, mapper.readValue(dataPointJson, DataPoint.class));
				return Response.status(Response.Status.OK).build();
			}
		}
		catch (Exception e)
		{
			log.error("UpdateWeather error.", e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	/**
	 * Return a list of known airports as a json formatted list
	 *
	 * @return HTTP Response code and a json formatted list of IATA codes
	 */
	@Override
	@GET
	@Path("/airports")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAirports()
	{
		Set<String> iatas = repo.getAirportCodes();
		return Response.status(Response.Status.OK).entity(iatas).build();
	}

	/**
	 * Retrieve airport data, including latitude and longitude for a particular airport
	 *
	 * @param iata the 3 letter airport code
	 * @return an HTTP Response with a json representation of {@link AirportData}
	 */
	@Override
	@GET
	@Path("/airport/{iata}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAirport(@PathParam("iata") String iata)
	{
		return repo.findAirport(iata).map(Response.status(Response.Status.OK)::entity)
				.orElse(Response.status(Response.Status.NOT_FOUND)).build();
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
	@POST
	@Path("/airport/{iata}/{lat}/{long}")
	public Response addAirport(@PathParam("iata") String iata, @PathParam("lat") String latString,
			@PathParam("long") String longString)
	{
		try
		{
			AirportData airport = AirportData.builder().iata(iata.toUpperCase()).lat(Double.valueOf(latString))
					.lon(Double.valueOf(longString)).build();

			repo.addAirport(airport);
			return Response.status(Response.Status.OK).build();
		}
		catch (WeatherException e)
		{
			log.error("Error adding airport.", e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	/**
	 * Add a new airport to the known airport list.
	 *
	 * @return HTTP Response code for the add operation
	 */
	@Override
	@POST
	@Path("/airport")
	public Response addAirport(String airportDataJson)
	{
		try
		{
			AirportData data = mapper.readValue(airportDataJson, AirportData.class);
			data = data.withIata(data.iata().toUpperCase()).withIcao(data.icao().toUpperCase());

			repo.addAirport(data);
			return Response.status(Response.Status.OK).build();
		}
		catch (Exception e)
		{
			log.error("Error adding airport.", e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	/**
	 * Remove an airport from the known airport list
	 *
	 * @param iata the 3 letter airport code
	 * @return HTTP Response code for the delete operation
	 */
	@Override
	@DELETE
	@Path("/airport/{iata}")
	public Response deleteAirport(@PathParam("iata") String iata)
	{
		repo.deleteAirport(iata);
		return Response.status(Response.Status.OK).build();
	}

	/**
	 * Stops the server
	 */
	@Override
	@GET
	@Path("/exit")
	public Response exit()
	{
		log.info("Closing app.");
		SpringApplication.exit(appContext, () -> 0);

		return Response.noContent().build();
	}
}
