/*
 * EnvironmentDivision - System and I/O configuration
 * Author: Sekacorn
 * Created: 2025-01-10
 * Created from scratch; no third-party code copied.
 */
package com.sekacorn.corn.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the ENVIRONMENT DIVISION of a COBOL program.
 * Contains configuration section and input-output section.
 */
public final class EnvironmentDivision {
    private final ConfigurationSection configuration;
    private final InputOutputSection inputOutput;

    @JsonCreator
    public EnvironmentDivision(
            @JsonProperty("configuration") ConfigurationSection configuration,
            @JsonProperty("inputOutput") InputOutputSection inputOutput) {
        this.configuration = configuration;
        this.inputOutput = inputOutput;
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public InputOutputSection getInputOutput() {
        return inputOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnvironmentDivision that)) return false;
        return Objects.equals(configuration, that.configuration) &&
                Objects.equals(inputOutput, that.inputOutput);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, inputOutput);
    }

    /**
     * Configuration section (source-computer, object-computer, special-names)
     */
    public static final class ConfigurationSection {
        private final String sourceComputer;
        private final String objectComputer;
        private final List<SpecialName> specialNames;

        @JsonCreator
        public ConfigurationSection(
                @JsonProperty("sourceComputer") String sourceComputer,
                @JsonProperty("objectComputer") String objectComputer,
                @JsonProperty("specialNames") List<SpecialName> specialNames) {
            this.sourceComputer = sourceComputer;
            this.objectComputer = objectComputer;
            this.specialNames = specialNames != null ? List.copyOf(specialNames) : Collections.emptyList();
        }

        public String getSourceComputer() {
            return sourceComputer;
        }

        public String getObjectComputer() {
            return objectComputer;
        }

        public List<SpecialName> getSpecialNames() {
            return specialNames;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConfigurationSection that)) return false;
            return Objects.equals(sourceComputer, that.sourceComputer) &&
                    Objects.equals(objectComputer, that.objectComputer) &&
                    Objects.equals(specialNames, that.specialNames);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceComputer, objectComputer, specialNames);
        }
    }

    /**
     * Input-Output section (file-control, I-O-control)
     */
    public static final class InputOutputSection {
        private final List<FileControlEntry> fileControl;

        @JsonCreator
        public InputOutputSection(@JsonProperty("fileControl") List<FileControlEntry> fileControl) {
            this.fileControl = fileControl != null ? List.copyOf(fileControl) : Collections.emptyList();
        }

        public List<FileControlEntry> getFileControl() {
            return fileControl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InputOutputSection that)) return false;
            return Objects.equals(fileControl, that.fileControl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileControl);
        }
    }

    public static final class SpecialName {
        private final String mnemonicName;
        private final String implementorName;

        @JsonCreator
        public SpecialName(
                @JsonProperty("mnemonicName") String mnemonicName,
                @JsonProperty("implementorName") String implementorName) {
            this.mnemonicName = mnemonicName;
            this.implementorName = implementorName;
        }

        public String getMnemonicName() {
            return mnemonicName;
        }

        public String getImplementorName() {
            return implementorName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SpecialName that)) return false;
            return Objects.equals(mnemonicName, that.mnemonicName) &&
                    Objects.equals(implementorName, that.implementorName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mnemonicName, implementorName);
        }
    }
}
