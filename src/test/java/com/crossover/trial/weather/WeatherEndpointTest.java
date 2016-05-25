package com.crossover.trial.weather;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.repo.WeatherRepository;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WeatherServer.class)
@WebAppConfiguration
public class WeatherEndpointTest
{
	@Autowired private WeatherRepository _repo;

	@Autowired private WeatherQueryEndpoint _query;

	@Autowired private WeatherCollectorEndpoint _update;

	private Gson _gson = new Gson();

	private DataPoint.Builder _dp;

	@Before
	public void setUp() throws Exception
	{
		_repo.init();
		_dp = DataPoint.builder().first(10).second(20).third(30).mean(22.0).count(10);
		_update.updateWeather("BOS", "wind", _gson.toJson(_dp.build()));
		_query.weather("BOS", "0").getEntity();
	}

	@Test
	public void testPing() throws Exception
	{
		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());
		assertEquals(5, pingResult.getAsJsonObject().get("iata_freq").getAsJsonObject().entrySet().size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGet() throws Exception
	{
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("BOS", "0").getEntity();
		assertEquals(ais.get(0).wind(), _dp.build());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetNearby() throws Exception
	{
		// check datasize response
		_update.updateWeather("JFK", "wind", _gson.toJson(_dp.build()));
		_dp.mean(40.0);
		_update.updateWeather("EWR", "wind", _gson.toJson(_dp.build()));
		_dp.mean(30.0);
		_update.updateWeather("LGA", "wind", _gson.toJson(_dp.build()));

		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("JFK", "200").getEntity();
		assertEquals(3, ais.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdate() throws Exception
	{
		DataPoint windDp = DataPoint.builder().first(10).second(20).third(30).mean(22.0).count(10).build();
		_update.updateWeather("BOS", "wind", _gson.toJson(windDp));
		_query.weather("BOS", "0").getEntity();

		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());

		DataPoint cloudCoverDp = DataPoint.builder().first(10).second(60).third(100).mean(50.0).count(4).build();
		_update.updateWeather("BOS", "cloudcover", _gson.toJson(cloudCoverDp));

		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("BOS", "0").getEntity();
		assertEquals(ais.get(0).wind(), windDp);
		assertEquals(ais.get(0).cloudCover(), cloudCoverDp);
	}

}