package com.techelevator.tenmo.dao;

import com.techelevator.dao.BaseDaoTests;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class JdbcAccountDaoTest extends BaseDaoTests {

    private JdbcUserDao userDao;
    private JdbcAccountDao accountDao;
    protected static final Account ACCOUNT_1 = new Account(2001, 1001, new BigDecimal("450.00"));
    protected static final Account ACCOUNT_2 = new Account(2002, 1002, new BigDecimal("0.00"));
    private static Account ACCOUNT_3 = new Account(2003, 1003, new BigDecimal("2000.00"));


    @Before
    public void setup() {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        userDao = new JdbcUserDao(template);
        accountDao = new JdbcAccountDao(template);
    }

    @Test
    public void new_user_has_1000_balance() {
        userDao.create("test", "test");
        int userId = userDao.findIdByUsername("test");
        BigDecimal balance = accountDao.getBalanceByUserId(userId);
        assertEquals(new BigDecimal("1000.00"), balance);
    }

    @Test
    public void get_balance_by_user_id_returns_balance() {
        BigDecimal balance = accountDao.getBalanceByUserId(ACCOUNT_1.getUserId());
        assertEquals(ACCOUNT_1.getBalance(), balance);
    }

    @Test(expected = NoSuchElementException.class)
    public void get_balance_by_invalid_user_id() {
        accountDao.getBalanceByUserId(45);
    }

//    @Test
//    public void update_account_updates_account_balance() {
//        Account accountToUpdate = accountDao.getAccountByUserId(1003);
//
//        accountToUpdate.setBalance(new BigDecimal("3300.00"));
//
//        Account updatedAccount = accountDao.updateAccount(accountToUpdate.getId(), accountToUpdate.getUserId(), accountToUpdate.getBalance());
//
//        Account retrievedAccount = accountDao.getAccountByUserId(1003);
//
//        Assert.assertEquals(updatedAccount.getBalance(), retrievedAccount.getBalance());
//
//
//    }

    @Test
    public void get_account_id_by_user_id_valid() {
    int returnedId = accountDao.getAccountIdByUserId(ACCOUNT_1.getUserId());
    assertEquals(ACCOUNT_1.getId(), returnedId);
    }

    @Test(expected = NoSuchElementException.class)
    public void get_account_id_by_invalid() {
        accountDao.getAccountIdByUserId(6000);
    }

    @Test
    public void get_account_by_user_id(){
        Account returned = accountDao.getAccountByUserId(ACCOUNT_2.getUserId());
        assertAccountsMatch(ACCOUNT_2, returned);
    }
    private void assertAccountsMatch(Account expected, Account actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getBalance(), actual.getBalance());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
    }
    @Test(expected = NoSuchElementException.class)
    public void get_account_by_user_id_invalid(){
        accountDao.getAccountByUserId(6);
    }

}