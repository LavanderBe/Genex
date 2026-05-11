package Genex.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Player extends User {
    private String prenom;
    private String nom;
    private String nickname;
    private String cin;
    private LocalDate birthday;
    private String nationality;
    private String city;

    private List<Game> games_played=new ArrayList<>();

    // Stats for accuracy and progression
    private int tacticalXp;
    private int totalAttempts;
    private int correctAnswers;

    public Player(String username, String email, String password, String role, String prenom, String nom, String nickname, String cin, LocalDate birthday, String nationality, String city) {
        super(username, email, password, role);
        this.prenom = prenom;
        this.nom = nom;
        this.nickname = nickname;
        this.cin = cin;
        this.birthday = birthday;
        this.nationality = nationality;
        this.city = city;
    }

    public Player(LocalDate birthday, String prenom, String nom, String nickname, String cin, String nationality, String city) {
        super();
        this.birthday = birthday;
        this.prenom = prenom;
        this.nom = nom;
        this.nickname = nickname;
        this.cin = cin;
        this.nationality = nationality;
        this.city = city;
    }

    public Player() {
        super();
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    // Stats methods
    public int getTacticalXp() { return tacticalXp; }
    public void setTacticalXp(int tacticalXp) { this.tacticalXp = tacticalXp; }
    public int getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public double getAccuracy() {
        if (totalAttempts == 0) return 0.0;
        return (double) correctAnswers / totalAttempts * 100.0;
    }

    @Override
    public String toString() {
        return "Player{" +
                "prenom='" + prenom + '\'' +
                ", nom='" + nom + '\'' +
                ", nickname='" + nickname + '\'' +
                ", cin='" + cin + '\'' +
                ", birthday=" + birthday +
                ", nationality='" + nationality + '\'' +
                ", city='" + city + '\'' +
                ", tacticalXp=" + tacticalXp +
                ", totalAttempts=" + totalAttempts +
                ", correctAnswers=" + correctAnswers +
                '}';
    }
}
