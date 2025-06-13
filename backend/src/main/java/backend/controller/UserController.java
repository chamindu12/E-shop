package backend.controller;

import backend.exception.UserNotFoundException;
import backend.model.UserModel;
import backend.repository.UserRepository;
import backend.service.CaptchaService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(
        origins = {"http://localhost:3000", "http://localhost:3001"},
        allowCredentials = "true",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CaptchaService captchaService;

    // User Registration - Hash password
    @PostMapping("/user")
    public ResponseEntity<?> newUserModel(@Valid @RequestBody UserModel newUserModel) {
        logger.info("Registering user: {}", newUserModel.getEmail());
        String hashedPassword = passwordEncoder.encode(newUserModel.getPassword());
        newUserModel.setPassword(hashedPassword);
        UserModel savedUser = userRepository.save(newUserModel);
        logger.info("User registered successfully: {}", savedUser.getId());
        return ResponseEntity.ok(savedUser);
    }


    // User login - Check hashed password
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> loginRequest, HttpSession session) {
        String email = (String) loginRequest.get("email");
        String password = (String) loginRequest.get("password");
        String captchaToken = (String) loginRequest.get("captchaToken");

        logger.info("Login attempt for email: {}", email);

        if (!captchaService.verifyCaptcha(captchaToken)) {
            logger.warn("Captcha verification failed for email: {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Captcha verification failed"));
        }

        try {
            UserModel user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Invalid email: " + email));

            if (passwordEncoder.matches(password, user.getPassword())) {
                session.setAttribute("user", user);
                logger.info("User logged in successfully: {}", email);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("id", user.getId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Wrong password attempt for email: {}", email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Wrong password!"));
            }
        } catch (Exception e) {
            logger.error("Login failed for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred during login"));
        }
    }

    // Display all users
    @GetMapping("/user")
    List<UserModel> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll();
    }

    // Get user by ID
    @GetMapping("/user/{id}")
    UserModel getUserById(@PathVariable long id) {
        logger.info("Fetching user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", id);
                    return new UserNotFoundException("Invalid id : " + id);
                });
    }

    // Update profile - hash new password
    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserModel newUserModel, @PathVariable long id) {
        logger.info("Updating profile for user ID: {}", id);
        UserModel updatedUser = userRepository.findById(id)
                .map(userModel -> {
                    userModel.setFullName(newUserModel.getFullName());
                    userModel.setEmail(newUserModel.getEmail());
                    userModel.setPhone(newUserModel.getPhone());

                    if (!newUserModel.getPassword().equals(userModel.getPassword())) {
                        String hashedPassword = passwordEncoder.encode(newUserModel.getPassword());
                        userModel.setPassword(hashedPassword);
                        logger.debug("Password updated for user ID: {}", id);
                    }

                    return userRepository.save(userModel);
                })
                .orElseThrow(() -> {
                    logger.warn("User not found for update: {}", id);
                    return new UserNotFoundException("Invalid id : " + id);
                });

        return ResponseEntity.ok(updatedUser);
    }


    // Delete user
    @DeleteMapping("/user/{id}")
    String deleteProfile(@PathVariable long id) {
        logger.info("Attempting to delete user ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("User not found for deletion: {}", id);
            throw new UserNotFoundException("Invalid id : " + id);
        }
        userRepository.deleteById(id);
        logger.info("User deleted successfully: {}", id);
        return "User deleted successfully";
    }

    // Check if email exists
    @GetMapping("/checkEmail")
    public boolean checkEmailExists(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        logger.info("Checking if email exists: {} -> {}", email, exists);
        return exists;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getLoggedInUser(HttpSession session) {
        UserModel user = (UserModel) session.getAttribute("user");
        if (user != null) {
            logger.info("User session found for ID: {}", user.getId());
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "email", user.getEmail(),
                    "fullName", user.getFullName()
            ));
        } else {
            logger.warn("No user in session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        logger.info("Logging out user");
        session.invalidate();
        return ResponseEntity.ok("Logged out");
    }
}
