package com.crossover.trial.weather;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "WeatherServer")
@SpringBootApplication
public class WeatherServer implements InitializingBean
{
	@Autowired private Environment environment;

	/**
	 * Starts the server
	 */
	public static void main(String[] args)
	{
		SpringApplication.run(WeatherServer.class, args);
	}

	/**
	 * Inform after server init
	 */
	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info("Weather Server started.\n url=http://{}:{}\n", environment.getProperty("server.address"),
				environment.getProperty("server.port"));
	}

	/**
	 * Configure Jackson Json Mapper
	 */
	@Bean
	public Jackson2ObjectMapperBuilder jacksonBuilder()
	{
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.indentOutput(true);
		return builder;
	}
}
