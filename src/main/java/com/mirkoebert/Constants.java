package com.mirkoebert;

import lombok.experimental.UtilityClass;

/**
 * Constants used throughout the application.
 */
@UtilityClass
public class Constants {

    /**
     * Unique identifier for the user.
     */
    public static final String ME = "103256477727391736832";
    
    public static final short HCP_Epsilon = 1;
    
    /**
     * Enum values for login types.
     */
    public enum LoginType {
        PRIMARY,
        SECONDARY
    }
}
