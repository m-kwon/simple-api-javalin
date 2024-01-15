package DAO;

import Model.Message;
import Util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    private boolean isValidMessage(String messageText) {
        return messageText != null && !messageText.trim().isEmpty() && messageText.length() <= 255;
    }

    private boolean accountExists(int accountId) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT COUNT(*) FROM account WHERE account_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, accountId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public Message createMessage(Message message) {
        if (!isValidMessage(message.getMessage_text()) || !accountExists(message.getPosted_by())) {
            return null;
        }

        Connection connection = ConnectionUtil.getConnection();

        try {
            String sql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, message.getPosted_by());
                preparedStatement.setString(2, message.getMessage_text());
                preparedStatement.setLong(3, message.getTime_posted_epoch());
                preparedStatement.executeUpdate();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int generatedMessageId = generatedKeys.getInt(1);
                    return new Message(generatedMessageId, message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating message: " + e.getMessage());
        }

        return null;
    }

    public List<Message> getAllMessages() {
        Connection connection = ConnectionUtil.getConnection();
        List<Message> messages = new ArrayList<>();
        try {
            String sql = "SELECT * FROM message";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Message message = new Message(
                    resultSet.getInt("message_id"),
                    resultSet.getInt("posted_by"),
                    resultSet.getString("message_text"),
                    resultSet.getLong("time_posted_epoch")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return messages;
    }

    public Message getMessageById(int messageId) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            String sql = "SELECT * FROM message WHERE message_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, messageId);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return new Message(
                        resultSet.getInt("message_id"),
                        resultSet.getInt("posted_by"),
                        resultSet.getString("message_text"),
                        resultSet.getLong("time_posted_epoch")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching message by ID: " + e.getMessage());
        }

        return null;
    }

    public Message deleteMessage(int messageId) {
        Connection connection = ConnectionUtil.getConnection();
        try {
            Message deletedMessage = getMessageById(messageId);

            if (deletedMessage != null) {
                String sql = "DELETE FROM message WHERE message_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, messageId);
                    preparedStatement.executeUpdate();
                }

                return deletedMessage;
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public Message updateMessage(int messageId, Message message) {
        if (!isValidMessage(message.getMessage_text())) {
            System.out.println("Message must not be blank and not over 255 characters.");
            return null;
        }

        Message updatedMessage = getMessageById(messageId);

        if (updatedMessage != null) {
            Connection connection = ConnectionUtil.getConnection();
            try {
                String sql = "UPDATE message SET message_text = ? WHERE message_id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, message.getMessage_text());
                    preparedStatement.setInt(2, messageId);
                    preparedStatement.executeUpdate();
                }

                updatedMessage.setMessage_text(message.getMessage_text());

                return updatedMessage;
            } catch (SQLException e) {
                System.out.println("Error updating message: " + e.getMessage());
            }
        } else {
            System.out.println("Message must already exist to be updated.");
        }

        return null;
    }

    public List<Message> getMessagesByAccountId(int accountId) {
        Connection connection = ConnectionUtil.getConnection();
        List<Message> messages = new ArrayList<>();
        try {
            String sql = "SELECT * FROM message WHERE posted_by = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, accountId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                Message message = new Message(
                    resultSet.getInt("message_id"),
                    resultSet.getInt("posted_by"),
                    resultSet.getString("message_text"),
                    resultSet.getLong("time_posted_epoch")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return messages;
    }
}