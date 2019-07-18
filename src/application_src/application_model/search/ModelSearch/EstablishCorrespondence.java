package application_src.application_model.search.ModelSearch;

import application_src.application_model.data.LineageData;
import application_src.application_model.data.OrganismDataType;
import application_src.application_model.search.ModelSearch.ModelSpecificSearchOps.StructuresSearch;
import application_src.application_model.search.OrganismSearchResults;
import application_src.application_model.threeD.subscenegeometry.SceneElementsList;
import javafx.scene.control.TreeItem;

import java.util.*;

/**
 * This class is one of two main interfaces between the model-agnostic Search pipeline
 * and the specifics of the model displayed in the window.
 *
 * The purpose of the class is to establish the correspondence between the results
 * of search and the underlying model. This correspondence (in the form of a list
 * of names) is then used by the annotation pipeline to annotate the model. After
 * completing the correspondence routine, EstablishCorrespondence dispatches the
 * results to {@Link application_src.annotation_model.annotation.AnnotationManager}
 */
public class EstablishCorrespondence {
    // representations of the underlying model
    private static LineageData lineageData;
    private static StructuresSearch structuresSearch;

    private static Map<String, TreeItem<String>> binLineageMap;

    public EstablishCorrespondence(LineageData lineageData, StructuresSearch structuresSearch, Map binLineageMap) {
        this.lineageData = lineageData;
        this.structuresSearch = structuresSearch;
        this.binLineageMap = binLineageMap;
    }

    public List<String> establishCorrespondence(OrganismSearchResults searchResults,
                                                boolean includeCellNuc, boolean includeCellBody) {
        ArrayList<String> correspondenceList = new ArrayList<>();

        if (searchResults.getSearchResultsDataType().equals(OrganismDataType.GENE)) {
            return searchResults.getSearchResults();
        }else if(searchResults.getSearchResultsDataType().equals(OrganismDataType.BINARY)){
            List<String> tempList = new ArrayList<>(searchResults.getSearchResults().size());

            for(String binResult: searchResults.getSearchResults()){
                for(String binEntry: binLineageMap.keySet()){
                    if(binLineageMap.get(binEntry).getValue().equals(binResult)){
                        tempList.add(binEntry);
                    }
                }
            }

            searchResults = new OrganismSearchResults(new AbstractMap.SimpleEntry<>(OrganismDataType.LINEAGE, tempList));
        }

        if (includeCellNuc) {
            // iterate over the search results, and find the entities that have overlap in the underlying model. Add these
            // to the correspondence list
            List<String> names = lineageData.getAllCellNames();
            for(String result : searchResults.getSearchResults()) {
                for (String lineageName : names) {
                    if (lineageName.equalsIgnoreCase(result)) {
                        correspondenceList.add(lineageName);
                    }
                }
            }
        }

        if (includeCellBody) {
            correspondenceList.addAll(structuresSearch.getCellBodiesList(searchResults.getSearchResults()));
        }

        // remove duplicates
        Set<String> hs = new HashSet<>();
        hs.addAll(correspondenceList);
        correspondenceList.clear();
        correspondenceList.addAll(hs);

        return correspondenceList;
    }

}
