package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class RequestDto {
    private int transferType;
    private String username;
    private BigDecimal amount;
    private int transferId;
//    private int accountFrom;
    public RequestDto(){};

    public RequestDto(String username, BigDecimal amount, int transferId) {
//        this.transferType = transferType;
        this.username = username;
        this.amount = amount;
        this.transferId = transferId;
//        this.accountFrom = accountFrom;
//        this.accountTo = accountTo;
//        this.transferStatus = transferStatus;
    }

//    public int getTransferType() {
//        return transferType;
//    }
//
//    public void setTransferType(int transferType) {
//        this.transferType = transferType;
//    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }

//    public int getAccountFrom() {
//        return accountFrom;
//    }
//
//    public void setAccountFrom(int accountFrom) {
//        this.accountFrom = accountFrom;
//    }

//    public int getAccountTo() {
//        return accountTo;
//    }
//
//    public void setAccountTo(int accountTo) {
//        this.accountTo = accountTo;
//    }
//
//    public int getTransferStatus() {
//        return transferStatus;
//    }
//
//    public void setTransferStatus(int transferStatus) {
//        this.transferStatus = transferStatus;
//    }
//
//    private int accountTo;
//    private int transferStatus;
}
