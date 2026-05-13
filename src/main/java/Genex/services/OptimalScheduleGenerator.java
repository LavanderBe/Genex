package Genex.services;

import Genex.entities.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to generate optimal training schedules based on esports best practices
 * Pure algorithmic logic - no database modifications
 */
public class OptimalScheduleGenerator {

    /**
     * Calculate suggested team level based on team data
     * Uses only guaranteed existing data: team age, team size, and training history
     */
    public TeamLevel calculateSuggestedLevel(Team team, List<Player> members, List<TrainingSession> pastSessions) {
        int score = 0;

        // Factor 1: Team age (how long the team exists)
        if (team.getCreatedAt() != null) {
            long daysOld = ChronoUnit.DAYS.between(team.getCreatedAt(), LocalDateTime.now());
            if (daysOld > 180) score += 4;      // 6+ months = experienced
            else if (daysOld > 90) score += 3;  // 3-6 months = intermediate
            else if (daysOld > 30) score += 2;  // 1-3 months = learning
            else score += 1;                     // < 1 month = beginner
        } else {
            score += 1; // New team
        }

        // Factor 2: Team size (full roster = more serious)
        if (members.size() == 5) score += 3;      // Full roster = serious
        else if (members.size() >= 4) score += 2; // Almost full
        else if (members.size() >= 3) score += 1; // Half team
        // Less than 3 = 0 points

        // Factor 3: Training activity (if past sessions provided)
        if (pastSessions != null && !pastSessions.isEmpty()) {
            long completedSessions = pastSessions.stream()
                    .filter(s -> s.getStatus() == TrainingSession.Status.COMPLETED)
                    .count();
            
            if (completedSessions > 50) score += 3;      // Very active
            else if (completedSessions > 20) score += 2; // Active
            else if (completedSessions > 5) score += 1;  // Some activity
        }

        // Convert score to level (max score = 10)
        if (score >= 8) return TeamLevel.PROFESSIONAL;  // 8-10 points
        if (score >= 6) return TeamLevel.ADVANCED;      // 6-7 points
        if (score >= 3) return TeamLevel.INTERMEDIATE;  // 3-5 points
        return TeamLevel.BEGINNER;                       // 0-2 points
    }
    
    /**
     * Overloaded method without past sessions (for backward compatibility)
     */
    public TeamLevel calculateSuggestedLevel(Team team, List<Player> members) {
        return calculateSuggestedLevel(team, members, null);
    }

    /**
     * Generate optimal schedule templates based on team level
     */
    public List<TrainingSessionTemplate> generateWeeklyTemplate(TeamLevel level) {
        List<TrainingSessionTemplate> templates = new ArrayList<>();

        switch (level) {
            case BEGINNER:
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.TUESDAY, LocalTime.of(18, 0), LocalTime.of(20, 0),
                        TrainingSession.Type.STRATEGY, "Stratégie & Théorie",
                        "Apprentissage des bases et des stratégies fondamentales"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.THURSDAY, LocalTime.of(18, 0), LocalTime.of(20, 0),
                        TrainingSession.Type.TEAM_PRACTICE, "Pratique en Équipe",
                        "Mise en pratique des stratégies apprises"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.SATURDAY, LocalTime.of(14, 0), LocalTime.of(16, 0),
                        TrainingSession.Type.AIM_TRAINING, "Entraînement Mécanique",
                        "Amélioration des compétences individuelles"
                ));
                break;

