package by.slava_borisov.service;

import java.math.BigDecimal;


public interface AccountService {

    void createAccount(Long userId);

    void closeAccount(Long accountId);

    void accountDeposit(Long accountId, BigDecimal amount);

    void accountTransfer(Long senderAccountId, Long recipientAccountId, BigDecimal amount);

    void accountWithDraw(Long accountId, BigDecimal amount);

}
