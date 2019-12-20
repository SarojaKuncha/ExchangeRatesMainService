package com.company.fxrateaggregatorservice.JAVA;
public class Root {
	private Rates rates;

    private String base;

    private String date;

    public void setRates(Rates rates){
        this.rates = rates;
    }
    public Rates getRates(){
        return this.rates;
    }
    public void setBase(String base){
        this.base = base;
    }
    public String getBase(){
        return this.base;
    }
   public void setDate(String date) {
		this.date = date;
	}

	public String getDate() {
		return this.date;
	}
}


