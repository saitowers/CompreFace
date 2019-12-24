package com.exadel.frs;

import com.exadel.frs.entity.User;
import com.exadel.frs.exception.EmailAlreadyRegisteredException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.exception.UserDoesNotExistException;
import com.exadel.frs.exception.UsernameAlreadyExistException;
import com.exadel.frs.repository.UserRepository;
import com.exadel.frs.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepositoryMock;
    private UserService userService;

    public UserServiceTest() {
        userRepositoryMock = mock(UserRepository.class);
        userService = new UserService(userRepositoryMock, PasswordEncoderFactories.createDelegatingPasswordEncoder());
    }

    @Test
    public void successGetUser() {
        Long userId = 1L;

        User user = User.builder().id(userId).build();

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getUser(userId);

        assertThat(result.getId(), is(userId));
    }

    @Test
    public void failGetUser() {
        Long userId = 1L;

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.empty());

        Assertions.assertThrows(UserDoesNotExistException.class, () -> userService.getUser(userId));
    }

    @Test
    public void successCreateUser() {
        User user = User.builder()
                .username("username")
                .password("password")
                .email("email")
                .build();

        userService.createUser(user);

        verify(userRepositoryMock).save(any(User.class));
    }

    @Test
    public void failCreateUserEmptyUsername() {
        User user = User.builder()
                .username("")
                .password("password")
                .email("email")
                .build();

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(user));
    }

    @Test
    public void failCreateUserEmptyPassword() {
        User user = User.builder()
                .username("username")
                .password("")
                .email("email")
                .build();

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(user));
    }

    @Test
    public void failCreateUserEmptyEmail() {
        User user = User.builder()
                .username("username")
                .password("password")
                .email("")
                .build();

        Assertions.assertThrows(EmptyRequiredFieldException.class, () -> userService.createUser(user));
    }

    @Test
    public void failCreateUserDuplicateUsername() {
        User user = User.builder()
                .username("username")
                .password("password")
                .email("email")
                .build();

        when(userRepositoryMock.findByUsername(anyString())).thenReturn(Optional.of(user));

        Assertions
                .assertThrows(UsernameAlreadyExistException.class, () -> userService.createUser(user));
    }

    @Test
    public void failCreateUserDuplicateEmail() {
        User user = User.builder()
                .username("username")
                .password("password")
                .email("email")
                .build();

        when(userRepositoryMock.findByEmail(anyString())).thenReturn(Optional.of(user));

        Assertions
                .assertThrows(EmailAlreadyRegisteredException.class, () -> userService.createUser(user));
    }

    @Test
    public void successUpdateUser() {
        Long userId = 1L;

        User repoUser = User.builder()
                .id(userId)
                .username("username")
                .password("password")
                .email("email")
                .firstName("firstName")
                .lastName("lastName")
                .build();

        User userUpdate = User.builder()
                .username("new_username")
                .password("new_password")
                .email("new_email")
                .firstName("new_firstName")
                .lastName("new_lastName")
                .build();

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(repoUser));

        userService.updateUser(userId, userUpdate);

        verify(userRepositoryMock).save(any(User.class));

        assertThat(repoUser.getFirstName(), is(userUpdate.getFirstName()));
        assertThat(repoUser.getLastName(), is(userUpdate.getLastName()));
        assertThat(repoUser.getEmail(), is(userUpdate.getEmail()));
        assertThat(repoUser.getPassword(), not("password"));
        assertThat(repoUser.getUsername(), not(userUpdate.getUsername()));
    }

    @Test
    public void failUpdateUser() {
        Long userId1 = 1L;
        Long userId2 = 2L;

        User user = User.builder()
                .id(userId1)
                .email("email")
                .build();

        User userUpdate = User.builder()
                .email("new_email")
                .build();

        User userWithSameEmail = User.builder()
                .id(userId2)
                .email("new_email")
                .build();

        when(userRepositoryMock.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepositoryMock.findByEmail(anyString())).thenReturn(Optional.of(userWithSameEmail));

        Assertions.assertThrows(EmailAlreadyRegisteredException.class, () -> userService.updateUser(userId1, userUpdate));
    }

    @Test
    public void successDeleteUser() {
        Long userId = 1L;

        userService.deleteUser(userId);

        verify(userRepositoryMock).deleteById(anyLong());
    }

}
