package by.slava_borisov.service;

import by.slava_borisov.dao.TransactionDao;
import by.slava_borisov.hibernate.entity.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionDao transactionDao;

    public TransactionService(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    public void showAllTransaction() {
        List<Transaction> transactions = transactionDao.getAllTransactions();
        if (!transactions.isEmpty()) {
            System.out.println("Список всех транзакций:");

            System.out.printf("%-5s %-15s %-20s %-30s %-15s %-15s%n",
                    "ID", "Сумма", "Тип", "Дата", "Отправитель", "Получатель");

            System.out.println("-".repeat(110));

            for (Transaction transaction : transactions) {
                System.out.printf("%-5d %-15s %-20s %-30s %-15s %-15s%n",
                        transaction.getId(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getCreatedAt(),
                        transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : "N/A",
                        transaction.getToAccount() != null ? transaction.getToAccount().getId() : "N/A");
            }
        } else {
            System.out.println("Транзакций не обнаружено.");
        }
    }
}