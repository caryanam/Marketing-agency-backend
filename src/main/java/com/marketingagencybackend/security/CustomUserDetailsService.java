package com.marketingagencybackend.security;

import com.marketingagencybackend.entity.Admin;
import com.marketingagencybackend.entity.Client;
import com.marketingagencybackend.enums.Role;
import com.marketingagencybackend.repository.AdminRepository;
import com.marketingagencybackend.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            return new CustomUserDetails(a.getAdminId() , a.getEmail(), a.getPassword(), Role.ADMIN);
        }

        Optional<Client> client = clientRepository.findByEmail(email);
        if (client.isPresent()) {
            Client c = client.get();
            return new CustomUserDetails(c.getId(), c.getEmail(), c.getPassword(), Role.CLIENT);
        }

        throw new UsernameNotFoundException("User not found");
    }
}
