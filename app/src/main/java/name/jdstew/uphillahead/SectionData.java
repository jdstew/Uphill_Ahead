package name.jdstew.uphillahead;

import java.util.Arrays;

public class SectionData {


    // the indexes below are 1-based
    public static final String[][] sectionBreaks = {
            {"CA-A, Campo (MX border) to Warner Springs",    "33.2734745000001", "-116.6452007", "46496"},
            {"CA-B, Warner Springs to Cabazon(I-10)",        "33.9240053", "-116.6945376", "85107"},
            {"CA-C, Cabazon(I-10) to Cajon P(I-15)",         "34.3063993000001", "-117.4655729", "146719"},
            {"CA-D, Cajon P(I-15) to Auga Dulce(HWY 14)",    "34.4768121000001", "-118.3055082", "196619"},
            {"CA-E, Agua Dulce(HWY 14) to Tehachapi(HWY58)", "35.0990091", "-118.2931353", "228843"},
            {"CA-F, Tehachapi P(HWY 58) to Walker(HWY 178)", "35.6627936", "-118.0363204", "264849"},
            {"CA-G, Walker P(HWY 178) to Crabtree M(Whit)",  "36.5525214", "-118.3586722", "318963"},
            {"CA-H, Crabtree M to Tuolumne(HWY 120)",        "37.8765925000001", "-119.3456312", "422098"},
            {"CA-I, Tuolumne(HWY 120) to Sonora P(HWY 108)", "38.3299362000001", "-119.6358403", "465651"},
            {"CA-J, Sonora P(HWY 108) to Echo Lake(HWY 50)", "38.8347410000001", "-120.0442356", "505533"},
            {"CA-K, Echo Lake(HWY 50) to Donner S(I-80)",    "39.3428771710001", "-120.336217262", "539723"},
            {"CA-L, Donner S(I-80) to Sierra City(HWY 49)",  "39.5765753480001", "-120.612559701", "552299"},
            {"CA-M, Sierra City(HWY 49) to Belden(HWY 70)",  "40.0070320890001", "-121.250589312", "578946"},
            {"CA-N, Belden(HWY 70) to Burney Falls(HWY 89)", "41.010826163", "-121.653128174", "619376"},
            {"CA-O, Burney Falls(HWY 89) to Castle C(I-5)",  "41.1624409", "-122.2984423", "645593"},
            {"CA-P, Castle Crag(I-5) to Etna S(HWY 3)",      "41.3958178", "-122.9958448", "693556"},
            {"CA-Q, Etna S(HWY 3) to Seiad Valley(HWY 96)",  "41.8347196", "-123.1776351", "721485"},
            {"CA-R, Seiad Valley(HWY 96) to Callahans(I-5)", "42.0638789000001", "-122.6025846", "751503"},
            {"OR-B, Callahans(I-5) to Fish L(HWY 140)",      "42.3958084000001", "-122.2914749", "778701"},
            {"OR-C, Fish L(HWY 140) to Cascade C(HWY 138)",  "43.0890144000001", "-122.0917902", "812555"},
            {"OR-D, Cascade C(HWY 138) to Willam P(HWY 58)", "43.5972164", "-122.0336008", "841086"},
            {"OR-E, Willam P(HWY 58) to McKenzie(HWY 242)",  "44.2598509600001", "-121.805200987", "879103"},
            {"OR-F, McKenzie(HWY 242) to Barlow P(HWY 35)",  "45.2843877", "-121.6815082", "934313"},
            {"OR-G, Barlow P(HWY 35) to Cascade LKS(I-84)",  "45.6621004", "-121.8965211", "966471"},
            {"WA-H, Cascade Locks(I-84) to White P(HWY 12)", "46.6435133000001", "-121.3788492", "1050887"},
            {"WA-I, White P(HWY 12) to Snoqualmie P(I-90)",  "47.4257927", "-121.4157883", "1103010"},
            {"WA-J, Snoqualmie P(I-90) to Stevens P(HWY 2)", "47.746344069", "-121.088544996", "1128210"},
            {"WA-K, Stevens P(HWY 2) to Rainy P(HWY 20)",    "48.5148531", "-120.7333205", "1203073"},
            {"WA-L, Rainy P(HWY 20) to Manning Park(HWY 3)", "49.0627761160001", "-120.782804412", "1239605"}
    };

    public static double MERGED_SEGMENT_MAX_VALUE = 106.0;

    public static double[][] segmentThresholds = {
            {0.0, 1.0}, // greater than or equal, less than
            {1.0, 1.5},
            {1.5, 2.0},
            {2.0, 2.5},
            {2.5, 3.0},
            {3.0, 4.0},
            {4.0, 5.0},
            {5.0, 10.0},
            {10.0, 15.0},
            {15.0, 20.0},
            {20.0, 25.0},
            {25.0, 30.0},
            {30.0, 40.0},
            {40.0, 50.0},
            {50.0, Double.MAX_VALUE}
    };

    public static void main(String[] args) {
        String[][] values = SectionData.sectionBreaks;

        for (String[] s: values) {
            System.out.println(Arrays.toString(s));
        }

    }

}
