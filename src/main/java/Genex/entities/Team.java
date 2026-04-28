package Genex.entities;

import java.time.LocalDate;

public class Team {

    private int teamId;
    private int coachId;
    private Integer centreId; // nullable
    private int gameId;

    private String nom;
    private LocalDate foundationDate;
    private String logoImage;
    private String contact;
    private String statut; // actif / inactif

    // Constructors
    public Team() {}

    public Team(int coachId, Integer centreId, int gameId, String nom,
                LocalDate foundationDate, String logoImage,
                String contact, String statut) {
        this.coachId = coachId;
        this.centreId = centreId;
        this.gameId = gameId;
        this.nom = nom;
        this.foundationDate = foundationDate;
        this.logoImage = logoImage;
        this.contact = contact;
        this.statut = statut;
    }

    // Getters & Setters
    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }

    public Integer getCentreId() { return centreId; }
    public void setCentreId(Integer centreId) { this.centreId = centreId; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public LocalDate getFoundationDate() { return foundationDate; }
    public void setFoundationDate(LocalDate foundationDate) { this.foundationDate = foundationDate; }

    public String getLogoImage() { return logoImage; }
    public void setLogoImage(String logoImage) { this.logoImage = logoImage; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}