package Genex.entities;

public class Team {
    private int    id_team;
    private String nom_team;
    private int    score;

    public Team() {}

    public Team(int id_team, String nom_team, int score) {
        this.id_team  = id_team;
        this.nom_team = nom_team;
        this.score    = score;
    }

    public int    getId_team()            { return id_team; }
    public void   setId_team(int id)      { this.id_team = id; }
    public String getNom_team()           { return nom_team; }
    public void   setNom_team(String n)   { this.nom_team = n; }
    public int    getScore()              { return score; }
    public void   setScore(int score)     { this.score = score; }

    @Override
    public String toString() { return nom_team != null ? nom_team : "Team #" + id_team; }
}
