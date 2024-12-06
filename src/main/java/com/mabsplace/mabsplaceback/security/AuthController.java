package com.mabsplace.mabsplaceback.security;


import com.mabsplace.mabsplaceback.domain.entities.Role;
import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.entities.VerificationToken;
import com.mabsplace.mabsplaceback.domain.entities.Wallet;
import com.mabsplace.mabsplaceback.domain.enums.AuthenticationType;
import com.mabsplace.mabsplaceback.domain.mappers.UserMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.RoleRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.domain.services.EmailVerificationService;
import com.mabsplace.mabsplaceback.domain.services.PromoCodeService;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.security.events.OnRegistrationCompleteEvent;
import com.mabsplace.mabsplaceback.security.jwt.JwtUtils;
import com.mabsplace.mabsplaceback.security.request.AuthResponse;
import com.mabsplace.mabsplaceback.security.request.LoginRequest;
import com.mabsplace.mabsplaceback.security.request.SignupRequest;
import com.mabsplace.mabsplaceback.security.request.VerifyCodeRequest;
import com.mabsplace.mabsplaceback.security.response.MessageResponse;
import com.mabsplace.mabsplaceback.security.services.UserServiceSec;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    HttpServletRequest request;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    UserServiceSec service;

    private final UserMapper userMapper;

    private final PromoCodeService promoCodeService;

    public AuthController(UserMapper userMapper, PromoCodeService promoCodeService) {
        this.userMapper = userMapper;
        this.promoCodeService = promoCodeService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws MessagingException {

//    Optional<User> userOptional = userRepository.findByPhoneNumber(loginRequest.getPhoneNumber());
        userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() -> new RuntimeException("User doesn't exist !!!!"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        Optional<User> loggedIn = userRepository.findByUsername(loginRequest.getUsername());

        if (loggedIn.isPresent() && !loggedIn.get().getEmailVerified()) {
            throw new RuntimeException("User not verified");
        }

        emailVerificationService.sendVerificationCode(loggedIn.get().getEmail());


        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);

        return ResponseEntity.ok().body(new AuthResponse(token, userMapper.toDto(loggedIn.get())));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest verifyCodeRequest) {
        String userEmail = verifyCodeRequest.getEmail();
        String userEnteredCode = verifyCodeRequest.getCode();

        if (emailVerificationService.verifyCode(userEmail, userEnteredCode))
            return ResponseEntity.ok().body(new MessageResponse("User verified successfully."));

        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid verification code."));

    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) throws BadRequestException {

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Username already in use.");
        }

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .phonenumber(signUpRequest.getPhonenumber())
                .emailVerified(true)
                .firstname(signUpRequest.getFirstname())
                .lastname(signUpRequest.getLastname())
                .build();

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            throw new BadRequestException("Role is not found.");
        } else {
            strRoles.forEach(role -> {
                Role adminRole = roleRepository.findByName(role)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(adminRole);
            });
        }

        user.setRoles(roles);
        user.setEmailVerified(true);
        user.setAuthType(AuthenticationType.DATABASE);

        User result = userRepository.save(user);

        result.setWallet(
                Wallet.builder()
                        .user(result)
                        .balance(BigDecimal.ZERO)
                        .currency(currencyRepository.findAll().getFirst())
                        .build()
        );

        promoCodeService.generatePromoCode(result);

        if (signUpRequest.getPromoCode() != null && !signUpRequest.getPromoCode().isEmpty())
            promoCodeService.registerUserWithPromoCode(signUpRequest.getPromoCode(), result);

        result = userRepository.save(result);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(result.getId()).toUri();

        String appUrl = request.getContextPath();

        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user,
                request.getLocale(), appUrl));


        return ResponseEntity.created(location)
                .body(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }

    @GetMapping("/user/me")
//    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    @GetMapping("/registrationConfirm")
    public boolean confirmRegistration
            (@RequestParam("token") String token) throws Exception {

        Locale locale = request.getLocale();

        VerificationToken verificationToken = service.getVerificationToken(token);
        if (verificationToken == null) {
            return false;
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return false;
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        return true;
    }

    @GetMapping("/sendVerificationCode/{email}")
    public ResponseEntity<?> sendVerificationCode(@PathVariable("email") String email) throws MessagingException {
        emailVerificationService.sendVerificationCode(email);
        return ResponseEntity.ok().body(new MessageResponse("Verification code sent successfully."));
    }

    @GetMapping("/verifyCode/{email}/{code}")
    public ResponseEntity<?> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        if (emailVerificationService.verifyCode(email, code))
            return ResponseEntity.ok().body(new MessageResponse("User verified successfully."));
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid verification code."));
    }


}
