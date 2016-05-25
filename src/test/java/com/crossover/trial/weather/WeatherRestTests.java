package com.crossover.trial.weather;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.repo.WeatherRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WeatherServer.class)
@WebAppConfiguration
@IntegrationTest("server.port:9090")
public class WeatherRestTests
{
	private static final Integer EXIT_ERROR_CODE = 1;
	private static final Integer EXIT_OK_CODE = 0;

	private RestTemplate rest = new TestRestTemplate();

	// Internal repository
	@Autowired private WeatherRepository repo;

	// Json Mapper
	@Autowired private ObjectMapper mapper;

	// Test Server port
	@Value("${server.port}") int port;

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
		DataPoint dp = DataPoint.builder().first(1).second(2).third(3).mean(2.0).count(1).build();

		// Bad calls (DataPointType wrong)
		assertTrue(!rest.postForEntity(getBase() + "/collect/weather/BOS/wrong", dp, String.class)
				.getStatusCode()
				.is2xxSuccessful());
		// Bad calls (Iata not existing)
		assertTrue(!rest.postForEntity(getBase() + "/collect/weather/non/wind", dp, String.class)
				.getStatusCode()
				.is2xxSuccessful());

		// Good call
		assertEquals(HttpStatus.OK,
				rest.postForEntity(getBase() + "/collect/weather/BOS/humidity", dp, String.class).getStatusCode());
		AirportData airport = rest.getForEntity(getBase() + "/collect/airport/BOS", AirportData.class).getBody();
		assertNotNull(airport);
		assertEquals(dp, airport.atmosphericInformation().humidity());
	}

	@Test
	public void collectGetAirports()
	{
		Set<String> airports = rest.exchange(getBase() + "/collect/airports", HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<Set<String>>()
				{}).getBody();
		assertNotNull(airports);
		assertEquals(5, airports.size());
	}

	@Test
	public void collectGetAirportByIATA()
	{
		ResponseEntity<AirportData> airport;

		// Bad call (iata doesn't exists)
		airport = rest.exchange(getBase() + "/collect/airport/ddd", HttpMethod.GET, HttpEntity.EMPTY,
				AirportData.class);
		assertTrue(!airport.getStatusCode().is2xxSuccessful());

		// Good call
		airport = rest.exchange(getBase() + "/collect/airport/BOS", HttpMethod.GET, HttpEntity.EMPTY,
				AirportData.class);
		assertNotNull(airport.getBody());
	}

	@Test
	public void collectAddAirportMin()
	{
		// Bad call (iata length > 3)
		assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaaa/10/100", null, String.class)
				.getStatusCode()
				.is2xxSuccessful());
		// Bad call (lat not in correct range)
		assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaa/-100/100", null, String.class)
				.getStatusCode()
				.is2xxSuccessful());
		// Bad call (lon not in correct range)
		assertTrue(!rest.postForEntity(getBase() + "/collect/airport/aaa/10/-190", null, String.class)
				.getStatusCode()
				.is2xxSuccessful());

		// Good call
		ResponseEntity<AirportData> airport;
		assertTrue(rest.postForEntity(getBase() + "/collect/airport/aaa/90/180", null, String.class)
				.getStatusCode()
				.is2xxSuccessful());
		airport = rest.exchange(getBase() + "/collect/airport/aaa", HttpMethod.GET, HttpEntity.EMPTY,
				AirportData.class);
		assertTrue(airport.getBody() != null);
		assertTrue(airport.getBody().iata().equals("AAA"));
		assertTrue(airport.getBody().lat() == 90);
		assertTrue(airport.getBody().lon() == 180);
	}

	@Test
	public void collectAddAirport()
	{
		MockAirportData b = new MockAirportData();
		b.name = "Barajas";
		b.city = "Madrid";
		b.country = "Spain";
		b.iata = "ffffsss"; // bad
		b.icao = "madridd"; // bad
		b.alt = 1000.23;
		b.lat = 10.2;
		b.lon = 33.22;
		b.timezone = 200.0;
		b.dst = "3"; // bad

		// Bad call (iata length > 3)
		assertTrue(
				!rest.postForEntity(getBase() + "/collect/airport", b, String.class).getStatusCode().is2xxSuccessful());

		// Bad call (icao length > 4)
		b.iata = "MAD";
		assertTrue(
				!rest.postForEntity(getBase() + "/collect/airport", b, String.class).getStatusCode().is2xxSuccessful());

		// Bad call (dst doesn't exists)
		b.icao = "MADR";
		assertTrue(
				!rest.postForEntity(getBase() + "/collect/airport", b, String.class).getStatusCode().is2xxSuccessful());

		// Good call (insert new airport)
		b.dst = "E";
		ResponseEntity<AirportData> airport;
		assertTrue(
				rest.postForEntity(getBase() + "/collect/airport", b, String.class).getStatusCode().is2xxSuccessful());
		airport = rest.exchange(getBase() + "/collect/airport/MAD", HttpMethod.GET, HttpEntity.EMPTY,
				AirportData.class);
		assertTrue(airport.getBody() != null);
		assertTrue(airport.getBody().iata().equals("MAD"));
		assertTrue(airport.getBody().lat() == 10.2);
		assertTrue(airport.getBody().lon() == 33.22);
		assertTrue(airport.getBody().name().equals("Barajas"));

		// Good call (update airport)
		b.lat = 1;
		b.lon = 9;
		assertTrue(
				rest.postForEntity(getBase() + "/collect/airport", b, String.class).getStatusCode().is2xxSuccessful());
		airport = rest.exchange(getBase() + "/collect/airport/MAD", HttpMethod.GET, HttpEntity.EMPTY,
				AirportData.class);
		assertTrue(airport.getBody() != null);
		assertTrue(airport.getBody().iata().equals("MAD"));
		assertTrue(airport.getBody().lat() == 1);
		assertTrue(airport.getBody().lon() == 9);
	}

	@Test
	public void collectDeleteAirport()
	{
		// Bad call (airport doesnt' exists)
		assertTrue(rest.exchange(getBase() + "/collect/airport/PPP", HttpMethod.DELETE, HttpEntity.EMPTY, String.class)
				.getStatusCode()
				.is2xxSuccessful());

		// Good call
		AirportData airport = rest
				.exchange(getBase() + "/collect/airport/BOS", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class)
				.getBody();
		assertNotNull(airport);
		assertTrue(rest.exchange(getBase() + "/collect/airport/BOS", HttpMethod.DELETE, HttpEntity.EMPTY, String.class)
				.getStatusCode()
				.is2xxSuccessful());
		assertTrue(
				!rest.exchange(getBase() + "/collect/airport/BOS", HttpMethod.GET, HttpEntity.EMPTY, AirportData.class)
						.getStatusCode()
						.is2xxSuccessful());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void queryPing() throws Exception
	{
		ResponseEntity<String> res = rest.getForEntity(getBase() + "/query/ping", String.class);
		assertTrue(res.getStatusCode().is2xxSuccessful());
		Map<String, Object> map = mapper.readValue(res.getBody(), Map.class);
		assertEquals(3, map.size());
		assertNotNull(map.get("datasize"));
		assertNotNull(map.get("iata_freq"));
		assertNotNull(map.get("radius_freq"));
	}

	@Test
	public void queryWeather()
	{
		// Bad call (iata doesn't exists)
		ResponseEntity<List<AtmosphericInformation>> info = rest.exchange(getBase() + "/query/weather/AAA/0",
				HttpMethod.GET, HttpEntity.EMPTY, new ParameterizedTypeReference<List<AtmosphericInformation>>()
				{});
		assertTrue(!info.getStatusCode().is2xxSuccessful());

		// Good call (return same airport info)
		info = rest.exchange(getBase() + "/query/weather/BOS/0", HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<AtmosphericInformation>>()
				{});
		assertNotNull(info.getBody());
		assertEquals(1, info.getBody().size());

		// Good call (return near airports info)
		DataPoint.Builder p = DataPoint.builder().first(10).second(20).third(30).mean(22.0).count(10);

		assertEquals(HttpStatus.OK,
				rest.postForEntity(getBase() + "/collect/weather/JFK/wind", p.build(), String.class).getStatusCode());
		p.mean(40.0);
		assertEquals(HttpStatus.OK,
				rest.postForEntity(getBase() + "/collect/weather/EWR/wind", p.build(), String.class).getStatusCode());
		p.mean(30.0);
		assertEquals(HttpStatus.OK,
				rest.postForEntity(getBase() + "/collect/weather/LGA/wind", p.build(), String.class).getStatusCode());

		info = rest.exchange(getBase() + "/query/weather/JFK/200", HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<AtmosphericInformation>>()
				{});
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

		Set<String> airports = rest.exchange(getBase() + "/collect/airports", HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<Set<String>>()
				{}).getBody();
		assertNotNull(airports);
		assertEquals(10, airports.size());
	}

	@Test
	public void testLoaderIncorrectRegistries() throws Exception
	{
		String dat = WeatherRestTests.class.getResource("/airports_bad.dat").getFile();
		AirportLoader al = new AirportLoader();
		al.upload(new FileReader(dat));

		Set<String> airports = rest.exchange(getBase() + "/collect/airports", HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<Set<String>>()
				{}).getBody();
		assertNotNull(airports);
		assertEquals(5, airports.size());
	}

	@Test
	public void testLoaderMainNoParams() throws Exception
	{
		execLoaderUnderSecurityManager(status -> assertEquals(EXIT_ERROR_CODE, status));
	}

	@Test
	public void testLoaderMainFileNotExists() throws Exception
	{
		execLoaderUnderSecurityManager(status -> assertEquals(EXIT_ERROR_CODE, status), "non_existing_file.txt");
	}

	@Test
	public void testLoaderMainCorrect() throws Exception
	{
		String dat = WeatherRestTests.class.getResource("/airports.dat").getFile();
		execLoaderUnderSecurityManager(status -> assertEquals(EXIT_OK_CODE, status), dat);

		Set<String> airports = rest.exchange(getBase() + "/collect/airports", HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<Set<String>>()
				{}).getBody();
		assertNotNull(airports);
		assertEquals(10, airports.size());
	}

	protected void execLoaderUnderSecurityManager(Consumer<Integer> func, String... file) throws IOException
	{
		SystemExitControl.forbidSystemExitCall();

		try
		{
			AirportLoader.main(file);
		}
		catch (SystemExitControl.ExitTrappedException e)
		{
			func.accept(e.getStatus());
		}
		finally
		{
			SystemExitControl.enableSystemExitCall();
		}
	}

	static class SystemExitControl
	{
		public static SecurityManager oldManager = null;

		public static class ExitTrappedException extends SecurityException
		{
			private static final long serialVersionUID = 5510302772223257513L;

			private Integer status;

			public ExitTrappedException(Integer status)
			{
				this.status = status;
			}

			public Integer getStatus()
			{
				return status;
			}
		}

		public static void forbidSystemExitCall()
		{
			oldManager = System.getSecurityManager();

			final SecurityManager securityManager = new SecurityManager()
			{
				@Override
				public void checkPermission(Permission perm)
				{}

				@Override
				public void checkPermission(Permission perm, Object context)
				{}

				@Override
				public void checkExit(int status)
				{
					throw new ExitTrappedException(status);
				}
			};
			System.setSecurityManager(securityManager);
		}

		public static void enableSystemExitCall()
		{
			System.setSecurityManager(oldManager);
		}
	}

	// Mock class without validations
	protected static class MockAirportData
	{
		public String iata;
		public double lat;
		public double lon;
		public String icao;
		public String name;
		public String city;
		public String country;
		public Double alt;
		public Double timezone;
		public String dst;
	}
}
