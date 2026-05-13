package Genex.entities;

import java.math.BigDecimal;

public class Budget {
    private String id;
    private Sponsor sponsor;  // Relation directe avec Sponsor
    private int fiscalYear;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;

    // Constructeurs
    public Budget() {
        this.spentAmount = BigDecimal.ZERO;
    }

    public Budget(String id, Sponsor sponsor, int fiscalYear, BigDecimal allocatedAmount) {
        this.id = id;
        this.sponsor = sponsor;
        this.fiscalYear = fiscalYear;
        this.allocatedAmount = allocatedAmount;
        this.spentAmount = BigDecimal.ZERO;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Sponsor getSponsor() {
        return sponsor;
    }

    public void setSponsor(Sponsor sponsor) {
        this.sponsor = sponsor;
    }

    public String getSponsorId() {
        if (sponsor == null) {
            return null;
        }
        return sponsor.getId();
    }

    public int getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(int fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(BigDecimal spentAmount) {
        this.spentAmount = spentAmount;
    }

    public BigDecimal getRemainingAmount() {
        BigDecimal a = (allocatedAmount != null) ? allocatedAmount : BigDecimal.ZERO;
        BigDecimal s = (spentAmount != null) ? spentAmount : BigDecimal.ZERO;
        return a.subtract(s);
    }
}