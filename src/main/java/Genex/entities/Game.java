package Genex.entities;

public class Game {
    private String id;
    private String nom;
    private String genre;
    private String platforme;
    private String mode;
    private int max_team_player;
    private String icon_url;

    public Game() {
    }

    public Game(String nom, String genre, String platforme, String mode, int max_team_player, String icon_url) {
        this.nom = nom;
        this.genre = genre;
        this.platforme = platforme;
        this.mode = mode;
        this.max_team_player = max_team_player;
        this.icon_url = icon_url;
    }




    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
    public String getPlatforme() {
        return platforme;
    }

    public void setPlatforme(String platforme) {
        this.platforme = platforme;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
    public int getMax_team_player() {
        return max_team_player;
    }

    public void setMax_team_player(int max_team_player) {
        this.max_team_player = max_team_player;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    @Override
    public String toString() {
        return "Game{" +
                "nom='" + nom + '\'' +
                ", genre='" + genre + '\'' +
                ", platforme='" + platforme + '\'' +
                ", mode='" + mode + '\'' +
                ", max_team_player=" + max_team_player +
                ", icon_url='" + icon_url + '\'' +
                '}';
    }
}
