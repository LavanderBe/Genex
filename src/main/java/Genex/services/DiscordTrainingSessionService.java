package Genex.services;

import Genex.entities.TrainingSession;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class DiscordTrainingSessionService {

    public boolean isJoinWindowOpen(TrainingSession session) {
        if (session == null || session.getSessionDatetime() == null ||
                session.getStartTime() == null || session.getEndTime() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startsAt = session.getSessionDatetime().with(session.getStartTime());
        LocalDateTime opensAt = startsAt.minusMinutes(15);
        LocalDateTime endsAt = session.getSessionDatetime().with(session.getEndTime());

        return !now.isBefore(opensAt) && now.isBefore(endsAt);
    }

    public String getJoinUrl(TrainingSession session) {
        String sessionBaseUrl = getConfig("genex.discord.sessionBaseUrl", "GENEX_DISCORD_SESSION_BASE_URL");
        if (sessionBaseUrl != null && !sessionBaseUrl.isBlank()) {
            return sessionBaseUrl +
                    (sessionBaseUrl.contains("?") ? "&" : "?") +
                    "sessionId=" + encode(session.getId()) +
                    "&teamId=" + encode(session.getTeamId());
        }

        String inviteUrl = getConfig("genex.discord.inviteUrl", "GENEX_DISCORD_INVITE_URL");
        if (inviteUrl != null && !inviteUrl.isBlank()) {
            return inviteUrl;
        }

        return null;
    }

    private String getConfig(String propertyName, String envName) {
        String value = System.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            value = System.getenv(envName);
        }
        return value;
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }
}
