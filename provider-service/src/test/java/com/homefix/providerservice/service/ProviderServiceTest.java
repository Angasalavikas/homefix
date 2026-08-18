package com.homefix.providerservice.service;

import com.homefix.providerservice.dto.AvailabilityUpdateRequest;
import com.homefix.providerservice.dto.ProviderRegistrationRequest;
import com.homefix.providerservice.dto.ProviderResponse;
import com.homefix.providerservice.entity.AvailabilityStatus;
import com.homefix.providerservice.entity.Provider;
import com.homefix.providerservice.entity.ProviderSkill;
import com.homefix.providerservice.entity.VerificationStatus;
import com.homefix.providerservice.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    private ProviderService providerService;

    @BeforeEach
    void setUp() {
        providerService = new ProviderService(providerRepository);
    }

    @Test
    void registerProfile_ShouldCreateNewProvider() {
        Long userId = 1L;
        ProviderRegistrationRequest request = ProviderRegistrationRequest.builder()
                .name("Jane Plumber")
                .experienceYears(5)
                .skills(List.of("Pipe Repair", "Drain Cleaning"))
                .build();

        when(providerRepository.existsByUserId(userId)).thenReturn(false);
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> {
            Provider saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setCreatedAt(java.time.LocalDateTime.now());
            saved.setUpdatedAt(java.time.LocalDateTime.now());
            return saved;
        });

        ProviderResponse response = providerService.registerProfile(userId, request);

        assertThat(response.getName()).isEqualTo("Jane Plumber");
        assertThat(response.getExperienceYears()).isEqualTo(5);
        assertThat(response.getSkills()).containsExactly("Pipe Repair", "Drain Cleaning");
        assertThat(response.getAvailability()).isEqualTo(AvailabilityStatus.AVAILABLE);
        assertThat(response.getVerificationStatus()).isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void registerProfile_ShouldThrowException_WhenDuplicateUser() {
        when(providerRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> providerService.registerProfile(1L, new ProviderRegistrationRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void registerProfile_ShouldCreateProviderWithoutSkills() {
        Long userId = 1L;
        ProviderRegistrationRequest request = ProviderRegistrationRequest.builder()
                .name("No Skills Jane")
                .experienceYears(3)
                .build();

        when(providerRepository.existsByUserId(userId)).thenReturn(false);
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> {
            Provider saved = invocation.getArgument(0);
            saved.setId(101L);
            saved.setCreatedAt(java.time.LocalDateTime.now());
            saved.setUpdatedAt(java.time.LocalDateTime.now());
            return saved;
        });

        ProviderResponse response = providerService.registerProfile(userId, request);

        assertThat(response.getName()).isEqualTo("No Skills Jane");
        assertThat(response.getSkills()).isEmpty();
    }

    @Test
    void getMyProfile_ShouldReturnProvider_WhenFound() {
        Provider provider = createTestProvider(1L, 100L, "Jane");

        when(providerRepository.findByUserId(1L)).thenReturn(Optional.of(provider));

        ProviderResponse response = providerService.getMyProfile(1L);

        assertThat(response.getName()).isEqualTo("Jane");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void getMyProfile_ShouldThrowException_WhenNotFound() {
        when(providerRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.getMyProfile(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getProviderById_ShouldReturnProvider_WhenFound() {
        Provider provider = createTestProvider(1L, 100L, "Jane");

        when(providerRepository.findById(100L)).thenReturn(Optional.of(provider));

        ProviderResponse response = providerService.getProviderById(100L);

        assertThat(response.getName()).isEqualTo("Jane");
        assertThat(response.getId()).isEqualTo(100L);
    }

    @Test
    void getProviderById_ShouldThrowException_WhenNotFound() {
        when(providerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.getProviderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateAvailability_ShouldUpdateStatus() {
        Provider provider = createTestProvider(1L, 100L, "Jane");
        provider.setAvailability(AvailabilityStatus.AVAILABLE);

        AvailabilityUpdateRequest request = AvailabilityUpdateRequest.builder()
                .availability(AvailabilityStatus.BUSY)
                .build();

        when(providerRepository.findByUserId(1L)).thenReturn(Optional.of(provider));
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProviderResponse response = providerService.updateAvailability(1L, request);

        assertThat(response.getAvailability()).isEqualTo(AvailabilityStatus.BUSY);
    }

    private Provider createTestProvider(Long userId, Long id, String name) {
        Provider provider = Provider.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .experienceYears(5)
                .skills(new ArrayList<>())
                .availability(AvailabilityStatus.AVAILABLE)
                .verificationStatus(VerificationStatus.PENDING)
                .build();
        provider.setCreatedAt(java.time.LocalDateTime.now());
        provider.setUpdatedAt(java.time.LocalDateTime.now());
        return provider;
    }
}
