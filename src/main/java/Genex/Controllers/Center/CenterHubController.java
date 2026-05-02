package Genex.Controllers.Center;

import Genex.entities.Center;
import Genex.services.CrudCenter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class CenterHubController {

    @FXML
    private StackPane rootStackPane;

    @FXML
    private VBox contentArea;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnAddCenter;

    @FXML
    private VBox centerCardsContainer;

    @FXML
    private VBox emptyState;

    private CrudCenter crudCenter;
    private List<Center> allCenters;

    @FXML
    public void initialize() {
        System.out.println("CenterHubController initialized");
        
        // Initialize CRUD service
        crudCenter = new CrudCenter();
        
        // Setup search listener
        setupSearchListener();
        
        // Load centers from database
        loadCentersFromDatabase();
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCenters(newValue);
        });
    }

    private void filterCenters(String searchText) {
        centerCardsContainer.getChildren().clear();
        
        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all centers
            displayCenters(allCenters);
        } else {
            // Filter centers by name or city
            String search = searchText.toLowerCase();
            List<Center> filtered = allCenters.stream()
                .filter(c -> c.getName().toLowerCase().contains(search) || 
                            c.getCity().toLowerCase().contains(search))
                .toList();
            displayCenters(filtered);
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = centerCardsContainer.getChildren().isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
    }

    @FXML
    private void openAddCenterModal() {
        try {
            System.out.println("Opening Add Center Modal...");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Center/AddCenterModal.fxml"));
            Parent addCenterForm = loader.load();

            // 1. Apply Blur effect to the background
            GaussianBlur blur = new GaussianBlur(15);
            contentArea.setEffect(blur);
            contentArea.setDisable(true); // Prevent clicking background items

            // 2. Wrap the form in a darkening overlay (dimmer)
            VBox overlay = new VBox(addCenterForm);
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Dim background

            // 3. Add to the stack
            rootStackPane.getChildren().add(overlay);

            // 4. Pass a "Close" callback to the AddCenterModalController
            AddCenterModalController controller = loader.getController();
            controller.setOnSaveCallback(center -> {
                System.out.println("Saving center: " + center.getName());

                // Save to database
                crudCenter.addEntity(center);

                // Remove overlay and reload
                rootStackPane.getChildren().remove(overlay);
                contentArea.setEffect(null);
                contentArea.setDisable(false);
                loadCentersFromDatabase();
            });

            // Also handle close without saving
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(overlay);
                contentArea.setEffect(null);
                contentArea.setDisable(false);
            });
            
        } catch (Exception e) {
            System.err.println("Error opening Add Center Modal");
            e.printStackTrace();
        }
    }

    private void loadCentersFromDatabase() {
        try {
            System.out.println("Loading centers from database...");
            
            // Get all centers from database
            allCenters = crudCenter.getAll();
            
            System.out.println("Loaded " + allCenters.size() + " centers");
            
            // Display centers
            displayCenters(allCenters);
            
        } catch (Exception e) {
            System.err.println("Error loading centers from database");
            e.printStackTrace();
        }
    }

    private void displayCenters(List<Center> centers) {
        centerCardsContainer.getChildren().clear();
        
        for (Center center : centers) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Center/CenterCard.fxml"));
                Parent card = loader.load();
                
                CenterCardController cardController = loader.getController();
                cardController.setCenter(center);
                cardController.setRootStackPane(rootStackPane, contentArea);
                
                // Set callback to reload centers when card is updated/deleted
                cardController.setOnUpdateCallback(this::loadCentersFromDatabase);
                
                centerCardsContainer.getChildren().add(card);
                
            } catch (Exception e) {
                System.err.println("Error creating center card for: " + center.getName());
                e.printStackTrace();
            }
        }
        
        updateEmptyState();
    }

    public void addCenterCard(Parent card) {
        centerCardsContainer.getChildren().add(card);
        updateEmptyState();
    }

    public void clearCenters() {
        centerCardsContainer.getChildren().clear();
        updateEmptyState();
    }
}
