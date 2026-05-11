package Genex.entities;

public enum TeamLevel {
    BEGINNER("Débutant", 2, 3, "Équipe qui débute dans l'esport"),
    INTERMEDIATE("Intermédiaire", 4, 5, "Équipe avec quelques mois d'expérience"),
    ADVANCED("Avancé", 5, 6, "Équipe expérimentée et compétitive"),
    PROFESSIONAL("Professionnel", 6, 7, "Équipe professionnelle avec engagement total");

    private final String displayName;
    private final int minSessionsPerWeek;
    private final int maxSessionsPerWeek;
    private final String description;

    TeamLevel(String displayName, int minSessionsPerWeek, int maxSessionsPerWeek, String description) {
        this.displayName = displayName;
        this.minSessionsPerWeek = minSessionsPerWeek;
        this.maxSessionsPerWeek = maxSessionsPerWeek;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinSessionsPerWeek() {
        return minSessionsPerWeek;
    }

    public int getMaxSessionsPerWeek() {
        return maxSessionsPerWeek;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName + " (" + minSessionsPerWeek + "-" + maxSessionsPerWeek + " sessions/semaine)";
    }
}
