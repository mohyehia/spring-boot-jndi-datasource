package com.mohyehia.jndiDatasource.dao;

import com.mohyehia.jndiDatasource.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDAO extends JpaRepository<User, Long> {
}
