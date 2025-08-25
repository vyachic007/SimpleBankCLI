package by.slava_borisov.impl;

import by.slava_borisov.dao.UserDao;
import by.slava_borisov.hibernate.entity.Account;
import by.slava_borisov.hibernate.entity.User;
import by.slava_borisov.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class InMemoryUserServiceImpl implements UserService {

    private final UserDao userDao;

    public void createUser(String login) {
        if (userDao.addUser(login)) {
            System.out.printf("Пользователь с логином %s успешно добавлен.%n", login);
        } else {
            System.out.printf("Ошибка. Логин \"%s\" уже существует.%n", login);
        }
    }

    public void showAllUsers() {
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователи не найдены");
            return;
        }
        System.out.println("Список всех пользователей:");
        for (User user : users) {
            System.out.printf("ID: %d, Логин: %s%n", user.getId(), user.getLogin());
            if (user.getAccounts() != null) {
                for (Account account : user.getAccounts()) {
                    System.out.printf("  Счёт ID: %d, Баланс: %.2f, Закрыт: %s%n",
                            account.getId(),
                            account.getBalance(),
                            account.isClosed() ? "Да" : "Нет");
                }
            }
        }
    }
}
