package com.lms.identity.config;

import com.lms.identity.entity.Role;
import com.lms.identity.entity.RoleName;
import com.lms.identity.entity.User;
import com.lms.identity.entity.UserStatus;
import com.lms.identity.repository.RoleRepository;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeRoles();
        initializeAdminUser();
        initializeTeacherUser();
        initializeTeacherManagerUser();
    }

    private void initializeRoles() {
        // Tạo ROLE_ADMIN
        if (roleRepository.findByName(RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = Role.builder()
                    .name(RoleName.ROLE_ADMIN)
                    .build();
            adminRole.setId("01JFZC5Y3K1M7X9C6T2B4N8PQ");
            adminRole.onCreate();
            roleRepository.save(adminRole);
            log.info("Created ROLE_ADMIN");
        }

        // Tạo ROLE_USER
        if (roleRepository.findByName(RoleName.ROLE_USER).isEmpty()) {
            Role userRole = Role.builder()
                    .name(RoleName.ROLE_USER)
                    .build();
            userRole.setId("01JFZC5Y3K1M7X9C6T2B4N8PR");
            userRole.onCreate();
            roleRepository.save(userRole);
            log.info("Created ROLE_USER");
        }
        if (roleRepository.findByName(RoleName.ROLE_TEACHER).isEmpty()) {
            Role teacherRole = Role.builder()
                    .name(RoleName.ROLE_TEACHER)
                    .build();
            teacherRole.setId("01JFZC5Y3K1M7X9C6T2B4N8PT");
            teacherRole.onCreate();
            roleRepository.save(teacherRole);
            log.info("Created ROLE_TEACHER");
        }

        // ROLE_TEACHER_MANAGER
        if (roleRepository.findByName(RoleName.ROLE_TEACHER_MANAGER).isEmpty()) {
            Role teacherManagerRole = Role.builder()
                    .name(RoleName.ROLE_TEACHER_MANAGER)
                    .build();
            teacherManagerRole.setId("01JFZC5Y3K1M7X9C6T2B4N8PU");
            teacherManagerRole.onCreate();
            roleRepository.save(teacherManagerRole);
            log.info("Created ROLE_TEACHER_MANAGER");
        }
    }

    private void initializeAdminUser() {
        if (userRepository.findByEmail("admin@local.dev").isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .email("admin@local.dev")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Admin")
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .roles(roles)
                    .build();
            admin.setId("01JFZC5Y3K1M7X9C6T2B4N8PS");
            admin.onCreate();
            userRepository.save(admin);
            log.info("Created admin user: admin@local.dev / admin123");
        }
    }

    private void initializeTeacherUser() {
        if (userRepository.findByEmail("teacher@local.dev").isEmpty()) {
            Role teacherRole = roleRepository.findByName(RoleName.ROLE_TEACHER)
                    .orElseThrow(() -> new RuntimeException("ROLE_TEACHER not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(teacherRole);

            User teacher = User.builder()
                    .email("teacher@local.dev")
                    .password(passwordEncoder.encode("teacher123"))
                    .fullName("Teacher Demo")
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .roles(roles)
                    .build();
            teacher.setId("01JFZC5Y3K1M7X9C6T2B4N8PV");
            teacher.onCreate();
            userRepository.save(teacher);
            log.info("Created teacher user: teacher@local.dev / teacher123");
        }
    }

    private void initializeTeacherManagerUser() {
        if (userRepository.findByEmail("manager@local.dev").isEmpty()) {
            Role teacherManagerRole = roleRepository.findByName(RoleName.ROLE_TEACHER_MANAGER)
                    .orElseThrow(() -> new RuntimeException("ROLE_TEACHER_MANAGER not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(teacherManagerRole);

            User manager = User.builder()
                    .email("manager@local.dev")
                    .password(passwordEncoder.encode("manager123"))
                    .fullName("Teacher Manager Demo")
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .roles(roles)
                    .build();
            manager.setId("01JFZC5Y3K1M7X9C6T2B4N8PW");
            manager.onCreate();
            userRepository.save(manager);
            log.info("Created teacher manager user: manager@local.dev / manager123");
        }
    }
}
