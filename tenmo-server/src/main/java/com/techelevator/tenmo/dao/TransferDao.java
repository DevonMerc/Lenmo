package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.RequestDto;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {
Transfer create(int transferTypeId, int transferStatusId, int accountFrom, int accountTo, BigDecimal amount);
Transfer getTransferById(Integer id);
List<TransferDto> findTransfers(int accountId);
List<RequestDto> viewRequests(int accountId);

Transfer updateTransfer(int transferStatus, int transferId);
}
