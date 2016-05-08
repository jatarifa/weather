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
import org.springframework.http.ResponseEntity;
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

    	assertTrue(!rest.postForEntity(getBase() + "/collect/weather/BOS/wrong", p, String.class).getStatusCode().is2xxSuccessful());
    	assertTrue(!rest.postForEntity(getBase() + "/collect/weather/non/wind", p, String.class).getStatusCode().is2xxSuccessful());
    	
    	assertEquals(HttpStatus.OK, rest.postForEntity(getBase() + "/collect/weather/BOS/humidity", p, String.class).getStatusCode());    	
    	AirportData airport = rest.getForEntity(getBase() + "/collect/airport/BOS", AirportData.class).getBody();
    	assertNotNull(airport);
    	assertEquals(p, airport.getAtmosphericInformation().getHumidity());
    }
    
    @Test
    public void collectGetAirports() 
    {
        Set<String> airports = rest.exchange(getBase() + "/collect/airports", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Set<String>>() {}).getBody();
        assertNotNull(airports);
        assertEquals(5, airports.size());
    }   
    
    @Test
    public void collectGetAirportByIATA() 
    {
    	ResponseEntity<AirportData> airport;
    	
        airport = rest.exchange(getBase() + "/collect/airport/ddd", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class);
        assertTrue(!airport.getStatusCode().is2xxSuccessful());
        
    	airport = rest.exchange(getBase() + "/collect/airport/BOS", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class);
        assertNotNull(airport.getBody());
    }      
    
    @Test
    public void collectAddAirportMin() 
    {
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaaa/10/100", null, String.class).getStatusCode().is2xxSuccessful());
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaa/-100/100", null, String.class).getStatusCode().is2xxSuccessful());
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaa/10/-190", null, String.class).getStatusCode().is2xxSuccessful());
    	
    	ResponseEntity<AirportData> airport;
    	assertTrue(rest.postForEntity(getBase() + "/collect/airport/aaa/90/180", null, String.class).getStatusCode().is2xxSuccessful());
    	airport = rest.exchange(getBase() + "/collect/airport/aaa", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class);
        assertTrue(airport.getBody() != null);
        assertTrue(airport.getBody().getIata().equals("AAA"));
        assertTrue(airport.getBody().getLat() == 90);
        assertTrue(airport.getBody().getLon() == 180);
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
