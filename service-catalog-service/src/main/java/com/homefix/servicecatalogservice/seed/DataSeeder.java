package com.homefix.servicecatalogservice.seed;

import com.homefix.servicecatalogservice.entity.Category;
import com.homefix.servicecatalogservice.entity.ServiceItem;
import com.homefix.servicecatalogservice.repository.CategoryRepository;
import com.homefix.servicecatalogservice.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ServiceItemRepository serviceItemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Categories already seeded, skipping...");
            return;
        }

        log.info("===== Seeding Categories and Services =====");

        // ==================== Plumbing ====================
        Category plumbing = categoryRepository.save(Category.builder()
                .name("Plumbing")
                .description("Professional plumbing services for repairs, installation, and maintenance of pipes, fixtures, and water systems.")
                .icon("wrench")
                .build());

        serviceItemRepository.saveAll(List.of(
            ServiceItem.builder().name("Faucet Repair").description("Fix leaking or malfunctioning faucets in kitchen and bathroom.").categoryId(plumbing.getId()).basePrice(new BigDecimal("80.00")).durationMinutes(60).build(),
            ServiceItem.builder().name("Drain Cleaning").description("Unclog and clean blocked drains in sinks, showers, and toilets.").categoryId(plumbing.getId()).basePrice(new BigDecimal("100.00")).durationMinutes(90).build(),
            ServiceItem.builder().name("Pipe Replacement").description("Replace old or damaged pipes to prevent leaks and water damage.").categoryId(plumbing.getId()).basePrice(new BigDecimal("200.00")).durationMinutes(180).build(),
            ServiceItem.builder().name("Water Heater Installation").description("Install new water heaters and replace old units.").categoryId(plumbing.getId()).basePrice(new BigDecimal("250.00")).durationMinutes(240).build()
        ));

        // ==================== Electrical ====================
        Category electrical = categoryRepository.save(Category.builder()
                .name("Electrical")
                .description("Expert electrical services for wiring, fixtures, troubleshooting, and safety inspections.")
                .icon("zap")
                .build());

        serviceItemRepository.saveAll(List.of(
            ServiceItem.builder().name("Light Fixture Installation").description("Install ceiling lights, chandeliers, wall sconces, and outdoor lighting.").categoryId(electrical.getId()).basePrice(new BigDecimal("60.00")).durationMinutes(45).build(),
            ServiceItem.builder().name("Outlet Repair").description("Fix or replace damaged, loose, or non-functioning electrical outlets.").categoryId(electrical.getId()).basePrice(new BigDecimal("70.00")).durationMinutes(60).build(),
            ServiceItem.builder().name("Circuit Breaker Replacement").description("Replace faulty circuit breakers and upgrade electrical panels.").categoryId(electrical.getId()).basePrice(new BigDecimal("150.00")).durationMinutes(120).build(),
            ServiceItem.builder().name("Wiring Inspection").description("Thorough inspection of home electrical wiring for safety and code compliance.").categoryId(electrical.getId()).basePrice(new BigDecimal("120.00")).durationMinutes(90).build()
        ));

        // ==================== Cleaning ====================
        Category cleaning = categoryRepository.save(Category.builder()
                .name("Cleaning")
                .description("Comprehensive cleaning services for homes and offices, from deep cleaning to regular maintenance.")
                .icon("sparkles")
                .build());

        serviceItemRepository.saveAll(List.of(
            ServiceItem.builder().name("Deep Cleaning").description("Thorough top-to-bottom cleaning of your entire home.").categoryId(cleaning.getId()).basePrice(new BigDecimal("150.00")).durationMinutes(240).build(),
            ServiceItem.builder().name("Kitchen Cleaning").description("Complete kitchen cleaning including appliances, counters, and cabinets.").categoryId(cleaning.getId()).basePrice(new BigDecimal("80.00")).durationMinutes(120).build(),
            ServiceItem.builder().name("Bathroom Scrubbing").description("Detailed bathroom cleaning, sanitizing tiles, fixtures, and glass.").categoryId(cleaning.getId()).basePrice(new BigDecimal("70.00")).durationMinutes(90).build(),
            ServiceItem.builder().name("Window Cleaning").description("Streak-free window cleaning, interior and exterior.").categoryId(cleaning.getId()).basePrice(new BigDecimal("50.00")).durationMinutes(60).build()
        ));

        // ==================== Painting ====================
        Category painting = categoryRepository.save(Category.builder()
                .name("Painting")
                .description("Interior and exterior painting services with professional-grade materials and finishes.")
                .icon("paintbrush")
                .build());

        serviceItemRepository.saveAll(List.of(
            ServiceItem.builder().name("Room Painting").description("Paint a single room with premium quality paint.").categoryId(painting.getId()).basePrice(new BigDecimal("200.00")).durationMinutes(300).build(),
            ServiceItem.builder().name("Wall Touch-Up").description("Fix scuffs, marks, and small patches on painted walls.").categoryId(painting.getId()).basePrice(new BigDecimal("60.00")).durationMinutes(60).build(),
            ServiceItem.builder().name("Ceiling Painting").description("Professional ceiling painting with clean edges and even coverage.").categoryId(painting.getId()).basePrice(new BigDecimal("180.00")).durationMinutes(240).build(),
            ServiceItem.builder().name("Exterior Painting").description("Weather-resistant exterior painting for house fronts and fences.").categoryId(painting.getId()).basePrice(new BigDecimal("500.00")).durationMinutes(480).build()
        ));

        // ==================== AC Repair ====================
        Category acRepair = categoryRepository.save(Category.builder()
                .name("AC Repair")
                .description("Air conditioning repair, maintenance, and installation services to keep you cool all year.")
                .icon("snowflake")
                .build());

        serviceItemRepository.saveAll(List.of(
            ServiceItem.builder().name("AC Tune-Up").description("Complete AC system inspection, cleaning, and performance check.").categoryId(acRepair.getId()).basePrice(new BigDecimal("90.00")).durationMinutes(60).build(),
            ServiceItem.builder().name("AC Gas Refill").description("Refill refrigerant gas to restore cooling efficiency.").categoryId(acRepair.getId()).basePrice(new BigDecimal("150.00")).durationMinutes(90).build(),
            ServiceItem.builder().name("Compressor Repair").description("Diagnose and repair faulty AC compressor units.").categoryId(acRepair.getId()).basePrice(new BigDecimal("300.00")).durationMinutes(180).build(),
            ServiceItem.builder().name("AC Installation").description("Install new split or window AC units professionally.").categoryId(acRepair.getId()).basePrice(new BigDecimal("250.00")).durationMinutes(240).build()
        ));

        // ==================== Carpenter ====================
        Category carpenter = categoryRepository.save(Category.builder()
                .name("Carpenter")
                .description("Skilled carpentry services for furniture assembly, custom builds, and woodwork repairs.")
                .icon("hammer")
                .build());

        serviceItemRepository.saveAll(List.of(
            ServiceItem.builder().name("Furniture Assembly").description("Assemble flat-pack furniture including beds, tables, shelves, and cabinets.").categoryId(carpenter.getId()).basePrice(new BigDecimal("80.00")).durationMinutes(120).build(),
            ServiceItem.builder().name("Cabinet Installation").description("Install kitchen and bathroom cabinets with proper alignment and finishing.").categoryId(carpenter.getId()).basePrice(new BigDecimal("180.00")).durationMinutes(240).build(),
            ServiceItem.builder().name("Door Repair").description("Fix sticking doors, replace hinges, and adjust door frames.").categoryId(carpenter.getId()).basePrice(new BigDecimal("70.00")).durationMinutes(60).build(),
            ServiceItem.builder().name("Custom Shelving").description("Design and build custom shelves, bookcases, and storage solutions.").categoryId(carpenter.getId()).basePrice(new BigDecimal("150.00")).durationMinutes(180).build()
        ));

        log.info("===== Seeding Complete! 6 categories and 24 services created =====");
    }
}
