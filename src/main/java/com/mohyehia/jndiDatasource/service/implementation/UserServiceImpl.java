package com.mohyehia.jndiDatasource.service.implementation;

import com.mohyehia.jndiDatasource.dao.UserDAO;
import com.mohyehia.jndiDatasource.entity.User;
import com.mohyehia.jndiDatasource.service.framework.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;

    @Autowired
    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public List<User> findAll() {
        return userDAO.findAll();
    }

    @Override
    public User save(User user) {
        return userDAO.save(user);
    }
}
