package com.crossover.trial.weather;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import com.crossover.trial.weather.loader.AirportLoader;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.repo.WeatherRepository;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.util.Set;

import org.junit.Before;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WeatherServer.class)
@WebAppConfiguration
@IntegrationTest("server.port:9090")
public class WeatherRestTests 
{
	private RestTemplate rest = new TestRestTemplate();

	@Autowired
	private WeatherRepository repo;
	
	@Value("${server.port}")
	int port;
		
	private String getBase()
	{
		return "http://localhost:" + port;
	}
	
	@Before
	public void init() throws Exception
	{
		repo.init();
	}

    @Test
    public void collectPing() 
    {
    	String reply = rest.getForEntity(getBase() + "/collect/ping", String.class).getBody();
    	assertEquals("ready", reply);
    }

    @Test
    public void collectUpdateWeather() 
    {
    	DataPoint p = new DataPoint.Builder().withFirst(1).withSecond(2).withThird(3).withMean(2).withCount(1).build();
    	assertEquals(HttpStatus.OK, rest.postForEntity(getBase() + "/collect/weather/BOS/humidity", p, String.class).getStatusCode());
    	
    	AirportData airport = rest.getForEntity(getBase() + "/collect/airport/BOS", AirportData.class).getBody();
    	assertNotNull(airport);
    	assertEquals(p, airport.getAtmosphericInformation().getHumidity());
    }
    
    @Test
    public void collect() 
    {
    	DataPoint p = new DataPoint.Builder().withFirst(1).withSecond(2).withThird(3).withMean(2).withCount(1).build();
    	assertEquals(HttpStatus.OK, rest.postForEntity(getBase() + "/collect/weather/BOS/humidity", p, String.class).getStatusCode());
    	
    	AirportData airport = rest.getForEntity(getBase() + "/collect/airport/BOS", AirportData.class).getBody();
    	assertNotNull(airport);
    	assertEquals(p, airport.getAtmosphericInformation().getHumidity());
    }   
    
    
    
    
    
    
    
    @Test
	public void testLoader() throws Exception
	{
		String dat = WeatherRestTests.class.getResource("/airports.dat").getFile();
		AirportLoader al = new AirportLoader();
        al.upload(new FileReader(dat));
        
        Set<String> airports = rest.exchange(getBase() + "/collect/airports", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Set<String>>() {}).getBody();
        assertNotNull(airports);
        assertEquals(10, airports.size());
	}
}
