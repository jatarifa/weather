package com.crossover.trial.weather.config;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JerseyProbe implements ContainerRequestFilter
{
	@Override
	public void filter(ContainerRequestContext req) throws IOException 
	{	
		String query = req.getUriInfo().getRequestUri().toString().substring(req.getUriInfo().getBaseUri().toString().length() - 1);
		log.info(query);
	}
}
