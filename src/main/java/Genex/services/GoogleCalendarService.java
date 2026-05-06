package Genex.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import Genex.entities.TrainingSession;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Genex Training Scheduler";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    private Calendar calendarService;

    public GoogleCalendarService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            calendarService = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            System.out.println("✓ Google Calendar Service initialized successfully");
        } catch (Exception e) {
            System.err.println("✗ Failed to initialize Google Calendar Service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(new FileInputStream(CREDENTIALS_FILE_PATH)));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Create a calendar event from a training session
     */
    public String createEvent(TrainingSession session) {
        try {
            Event event = new Event()
                    .setSummary(session.getTitle())
                    .setDescription(buildEventDescription(session))
                    .setColorId(getColorIdForType(session.getType()));

            // Set start time
            LocalDateTime startDateTime = LocalDateTime.of(
                    session.getSessionDatetime().toLocalDate(),
                    session.getStartTime()
            );
            DateTime start = new DateTime(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            event.setStart(new EventDateTime().setDateTime(start).setTimeZone("UTC"));

            // Set end time
            LocalDateTime endDateTime = LocalDateTime.of(
                    session.getSessionDatetime().toLocalDate(),
                    session.getEndTime()
            );
            DateTime end = new DateTime(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("UTC"));

            // Set location if available
            if (session.getLocation() != null && !session.getLocation().isEmpty()) {
                event.setLocation(session.getLocation());
            }

            event = calendarService.events().insert("primary", event).execute();
            System.out.println("✓ Calendar event created: " + event.getHtmlLink());
            return event.getId();

        } catch (Exception e) {
            System.err.println("✗ Failed to create calendar event: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update an existing calendar event
     */
    public void updateEvent(String eventId, TrainingSession session) {
        try {
            Event event = calendarService.events().get("primary", eventId).execute();

            event.setSummary(session.getTitle());
            event.setDescription(buildEventDescription(session));
            event.setColorId(getColorIdForType(session.getType()));

            // Update start time
            LocalDateTime startDateTime = LocalDateTime.of(
                    session.getSessionDatetime().toLocalDate(),
                    session.getStartTime()
            );
            DateTime start = new DateTime(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            event.setStart(new EventDateTime().setDateTime(start).setTimeZone("UTC"));

            // Update end time
            LocalDateTime endDateTime = LocalDateTime.of(
                    session.getSessionDatetime().toLocalDate(),
                    session.getEndTime()
            );
            DateTime end = new DateTime(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()));
            event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("UTC"));

            // Update location
            if (session.getLocation() != null && !session.getLocation().isEmpty()) {
                event.setLocation(session.getLocation());
            }

            calendarService.events().update("primary", eventId, event).execute();
            System.out.println("✓ Calendar event updated: " + eventId);

        } catch (Exception e) {
            System.err.println("✗ Failed to update calendar event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Delete a calendar event
     */
    public void deleteEvent(String eventId) {
        try {
            calendarService.events().delete("primary", eventId).execute();
            System.out.println("✓ Calendar event deleted: " + eventId);
        } catch (Exception e) {
            System.err.println("✗ Failed to delete calendar event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get events for a specific month
     */
    public List<Event> getEventsForMonth(int year, int month) {
        try {
            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

            DateTime timeMin = new DateTime(Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant()));
            DateTime timeMax = new DateTime(Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant()));

            Events events = calendarService.events().list("primary")
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems();

        } catch (Exception e) {
            System.err.println("✗ Failed to fetch calendar events: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Build event description from training session
     */
    private String buildEventDescription(TrainingSession session) {
        StringBuilder desc = new StringBuilder();
        desc.append("Type: ").append(session.getType()).append("\n");
        desc.append("Status: ").append(session.getStatus()).append("\n");
        desc.append("Duration: ").append(session.getFormattedDuration()).append("\n");
        if (session.getNotes() != null && !session.getNotes().isEmpty()) {
            desc.append("\nNotes:\n").append(session.getNotes());
        }
        return desc.toString();
    }

    /**
     * Get Google Calendar color ID for training session type
     */
    private String getColorIdForType(TrainingSession.Type type) {
        if (type == null) return "8"; // Gray
        
        return switch (type) {
            case SCRIM -> "9";          // Blue
            case AIM_TRAINING -> "11";  // Red
            case STRATEGY -> "10";      // Green
            case TEAM_PRACTICE -> "5";  // Yellow
            case OTHER -> "8";          // Gray
        };
    }
}
