package DAO;

import Model.Account;
import Util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {
    private boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 4;
    }

    private boolean accountExists(String username) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT * FROM account WHERE username = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public Account registerAccount(Account account) {
        if (!isValidUsername(account.getUsername()) || !isValidPassword(account.getPassword()) || accountExists(account.getUsername())) {
            return null;
        }

        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "INSERT INTO account (username, password) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, account.getUsername());
                preparedStatement.setString(2, account.getPassword());
                preparedStatement.executeUpdate();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedAccountId = generatedKeys.getInt(1);
                    return new Account(generatedAccountId, account.getUsername(), account.getPassword());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating message: " + e.getMessage());
        }

        return null;
    }

    public Account loginAccount(Account account) {
        if (!isValidUsername(account.getUsername())) {
            System.out.println("Username must not be blank.");
            return null;
        }

        if (!isValidPassword(account.getPassword())) {
            System.out.println("Password must be at least 4 characters long.");
            return null;
        }

        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT * FROM account WHERE username = ? AND password = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, account.getUsername());
                preparedStatement.setString(2, account.getPassword());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int accountId = resultSet.getInt("account_id");
                        return new Account(accountId, account.getUsername(), account.getPassword());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }
}