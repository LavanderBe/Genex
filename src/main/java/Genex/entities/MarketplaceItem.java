package Genex.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketplaceItem {

    public enum ProductType {
        PCS("PCs / Matériel"),
        ACCESSORIES("Accessoires"),
        GIFT_CARDS("Cartes Cadeaux"),
        PLUSHIES("Peluches"),
        FIGURINES("Figurines"),
        CLOTHES("Vêtements");

        private final String label;
        ProductType(String label) { this.label = label; }
        public String getLabel()  { return label; }

        @Override public String toString() { return label; }
    }

    public enum ItemStatus {
        AVAILABLE, SOLD_OUT
    }

    public enum ItemCondition {
        NEUF("Neuf"),
        TRES_BON_ETAT("Très bon état"),
        BON_ETAT("Bon état"),
        USE("Usagé");

        private final String label;
        ItemCondition(String label) { this.label = label; }
        public String getLabel()    { return label; }

        @Override public String toString() { return label; }
    }

    private String        id;           // UUID generated in service
    private String        sellerId;
    private String        sellerName;
    private String        sellerPhone;
    private String        productName;
    private ProductType   productType;
    private BigDecimal    price;
    private Integer       quantity;     // null = single item no stock tracking
    private boolean       hasDelivery;
    private ItemStatus    status;
    private LocalDateTime listedAt;
    private String        imagePaths;   // comma-separated absolute file paths
    private ItemCondition condition;    // NEUF | TRES_BON_ETAT | BON_ETAT | USE

    public MarketplaceItem() {}

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getId()                          { return id; }
    public void   setId(String id)                 { this.id = id; }

    public String getSellerId()                    { return sellerId; }
    public void   setSellerId(String s)            { this.sellerId = s; }

    public String getSellerName()                  { return sellerName; }
    public void   setSellerName(String s)          { this.sellerName = s; }

    public String getSellerPhone()                 { return sellerPhone; }
    public void   setSellerPhone(String s)         { this.sellerPhone = s; }

    public String getProductName()                 { return productName; }
    public void   setProductName(String s)         { this.productName = s; }

    public ProductType getProductType()                    { return productType; }
    public void        setProductType(ProductType t)       { this.productType = t; }

    public BigDecimal getPrice()                   { return price; }
    public void       setPrice(BigDecimal p)       { this.price = p; }

    public Integer getQuantity()                   { return quantity; }
    public void    setQuantity(Integer q)          { this.quantity = q; }

    public boolean isHasDelivery()                 { return hasDelivery; }
    public void    setHasDelivery(boolean b)       { this.hasDelivery = b; }

    public ItemStatus getStatus()                  { return status; }
    public void       setStatus(ItemStatus s)      { this.status = s; }

    public LocalDateTime getListedAt()             { return listedAt; }
    public void          setListedAt(LocalDateTime d) { this.listedAt = d; }

    // ── Display helpers ────────────────────────────────────────────────────

    public String getTypeLabel() {
        return productType != null ? productType.getLabel() : "";
    }

    public String getStatusLabel() {
        return status == ItemStatus.SOLD_OUT ? "ÉPUISÉ" : "DISPONIBLE";
    }

    public String getQuantityDisplay() {
        return quantity == null ? "—" : String.valueOf(quantity);
    }

    public String getPriceDisplay() {
        return price != null ? price.toPlainString() + " TND" : "—";
    }

    public String getImagePaths()              { return imagePaths; }
    public void   setImagePaths(String paths)  { this.imagePaths = paths; }

    public ItemCondition getCondition()                    { return condition; }
    public void          setCondition(ItemCondition c)     { this.condition = c; }

    public String getConditionLabel() {
        return condition != null ? condition.getLabel() : "Neuf";
    }

    /** Returns the first image path, or null if none. */
    public String getFirstImagePath() {
        if (imagePaths == null || imagePaths.isBlank()) return null;
        return imagePaths.split(",")[0].trim();
    }

    /** Returns all image paths as a list. */
    public java.util.List<String> getAllImagePaths() {
        if (imagePaths == null || imagePaths.isBlank()) return java.util.List.of();
        return java.util.Arrays.stream(imagePaths.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .toList();
    }
}
