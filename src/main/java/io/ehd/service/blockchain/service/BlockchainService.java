package io.ehd.service.blockchain.service;

import io.ehd.service.blockchain.model.BlockchainTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class BlockchainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockchainService.class);

    private final Web3j web3j;
    private final Admin admin;

    public BlockchainService(Web3j web3j, Admin admin) {
        this.web3j = web3j;
        this.admin = admin;
    }

    public BlockchainTransaction transaction(BlockchainTransaction trx, String password) throws IOException, InterruptedException, ExecutionException, TimeoutException {

        EthGetTransactionCount transactionCount = web3j.ethGetTransactionCount(trx.getFromAddr(), DefaultBlockParameterName.LATEST).send();
        admin.personalUnlockAccount(trx.getFromAddr(), password).sendAsync().get(5, TimeUnit.MINUTES);

        Transaction transaction = Transaction.createEtherTransaction(
                trx.getFromAddr(), transactionCount.getTransactionCount(), BigInteger.valueOf(trx.getValue()),
                BigInteger.valueOf(21_000), trx.getToAddr(), Convert.toWei(String.valueOf(trx.getValue()), Convert.Unit.ETHER).toBigInteger());

        EthSendTransaction response = web3j.ethSendTransaction(transaction).send();

        if (response.getError() != null) {
            trx.setAccepted(false);
            LOGGER.info("Tx rejected: {}", response.getError().getMessage());
            return trx;
        }

        trx.setAccepted(true);
        String txHash = response.getTransactionHash();
        LOGGER.info("Tx hash: {}", txHash);

        trx.setId(txHash);
        EthGetTransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash).send();

        receipt.getTransactionReceipt().ifPresent(transactionReceipt -> LOGGER.info("Tx receipt:  {}", transactionReceipt.getCumulativeGasUsed().intValue()));

        return trx;

    }

    public BigDecimal getBalance(String address) throws ExecutionException, InterruptedException {
        EthGetBalance ethGetBalance = web3j
                .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get();
        return Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER);
    }

    public List<String> getAccountList() throws IOException {
        return admin.personalListAccounts().send().getAccountIds();
    }

    public String createAccount(String password) throws IOException {
        NewAccountIdentifier newAccountIdentifier = admin.personalNewAccount(password).send();
        if (newAccountIdentifier != null) {
            String accountId = newAccountIdentifier.getAccountId();
            return accountId;
        }
        return null;
    }

    public Object getTransactionByHash(String hash) throws IOException {
        EthTransaction ethTransaction = admin.ethGetTransactionByHash(hash).send();
        return ethTransaction.getResult();
    }

    public Object getBlockByNumber(BigInteger number) throws IOException {
        EthBlock ethBlock = admin.ethGetBlockByNumber(DefaultBlockParameter.valueOf(number), true).send();
        return ethBlock.getResult();
    }

    public Object getBlockByHash(String hash) throws IOException {
        EthBlock ethBlock = admin.ethGetBlockByHash(hash, true).send();
        return ethBlock.getResult();
    }

    public Object blockNumber() throws IOException {
        EthBlockNumber blockNumber = admin.ethBlockNumber().send();
        return blockNumber.getResult();
    }

    public Object ethGasPrice() throws IOException {
        EthGasPrice gasPrice = admin.ethGasPrice().send();
        return gasPrice.getResult();
    }

    public Object ethEstimateGas(BlockchainTransaction trx) throws IOException {
        Transaction transaction = Transaction.createEthCallTransaction(trx.getFromAddr(),trx.getToAddr(),trx.getId());
        EthEstimateGas estimateGas = admin.ethEstimateGas(transaction).send();
        return estimateGas.getResult();
    }
}
