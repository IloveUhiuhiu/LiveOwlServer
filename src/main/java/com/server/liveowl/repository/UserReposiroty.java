package com.server.liveowl.repository;

import com.server.liveowl.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserReposiroty extends JpaRepository<Users, Integer> {
    List<Users> findByUserNameAndPassWord(String username, String password);

    List<Users> findByUserNameAndEmail(String username, String email);

    List<Users> findByUserNameOrEmail(String username, String email);
}
