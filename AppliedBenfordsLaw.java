package benfordslaw;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

/*
    this class handles benford's law analysis on a data
    calculates the leading digit frequency of numbers (1-9)
    & compares them to expected distributions to detect anomalies often seen in fraudulent data
*/
public class AppliedBenfordsLaw {
    private Map<Integer, Integer> digit_freq_map = new HashMap<>(); // store freq of each leading digit
    private Map<Integer, Double> digit_percent_map = new HashMap<>(); // store %freq of each leading digit
    private final double[] benfordPercentages = {0.0, 30.1, 17.6, 12.5, 9.7, 7.9, 6.7, 5.8, 5.1, 4.6}; // an arr of expected benfords freqs
    private double chiSquared = 0.0; // for chi sqred test
    
    
    // analyzes a list of nums, finds freqs of leading digits, converts freqs to %freqs, returns a map of digits to their %freq.
    public Map<Integer, Double> applyBenford(List<Integer> numbers) {
        int total = 0; 
        // calculate freq of each leading digit
        for (int number : numbers) {
            int first_digit = getFirstDigit(number);
            digit_freq_map.put(first_digit, digit_freq_map.getOrDefault(first_digit, 0) + 1);
            total++;
        }
        // convert freqs to %
        for (int i = 1; i <= 9; i++) {
            int freq = digit_freq_map.getOrDefault(i, 0);
            double percentage = (double) freq * 100.0 / total;
            digit_percent_map.put(i, percentage);
        }
        return digit_percent_map;
    }
    
    
    // helper func to get a num's leading digit
    private int getFirstDigit(int number) {
        while (number >= 10) {
            number /= 10;
        }
        return number;
    }
    
    
    // cmps the calculated freqs with expected freqs & uses a chi-squared test to find anomalies
    // degrees of freedom = 8, p_val = 0.05, so the critical val must be within ~15.51 to not be fraudulent.
    public boolean compareWithBenford() {
        for (int i = 1; i <= 9; i++) {
            double observed = digit_percent_map.getOrDefault(i, 0.0);  // get observed % or 0 if dne
            double expected = benfordPercentages[i];  // get expected %
            if (expected > 0.0) { 
                chiSquared += Math.pow(observed - expected, 2) / expected;
            }
        }
        return chiSquared > 15.51;
    }
    
    
    // reset to analyze new data
    public void Clear() {
        digit_freq_map.clear();
        digit_percent_map.clear();
        chiSquared = 0.0;
    }
}
