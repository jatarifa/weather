package com.crossover.trial.weather.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A collected point, including some information about the range of collected values
 *
 * @author code test administrator
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class DataPoint
{
	private int first = 0;
	private int second = 0;
	private int third = 0;
	private double mean = 0.0;
	private int count = 0;

    @SuppressWarnings("unused")
	private DataPoint() {}

    protected DataPoint(int first, int second, int third, double mean, int count) 
    {
        this.first = first;
        this.second = second;
        this.third = third;
        this.mean = mean;
        this.count = count;
    }

    public DataPoint clone()
    {
    	return new Builder().withFirst(first).withSecond(second).withThird(third).withMean(mean).withCount(count).build();
    }
    
    static public class Builder 
    {
    	private int first;
    	private int second;
    	private int third;
    	private double mean;
    	private int count;

        public Builder withFirst(int first) 
        {
            this.first = first;
            return this;
        }

        public Builder withSecond(int second) 
        {
        	this.second = second;
            return this;
        }

        public Builder withThird(int third) 
        {
        	this.third = third;
            return this;
        }

        public Builder withMean(double mean) 
        {
        	this.mean = mean;
            return this;
        }

        public Builder withCount(int count) 
        {
        	this.count = count;
            return this;
        }

        public DataPoint build() 
        {
            return new DataPoint(this.first, this.second, this.third, this.mean, this.count);
        }
    }
}