            case INTERMEDIATE:
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(20, 30),
                        TrainingSession.Type.AIM_TRAINING, "Warm-up & Mécanique",
                        "Échauffement et entraînement des mécaniques"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.WEDNESDAY, LocalTime.of(18, 0), LocalTime.of(21, 0),
                        TrainingSession.Type.SCRIM, "Scrim #1",
                        "Match d'entraînement contre une autre équipe"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.FRIDAY, LocalTime.of(18, 0), LocalTime.of(21, 0),
                        TrainingSession.Type.SCRIM, "Scrim #2",
                        "Match d'entraînement et analyse"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.SATURDAY, LocalTime.of(14, 0), LocalTime.of(17, 0),
                        TrainingSession.Type.TEAM_PRACTICE, "Pratique Intensive",
                        "Travail sur les points faibles identifiés"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.SUNDAY, LocalTime.of(15, 0), LocalTime.of(17, 0),
                        TrainingSession.Type.STRATEGY, "Revue & Stratégie",
                        "Analyse des scrims et préparation de nouvelles stratégies"
                ));
                break;

            case ADVANCED:
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(20, 0),
                        TrainingSession.Type.AIM_TRAINING, "Warm-up Intensif",
                        "Échauffement et drill mécanique avancé"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.TUESDAY, LocalTime.of(18, 0), LocalTime.of(22, 0),
                        TrainingSession.Type.SCRIM, "Scrim Compétitif #1",
                        "Match contre équipe de niveau similaire ou supérieur"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.WEDNESDAY, LocalTime.of(17, 0), LocalTime.of(20, 0),
                        TrainingSession.Type.STRATEGY, "Analyse & Stratégie",
                        "VOD review et développement de nouvelles stratégies"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.THURSDAY, LocalTime.of(18, 0), LocalTime.of(22, 0),
                        TrainingSession.Type.SCRIM, "Scrim Compétitif #2",
                        "Match d'entraînement avec focus sur les nouvelles stratégies"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.FRIDAY, LocalTime.of(18, 0), LocalTime.of(21, 0),
                        TrainingSession.Type.TEAM_PRACTICE, "Pratique Ciblée",
                        "Travail sur les situations spécifiques et exécutions"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.SATURDAY, LocalTime.of(14, 0), LocalTime.of(18, 0),
                        TrainingSession.Type.SCRIM, "Scrim Marathon",
                        "Série de matchs d'entraînement"
                ));
                break;

            case PROFESSIONAL:
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.MONDAY, LocalTime.of(16, 0), LocalTime.of(20, 0),
                        TrainingSession.Type.AIM_TRAINING, "Warm-up Pro",
                        "Routine d'échauffement professionnelle"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.MONDAY, LocalTime.of(20, 0), LocalTime.of(23, 0),
                        TrainingSession.Type.SCRIM, "Scrim Pro #1",
                        "Match contre équipe professionnelle"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.TUESDAY, LocalTime.of(16, 0), LocalTime.of(19, 0),
                        TrainingSession.Type.STRATEGY, "VOD Review Approfondie",
                        "Analyse détaillée des performances et adversaires"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.TUESDAY, LocalTime.of(19, 0), LocalTime.of(23, 0),
                        TrainingSession.Type.SCRIM, "Scrim Pro #2",
                        "Match d'entraînement avec nouvelles stratégies"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.WEDNESDAY, LocalTime.of(16, 0), LocalTime.of(20, 0),
                        TrainingSession.Type.TEAM_PRACTICE, "Pratique Intensive",
                        "Drill et exécutions répétées"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.THURSDAY, LocalTime.of(16, 0), LocalTime.of(23, 0),
                        TrainingSession.Type.SCRIM, "Scrim Pro #3",
                        "Match longue durée avec analyse en temps réel"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.FRIDAY, LocalTime.of(16, 0), LocalTime.of(20, 0),
                        TrainingSession.Type.SCRIM, "Scrim Pro #4",
                        "Derniers ajustements avant le weekend"
                ));
                templates.add(new TrainingSessionTemplate(
                        DayOfWeek.SATURDAY, LocalTime.of(13, 0), LocalTime.of(19, 0),
                        TrainingSession.Type.OTHER, "Bootcamp Weekend",
                        "Session intensive de préparation"
                ));
                break;
        }

        return templates;
    }

    /**
     * Generate actual training sessions from templates for a date range
     */
    public List<TrainingSession> generateSchedule(
            String teamId,
            TeamLevel level,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<TrainingSession> sessions = new ArrayList<>();
        List<TrainingSessionTemplate> templates = generateWeeklyTemplate(level);

        // Iterate through each week in the date range
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // For each template, create a session on the matching day
            for (TrainingSessionTemplate template : templates) {
                LocalDate sessionDate = currentDate.with(template.getDayOfWeek());

                // Only add if within range
                if (!sessionDate.isBefore(startDate) && !sessionDate.isAfter(endDate)) {
                    TrainingSession session = new TrainingSession();
                    session.setTeamId(teamId);
                    session.setTitle(template.getTitle());
                    session.setType(template.getType());
                    session.setSessionDatetime(sessionDate.atTime(template.getStartTime()));
                    session.setStartTime(template.getStartTime());
                    session.setEndTime(template.getEndTime());
                    session.setNotes(template.getNotes());
                    session.setStatus(TrainingSession.Status.PLANNED);

                    sessions.add(session);
                }
            }

            // Move to next week
            currentDate = currentDate.plusWeeks(1);
        }

        return sessions;
    }

    /**
     * Get statistics about a generated schedule
     */
    public ScheduleStatistics getScheduleStatistics(List<TrainingSession> sessions) {
        ScheduleStatistics stats = new ScheduleStatistics();

        stats.totalSessions = sessions.size();
        stats.totalHours = sessions.stream()
                .mapToInt(TrainingSession::getDurationMinutes)
                .sum() / 60.0;

        // Count by type
        for (TrainingSession session : sessions) {
            switch (session.getType()) {
                case SCRIM:
                    stats.scrimCount++;
                    break;
                case AIM_TRAINING:
                    stats.aimTrainingCount++;
                    break;
                case STRATEGY:
                    stats.strategyCount++;
                    break;
                case TEAM_PRACTICE:
                    stats.teamPracticeCount++;
                    break;
                case OTHER:
                    stats.otherCount++;
                    break;
            }
        }

        return stats;
    }

    /**
     * Check for conflicts with existing sessions
     */
    public List<ConflictInfo> detectConflicts(
            List<TrainingSession> newSessions,
            List<TrainingSession> existingSessions
    ) {
        List<ConflictInfo> conflicts = new ArrayList<>();

        for (TrainingSession newSession : newSessions) {
            for (TrainingSession existing : existingSessions) {
                if (hasTimeConflict(newSession, existing)) {
                    conflicts.add(new ConflictInfo(newSession, existing));
                }
            }
        }

        return conflicts;
    }

    /**
     * Check if two sessions have a time conflict
     */
    private boolean hasTimeConflict(TrainingSession session1, TrainingSession session2) {
        if (session1.getSessionDatetime() == null || session2.getSessionDatetime() == null) {
            return false;
        }

        LocalDate date1 = session1.getSessionDatetime().toLocalDate();
        LocalDate date2 = session2.getSessionDatetime().toLocalDate();

        // Different days = no conflict
        if (!date1.equals(date2)) {
            return false;
        }

        // Same day - check time overlap
        LocalTime start1 = session1.getStartTime();
        LocalTime end1 = session1.getEndTime();
        LocalTime start2 = session2.getStartTime();
        LocalTime end2 = session2.getEndTime();

        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        // Check if times overlap
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Analyze training balance and provide recommendations
     */
    public BalanceAnalysis analyzeTrainingBalance(List<TrainingSession> sessions) {
        BalanceAnalysis analysis = new BalanceAnalysis();
        
        if (sessions.isEmpty()) {
            analysis.isBalanced = false;
            analysis.recommendations.add("Aucune session planifiée");
            return analysis;
        }

        ScheduleStatistics stats = getScheduleStatistics(sessions);
        int total = stats.totalSessions;

        // Calculate percentages
        double scrimPercent = (stats.scrimCount * 100.0) / total;
        double aimPercent = (stats.aimTrainingCount * 100.0) / total;
        double strategyPercent = (stats.strategyCount * 100.0) / total;
        double practicePercent = (stats.teamPracticeCount * 100.0) / total;

        analysis.scrimPercentage = scrimPercent;
        analysis.aimPercentage = aimPercent;
        analysis.strategyPercentage = strategyPercent;
        analysis.practicePercentage = practicePercent;

        // Ideal balance: 40% Scrim, 20% Aim, 20% Strategy, 20% Practice
        analysis.isBalanced = true;

        if (scrimPercent < 30) {
            analysis.recommendations.add("⚠️ Pas assez de scrims - augmentez les matchs d'entraînement");
            analysis.isBalanced = false;
        } else if (scrimPercent > 60) {
            analysis.recommendations.add("⚠️ Trop de scrims - ajoutez plus de pratique et stratégie");
            analysis.isBalanced = false;
        }

        if (strategyPercent < 10) {
            analysis.recommendations.add("⚠️ Manque de sessions stratégiques - ajoutez des VOD reviews");
            analysis.isBalanced = false;
        }

        if (aimPercent < 10) {
            analysis.recommendations.add("⚠️ Manque d'entraînement mécanique - ajoutez des warm-ups");
            analysis.isBalanced = false;
        }

        if (stats.totalHours < 8) {
            analysis.recommendations.add("💡 Volume d'entraînement faible - considérez plus de sessions");
        } else if (stats.totalHours > 40) {
            analysis.recommendations.add("⚠️ Risque de burnout - réduisez le volume d'entraînement");
            analysis.isBalanced = false;
        }

        if (analysis.isBalanced) {
            analysis.recommendations.add("✅ Planning bien équilibré!");
        }

        return analysis;
    }

    /**
     * Inner class to hold schedule statistics
     */
    public static class ScheduleStatistics {
        public int totalSessions;
        public double totalHours;
        public int scrimCount;
        public int aimTrainingCount;
        public int strategyCount;
        public int teamPracticeCount;
        public int otherCount;

        @Override
        public String toString() {
            return String.format(
                    "Total: %d sessions (%.1fh)\n" +
                            "Scrims: %d | Aim: %d | Strategy: %d | Practice: %d | Other: %d",
                    totalSessions, totalHours,
                    scrimCount, aimTrainingCount, strategyCount, teamPracticeCount, otherCount
            );
        }
    }

    /**
     * Inner class to hold conflict information
     */
    public static class ConflictInfo {
        public TrainingSession newSession;
        public TrainingSession existingSession;

        public ConflictInfo(TrainingSession newSession, TrainingSession existingSession) {
            this.newSession = newSession;
            this.existingSession = existingSession;
        }

        @Override
        public String toString() {
            return String.format("Conflit: %s (%s) chevauche %s (%s)",
                    newSession.getTitle(),
                    newSession.getSessionDatetime(),
                    existingSession.getTitle(),
                    existingSession.getSessionDatetime());
        }
    }

    /**
     * Inner class to hold balance analysis
     */
    public static class BalanceAnalysis {
        public boolean isBalanced;
        public double scrimPercentage;
        public double aimPercentage;
        public double strategyPercentage;
        public double practicePercentage;
        public List<String> recommendations = new ArrayList<>();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("═══ ANALYSE D'ÉQUILIBRE ═══\n\n");
            sb.append(String.format("Scrims: %.1f%%\n", scrimPercentage));
            sb.append(String.format("Aim Training: %.1f%%\n", aimPercentage));
            sb.append(String.format("Stratégie: %.1f%%\n", strategyPercentage));
            sb.append(String.format("Pratique: %.1f%%\n\n", practicePercentage));
            sb.append("Recommandations:\n");
            for (String rec : recommendations) {
                sb.append("• ").append(rec).append("\n");
            }
            return sb.toString();
        }
    }
}
