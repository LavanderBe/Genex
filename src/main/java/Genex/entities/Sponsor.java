package Genex.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class Sponsor {
    private String id;
    private String name;
    private String logoUrl;
    private String websiteUrl;
    private String industry;
    private String contactEmail;
    private LocalDateTime createdAt;

    // Constructeur par défaut
    public Sponsor() {}

    // Constructeur avec paramètres
    public Sponsor(String id, String name, String logoUrl, String websiteUrl,
                   String industry, String contactEmail) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.websiteUrl = websiteUrl;
        this.industry = industry;
        this.contactEmail = contactEmail;
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur simplifié (pour compatibilité avec votre code existant)
    public Sponsor(String id, String name) {
        this.id = id;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static String generateId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "Sponsor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", industry='" + industry + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                '}';
    }
}