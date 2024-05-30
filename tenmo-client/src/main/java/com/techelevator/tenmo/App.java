package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Objects;


public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;
    RestTemplate restTemplate = new RestTemplate();
    private final BasicLogger logger = new BasicLogger();

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");

            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests(getAccountId());
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }
//  private void authenticate(){
//      RestTemplate restTemplate = new RestTemplate();
//      HttpHeaders headers = new HttpHeaders();
//      headers.setBearerAuth(currentUser.getToken());
//      HttpEntity<Void> entity = new HttpEntity<>(headers);
//  }

    private void viewCurrentBalance() {
        ResponseEntity response = restTemplate.exchange(
                API_BASE_URL, HttpMethod.GET, createHeader(), BigDecimal.class);
        System.out.println(response.getBody());
//		int userId = currentUser.getUser().getId();
//        Account account = new Account();
//        BigDecimal balance = account.getBalance();
    }

    private String getCurrentBalance() {
        ResponseEntity response = restTemplate.exchange(
                API_BASE_URL, HttpMethod.GET, createHeader(), BigDecimal.class);
        return response.getBody().toString();
    }

    private int getAccountId() {
        ResponseEntity response = restTemplate.exchange(API_BASE_URL + "/account_id", HttpMethod.GET, createHeader(), int.class);
        return Integer.parseInt(response.getBody().toString());
    }

    private Account getAccountByUserId(int userId) {
        ResponseEntity<Account> response = restTemplate.exchange(API_BASE_URL + "/account/" + userId, HttpMethod.GET, createHeader(), Account.class);
        return response.getBody();
    }

    private Account getAccountById(int id) {
        ResponseEntity<Account> response = restTemplate.exchange(API_BASE_URL + "/account_get/" + id, HttpMethod.GET, createHeader(), Account.class);
        return response.getBody();
    }

    private Transfer getTransferById(int transId) {
        ResponseEntity<Transfer> response = restTemplate.exchange(API_BASE_URL + "/transfer_get/" + transId, HttpMethod.GET, createHeader(), Transfer.class);
        return response.getBody();
    }

    private void viewTransferHistory() {
        findTransfers(getAccountId());
    }

    private void viewPendingRequests(int accountId) {
        ResponseEntity<RequestDTO[]> response = restTemplate.exchange(API_BASE_URL + "/request/" + accountId, HttpMethod.GET, createHeader(), RequestDTO[].class);
        RequestDTO[] requests = response.getBody();
        if (requests != null) {
            System.out.println("-------------------------------------------");
            System.out.println(" Pending Transfers:");
            System.out.println(" ID:        From:           Amount:");
            System.out.println("-------------------------------------------");
            boolean approvable = true;
            for (RequestDTO request : requests) {
                System.out.println(request.getTransferId() + "         " + request.getUsername() + "        " + "$" + request.getAmount());

            }
            System.out.println("--------------------------------------");
            int requestId = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel):");
            if (requestId != 0) {
                System.out.println("1: Approve\n" +
                        "2: Reject\n" +
                        "0: Don't approve or reject\n" +
                        "---------");
                int option = consoleService.promptForInt("Please choose an option:");
                if (option == 1) {
//                     TODO: getTransferById returns null for invalid IDs, calling getAmount() then crashes
                    BigDecimal requestAmount = getTransferById(requestId).getAmount();
                    if (requestAmount.compareTo(new BigDecimal(getCurrentBalance())) > 0) {
                        System.out.println("Cannot approve request: Account funds insufficient.");
                    } else {
                        updateTransfer(1, requestId);

                        BigDecimal newBalance = new BigDecimal(getCurrentBalance()).subtract(requestAmount);
                        updateAccount(getAccountId(), newBalance);

                        BigDecimal otherBalance = getAccountById(getTransferById(requestId).getAccountFrom()).getBalance().add(requestAmount);
                        updateAccount((getTransferById(requestId).getAccountFrom()), new BigDecimal(String.valueOf(otherBalance)));

                        System.out.println("Request Approved!");


                    }
                } else if (option == 2) {
                    updateTransfer(3, requestId);
                    System.out.println("Request Successfully Rejected.");
                }
                mainMenu();
            }
        }
    }


    private void findTransfers(int accountId) {
        ResponseEntity<TransferDTO[]> response = restTemplate.exchange(
                API_BASE_URL + "/transfer/" + accountId, HttpMethod.GET, createHeader(), TransferDTO[].class);
        TransferDTO[] transfers = response.getBody();
        if (transfers != null) {
            System.out.println("----------------------------------");
            System.out.println("Transfers");
            System.out.println("ID          From/To          Amount");
            System.out.println("----------------------------------------");
            for (TransferDTO transferDto : transfers) {
                if (Objects.equals(transferDto.getClassification(), "FROM")) {
                    System.out.println(transferDto.getTransferId() + "       From: " + transferDto.getUsername() + "       " + transferDto.getAmount());
                } else if (Objects.equals(transferDto.getClassification(), "TO")) {
                    System.out.println(transferDto.getTransferId() + "       To: " + transferDto.getUsername() + "          " + transferDto.getAmount());
                }
            }
            int transferId = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel):");
            if (transferId != 0) {
                for (TransferDTO transferDto : transfers) {
                    if (transferDto.getTransferId() == transferId) {
                        System.out.println("--------------------------------------------");
                        System.out.println("Transfer Details");
                        System.out.println("--------------------------------------------");
                        System.out.println("Id: " + transferDto.getTransferId());
                        if (Objects.equals(transferDto.getClassification(), "FROM")) {
                            System.out.println("From: " + transferDto.getUsername());
                            System.out.println("To: " + currentUser.getUser().getUsername());
                        } else if (Objects.equals(transferDto.getClassification(), "TO")) {
                            System.out.println("From: " + currentUser.getUser().getUsername());
                            System.out.println("To: " + transferDto.getUsername());
                        }
                        if (getTransferById(transferId).getTransferTypeId() == 1) {
                            System.out.println("Type: Send");
                        } else if (getTransferById(transferId).getTransferTypeId() == 2) {
                            System.out.println("Type: Request");
                            if (getTransferById(transferId).getTransferStatusId() == 2) {
                                System.out.println("Status: Pending");
                            } else if (getTransferById(transferId).getTransferStatusId() == 3) {
                                System.out.println("Status: Rejected");
                            }
                        }
                        if (getTransferById(transferId).getTransferStatusId() == 1) {
                            System.out.println("Status: Approved");
                        }
                        System.out.println("Amount: " + "$" + transferDto.getAmount());
                        System.out.println("-----------------------------------------");
                    }
                }
            }
        } else {
            System.out.println("No Transfers Found.");
        }
        mainMenu();
    }


    private void findAll() {
        ResponseEntity<User[]> response = restTemplate.exchange(
                API_BASE_URL + "/user", HttpMethod.GET, createHeader(), User[].class);
        User[] users = response.getBody();
        if (users != null) {
            System.out.println("-------------------------------------");
            System.out.println("Users");
            System.out.println("ID          Name");
            System.out.println("-------------------------------------");
            for (User user : users) {

                System.out.println(user.getId() + "      " + user.getUsername());

            }
            System.out.println("-------------------------------------");
        } else {
            System.out.println("No users found.");
        }


    }

    private void sendBucks() {
        findAll();
        Transfer transfer = new Transfer();
        transfer.setTransferTypeId(1);
        transfer.setTransferStatusId(1);
        transfer.setAccountFrom(currentUser.getUser().getId());
        int accountTo = consoleService.promptForInt("Enter ID of user you are sending to (0 to cancel):");
        transfer.setAccountTo(accountTo);
        if (accountTo == currentUser.getUser().getId()) {
            System.out.println("You cannot send to your own account.");
        }

        if (accountTo != 0 && accountTo != currentUser.getUser().getId()) {
            BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter amount:");
            transfer.setAmount(transferAmount);
            int currentUserId = currentUser.getUser().getId();
            BigDecimal balance = new BigDecimal(getCurrentBalance());
            if (transferAmount.compareTo(balance) > 0) {
                System.out.println("Account funds insufficient!");
            } else if (BigDecimal.ZERO.compareTo(transfer.getAmount()) > 0 || transferAmount.equals(BigDecimal.ZERO)) {
                System.out.println("Transfer must be greater than zero!");
            } else if (transfer.getAmount().compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(transfer.getAmount()) > 0) {


                ResponseEntity<Transfer> response = restTemplate.exchange(
                        API_BASE_URL + "/transfer", HttpMethod.POST, createEntityWithBody(transfer), Transfer.class);
                System.out.println("Transfer Sent!");
                int accountFrom = getAccountId();
//                int accountBalanceTo = response.getBody().getAccountTo();
                BigDecimal amountAdded = response.getBody().getAmount();
                BigDecimal newBalance = new BigDecimal(getCurrentBalance()).subtract(transfer.getAmount());
//                System.out.println(newBalance);
                updateAccount(accountFrom, newBalance);
//                updateAccount(accountFrom, currentUserId, newBalance);

                int accountFromNew = getAccountByUserId(accountTo).getAccountId();
                BigDecimal newBalanceNew = new BigDecimal(String.valueOf(getAccountByUserId(accountTo).getBalance())).add(transfer.getAmount());
//                System.out.println(newBalanceNew);
                updateAccount(accountFromNew,newBalanceNew);
//                updateAccount(accountFromNew, accountTo, newBalanceNew);
            }
        }

        mainMenu();

    }

    private void updateAccount(int accountFrom, BigDecimal newBalance) {
        Account account = new Account();
        account.setAccountId(accountFrom);
//        account.setUserId(accountUserId);
        account.setBalance(newBalance);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        HttpEntity<Account> entity = new HttpEntity<>(account, headers);
        restTemplate.put(API_BASE_URL + "/account/" + account.getAccountId(), entity);
//        return getAccountByUserId(account.getUserId());
    }

    private Transfer updateTransfer(int transferStatus, int transferId) {
        Transfer transfer = new Transfer();
        transfer.setTransferStatusId(transferStatus);
        transfer.setTransferId(transferId);
//        transfer.setAccountFrom(getTransferById(transferId).getAccountFrom());
//        transfer.setAccountTo(getTransferById(transferId).getAccountTo());
//        transfer.setAmount(getTransferById(transferId).getAmount());
//        transfer.setTransferTypeId(getTransferById(transferId).getTransferTypeId());
        restTemplate.put(API_BASE_URL + "/update_transfer/" + transferId, createEntityWithBody(transfer));
        transfer = getTransferById(transfer.getTransferId());
        return transfer;
    }

    private void requestBucks() {
        findAll();
        Transfer transfer = new Transfer();
        transfer.setTransferTypeId(2);
        transfer.setTransferStatusId(2);
        transfer.setAccountFrom(currentUser.getUser().getId());
        int accountTo = consoleService.promptForInt("Enter ID of user you are requesting from (0 to cancel): ");
        transfer.setAccountTo(accountTo);
        if (accountTo == currentUser.getUser().getId()) {
            System.out.println("You cannot request your own account.");
        }
        if (accountTo != 0 && accountTo != currentUser.getUser().getId()) {
            BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter amount:");
            transfer.setAmount(transferAmount);
            int currentUserId = currentUser.getUser().getId();
            BigDecimal balance = new BigDecimal(getCurrentBalance());
            if (BigDecimal.ZERO.compareTo(transfer.getAmount()) > 0 || transferAmount.equals(BigDecimal.ZERO)) {
                System.out.println("Request must be greater than zero!");
            } else if (transfer.getAmount().compareTo(BigDecimal.ZERO) > 0) {


                ResponseEntity<Transfer> response = restTemplate.exchange(
                        API_BASE_URL + "/transfer", HttpMethod.POST, createEntityWithBody(transfer), Transfer.class);
                System.out.println("Transfer Requested!");
            }
        }
    }


    private HttpEntity<Void> createHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Transfer> createEntityWithBody(Transfer body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(body, headers);
    }

}
