package Genex.services;

import Genex.entities.MarketplaceItem;
import Genex.entities.MarketplaceItem.ItemCondition;
import Genex.entities.MarketplaceItem.ItemStatus;
import Genex.entities.MarketplaceItem.ProductType;
import Genex.entities.MarketplaceOrder;
import Genex.entities.MarketplaceOrder.OrderStatus;
import Genex.entities.MarketplaceOrder.PaymentMethod;
import Genex.entities.MarketplaceOrder.PaymentStatus;
import Genex.entities.MarketplaceReview;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrudMarketplace {

    private Connection getCnx() {
        return Myconnection.getInstance().getCnx();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ITEMS
    // ══════════════════════════════════════════════════════════════════════

    public List<MarketplaceItem> getAvailableItems() {
        return getAvailableItems(null, null, null);
    }

    /** Browse with optional price range and condition filters. */
    public List<MarketplaceItem> getAvailableItems(BigDecimalRange priceRange,
                                                    String condition,
                                                    String type) {
        StringBuilder where = new StringBuilder("WHERE status = 'AVAILABLE'");
        if (priceRange != null) {
            where.append(" AND price >= ").append(priceRange.min)
                 .append(" AND price <= ").append(priceRange.max);
        }
        if (condition != null && !condition.isBlank()) {
            where.append(" AND item_condition = '").append(condition).append("'");
        }
        if (type != null && !type.isBlank()) {
            where.append(" AND product_type = '").append(type).append("'");
        }
        where.append(" ORDER BY listed_at DESC");
        return getItems(where.toString());
    }

    public List<MarketplaceItem> getItemsBySeller(String sellerId) {
        return getItems("WHERE seller_id = '" + sellerId + "' ORDER BY listed_at DESC");
    }

    private List<MarketplaceItem> getItems(String whereClause) {
        List<MarketplaceItem> list = new ArrayList<>();
        String sql = "SELECT * FROM marketplace_items " + whereClause;
        try (Statement st = getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapItem(rs));
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.getItems: " + e.getMessage(), e);
        }
        return list;
    }

    public void addItem(MarketplaceItem item) {
        item.setId(UUID.randomUUID().toString());
        String sql = "INSERT INTO marketplace_items " +
                "(id, seller_id, seller_name, seller_phone, product_name, product_type, " +
                " price, quantity, has_delivery, status, image_paths, item_condition, listed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'AVAILABLE', ?, ?, NOW())";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, item.getId());
            pst.setString(2, item.getSellerId());
            pst.setString(3, item.getSellerName());
            pst.setString(4, nullIfBlank(item.getSellerPhone()));
            pst.setString(5, item.getProductName());
            pst.setString(6, item.getProductType() != null ? item.getProductType().name() : null);
            pst.setBigDecimal(7, item.getPrice());
            if (item.getQuantity() != null) pst.setInt(8, item.getQuantity());
            else pst.setNull(8, Types.INTEGER);
            pst.setBoolean(9, item.isHasDelivery());
            pst.setString(10, nullIfBlank(item.getImagePaths()));
            pst.setString(11, item.getCondition() != null ? item.getCondition().name() : ItemCondition.NEUF.name());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.addItem: " + e.getMessage(), e);
        }
    }

    public void deleteItem(String itemId) {
        try (PreparedStatement pst = getCnx().prepareStatement(
                "DELETE FROM marketplace_items WHERE id=?")) {
            pst.setString(1, itemId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.deleteItem: " + e.getMessage(), e);
        }
    }

    public void updateItem(MarketplaceItem item) {
        String sql = "UPDATE marketplace_items SET product_name=?, product_type=?, price=?, " +
                     "quantity=?, has_delivery=?, seller_phone=?, image_paths=?, item_condition=? WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, item.getProductName());
            pst.setString(2, item.getProductType() != null ? item.getProductType().name() : null);
            pst.setBigDecimal(3, item.getPrice());
            if (item.getQuantity() != null) pst.setInt(4, item.getQuantity());
            else pst.setNull(4, Types.INTEGER);
            pst.setBoolean(5, item.isHasDelivery());
            pst.setString(6, nullIfBlank(item.getSellerPhone()));
            pst.setString(7, nullIfBlank(item.getImagePaths()));
            pst.setString(8, item.getCondition() != null ? item.getCondition().name() : ItemCondition.NEUF.name());
            pst.setString(9, item.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.updateItem: " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ORDERS
    // ══════════════════════════════════════════════════════════════════════

    public MarketplaceOrder placeOrder(MarketplaceOrder order) {
        order.setId(UUID.randomUUID().toString());
        String sql = "INSERT INTO marketplace_orders " +
                "(id, item_id, buyer_id, buyer_name, quantity_bought, " +
                " payment_method, payment_status, order_status, payment_ref, ordered_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, order.getId());
            pst.setString(2, order.getItemId());
            pst.setString(3, order.getBuyerId());
            pst.setString(4, order.getBuyerName());
            pst.setInt   (5, order.getQuantityBought());
            pst.setString(6, order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
            pst.setString(7, PaymentStatus.PENDING.name());
            pst.setString(8, OrderStatus.PENDING.name());
            pst.setString(9, nullIfBlank(order.getPaymentRef()));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.placeOrder: " + e.getMessage(), e);
        }
        decrementStock(order.getItemId(), order.getQuantityBought());
        return order;
    }

    /** Seller updates the fulfilment status of an order. */
    public void updateOrderStatus(String orderId, OrderStatus status) {
        String sql = "UPDATE marketplace_orders SET order_status=? WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, status.name());
            pst.setString(2, orderId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.updateOrderStatus: " + e.getMessage(), e);
        }
    }

    public void updatePaymentStatus(String orderId, PaymentStatus status, String paymentRef) {
        String sql = "UPDATE marketplace_orders SET payment_status=?, payment_ref=? WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, status.name());
            pst.setString(2, paymentRef);
            pst.setString(3, orderId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.updatePaymentStatus: " + e.getMessage(), e);
        }
    }

    public List<MarketplaceOrder> getOrdersByBuyer(String buyerId) {
        return getOrders("WHERE o.buyer_id = '" + buyerId + "' ORDER BY o.ordered_at DESC");
    }

    public List<MarketplaceOrder> getOrdersForSeller(String sellerId) {
        return getOrders("WHERE i.seller_id = '" + sellerId + "' ORDER BY o.ordered_at DESC");
    }

    private List<MarketplaceOrder> getOrders(String whereClause) {
        List<MarketplaceOrder> list = new ArrayList<>();
        String sql = "SELECT o.*, i.product_name, i.product_type, i.price, i.seller_name " +
                "FROM marketplace_orders o " +
                "LEFT JOIN marketplace_items i ON o.item_id = i.id " +
                whereClause;
        try (Statement st = getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrder(rs));
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.getOrders: " + e.getMessage(), e);
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REVIEWS
    // ══════════════════════════════════════════════════════════════════════

    public void addReview(MarketplaceReview r) {
        r.setId(UUID.randomUUID().toString());
        String sql = "INSERT INTO marketplace_reviews " +
                "(id, order_id, item_id, reviewer_id, seller_id, rating, comment, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, r.getId());
            pst.setString(2, r.getOrderId());
            pst.setString(3, r.getItemId());
            pst.setString(4, r.getReviewerId());
            pst.setString(5, r.getSellerId());
            pst.setInt   (6, r.getRating());
            pst.setString(7, nullIfBlank(r.getComment()));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.addReview: " + e.getMessage(), e);
        }
    }

    public List<MarketplaceReview> getReviewsForSeller(String sellerId) {
        List<MarketplaceReview> list = new ArrayList<>();
        String sql = "SELECT r.*, u.username AS reviewer_name " +
                "FROM marketplace_reviews r " +
                "LEFT JOIN users u ON r.reviewer_id = u.id " +
                "WHERE r.seller_id = ? ORDER BY r.created_at DESC";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, sellerId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                MarketplaceReview rev = new MarketplaceReview();
                rev.setId(rs.getString("id"));
                rev.setOrderId(rs.getString("order_id"));
                rev.setItemId(rs.getString("item_id"));
                rev.setReviewerId(rs.getString("reviewer_id"));
                rev.setReviewerName(rs.getString("reviewer_name"));
                rev.setSellerId(rs.getString("seller_id"));
                rev.setRating(rs.getInt("rating"));
                rev.setComment(rs.getString("comment"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) rev.setCreatedAt(ts.toLocalDateTime());
                list.add(rev);
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.getReviewsForSeller: " + e.getMessage(), e);
        }
        return list;
    }

    /** Average rating for a seller (0.0 if no reviews). */
    public double getSellerRating(String sellerId) {
        String sql = "SELECT AVG(rating) FROM marketplace_reviews WHERE seller_id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, sellerId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : avg;
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.getSellerRating: " + e.getMessage(), e);
        }
        return 0.0;
    }

    /** True if the buyer has already reviewed this order. */
    public boolean hasReviewed(String orderId) {
        String sql = "SELECT COUNT(*) FROM marketplace_reviews WHERE order_id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, orderId);
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  WISHLIST
    // ══════════════════════════════════════════════════════════════════════

    public void addToWishlist(String userId, String itemId) {
        String sql = "INSERT IGNORE INTO marketplace_wishlist (id, user_id, item_id, added_at) VALUES (UUID(), ?, ?, NOW())";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setString(2, itemId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.addToWishlist: " + e.getMessage(), e);
        }
    }

    public void removeFromWishlist(String userId, String itemId) {
        String sql = "DELETE FROM marketplace_wishlist WHERE user_id=? AND item_id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setString(2, itemId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.removeFromWishlist: " + e.getMessage(), e);
        }
    }

    public boolean isInWishlist(String userId, String itemId) {
        String sql = "SELECT COUNT(*) FROM marketplace_wishlist WHERE user_id=? AND item_id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setString(2, itemId);
            ResultSet rs = pst.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<MarketplaceItem> getWishlist(String userId) {
        List<MarketplaceItem> list = new ArrayList<>();
        String sql = "SELECT i.* FROM marketplace_items i " +
                "JOIN marketplace_wishlist w ON i.id = w.item_id " +
                "WHERE w.user_id = ? ORDER BY w.added_at DESC";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(mapItem(rs));
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.getWishlist: " + e.getMessage(), e);
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  CART
    // ══════════════════════════════════════════════════════════════════════

    public void addToCart(String userId, String itemId, int quantity) {
        // Check if already in cart
        String check = "SELECT id, quantity FROM marketplace_cart WHERE user_id=? AND item_id=?";
        try (PreparedStatement sel = getCnx().prepareStatement(check)) {
            sel.setString(1, userId);
            sel.setString(2, itemId);
            ResultSet rs = sel.executeQuery();
            if (rs.next()) {
                // Already exists — update quantity
                String update = "UPDATE marketplace_cart SET quantity=? WHERE id=?";
                try (PreparedStatement upd = getCnx().prepareStatement(update)) {
                    upd.setInt(1, rs.getInt("quantity") + quantity);
                    upd.setString(2, rs.getString("id"));
                    upd.executeUpdate();
                }
            } else {
                // New entry
                String insert = "INSERT INTO marketplace_cart (id, user_id, item_id, quantity, added_at) VALUES (UUID(), ?, ?, ?, NOW())";
                try (PreparedStatement ins = getCnx().prepareStatement(insert)) {
                    ins.setString(1, userId);
                    ins.setString(2, itemId);
                    ins.setInt(3, quantity);
                    ins.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.addToCart: " + e.getMessage(), e);
        }
    }

    public void removeFromCart(String userId, String itemId) {
        String sql = "DELETE FROM marketplace_cart WHERE user_id=? AND item_id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setString(2, itemId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.removeFromCart: " + e.getMessage(), e);
        }
    }

    public void clearCart(String userId) {
        String sql = "DELETE FROM marketplace_cart WHERE user_id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.clearCart: " + e.getMessage(), e);
        }
    }

    /** Returns items in the cart with their quantities. */
    public List<CartEntry> getCart(String userId) {
        List<CartEntry> list = new ArrayList<>();
        String sql = "SELECT c.quantity AS cart_qty, i.* FROM marketplace_cart c " +
                     "JOIN marketplace_items i ON c.item_id = i.id " +
                     "WHERE c.user_id = ? ORDER BY c.added_at DESC";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                MarketplaceItem item = mapItem(rs);
                int qty = rs.getInt("cart_qty");
                list.add(new CartEntry(item, qty));
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.getCart: " + e.getMessage(), e);
        }
        return list;
    }

    public static class CartEntry {
        public final MarketplaceItem item;
        public final int quantity;
        public CartEntry(MarketplaceItem item, int quantity) {
            this.item = item; this.quantity = quantity;
        }
        public java.math.BigDecimal getLineTotal() {
            return item.getPrice() != null
                    ? item.getPrice().multiply(java.math.BigDecimal.valueOf(quantity))
                    : java.math.BigDecimal.ZERO;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FINANCE OVERVIEW STATS
    // ══════════════════════════════════════════════════════════════════════

    public int getTotalListings()   { return countQuery("SELECT COUNT(*) FROM marketplace_items"); }
    public int getActiveListings()  { return countQuery("SELECT COUNT(*) FROM marketplace_items WHERE status='AVAILABLE'"); }
    public int getTotalOrders()     { return countQuery("SELECT COUNT(*) FROM marketplace_orders"); }

    private int countQuery(String sql) {
        try (Statement st = getCnx().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private void decrementStock(String itemId, int qty) {
        try {
            PreparedStatement sel = getCnx().prepareStatement(
                    "SELECT quantity FROM marketplace_items WHERE id=?");
            sel.setString(1, itemId);
            ResultSet rs = sel.executeQuery();
            if (rs.next()) {
                int current = rs.getInt("quantity");
                boolean wasNull = rs.wasNull();
                if (!wasNull) {
                    int newQty = Math.max(0, current - qty);
                    PreparedStatement upd = getCnx().prepareStatement(
                            "UPDATE marketplace_items SET quantity=?, status=? WHERE id=?");
                    upd.setInt(1, newQty);
                    upd.setString(2, newQty == 0 ? "SOLD_OUT" : "AVAILABLE");
                    upd.setString(3, itemId);
                    upd.executeUpdate();
                } else {
                    PreparedStatement upd = getCnx().prepareStatement(
                            "UPDATE marketplace_items SET status='SOLD_OUT' WHERE id=?");
                    upd.setString(1, itemId);
                    upd.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudMarketplace.decrementStock: " + e.getMessage(), e);
        }
    }

    private MarketplaceItem mapItem(ResultSet rs) throws SQLException {
        MarketplaceItem item = new MarketplaceItem();
        item.setId(rs.getString("id"));
        item.setSellerId(rs.getString("seller_id"));
        item.setSellerName(rs.getString("seller_name"));
        item.setSellerPhone(rs.getString("seller_phone"));
        item.setProductName(rs.getString("product_name"));
        try { item.setProductType(ProductType.valueOf(rs.getString("product_type"))); } catch (Exception ignored) {}
        item.setPrice(rs.getBigDecimal("price"));
        int qty = rs.getInt("quantity");
        item.setQuantity(rs.wasNull() ? null : qty);
        item.setHasDelivery(rs.getBoolean("has_delivery"));
        try { item.setStatus(ItemStatus.valueOf(rs.getString("status"))); } catch (Exception ignored) { item.setStatus(ItemStatus.AVAILABLE); }
        item.setImagePaths(rs.getString("image_paths"));
        try { item.setCondition(ItemCondition.valueOf(rs.getString("item_condition"))); } catch (Exception ignored) { item.setCondition(ItemCondition.NEUF); }
        Timestamp ts = rs.getTimestamp("listed_at");
        if (ts != null) item.setListedAt(ts.toLocalDateTime());
        return item;
    }

    private MarketplaceOrder mapOrder(ResultSet rs) throws SQLException {
        MarketplaceOrder order = new MarketplaceOrder();
        order.setId(rs.getString("id"));
        order.setItemId(rs.getString("item_id"));
        order.setBuyerId(rs.getString("buyer_id"));
        order.setBuyerName(rs.getString("buyer_name"));
        order.setQuantityBought(rs.getInt("quantity_bought"));
        try { order.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method"))); } catch (Exception ignored) {}
        try { order.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status"))); } catch (Exception ignored) { order.setPaymentStatus(PaymentStatus.PENDING); }
        try { order.setOrderStatus(OrderStatus.valueOf(rs.getString("order_status"))); } catch (Exception ignored) { order.setOrderStatus(OrderStatus.PENDING); }
        order.setPaymentRef(rs.getString("payment_ref"));
        Timestamp ts = rs.getTimestamp("ordered_at");
        if (ts != null) order.setOrderedAt(ts.toLocalDateTime());
        MarketplaceItem item = new MarketplaceItem();
        item.setId(rs.getString("item_id"));
        try { item.setProductName(rs.getString("product_name")); } catch (Exception ignored) {}
        try { item.setSellerName(rs.getString("seller_name")); } catch (Exception ignored) {}
        try { item.setPrice(rs.getBigDecimal("price")); } catch (Exception ignored) {}
        try { item.setProductType(ProductType.valueOf(rs.getString("product_type"))); } catch (Exception ignored) {}
        order.setItem(item);
        return order;
    }

    private String nullIfBlank(String s) { return (s == null || s.isBlank()) ? null : s; }

    /** Simple price range helper. */
    public static class BigDecimalRange {
        public final java.math.BigDecimal min, max;
        public BigDecimalRange(java.math.BigDecimal min, java.math.BigDecimal max) {
            this.min = min; this.max = max;
        }
    }
}
