package Genex.entities;

import java.time.LocalDate;

public class Player {
    private String id;
    private String prenom;
    private String nom;
    private String nickname;
    private String cin;
    private LocalDate birthday;
    private String nationality;
    private String city;



    public Player(LocalDate birthday, String prenom, String nom, String nickname, String cin, String nationality, String city) {
        this.birthday = birthday;
        this.prenom = prenom;
        this.nom = nom;
        this.nickname = nickname;
        this.cin = cin;
        this.nationality = nationality;
        this.city = city;
    }

    public Player() {
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
                '}';
    }
}
