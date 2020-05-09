package io.ehd.service.blockchain.controller;

import io.ehd.service.blockchain.model.BlockchainTransaction;
import org.springframework.web.bind.annotation.*;
import io.ehd.service.blockchain.service.BlockchainService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
public class BlockchainController {

    private final BlockchainService service;

    public BlockchainController(BlockchainService service) {
        this.service = service;
    }

    @PostMapping("/transaction")
    public BlockchainTransaction transaction(@RequestBody BlockchainTransaction transaction, @RequestParam String password) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        return service.transaction(transaction, password);
    }

    @GetMapping("/getBalance")
    public BigDecimal getBalance(@RequestParam String address) throws ExecutionException, InterruptedException {
        return service.getBalance(address);
    }

    @PostMapping("/newAccount")
    public String newAccount(@RequestParam String password) throws IOException {
        return service.createAccount(password);
    }

    @GetMapping("/accountList")
    public List<String> accountList() throws IOException {
        return service.getAccountList();
    }

    @GetMapping("/getTransactionByHash")
    public Object getTransactionByHash(@RequestParam String hash) throws IOException {
         return service.getTransactionByHash(hash);
    }

    @GetMapping("/getBlockByNumber")
    public Object getBlockByNumber(@RequestParam BigInteger number) throws IOException {
        return service.getBlockByNumber(number);
    }

    @GetMapping("/getBlockByHash")
    public Object getBlockByHash(@RequestParam String hash) throws IOException {
        return service.getBlockByHash(hash);
    }

    @GetMapping("/blockNumber")
    public Object blockNumber() throws IOException {
        return service.blockNumber();
    }

    @GetMapping("/ethGasPrice")
    public Object ethGasPrice() throws IOException {
        return service.ethGasPrice();
    }

    @PostMapping("/ethEstimateGas")
    public Object ethEstimateGas(@RequestBody BlockchainTransaction transaction) throws IOException {
        return service.ethEstimateGas(transaction);
    }
}
