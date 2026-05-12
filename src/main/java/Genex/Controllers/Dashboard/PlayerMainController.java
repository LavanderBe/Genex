package Genex.Controllers.Dashboard;

import Genex.services.ExchangeService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerMainController {

    @FXML private Label            exchangeLastUpdate;
    @FXML private ListView<String> exchangeRatesList;
    @FXML private TextField        convertAmount;
    @FXML private ComboBox<String> convertFrom;
    @FXML private ComboBox<String> convertTo;
    @FXML private Label            convertResult;
    @FXML private Label            convertRate;

    @FXML private Button gamebtn;

    private final ExchangeService service = new ExchangeService();

    @FXML
    public void initialize() {
        setupCombos();
        loadRates();
        styleListView();
    }

    private void setupCombos() {
        List<String> codes = new ArrayList<>(ExchangeService.POPULAR);
        List<String> upper = codes.stream().map(String::toUpperCase).toList();
        convertFrom.setItems(FXCollections.observableArrayList(upper));
        convertTo.setItems(FXCollections.observableArrayList(upper));
        convertFrom.setValue("TND");
        convertTo.setValue("USD");
    }

    private void loadRates() {
        exchangeRatesList.setItems(FXCollections.observableArrayList("Chargement..."));
        Thread t = new Thread(() -> {
            try {
                Map<String, Double> rates = service.getRates("tnd");
                List<String> lines = new ArrayList<>();
                for (String code : new String[]{"usd", "eur"}) {
                    Double rate = rates.get(code);
                    if (rate == null) continue;
                    double inTND = rate > 0 ? 1.0 / rate : 0;
                    lines.add(String.format("1 %-5s  =  %.4f TND", code.toUpperCase(), inTND));
                }
                Platform.runLater(() -> {
                    exchangeRatesList.setItems(FXCollections.observableArrayList(lines));
                    exchangeLastUpdate.setText("Mis a jour : " + java.time.LocalTime.now().withNano(0));
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    exchangeRatesList.setItems(FXCollections.observableArrayList("Erreur : " + e.getMessage())));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** Force dark styling on the ListView cells via code since CSS inheritance can be tricky. */
    private void styleListView() {
        exchangeRatesList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item);
                    setStyle("-fx-background-color: transparent;" +
                             "-fx-text-fill: rgba(255,255,255,0.85);" +
                             "-fx-font-family: Consolas, monospace;" +
                             "-fx-font-size: 18px;" +
                             "-fx-padding: 6 14;");
                }
            }
        });
    }

    @FXML
    private void handleConvert() {
        String amtText = convertAmount.getText().trim();
        if (amtText.isBlank()) { convertResult.setText("Entrez un montant"); return; }
        double amount;
        try { amount = Double.parseDouble(amtText); }
        catch (Exception e) { convertResult.setText("Montant invalide"); return; }

        String from = convertFrom.getValue();
        String to   = convertTo.getValue();
        if (from == null || to == null) return;

        convertResult.setText("...");
        convertRate.setText("...");

        Thread t = new Thread(() -> {
            try {
                BigDecimal result = service.convert(amount, from.toLowerCase(), to.toLowerCase());
                Map<String, Double> rates = service.getRates(from.toLowerCase());
                Double rate = rates.get(to.toLowerCase());
                Platform.runLater(() -> {
                    convertResult.setText(String.format("%.4f %s", result.doubleValue(), to));
                    if (rate != null)
                        convertRate.setText(String.format("1 %s = %.6f %s", from, rate, to));
                });
            } catch (Exception e) {
                Platform.runLater(() -> convertResult.setText("Erreur: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    void HandleGame(ActionEvent event) {

    }
}
