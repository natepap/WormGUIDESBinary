package application_src.application_model.threeD.subscenegeometry;

/**
 * Created by bradenkatzman on 7/23/17.
 */

/**
 * Options specified in the shapes file that distinguish among types of scene element entities
 * Currently, this functions purely as metadata
 */
public enum SceneElementType {

    /** SearchLayer for descendants of cells in the search results list */
    SINGLE_CELL("its descendants"),

    /** SearchLayer for descendants of cells in the search results list */
    MCS_BODY("Multicellular Structure Body"),

    /** SearchLayer for descendants of cells in the search results list */
    MCS_CONNECTOR("Multicellular Structure Connector"),

    /** SearchLayer for descendants of cells in the search results list */
    MCS_PROCESS("Multicellular Structure Process"),

    /** SearchLayer for descendants of cells in the search results list */
    TRACT("A cellular tract");

    private final String description;

    SceneElementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
