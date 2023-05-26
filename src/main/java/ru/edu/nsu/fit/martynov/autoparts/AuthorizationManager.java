package ru.edu.nsu.fit.martynov.autoparts;

import ru.edu.nsu.fit.martynov.autoparts.db.DataBaseController;
import ru.edu.nsu.fit.martynov.autoparts.dto.UserDto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class AuthorizationManager {
    public static boolean authorize(String login, String password) throws SQLException {
        UserDto userInfo = DataBaseController.getInstance().getUserByLogin(login);
        if (userInfo == null) return false;
        if (hashPassword(password).equals(userInfo.password()))
            return true;
        return false;
    }

    private static String hashPassword(String password) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        md.update(password.getBytes());
        byte[] bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
