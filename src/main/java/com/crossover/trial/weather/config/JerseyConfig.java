package com.crossover.trial.weather.config;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.crossover.trial.weather.RestWeatherCollectorEndpoint;
import com.crossover.trial.weather.RestWeatherQueryEndpoint;

@Component
public class JerseyConfig extends ResourceConfig
{
	/**
	 * Configure Jersey on Spring Boot
	 */
	public JerseyConfig()
	{
		register(RestWeatherCollectorEndpoint.class);
		register(RestWeatherQueryEndpoint.class);
		register(JerseyProbe.class);
	}
}