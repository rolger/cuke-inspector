package org.cuke.inspector;

import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CukeInspectorBuilder {
    private final Map<String, InputStream> featureSources;
    private final List<URI> featureURIs;
    private URI glueDirectoryUri;

    CukeInspectorBuilder() {
        featureSources = new HashMap<>();
        featureURIs = new ArrayList<>();
        glueDirectoryUri = null;
    }

    @SneakyThrows
    private void addSource(String uriAsString, InputStream inputStream) {
        featureURIs.add(new URI(uriAsString));
        featureSources.put(uriAsString, inputStream);
    }

    public CukeInspectorBuilder withFeatureFile(String featureSource, InputStream inputStream) {
        addSource(featureSource, inputStream);
        return this;
    }

    public CukeInspectorBuilder withFeatureDirectory(Path directory) throws IOException {
        try (Stream<Path> files = Files.walk(directory)) {
            addAllFeatureFiles(files);
        }
        return this;
    }

    private void addAllFeatureFiles(Stream<Path> files) {
        files.filter(file -> !Files.isDirectory(file))
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".feature"))
                .forEach(this::addSource);
    }

    @SneakyThrows
    private void addSource(Path file) {
        addSource(file.toUri().toString(), new ByteArrayInputStream(Files.readAllBytes(file)));
    }

    public CukeInspectorBuilder withJavaPackage(String packageName) {
        glueDirectoryUri = URI.create("classpath:" + packageName);
        return this;
    }

    public CukeInspector should() {
        return new CukeInspector(new CucumberSupplier(featureSources, featureURIs, glueDirectoryUri));
    }
}
