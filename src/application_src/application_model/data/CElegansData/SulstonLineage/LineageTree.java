/*
 * Bao Lab 2016
 */

package application_src.application_model.data.CElegansData.SulstonLineage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.control.TreeItem;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

import binaryLineage.binaryLineageTree;

/**
 * Tree containing the lineage of the underlying embryo.
 *
 * This is used to represent the model itself and assumes a C Elegans lineage structure.
 */
public class LineageTree {

    /** Maps a lower case cell name to its tree node */
    private static final Map<String, TreeItem<String>> nameNodeHash = new HashMap<>();

    private static boolean isSulstonMode;

    private final String[] allCellNames;

    private List<String> treeBaseNames;

    private TreeItem<String> root;
    private TreeItem<String> ab;
    private TreeItem<String> ms;
    private TreeItem<String> e;
    private TreeItem<String> c;
    private TreeItem<String> d;

    private Map<String, TreeItem<String>> binNameHash;
    private TreeItem<String> binRoot;
    private binaryLineageTree binLineageTree;

    public LineageTree(final String[] allCellNames, final boolean isSulstonMode) {
        if (allCellNames != null) {
            this.allCellNames = allCellNames;
        } else {
            this.allCellNames = new String[1];
        }

        LineageTree.isSulstonMode = isSulstonMode;
        if (isSulstonMode) {
            root = new TreeItem<>("P0");
            // names of the cell added to tree upon initialization
            treeBaseNames = asList(
                    "p0",
                    "ab",
                    "aba",
                    "abal",
                    "abar",
                    "abp",
                    "abpl",
                    "abpr",
                    "p1",
                    "ems",
                    "ms",
                    "e",
                    "p2",
                    "c",
                    "p3",
                    "d",
                    "p4",
                    "z2",
                    "z3");

            // zero-th root layer
            nameNodeHash.put("p0", root);

            // first layer
            ab = makeTreeItem("AB");
            final TreeItem<String> p1 = makeTreeItem("P1");
            root.getChildren().add(ab);
            root.getChildren().add(p1);

            // second layer
            final TreeItem<String> aba = makeTreeItem("ABa");
            final TreeItem<String> abp = makeTreeItem("ABp");
            ab.getChildren().add(aba);
            ab.getChildren().add(abp);

            final TreeItem<String> ems = makeTreeItem("EMS");
            final TreeItem<String> p2 = makeTreeItem("P2");
            p1.getChildren().add(ems);
            p1.getChildren().add(p2);

            // third layer
            final TreeItem<String> abal = makeTreeItem("ABal");
            final TreeItem<String> abar = makeTreeItem("ABar");
            aba.getChildren().add(abal);
            aba.getChildren().add(abar);

            final TreeItem<String> abpl = makeTreeItem("ABpl");
            final TreeItem<String> abpr = makeTreeItem("ABpr");
            abp.getChildren().add(abpl);
            abp.getChildren().add(abpr);

            ms = makeTreeItem("MS");
            e = makeTreeItem("E");
            ems.getChildren().add(ms);
            ems.getChildren().add(e);

            c = makeTreeItem("C");
            final TreeItem<String> p3 = makeTreeItem("P3");
            p2.getChildren().add(c);
            p2.getChildren().add(p3);

            // fourth layer (rightmost branch)
            d = makeTreeItem("D");
            final TreeItem<String> p4 = makeTreeItem("P4");
            p3.getChildren().add(d);
            p3.getChildren().add(p4);

            // fifth layer (rightmost branch)
            final TreeItem<String> z2 = makeTreeItem("Z2");
            final TreeItem<String> z3 = makeTreeItem("Z3");
            p4.getChildren().add(z2);
            p4.getChildren().add(z3);
        }
        addAllCells();
    }

    /**
     * Adds tree nodes for all cells in 'allCellNames'
     */
    private void addAllCells() {
        for (String name : allCellNames) {
            if (isSulstonMode) {
                if (treeBaseNames.contains(name.toLowerCase())) {
                    continue;
                }
            }
            if (name.toLowerCase().startsWith("nuc")) {
                continue;
            }
            addCell(name);
        }
        binLineageTree = new binaryLineageTree(getRoot(), nameNodeHash);
        binNameHash = binLineageTree.makeBinTree();
        binRoot = binLineageTree.getRoot();
    }

    /**
     * @param name
     *         the name to check
     *
     * @return the case-sensitive name of that name
     */
    public static String getCaseSensitiveName(String name) {
        name = name.toLowerCase();
        if (nameNodeHash.get(name) == null) {
            return "'" + name + "' Systematic";
        }
        return nameNodeHash.get(name).getValue();
    }

    /**
     * @param ancestor
     *         the cell name to check
     * @param descendant
     *         the potential descendant
     *
     * @return true if 'ancestor' is the ancestor of 'descendant', false otherwise
     */
    public static boolean isAncestor(final String ancestor, final String descendant) {
        return isDescendant(descendant, ancestor);
    }

