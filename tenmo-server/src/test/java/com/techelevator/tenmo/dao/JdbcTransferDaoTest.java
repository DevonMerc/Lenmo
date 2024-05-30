package com.techelevator.tenmo.dao;

import com.techelevator.dao.BaseDaoTests;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDto;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class JdbcTransferDaoTest extends BaseDaoTests {
private TransferDao transferDao;

@Before
public void setup(){
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    AccountDao accountDao = new JdbcAccountDao(jdbcTemplate);
    transferDao = new JdbcTransferDao(jdbcTemplate, accountDao);
}
protected static final Transfer TRANSFER_1 = new Transfer(3001,1,1,1001,1002,new BigDecimal("50.00"));
    @Test
    public void create() {
        Transfer transfer = new Transfer();
        transfer.setTransferStatusId(1);
        transfer.setTransferTypeId(1);
        transfer.setAccountFrom(2001);
        transfer.setAccountTo(2002);
        transfer.setAmount(new BigDecimal("50"));
        Transfer createdTransfer = transferDao.create(transfer.getTransferTypeId(),transfer.getTransferStatusId(),1001,1002,transfer.getAmount());
        int newId = createdTransfer.getTransferId();
        Transfer retrieved = transferDao.getTransferById(newId);
        assertTransfersMatch(createdTransfer, retrieved);
    }

    private void assertTransfersMatch(Transfer expected, Transfer actual) {
        Assert.assertEquals(expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(expected.getTransferTypeId(), actual.getTransferTypeId());
        Assert.assertEquals(expected.getTransferStatusId(), actual.getTransferStatusId());
        Assert.assertEquals(expected.getAccountFrom(), actual.getAccountFrom());
        Assert.assertEquals(expected.getAccountTo(), actual.getAccountTo());
        Assert.assertEquals(expected.getAmount(), actual.getAmount());
    }



    @Test
    public void findTransfers() {

        Transfer transfer = new Transfer();
        transfer.setTransferStatusId(1);
        transfer.setTransferTypeId(1);
        transfer.setAccountFrom(2001);
        transfer.setAccountTo(2002);
        transfer.setAmount(new BigDecimal("50"));
        Transfer createdTransfer = transferDao.create(transfer.getTransferTypeId(),transfer.getTransferStatusId(),1001,1002,transfer.getAmount());

        List<TransferDto> transfers = transferDao.findTransfers(createdTransfer.getAccountFrom());


        Assert.assertNotNull(transfers);
        Assert.assertEquals(1,transfers.size());
    }

}