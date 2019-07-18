package application_src.application_model.data;

public enum OrganismDataType {
    LINEAGE("SulstonLineage"),

    FUNCTIONAL("Functional"),

    BINARY("Binary"),

    GENE("Gene");

    private final String description;

    OrganismDataType(String description) { this.description = description; }

    public String getDescription() { return description; }
}