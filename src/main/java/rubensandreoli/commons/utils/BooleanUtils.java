package rubensandreoli.commons.utils;

import rubensandreoli.commons.exceptions.CastException;

public class BooleanUtils {
    
    private BooleanUtils(){};
    
    public static boolean parseBoolean(String s) throws CastException {
        switch(s.toLowerCase()){
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new CastException();
        }
    }
    
}
