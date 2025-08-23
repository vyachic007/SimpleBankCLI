package by.slava_borisov.dao;

import by.slava_borisov.hibernate.entity.Transaction;
import by.slava_borisov.hibernate.entity.TransactionType;
import by.slava_borisov.util.TransactionHelper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class TransactionDao {

    private final TransactionHelper transactionHelper;

    public TransactionDao(TransactionHelper transactionHelper) {
        this.transactionHelper = transactionHelper;
    }

    public void createTransaction(TransactionType type, BigDecimal amount, Long fromAccountId, Long toAccountId) {
        transactionHelper.executeInTransaction(session -> {
            Transaction transaction = Transaction.builder()
                    .type(type)
                    .amount(amount)
                    .createdAt(LocalDateTime.now())
                    .fromAccount(fromAccountId != null ?
                            session.find(by.slava_borisov.hibernate.entity.Account.class, fromAccountId) : null)
                    .toAccount(toAccountId != null ?
                            session.find(by.slava_borisov.hibernate.entity.Account.class, toAccountId) : null)
                    .build();
            session.persist(transaction);
            return null;
        });
    }

    public void createTransaction(TransactionType type, BigDecimal amount, Long accountId) {
        createTransaction(type, amount, accountId, null);
    }

    public List<Transaction> getAllTransactions() {
        return transactionHelper.executeInTransaction(session -> {
            return session.createQuery("SELECT t FROM Transaction t", Transaction.class)
                    .getResultList();
        });
    }
}