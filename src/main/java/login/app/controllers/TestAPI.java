package login.app.controllers;

import login.app.model.User;
import login.app.repository.UserRepository;
import login.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestAPI {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    EmailService emailService;
    @GetMapping("/get-all")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getListUser(){
        return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
    }
    @PostMapping("/send")
    public ResponseEntity<User> send(@RequestBody User user){
        emailService.sendEmail(user.getEmail(), "dấdsad", "dsadsadas");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
