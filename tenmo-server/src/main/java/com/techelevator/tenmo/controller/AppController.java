package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class AppController {
    @Autowired
    private final AccountDao accountDao;
    @Autowired
    private final UserDao userDao;
    @Autowired
    private final TransferDao transferDao;


    public AppController(TransferDao transferDao, UserDao userDao, AccountDao accountDao) {
        this.transferDao = transferDao;
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    @RequestMapping(method = RequestMethod.GET)
    public BigDecimal getBalance(Principal principal) {
        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);
        return accountDao.getBalanceByUserId(userId);
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public List<User> findALl(Principal principal) {
        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);
        return userDao.findAll();

    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public Transfer add(@RequestBody Transfer transfer) {
        return transferDao.create(transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
//        FIX THIS
//        return new Transfer(transfer.getTransferTypeId(), transfer.getTransferStatusId(),transfer.getAccountFrom(),transfer.getAccountTo(),transfer.getAmount());
    }

    @RequestMapping(path = "/account/{accountId}", method = RequestMethod.PUT)
    public void update(@RequestBody Account account, @PathVariable int accountId) {
        try {
            accountDao.updateAccount(accountId, account.getBalance());

        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
    }
//    @RequestMapping(path = "/account/{accountId}", method = RequestMethod.PUT)
//    public Account update(@RequestBody Account account, @PathVariable int accountId) {
//        try {
//            return accountDao.updateAccount(accountId, account.getUserId(), account.getBalance());
//
//        } catch (DaoException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
//        }
//    }

    @RequestMapping(path = "/update_transfer/{transferId}", method = RequestMethod.PUT)
    public Transfer update(@RequestBody Transfer transfer, @PathVariable int transferId){
        try {
            return transferDao.updateTransfer(transfer.getTransferStatusId(), transferId);
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found");
        }
    }

    @RequestMapping(path = "/transfer_get/{transId}", method = RequestMethod.GET)
        public Transfer getTransferById(@PathVariable int transId){
            return transferDao.getTransferById(transId);
        }


    @RequestMapping(path = "/account/{userId}", method = RequestMethod.GET)
    public Account getAccountByUserId(@PathVariable int userId){
        return accountDao.getAccountByUserId(userId);
    }

    @RequestMapping(path = "/account_get/{id}", method = RequestMethod.GET)
    public Account getAccountById(@PathVariable int id){
        return accountDao.getAccountById(id);
    }

    @RequestMapping(path="/account_id", method = RequestMethod.GET)
    public int getAccountId(Principal principal) {
        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);
        return accountDao.getAccountIdByUserId(userId);
    }

    @RequestMapping(path ="/transfer/{accountId}",method = RequestMethod.GET)
    public List<TransferDto> findTransfers(@PathVariable int accountId){
        List<TransferDto> transfers = transferDao.findTransfers(accountId);
       return transfers;
    }

    @RequestMapping(path ="/request/{accountId}", method = RequestMethod.GET)
    public List<RequestDto> viewRequests(@PathVariable int accountId){
        List<RequestDto> requests = transferDao.viewRequests(accountId);
        return requests;
    }



}
