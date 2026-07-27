package com.homefix.providerservice.service;

import com.homefix.providerservice.dto.*;
import com.homefix.providerservice.entity.Provider;
import com.homefix.providerservice.entity.ProviderSkill;
import com.homefix.providerservice.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;

    @Transactional
    public ProviderResponse registerProfile(Long userId, ProviderRegistrationRequest request) {
        if (providerRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("Provider profile already exists for this user");
        }

        Provider provider = Provider.builder()
                .userId(userId)
                .name(request.getName())
                .experienceYears(request.getExperienceYears())
                .build();

        if (request.getSkills() != null) {
            final Provider finalProvider = provider;
            List<ProviderSkill> skillEntities = request.getSkills().stream()
                    .map(skillName -> ProviderSkill.builder()
                            .provider(finalProvider)
                            .skillName(skillName)
                            .build())
                    .collect(Collectors.toList());
            provider.setSkills(skillEntities);
        }

        Provider savedProvider = providerRepository.save(provider);
        return ProviderResponse.fromProvider(savedProvider);
    }

    public ProviderResponse getMyProfile(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found. Please register first."));
        return ProviderResponse.fromProvider(provider);
    }

    public ProviderResponse getProviderById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        return ProviderResponse.fromProvider(provider);
    }

    @Transactional
    public ProviderResponse updateAvailability(Long userId, AvailabilityUpdateRequest request) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Provider profile not found."));
        provider.setAvailability(request.getAvailability());
        provider = providerRepository.save(provider);
        return ProviderResponse.fromProvider(provider);
    }
}