    /**
     * @param descendant
     *         the cell name to check
     * @param ancestor
     *         the potential ancestor
     *
     * @return true if 'descendant' is a descendant of 'ancestor', false otherwise
     */
    public static boolean isDescendant(String descendant, String ancestor) {
        descendant = descendant.trim().toLowerCase();
        ancestor = ancestor.trim().toLowerCase();

        if (!nameNodeHash.containsKey(descendant) || !nameNodeHash.containsKey(ancestor)) {
            return false;
        }

        if (isSulstonMode) {
            final String p0 = "p0";
            final String p = "p";

            // root p0 is always an ancestor
            if (ancestor.equalsIgnoreCase(p0)) {
                return true;
            }
            // root p0 is never a descendant
            if (descendant.equalsIgnoreCase(p0)) {
                return false;
            }
            // if the descendant is "AB", check if the ancestor is "P0"
            if (descendant.equals("ab")) {
                return ancestor.equals(p0);
            }

            // if the descendant starts with "Z", see if the ancestor is any "PX" cell
            if (descendant.startsWith("z")) {
                return ancestor.startsWith(p);
            }

            // if the descendant is "D", see if the ancestor is any "PX" cell excluding P4 (D's sibling)
            if (descendant.equals("d")) {
                return ancestor.startsWith(p) && parseInt(ancestor.substring(1, 2)) <= 3;
            }

            // if the descendant is "C", see if the ancestor is "P2", "P1" or "P0"
            if (descendant.equals("c")) {
                return ancestor.startsWith(p) && parseInt(ancestor.substring(1, 2)) <= 2;
            }

            // if the descendant is "EMS", see if the ancestor is "P1" or "P0"
            if (descendant.equals("ems")) {
                return ancestor.startsWith(p) && parseInt(ancestor.substring(1, 2)) <= 1;
            }

            // if the descendant is "MS", see if the ancestor is "EMS", "P1" or "P0"
            if (descendant.equals("ms") || descendant.equals("e")) {
                return ancestor.equals("ems")
                        || (ancestor.startsWith(p) && parseInt(ancestor.substring(1, 2)) <= 1);
            }

            // for the p cells, test number after the 'p'
            if (descendant.startsWith(p) && ancestor.startsWith(p)) {
                return descendant.compareTo(ancestor) > 0;
            }

            // if the descendant starts with "E" or "M", see if the ancestor is "EMS", but continue if not
            if (descendant.startsWith("e") || descendant.startsWith("ms")) {
                if (ancestor.equals("ems") || ancestor.equals("p1")) return true;
            }

            // if the descendant starts with "C", see if the ancestor is "P2", "P1" or "P0", but continue if not
            if (descendant.startsWith("c")) {
                if (ancestor.startsWith(p) && parseInt(ancestor.substring(1, 2)) <= 2) return true;
            }

            // if the descendant starts with "D", see if the ancestor is "P3", "P2", "P1" or "P0", but continue if not
            if (descendant.startsWith("d")) {
                if (ancestor.startsWith(p) && parseInt(ancestor.substring(1, 2)) <= 3) return true;
            }

            // try to decipher lineage from names
            if (descendant.startsWith(ancestor)
                    && descendant.length() > ancestor.length()) {
                return true;
            }
        }
        return false;
    }

    public TreeItem<String> getRoot() {
        return root;
    }

    /**
     * Adds a node to the tree specified by a cell name
     *
     * @param cellName
     *         the cell name
     */
    private void addCell(String cellName) {
        String startingLetter = cellName.substring(0, 1).toLowerCase();
        TreeItem<String> startingNode = null;
        TreeItem<String> parent;
        switch (startingLetter) {
            case "a":
                startingNode = ab;
                break;
            case "m":
                startingNode = ms;
                break;
            case "e":
                startingNode = e;
                break;
            case "c":
                startingNode = c;
                break;
            case "d":
                startingNode = d;
                break;
        }

        if (startingNode != null) {
            parent = addCellHelper(cellName, startingNode);
            if (parent != null) {
                parent.getChildren().add(makeTreeItem(cellName));
            }
        }
    }

    /**
     * @param cellName
     *         name of the node we want to fetch the parent for
     * @param node
     *         the node to check, may be the parent node
     *
     * @return parent of the node specified by the name
     */
    private TreeItem<String> addCellHelper(String cellName, TreeItem<String> node) {
        String currName = node.getValue().toLowerCase();
        cellName = cellName.toLowerCase();

        if (cellName.length() == currName.length() + 1 && cellName.startsWith(currName)) {
            return node;
        }

        for (TreeItem<String> child : node.getChildren()) {
            String childName = child.getValue().toLowerCase();
            if (cellName.startsWith(childName)) {
                return addCellHelper(cellName, child);
            }
        }

        return null;
    }

    private TreeItem<String> makeTreeItem(String name) {
        final TreeItem<String> node = new TreeItem<>(name);
        nameNodeHash.put(name.toLowerCase(), node);
        return node;
    }

    public static boolean isLineageNameInTree(String name) {
        return nameNodeHash.containsKey(name.toLowerCase());
    }

    public Map<String, TreeItem<String>> getBinMap(){return binNameHash;}
}
