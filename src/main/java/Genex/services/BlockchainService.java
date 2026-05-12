package Genex.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Service to simulate blockchain payment verification.
 * In a real app, this would use Web3j to check the transaction status on-chain.
 */
public class BlockchainService {

    // Mock exchange rate: 1 ETH = 8500 TND (approx)
    private static final BigDecimal ETH_PRICE_TND = new BigDecimal("8500");
    private static final String DESTINATION_WALLET = "0x71C7656EC7ab88b098defB751B7401B5f6d8976F";

    public String getDestinationWallet() {
        return DESTINATION_WALLET;
    }

    /** Converts TND amount to ETH */
    public BigDecimal convertTndToEth(BigDecimal tndAmount) {
        if (tndAmount == null) return BigDecimal.ZERO;
        return tndAmount.divide(ETH_PRICE_TND, 6, RoundingMode.HALF_UP);
    }

    /** 
     * Simulates verifying a transaction hash.
     * In a real scenario, this would call an API (Etherscan, Infura) or use a local node.
     */
    public boolean verifyTransaction(String txHash, BigDecimal expectedEth) {
        if (txHash == null || txHash.isBlank()) return false;
        
        // Basic validation of ETH address format (mock)
        if (!txHash.startsWith("0x") || txHash.length() < 30) return false;

        // Simulate network latency
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // Mock success (always true if hash looks valid for demo purposes)
        return true; 
    }

    public static class BlockchainResult {
        public final boolean success;
        public final String error;
        public final String txHash;

        public BlockchainResult(boolean success, String error, String txHash) {
            this.success = success;
            this.error = error;
            this.txHash = txHash;
        }
    }
}
