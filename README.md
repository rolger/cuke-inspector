
# 🥒 cuke-inspector

<div align="center">

[![Build Status](https://github.com/rolger/cuke-inspector/workflows/Build/badge.svg)](https://github.com/rolger/cuke-inspector/actions)
[![#StandWithUkraine](https://raw.githubusercontent.com/vshymanskyy/StandWithUkraine/main/badges/StandWithUkraine.svg)](https://vshymanskyy.github.io/StandWithUkraine)

</div>

**cuke-inspector** is a lightweight, pluggable linter for [Cucumber](https://cucumber.io/) and [Gherkin](https://cucumber.io/docs/gherkin/). It analyzes feature files and reports common issues, inconsistencies, and violations of your team's conventions.

> ✨ Keep your Gherkin clean. Write better specifications. Find problems before they bite.

---

## 🚀 Features

- ✅ Detect duplicate scenario names
- ✅ Enforce presence of reference tags (e.g., JIRA story IDs) on scenarios
- ✅ Find usage of forbidden tags at the feature level
- ✅ Find forbidden tag combinations on steps
- ✅ Flags usage of Gherkin step keywords that are not allowed by project conventions (e.g., `But`, `Gegeben sei`)
- ✅ ...and more lint rules coming soon!

---

## 📦 Installation

Currently not available as jar file. Clone the repository locally

## Example

```
class FindDuplicatesScenarioNameTest {
    private static final Path FEATURE_FILE_PATH = Paths.get("src/test/resources/at.gv.bmf.ef.zkvbg.bdd.test.feature/");

    @Test
    void scenarioNameShouldBeUnique() throws IOException {
        CukeInspector.withFeatureDirectory(FEATURE_FILE_PATH)
                .should()
                .findDuplicateScenarioNames()
                .hasNoViolations();
    }
}
```
---

## 🤝 Contributing
Got an idea for a rule or fix? PRs and issues are welcome!

## 📄 License
MIT

## 🧑‍💻 Maintained by
cuke-inspector is crafted with ☕ and 🥒 by developers who care about clean specs.
