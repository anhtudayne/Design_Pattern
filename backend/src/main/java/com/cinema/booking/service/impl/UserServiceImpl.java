package com.cinema.booking.service.impl;

import com.cinema.booking.dto.UserDTO;
import com.cinema.booking.dto.request.UserUpdateRequest;
import com.cinema.booking.entity.User;
import com.cinema.booking.repository.UserRepository;
import com.cinema.booking.security.UserDetailsImpl;
import com.cinema.booking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private Integer getCurrentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        Integer userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        return toDto(user);
    }

    @Override
    @Transactional
    public UserDTO updateProfile(UserUpdateRequest request) {
        Integer userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (request.getFullname() != null) {
            user.setFullname(request.getFullname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user = userRepository.save(user);
        return toDto(user);
    }

    private static UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setFullname(user.getFullname());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getUserAccount() != null ? user.getUserAccount().getEmail() : null);
        return dto;
    }
}
