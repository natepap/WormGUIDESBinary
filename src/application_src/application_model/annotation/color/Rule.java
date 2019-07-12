/*
 * Bao Lab 2017
 */

package application_src.application_model.annotation.color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.CElegansSearch.CElegansSearch;
import application_src.application_model.search.CElegansSearch.CElegansSearchResults;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.ModelSpecificSearchUtil;
import application_src.application_model.search.SearchConfiguration.SearchOption;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import application_src.application_model.search.SearchConfiguration.SearchType;
import application_src.MainApp;
import application_src.controllers.controllers.RuleEditorController;
import application_src.controllers.layers.SearchLayer;
import application_src.views.graphical_representations.RuleGraphic;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import static javafx.application.Platform.runLater;
import static javafx.stage.Modality.NONE;



import static application_src.application_model.data.CElegansData.PartsList.PartsList.getFunctionalNameByLineageName;
import static application_src.application_model.data.CElegansData.PartsList.PartsList.isLineageName;
import static application_src.application_model.search.SearchConfiguration.SearchType.GENE;
import static application_src.application_model.search.SearchConfiguration.SearchType.STRUCTURES_BY_HEADING;
import static application_src.application_model.search.SearchConfiguration.SearchType.STRUCTURE_BY_SCENE_NAME;
import static application_src.application_model.data.CElegansData.SulstonLineage.LineageTree.isAncestor;
import static application_src.application_model.data.CElegansData.SulstonLineage.LineageTree.isDescendant;
import static application_src.application_model.search.SearchConfiguration.SearchOption.ANCESTOR;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_BODY;
import static application_src.application_model.search.SearchConfiguration.SearchOption.CELL_NUCLEUS;
import static application_src.application_model.search.SearchConfiguration.SearchOption.DESCENDANT;

/**
 * This class is the color rule that determines the coloring/striping of cell, cell bodies, and multicellular
 * structures. It is instantiated by the {@link SearchLayer} class and added to the list of rules in 'Display
 * Options' tab. This class also contains the graphical representation of the rule, which is used to display the rule
 * in the list view in the tab.
 */

public class Rule {

    private final SubmitHandler submitHandler;

    private final SearchType searchType;

    private final RuleGraphic graphic;

    private BooleanProperty rebuildSubsceneFlag;

    private Stage editStage;

    private String text;

    private List<SearchOption> currentOptions;
    private List<SearchOption> previousOptions;
    private BooleanProperty ruleChanged;
    private boolean visible;
    private Color color;

    private List<String> cells;
    /**
     * True if the list of cells has been set, false otherwise. The cells list of a structure rule based on a scene
     * name (with the search type {@link SearchType#STRUCTURE_BY_SCENE_NAME}) is never set.
     */
    private boolean cellsSet;

    private RuleEditorController editController;

