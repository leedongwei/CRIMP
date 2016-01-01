package com.nusclimb.live.crimp.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class contains helper method for CRIMP. All methods must be 
 * public static.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 *
 */
public class Helper {
    /**
     * Pair class for storing pair of strings.
     */
    public static class StringPair{
        public String first;
        public String second;

        public StringPair(String first, String second){
            this.first = first;
            this.second = second;
        }
    }

    /*=========================================================================
     * String mapping in CRIMP
     *=======================================================================*/
    // Map containing alias of names for each round.
    private static final Map<String, StringPair> roundMap;
    static {
        Map<String, StringPair> aMap = new HashMap<>() ;
        aMap.put("U17M Qualifier", new StringPair("U17M", "UMQ"));
        aMap.put("U17M Final", new StringPair("U17M", "UMF"));

        aMap.put("U17W Qualifier", new StringPair("U17W", "UWQ"));
        aMap.put("U17W Final", new StringPair("U17W", "UWF"));

        aMap.put("NM Qualifier", new StringPair("NM", "NMQ"));
        aMap.put("NM Final", new StringPair("NM", "NMF"));

        aMap.put("NW Qualifier", new StringPair("NW", "NWQ"));
        aMap.put("NW Final", new StringPair("NW", "NWF"));

        aMap.put("IM Qualifier", new StringPair("IM", "IMQ"));
        aMap.put("IM Final", new StringPair("IM", "IMF"));

        aMap.put("IW Qualifier", new StringPair("IW", "IWQ"));
        aMap.put("IW Final", new StringPair("IW", "IWF"));

        aMap.put("OM Qualifier", new StringPair("OM", "OMQ"));
        aMap.put("OM Final", new StringPair("OM", "OMF"));

        aMap.put("OW Qualifier", new StringPair("OW", "OWQ"));
        aMap.put("OW Final", new StringPair("OW", "OWF"));

        roundMap = Collections.unmodifiableMap(aMap);
    }

    /**
     * Convert to round id given full round name.
     *
     * @param fullRoundName Full name of round.
     * @return Id of round.
     */
    public static String toIdRound(String fullRoundName){
        StringPair synonyms = roundMap.get(fullRoundName);
        if(synonyms != null)
            return synonyms.first;
        else
            return null;
    }

    /**
     * Convert to round name suitable for server side.
     *
     * @param fullRoundName Full name of round.
     * @return Name of round suitable for server.
     */
    public static String toServerRound(String fullRoundName){
        StringPair synonyms = roundMap.get(fullRoundName);
        if(synonyms != null)
            return synonyms.second;
        else
            return null;
    }

    /**
     * Parse a route name to 2 digit String.
     *
     * @param routeFullName Route name to parse.
     * @return Parsed route name
     */
    public static String parseRoute(String routeFullName){
        String[] temp = routeFullName.split(" ");
        if(temp[1].length() < 2) {
            return "0" + temp[1];
        }
        else{
            return temp[1];
        }
    }
	
	
	
	/*=========================================================================
	 * String/int generation
	 *=======================================================================*/
    /**
     *  Variable for generateViewId()
     */
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in View.setId(int).
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    /**
     * Allowed char for generating random strings.
     */
    private static char[] symbols;
    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ch++)
            tmp.append(ch);
        for (char ch = 'a'; ch <= 'z'; ch++)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();
    }

    private final static Random random = new Random();

    /**
     * Returns a random alpha numeric String of length n.
     * @param n Length of string to generate.
     * @return Random alpha numeric String.
     */
    public static String nextAlphaNumeric(int n) {
        StringBuilder sb = new StringBuilder();

        for (int idx = 0; idx < n; idx++)
            sb.append(symbols[random.nextInt(symbols.length)]);

        return sb.toString();
    }

    /**
     * Remove all non-alphanumeric characters from {@code string}.
     * @param string text to perform operation on.
     * @return {@code string} with all non-alphanumeric characters removed.
     */
    public static String toAlphaNumeric(String string){
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<string.length(); i++){
            if(isAlphaNumeric(string.charAt(i))){
                sb.append(string.charAt(i));
            }
        }

        return sb.toString();
    }

    /**
     * Check if {@code c} is an alphanumeric character. An alphanumeric
     * character is defined as a character in the set comprise of A-Z
     * (both upper and lower case) and 0-9.
     * @param c {@code char} to check.
     * @return if {@code c} is alphanumeric
     */
    public static boolean isAlphaNumeric(char c){
        return ( ('a'<=c && c<='z') || ('A'<=c && c<='Z') || ('0'<=c && c<='9')  );
    }

	
	
	
	/*=========================================================================
	 * Other helper methods
	 *=======================================================================*/
    /**
     * Helper method to convert int[] to List.
     *
     * @param primitive int[] to convert.
     * @return Converted List.
     */
    public static List<Integer> primitiveToList(int[] primitive){
        List<Integer> myList = new ArrayList<>();

        for(int i : primitive){
            myList.add(i);
        }

        return myList;
    }

    /**
     * Helper method to convert String[] to List.
     *
     * @param primitive String[] to convert.
     * @return Converted List.
     */
    public static List<String> primitiveToList(String[] primitive){
        List<String> myList = new ArrayList<>();

        for(String i : primitive){
            myList.add(i);
        }

        return myList;
    }

    /**
     * Helper method to convert InputStream to String. Does not close stream.
     * Credit goes to http://stackoverflow.com/a/5445161/3733616
     *
     * @param is InputStream to convert
     * @return String of stream content.
     */
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Get current time in dd MMM, HH:mm:ss format.
     *
     * @return Current time as string.
     */
    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm:ss", Locale.US);

            return dateFormat.format(new Date());
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
