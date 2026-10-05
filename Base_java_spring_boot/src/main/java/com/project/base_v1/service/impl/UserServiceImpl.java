package com.project.base_v1.service.impl;

import com.project.base_v1.config.CacheNames;
import com.project.base_v1.dto.request.user.CreateUserRequest;
import com.project.base_v1.dto.request.user.UpdateUserRequest;
import com.project.base_v1.dto.request.user.UserSearchRequest;
import com.project.base_v1.dto.response.user.UserResponse;
import com.project.base_v1.dto.response.user.UserProfileResponse;
import com.project.base_v1.dto.request.user.UpdateProfileRequest;
import com.project.base_v1.entity.User;
import com.project.base_v1.entity.UserProfile;
import com.project.base_v1.exception.BusinessException;
import com.project.base_v1.exception.ErrorCode;
import com.project.base_v1.mapper.UserMapper;
import com.project.base_v1.repository.UserRepository;
import com.project.base_v1.repository.UserProfileRepository;
import com.project.base_v1.repository.spec.UserSpecification;
import com.project.base_v1.security.CurrentUser;
import com.project.base_v1.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserProfileRepository userProfileRepository;
    com.project.base_v1.repository.StudentRegistryRepository studentRegistryRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(
            value = CacheNames.USER_SEARCH,
            key = "'keyword=' + (#request.keyword() == null ? '' : #request.keyword()) " +
                    "+ '|role=' + (#request.role() == null ? '' : #request.role()) " +
                    "+ '|status=' + (#request.status() == null ? '' : #request.status()) " +
                    "+ '|page=' + #pageable.pageNumber " +
                    "+ '|size=' + #pageable.pageSize " +
                    "+ '|sort=' + #pageable.sort.toString()"
    )
    public Page<UserResponse> searchUsers(
            UserSearchRequest request,
            Pageable pageable
    ) {
        Specification<User> spec = Specification.allOf(
                UserSpecification.hasKeyword(request.keyword()),
                UserSpecification.hasRole(request.role()),
                UserSpecification.hasStatus(request.status())
        );

        return userRepository.findAll(spec, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_DETAIL, key = "#userId"),
            @CacheEvict(value = CacheNames.USER_CURRENT, allEntries = true),
            @CacheEvict(value = CacheNames.USER_SEARCH, allEntries = true)
    })
    public void deleteUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setDeletedAt(Instant.now());
        user.setDeletedBy(CurrentUser.username());

        userRepository.save(user);
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.USER_CURRENT, allEntries = true)
    })
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(com.project.base_v1.enums.AccountStatus.ACTIVE)
                .build();

        userRepository.save(user);

        String initialName = studentRegistryRepository.findByStudentCodeIgnoreCase(user.getUsername())
                .map(com.project.base_v1.entity.StudentRegistry::getFullName)
                .orElse(user.getUsername());
        userProfileRepository.save(UserProfile.builder()
                .userId(user.getId())
                .fullName(initialName)
                .studentCode(user.getRole() == com.project.base_v1.enums.UserRole.USER ? user.getUsername() : null)
                .build());

        return userMapper.toResponse(user);
    }
    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(value = CacheNames.USER_DETAIL, key = "#id")
            },
            evict = {
                    @CacheEvict(value = CacheNames.USER_CURRENT, allEntries = true),
                    @CacheEvict(value = CacheNames.USER_SEARCH, allEntries = true)
            }
    )
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        if (request.status() != null) {
            user.setStatus(request.status());
        }

        userRepository.save(user);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse getCurrentProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UUID userId = user.getId();
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        if (profile == null || profile.getFullName() == null || profile.getFullName().isBlank()) {
            String fullName = null;
            String studentCode = null;
            String phone = null;
            String className = null;

            if (user.getStudentRegistryId() != null) {
                var studentOpt = studentRegistryRepository.findById(user.getStudentRegistryId());
                if (studentOpt.isPresent()) {
                    var s = studentOpt.get();
                    fullName = s.getFullName();
                    studentCode = s.getStudentCode();
                    phone = s.getPhone();
                    className = s.getClassName();
                }
            }

            if (fullName == null || fullName.isBlank()) {
                var studentOpt = studentRegistryRepository.findByStudentCodeIgnoreCase(user.getUsername());
                if (studentOpt.isPresent()) {
                    var s = studentOpt.get();
                    fullName = s.getFullName();
                    studentCode = s.getStudentCode();
                    phone = s.getPhone();
                    className = s.getClassName();
                }
            }

            if ((fullName == null || fullName.isBlank()) && user.getEmail() != null) {
                var studentOpt = studentRegistryRepository.findByEmailIgnoreCase(user.getEmail());
                if (studentOpt.isPresent()) {
                    var s = studentOpt.get();
                    fullName = s.getFullName();
                    studentCode = s.getStudentCode();
                    phone = s.getPhone();
                    className = s.getClassName();
                }
            }

            if (fullName == null || fullName.isBlank()) {
                if (user.getRole() == com.project.base_v1.enums.UserRole.USER) {
                    studentCode = user.getUsername();
                    fullName = "Sinh viên " + user.getUsername();
                } else if (user.getRole() == com.project.base_v1.enums.UserRole.STAFF) {
                    fullName = "Cán bộ quản lý KTX";
                } else if (user.getRole() == com.project.base_v1.enums.UserRole.ADMIN) {
                    fullName = "Quản trị viên KTX";
                } else {
                    fullName = user.getUsername();
                }
            }

            if (profile == null) {
                profile = UserProfile.builder()
                        .userId(userId)
                        .fullName(fullName)
                        .studentCode(studentCode)
                        .phone(phone)
                        .className(className)
                        .build();
            } else {
                profile.setFullName(fullName);
                if (profile.getStudentCode() == null) profile.setStudentCode(studentCode);
                if (profile.getPhone() == null) profile.setPhone(phone);
                if (profile.getClassName() == null) profile.setClassName(className);
            }
            profile = userProfileRepository.save(profile);
        }
        return toProfileResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UUID userId = user.getId();
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> UserProfile.builder().userId(userId).build());
        profile.setFullName(request.fullName().trim());
        profile.setDateOfBirth(request.dateOfBirth());
        profile.setGender(blankToNull(request.gender()));
        profile.setPhone(blankToNull(request.phone()));
        profile.setAddress(blankToNull(request.address()));
        profile.setEmergencyContactName(blankToNull(request.emergencyContactName()));
        profile.setEmergencyContactPhone(blankToNull(request.emergencyContactPhone()));
        return toProfileResponse(userProfileRepository.save(profile));
    }

    private UserProfileResponse toProfileResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getUserId(), profile.getFullName(), profile.getStudentCode(),
                profile.getDateOfBirth(), profile.getGender(), profile.getPhone(),
                profile.getIdentityNumber(), profile.getFaculty(), profile.getClassName(),
                profile.getAddress(), profile.getEmergencyContactName(), profile.getEmergencyContactPhone()
        );
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
