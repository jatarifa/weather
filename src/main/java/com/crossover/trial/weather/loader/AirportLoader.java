package com.crossover.trial.weather.loader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.crossover.trial.weather.exceptions.WeatherException;
import com.crossover.trial.weather.model.AirportData;

import lombok.extern.slf4j.Slf4j;

/**
 * A simple airport loader which reads a file from disk and sends entries to the webservice
 * 
 * @author code test administrator
 */
@Slf4j
public class AirportLoader 
{
	private static final String BASE_SERVER = "http://localhost:9090";
	
    private final WebTarget collect = ClientBuilder.newClient().target(BASE_SERVER + "/collect");
    
    public static void main(String args[]) throws IOException
    {
        File airportDataFile = new File(args[0]);
        if (!airportDataFile.exists() || airportDataFile.length() == 0) 
        {
            log.error(airportDataFile + " is not a valid input");
            System.exit(1);
        }

        AirportLoader al = new AirportLoader();
        al.upload(new FileReader(airportDataFile));
        
        System.exit(0);
    }
    
    public void upload(FileReader in) throws IOException
    {
    	CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter(',')
    											   .withIgnoreEmptyLines()
    											   .withIgnoreSurroundingSpaces()
    											   .withQuote('"')
    											   .withSkipHeaderRecord();
    										
    	try(CSVParser parser = new CSVParser(in, csvFileFormat))
    	{
	    	Iterator<CSVRecord> it = parser.iterator();
	    	while(it.hasNext())
	    	{
	    		try
	    		{
		    		CSVRecord record = it.next();
		    		AirportData a = parseAirportRegistry(record);
		    		AirportData.validateData(a);
		    		uploadAirport(a);
		    		
		    		log.info("Airport {} loaded.",  a.getIata());
	    		}
	    		catch(WeatherException e)
	    		{
	    			log.error("Error parsing file : {}", e.getMessage());
	    		}
	    	}
    	}
    }
    
    private AirportData parseAirportRegistry(CSVRecord record)
    {
		AirportData a = new AirportData();
		a.setName(record.get(1));
		a.setCity(record.get(2));
		a.setCountry(record.get(3));
		a.setIata(record.get(4));
		a.setIcao(record.get(5));
		a.setLat(checkDouble("Latitude", record.get(6)));
		a.setLon(checkDouble("Longitude", record.get(7)));
		a.setAlt(checkDouble("Altitude", record.get(8)));
		a.setTimezone(checkDouble("Timezone", record.get(9)));
		a.setDst(record.get(10));
		
		return a;    	
    }

    private void uploadAirport(AirportData a)
    {
    	try
    	{
			WebTarget path = collect.path("/airport");
			Response post = path.request().post(Entity.entity(a, "application/json"));
			if(!post.getStatusInfo().getReasonPhrase().equalsIgnoreCase("OK"))
				throw new Exception(post.getStatusInfo().getReasonPhrase());
    	}
    	catch(Exception e)
    	{
			throw new WeatherException("Airport upload fail : " + e.getMessage());    	    		
    	}
    }
    
    private Double checkDouble(String name, String d)
    {
    	try
    	{
    		return Double.parseDouble(d);
    	}
    	catch(NumberFormatException nfe)
    	{
    		throw new WeatherException(name + " value not valid : " + d);
    	}
    }
}
