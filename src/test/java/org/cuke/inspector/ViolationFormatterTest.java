package org.cuke.inspector;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ViolationFormatterTest {
    CukeViolation cukeViolation = new CukeViolation() {
        @Override
        public String message() {
            return "some message";
        }

        @Override
        public FeatureLocation featureLocation() {
            return new FeatureLocation("file.txt", "token", 1L, 0L);
        }
    };

    @Test
    void shouldContainFileAndMessage() {
        String formatted = ViolationFormatter.format(List.of(cukeViolation));

        Assertions.assertThat(formatted.trim())
                .startsWith("file.txt")
                .endsWith("some message");
    }

    @Test
    void shouldContainPositions() {
        String formatted = ViolationFormatter.format(List.of(cukeViolation));

        Assertions.assertThat(formatted).contains("[1,0]");
    }

}