package com.company.fxrateaggregatorservice.JAVA;
public class RatesTableEntity {
	private String baseCurrency;

	private String toCurrency;
	
	private String country;

	private Double rate;

	private String lastUpdateTimeStamp;

	private String source;

	private String requestedDateTime;

	public String getBaseCurrency() {
		return baseCurrency;
	}

	public void setBaseCurrency(String baseCurrency) {
		this.baseCurrency = baseCurrency;
	}
	
		public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getToCurrency() {
		return toCurrency;
	}

	public void setToCurrency(String toCurrency) {
		this.toCurrency = toCurrency;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public String getLastUpdateTimeStamp() {
		return lastUpdateTimeStamp;
	}

	public void setLastUpdateTimeStamp(String lastUpdateTimeStamp) {
		this.lastUpdateTimeStamp = lastUpdateTimeStamp;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getRequestedDateTime() {
		return requestedDateTime;
	}

	public void setRequestedDateTime(String requestedDateTime) {
		this.requestedDateTime = requestedDateTime;
	}

}






