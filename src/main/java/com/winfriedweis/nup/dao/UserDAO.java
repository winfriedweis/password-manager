package com.winfriedweis.nup.dao;

import com.winfriedweis.nup.model.User;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String identifier);
    boolean createUser(User user);
    boolean updateProfileImage(int userId, byte[] imageData, String imageType);
    Optional<byte[]> getProfileImage(int userId);
}
