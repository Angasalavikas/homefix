package com.homefix.providerservice.repository;

import com.homefix.providerservice.entity.ProviderSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProviderSkillRepository extends JpaRepository<ProviderSkill, Long> {
}
