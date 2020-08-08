/*
 * Copyright (C) 2020 Rubens A. Andreoli Jr.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package rubensandreoli.commons.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StringUtils {
    
    public static final int BAG_OF_WORDS = 1;
    public static final int LEVENSHTEIN = 2;
    public static final int DEMERAU = 3;
    
    private StringUtils(){}
    
    public static double compare(String s1, String s2, int mode){
        switch(mode){
            case BAG_OF_WORDS:
                return bagOfWords(s1, s2);
            case LEVENSHTEIN:
                return levenshtein(s1, s2);
            case DEMERAU:
                return demerau(s1, s2);
            default:
                return 0;
       }
    }
    
    public static double compare(String s1, String s2){
        return compare(s1, s2, LEVENSHTEIN);
    }
    
    public static long getStringSize(String str){
        if(str == null) return 0L;
        return str.length()*Character.BYTES;
    }

    // <editor-fold defaultstate="collapsed" desc=" COMPARE MODES "> 
    private static double bagOfWords(String s1, String s2) {
	if(s1.equals(s2)) return 1;
	Set<String> w1 = splitWords(s1);
	Set<String> w2 = splitWords(s2);
	int similar = 0;
	for (String word : w1) {
	    if (w2.contains(word)) similar++;
	}
	return (similar / (double)Math.max(w1.size(), w2.size()));
    }
    
    public static Set<String> splitWords(String s){
	s = s.replaceAll("('s)", "") //remove 's
            .replaceAll("[?!#%'(),]", "") //remove ? ! # % ' ( ) ,
            .replaceAll("[_\\-.]", " ") //replace _ - . for space
            .replaceAll("[\\s]{2,}", " ") // replace 2 consecutive spaces for space
            .replaceAll("(?<=[a-z])(?=[A-Z][a-z])", " ") //add space on camelcase without replacing
            .toLowerCase();
	String[] tokens = s.split(" ");
	Set<String> words = new HashSet<>();
	for (String token : tokens) {
	    for(int i=1; !words.add(token+i); i++);
	}
	return words;
    }

    private static double levenshtein(String s1, String s2) {
	String longer = s1, shorter = s2;
	if (s1.length() < s2.length()) {
	    longer = s2;
	    shorter = s1;
	}
	int longerLength = longer.length();
	if (longerLength == 0) return 1.0;
	return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(String s1, String s2) {
	s1 = s1.toLowerCase();
	s2 = s2.toLowerCase();
	int[] costs = new int[s2.length() + 1];
	for (int i = 0; i <= s1.length(); i++) {
	    int lastValue = i;
	    for (int j = 0; j <= s2.length(); j++) {
		if (i == 0) {
		    costs[j] = j;
		} else if (j > 0) {
		    int newValue = costs[j - 1];
		    if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
			newValue = Math.min(Math.min(newValue, lastValue),
				costs[j]) + 1;
		    }
		    costs[j - 1] = lastValue;
		    lastValue = newValue;
		}
	    }
	    if (i > 0) costs[s2.length()] = lastValue;
	}
	return costs[s2.length()];
    }
    
    
    private static double demerau(String s1, String s2) {
        if (s1.equals(s2)) return 1;
	int s1Length = s1.length();
	int s2Length = s2.length();
        int inf = s1Length + s2Length;
        HashMap<Character, Integer> da = new HashMap<Character, Integer>();
        for(int d=0; d<s1Length; d++) da.put(s1.charAt(d), 0);
        for(int d=0; d<s2Length; d++) da.put(s2.charAt(d), 0);
        int[][] h = new int[s1Length+2][s2Length+2];
        for(int i=0; i<=s1Length; i++) {
            h[i+1][0] = inf;
            h[i+1][1] = i;
        }
        for(int j=0; j<=s2Length; j++) {
            h[0][j+1] = inf;
            h[1][j+1] = j;

        }
        for(int i=1; i<=s1Length; i++) {
            int db = 0;
            for(int j=1; j<=s2Length; j++) {
                int i1 = da.get(s2.charAt(j-1));
                int j1 = db;
                int cost = 1;
                if(s1.charAt(i-1) == s2.charAt(j-1)) {
                    cost = 0;
                    db = j;
                }
                h[i+1][j+1] = min(
                        h[i][j] + cost,
                        h[i+1][j] + 1,
                        h[i][j+1] + 1,
                        h[i1][j1] + (i-i1-1) + 1 + (j-j1-1));
            }

            da.put(s1.charAt(i-1), i);
        }
        return 1-((h[s1Length+1][s2Length+1])/(double)inf);
    }

    private static int min(int a, int b, int c, int d) {
        return Math.min(a, Math.min(b, Math.min(c, d)));
    }
    // </editor-fold>

    public static int getNthIndexOf(String str, String regex, int n){
        return getNthIndexOf(str, regex, n, false);
    }
    
    public static int getNthIndexOf(String str, String regex, int n, boolean reverse){
        if(reverse) str = new StringBuilder(str).reverse().toString();
        String[] tokens = str.split(regex);
        if(tokens.length <= n) return -1;
        
        int index = n-1; //add regex previous occurances
        for (int i = 0; i < n; i++) {
            index += tokens[i].length();
        }
        return reverse? str.length()-index:index ;
    }
    
}
