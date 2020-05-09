package io.ehd.service.blockchain.model;

import lombok.Data;

@Data
public class BlockchainTransaction {

    private String id;
    private String fromAddr;
    private String toAddr;
    private long value;
    private boolean accepted;

    public BlockchainTransaction() {

    }

    public BlockchainTransaction(String fromAddr, String toAddr, long value) {
        this.fromAddr = fromAddr;
        this.toAddr = toAddr;
        this.value = value;
    }
}
