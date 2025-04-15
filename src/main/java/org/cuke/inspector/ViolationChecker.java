package org.cuke.inspector;

import io.cucumber.messages.types.GherkinDocument;

import java.util.Collection;
import java.util.List;

public interface ViolationChecker {

    Collection<? extends CukeViolation> inspect(List<GherkinDocument> gherkinDocuments);

}
