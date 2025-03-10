package com.flashdash.activity.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FrnGeneratorTest {

    private static final String FRN_PREFIX = "frn:flashdash";
    private static final Pattern BASE62_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");

    @Test
    void shouldGenerateValidFrnForEachResourceType() {
        for (ResourceType resourceType : ResourceType.values()) {
            // Act
            String generatedFrn = FrnGenerator.generateFrn(resourceType);

            // Assert
            assertThat(generatedFrn).isNotNull();
            assertThat(generatedFrn).startsWith(FRN_PREFIX + ":" + resourceType.getType());

            String[] parts = generatedFrn.split(":");
            assertThat(parts).hasSize(4);
            assertThat(BASE62_PATTERN.matcher(parts[3]).matches()).isTrue();
        }
    }

    @RepeatedTest(10)
    void shouldGenerateUniqueFrns() {
        // Arrange
        Set<String> generatedFrns = new HashSet<>();

        for (ResourceType resourceType : ResourceType.values()) {
            // Act
            String newFrn = FrnGenerator.generateFrn(resourceType);

            // Assert
            assertThat(generatedFrns).doesNotContain(newFrn);
            generatedFrns.add(newFrn);
        }
    }
}
