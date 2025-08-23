package by.slava_borisov.service;

import by.slava_borisov.dao.AccountDao;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountDao accountDao;

    public AccountService(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public void createAccount(Long userId) {
        if (accountDao.createAccount(userId)) {
            System.out.printf("Счёт для пользователя ID:%d создан.%n", userId);
        } else {
            System.out.printf("Ошибка. Пользователь с ID:%d не найден.%n", userId);
        }
    }


    public void closeAccount(Long accountId) {
        if (accountDao.closeAccount(accountId)) {
            System.out.printf("Счёт %d закрыт.%n", accountId);
        } else {
            System.out.println("Не удалось закрыть счёт.");
        }
    }

    public void accountDeposit(Long accountId, BigDecimal amount) {
        if (accountDao.deposit(accountId, amount)) {
            System.out.printf("Сумма %s переведена на счёт %d.%n", amount, accountId);
        } else {
            System.out.println("Ошибка. Счёт не пополнен.");
        }
    }

    public void accountTransfer(Long senderAccountId, Long recipientAccountId, BigDecimal amount) {
        if (accountDao.transfer(senderAccountId, recipientAccountId, amount)) {
            System.out.printf(
                    "Совершен перевод %s со счета отправителя: %d на счёт получателя: %d.%n",
                    amount, senderAccountId, recipientAccountId);
        } else {
            System.out.println("Ошибка. Деньги не переведены.");
        }
    }

    public void accountWithDraw(Long accountId, BigDecimal amount) {
        if (accountDao.withdraw(accountId, amount)) {
            System.out.printf("Сумма %s успешно снята со счёта %d.%n", amount, accountId);
        } else {
            System.out.println("Ошибка при снятии наличных.");
        }
    }

}
