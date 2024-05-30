package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {

BigDecimal getBalanceByUserId(int userId);
int getAccountIdByUserId(int userId);



    void updateAccount(int accountFrom, BigDecimal balance);

    Account getAccountByUserId(int userId);

     Account getAccountById(int id);

}
