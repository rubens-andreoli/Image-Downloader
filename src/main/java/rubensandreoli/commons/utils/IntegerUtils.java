package rubensandreoli.commons.utils;

public class IntegerUtils {
    
    /**
     * Parses the string argument as a signed decimal integer.
     * Convenience method for {@code Integer#parseInt()} that doesn't
     * throw {@code NumberFormatException}.
     * 
     * @param value a {@code String} containing the {@code int} to be parsed
     * @return the integer value or {@literal 0} if exception is thrown
     */
    public static int parseInteger(String value){
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException ex){
            return 0;
        }
    }
    
    public static int getRandomBetween(int min, int max){
        if(max < min) max = min;
        return (int) (Math.random() * (max - min)) + min;
    }

}
