package Genex.services;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;

public class StripeService {

    // ── Replace with your sk_test_... key ──────────────────────────────────
    private static final String SECRET_KEY =
            "";
    // ──────────────────────────────────────────────────────────────────────

    /** Stripe redirects here after a successful payment. */
    public static final String SUCCESS_URL =
            "https://genex-app.local/payment/success?session_id={CHECKOUT_SESSION_ID}";
    /** Stripe redirects here if the user cancels. */
    public static final String CANCEL_URL  =
            "https://genex-app.local/payment/cancel";

    // ── Result wrapper ─────────────────────────────────────────────────────

    public static class PaymentResult {
        public final boolean success;
        public final String  sessionId;    // store as payment_ref in DB
        public final String  checkoutUrl;  // open with Desktop.browse()
        public final String  error;

        public PaymentResult(boolean success, String sessionId,
                             String checkoutUrl, String error) {
            this.success     = success;
            this.sessionId   = sessionId;
            this.checkoutUrl = checkoutUrl;
            this.error       = error;
        }
    }

    // ── Create Checkout Session ────────────────────────────────────────────

    /**
     * @param amountTND   Amount to charge (e.g. 29.99) — treated as USD cents for Stripe test mode
     * @param productName Label shown on the Stripe checkout page
     * @param orderId     Your internal order UUID
     * @param quantity    Number of units
     */
    public PaymentResult createCheckoutSession(BigDecimal amountTND,
                                                String productName,
                                                String orderId,
                                                int quantity) {
        try {
            StripeClient client = new StripeClient(SECRET_KEY);

            // Stripe amounts are in the smallest currency unit.
            // Using USD cents (amount × 100) — neutral currency for test mode.
            long amountCents = amountTND.multiply(BigDecimal.valueOf(100)).longValue();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(SUCCESS_URL)
                    .setCancelUrl(CANCEL_URL)
                    .setClientReferenceId(orderId)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity((long) quantity)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(amountCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(productName)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = client.checkout().sessions().create(params);
            return new PaymentResult(true, session.getId(), session.getUrl(), null);

        } catch (StripeException e) {
            System.err.println("StripeService error: " + e.getMessage());
            return new PaymentResult(false, null, null,
                    "Stripe (" + e.getCode() + "): " + e.getMessage());
        } catch (Exception e) {
            return new PaymentResult(false, null, null, e.getMessage());
        }
    }

    // ── Verify Session ─────────────────────────────────────────────────────

    /**
     * Returns true if the Checkout Session payment_status is "paid".
     * Call this after the user returns from the Stripe page.
     */
    public boolean verifySession(String sessionId) {
        try {
            StripeClient client = new StripeClient(SECRET_KEY);
            Session session = client.checkout().sessions().retrieve(sessionId);
            return "paid".equals(session.getPaymentStatus());
        } catch (StripeException e) {
            System.err.println("StripeService.verifySession error: " + e.getMessage());
            return false;
        }
    }
}
