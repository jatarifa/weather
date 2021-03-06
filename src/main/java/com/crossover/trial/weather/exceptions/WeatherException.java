package com.crossover.trial.weather.exceptions;

/**
 * An internal exception marker
 */
public class WeatherException extends RuntimeException
{
	private static final long serialVersionUID = -3926816220257601136L;

	public WeatherException()
	{
		super();
	}

	public WeatherException(String message)
	{
		super(message);
	}

	public WeatherException(Exception e)
	{
		super(e);
	}
}
