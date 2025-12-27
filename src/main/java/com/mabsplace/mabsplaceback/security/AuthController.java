package com.mabsplace.mabsplaceback.security;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.mabsplace.mabsplaceback.domain.dtos.auth.OAuth2AuthRequest;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserResponseDto;
import com.mabsplace.mabsplaceback.domain.dtos.user.UserLightweightResponseDto;
import com.mabsplace.mabsplaceback.domain.entities.*;
import com.mabsplace.mabsplaceback.domain.enums.AuthenticationType;
import com.mabsplace.mabsplaceback.domain.mappers.UserMapper;
import com.mabsplace.mabsplaceback.domain.mappers.UserLightweightMapper;
import com.mabsplace.mabsplaceback.domain.repositories.CurrencyRepository;
import com.mabsplace.mabsplaceback.domain.repositories.RoleRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserProfileRepository;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.domain.services.EmailVerificationService;
import com.mabsplace.mabsplaceback.domain.services.PromoCodeService;
import com.mabsplace.mabsplaceback.domain.services.TransactionService;
import com.mabsplace.mabsplaceback.domain.services.UserService;
import com.mabsplace.mabsplaceback.exceptions.ResourceNotFoundException;
import com.mabsplace.mabsplaceback.security.events.OnRegistrationCompleteEvent;
import com.mabsplace.mabsplaceback.security.jwt.JwtUtils;
import com.mabsplace.mabsplaceback.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.mabsplace.mabsplaceback.security.request.AuthResponse;
import com.mabsplace.mabsplaceback.security.request.LoginRequest;
import com.mabsplace.mabsplaceback.security.request.SignupRequest;
import com.mabsplace.mabsplaceback.security.request.VerifyCodeRequest;
import com.mabsplace.mabsplaceback.security.response.MessageResponse;
import com.mabsplace.mabsplaceback.security.services.UserServiceSec;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${mabsplace.google.clientId}")
    private static final String CLIENT_ID = "";

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    private final UserService userService;

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

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private final UserMapper userMapper;

    private final UserLightweightMapper userLightweightMapper;

    private final PromoCodeService promoCodeService;

    private final UserProfileRepository userProfileRepository;


    public AuthController(UserService userService, UserMapper userMapper, UserLightweightMapper userLightweightMapper, PromoCodeService promoCodeService, UserProfileRepository userProfileRepository) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.userLightweightMapper = userLightweightMapper;
        this.promoCodeService = promoCodeService;
        this.userProfileRepository = userProfileRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) throws MessagingException {

        // Try normalized username first
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

        // Fallback to case-insensitive search for legacy users
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByUsernameIgnoreCase(loginRequest.getUsername());
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User doesn't exist !!!!");
        }

        User user = userOptional.get();

        // Authenticate with actual username from database
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        loginRequest.getPassword()
                )
        );

        /*if (user.getEmailVerified() != null && !user.getEmailVerified()) {
            throw new RuntimeException("User not verified");
        }*/

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.createToken(authentication);

        return ResponseEntity.ok().body(new AuthResponse(token, userMapper.toDto(user)));
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

        // Normalize username (trim + lowercase)
        String normalizedUsername = signUpRequest.getUsername().trim().toLowerCase();
        signUpRequest.setUsername(normalizedUsername);

        // Additional validation - ensure no spaces in username
        if (normalizedUsername.contains(" ")) {
            throw new BadRequestException("Username cannot contain spaces.");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Email address already in use.");
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new BadRequestException("Username already in use.");
        }

        User user = User.builder()
                .username(normalizedUsername)
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .phonenumber(signUpRequest.getPhonenumber())
                .emailVerified(true)
                .firstname(signUpRequest.getFirstname())
                .lastname(signUpRequest.getLastname())
                .authType(AuthenticationType.DATABASE)
                .build();

        // Find default user profile or create if doesn't exist
        UserProfile defaultProfile = userProfileRepository.findByName(signUpRequest.getProfileName())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setName("USER_PROFILE");
                    newProfile.setDescription("Default user profile");
                    Role userRole = roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
                    newProfile.setRoles(Collections.singleton(userRole));
                    return userProfileRepository.save(newProfile);
                });

        // Set Referrer using referral code
        if (signUpRequest.getReferralCode() != null && !signUpRequest.getReferralCode().isEmpty()) {
            User referrer = userRepository.findByReferralCode(signUpRequest.getReferralCode())
                    .orElse(null);

            if (referrer != null) {
                // Check that user isn't trying to use their own referral code
                if (referrer.getUsername().equals(signUpRequest.getUsername()) ||
                        referrer.getEmail().equals(signUpRequest.getEmail())) {
                    logger.warn("User {} attempted to use their own referral code", signUpRequest.getUsername());
                } else {
                    user.setReferrer(referrer);
                    logger.info("User {} referred by user {} with code {}", user.getUsername(),
                            referrer.getUsername(), signUpRequest.getReferralCode());
                }
            } else {
                logger.warn("Invalid referral code provided: {}", signUpRequest.getReferralCode());
            }
        }

        // Assign default profile
        user.setUserProfile(defaultProfile);

        user.setEmailVerified(true);
        user.setAuthType(AuthenticationType.DATABASE);

        User result = userRepository.save(user);

        userService.generateReferralCode(result);

        result.setWallet(
                Wallet.builder()
                        .user(result)
                        .balance(BigDecimal.ZERO)
                        .currency(currencyRepository.findAll().getFirst())
                        .build()
        );

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
    public UserResponseDto getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userMapper.toDto(userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId())));
    }

    @GetMapping("/user/me/lightweight")