    /**
     * Rule class constructor called by the {@link SearchLayer} class
     *
     * @param searched
     *         text that user searched
     * @param color
     *         color that the search cell(s), cell body(ies), and/or multicellular structure(s) should have in the 3D
     *         subscene
     * @param type
     *         type of search that was made
     * @param options
     *         options that the rule should be extended to
     */
    public Rule(
            final BooleanProperty rebuildSubsceneFlag,
            String searched,
            Color color,
            SearchType type,
            List<SearchOption> options,
            CElegansSearch cElegansSearch) {

        this.rebuildSubsceneFlag = requireNonNull(rebuildSubsceneFlag);

        searchType = type;
        setOptions(options);

        ruleChanged = new SimpleBooleanProperty(false);
        graphic = new RuleGraphic(this, ruleChanged);
        ruleChanged.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (editController != null) {
                    graphic.setColorButton(editController.getColor());
                }

                // if the ancestors or descendants options change, we need to rerun the search
                boolean prevAnc = previousOptions.contains(ANCESTOR);
                boolean prevDes = previousOptions.contains(DESCENDANT);
                boolean currAnc = currentOptions.contains(ANCESTOR);
                boolean currDes = currentOptions.contains(DESCENDANT);
                if (prevAnc != currAnc || prevDes != currDes) {
                    switch (searchType) {
                        case LINEAGE:
                            List<String> cells = new ArrayList<>();
                            CElegansSearchResults cElegansDataSearchResults = new CElegansSearchResults(
                                    cElegansSearch.executeLineageSearch(searched,
                                            currAnc,
                                            currDes));
                            cells.addAll(cElegansDataSearchResults.getSearchResults());

                            /** The lineage type has a fallthrough method which does a strict
                             * string matching search if there are no static results. That way,
                             * in the event of a non-sulston embryo, you can search directly for
                             * the names of specific entities */
                            if (cells.isEmpty()) {
                                cells.addAll(ModelSpecificSearchUtil.nonSulstonLineageSearch(searched,
                                        currAnc,
                                        currDes));

                            }

                            setCells(cells);
                            break;
                        case FUNCTIONAL:
                            setCells(cElegansSearch.executeFunctionalSearch(searched.substring(searched.indexOf("'")+1, searched.lastIndexOf("'")), currAnc, currDes, OrganismDataType.LINEAGE).getValue());
                            break;
                        case DESCRIPTION:
                            setCells(cElegansSearch.executeDescriptionSearch(searched.substring(searched.indexOf("'")+1, searched.lastIndexOf("'")), currAnc, currDes, OrganismDataType.LINEAGE).getValue());
                            break;
                        case GENE:
                            System.out.println("Updating gene rule from RuleEditorController is currently not supported.\nPlease delete the rule and run an updated search from the Find Cells tab. (07/2018)");
                            break;
                        case CONNECTOME:
                            boolean pre = false;
                            boolean post = false;
                            boolean elec = false;
                            boolean neuro = false;
                            if (text.contains("pre")) {
                                pre = true;
                            }
                            if (text.contains("post")) {
                                post = true;
                            }
                            if (text.contains("elec")) {
                                elec = true;
                            }
                            if (text.contains("neuro")) {
                                neuro = true;
                            }
                            setCells(cElegansSearch.executeConnectomeSearch(searched.substring(searched.indexOf("'")+1, searched.lastIndexOf("'")), currAnc, currDes, pre, post, elec, neuro, OrganismDataType.LINEAGE).getValue());
                            break;
                    }
                }

                rebuildSubsceneFlag.set(true);
                ruleChanged.set(false);
            }
        });

        submitHandler = new SubmitHandler();

        cells = new ArrayList<>();
        cellsSet = false;

        visible = true;

        setColor(color);
        setSearchedText(searched);
        graphic.resetTooltip(toStringFull());
    }

    public void resetLabel(final String labelText) {
        graphic.resetLabel(labelText);
    }

    /**
     * Changes the visibility button graphic according to whether or now the rule should be applied to the subscene
     * entities
     *
     * @param isBlackedOut
     *         true if the visibility button should be blacked out, false otherwise. The visibility button is blacked
     *         out when the rule is not applied to the subscene entities
     */
    private void blackOutVisibleButton(final boolean isBlackedOut) {
        runLater(() -> graphic.blackOutVisibleButton(isBlackedOut));
    }

    /**
     * Sets the rule's visibility
     *
     * @param isVisible
     *         true if the rule is visible, false otherwise
     */
    public void setVisible(final boolean isVisible) {
        visible = isVisible;
    }

    /**
     * Shows the editor for the rule
     *
     * @param stage
     *         the stage that the rule editor window belongs to
     */
    public void showEditStage(final Stage stage) {
        if (editStage == null) {
            initEditStage(stage, rebuildSubsceneFlag);
        }
        editController.setHeading(graphic.getLabelText());
        editStage.show();
        ((Stage) editStage.getScene().getWindow()).toFront();
    }

    /**
     * Initializes the edit stage by loading the layout RuleEditorLayout.fxml
     *
     * @param stage
     *         The {@link Stage} to which the rule editor window belongs to
     */
    private void initEditStage(final Stage stage, final BooleanProperty rebuildSubsceneFlag) {
        editController = new RuleEditorController();

        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("/application_src/views/layouts/RuleEditorLayout.fxml"));

        loader.setController(editController);
        loader.setRoot(editController);

        try {
            editStage = new Stage();
            editStage.setScene(new Scene(loader.load()));

            for (Node node : editStage.getScene().getRoot().getChildrenUnmodifiable()) {
                node.setStyle("-fx-focus-color: -fx-outer-border; -fx-faint-focus-color: transparent;");
            }

            editStage.setTitle("Edit Rule");
            if (stage != null) {
                editStage.initOwner(stage);
            }
            editStage.initModality(NONE);

            editController.setHeading(text);
            editController.setSubmitHandler(submitHandler);
            editController.setColor(color);
            editController.setCellTicked(isCellSelected());
            editController.setCellBodyTicked(isCellBodySelected());
            editController.setAncestorsTicked(isAncestorSelected());
            editController.setDescendantsTicked(isDescendantSelected());

            final String textLowerCase = text.toLowerCase();
            if (textLowerCase.contains("functional") || textLowerCase.contains("description") || textLowerCase.contains("connectome")) {
                editController.disableDescendantOption();
            } else if (isStructureRuleBySceneName() || isStructureRuleByHeading()) {
                editController.disableOptionsForStructureRule();
            } else if (textLowerCase.contains("gene")) {
                // this is a harder operation to support because this a threaded search in the CElegansSearch class.
                // for now we won't support it. In the future, to support this, you need to write a subroutine that
                // take the list of gene search results and executes lineage searching on it
                editController.disableAncestorOption();
                editController.disableDescendantOption();
            }

        } catch (IOException ioe) {
            System.out.println("error in instantiating rule editor - input/output exception");
            ioe.printStackTrace();

        } catch (NullPointerException npe) {
            System.out.println("error in instantiating rule editor - null pointer exception");
            npe.printStackTrace();
        }
    }

    /**
     * @return true if the rule should color a multicellular structure, false otherwise
     */
    public boolean isStructureRuleBySceneName() {
        return searchType == STRUCTURE_BY_SCENE_NAME;
    }

    /**
     * @return true if the rule is a structure heading rule, false otherwise
     */
    public boolean isStructureRuleByHeading() {
        return searchType == STRUCTURES_BY_HEADING;
    }

    /**
     * @return the list of baseline cells that this rule affects, not including
     * decsendant or ancestor cells.
     */
    public List<String> getCells() {
        return cells;
    }

    /**
     * Called by the {@link SearchLayer} class to set the list of cells that the rule affects. Multicellular
     * structure rule cells are never set since they are queried by name only.
     *
     * @param list
     *
     */
    public void setCells(final List<String> list) {
        if (list != null) {
            cells.clear();
            cells.addAll(list);
            cellsSet = true;
        }
    }

    public String getSearchedText() {
        return text;
    }

    /**
     * Sets the searched term entered by the user when the rule was added.
     *
     * @param name
     *         user-searched name
     */
    public void setSearchedText(final String name) {
        if (name != null) {
            text = name;
            graphic.resetLabel(toStringFull());
        }
    }

    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the rule.
     *
     * @param color
     *         color that the rule should apply to the cell(s), cell body(ies), and/or multicellular structures it
     *         affects
     */
    public void setColor(final Color color) {
        if (color != null) {
            this.color = color;
            graphic.setColorButton(color);
        }
    }

    public HBox getGraphic() {
        return graphic;
    }

    public Button getDeleteButton() {
        return graphic.getDeleteButton();
    }

    public boolean isCellSelected() {
        return currentOptions.contains(CELL_NUCLEUS);
    }

    public boolean isCellBodySelected() {
        return currentOptions.contains(CELL_BODY);
    }

    public boolean isAncestorSelected() {
        return currentOptions.contains(ANCESTOR);
    }

    public boolean isDescendantSelected() {
        return currentOptions.contains(DESCENDANT);
    }

    public SearchOption[] getOptions() {
        return currentOptions.toArray(new SearchOption[currentOptions.size()]);
    }

    public void setOptions(final List<SearchOption> options) {
        previousOptions = new ArrayList<>();
        if (currentOptions != null) {
            // set the previous
            previousOptions.addAll(currentOptions.stream().filter(Objects::nonNull).collect(toList()));
        }

        // set the new options to the current
        this.currentOptions = new ArrayList<>();
        this.currentOptions.addAll(options.stream().filter(Objects::nonNull).collect(toList()));
    }

    /**
     * @param other
     *         rule to compare to
     *
     * @return true if the rules contain the same searched text, false otherwise
     */
    public boolean equals(final Rule other) {
        return text.equalsIgnoreCase(other.getSearchedText());
    }

    @Override
    public String toString() {
        return toStringFull();
    }

    /**
     * @return full description of the rule used in the tooltip and the label in the heading of the rule editor popup.
     * The return string contains the rule's name and options.
     */
    public String toStringFull() {
        final StringBuilder sb = new StringBuilder(text);
        sb.append(" ");
        if (!currentOptions.isEmpty()) {
            sb.append("(");
            for (int i = 0; i < currentOptions.size(); i++) {
                sb.append(currentOptions.get(i).toString());
                if (i < currentOptions.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * @param name
     *         lineage name of queried cell
     *
     * @return true if the rule is visible and applies to cell nucleus with specified name, false otherwise
     */
    public boolean appliesToCellNucleus(String name) {
        if (!visible) {
            return false;
        }

        if (name == null) return false;

        name = name.trim();
        if (currentOptions.contains(CELL_NUCLEUS) && cells.contains(name)) {
            //System.out.println("Cells contains: " + name);
            return true;
        }
//        for (String cell : cells) {
//            if (currentOptions.contains(ANCESTOR) && isAncestor(name, cell)) {
//                System.out.println("isAncestor: " + cell);
//                return true;
//            }
//            if (currentOptions.contains(DESCENDANT) && isDescendant(name, cell)) {
//                System.out.println("isDescendant: " + cell);
//                return true;
//            }
//        }
        return false;
    }

    /**
     * @param name
     *         scene name of multicellular structure
     *
     * @return true if the rule is visible and it applies to multicellcular structure with the specified name, false
     * otherwise
     */
    public boolean appliesToStructureWithSceneName(String name) {
        if (visible && (isStructureRuleBySceneName() || isStructureRuleByHeading())) {
            final String structureName = text.substring(1, text.lastIndexOf("'"));
            if (isStructureRuleBySceneName()) {
                if (isLineageName(name)) {
                    // translate the rule's lineage name to its functional name because the rule itself uses
                    // functional names for legibility
                    name = getFunctionalNameByLineageName(name);
                }
                return structureName.equalsIgnoreCase(name.trim());
            } else if (isStructureRuleByHeading()) {
                for (String structure : cells) {
                    if (structure.equalsIgnoreCase(name.trim())) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    /**
     * @param name
     *         lineage name of the cell to check
     *
     * @return true if the rule is visible, applies to a cell body, and applies to the cell with the input name, false
     * otherwise
     */
    public boolean appliesToCellBody(final String name) {
        if (!visible || !currentOptions.contains(CELL_BODY)) {
            return false;
        }

        for (String cell : cells) {
            if (cell.equalsIgnoreCase(name)) {
                return true;
            }
//            if (currentOptions.contains(DESCENDANT) && isDescendant(name, cell)) {
//                return true;
//            }
//            if (currentOptions.contains(ANCESTOR) && isAncestor(name, cell)) {
//                return true;
//            }
        }
        return false;
    }

    /**
     * @return the search type of the rule
     */
    public SearchType getSearchType() {
        return searchType;
    }

    /**
     * @return true if rule is visible; false otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Action event submitHandler for a click on the 'Submit' button in the rule editor popup.
     */
    private class SubmitHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (editController != null) {
                setColor(editController.getColor());
                editStage.hide();

                // because the multicellular name based rule is not a check option, we need to override this function
                // to avoid overwriting the multicellular search option
                if (searchType != STRUCTURE_BY_SCENE_NAME) {
                    setOptions(editController.getOptions());
                }
                final String fullRuleString = toStringFull();
                graphic.resetLabel(fullRuleString);
                graphic.resetTooltip(fullRuleString);

                ruleChanged.set(true);
            }
        }
    }
}