package org.cuke.inspector;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

class ViolationFormatterTest {

    @Test
    void shouldFormatViolationWithSingleLocation() throws IOException {
        CukeViolation cukeViolation = new CukeViolation() {
            @Override
            public String message() {
                return "some message";
            }

            @Override
            public FeatureLocation featureLocation() {
                return new FeatureLocation("file.txt", "token", 1L, Optional.empty());
            }
        };

        String formatted = ViolationFormatter.format(List.of(cukeViolation));

        Assertions.assertThat(formatted.trim())
                .startsWith("file.txt")
                .endsWith("some message");
    }

}