//    @PreAuthorize("hasRole('USER')")
    public UserLightweightResponseDto getCurrentUserLightweight(@CurrentUser UserPrincipal userPrincipal) {
        return userLightweightMapper.toDto(userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId())));
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

    @PostMapping("/verify-new-email")
    public ResponseEntity<?> sendVerificationToNewEmail(@RequestParam String email) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Email is already registered"));
            }

            emailVerificationService.sendVerificationCodeToNewEmail(email);
            return ResponseEntity.ok()
                    .body(new MessageResponse("Verification code sent successfully"));
        } catch (MessagingException e) {
            logger.error("Error sending verification code to email: {}", email, e);
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("Error sending verification code"));
        }
    }

    @PostMapping("/verify-new-email/check")
    public ResponseEntity<?> verifyNewEmail(@RequestBody VerifyCodeRequest request) {
        if (emailVerificationService.verifyCode(request.getEmail(), request.getCode())) {
            return ResponseEntity.ok()
                    .body(new MessageResponse("Email verified successfully"));
        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse("Invalid verification code"));
    }

    @GetMapping("/sendVerificationCode/{email}")
    public ResponseEntity<?> sendVerificationCode(@PathVariable("email") String email) throws MessagingException {
        emailVerificationService.sendVerificationCode(email);
        return ResponseEntity.ok().body(new MessageResponse("Verification code sent successfully."));
    }

    @GetMapping("/sendVerificationCode2/{username}")
    public ResponseEntity<?> sendVerificationCode2(@PathVariable("username") String username) throws MessagingException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        String email = user.getEmail();
        emailVerificationService.sendVerificationCode(email);
        return ResponseEntity.ok().body(new MessageResponse(email));
    }

    @GetMapping("/verifyCode/{email}/{code}")
    public ResponseEntity<?> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        if (emailVerificationService.verifyCode(email, code))
            return ResponseEntity.ok().body(new MessageResponse("User verified successfully."));
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponse("Invalid verification code."));
    }

    @PostMapping("/oauth2/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody OAuth2AuthRequest authRequest) {
        try {
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            // Configurer le transport HTTP
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory)
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(authRequest.getToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                // Check if user exists
                Optional<User> userOptional = userRepository.findByEmail(payload.getEmail());
                User user;

                // generate random phone number
                String phoneNumber = String.valueOf((int) (Math.random() * 1000000000));

                if (userOptional.isEmpty()) {
                    // Find or create default user profile
                    UserProfile defaultProfile = userProfileRepository.findByName("USER_PROFILE")
                            .orElseGet(() -> {
                                UserProfile newProfile = new UserProfile();
                                newProfile.setName("USER_PROFILE");
                                newProfile.setDescription("Default user profile");
                                Role userRole = roleRepository.findByName("ROLE_USER")
                                        .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
                                newProfile.setRoles(Collections.singleton(userRole));
                                return userProfileRepository.save(newProfile);
                            });

                    // Create new user
                    user = User.builder()
                            .email(payload.getEmail())
                            .emailVerified(true)
                            .password(encoder.encode("password"))
                            .phonenumber(phoneNumber)
                            .username(payload.getEmail().trim().toLowerCase())
                            .firstname((String) payload.get("given_name"))
                            .lastname((String) payload.get("family_name"))
                            .authType(AuthenticationType.GOOGLE)
                            .userProfile(defaultProfile)
                            .build();


                    user = userRepository.save(user);

                    // Initialize wallet
                    user.setWallet(
                            Wallet.builder()
                                    .user(user)
                                    .balance(BigDecimal.ZERO)
                                    .currency(currencyRepository.findAll().getFirst())
                                    .build()
                    );

                    user = userRepository.save(user);
                } else {
                    user = userOptional.get();
                }

                String token = tokenProvider.createToken(user.getEmail());
                return ResponseEntity.ok(new AuthResponse(token, userMapper.toDto(user)));
            }

            return ResponseEntity.badRequest().body(new MessageResponse("Invalid ID token."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Authentication failed."));
        }
    }

    @PostMapping("/oauth2/apple")
    public ResponseEntity<?> authenticateApple(@RequestBody OAuth2AuthRequest authRequest) {
        try {
            // Verify Apple ID token
            SignedJWT signedJWT = SignedJWT.parse(authRequest.getToken());
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            String email = claims.getSubject();

            // Check if user exists
            Optional<User> userOptional = userRepository.findByEmail(email);
            User user;

            if (userOptional.isEmpty()) {
                // Find or create default user profile
                UserProfile defaultProfile = userProfileRepository.findByName("USER_PROFILE")
                        .orElseGet(() -> {
                            UserProfile newProfile = new UserProfile();
                            newProfile.setName("USER_PROFILE");
                            newProfile.setDescription("Default user profile");
                            Role userRole = roleRepository.findByName("ROLE_USER")
                                    .orElseThrow(() -> new RuntimeException("Error: Default role not found."));
                            newProfile.setRoles(Collections.singleton(userRole));
                            return userProfileRepository.save(newProfile);
                        });

                // Create new user
                user = User.builder()
                        .email(email)
                        .emailVerified(true)
                        .password(encoder.encode("password"))
                        .phonenumber(String.valueOf((int) (Math.random() * 1000000000)))
                        .username(email.trim().toLowerCase())
                        .firstname((String) claims.getClaim("first_name"))
                        .lastname((String) claims.getClaim("last_name"))
                        .authType(AuthenticationType.APPLE)
                        .userProfile(defaultProfile)
                        .build();

                user = userRepository.save(user);

                // Initialize wallet
                user.setWallet(
                        Wallet.builder()
                                .user(user)
                                .balance(BigDecimal.ZERO)
                                .currency(currencyRepository.findAll().getFirst())
                                .build()
                );

                user = userRepository.save(user);
            } else {
                user = userOptional.get();
            }

            String token = tokenProvider.createToken(user.getEmail());
            return ResponseEntity.ok(new AuthResponse(token, userMapper.toDto(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Authentication failed."));
        }
    }

    @PostMapping("/renew-token")
    public ResponseEntity<?> renewToken(Authentication authentication) {
        logger.info("Token renewal requested for user: {}", authentication.getName());
        
        // Get current authenticated user
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", authentication.getName()));
        
        // Generate new token
        String newToken = tokenProvider.createToken(authentication);
        
        logger.info("Token renewed successfully for user: {}", authentication.getName());
        return ResponseEntity.ok().body(new AuthResponse(newToken, userMapper.toDto(user)));
    }

}
