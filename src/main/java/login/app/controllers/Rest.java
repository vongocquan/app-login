package login.app.controllers;

import login.app.common.JwtUtils;
import login.app.model.ERole;
import login.app.model.Role;
import login.app.model.User;
import login.app.model.dto.JwtResponse;
import login.app.model.dto.LoginRequest;
import login.app.model.dto.MessageResponse;
import login.app.model.dto.SignupRequest;
import login.app.repository.RoleRepository;
import login.app.repository.UserRepository;
import login.app.service.EmailService;
import login.app.service.impl.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Rest {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private EmailService emailService;
    @PostMapping("/sign-in")
    public ResponseEntity<?> authenticateUser(@Validated @RequestBody LoginRequest loginRequest){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        System.out.println(userDetails.getBirthday());
        JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles,
                userDetails.getLastname(), userDetails.getFirtname(), userDetails.getPhone(), userDetails.getImage(), userDetails.getBirthday(), userDetails.getAddress());

        return ResponseEntity.ok(jwtResponse);
    }
    @PostMapping("/sign-up")
    public ResponseEntity<?> registerUser(@Validated @RequestBody User user){
        if (userRepository.existsByUsername(user.getUsername())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("user name already exist!"));
        }
        if (userRepository.existsByEmail(user.getEmail())){
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("email already exist!"));
        }


        user.setPassword(encoder.encode(user.getPassword()));
        user.setImage("https://ict-imgs.vgcloud.vn/2020/09/01/19/huong-dan-tao-facebook-avatar.jpg");
        Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        userRepository.save(user);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/send-mail/{email}")
    public ResponseEntity<?> sendMailForgotPassword(@PathVariable String email){
        if (!userRepository.existsByEmail(email)){
            return ResponseEntity.badRequest().body(new MessageResponse("email no already exist!"));
        }
        Random random = new Random();
        long key = random.nextLong();
        User user = userRepository.findByEmail(email);
        user.setNum(key);
        userRepository.save(user);

        emailService.sendEmail(email, "Forgot Password", "verification code: " + key);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @GetMapping("/check-mail/{email}/{num}")
    public ResponseEntity<?> checkKey(@PathVariable String email, @PathVariable long num){
        User user = userRepository.findByEmail(email);
        if (user.getNum() == num){
            Random random = new Random();
            long key = random.nextLong();
            user.setNum(key);
            userRepository.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body(new MessageResponse("error"));
    }
    @GetMapping("/edit-password/{password1}/{password2}/{email}")
    public ResponseEntity<?> editPassword(@PathVariable String password1, @PathVariable String password2,
                                          @PathVariable String email){
        if (!password1.equals(password2)){
            return ResponseEntity.badRequest().body(new MessageResponse("error password"));
        }
        User user = userRepository.findByEmail(email);
        user.setPassword(encoder.encode(password1));
        userRepository.save(user);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(user.getUsername());
        loginRequest.setPassword(password1);
        return new ResponseEntity<>(loginRequest , HttpStatus.OK);
    }

}
