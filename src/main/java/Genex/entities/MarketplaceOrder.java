package Genex.entities;

import java.time.LocalDateTime;

public class MarketplaceOrder {

    public enum PaymentMethod {
        STRIPE("Paiement Stripe (en ligne)"),
        CASH_ON_DELIVERY("Paiement à la livraison");

        private final String label;
        PaymentMethod(String label) { this.label = label; }
        public String getLabel()    { return label; }

        @Override public String toString() { return label; }
    }

    public enum PaymentStatus {
        PENDING, PAID, FAILED
    }

    /** Delivery / fulfilment status — updated by the seller */
    public enum OrderStatus {
        PENDING("En attente"),
        CONFIRMED("Confirmée"),
        SHIPPED("Expédiée"),
        DELIVERED("Livrée");

        private final String label;
        OrderStatus(String label) { this.label = label; }
        public String getLabel()  { return label; }

        @Override public String toString() { return label; }
    }

    private String          id;
    private String          itemId;
    private MarketplaceItem item;
    private String          buyerId;
    private String          buyerName;
    private int             quantityBought;
    private PaymentMethod   paymentMethod;
    private PaymentStatus   paymentStatus;
    private OrderStatus     orderStatus;
    private String          paymentRef;
    private LocalDateTime   orderedAt;

    public MarketplaceOrder() {
        this.orderStatus = OrderStatus.PENDING;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getId()                              { return id; }
    public void   setId(String id)                     { this.id = id; }

    public String getItemId()                          { return itemId; }
    public void   setItemId(String s)                  { this.itemId = s; }

    public MarketplaceItem getItem()                   { return item; }
    public void            setItem(MarketplaceItem i)  { this.item = i; this.itemId = i != null ? i.getId() : null; }

    public String getBuyerId()                         { return buyerId; }
    public void   setBuyerId(String s)                 { this.buyerId = s; }

    public String getBuyerName()                       { return buyerName; }
    public void   setBuyerName(String s)               { this.buyerName = s; }

    public int  getQuantityBought()                    { return quantityBought; }
    public void setQuantityBought(int q)               { this.quantityBought = q; }

    public PaymentMethod getPaymentMethod()                    { return paymentMethod; }
    public void          setPaymentMethod(PaymentMethod m)     { this.paymentMethod = m; }

    public PaymentStatus getPaymentStatus()                    { return paymentStatus; }
    public void          setPaymentStatus(PaymentStatus s)     { this.paymentStatus = s; }

    public OrderStatus getOrderStatus()                        { return orderStatus; }
    public void        setOrderStatus(OrderStatus s)           { this.orderStatus = s != null ? s : OrderStatus.PENDING; }

    public String getPaymentRef()                      { return paymentRef; }
    public void   setPaymentRef(String s)              { this.paymentRef = s; }

    public LocalDateTime getOrderedAt()                { return orderedAt; }
    public void          setOrderedAt(LocalDateTime d) { this.orderedAt = d; }

    // ── Display helpers ────────────────────────────────────────────────────

    public String getItemName()        { return item != null ? item.getProductName() : itemId; }
    public String getMethodLabel()     { return paymentMethod != null ? paymentMethod.getLabel() : ""; }
    public String getOrderStatusLabel(){ return orderStatus != null ? orderStatus.getLabel() : "En attente"; }

    public String getPaymentStatusLabel() {
        if (paymentStatus == null) return "En attente";
        return switch (paymentStatus) {
            case PAID    -> "Paye";
            case FAILED  -> "Echoue";
            default      -> "En attente";
        };
    }
}
