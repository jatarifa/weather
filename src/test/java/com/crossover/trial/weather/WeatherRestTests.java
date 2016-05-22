package com.crossover.trial.weather;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
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
import com.crossover.trial.weather.model.AirportData.AirportDataBuilder;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPoint.DataPointBuilder;
import com.crossover.trial.weather.repo.WeatherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WeatherServer.class)
@WebAppConfiguration
@IntegrationTest("server.port:9090")
public class WeatherRestTests 
{
	private RestTemplate rest = new TestRestTemplate();

	// Internal repository
	@Autowired
	private WeatherRepository repo;
	
	// Json Mapper
	@Autowired
	private ObjectMapper mapper;
	
	// Test Server port
	@Value("${server.port}")
	int port;
		
	private String getBase()
	{
		// Base URL
		return "http://localhost:" + port;
	}
	
	@Before
	public void init() throws Exception
	{
		// Always init the airports before tests
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
    	DataPointBuilder p = new DataPointBuilder().withFirst(1).withSecond(2).withThird(3).withMean(2).withCount(1);
    	DataPoint dp = p.build();

    	// Bad calls (DataPointType wrong)
    	assertTrue(!rest.postForEntity(getBase() + "/collect/weather/BOS/wrong", dp, String.class).getStatusCode().is2xxSuccessful());
    	// Bad calls (Iata not existing)
    	assertTrue(!rest.postForEntity(getBase() + "/collect/weather/non/wind", dp, String.class).getStatusCode().is2xxSuccessful());
    	
    	// Good call
    	assertEquals(HttpStatus.OK, rest.postForEntity(getBase() + "/collect/weather/BOS/humidity", dp, String.class).getStatusCode());    	
    	AirportData airport = rest.getForEntity(getBase() + "/collect/airport/BOS", AirportData.class).getBody();
    	assertNotNull(airport);
    	assertEquals(dp, airport.getAtmosphericInformation().getHumidity());
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
    	
    	// Bad call (iata doesn't exists)
        airport = rest.exchange(getBase() + "/collect/airport/ddd", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class);
        assertTrue(!airport.getStatusCode().is2xxSuccessful());
        
    	// Good call
    	airport = rest.exchange(getBase() + "/collect/airport/BOS", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class);
        assertNotNull(airport.getBody());
    }      
    
    @Test
    public void collectAddAirportMin() 
    {
    	// Bad call (iata length > 3)
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaaa/10/100", null, String.class).getStatusCode().is2xxSuccessful());
    	// Bad call (lat not in correct range)
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaa/-100/100", null, String.class).getStatusCode().is2xxSuccessful());
    	// Bad call (lon not in correct range)
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaa/10/-190", null, String.class).getStatusCode().is2xxSuccessful());
    	
    	// Good call
    	ResponseEntity<AirportData> airport;
    	assertTrue(rest.postForEntity(getBase() + "/collect/airport/aaa/90/180", null, String.class).getStatusCode().is2xxSuccessful());
    	airport = rest.exchange(getBase() + "/collect/airport/aaa", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class);
        assertTrue(airport.getBody() != null);
        assertTrue(airport.getBody().getIata().equals("AAA"));
        assertTrue(airport.getBody().getLat() == 90);
        assertTrue(airport.getBody().getLon() == 180);
    }       
    
    @Test
    public void collectAddAirport() 
    {
    	AirportDataBuilder b = new AirportDataBuilder();
    	b.withName("Barajas");
    	b.withCity("Madrid");
    	b.withCountry("Spain");
    	b.withIata("KKFKFK");  //bad
    	b.withIcao("MADRDD");  //bad
    	b.withAlt(1000.23);
    	b.withLat(10.2);
    	b.withLon(33.22);
    	b.withTimezone(200);
    	b.withDst("3");        //bad
    	
    	// Bad call (iata length > 3)
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport", b.build(), String.class).getStatusCode().is2xxSuccessful());

    	// Bad call (icao length > 4)
    	b.withIata("MAD");
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport", b.build(), String.class).getStatusCode().is2xxSuccessful());

    	// Bad call (dst doesn't exists)
    	b.withIcao("MADR");
    	assertTrue(!rest.postForEntity(getBase() + "/collect/airport", b.build(), String.class).getStatusCode().is2xxSuccessful());

    	// Good call (insert new airport)
    	b.withDst("E");
    	ResponseEntity<AirportData> airport;
    	assertTrue(rest.postForEntity(getBase() + "/collect/airport", b.build(), String.class).getStatusCode().is2xxSuccessful());
    	airport = rest.exchange(getBase() + "/collect/airport/MAD", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class);
        assertTrue(airport.getBody() != null);
        assertTrue(airport.getBody().getIata().equals("MAD"));
        assertTrue(airport.getBody().getLat() == 10.2);
        assertTrue(airport.getBody().getLon() == 33.22);
        assertTrue(airport.getBody().getName().equals("Barajas"));

        // Good call (update airport)
        b.withLat(1);
        b.withLon(9);
    	assertTrue(rest.postForEntity(getBase() + "/collect/airport", b.build(), String.class).getStatusCode().is2xxSuccessful());
    	airport = rest.exchange(getBase() + "/collect/airport/MAD", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class);
        assertTrue(airport.getBody() != null);
        assertTrue(airport.getBody().getIata().equals("MAD"));
        assertTrue(airport.getBody().getLat() == 1);
        assertTrue(airport.getBody().getLon() == 9);
    }       

