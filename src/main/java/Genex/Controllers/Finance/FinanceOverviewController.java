package Genex.Controllers.Finance;

import Genex.entities.Budget;
import Genex.entities.Sponsor;
import Genex.services.CrudBudget;
import Genex.services.CrudMarketplace;
import Genex.services.CrudSponsor;
import Genex.services.CrudSponsorTeam;
import Genex.services.CrudSponsorTournament;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinanceOverviewController {

    // ── Budget ─────────────────────────────────────────────────────────────
    @FXML private Label       lblTotalBudgets;
    @FXML private Label       lblTotalAllocated;
    @FXML private Label       lblTotalSpent;
    @FXML private Label       lblTotalRemaining;
    @FXML private Label       lblBudgetHealth;
    @FXML private ProgressBar budgetHealthBar;
    @FXML private BarChart<String, Number> budgetChart;

    // ── Sponsors ───────────────────────────────────────────────────────────
    @FXML private Label lblTotalSponsors;
    @FXML private Label lblTournamentSponsors;
    @FXML private Label lblTeamSponsors;
    @FXML private Label lblContractValue;
    @FXML private VBox  sponsorBreakdownBox;

    // ── Marketplace ────────────────────────────────────────────────────────
    @FXML private Label lblTotalListings;
    @FXML private Label lblActiveListings;
    @FXML private Label lblTotalOrders;

    // ── Top sponsors + expiry ──────────────────────────────────────────────
    @FXML private VBox  topSponsorsBox;
    @FXML private Label lblExpiringContracts;
    @FXML private VBox  expiringContractsBox;

    @FXML
    public void initialize() {
        loadBudgetStats();
        loadSponsorStats();
        loadMarketplaceStats();
        loadTopSponsors();
        loadExpiringContracts();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BUDGET
    // ══════════════════════════════════════════════════════════════════════

    private void loadBudgetStats() {
        try {
            List<Budget> budgets = new CrudBudget().getAll();

            BigDecimal allocated = budgets.stream().map(Budget::getAllocatedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal spent = budgets.stream().map(Budget::getSpentAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal remaining = allocated.subtract(spent);

            lblTotalBudgets.setText(String.valueOf(budgets.size()));
            lblTotalAllocated.setText(allocated.toPlainString() + " TND");
            lblTotalSpent.setText(spent.toPlainString() + " TND");
            lblTotalRemaining.setText(remaining.toPlainString() + " TND");

            // Health progress bar logic
            if (allocated.compareTo(BigDecimal.ZERO) > 0) {
                double spentPct = spent.multiply(BigDecimal.valueOf(100))
                        .divide(allocated, 2, RoundingMode.HALF_UP).doubleValue();
                double remainingPct = 100.0 - spentPct;
                
                // Set progress based on remaining (so bar decreases as you spend)
                budgetHealthBar.setProgress(remainingPct / 100.0);

                String status;
                String color;
                String styleClass;

                if (spentPct < 60) {
                    status = "STABLE";
                    color = "#00f3ff"; // Neon Cyan
                    styleClass = "progress-cyan";
                } else if (spentPct < 85) {
                    status = "ATTENTION";
                    color = "#f59e0b"; // Warning Orange
                    styleClass = "progress-magenta";
                } else {
                    status = "CRITIQUE";
                    color = "#ff4d4d"; // Neon Red
                    styleClass = "progress-red";
                }

                // Make it make more sense: Show how much is consumed vs how much is left
                lblBudgetHealth.setText(String.format("SYSTÈME %s : %.1f%% DU BUDGET CONSOMMÉ (RESTE: %s TND)", 
                    status, spentPct, remaining.toPlainString()));
                lblBudgetHealth.setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-letter-spacing: 0.5px;");
                
                budgetHealthBar.getStyleClass().removeAll("progress-cyan", "progress-magenta", "progress-red");
                budgetHealthBar.getStyleClass().addAll("cyber-progress", styleClass);
            } else {
                budgetHealthBar.setProgress(0);
                lblBudgetHealth.setText("SYSTÈME : AUCUN BUDGET DÉTECTÉ");
                lblBudgetHealth.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-family: 'Consolas';");
            }

            // Bar chart — allocated vs spent per fiscal year
            buildBudgetChart(budgets);

        } catch (Exception e) {
            lblTotalBudgets.setText("—");
        }
    }

    private void buildBudgetChart(List<Budget> budgets) {
        budgetChart.getData().clear();

        // Group by fiscal year
        Map<Integer, BigDecimal[]> byYear = new LinkedHashMap<>();
        for (Budget b : budgets) {
            byYear.computeIfAbsent(b.getFiscalYear(), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            byYear.get(b.getFiscalYear())[0] = byYear.get(b.getFiscalYear())[0].add(b.getAllocatedAmount());
            byYear.get(b.getFiscalYear())[1] = byYear.get(b.getFiscalYear())[1].add(b.getSpentAmount());
        }

        XYChart.Series<String, Number> allocSeries = new XYChart.Series<>();
        allocSeries.setName("Alloue");
        XYChart.Series<String, Number> spentSeries = new XYChart.Series<>();
        spentSeries.setName("Depense");

        byYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    String year = String.valueOf(e.getKey());
                    allocSeries.getData().add(new XYChart.Data<>(year, e.getValue()[0].doubleValue()));
                    spentSeries.getData().add(new XYChart.Data<>(year, e.getValue()[1].doubleValue()));
                });

        budgetChart.getData().addAll(allocSeries, spentSeries);

        // CSS handles colors via .default-color0 and .default-color1
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SPONSORS
    // ══════════════════════════════════════════════════════════════════════

    private void loadSponsorStats() {
        try {
            List<Sponsor> sponsors = new CrudSponsor().getAll();

            long tournoi = sponsors.stream().filter(s -> s.getSponsorType() != null
                    && s.getSponsorType().contains("Tournoi")).count();
            long equipe  = sponsors.stream().filter(s -> s.getSponsorType() != null
                    && s.getSponsorType().contains("quipe")).count();

            lblTotalSponsors.setText(String.valueOf(sponsors.size()));
            lblTournamentSponsors.setText(String.valueOf(tournoi));
            lblTeamSponsors.setText(String.valueOf(equipe));

            BigDecimal teamVal = new CrudSponsorTeam().getAll().stream()
                    .filter(st -> st.getBudgetAmount() != null)
                    .map(st -> st.getBudgetAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tourVal = new CrudSponsorTournament().getAll().stream()
                    .filter(st -> st.getBudgetAmount() != null)
                    .map(st -> st.getBudgetAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblContractValue.setText(teamVal.add(tourVal).toPlainString() + " TND");

            // Breakdown bars
            buildSponsorBreakdown(sponsors.size(), (int) tournoi, (int) equipe);

        } catch (Exception e) {
            lblTotalSponsors.setText("—");
        }
    }

    private void buildSponsorBreakdown(int total, int tournoi, int equipe) {
        sponsorBreakdownBox.getChildren().clear();
        if (total == 0) return;
        addBreakdownBar(sponsorBreakdownBox, "Tournois", tournoi, total, "progress-cyan");
        addBreakdownBar(sponsorBreakdownBox, "Equipes",  equipe,  total, "progress-magenta");
    }

    private void addBreakdownBar(VBox parent, String label, int count, int total, String color) {
        double pct = total > 0 ? (count * 100.0 / total) : 0;
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lbl = new Label(label.toUpperCase());
        lbl.setStyle("-fx-text-fill: white; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 100; -fx-letter-spacing: 1px;");

        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.getStyleClass().addAll("cyber-progress", color);
        bar.setPrefHeight(12); // Increased height
        HBox.setHgrow(bar, Priority.ALWAYS);
        bar.setMaxWidth(Double.MAX_VALUE);

        Label countLbl = new Label(count + " (" + String.format("%.0f", pct) + "%)");
        countLbl.setStyle("-fx-text-fill: " + (color.contains("cyan") ? "#00f3ff" : "#ff00ff") + "; -fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 90; -fx-alignment: center-right;");

        row.getChildren().addAll(lbl, bar, countLbl);
        parent.getChildren().add(row);

    }

    // ══════════════════════════════════════════════════════════════════════
    //  MARKETPLACE
    // ══════════════════════════════════════════════════════════════════════

    private void loadMarketplaceStats() {
        try {
            CrudMarketplace svc = new CrudMarketplace();
            lblTotalListings.setText(String.valueOf(svc.getTotalListings()));
            lblActiveListings.setText(String.valueOf(svc.getActiveListings()));
            lblTotalOrders.setText(String.valueOf(svc.getTotalOrders()));
        } catch (Exception e) {
            lblTotalListings.setText("—");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  TOP SPONSORS
    // ══════════════════════════════════════════════════════════════════════

    private void loadTopSponsors() {
        topSponsorsBox.getChildren().clear();
        try {
            Map<String, BigDecimal> totals = new LinkedHashMap<>();
            new CrudSponsorTeam().getAll().forEach(st -> {
                if (st.getBudgetAmount() != null)
                    totals.merge(st.getSponsorName(), st.getBudgetAmount(), BigDecimal::add);
            });
            new CrudSponsorTournament().getAll().forEach(st -> {
                if (st.getBudgetAmount() != null)
                    totals.merge(st.getSponsorName(), st.getBudgetAmount(), BigDecimal::add);
            });

            BigDecimal maxVal = totals.values().stream().max(BigDecimal::compareTo).orElse(BigDecimal.ONE);

            totals.entrySet().stream()
                    .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> {
                        VBox row = new VBox(4);
                        HBox header = new HBox();
                        Label name = new Label(e.getKey());
                        name.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                        Label val = new Label(e.getValue().toPlainString() + " TND");
                        val.setStyle("-fx-text-fill: #ff4d4d; -fx-font-size: 12px;");
                        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
                        header.getChildren().addAll(name, spacer, val);

                        // Progress bar relative to max
                        ProgressBar bar = new ProgressBar();
                        double progress = maxVal.compareTo(BigDecimal.ZERO) > 0
                                ? e.getValue().divide(maxVal, 4, RoundingMode.HALF_UP).doubleValue() : 0;
                        bar.setProgress(progress);
                        bar.setMaxWidth(Double.MAX_VALUE);
                        bar.setPrefHeight(6);
                        bar.getStyleClass().addAll("cyber-progress", "progress-red");

                        row.getChildren().addAll(header, bar);
                        topSponsorsBox.getChildren().add(row);
                    });

            if (topSponsorsBox.getChildren().isEmpty()) {
                Label empty = new Label("Aucun contrat enregistre");
                empty.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 12px;");
                topSponsorsBox.getChildren().add(empty);
            }
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EXPIRING CONTRACTS
    // ══════════════════════════════════════════════════════════════════════

    private void loadExpiringContracts() {
        expiringContractsBox.getChildren().clear();
        int count = 0;
        try {
            java.time.LocalDate soon = java.time.LocalDate.now().plusDays(30);

            for (var c : new CrudSponsorTeam().getAll()) {
                if (c.getEndDate() != null && !c.getEndDate().isAfter(soon)) {
                    boolean expired = c.getEndDate().isBefore(java.time.LocalDate.now());
                    addExpiryRow(c.getSponsorName() + " / " + c.getTeamName()
                            + "  —  " + c.getEndDate(), expired);
                    count++;
                }
            }
            for (var c : new CrudSponsorTournament().getAll()) {
                if (c.getEndDate() != null && !c.getEndDate().isAfter(soon)) {
                    boolean expired = c.getEndDate().isBefore(java.time.LocalDate.now());
                    addExpiryRow(c.getSponsorName() + " / " + c.getTournamentName()
                            + "  —  " + c.getEndDate(), expired);
                    count++;
                }
            }

            lblExpiringContracts.setText(count == 0
                    ? "CONTRATS — AUCUNE EXPIRATION PROCHE"
                    : "CONTRATS EXPIRANT BIENTOT  (" + count + ")");
            lblExpiringContracts.setStyle(count == 0
                    ? "-fx-text-fill: #22c55e; -fx-font-size:12px; -fx-font-weight:700;"
                    : "-fx-text-fill: #f59e0b; -fx-font-size:12px; -fx-font-weight:700;");

            if (count == 0) {
                Label ok = new Label("Tous les contrats sont en ordre.");
                ok.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size:12px;");
                expiringContractsBox.getChildren().add(ok);
            }
        } catch (Exception ignored) {}
    }

    private void addExpiryRow(String text, boolean expired) {
        Label lbl = new Label((expired ? "EXPIRE  " : "BIENTOT  ") + text);
        lbl.setStyle("-fx-text-fill: " + (expired ? "#ff4d4d" : "#f59e0b")
                + "; -fx-font-size: 12px;");
        expiringContractsBox.getChildren().add(lbl);
    }
}
