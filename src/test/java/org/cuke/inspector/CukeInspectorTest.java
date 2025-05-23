package org.cuke.inspector;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class CukeInspectorTest {

    public static final String USERSTORY_TAG = "@\\d{6}";

    @Nested
    class CommonTests {
        @Test
        void newlyCreatedInspectorShouldNotHaveViolations() throws IOException {
            CukeInspector cukeInspector = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/some_feature.feature"))
                    .should();

            assertThat(cukeInspector.getViolations()).isEmpty();
        }

        @Test
        void newlyCreatedInspectorShouldNotThrowAssertion() throws IOException {
            CukeInspector cukeInspector = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/some_feature.feature"))
                    .should();

            assertThatNoException().isThrownBy(cukeInspector::hasNoViolations);
        }

    }

    @Nested
    class InvalidTagCombinations {

        public static final String INVALID_TAG_COMBINATIONS_DIRECTORY = "src/test/resources/invalidtagcombinations/";

        @Test
        void shouldNotFindViolationsIfNotTagCombinationIsProvided() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get(INVALID_TAG_COMBINATIONS_DIRECTORY + "feature_A.feature"))
                    .should()
                    .findInvalidTagCombinations(Set.of())
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldFindAllViolationsWithTwoTagsCombinationInDirectory() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureDirectory(Paths.get(INVALID_TAG_COMBINATIONS_DIRECTORY))
                    .should()
                    .findInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .getViolations();

            assertThat(violations).hasSize(3);
            System.out.println(violations);
        }

        @Test
        void shouldFindInvalidFeatureTags() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get(INVALID_TAG_COMBINATIONS_DIRECTORY + "feature_with_feature_tags.feature"))
                    .should()
                    .findInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .getViolations();

            assertThat(violations.getFirst().message()).startsWith("Feature");
            System.out.println(violations);
        }

        @Test
        void shouldFindInvalidScenarioTags() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get(INVALID_TAG_COMBINATIONS_DIRECTORY + "dir.feature/feature_with_scenario_tags.feature"))
                    .should()
                    .findInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .getViolations();

            assertThat(violations.getFirst().message()).startsWith("Scenario");
            System.out.println(violations);
        }

        @Test
        void shouldFindInvalidMixtureOfFeatureAndScenarioTags() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get(INVALID_TAG_COMBINATIONS_DIRECTORY + "feature_with_mixed_tags.feature"))
                    .should()
                    .findInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .getViolations();

            assertThat(violations.getFirst().message()).startsWith("Scenario");
            System.out.println(violations);
        }
    }

    @Nested
    class InvalidStepKeywords {
        @Test
        void shouldNotFindViolationsIfNoKeywordIsProvided() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/invalidstepkeywords/feature_language_en.feature"))
                    .should()
                    .findInvalidKeywords(List.of())
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldNotFindViolationsIfKeywordDoesNotExist() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/invalidstepkeywords/feature_language_en.feature"))
                    .should()
                    .findInvalidKeywords(List.of("Happy"))
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldFindViolationsIfKeywordDoesExist() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/invalidstepkeywords/feature_language_en.feature"))
                    .should()
                    .findInvalidKeywords(List.of("But"))
                    .getViolations();

            assertThat(violations).hasSize(3);
            assertThat(violations.getFirst().message()).startsWith("Step");
        }

        @Test
        void shouldFindMultipleViolations() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/invalidstepkeywords/feature_language_de.feature"))
                    .should()
                    .findInvalidKeywords(List.of("Gegeben seien", "Gegeben sei", "Aber"))
                    .getViolations();

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(CukeViolation::message)
                    .allSatisfy(s -> assertThat(s).startsWith("Step"))
                    .allSatisfy(s -> assertThat(s).containsAnyOf("Aber", "Gegeben seien", "Gegeben sei"));
        }
    }

    @Nested
    class MissingScenarioTags {
        @Test
        void shouldFindStepsWithoutUserStoryTag() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/missingtags/feature_without_tags.feature"))
                    .should()
                    .findScenariosMissingRequiredTags(USERSTORY_TAG)
                    .getViolations();

            assertThat(violations).hasSize(2);
            assertThat(violations.getFirst().message()).startsWith("Scenario");
        }
    }

    @Nested
    class ForbiddenFeatureTags {
        @Test
        void shouldFindForbiddenFeatureTag() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureDirectory(Paths.get("src/test/resources/forbiddentags"))
                    .should()
                    .findFeaturesWithDisallowedTags(USERSTORY_TAG)
                    .getViolations();

            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    class DuplicatedScenarios {
        @Test
        void shouldNotFindDuplicatedScenarios() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/duplicatescenarios/feature1.feature"))
                    .should()
                    .findDuplicateScenarioNames()
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldFindDuplicatedScenariosInSameFile() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/duplicatescenarios/feature2.feature"))
                    .should()
                    .findDuplicateScenarioNames()
                    .getViolations();

            assertThat(violations).isNotEmpty();
        }

        @Test
        void shouldFindDuplicatedScenariosInDifferentFiles() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureDirectory(Paths.get("src/test/resources/duplicatescenarios"))
                    .should()
                    .findDuplicateScenarioNames()
                    .getViolations();

            assertThat(violations).hasSize(1);
        }
    }

    @Nested
    class DuplicatedStepExpressions {
        @Test
        void shouldFindDuplicatedStepExpressionsInOneFile() {
            List<CukeViolation> violations = CukeInspector
                    .withJavaPackage("org.cuke.inspector.steps.duplicated.expressions")
                    .should()
                    .findDuplicateStepDefinitions()
                    .getViolations();

            assertThat(violations).hasSize(1);
            assertThat(violations)
                    .extracting(CukeViolation::message)
                    .allSatisfy(s -> assertThat(s).endsWith("'an expression' was found 3 times."));
        }
    }

    @Nested
    class UnusedStepDefinitions {
        @Test
        void shouldNotFindAnyUnusedStepDefinitions() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/nousage/matching_steps.feature"))
                    .withJavaPackage("org.cuke.inspector.steps.matching.steps")
                    .should()
                    .findUnusedStepDefinitions()
                    .getViolations();

            assertThat(violations).isEmpty();
        }

        @Test
        void shouldFindUnusedStepDefinition() throws IOException {
            CukeInspector cukeInspector = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/nousage/matching_steps.feature"))
                    .withJavaPackage("org.cuke.inspector.steps.noparam")
                    .should()
                    .findUnusedStepDefinitions();

            assertThat(cukeInspector.getViolations()).hasSize(1);
        }

        @Test
        void shouldAcceptCustomTypesInExpressions() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureDirectory(Paths.get("src/test/resources/customtype"))
                    .withJavaPackage("org.cuke.inspector.steps.customtype")
                    .should()
                    .findUnusedStepDefinitions()
                    .getViolations();

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    class MissingStepDefinitions {
        @Test
        void shouldFindNotYetImplementedSteps() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureFile(Paths.get("src/test/resources/missingsteps/feature_without_step_impl.feature"))
                    .withJavaPackage("org.cuke.inspector.steps.missing.steps")
                    .should()
                    .findMissingStepDefinitions()
                    .getViolations();

            assertThat(violations).hasSize(1);
            assertThat(violations.getFirst().message()).contains("'Then there is no implementation'");
        }

    }

    @Nested
    class MultipleAnalysis {
        @Test
        void shouldAggregateViolationsOfMultipleAnalysis() throws IOException {
            List<CukeViolation> violations = CukeInspector
                    .withFeatureDirectory(Paths.get("src/test/resources/duplicatescenarios"))
                    .withFeatureDirectory(Paths.get("src/test/resources/forbiddentags"))
                    .withFeatureDirectory(Paths.get("src/test/resources/invalidtagcombinations/"))
                    .withJavaPackage("org.cuke.inspector.steps.duplicated.expressions")
                    .should()
                    .findDuplicateScenarioNames()
                    .findFeaturesWithDisallowedTags(USERSTORY_TAG)
                    .findScenariosMissingRequiredTags(USERSTORY_TAG)
                    .findDuplicateStepDefinitions()
                    .findInvalidTagCombinations(Set.of("@tag2", "@tag3"))
                    .findInvalidKeywords(List.of("But"))
                    .getViolations();

            assertThat(violations).hasSize(15);
        }
    }

}