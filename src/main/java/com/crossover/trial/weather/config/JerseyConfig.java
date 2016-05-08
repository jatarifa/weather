package com.crossover.trial.weather.config;

import org.glassfish.jersey.server.ResourceConfig;

import org.springframework.stereotype.Component;

import com.crossover.trial.weather.RestWeatherQueryEndpoint;
import com.crossover.trial.weather.RestWeatherCollectorEndpoint;

@Component
public class JerseyConfig extends ResourceConfig 
{
	public JerseyConfig() 
	{
		register(RestWeatherCollectorEndpoint.class);
		register(RestWeatherQueryEndpoint.class);
		register(JerseyProbe.class);
	}
}