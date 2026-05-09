package Genex.entities;

import java.time.LocalDateTime;

public class MarketplaceReview {

    private String        id;
    private String        orderId;
    private String        itemId;
    private String        reviewerId;
    private String        reviewerName;
    private String        sellerId;
    private int           rating;       // 1–5
    private String        comment;
    private LocalDateTime createdAt;

    public MarketplaceReview() {}

    public String getId()                          { return id; }
    public void   setId(String id)                 { this.id = id; }

    public String getOrderId()                     { return orderId; }
    public void   setOrderId(String s)             { this.orderId = s; }

    public String getItemId()                      { return itemId; }
    public void   setItemId(String s)              { this.itemId = s; }

    public String getReviewerId()                  { return reviewerId; }
    public void   setReviewerId(String s)          { this.reviewerId = s; }

    public String getReviewerName()                { return reviewerName; }
    public void   setReviewerName(String s)        { this.reviewerName = s; }

    public String getSellerId()                    { return sellerId; }
    public void   setSellerId(String s)            { this.sellerId = s; }

    public int  getRating()                        { return rating; }
    public void setRating(int r)                   { this.rating = Math.max(1, Math.min(5, r)); }

    public String getComment()                     { return comment; }
    public void   setComment(String s)             { this.comment = s; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void          setCreatedAt(LocalDateTime d) { this.createdAt = d; }

    /** Returns star string e.g. "★★★★☆" */
    public String getStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) sb.append(i <= rating ? "★" : "☆");
        return sb.toString();
    }
}
