package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
public class UserController {
    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    // TODO: wire in the user repository (~ 1 line)
    @Autowired
    private UserRepository userRepository;

    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        // TODO: Create an user entity with information given in the payload, store it in the database
        //       and return the id of the user in 200 OK response
        LOGGER.info("Received request to create user with name [" + payload.getName() + "] and email [" + payload.getEmail() + "]");
        try {
            User user = new User();
            user.setName(payload.getName());
            user.setEmail(payload.getEmail());
            User savedUser = userRepository.save(user);

            LOGGER.info("User with id [" + savedUser.getId() + "] created successfully");
            return ResponseEntity.ok(savedUser.getId());
        } catch (Exception e) {
            LOGGER.severe("Error creating user with name [" + payload.getName() + "] and email [" + payload.getEmail() + "]");
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // TODO: Return 200 OK if a user with the given ID exists, and the deletion is successful
        //       Return 400 Bad Request if a user with the ID does not exist
        //       The response body could be anything you consider appropriate
        LOGGER.info("Received request to delete user with id [" + userId + "]");
        try {
            if (userRepository.existsById(userId)) {
                userRepository.deleteById(userId);

                LOGGER.info("User with id [" + userId + "] deleted successfully");
                return ResponseEntity.ok("User with id [" + userId + "] deleted successfully");
            } else {
                LOGGER.warning("User with id [" + userId + "] does not exist");
                return ResponseEntity.badRequest().body("User with id [" + userId + "] does not exist");
            }
        } catch (Exception e) {
            LOGGER.severe("Error deleting user with id [" + userId + "]");
            return ResponseEntity.internalServerError().body("Error deleting user with id [" + userId + "]");
        }
    }
}
