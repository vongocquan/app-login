package login.app;

import login.app.model.ERole;
import login.app.model.Role;
import login.app.model.User;
import login.app.repository.RoleRepository;
import login.app.repository.UserRepository;
import login.app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SpringBootApplication
public class AppApplication implements CommandLineRunner{

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("123")) {
            User user = new User();
            user.setUsername("123");
            user.setEmail("quanv4297@gmail.com");
            user.setFirtname("quan");
            user.setLastname("vo");
            user.setPhone("0345443380");
            user.setPassword(encoder.encode("123"));
            user.setImage("https://ict-imgs.vgcloud.vn/2020/09/01/19/huong-dan-tao-facebook-avatar.jpg");
            user.setReferrals("dhjasjdsa");
            Role userRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error"));
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
            userRepository.save(user);
        }


    }
}
