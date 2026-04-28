package Genex.entities;

import java.math.BigDecimal;

public class Budget {
    private String id;
    private String centerId;
    private Sponsor sponsor;  // Relation directe avec Sponsor
    private int fiscalYear;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;

    // Constructeurs
    public Budget() {
        this.spentAmount = BigDecimal.ZERO;
    }

    public Budget(String id, String centerId, Sponsor sponsor, int fiscalYear, BigDecimal allocatedAmount) {
        this.id = id;
        this.centerId = centerId;
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

    public String getCenterId() {
        return centerId;
    }

    public void setCenterId(String centerId) {
        this.centerId = centerId;
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
        return allocatedAmount.subtract(spentAmount);
    }
}