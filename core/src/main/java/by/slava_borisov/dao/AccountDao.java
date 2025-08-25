package by.slava_borisov.dao;

import by.slava_borisov.hibernate.entity.Account;
import by.slava_borisov.hibernate.entity.TransactionType;
import by.slava_borisov.hibernate.entity.User;
import by.slava_borisov.util.TransactionHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountDao {

    private final TransactionHelper transactionHelper;
    private final UserDao userDao;
    private final TransactionDao transactionDao;

    @Value("${bank.default.balance}")
    private BigDecimal defaultBalance;
    @Value("${bank.fee}")
    private BigDecimal fee;

    public AccountDao(TransactionHelper transactionHelper, UserDao userDao, TransactionDao transactionDao) {
        this.transactionHelper = transactionHelper;
        this.userDao = userDao;
        this.transactionDao = transactionDao;
    }


    public Account getAccountById(Long id) {
        return transactionHelper.executeInTransaction(session -> {
            return session.find(Account.class, id);
        });
    }

    public boolean createAccount(Long id) {
        return transactionHelper.executeInTransaction(session -> {
            User user = userDao.getUserById(id);
            if (user != null) {
                Account account = Account.builder()
                        .user(user)
                        .balance(defaultBalance)
                        .createdAt(LocalDateTime.now())
                        .isClosed(false)
                        .build();
                session.persist(account);

                transactionDao.createTransaction(TransactionType.ACCOUNT_CREATED, defaultBalance, account.getId());

                return true;
            } else {
                return false;
            }
        });
    }

    public boolean closeAccount(Long accountId) {
        return transactionHelper.executeInTransaction(session -> {
            Account account = session.find(Account.class, accountId);
            if (account == null) {
                System.out.println("Счёт с ID " + accountId + " не найден.");
                return false;
            }
            if (account.isClosed()) {
                System.out.println("Счёт уже закрыт.");
                return false;
            }

            User user = account.getUser();

            List<Account> activeAccounts = session.createQuery(
                            "SELECT a FROM Account a WHERE a.user = :user AND a.isClosed = false")
                    .setParameter("user", user)
                    .getResultList();

            if (activeAccounts.size() == 1) {
                System.out.println("Нельзя закрыть единственный счёт пользователя");
                return false;
            }

            Account targetAccount = activeAccounts.stream()
                    .filter(acc -> !acc.getId().equals(account.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Не найден активный счёт для перевода"));

            BigDecimal balance = account.getBalance();
            targetAccount.setBalance(targetAccount.getBalance().add(balance));
            session.merge(targetAccount);

            account.setClosed(true);
            session.merge(account);

            transactionDao.createTransaction(TransactionType.ACCOUNT_CLOSED, balance, account.getId(),
                    targetAccount.getId());

            System.out.println(balance + " переведено на счёт " + targetAccount.getId());
            return true;
        });
    }

    public boolean deposit(Long accountId, BigDecimal amount) {
        return transactionHelper.executeInTransaction(session -> {
            Account account = session.find(Account.class, accountId);
            if (account == null || account.isClosed()) {
                System.out.println("Данного счета не существует или счёт закрыт.");
                return false;
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Сумма не может быть отрицательной.");
                return false;
            }
            account.setBalance(account.getBalance().add(amount));
            session.merge(account);

            transactionDao.createTransaction(TransactionType.DEPOSIT, amount, accountId);

            return true;
        });
    }

    public boolean transfer(Long senderId, Long recipientId, BigDecimal amount) {
        return transactionHelper.executeInTransaction(session -> {
            Account senderAccount = session.find(Account.class, senderId);
            Account recipientAccount = session.find(Account.class, recipientId);

            if (senderAccount == null || recipientAccount == null ||
                    senderAccount.isClosed() || recipientAccount.isClosed()) {
                System.out.println("Данного счета не существует или счёт закрыт.");
                return false;
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Сумма не может быть отрицательной.");
                return false;
            }

            boolean isInterUserTransfer = !senderAccount.getUser().getId()
                    .equals(recipientAccount.getUser().getId());

            BigDecimal feeAmount = isInterUserTransfer ? amount.multiply(fee) : BigDecimal.ZERO;

            if (senderAccount.getBalance().compareTo(amount.add(feeAmount)) < 0) {
                System.out.println("Недостаточно средств на счете.");
                return false;
            }

            senderAccount.setBalance(senderAccount.getBalance().subtract(amount).subtract(feeAmount));
            recipientAccount.setBalance(recipientAccount.getBalance().add(amount));

            session.merge(senderAccount);
            session.merge(recipientAccount);

            transactionDao.createTransaction(TransactionType.TRANSFER, amount, senderId, recipientId);

            if (feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                transactionDao.createTransaction(TransactionType.FEE, feeAmount, senderId);
                System.out.printf("Комиссия составила: %s%n", feeAmount);
            }

            return true;
        });
    }

    public boolean withdraw(Long accountId, BigDecimal amount) {
        return transactionHelper.executeInTransaction(session -> {
            Account account = session.find(Account.class, accountId);

            if (account == null) {
                System.out.println("Данного счета не существует.");
                return false;
            }

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("Сумма не может быть отрицательной.");
                return false;
            }

            if (account.isClosed()) {
                System.out.printf("Cчёт %d закрыт.%n", accountId);
                return false;
            }

            if (account.getBalance().compareTo(amount) < 0) {
                System.out.println("Не достаточно средств");
                return false;
            }

            account.setBalance(account.getBalance().subtract(amount));
            session.merge(account);

            transactionDao.createTransaction(TransactionType.WITHDRAWAL, amount, accountId);

            return true;
        });
    }
}