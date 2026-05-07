package Genex.Controllers.Team;

import Genex.entities.TeamMessage;
import Genex.services.CrudTeamMessage;
import Genex.services.CrudTeamMember;
import Genex.utils.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TeamChatPanelController {

    @FXML private Label onlineCountLabel;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;
    @FXML private VBox chatModalPanel;
    @FXML private Button closeButton;

    private String teamId;
    private String currentUserId;
    private CrudTeamMessage crudTeamMessage;
    private CrudTeamMember crudTeamMember;
    private Timeline refreshTimeline;
    private LocalDateTime lastMessageTime;
    private DateTimeFormatter timeFormatter;
    private Runnable onCloseCallback;

    @FXML
    public void initialize() {
        crudTeamMessage = new CrudTeamMessage();
        crudTeamMember = new CrudTeamMember();
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        // Setup enter key to send message
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                handleSendMessage();
            }
        });
    }

    public void setTeam(String teamId) {
        this.teamId = teamId;
        loadMessages();
        updateOnlineCount();
        startAutoRefresh();
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    private void handleClose() {
        stopAutoRefresh();
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    @FXML
    private void handleBackgroundClick(javafx.scene.input.MouseEvent event) {
        // Close modal when clicking on dark background
        if (event.getTarget() == event.getSource()) {
            handleClose();
        }
    }

    @FXML
    private void consumeClick(javafx.scene.input.MouseEvent event) {
        // Prevent clicks inside the modal from closing it
        event.consume();
    }

    /**
     * Load all messages for the team
     */
    private void loadMessages() {
        try {
            List<TeamMessage> messages = crudTeamMessage.getMessagesByTeam(teamId);
            
            messagesContainer.getChildren().clear();
            
            if (messages.isEmpty()) {
                showEmptyState();
            } else {
                for (TeamMessage message : messages) {
                    addMessageToUI(message);
                }
                // Update last message time
                if (!messages.isEmpty()) {
                    lastMessageTime = messages.get(messages.size() - 1).getSentAt();
                }
                scrollToBottom();
            }
            
        } catch (Exception e) {
            System.err.println("Error loading messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show empty state when no messages
     */
    private void showEmptyState() {
        VBox emptyState = new VBox(12);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.getStyleClass().add("chat-empty-state");
        
        Text icon = new Text("💬");
        icon.getStyleClass().add("chat-empty-icon");
        
        Text text1 = new Text("Aucun message pour l'instant");
        text1.getStyleClass().add("chat-empty-text");
        text1.setTextAlignment(TextAlignment.CENTER);
        
        Text text2 = new Text("Soyez le premier à envoyer un message à l'équipe!");
        text2.getStyleClass().add("chat-empty-text");
        text2.setTextAlignment(TextAlignment.CENTER);
        
        emptyState.getChildren().addAll(icon, text1, text2);
        messagesContainer.getChildren().add(emptyState);
    }

    /**
     * Add a message to the UI
     */
    private void addMessageToUI(TeamMessage message) {
        if (message.getMessageType() == TeamMessage.MessageType.SYSTEM) {
            addSystemMessage(message);
        } else {
            addUserMessage(message);
        }
    }

    /**
     * Add a user message bubble
     */
    private void addUserMessage(TeamMessage message) {
        boolean isOwnMessage = message.getSenderId().equals(currentUserId);
        
        // Message container
        HBox messageRow = new HBox();
        messageRow.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageRow.setPadding(new Insets(4, 0, 4, 0));
        
        // Message bubble
        VBox bubble = new VBox(4);
        bubble.getStyleClass().add("message-bubble");
        bubble.getStyleClass().add(isOwnMessage ? "message-bubble-own" : "message-bubble-other");
        bubble.setMaxWidth(messagesContainer.getWidth() * 0.7);
        
        // Sender name (only for other's messages)
        if (!isOwnMessage) {
            Text senderName = new Text(message.getSenderName());
            senderName.getStyleClass().add("message-sender");
            bubble.getChildren().add(senderName);
        }
        
        // Message text
        Text messageText = new Text(message.getMessage());
        messageText.getStyleClass().add("message-text");
        messageText.setWrappingWidth(messagesContainer.getWidth() * 0.65);
        bubble.getChildren().add(messageText);
        
        // Timestamp
        Text timestamp = new Text(message.getSentAt().format(timeFormatter));
        timestamp.getStyleClass().add("message-timestamp");
        bubble.getChildren().add(timestamp);
        
        messageRow.getChildren().add(bubble);
        messagesContainer.getChildren().add(messageRow);
    }

    /**
     * Add a system message (centered)
     */
    private void addSystemMessage(TeamMessage message) {
        HBox messageRow = new HBox();
        messageRow.setAlignment(Pos.CENTER);
        messageRow.setPadding(new Insets(8, 0, 8, 0));
        
        VBox bubble = new VBox(4);
        bubble.getStyleClass().add("message-bubble-system");
        bubble.setAlignment(Pos.CENTER);
        
        Text messageText = new Text(message.getMessage());
        messageText.getStyleClass().add("message-text-system");
        messageText.setTextAlignment(TextAlignment.CENTER);
        
        Text timestamp = new Text(message.getSentAt().format(timeFormatter));
        timestamp.getStyleClass().add("message-timestamp");
        
        bubble.getChildren().addAll(messageText, timestamp);
        messageRow.getChildren().add(bubble);
        messagesContainer.getChildren().add(messageRow);
    }

    /**
     * Handle send message button click
     */
    @FXML
    private void handleSendMessage() {
        String messageText = messageInput.getText().trim();
        
        if (messageText.isEmpty()) {
            return;
        }
        
        try {
            // Create new message
            TeamMessage message = new TeamMessage(teamId, currentUserId, messageText);
            
            // Save to database
            crudTeamMessage.addMessage(message);
            
            // Add to UI immediately (optimistic update)
            message.setSenderName("Vous"); // Temporary name for own message
            addMessageToUI(message);
            
            // Clear input
            messageInput.clear();
            
            // Scroll to bottom
            scrollToBottom();
            
            // Update last message time
            lastMessageTime = message.getSentAt();
            
            System.out.println("Message sent successfully");
            
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Scroll to bottom of messages
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            messagesScrollPane.setVvalue(1.0);
        });
    }

    /**
     * Update online member count
     */
    private void updateOnlineCount() {
        try {
            int memberCount = crudTeamMember.getMemberCount(teamId);
            onlineCountLabel.setText(memberCount + " membre" + (memberCount > 1 ? "s" : "") + " en ligne");
        } catch (Exception e) {
            System.err.println("Error updating online count: " + e.getMessage());
        }
    }

    /**
     * Start auto-refresh for new messages (polling every 3 seconds)
     */
    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            refreshMessages();
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    /**
     * Refresh messages (check for new ones)
     */
    private void refreshMessages() {
        if (lastMessageTime == null) {
            return;
        }
        
        try {
            List<TeamMessage> newMessages = crudTeamMessage.getNewMessages(teamId, lastMessageTime);
            
            if (!newMessages.isEmpty()) {
                // Check if we're at the bottom before adding
                boolean wasAtBottom = messagesScrollPane.getVvalue() >= 0.95;
                
                // Remove empty state if present
                if (messagesContainer.getChildren().size() == 1 && 
                    messagesContainer.getChildren().get(0) instanceof VBox) {
                    VBox firstChild = (VBox) messagesContainer.getChildren().get(0);
                    if (firstChild.getStyleClass().contains("chat-empty-state")) {
                        messagesContainer.getChildren().clear();
                    }
                }
                
                // Add new messages
                for (TeamMessage message : newMessages) {
                    // Skip own messages (already added optimistically)
                    if (!message.getSenderId().equals(currentUserId)) {
                        addMessageToUI(message);
                    }
                }
                
                // Update last message time
                lastMessageTime = newMessages.get(newMessages.size() - 1).getSentAt();
                
                // Auto-scroll if was at bottom
                if (wasAtBottom) {
                    scrollToBottom();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error refreshing messages: " + e.getMessage());
        }
    }

    /**
     * Stop auto-refresh when leaving the chat
     */
    public void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }
}
