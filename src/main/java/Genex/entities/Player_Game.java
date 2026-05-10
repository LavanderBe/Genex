package Genex.entities;

public class Player_Game {
    private String user_id;
    private String game_id;
    private String nickname_game;
    private String rank;
    private int hours_played;
    private String ingame_uid;


    //this one is just for linking both without adding any data
    public Player_Game(String user_id,String game_id){
        this.game_id=game_id;
        this.user_id=user_id;
    }

    //this one doesn't have the hours played (maybe some apis don't accept such a tthing)
    public Player_Game(String user_id, String game_id, String ingame_uid, String rank, String nickname_game) {
        this.user_id = user_id;
        this.game_id = game_id;
        this.ingame_uid = ingame_uid;
        this.rank = rank;
        this.nickname_game = nickname_game;
    }

    //this one is for having the full data
    public Player_Game(String user_id, String game_id, String nickname_game, String rank, int hours_played, String ingame_uid) {
        this.user_id = user_id;
        this.game_id = game_id;
        this.nickname_game = nickname_game;
        this.rank = rank;
        this.hours_played = hours_played;
        this.ingame_uid = ingame_uid;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id;
    }

    public String getNickname_game() {
        return nickname_game;
    }

    public void setNickname_game(String nickname_game) {
        this.nickname_game = nickname_game;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public int getHours_played() {
        return hours_played;
    }

    public void setHours_played(int hours_played) {
        this.hours_played = hours_played;
    }

    public String getIngame_uid() {
        return ingame_uid;
    }

    public void setIngame_uid(String ingame_uid) {
        this.ingame_uid = ingame_uid;
    }

    @Override
    public String toString() {
        return "Player_Game{" +
                "user_id='" + user_id + '\'' +
                ", game_id='" + game_id + '\'' +
                ", nickname_game='" + nickname_game + '\'' +
                ", rank='" + rank + '\'' +
                ", hours_played=" + hours_played +
                ", ingame_uid='" + ingame_uid + '\'' +
                '}';
    }
}
