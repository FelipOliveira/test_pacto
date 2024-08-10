package com.br.foliveira.test_pacto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.br.foliveira.test_pacto.models.User;
import com.br.foliveira.test_pacto.repository.UserRepository;

@DataJpaTest
public class UserJpaUnitTest {
    
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository repository;

    @Test
    public void getUserById_returnUserDataById(){
        List<User> users = Stream.of(
            new User(),
            new User(),
            new User()
        ).collect(Collectors.toList());

        users.forEach(entityManager::persist);
        User userData = repository.findById(users.get(1).getId()).get();
        assertThat(userData).isEqualTo(users.get(1));
    }

    @Test
    public void postUser_returnsSavedUserData(){
        User user = new User("name1", "email@test.com", "123456");
        entityManager.persist(user);

        User userData = repository.findById(user.getId()).orElse(new User());

        assertThat(userData).hasFieldOrPropertyWithValue("username", "name1");
        assertThat(userData).hasFieldOrPropertyWithValue("email", "email@test.com");
        assertThat(userData).hasFieldOrPropertyWithValue("password", "123456");
    }

    @Test
    public void deleteUserById_returnsListWithTwoUsers(){
        List<User> users = Stream.of(
            new User(),
            new User(),
            new User()
        ).collect(Collectors.toList());
        
        users.forEach(entityManager::persist);
        repository.deleteById(users.get(1).getId());

        Iterable<User> userData = repository.findAll();
        assertThat(userData).hasSize(2).contains(users.get(0), users.get(2));
    }
}
