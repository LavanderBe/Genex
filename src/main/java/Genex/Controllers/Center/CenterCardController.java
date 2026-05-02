package Genex.Controllers.Center;

import Genex.entities.Center;
import Genex.services.CrudCenter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class CenterCardController {

    @FXML
    private Label centerName;

    @FXML
    private Label centerCity;

    @FXML
    private Label centerAddress;

    @FXML
    private Label centerEmail;

    @FXML
    private Button btnViewMap;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private Center center;
    private Runnable onClickCallback;
    private Runnable onUpdateCallback;
    private StackPane rootStackPane;
    private VBox contentArea;

    public void setCenter(Center center) {
        this.center = center;
        updateUI();
    }

    public void setRootStackPane(StackPane rootStackPane, VBox contentArea) {
        this.rootStackPane = rootStackPane;
        this.contentArea = contentArea;
    }

    private void updateUI() {
        if (center != null) {
            centerName.setText(center.getName());
            centerCity.setText(center.getCity());
            centerAddress.setText(center.getAddress());
            centerEmail.setText(center.getContactEmail());
        }
    }

    @FXML
    private void handleCardClick() {
        System.out.println("Center card clicked: " + (center != null ? center.getName() : "null"));
        if (onClickCallback != null) {
            onClickCallback.run();
        }
    }

    @FXML
    private void handleViewMap() {
        System.out.println("View map for: " + (center != null ? center.getName() : "null"));
        
        if (center != null && center.getMapUrl() != null && !center.getMapUrl().isEmpty()) {
            try {
                // Open browser with map URL
                java.awt.Desktop.getDesktop().browse(new java.net.URI(center.getMapUrl()));
            } catch (Exception e) {
                System.err.println("Error opening map URL: " + e.getMessage());
            }
        } else {
            showAlert("Carte non disponible", "Aucune URL de carte n'est définie pour ce centre.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleEdit() {
        System.out.println("Edit center: " + (center != null ? center.getName() : "null"));
        
        if (center == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Center/AddCenterModal.fxml"));
            Parent editCenterForm = loader.load();

            // 1. Apply Blur effect to the background
            GaussianBlur blur = new GaussianBlur(15);
            contentArea.setEffect(blur);
            contentArea.setDisable(true);

            // 2. Wrap the form in a darkening overlay
            VBox overlay = new VBox(editCenterForm);
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

            // 3. Add to the stack
            rootStackPane.getChildren().add(overlay);

            // 4. Get controller and set center data
            AddCenterModalController controller = loader.getController();
            controller.setCenter(center);
            controller.setOnSaveCallback(updatedCenter -> {
                System.out.println("Updating center: " + updatedCenter.getName());
                
                // Update in database
                CrudCenter crudCenter = new CrudCenter();
                crudCenter.updateEntity(updatedCenter, center.getCenterId());
                
                // Remove overlay
                rootStackPane.getChildren().remove(overlay);
                contentArea.setEffect(null);
                contentArea.setDisable(false);
                
                // Refresh the hub
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }
            });

            // Handle close without saving
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(overlay);
                contentArea.setEffect(null);
                contentArea.setDisable(false);
            });
            
        } catch (Exception e) {
            System.err.println("Error opening edit modal");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        System.out.println("Delete center: " + (center != null ? center.getName() : "null"));
        
        if (center == null) return;
        
        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer le centre \"" + center.getName() + "\" ?");
        alert.setContentText("Cette action est irréversible.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete from database
                CrudCenter crudCenter = new CrudCenter();
                crudCenter.deleteEntity(center);
                
                System.out.println("Center deleted: " + center.getName());
                
                // Refresh the hub
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }
                
            } catch (Exception e) {
                System.err.println("Error deleting center");
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer le centre.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setOnClickCallback(Runnable callback) {
        this.onClickCallback = callback;
    }

    public void setOnUpdateCallback(Runnable callback) {
        this.onUpdateCallback = callback;
    }
}
