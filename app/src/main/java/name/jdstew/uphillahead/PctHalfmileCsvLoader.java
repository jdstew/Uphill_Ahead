package name.jdstew.uphillahead;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PctHalfmileCsvLoader {
    private static final String DEBUG_TAG = "name.jdstew.uphillahead.PctHalfmileCsvLoader";

    public static final String PCTA_INPUT_FILE = "C:\\tmp\\PCTA\\pcta_halfmile_elevation2.csv";

    public static final String HALFMILE_INPUT_FILE = "C:\\tmp\\Halfmile\\halfmiles_pcta_marked_modified.txt";

    public static void uploadData() {
        // create HashMap of Halfmile nodes
        HashMap<Integer, HalfmileNode> halfmileMap = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(HALFMILE_INPUT_FILE))){
            String inputLine = bufferedReader.readLine();
            while (inputLine != null) {
                // 6	32.605528152	-116.483197315	2470.24352624449	Hwy94	Highway 94	764	6.038669442255579
                String[] inputParts = inputLine.split("\t");

                String name = inputParts[4];
                String description = inputParts[5];
                int pctaSeqNum = Integer.parseInt(inputParts[6]);
                double distToPcta = Double.parseDouble(inputParts[7]);

                halfmileMap.put(pctaSeqNum, new HalfmileNode(name, description, distToPcta));

                inputLine = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Halfmile nodes read: " + halfmileMap.size());

        // read PCTA data
        List<PctaNode> pctaList = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(PCTA_INPUT_FILE))){
            String inputLine = bufferedReader.readLine();
            while (inputLine != null) {
                // 1,32.5897411500001,-116.466980645,889.41015625,49.020283398724,49.020283398724,0,1
                String[] inputParts = inputLine.split(",");

                int seqNum = Integer.parseInt(inputParts[0]);
                double latitude = Double.parseDouble(inputParts[1]);
                double longitude = Double.parseDouble(inputParts[2]);
                double elevation = Double.parseDouble(inputParts[3]);
                double segLength = Double.parseDouble(inputParts[4]);
                int sectionMark = Integer.parseInt(inputParts[6]);

                pctaList.add(new PctaNode(seqNum, latitude, longitude, elevation, segLength, sectionMark));

                inputLine = bufferedReader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("PCTA nodes read: " + pctaList.size());

        int sectionIndex = 0;
        double prevSegLength = -1.0;
        Graph graph = new Graph();
        graph.setStartDescription("Northbound");
        graph.setEndDescription("Southbound");
        graph.setName(SectionData.sectionBreaks[sectionIndex][0]);
        for (int i = 0; i < pctaList.size(); ++i) {
            PctaNode pctaNode = pctaList.get(i);
            if (i > 0) {
                prevSegLength = pctaList.get(i - 1).segLength;
            }

            // add node
            Node graphNode = new Node(pctaNode.latitude, pctaNode.longitude, pctaNode.elevation);
            if (halfmileMap.containsKey(pctaNode.seqNum)) {
                graphNode.setName(Objects.requireNonNull(halfmileMap.get(pctaNode.seqNum)).name);
                double dist = Objects.requireNonNull(halfmileMap.get(pctaNode.seqNum)).distToPcta;
                if (dist > 30.48) {
                    String sb = Calcs.getDisplayedDist(dist, Config.SYSTEM_IMPERIAL) +
                            " away: " +
                            Objects.requireNonNull(halfmileMap.get(pctaNode.seqNum)).description;
//                    System.out.println("modified: " + sb);
                    graphNode.setDescription(sb);
                } else {
                    graphNode.setDescription(Objects.requireNonNull(halfmileMap.get(pctaNode.seqNum)).description);
                }
            }
            graph.appendNode(graphNode, prevSegLength);

            if (pctaNode.sectionMark == 1) {
                GraphManager.getInstance().addGraph(graph);
                System.out.println("added graph #" + sectionIndex + ": " + SectionData.sectionBreaks[sectionIndex][0]);

                ++sectionIndex;
                graph = new Graph();
                graph.setStartDescription("Northbound");
                graph.setEndDescription("Southbound");
                if (sectionIndex < SectionData.sectionBreaks.length) {
                    graph.setName(SectionData.sectionBreaks[sectionIndex][0]);
                }
                graph.appendNode(graphNode, -1.0);
            }
            prevSegLength = pctaNode.segLength;
        }
    }

    public static void main(String[] args) {
        PctHalfmileCsvLoader.uploadData();
        GraphManager.getInstance().saveGraphs();
        GraphManager.getInstance().saveInstance();
    }

    static class PctaNode {
        public int seqNum;
        public double latitude;
        public double longitude;
        public double elevation;
        public double segLength;
        public int sectionMark;

        PctaNode(int seqNum, double latitude, double longitude, double elevation, double segLength, int sectionMark) {
            this.seqNum = seqNum;
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = elevation;
            this.segLength = segLength;
            this.sectionMark = sectionMark;
        }

    }

    static class HalfmileNode {
        // 6	32.605528152	-116.483197315	2470.24352624449	Hwy94	Highway 94	764	6.038669442255579

        public String name;
        public String description;
        public double distToPcta;

        HalfmileNode(String name, String description, double distToPcta) {
            this.name = name;
            this.description = description;
            this.distToPcta = distToPcta;
        }
    }
}
