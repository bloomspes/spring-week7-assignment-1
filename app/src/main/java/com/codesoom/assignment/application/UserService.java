package com.codesoom.assignment.application;

import com.codesoom.assignment.domain.User;
import com.codesoom.assignment.domain.UserRepository;
import com.codesoom.assignment.dto.UserModificationData;
import com.codesoom.assignment.dto.UserRegistrationData;
import com.codesoom.assignment.errors.UserEmailDuplicationException;
import com.codesoom.assignment.errors.UserNotFoundException;
import com.codesoom.assignment.errors.UserNotMatchException;
import com.github.dozermapper.core.Mapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class UserService {
    private final Mapper mapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(Mapper dozerMapper, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.mapper = dozerMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationData registrationData) {
        String email = registrationData.getEmail();
        if (userRepository.existsByEmail(email)) {
            throw new UserEmailDuplicationException(email);
        }
        User user = User.builder()
                .name(registrationData.getName())
                .email(registrationData.getEmail())
                .password(passwordEncoder.encode(registrationData.getPassword()))
                .build();
        return userRepository.save(user);
    }

    public User updateUser(Long authenticatedId, Long id, UserModificationData modificationData) {
        checkAuthenticate(authenticatedId, id);

        User user = findUser(id);

        User source = User.builder()
                .name(modificationData.getName())
                .password(passwordEncoder.encode(modificationData.getPassword()))
                .build();

        user.changeWith(source);

        return user;
    }

    public User deleteUser(Long authenticatedId, Long id) {
        checkAuthenticate(authenticatedId, id);

        User user = findUser(id);
        user.destroy();
        return user;
    }

    private User findUser(Long id) {
        return userRepository.findByIdAndDeletedIsFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void checkAuthenticate(Long authenticatedId, Long id) {
        if (!isMatchUserId(authenticatedId, id)) {
            throw new UserNotMatchException(id);
        }
    }

    private boolean isMatchUserId(Long authenticatedId, Long id) {
        return authenticatedId.equals(id);
    }
}
