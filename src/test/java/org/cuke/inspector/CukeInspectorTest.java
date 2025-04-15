package org.cuke.inspector;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class CukeInspectorTest {

    public static final String TAG_REGEX = "@\\d{6}";

    @Nested
    class CommonTests {
        @Test
        void newlyCreatedInspectorShouldNotHaveViolations() throws IOException {
            String source = "";
            CukeInspector cukeInspector = CukeInspector
                    .withFeatureFile("classpath:com/example.feature", new ByteArrayInputStream(source.getBytes()))
                    .should();

            Assertions.assertThatNoException().isThrownBy(() -> cukeInspector.hasNoViolations());
        }

        @Test
        void shouldThrowAssertion() throws IOException {
            String source = """
                    Feature: Simple feature
                      Scenario: scenario to test
                        Then something is done
                      Scenario: scenario to test
                        Then something else is done
                    """;
            CukeInspector cukeInspector = CukeInspector
                    .withFeatureFile("classpath:com/example.feature", new ByteArrayInputStream(source.getBytes()))
                    .should();

            try {
                cukeInspector.hasNoViolations();
                fail("Expected AssertionError to be thrown");
            } catch (AssertionError e) {
            }
        }
    }

    @Nested
    class InvalidTagCombinations {
        @Test
        void canCheckSingleFile() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_without_tags.feature"))
                    .should()
                    .checkInvalidTagCombinations(Set.of())
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void canCheckDirectoryWithFeatureFiles() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureDirectory(Paths.get("src/test/resources/subdir"))
                    .should()
                    .checkInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .getViolations();

            assertThat(violations).hasSize(3);
            System.out.println(violations);
        }

        @Test
        void shouldFindInvalidFeatureTags() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_with_tags.feature"))
                    .should()
                    .checkInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .getViolations();

            assertThat(violations.get(0).message()).startsWith("Feature");
            System.out.println(violations);
        }

        @Test
        void shouldFindInvalidScenarioTags() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_with_scenario_tags.feature"))
                    .should()
                    .checkInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .getViolations();

            assertThat(violations.get(0).message()).startsWith("Scenario");
            System.out.println(violations);
        }

        @Test
        void shouldFindInvalidMixtureOfFeatureAndScenarioTags() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_with_mixed_tags.feature"))
                    .should()
                    .checkInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .getViolations();

            assertThat(violations.get(0).message()).startsWith("Scenario");
            System.out.println(violations);
        }
    }

    @Nested
    class InvalidStepKeywords {
        @Test
        void shouldNotFindViolationsIfNoKeywordIsProvided() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_language_en.feature"))
                    .should()
                    .checkInvalidInvalidKeywords(List.of())
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldNotFindViolationsIfKeywordDoesNotExist() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_language_en.feature"))
                    .should()
                    .checkInvalidInvalidKeywords(List.of("Happy"))
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldFindViolationsIfKeywordDoesExist() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_language_en.feature"))
                    .should()
                    .checkInvalidInvalidKeywords(List.of("But"))
                    .getViolations();

            assertThat(violations).isNotEmpty();
            assertThat(violations.get(0).message()).startsWith("Step");
        }

        @Test
        void shouldFindMultipleViolations() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_language_de.feature"))
                    .should()
                    .checkInvalidInvalidKeywords(List.of("Gegeben sei", "Aber"))
                    .getViolations();

            assertThat(violations).hasSize(2);
            assertThat(violations.get(0).message()).startsWith("Step").contains("Gegeben sei");
            assertThat(violations.get(1).message()).startsWith("Step").contains("Aber");
        }

    }

    @Nested
    class MissingTags {
        @Test
        void shouldFindStepsWithoutUserStoryTag() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_without_tags.feature"))
                    .should()
                    .findScenariosMissingRequiredTags(TAG_REGEX)
                    .getViolations();

            assertThat(violations).hasSize(1);
            assertThat(violations.get(0).message()).startsWith("Scenario");
        }
    }

    @Nested
    class ForbiddenTags {
        @Test
        void shouldFindForbiddenFeatureTag() throws IOException {
            String source = """
                    @123456
                    Feature: Simple feature
                      Scenario: scenario to test
                        Then something is done
                    """;

            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile("classpath:com/example.feature", new ByteArrayInputStream(source.getBytes()))
                    .should()
                    .findFeaturesWithDisallowedTags(TAG_REGEX)
                    .getViolations();

            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    class DuplicatedScenarios {
        @Test
        void shouldNotFindDuplicatedScenarios() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/feature_without_tags.feature"))
                    .should()
                    .findDuplicateScenarioNames()
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldFindDuplicatedScenariosInSameFile() throws IOException {
            String source = """
                    Feature: Simple feature
                      Scenario: scenario to test
                        Then withparam is done
                      Scenario: scenario to test
                        Then something else is done
                    """;

            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile("classpath:com/example.feature", new ByteArrayInputStream(source.getBytes()))
                    .should()
                    .findDuplicateScenarioNames()
                    .getViolations();

            assertThat(violations).isNotEmpty();
        }

        @Test
        void shouldFindDuplicatedScenariosInDifferentFiles() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureDirectory(Paths.get("src/test/resources/duplicates"))
                    .should()
                    .findDuplicateScenarioNames()
                    .getViolations();

            assertThat(violations).hasSize(1);
        }

    }

    @Nested
    class DuplicatedStepExpressions {
        @Test
        void shouldFindDuplicatedStepExpressions() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withJavaPackage("org.cuke.inspector.steps")
                    .should()
                    .findDuplicateStepDefinitions()
                    .getViolations();

            assertThat(violations).hasSize(1);
            assertThat(violations.get(0).message()).endsWith("3 times.");

            System.out.println(ViolationFormatter.format(violations));
        }
    }

    @Nested
    class UnusedStepDefinitions {
        @Test
        void shouldNotFindAnyUnusedStepDefinitions() throws IOException {
            String source = """
                    Feature: Simple feature
                      Scenario: scenario to test
                        Then test
                    """;

            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile("classpath:com/example.feature", new ByteArrayInputStream(source.getBytes()))
                    .withJavaPackage("org.cuke.inspector.steps.noparam")
                    .should()
                    .findUnusedStepDefinitions()
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldFindUnusedStepDefinition() throws IOException {
            String source = """
                    Feature: Simple feature
                      Scenario: scenario to test
                        Then no match step 
                    """;

            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile("classpath:com/example.feature", new ByteArrayInputStream(source.getBytes()))
                    .withJavaPackage("org.cuke.inspector.steps.noparam")
                    .should()
                    .findUnusedStepDefinitions()
                    .getViolations();

            assertThat(violations).hasSize(1);
        }
        @Test
        void shouldNotFindAnyUnusedStepDefinitionsWithParam() throws IOException {
            String source = """
                    Feature: Simple feature
                      Scenario: scenario to test
                        Then test "me"
                    """;

            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile("classpath:com/example.feature", new ByteArrayInputStream(source.getBytes()))
                    .withJavaPackage("org.cuke.inspector.steps.withparam")
                    .should()
                    .findUnusedStepDefinitions()
                    .getViolations();

            assertThat(violations).isEmpty();
        }


    }
}