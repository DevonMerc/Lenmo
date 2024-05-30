package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

@Component
@PreAuthorize("isAuthenticated()")
public class JdbcAccountDao implements AccountDao {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public BigDecimal getBalanceByUserId(int userId) {
        String sql = "SELECT * FROM account WHERE user_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId);
        if (result.next()) {
            Account account = mapRowToAccount(result);
            return account.getBalance();
        } else {
            throw new NoSuchElementException("No account found for user ID: " + userId);
        }
    }

    public int getAccountIdByUserId(int userId) {
        String sql = "SELECT * FROM account WHERE user_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId);
        if (result.next()) {
            Account account = mapRowToAccount(result);
            return account.getId();
        } else {
            throw new NoSuchElementException("No account found for user ID: " + userId);
        }
    }

    public Account getAccountById(int id) {
        String sql = "SELECT * FROM account WHERE account_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, id);
        if (result.next()) {
            Account account = mapRowToAccount(result);
            return account;
        } else {
            throw new NoSuchElementException("No account found for ID: " + id);
        }
    }


    @Override
    public void updateAccount(int accountFrom, BigDecimal balance) {
        Account updatedAccount = null;
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
        jdbcTemplate.update(sql, balance, accountFrom);
//        updatedAccount = getAccountByUserId(userId);
//        return updatedAccount;
    }

    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }

    public Account getAccountByUserId(int userId) {
        Account account = null;
        String sql = "SELECT * FROM account WHERE user_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, userId);
        if (result.next()) {
            account = mapRowToAccount(result);
        } else {
            throw new NoSuchElementException("No Account Found For User ID: " + userId);
        }
        return account;
    }


}
