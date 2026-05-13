package Genex.Controllers.Boutique;

import Genex.entities.MarketplaceItem;
import Genex.entities.MarketplaceItem.ItemCondition;
import Genex.entities.MarketplaceItem.ProductType;
import Genex.entities.MarketplaceOrder;
import Genex.entities.MarketplaceOrder.OrderStatus;
import Genex.entities.MarketplaceOrder.PaymentMethod;
import Genex.entities.MarketplaceOrder.PaymentStatus;
import Genex.entities.MarketplaceReview;
import Genex.entities.User;
import Genex.services.CrudMarketplace;
import Genex.services.CrudMarketplace.CartEntry;
import Genex.services.StripeService;
import Genex.services.BlockchainService;
import Genex.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BoutiqueController {

    // ── Header filters ─────────────────────────────────────────────────────
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterCondition;
    @FXML private TextField        filterMinPrice;
    @FXML private TextField        filterMaxPrice;
    @FXML private TabPane          tabPane;

    // ── Browse ─────────────────────────────────────────────────────────────
    @FXML private FlowPane cardGrid;

    // ── Cart tab ───────────────────────────────────────────────────────────
    @FXML private TableView<CartEntry>               cartTable;
    @FXML private TableColumn<CartEntry, String>     cartColName;
    @FXML private TableColumn<CartEntry, String>     cartColPrice;
    @FXML private TableColumn<CartEntry, String>     cartColQty;
    @FXML private TableColumn<CartEntry, String>     cartColTotal;
    @FXML private TableColumn<CartEntry, Void>       cartColActions;
    @FXML private Label                              cartTotalLabel;

    // ── Wishlist ───────────────────────────────────────────────────────────
    @FXML private FlowPane wishlistGrid;

    // ── Sell form ──────────────────────────────────────────────────────────
    @FXML private TextField              sellName;
    @FXML private ComboBox<ProductType>  sellType;
    @FXML private ComboBox<ItemCondition> sellCondition;
    @FXML private TextField              sellPrice;
    @FXML private TextField              sellQty;
    @FXML private CheckBox               sellHasDelivery;
    @FXML private TextField              sellPhone;
    @FXML private VBox                   sellPhoneRow;
    @FXML private HBox                   sellImagesPreview;
    @FXML private Label                  sellImagesCount;
    @FXML private Label                  errSellName;
    @FXML private Label                  errSellPrice;
    @FXML private Label                  errSellPhone;

    // ── My listings ────────────────────────────────────────────────────────
    @FXML private FlowPane myListingsGrid;
    @FXML private TableView<MarketplaceOrder>           sellerOrdersTable;
    @FXML private TableColumn<MarketplaceOrder, String> soColItem;
    @FXML private TableColumn<MarketplaceOrder, String> soColBuyer;
    @FXML private TableColumn<MarketplaceOrder, String> soColQty;
    @FXML private TableColumn<MarketplaceOrder, String> soColStatus;
    @FXML private TableColumn<MarketplaceOrder, Void>   soColActions;

    // ── My orders ──────────────────────────────────────────────────────────
    @FXML private TableView<MarketplaceOrder>           myOrdersTable;
    @FXML private TableColumn<MarketplaceOrder, String> moColItem;
    @FXML private TableColumn<MarketplaceOrder, String> moColQty;
    @FXML private TableColumn<MarketplaceOrder, String> moColMethod;
    @FXML private TableColumn<MarketplaceOrder, String> moColOrderStatus;
    @FXML private TableColumn<MarketplaceOrder, String> moColDate;
    @FXML private TableColumn<MarketplaceOrder, Void>   moColReview;

    // ── Product viewer overlay ─────────────────────────────────────────────
    @FXML private StackPane  productOverlay;
    @FXML private ImageView  viewerImage;
    @FXML private Button     viewerPrev;
    @FXML private Button     viewerNext;
    @FXML private Label      viewerCounter;
    @FXML private Label      viewerPanelTitle;
    @FXML private VBox       buyerPanel;
    @FXML private ScrollPane vendorPanel;
    // Buyer panel fields
    @FXML private Label           vpName;
    @FXML private Label           vpType;
    @FXML private Label           vpCondition;
    @FXML private Label           vpPrice;
    @FXML private Label           vpSeller;
    @FXML private Label           vpRating;
    @FXML private Label           vpDelivery;
    @FXML private Spinner<Integer> vpQtySpinner;
    @FXML private Label           vpTotal;
    // Vendor edit fields
    @FXML private TextField              editName;
    @FXML private ComboBox<ProductType>  editType;
    @FXML private ComboBox<ItemCondition> editCondition;
    @FXML private TextField              editPrice;
    @FXML private TextField              editQty;
    @FXML private CheckBox               editHasDelivery;
    @FXML private TextField              editPhone;
    @FXML private VBox                   editPhoneRow;
    @FXML private HBox                   editImagesPreview;
    @FXML private Label                  editImagesCount;
    @FXML private Label                  errEditName;
    @FXML private Label                  errEditPrice;

    // ── Buy drawer ─────────────────────────────────────────────────────────
    @FXML private StackPane               buyDrawer;
    @FXML private Label                   buyItemName;
    @FXML private Label                   buyItemCondition;
    @FXML private Label                   buyItemPrice;
    @FXML private Label                   buyItemSeller;
    @FXML private Label                   buySellerRating;
    @FXML private HBox                    buyImagesRow;
    @FXML private Spinner<Integer>        buyQtySpinner;
    @FXML private ComboBox<PaymentMethod> buyPaymentMethod;
    @FXML private Label                   buyTotal;
    @FXML private Label                   errBuy;

    // ── Review drawer ──────────────────────────────────────────────────────
    @FXML private StackPane        reviewDrawer;
    @FXML private Label            reviewItemLabel;
    @FXML private Spinner<Integer> reviewRating;
    @FXML private TextArea         reviewComment;

    // ── Stripe WebView overlay ─────────────────────────────────────────────
    @FXML private StackPane stripeOverlay;
    @FXML private WebView   stripeWebView;
    private String          pendingOrderId;
    private String          pendingSessionId;

    // ── Blockchain overlay ────────────────────────────────────────────────
    @FXML private StackPane blockchainOverlay;
    @FXML private TextField bcWalletAddress;
    @FXML private Label     bcAmountLabel;
    @FXML private Label     bcTotalTndLabel;
    @FXML private TextField bcTxHash;
    @FXML private Label     errBc;

    // ── State ──────────────────────────────────────────────────────────────
    private CrudMarketplace service;
    private StripeService   stripe;
    private BlockchainService blockchain;
    private User            currentUser;

    // Product viewer state
    private MarketplaceItem viewerItem;
    private List<String>    viewerPaths = new ArrayList<>();
    private int             viewerIndex = 0;

    // Edit state
    private MarketplaceItem editingItem;
    private final List<String> editImagePaths = new ArrayList<>();

    // Buy state
    private MarketplaceItem selectedItem;
    private MarketplaceOrder reviewTargetOrder;
    private final List<String> selectedImagePaths = new ArrayList<>();

    private final ObservableList<MarketplaceItem>  browseData    = FXCollections.observableArrayList();
    private final ObservableList<MarketplaceItem>  myListingData = FXCollections.observableArrayList();
    private final ObservableList<MarketplaceOrder> myOrderData   = FXCollections.observableArrayList();
    private final ObservableList<MarketplaceOrder> sellerOrderData = FXCollections.observableArrayList();
    private final ObservableList<CartEntry>        cartData      = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        service     = new CrudMarketplace();
        stripe      = new StripeService();
        blockchain  = new BlockchainService();
        currentUser = SessionManager.getInstance().getCurrentUser();

        setupFilterCombos();
        setupSellForm();
        setupEditForm();
        setupCartTable();
        setupMyOrdersTable();
        setupSellerOrdersTable();
        setupBuyDrawer();
        setupReviewDrawer();

        loadBrowse();
        loadWishlist();
        loadMyListings();
        loadMyOrders();
        loadSellerOrders();
        loadCart();

        searchField.textProperty().addListener((obs, o, v) -> applyFilter());
        filterType.valueProperty().addListener((obs, o, v) -> applyFilter());
        filterCondition.valueProperty().addListener((obs, o, v) -> applyFilter());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SETUP
    // ══════════════════════════════════════════════════════════════════════

    private void setupFilterCombos() {
        List<String> types = new ArrayList<>(); types.add("Tous");
        for (ProductType t : ProductType.values()) types.add(t.getLabel());
        filterType.setItems(FXCollections.observableArrayList(types)); filterType.setValue("Tous");
        List<String> conds = new ArrayList<>(); conds.add("Tous");
        for (ItemCondition c : ItemCondition.values()) conds.add(c.getLabel());
        filterCondition.setItems(FXCollections.observableArrayList(conds)); filterCondition.setValue("Tous");
    }

    private void setupSellForm() {
        sellType.setItems(FXCollections.observableArrayList(ProductType.values()));
        sellCondition.setItems(FXCollections.observableArrayList(ItemCondition.values()));
        sellCondition.setValue(ItemCondition.NEUF);
        sellHasDelivery.selectedProperty().addListener((obs, o, v) -> { sellPhoneRow.setVisible(v); sellPhoneRow.setManaged(v); });
        sellPhoneRow.setVisible(false); sellPhoneRow.setManaged(false);
    }

    private void setupEditForm() {
        editType.setItems(FXCollections.observableArrayList(ProductType.values()));
        editCondition.setItems(FXCollections.observableArrayList(ItemCondition.values()));
        editHasDelivery.selectedProperty().addListener((obs, o, v) -> { editPhoneRow.setVisible(v); editPhoneRow.setManaged(v); });
    }

    private void setupCartTable() {
        cartColName.setCellValueFactory(cd  -> new SimpleStringProperty(cd.getValue().item.getProductName()));
        cartColPrice.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().item.getPriceDisplay()));
        cartColQty.setCellValueFactory(cd   -> new SimpleStringProperty(String.valueOf(cd.getValue().quantity)));
        cartColTotal.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getLineTotal().toPlainString() + " TND"));
        cartColActions.setCellFactory(col -> new TableCell<>() {
            private final Button rem = new Button("Retirer"); { rem.getStyleClass().add("btn-card-delete");
                rem.setOnAction(e -> { CartEntry ce = getTableView().getItems().get(getIndex());
                    if (currentUser != null) { service.removeFromCart(currentUser.getId(), ce.item.getId()); loadCart(); } }); }
            @Override protected void updateItem(Void v, boolean empty) { super.updateItem(v, empty); setGraphic(empty ? null : rem); }
        });
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupBuyDrawer() {
        buyPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        buyPaymentMethod.setValue(PaymentMethod.STRIPE);
        buyQtySpinner.valueProperty().addListener((obs, o, v) -> updateBuyTotal());
    }

    private void setupReviewDrawer() {
        reviewRating.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 5));
    }

    private void setupMyOrdersTable() {
        moColItem.setCellValueFactory(cd        -> new SimpleStringProperty(cd.getValue().getItemName()));
        moColQty.setCellValueFactory(cd         -> new SimpleStringProperty(String.valueOf(cd.getValue().getQuantityBought())));
        moColMethod.setCellValueFactory(cd      -> new SimpleStringProperty(cd.getValue().getMethodLabel()));
        moColOrderStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getOrderStatusLabel()));
        moColDate.setCellValueFactory(cd        -> new SimpleStringProperty(cd.getValue().getOrderedAt() != null ? cd.getValue().getOrderedAt().toLocalDate().toString() : "-"));
        moColReview.setCellFactory(col -> new TableCell<>() {
            private final Button reviewBtn = new Button("Avis");
            private final Button verifyBtn = new Button("Verifier");
            { reviewBtn.getStyleClass().add("btn-card-buy"); verifyBtn.getStyleClass().add("btn-card-buy");
              reviewBtn.setOnAction(e -> openReviewDrawer(getTableView().getItems().get(getIndex())));
              verifyBtn.setOnAction(e -> verifyStripePayment(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty); if (empty) { setGraphic(null); return; }
                MarketplaceOrder o = getTableView().getItems().get(getIndex());
                if (o.getPaymentMethod() == PaymentMethod.STRIPE && o.getPaymentStatus() == PaymentStatus.PENDING && o.getPaymentRef() != null) { setGraphic(verifyBtn); return; }
                boolean canReview = o.getOrderStatus() == OrderStatus.DELIVERED && !service.hasReviewed(o.getId());
                reviewBtn.setDisable(!canReview); setGraphic(reviewBtn);
            }
        });
        myOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupSellerOrdersTable() {
        soColItem.setCellValueFactory(cd   -> new SimpleStringProperty(cd.getValue().getItemName()));
        soColBuyer.setCellValueFactory(cd  -> new SimpleStringProperty(cd.getValue().getBuyerName()));
        soColQty.setCellValueFactory(cd    -> new SimpleStringProperty(String.valueOf(cd.getValue().getQuantityBought())));
        soColStatus.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getOrderStatusLabel()));
        soColActions.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<OrderStatus> combo = new ComboBox<>();
            private final Button btn = new Button("Mettre a jour");
            private final HBox box = new HBox(6, combo, btn);
            {
                combo.setItems(FXCollections.observableArrayList(OrderStatus.values()));
                combo.getStyleClass().add("combo-field");
                btn.getStyleClass().add("btn-card-buy");
                btn.setOnAction(e -> {
                    MarketplaceOrder o = getTableView().getItems().get(getIndex());
                    if (combo.getValue() != null) {
                        service.updateOrderStatus(o.getId(), combo.getValue());
                        loadSellerOrders();
                        loadMyOrders();
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                combo.setValue(getTableView().getItems().get(getIndex()).getOrderStatus());
                setGraphic(box);
            }
        });
        sellerOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BROWSE + FILTER
    // ══════════════════════════════════════════════════════════════════════

    private void loadBrowse() {
        try { browseData.setAll(service.getAvailableItems()); renderCards(browseData, cardGrid, false, false); }
        catch (Exception e) { showAlert("Erreur", e.getMessage()); }
    }

    private void applyFilter() { applyFilterBtn(); }

    @FXML
    private void applyFilterBtn() {
        String q = searchField.getText(); String type = filterType.getValue(); String cond = filterCondition.getValue();
        BigDecimal minP = parseBD(filterMinPrice.getText()); BigDecimal maxP = parseBD(filterMaxPrice.getText());
        List<MarketplaceItem> filtered = browseData.filtered(item -> {
            boolean mQ = q == null || q.isBlank() || item.getProductName().toLowerCase().contains(q.toLowerCase()) || item.getSellerName().toLowerCase().contains(q.toLowerCase());
            boolean mT = type == null || type.equals("Tous") || item.getTypeLabel().equals(type);
            boolean mC = cond == null || cond.equals("Tous") || item.getConditionLabel().equals(cond);
            boolean mMin = minP == null || (item.getPrice() != null && item.getPrice().compareTo(minP) >= 0);
            boolean mMax = maxP == null || (item.getPrice() != null && item.getPrice().compareTo(maxP) <= 0);
            return mQ && mT && mC && mMin && mMax;
        });
        renderCards(filtered, cardGrid, false, false);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CART
    // ══════════════════════════════════════════════════════════════════════

    private void loadCart() {
        if (currentUser == null) return;
        try {
            cartData.setAll(service.getCart(currentUser.getId()));
            cartTable.setItems(cartData);
            BigDecimal total = cartData.stream().map(CartEntry::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            cartTotalLabel.setText("Total : " + total.toPlainString() + " TND");
        } catch (Exception e) {
            // Non-critical — cart display failed but item was added
            cartData.clear();
            cartTable.setItems(cartData);
            cartTotalLabel.setText("Total : — TND");
            System.err.println("loadCart error: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearCart() {
        if (currentUser == null) return;
        service.clearCart(currentUser.getId()); loadCart();
    }

    @FXML
    private void handleCheckoutCart() {
        if (cartData.isEmpty()) { showAlert("Panier vide", "Ajoutez des articles avant de commander."); return; }
        // Open buy drawer for the first item — in a real app you'd loop through all
        CartEntry first = cartData.get(0);
        openBuyDrawer(first.item, first.quantity);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  WISHLIST
    // ══════════════════════════════════════════════════════════════════════

    private void loadWishlist() {
        if (currentUser == null) return;
        try { List<MarketplaceItem> items = service.getWishlist(currentUser.getId()); renderCards(items, wishlistGrid, false, true); }
        catch (Exception e) { showAlert("Erreur", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CARD BUILDER
    // ══════════════════════════════════════════════════════════════════════

    private void renderCards(Iterable<MarketplaceItem> items, FlowPane pane, boolean isMyListings, boolean isWishlist) {
        pane.getChildren().clear(); boolean any = false;
        for (MarketplaceItem item : items) { pane.getChildren().add(buildCard(item, isMyListings, isWishlist)); any = true; }
        if (!any) { Label e = new Label(isMyListings ? "Aucune annonce" : isWishlist ? "Aucun favori" : "Aucun article"); e.getStyleClass().add("table-placeholder"); pane.getChildren().add(e); }
    }

    private VBox buildCard(MarketplaceItem item, boolean isMyListings, boolean isWishlist) {
        VBox card = new VBox(0); card.getStyleClass().add("product-card"); card.setPrefWidth(220); card.setMaxWidth(220);
        StackPane imgContainer = new StackPane(); imgContainer.getStyleClass().add("card-img-container"); imgContainer.setPrefHeight(160);
        ImageView iv = new ImageView(); iv.setFitWidth(220); iv.setFitHeight(180); iv.setPreserveRatio(false);
        String fp = item.getFirstImagePath();
        if (fp != null) { try { File f = new File(fp); if (f.exists()) iv.setImage(new Image(f.toURI().toString(), 220, 180, false, true, true)); } catch (Exception ignored) {} }
        if (item.getStatus() == MarketplaceItem.ItemStatus.SOLD_OUT) { Label s = new Label("EPUISE"); s.getStyleClass().add("sold-out-badge"); imgContainer.getChildren().addAll(iv, s); } else imgContainer.getChildren().add(iv);
        if (item.getAllImagePaths().size() > 1) { Label m = new Label("+" + (item.getAllImagePaths().size()-1) + " photos"); m.getStyleClass().add("multi-img-badge"); StackPane.setAlignment(m, Pos.BOTTOM_RIGHT); imgContainer.getChildren().add(m); }

        VBox info = new VBox(4); info.getStyleClass().add("card-info"); info.setPadding(new Insets(10,12,8,12));
        Label name = new Label(item.getProductName()); name.getStyleClass().add("card-name"); name.setWrapText(true);
        Label type = new Label(item.getTypeLabel()); type.getStyleClass().add("card-type");
        Label cond = new Label(item.getConditionLabel()); cond.getStyleClass().add("card-seller");
        Label price = new Label(item.getPriceDisplay()); price.getStyleClass().add("card-price");
        Label seller = new Label("par " + item.getSellerName()); seller.getStyleClass().add("card-seller");
        double rating = 0; try { rating = service.getSellerRating(item.getSellerId()); } catch (Exception ignored) {}
        Label ratingLbl = new Label(rating > 0 ? String.format("%.1f ★", rating) : "Nouveau vendeur"); ratingLbl.getStyleClass().add("card-seller");
        HBox meta = new HBox(8);
        if (item.isHasDelivery()) { Label d = new Label("Livraison"); d.getStyleClass().add("card-badge-delivery"); meta.getChildren().add(d); }
        if (item.getQuantity() != null) { Label q = new Label("Stock: "+item.getQuantity()); q.getStyleClass().add("card-badge-stock"); meta.getChildren().add(q); }
        info.getChildren().addAll(name, type, cond, price, seller, ratingLbl);
        if (!meta.getChildren().isEmpty()) info.getChildren().add(meta);

        HBox btnRow = new HBox(6); btnRow.getStyleClass().add("card-btn-row"); btnRow.setPadding(new Insets(0,12,12,12));
        boolean isOwn = currentUser != null && item.getSellerId().equals(currentUser.getId());

        if (isMyListings) {
            Button del = new Button("Retirer"); del.getStyleClass().add("btn-card-delete"); del.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(del, Priority.ALWAYS);
            del.setOnAction(e -> { Alert c = new Alert(Alert.AlertType.CONFIRMATION,"Retirer ?",ButtonType.YES,ButtonType.NO); c.setHeaderText(null); c.showAndWait().ifPresent(b -> { if (b==ButtonType.YES) { try { service.deleteItem(item.getId()); loadMyListings(); loadBrowse(); } catch (Exception ex) { showAlert("Erreur",ex.getMessage()); } } }); });
            btnRow.getChildren().add(del);
        } else {
            boolean inWish = currentUser != null && service.isInWishlist(currentUser.getId(), item.getId());
            Button wish = new Button(inWish ? "♥" : "♡"); wish.getStyleClass().add("btn-card-delete");
            wish.setOnAction(e -> { if (currentUser==null) return; if (service.isInWishlist(currentUser.getId(),item.getId())) service.removeFromWishlist(currentUser.getId(),item.getId()); else service.addToWishlist(currentUser.getId(),item.getId()); loadBrowse(); loadWishlist(); });
            btnRow.getChildren().add(wish);
            if (!isOwn && !isWishlist) {
                Button cart = new Button("🛒"); cart.getStyleClass().add("btn-card-delete");
                cart.setDisable(item.getStatus() == MarketplaceItem.ItemStatus.SOLD_OUT);
                cart.setOnAction(e -> {
                    if (currentUser == null) { showAlert("Erreur", "Vous devez etre connecte."); return; }
                    try {
                        service.addToCart(currentUser.getId(), item.getId(), 1);
                        loadCart();
                        // Visual feedback — briefly change button text
                        cart.setText("✓");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        showAlert("Erreur panier", "Impossible d'ajouter au panier.\nVerifiez que la table marketplace_cart existe dans la DB.\n" + ex.getMessage());
                    }
                });
                btnRow.getChildren().add(cart);
            }
        }

        card.getChildren().addAll(imgContainer, info, btnRow);
        imgContainer.setOnMouseClicked(e -> { openProductViewer(item); e.consume(); });
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRODUCT VIEWER (arrows + buyer/vendor panels)
    // ══════════════════════════════════════════════════════════════════════

    private void openProductViewer(MarketplaceItem item) {
        viewerItem  = item;
        viewerPaths = new ArrayList<>(item.getAllImagePaths()); // mutable copy
        viewerIndex = 0;
        updateViewerImage();

        boolean isOwn = currentUser != null && item.getSellerId().equals(currentUser.getId());

        if (isOwn) {
            // Vendor mode — show edit form
            viewerPanelTitle.setText("MODIFIER L'ARTICLE");
            buyerPanel.setVisible(false); buyerPanel.setManaged(false);
            vendorPanel.setVisible(true); vendorPanel.setManaged(true);
            populateEditForm(item);
        } else {
            // Buyer mode — show product details
            viewerPanelTitle.setText("DETAIL DE L'ARTICLE");
            vendorPanel.setVisible(false); vendorPanel.setManaged(false);
            buyerPanel.setVisible(true); buyerPanel.setManaged(true);
            populateBuyerPanel(item);
        }

        productOverlay.setVisible(true);
        productOverlay.setManaged(true);
    }

    private void updateViewerImage() {
        if (viewerPaths.isEmpty()) {
            viewerImage.setImage(null);
            viewerCounter.setText("0 / 0");
            viewerPrev.setDisable(true);
            viewerNext.setDisable(true);
            return;
        }
        String path = viewerPaths.get(viewerIndex);
        try { File f = new File(path); if (f.exists()) viewerImage.setImage(new Image(f.toURI().toString(), 560, 560, true, true)); }
        catch (Exception ignored) {}
        viewerCounter.setText((viewerIndex + 1) + " / " + viewerPaths.size());
        viewerPrev.setDisable(viewerIndex == 0);
        viewerNext.setDisable(viewerIndex == viewerPaths.size() - 1);
    }

    @FXML private void viewerPrevImage() { if (viewerIndex > 0) { viewerIndex--; updateViewerImage(); } }
    @FXML private void viewerNextImage() { if (viewerIndex < viewerPaths.size()-1) { viewerIndex++; updateViewerImage(); } }

    @FXML
    private void closeProductViewer() {
        productOverlay.setVisible(false);
        productOverlay.setManaged(false);
        viewerItem = null;
        viewerPaths = new ArrayList<>();  // always a fresh mutable list
        viewerIndex = 0;
    }

    private void populateBuyerPanel(MarketplaceItem item) {
        vpName.setText(item.getProductName());
        vpType.setText(item.getTypeLabel());
        vpCondition.setText("Etat : " + item.getConditionLabel());
        vpPrice.setText(item.getPriceDisplay() + " / unite");
        vpSeller.setText("Vendeur : " + item.getSellerName());
        double rating = 0; try { rating = service.getSellerRating(item.getSellerId()); } catch (Exception ignored) {}
        vpRating.setText(rating > 0 ? String.format("Note : %.1f / 5", rating) : "Nouveau vendeur");
        vpDelivery.setText(item.isHasDelivery() ? "Livraison disponible" : "Pas de livraison");
        int maxQty = item.getQuantity() != null ? item.getQuantity() : 1;
        vpQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQty, 1));
        vpQtySpinner.setDisable(item.getQuantity() == null);
        vpQtySpinner.valueProperty().addListener((obs, o, v) -> updateVpTotal(item));
        updateVpTotal(item);
    }

    private void updateVpTotal(MarketplaceItem item) {
        if (item.getPrice() == null) return;
        int qty = vpQtySpinner.getValue() != null ? vpQtySpinner.getValue() : 1;
        vpTotal.setText("Total : " + item.getPrice().multiply(BigDecimal.valueOf(qty)).toPlainString() + " TND");
    }

    @FXML
    private void handleAddToCart() {
        if (currentUser == null) { showAlert("Erreur", "Vous devez etre connecte."); return; }
        if (viewerItem == null) return;
        int qty = vpQtySpinner.getValue() != null ? vpQtySpinner.getValue() : 1;
        try {
            service.addToCart(currentUser.getId(), viewerItem.getId(), qty);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            showAlert("Erreur panier", cause.getClass().getSimpleName() + ": " + cause.getMessage());
            return;
        }
        loadCart(); // silent — won't show error even if display fails
        closeProductViewer();
        tabPane.getSelectionModel().select(1);
        showInfo("Ajoute au panier !");
    }

    @FXML
    private void handleBuyNow() {
        if (currentUser == null) { showAlert("Erreur", "Vous devez etre connecte."); return; }
        if (viewerItem == null) { showAlert("Erreur", "Aucun article selectionne."); return; }
        int qty = vpQtySpinner.getValue() != null ? vpQtySpinner.getValue() : 1;
        closeProductViewer();
        openBuyDrawer(viewerItem, qty);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  VENDOR EDIT (inside product viewer)
    // ══════════════════════════════════════════════════════════════════════

    private void populateEditForm(MarketplaceItem item) {
        editingItem = item;
        editImagePaths.clear();
        if (item.getImagePaths() != null && !item.getImagePaths().isBlank())
            for (String p : item.getImagePaths().split(",")) { String t = p.trim(); if (!t.isEmpty()) editImagePaths.add(t); }
        editName.setText(item.getProductName());
        editType.setValue(item.getProductType());
        editCondition.setValue(item.getCondition() != null ? item.getCondition() : ItemCondition.NEUF);
        editPrice.setText(item.getPrice() != null ? item.getPrice().toPlainString() : "");
        editQty.setText(item.getQuantity() != null ? String.valueOf(item.getQuantity()) : "");
        editHasDelivery.setSelected(item.isHasDelivery());
        editPhone.setText(item.getSellerPhone() != null ? item.getSellerPhone() : "");
        editPhoneRow.setVisible(item.isHasDelivery()); editPhoneRow.setManaged(item.isHasDelivery());
        refreshEditImagePreviews();
        hideErr(errEditName); hideErr(errEditPrice);
    }

    @FXML
    private void handleEditPickImages() {
        FileChooser fc = new FileChooser(); fc.setTitle("Choisir des photos");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg"));
        List<File> files = fc.showOpenMultipleDialog(editName.getScene().getWindow());
        if (files == null || files.isEmpty()) return;
        for (File f : files) if (!editImagePaths.contains(f.getAbsolutePath())) editImagePaths.add(f.getAbsolutePath());
        refreshEditImagePreviews();
        // Refresh viewer images too
        viewerPaths = new ArrayList<>(editImagePaths);
        viewerIndex = 0;
        updateViewerImage();
    }

    private void refreshEditImagePreviews() {
        editImagesPreview.getChildren().clear();
        for (String path : editImagePaths) {
            try { File f = new File(path); if (!f.exists()) continue;
                StackPane thumb = new StackPane(); thumb.getStyleClass().add("img-thumb");
                ImageView iv = new ImageView(new Image(f.toURI().toString(), 55, 55, true, true));
                Button rem = new Button("X"); rem.getStyleClass().add("img-thumb-remove");
                StackPane.setAlignment(rem, Pos.TOP_RIGHT);
                rem.setOnAction(e -> { editImagePaths.remove(path); refreshEditImagePreviews(); viewerPaths = new ArrayList<>(editImagePaths); viewerIndex = Math.min(viewerIndex, Math.max(0, viewerPaths.size()-1)); updateViewerImage(); });
                thumb.getChildren().addAll(iv, rem); editImagesPreview.getChildren().add(thumb);
            } catch (Exception ignored) {}
        }
        editImagesCount.setText(editImagePaths.isEmpty() ? "Aucune image" : editImagePaths.size() + " image(s)");
    }

    @FXML
    private void handleSaveEdit() {
        if (editingItem == null) return;
        if (editName.getText().isBlank()) { showErr(errEditName, "Nom obligatoire."); return; }
        try { new BigDecimal(editPrice.getText().trim()); } catch (Exception e) { showErr(errEditPrice, "Prix invalide."); return; }
        editingItem.setProductName(editName.getText().trim());
        editingItem.setProductType(editType.getValue());
        editingItem.setCondition(editCondition.getValue());
        editingItem.setPrice(new BigDecimal(editPrice.getText().trim()));
        String q = editQty.getText().trim(); editingItem.setQuantity(q.isEmpty() ? null : Integer.parseInt(q));
        editingItem.setHasDelivery(editHasDelivery.isSelected());
        editingItem.setSellerPhone(editHasDelivery.isSelected() ? editPhone.getText().trim() : null);
        editingItem.setImagePaths(editImagePaths.isEmpty() ? null : String.join(",", editImagePaths));
        try { service.updateItem(editingItem); closeProductViewer(); loadBrowse(); loadMyListings(); showInfo("Article mis a jour !"); }
        catch (Exception e) { showAlert("Erreur", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SELL
    // ══════════════════════════════════════════════════════════════════════

    @FXML
    private void handlePickImages() {
        FileChooser fc = new FileChooser(); fc.setTitle("Choisir des images");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg"));
        List<File> files = fc.showOpenMultipleDialog(sellName.getScene().getWindow());
        if (files == null || files.isEmpty()) return;
        for (File f : files) if (!selectedImagePaths.contains(f.getAbsolutePath())) selectedImagePaths.add(f.getAbsolutePath());
        refreshSellImagePreviews();
    }

    private void refreshSellImagePreviews() {
        sellImagesPreview.getChildren().clear();
        for (String path : selectedImagePaths) {
            try { File f = new File(path); if (!f.exists()) continue;
                StackPane thumb = new StackPane(); thumb.getStyleClass().add("img-thumb");
                ImageView iv = new ImageView(new Image(f.toURI().toString(), 60, 60, true, true));
                Button rem = new Button("X"); rem.getStyleClass().add("img-thumb-remove");
                StackPane.setAlignment(rem, Pos.TOP_RIGHT);
                rem.setOnAction(e -> { selectedImagePaths.remove(path); refreshSellImagePreviews(); });
                thumb.getChildren().addAll(iv, rem); sellImagesPreview.getChildren().add(thumb);
            } catch (Exception ignored) {}
        }
        sellImagesCount.setText(selectedImagePaths.isEmpty() ? "Aucune image" : selectedImagePaths.size() + " image(s)");
    }

    @FXML
    private void handleSell() {
        if (!validateSellForm()) return;
        if (currentUser == null) { showAlert("Erreur", "Vous devez etre connecte."); return; }
        MarketplaceItem item = new MarketplaceItem();
        item.setSellerId(currentUser.getId()); item.setSellerName(currentUser.getUsername());
        item.setProductName(sellName.getText().trim()); item.setProductType(sellType.getValue());
        item.setCondition(sellCondition.getValue()); item.setPrice(new BigDecimal(sellPrice.getText().trim()));
        String qtyText = sellQty.getText().trim(); item.setQuantity(qtyText.isEmpty() ? null : Integer.parseInt(qtyText));
        item.setHasDelivery(sellHasDelivery.isSelected());
        item.setSellerPhone(sellHasDelivery.isSelected() ? sellPhone.getText().trim() : null);
        item.setImagePaths(selectedImagePaths.isEmpty() ? null : String.join(",", selectedImagePaths));
        try { service.addItem(item); clearSellForm(); loadBrowse(); loadMyListings(); tabPane.getSelectionModel().select(0); showInfo("Article mis en vente !"); }
        catch (Exception e) { showAlert("Erreur", e.getMessage()); }
    }

    private boolean validateSellForm() {
        boolean ok = true; hideErr(errSellName); hideErr(errSellPrice); hideErr(errSellPhone);
        if (sellName.getText().isBlank()) { showErr(errSellName, "Nom obligatoire."); ok = false; }
        try { new BigDecimal(sellPrice.getText().trim()); } catch (Exception e) { showErr(errSellPrice, "Prix invalide."); ok = false; }
        if (sellHasDelivery.isSelected() && sellPhone.getText().isBlank()) { showErr(errSellPhone, "Numero requis."); ok = false; }
        return ok;
    }

    private void clearSellForm() {
        sellName.clear(); sellPrice.clear(); sellQty.clear(); sellPhone.clear();
        sellType.setValue(null); sellCondition.setValue(ItemCondition.NEUF); sellHasDelivery.setSelected(false);
        selectedImagePaths.clear(); refreshSellImagePreviews();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MY LISTINGS + SELLER ORDERS
    // ══════════════════════════════════════════════════════════════════════

    private void loadMyListings() {
        if (currentUser == null) return;
        try { myListingData.setAll(service.getItemsBySeller(currentUser.getId())); renderCards(myListingData, myListingsGrid, true, false); }
        catch (Exception e) { showAlert("Erreur", e.getMessage()); }
    }

    private void loadSellerOrders() {
        if (currentUser == null) return;
        try { sellerOrderData.setAll(service.getOrdersForSeller(currentUser.getId())); sellerOrdersTable.setItems(sellerOrderData); }
        catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MY ORDERS
    // ══════════════════════════════════════════════════════════════════════

    private void loadMyOrders() {
        if (currentUser == null) return;
        try { myOrderData.setAll(service.getOrdersByBuyer(currentUser.getId())); myOrdersTable.setItems(myOrderData); }
        catch (Exception e) { showAlert("Erreur", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  BUY DRAWER
    // ══════════════════════════════════════════════════════════════════════

    private void openBuyDrawer(MarketplaceItem item, int qty) {
        selectedItem = item;
        buyItemName.setText(item.getProductName());
        buyItemCondition.setText("Etat : " + item.getConditionLabel());
        buyItemPrice.setText(item.getPriceDisplay() + " / unite");
        buyItemSeller.setText("Vendeur : " + item.getSellerName());
        double rating = 0; try { rating = service.getSellerRating(item.getSellerId()); } catch (Exception ignored) {}
        buySellerRating.setText(rating > 0 ? String.format("Note : %.1f / 5", rating) : "Nouveau vendeur");
        buyImagesRow.getChildren().clear();
        for (String path : item.getAllImagePaths()) { try { File f = new File(path); if (f.exists()) { ImageView iv = new ImageView(new Image(f.toURI().toString(), 70, 60, true, true)); iv.getStyleClass().add("drawer-img-thumb"); buyImagesRow.getChildren().add(iv); } } catch (Exception ignored) {} }
        int maxQty = item.getQuantity() != null ? item.getQuantity() : Math.max(qty, 1);
        buyQtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQty, Math.min(qty, maxQty)));
        buyQtySpinner.setDisable(item.getQuantity() == null);
        updateBuyTotal(); hideErr(errBuy);
        buyDrawer.setVisible(true); buyDrawer.setManaged(true);
    }

    private void updateBuyTotal() {
        if (selectedItem == null || selectedItem.getPrice() == null) return;
        int qty = buyQtySpinner.getValue() != null ? buyQtySpinner.getValue() : 1;
        buyTotal.setText("Total : " + selectedItem.getPrice().multiply(BigDecimal.valueOf(qty)).toPlainString() + " TND");
    }

    @FXML
    private void handleConfirmBuy() {
        if (currentUser == null) { showErr(errBuy, "Vous devez etre connecte."); return; }
        if (selectedItem == null) return;
        int qty = buyQtySpinner.getValue() != null ? buyQtySpinner.getValue() : 1;
        PaymentMethod method = buyPaymentMethod.getValue();
        BigDecimal total = selectedItem.getPrice().multiply(BigDecimal.valueOf(qty));
        MarketplaceOrder order = new MarketplaceOrder();
        order.setItemId(selectedItem.getId()); order.setBuyerId(currentUser.getId()); order.setBuyerName(currentUser.getUsername());
        order.setQuantityBought(qty); order.setPaymentMethod(method); order.setPaymentStatus(PaymentStatus.PENDING); order.setOrderStatus(OrderStatus.PENDING);
        if (method == PaymentMethod.STRIPE) {
            MarketplaceOrder saved = service.placeOrder(order);
            StripeService.PaymentResult result = stripe.createCheckoutSession(total, "GENEX — " + selectedItem.getProductName(), saved.getId(), qty);
            if (!result.success) { showErr(errBuy, "Stripe: " + result.error); return; }
            service.updatePaymentStatus(saved.getId(), PaymentStatus.PENDING, result.sessionId);
            pendingOrderId  = saved.getId();
            pendingSessionId = result.sessionId;
            closeBuyDrawer();
            openStripeWebView(result.checkoutUrl);
        } else if (method == PaymentMethod.BLOCKCHAIN) {
            MarketplaceOrder saved = service.placeOrder(order);
            pendingOrderId = saved.getId();
            closeBuyDrawer();
            openBlockchainOverlay(total);
        } else {
            service.placeOrder(order); closeBuyDrawer();
            showInfo("Commande enregistree ! Le vendeur vous contactera.");
        }
        if (currentUser != null) service.removeFromCart(currentUser.getId(), selectedItem.getId());
        loadBrowse(); loadMyOrders(); loadCart();
    }

    private void openStripeWebView(String checkoutUrl) {
        stripeWebView.getEngine().load(checkoutUrl);

        // Listen for redirect to success/cancel URL
        stripeWebView.getEngine().locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl == null) return;
            if (newUrl.contains("payment/success") || newUrl.contains("session_id=")) {
                // Extract session_id from URL
                String sid = pendingSessionId;
                if (newUrl.contains("session_id=")) {
                    try { sid = newUrl.split("session_id=")[1].split("&")[0]; } catch (Exception ignored) {}
                }
                final String finalSid = sid;
                closeStripeOverlay();
                // Verify on background thread to avoid blocking UI
                javafx.concurrent.Task<Boolean> verifyTask = new javafx.concurrent.Task<>() {
                    @Override protected Boolean call() { return stripe.verifySession(finalSid); }
                };
                verifyTask.setOnSucceeded(e -> {
                    boolean paid = verifyTask.getValue();
                    if (paid) {
                        service.updatePaymentStatus(pendingOrderId, PaymentStatus.PAID, finalSid);
                        loadMyOrders();
                        showInfo("Paiement confirme ! Merci pour votre achat.");
                    } else {
                        service.updatePaymentStatus(pendingOrderId, PaymentStatus.FAILED, finalSid);
                        showAlert("Paiement", "Le paiement n'a pas pu etre confirme.");
                    }
                });
                new Thread(verifyTask).start();
            } else if (newUrl.contains("payment/cancel")) {
                closeStripeOverlay();
                showAlert("Paiement annule", "Vous avez annule le paiement.");
            }
        });

        stripeOverlay.setVisible(true);
        stripeOverlay.setManaged(true);
    }

    @FXML
    private void closeStripeOverlay() {
        stripeWebView.getEngine().load("about:blank");
        stripeOverlay.setVisible(false);
        stripeOverlay.setManaged(false);
    }

    private void verifyStripePayment(MarketplaceOrder order) {
        if (order.getPaymentRef() == null) return;
        boolean paid = stripe.verifySession(order.getPaymentRef());
        if (paid) { service.updatePaymentStatus(order.getId(), PaymentStatus.PAID, order.getPaymentRef()); loadMyOrders(); showInfo("Paiement confirme !"); }
        else showAlert("Non confirme", "Completez le paiement sur Stripe puis reessayez.");
    }

    // ── Blockchain Logic ──────────────────────────────────────────────────
    private void openBlockchainOverlay(BigDecimal totalTnd) {
        BigDecimal eth = blockchain.convertTndToEth(totalTnd);
        bcAmountLabel.setText(eth.toPlainString() + " ETH");
        bcTotalTndLabel.setText("(≈ " + totalTnd.toPlainString() + " TND)");
        bcTxHash.clear();
        hideErr(errBc);
        blockchainOverlay.setVisible(true);
        blockchainOverlay.setManaged(true);
    }

    @FXML
    private void closeBlockchainOverlay() {
        blockchainOverlay.setVisible(false);
        blockchainOverlay.setManaged(false);
    }

    @FXML
    private void handleCopyWallet() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(bcWalletAddress.getText());
        clipboard.setContent(content);
        // Show subtle feedback if needed, for now just copy
    }

    @FXML
    private void handleVerifyBlockchain() {
        String hash = bcTxHash.getText().trim();
        if (hash.isEmpty()) { showErr(errBc, "Veuillez entrer le hash de la transaction."); return; }
        
        hideErr(errBc);
        // In a real app, we'd use the amount too
        boolean success = blockchain.verifyTransaction(hash, BigDecimal.ZERO); 
        
        if (success) {
            service.updatePaymentStatus(pendingOrderId, PaymentStatus.PAID, hash);
            closeBlockchainOverlay();
            loadMyOrders();
            showInfo("Paiement Blockchain confirme ! Merci pour votre achat.");
        } else {
            showErr(errBc, "Transaction introuvable ou invalide.");
        }
    }

    @FXML private void handleCancelBuy() { closeBuyDrawer(); }
    private void closeBuyDrawer() { buyDrawer.setVisible(false); buyDrawer.setManaged(false); selectedItem = null; }

    // ══════════════════════════════════════════════════════════════════════
    //  REVIEW DRAWER
    // ══════════════════════════════════════════════════════════════════════

    private void openReviewDrawer(MarketplaceOrder order) {
        reviewTargetOrder = order; reviewItemLabel.setText(order.getItemName());
        reviewRating.getValueFactory().setValue(5); reviewComment.clear();
        reviewDrawer.setVisible(true); reviewDrawer.setManaged(true);
    }

    @FXML
    private void handleSubmitReview() {
        if (reviewTargetOrder == null || currentUser == null) return;
        MarketplaceReview rev = new MarketplaceReview();
        rev.setOrderId(reviewTargetOrder.getId()); rev.setItemId(reviewTargetOrder.getItemId());
        rev.setReviewerId(currentUser.getId()); rev.setReviewerName(currentUser.getUsername());
        rev.setSellerId(reviewTargetOrder.getItem() != null ? reviewTargetOrder.getItem().getSellerId() : "");
        rev.setRating(reviewRating.getValue()); rev.setComment(reviewComment.getText().trim());
        try { service.addReview(rev); handleCancelReview(); loadMyOrders(); showInfo("Avis envoye !"); }
        catch (Exception e) { showAlert("Erreur", e.getMessage()); }
    }

    @FXML private void handleCancelReview() { reviewDrawer.setVisible(false); reviewDrawer.setManaged(false); reviewTargetOrder = null; }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private BigDecimal parseBD(String s) { try { return (s==null||s.isBlank()) ? null : new BigDecimal(s.trim()); } catch (Exception e) { return null; } }
    private void showErr(Label l, String msg)  { l.setText(msg); l.setVisible(true); l.setManaged(true); }
    private void hideErr(Label l)              { l.setVisible(false); l.setManaged(false); }
    private void showAlert(String t, String m) { Alert a = new Alert(Alert.AlertType.ERROR);       a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void showInfo(String m)            { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Boutique"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}