    @Test
	public void collectDeleteAirport()
	{
        // Bad call (airport doesnt' exists)
    	assertTrue(rest.exchange(getBase() + "/collect/airport/PPP", HttpMethod.DELETE, HttpEntity.EMPTY, String.class).getStatusCode().is2xxSuccessful());

        // Good call
    	AirportData airport = rest.exchange(getBase() + "/collect/airport/BOS", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class).getBody();
    	assertNotNull(airport);
    	assertTrue(rest.exchange(getBase() + "/collect/airport/BOS", HttpMethod.DELETE, HttpEntity.EMPTY, String.class).getStatusCode().is2xxSuccessful());
    	assertTrue(!rest.exchange(getBase() + "/collect/airport/BOS", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class).getStatusCode().is2xxSuccessful());
	}
	
    @SuppressWarnings("unchecked")
	@Test
	public void queryPing() throws Exception
	{
    	ResponseEntity<String> res = rest.getForEntity(getBase() + "/query/ping", String.class);
    	assertTrue(res.getStatusCode().is2xxSuccessful());
    	Map<String, Object> map = (Map<String, Object>)mapper.readValue(res.getBody(), Map.class);
    	assertEquals(3, map.size());
    	assertNotNull(map.get("datasize"));
    	assertNotNull(map.get("iata_freq"));
    	assertNotNull(map.get("radius_freq"));
	}
    
    @Test
	public void queryWeather()
	{
    	// Bad call (iata doesn't exists)
		ResponseEntity<List<AtmosphericInformation>> info = rest.exchange(getBase() + "/query/weather/AAA/0", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<AtmosphericInformation>>() {});
		assertTrue(!info.getStatusCode().is2xxSuccessful());

    	// Good call (return same airport info)
		info = rest.exchange(getBase() + "/query/weather/BOS/0", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<AtmosphericInformation>>() {});
		assertNotNull(info.getBody());
		assertEquals(1, info.getBody().size());

    	// Good call (return near airports info)		
		DataPointBuilder p = new DataPointBuilder().withFirst(10).withSecond(20).withThird(30).withMean(22).withCount(10);
		
    	assertEquals(HttpStatus.OK, rest.postForEntity(getBase() + "/collect/weather/JFK/wind", p.build(), String.class).getStatusCode());    	
        p.withMean(40.0);
    	assertEquals(HttpStatus.OK, rest.postForEntity(getBase() + "/collect/weather/EWR/wind", p.build(), String.class).getStatusCode());    	
        p.withMean(30.0);
    	assertEquals(HttpStatus.OK, rest.postForEntity(getBase() + "/collect/weather/LGA/wind", p.build(), String.class).getStatusCode());
		
		info = rest.exchange(getBase() + "/query/weather/JFK/200", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<AtmosphericInformation>>() {});
		assertNotNull(info.getBody());
		assertEquals(3, info.getBody().size());
	}
    
    @Test
	public void testLoader() throws Exception
	{
    	// Test the loader
    	
		String dat = WeatherRestTests.class.getResource("/airports.dat").getFile();
		AirportLoader al = new AirportLoader();
        al.upload(new FileReader(dat));
        
        Set<String> airports = rest.exchange(getBase() + "/collect/airports", HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<Set<String>>() {}).getBody();
        assertNotNull(airports);
        assertEquals(10, airports.size());
	}
}
