package com.crossover.trial.weather.config;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JerseyProbe implements ContainerRequestFilter
{
	/**
	 * logs each requests
	 */
	@Override
	public void filter(ContainerRequestContext req) throws IOException
	{
		int pos = req.getUriInfo().getBaseUri().toString().length() - 1;

		String query = req.getUriInfo().getRequestUri().toString().substring(pos);

		log.info(query);
	}
}
