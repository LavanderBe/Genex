package Genex.services;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import Genex.entities.TrainingSession;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Genex Training Sessions";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private Calendar calendarService;
    private String calendarId = "primary"; // Can be changed to a specific calendar ID

    public GoogleCalendarService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            // Load service account credentials
            InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            
            GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                    .createScoped(SCOPES);
            
            calendarService = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, 
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
                    
            System.out.println("Google Calendar Service initialized successfully");
            
        } catch (Exception e) {
            System.err.println("Error initializing Google Calendar Service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set a specific calendar ID (optional, defaults to "primary")
     */
    public void setCalendarId(String calendarId) {
        this.calendarId = calendarId;
    }

    /**
     * Create a calendar event for a training session
     */
    public String createEvent(TrainingSession session) {
        try {
            Event event = new Event()
                    .setSummary(session.getTitle())
                    .setLocation(session.getLocation())
                    .setDescription(buildDescription(session));

            // Set start and end times
            LocalDateTime sessionDate = session.getSessionDatetime();
            LocalDateTime startDateTime = sessionDate.with(session.getStartTime());
            LocalDateTime endDateTime = sessionDate.with(session.getEndTime());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new DateTime(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant())))
                    .setTimeZone(ZoneId.systemDefault().getId());
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                    .setDateTime(new DateTime(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())))
                    .setTimeZone(ZoneId.systemDefault().getId());
            event.setEnd(end);

            // Add color coding based on session type
            event.setColorId(getColorIdForType(session.getType()));

            // Insert event
            event = calendarService.events().insert(calendarId, event).execute();
            System.out.println("Event created: " + event.getHtmlLink());
            return event.getId();

        } catch (Exception e) {
            System.err.println("Error creating calendar event: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update an existing calendar event
     */
    public boolean updateEvent(TrainingSession session) {
        try {
            if (session.getCalendarEventId() == null) {
                System.err.println("Cannot update event: calendarEventId is null");
                return false;
            }

            // Retrieve the existing event
            Event event = calendarService.events().get(calendarId, session.getCalendarEventId()).execute();

            // Update event details
            event.setSummary(session.getTitle());
            event.setLocation(session.getLocation());
            event.setDescription(buildDescription(session));

            // Update start and end times
            LocalDateTime sessionDate = session.getSessionDatetime();
            LocalDateTime startDateTime = sessionDate.with(session.getStartTime());
            LocalDateTime endDateTime = sessionDate.with(session.getEndTime());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new DateTime(Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant())))
                    .setTimeZone(ZoneId.systemDefault().getId());
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                    .setDateTime(new DateTime(Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())))
                    .setTimeZone(ZoneId.systemDefault().getId());
            event.setEnd(end);

            // Update color coding
            event.setColorId(getColorIdForType(session.getType()));

            // Update the event
            calendarService.events().update(calendarId, event.getId(), event).execute();
            System.out.println("Event updated: " + event.getHtmlLink());
            return true;

        } catch (Exception e) {
            System.err.println("Error updating calendar event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a calendar event
     */
    public boolean deleteEvent(String calendarEventId) {
        try {
            if (calendarEventId == null) {
                System.err.println("Cannot delete event: calendarEventId is null");
                return false;
            }

            calendarService.events().delete(calendarId, calendarEventId).execute();
            System.out.println("Event deleted: " + calendarEventId);
            return true;

        } catch (Exception e) {
            System.err.println("Error deleting calendar event: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get events for a specific month
     */
    public List<Event> getEventsForMonth(int year, int month) {
        try {
            // Create start and end of month
            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            DateTime timeMin = new DateTime(Date.from(startOfMonth.atZone(ZoneId.systemDefault()).toInstant()));
            DateTime timeMax = new DateTime(Date.from(endOfMonth.atZone(ZoneId.systemDefault()).toInstant()));

            Events events = calendarService.events().list(calendarId)
                    .setTimeMin(timeMin)
                    .setTimeMax(timeMax)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems();

        } catch (Exception e) {
            System.err.println("Error retrieving calendar events: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Get color ID for session type (Google Calendar color IDs)
     * 1=Lavender, 2=Sage, 3=Grape, 4=Flamingo, 5=Banana, 
     * 6=Tangerine, 7=Peacock, 8=Graphite, 9=Blueberry, 10=Basil, 11=Tomato
     */
    private String getColorIdForType(TrainingSession.Type type) {
        return switch (type) {
            case SCRIM -> "9";           // Blueberry (Blue)
            case AIM_TRAINING -> "11";   // Tomato (Red)
            case STRATEGY -> "10";       // Basil (Green)
            case TEAM_PRACTICE -> "5";   // Banana (Yellow)
            case OTHER -> "8";           // Graphite (Gray)
        };
    }

    /**
     * Build event description from training session
     */
    private String buildDescription(TrainingSession session) {
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
     * Check if the service is properly initialized
     */
    public boolean isInitialized() {
        return calendarService != null;
    }
}
