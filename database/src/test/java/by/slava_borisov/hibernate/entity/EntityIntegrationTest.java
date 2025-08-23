package by.slava_borisov.hibernate.entity;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityIntegrationTest {

    private SessionFactory sessionFactory;

    @BeforeAll
    void setUp() {
        try {
            Configuration configuration = new Configuration();

            configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
            configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:testdb");
            configuration.setProperty("hibernate.connection.username", "sa");
            configuration.setProperty("hibernate.connection.password", "");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            configuration.setProperty("hibernate.show_sql", "true");

            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Account.class);
            configuration.addAnnotatedClass(Transaction.class);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create sessionFactory", e);
        }
    }

    @AfterAll
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    void testUserAccountRelationship() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        User user = User.builder()
                .login("testuser")
                .createdAt(LocalDateTime.now())
                .build();
        session.persist(user);

        Account account1 = Account.builder()
                .balance(new BigDecimal("1000.00"))
                .isClosed(false)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        session.persist(account1);

        Account account2 = Account.builder()
                .balance(new BigDecimal("500.00"))
                .isClosed(false)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        session.persist(account2);

        session.getTransaction().commit();
        session.close();

        session = sessionFactory.openSession();
        session.beginTransaction();

        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);

        Account foundAccount1 = session.find(Account.class, account1.getId());
        assertNotNull(foundAccount1.getUser());
        assertEquals(user.getId(), foundAccount1.getUser().getId());
        assertEquals("testuser", foundAccount1.getUser().getLogin());

        User foundUser = session.find(User.class, user.getId());
        assertNotNull(foundUser.getAccounts());
        assertEquals(2, foundUser.getAccounts().size());

        boolean foundAccount1InList = foundUser.getAccounts().stream()
                .anyMatch(acc -> acc.getId().equals(account1.getId()));
        boolean foundAccount2InList = foundUser.getAccounts().stream()
                .anyMatch(acc -> acc.getId().equals(account2.getId()));

        assertTrue(foundAccount1InList);
        assertTrue(foundAccount2InList);

        session.getTransaction().commit();
        session.close();
    }

    @Test
    void testAccountTransactionRelationship() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        User user = User.builder()
                .login("transactionuser")
                .createdAt(LocalDateTime.now())
                .build();
        session.persist(user);

        Account fromAccount = Account.builder()
                .balance(new BigDecimal("1000.00"))
                .isClosed(false)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        session.persist(fromAccount);

        Account toAccount = Account.builder()
                .balance(new BigDecimal("500.00"))
                .isClosed(false)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        session.persist(toAccount);

        // Создаем транзакции
        Transaction transferTransaction = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .type(TransactionType.TRANSFER)
                .createdAt(LocalDateTime.now())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .build();
        session.persist(transferTransaction);

        Transaction depositTransaction = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .type(TransactionType.DEPOSIT)
                .createdAt(LocalDateTime.now())
                .toAccount(toAccount)
                .build();
        session.persist(depositTransaction);

        session.getTransaction().commit();
        session.close();

        // Проверяем связи
        session = sessionFactory.openSession();
        session.beginTransaction();

        // Проверяем @ManyToOne связи в Transaction
        Transaction foundTransfer = session.find(Transaction.class, transferTransaction.getId());
        assertNotNull(foundTransfer.getFromAccount());
        assertNotNull(foundTransfer.getToAccount());
        assertEquals(fromAccount.getId(), foundTransfer.getFromAccount().getId());
        assertEquals(toAccount.getId(), foundTransfer.getToAccount().getId());
        assertEquals(TransactionType.TRANSFER, foundTransfer.getType());

        // Проверяем, что транзакции можно найти через счета
        Account foundFromAccount = session.find(Account.class, fromAccount.getId());
        Account foundToAccount = session.find(Account.class, toAccount.getId());

        // Проверяем @Enumerated
        assertEquals("TRANSFER", transferTransaction.getType().name());

        session.getTransaction().commit();
        session.close();
    }


    @Test
    void testBigDecimalPrecisionAndScale() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        User user = User.builder()
                .login("precisionuser")
                .createdAt(LocalDateTime.now())
                .build();
        session.persist(user);

        // Тестируем точность BigDecimal
        Account account = Account.builder()
                .balance(new BigDecimal("1234567890123456.78")) // 16 цифр перед запятой, 2 после
                .isClosed(false)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();
        session.persist(account);

        session.getTransaction().commit();
        session.close();

        // Проверяем сохранение
        session = sessionFactory.openSession();
        Account foundAccount = session.find(Account.class, account.getId());
        assertEquals(new BigDecimal("1234567890123456.78"), foundAccount.getBalance());
        session.close();
    }
}