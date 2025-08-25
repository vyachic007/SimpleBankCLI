package by.slava_borisov.dao;

import by.slava_borisov.hibernate.entity.Account;
import by.slava_borisov.hibernate.entity.User;
import by.slava_borisov.util.TransactionHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class UserDao {

    private final TransactionHelper transactionHelper;

    @Value("${bank.default.balance}")
    private BigDecimal defaultBalance;

    public UserDao(TransactionHelper transactionHelper) {
        this.transactionHelper = transactionHelper;
    }

    public User getUserById(Long id) {
        return transactionHelper.executeInTransaction(session -> {
            return session.find(User.class, id);
        });
    }

    public User getUserByLogin(String login) {
        return transactionHelper.executeInTransaction(session -> {
            return session.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class)
                    .setParameter("login", login)
                    .uniqueResult();
        });
    }

    public boolean addUser(String login) {
        return transactionHelper.executeInTransaction(session -> {
            Long count = session.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.login = :login", Long.class)
                    .setParameter("login", login)
                    .uniqueResult();

            if (count > 0) {
                return false;
            }

            User user = User.builder()
                    .login(login)
                    .createdAt(LocalDateTime.now())
                    .build();
            session.persist(user);

            Account account = Account.builder()
                    .user(user)
                    .balance(defaultBalance)
                    .isClosed(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            session.persist(account);

            if (user.getAccounts() == null) {
                user.setAccounts(Collections.singletonList(account));
            } else {
                user.getAccounts().add(account);
            }

            return true;
        });
    }

    public List<User> getAllUsers() {
        return transactionHelper.executeInTransaction(session -> {
            List<User> users = session.createQuery("SELECT u FROM User u", User.class)
                    .getResultList();
            for (User user : users) {
                Hibernate.initialize(user.getAccounts());
            }
            return users;
        });
    }
}
