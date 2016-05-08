package com.crossover.trial.weather;

import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.repo.WeatherRepository;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WeatherServer.class)
@WebAppConfiguration
public class WeatherEndpointTest 
{
	@Autowired
    private WeatherRepository _repo;
	
	@Autowired
    private WeatherQueryEndpoint _query;

	@Autowired
    private WeatherCollectorEndpoint _update;

    private Gson _gson = new Gson();

    private DataPoint _dp;
    
    @Before
    public void setUp() throws Exception 
    {
    	_repo.init();
        _dp = new DataPoint.Builder().withCount(10).withFirst(10).withSecond(20).withThird(30).withMean(22).build();
        _update.updateWeather("BOS", "wind", _gson.toJson(_dp));
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
        assertEquals(ais.get(0).getWind(), _dp);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testGetNearby() throws Exception 
    {
        // check datasize response
        _update.updateWeather("JFK", "wind", _gson.toJson(_dp));
        _dp.setMean(40.0);
        _update.updateWeather("EWR", "wind", _gson.toJson(_dp));
        _dp.setMean(30.0);
        _update.updateWeather("LGA", "wind", _gson.toJson(_dp));

        List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("JFK", "200").getEntity();
        assertEquals(3, ais.size());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testUpdate() throws Exception 
    {
        DataPoint windDp = new DataPoint.Builder().withCount(10).withFirst(10).withSecond(20).withThird(30).withMean(22).build();
        _update.updateWeather("BOS", "wind", _gson.toJson(windDp));
        _query.weather("BOS", "0").getEntity();

        String ping = _query.ping();
        JsonElement pingResult = new JsonParser().parse(ping);
        assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());

        DataPoint cloudCoverDp = new DataPoint.Builder().withCount(4).withFirst(10).withSecond(60).withThird(100).withMean(50).build();
        _update.updateWeather("BOS", "cloudcover", _gson.toJson(cloudCoverDp));

        List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("BOS", "0").getEntity();
        assertEquals(ais.get(0).getWind(), windDp);
        assertEquals(ais.get(0).getCloudCover(), cloudCoverDp);
    }

}