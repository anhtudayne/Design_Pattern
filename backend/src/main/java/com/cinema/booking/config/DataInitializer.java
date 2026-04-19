package com.cinema.booking.config;

import com.cinema.booking.entity.Admin;
import com.cinema.booking.entity.Customer;
import com.cinema.booking.entity.Staff;
import com.cinema.booking.entity.UserAccount;
import com.cinema.booking.repository.UserAccountRepository;
import com.cinema.booking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data initializer to create default user accounts with password '123456'
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "123456";

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, 
                                      UserAccountRepository userAccountRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Only initialize if no users exist
            if (userRepository.count() > 0) {
                log.info("Users already exist in database, skipping initialization");
                return;
            }

            log.info("Initializing default user accounts with password '{}'", DEFAULT_PASSWORD);
            
            String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

            // Create Admin account
            createAdmin(userRepository, userAccountRepository, 
                    "admin@starcine.local", "System Admin", "0900000001", encodedPassword);

            // Create Staff accounts
            createStaff(userRepository, userAccountRepository,
                    "staff@starcine.local", "Counter Staff", "0900000002", encodedPassword);
            
            createStaff(userRepository, userAccountRepository,
                    "staff2@starcine.local", "Box Office Staff", "0900000003", encodedPassword);

            // Create Customer accounts
            createCustomer(userRepository, userAccountRepository,
                    "user1@starcine.local", "Nguyen Van A", "0900000004", encodedPassword);
            
            createCustomer(userRepository, userAccountRepository,
                    "user2@starcine.local", "Tran Thi B", "0900000005", encodedPassword);
            
            createCustomer(userRepository, userAccountRepository,
                    "user3@starcine.local", "Le Van C", "0900000006", encodedPassword);

            log.info("Successfully created {} default user accounts", 6);
        };
    }

    private void createAdmin(UserRepository userRepository, 
                            UserAccountRepository userAccountRepository,
                            String email, String fullname, String phone, 
                            String encodedPassword) {
        if (userAccountRepository.existsByEmail(email)) {
            log.info("Admin account {} already exists, skipping", email);
            return;
        }

        Admin admin = new Admin();
        admin.setFullname(fullname);
        admin.setPhone(phone);

        UserAccount account = UserAccount.builder()
                .email(email)
                .passwordHash(encodedPassword)
                .build();

        account.setUser(admin);
        admin.setUserAccount(account);

        userRepository.save(admin);
        log.info("Created admin account: {} ({})", email, fullname);
    }

    private void createStaff(UserRepository userRepository, 
                            UserAccountRepository userAccountRepository,
                            String email, String fullname, String phone, 
                            String encodedPassword) {
        if (userAccountRepository.existsByEmail(email)) {
            log.info("Staff account {} already exists, skipping", email);
            return;
        }

        Staff staff = new Staff();
        staff.setFullname(fullname);
        staff.setPhone(phone);

        UserAccount account = UserAccount.builder()
                .email(email)
                .passwordHash(encodedPassword)
                .build();

        account.setUser(staff);
        staff.setUserAccount(account);

        userRepository.save(staff);
        log.info("Created staff account: {} ({})", email, fullname);
    }

    private void createCustomer(UserRepository userRepository, 
                               UserAccountRepository userAccountRepository,
                               String email, String fullname, String phone, 
                               String encodedPassword) {
        if (userAccountRepository.existsByEmail(email)) {
            log.info("Customer account {} already exists, skipping", email);
            return;
        }

        Customer customer = new Customer();
        customer.setFullname(fullname);
        customer.setPhone(phone);

        UserAccount account = UserAccount.builder()
                .email(email)
                .passwordHash(encodedPassword)
                .build();

        account.setUser(customer);
        customer.setUserAccount(account);

        userRepository.save(customer);
        log.info("Created customer account: {} ({})", email, fullname);
    }
}
