package hotelmanagementSystem;

public class Promo {
	 private String name;
	    private double discount;
	    private java.sql.Date validFrom;
	    private java.sql.Date validUntil;

	    public Promo(String name, double discount, java.sql.Date validFrom, java.sql.Date validUntil) {
	        this.name = name;
	        this.discount = discount;
	        this.validFrom = validFrom;
	        this.validUntil = validUntil;
	    }
	    public double getDiscountPercentage() {
	        return discount;
	    }

	    public java.sql.Date getValidFrom() {
	        return validFrom;
	    }

	    public java.sql.Date getValidUntil() {
	        return validUntil;
	    }
	}


