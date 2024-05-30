package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.RequestDto;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {
    private final JdbcTemplate jdbcTemplate;
    private final AccountDao accountDao;


    public JdbcTransferDao(JdbcTemplate jdbcTemplate, AccountDao accountDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountDao = accountDao;
    }

    public Transfer create(int transferTypeId, int transferStatusId, int accountFrom, int accountTo, BigDecimal amount) {
        Transfer transfer = null;

        int accountFromNew = accountDao.getAccountIdByUserId(accountFrom);
        int accountToNew = accountDao.getAccountIdByUserId(accountTo);

        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?,?,?,?,?) RETURNING transfer_id";
        try {
            Integer newTransferId = jdbcTemplate.queryForObject(sql, Integer.class, transferTypeId, transferStatusId, accountFromNew, accountToNew, amount);
           if(newTransferId != null) {
               transfer = getTransferById(newTransferId);
           } else {
               throw new IllegalStateException("Failed to create transfer.");
           }
        } catch (CannotGetJdbcConnectionException e) {
            System.out.println(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            System.out.println(e.getMessage());
        }
        return transfer;
    }

//    public List<Transfer> findTransfers(int accountId) {
//        List<Transfer> transfers = new ArrayList<>();
//        String sql = "SELECT account_from, amount, 'SENT' AS classification " +
//                "FROM transfer WHERE account_from = ?" +
//                " UNION" +
//                " SELECT account_to, amount, 'RECEIVED' AS classification" +
//                " FROM transfer " +
//                "WHERE account_to = ?";
//        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
//        while (results.next()) {
//            Transfer transfer = mapRowToTransfer(results);
//            transfers.add(transfer);
//        }
//        return transfers;
//
//    }
public List<TransferDto> findTransfers(int accountId) {
    List<TransferDto> transfers = new ArrayList<>();
    String sql = "SELECT 'TO' AS classification, username, amount, transfer_id\n" +
            "FROM transfer JOIN account ON transfer.account_to = account.account_id \n" +
            "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
            "WHERE account_from = ? " +
            "UNION " +
            "SELECT 'FROM' AS classification, username, amount, transfer_id\n" +
            "FROM transfer JOIN account ON transfer.account_from = account.account_id \n" +
            "JOIN tenmo_user ON account.user_id = tenmo_user.user_id\n" +
            "WHERE account_to = ? " +
            "ORDER BY transfer_id ";

    SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
    while (results.next()) {
        TransferDto transferDto = mapRowToTransferDto(results);
        transfers.add(transferDto);
    }
    return transfers;

}
public List<RequestDto> viewRequests(int accountId){
        List<RequestDto> requests = new ArrayList<>();
        String sql = "SELECT username, amount, transfer_id " +
                "FROM transfer JOIN account ON transfer.account_from = account.account_id " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE account_to = ? AND transfer_type_id = 2 AND transfer_status_id = 2";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,accountId);
        while(results.next()){
            RequestDto requestDto = mapRowToRequestDto(results);
            requests.add(requestDto);
        }
        return requests;
}

    @Override
    public Transfer updateTransfer(int transferStatus, int transferId) {
        Transfer updatedTransfer = null;
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        jdbcTemplate.update(sql, transferStatus, transferId);
        updatedTransfer = getTransferById(transferId);
        return updatedTransfer;
    }

    public Transfer getTransferById(Integer newTransferId) {
        String sql = "SELECT * FROM transfer WHERE transfer_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, newTransferId);
        if (results.next()) {
            return mapRowToTransfer(results);
        } else {
            return null;
        }
    }

    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setAmount(rs.getBigDecimal("amount"));
        transfer.setAccountTo(rs.getInt("account_to"));
        transfer.setAccountFrom(rs.getInt("account_from"));


        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setTransferTypeId(rs.getInt("transfer_type_id"));
        transfer.setTransferStatusId(rs.getInt("transfer_status_id"));
        return transfer;
    }
    private TransferDto mapRowToTransferDto(SqlRowSet rs) {
        TransferDto transferDto = new TransferDto();
        transferDto.setClassification(rs.getString("classification"));
        transferDto.setAmount(rs.getBigDecimal("amount"));
        transferDto.setTransferId(rs.getInt("transfer_id"));
        transferDto.setUsername(rs.getString("username"));

        return transferDto;
    }

    private RequestDto mapRowToRequestDto(SqlRowSet rs){
        RequestDto request = new RequestDto();
//        request.setAccountFrom(rs.getInt("account_from"));
//        request.setAccountTo(rs.getInt("acocunt_to"));
        request.setAmount(rs.getBigDecimal("amount"));
        request.setTransferId(rs.getInt("transfer_id"));
//        request.setTransferStatus(rs.getInt("transfer_status"));
//        request.setTransferType(rs.getInt("transfer_type"));
        request.setUsername(rs.getString("username"));
        return request;
    }
}
