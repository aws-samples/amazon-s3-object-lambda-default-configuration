package com.amazon.s3objectlambda.defaultconfig;

/**
 * Holds Constant key value for IContext to pass around resources name between test groups.
 */
public final class KeyConstants {
    /**
     * IContext Key for Cloudformation Stack name.
     */
    public static final String STACK_NAME_KEY = "STACK_NAME_KEY";
    /**
     * IContext Key for support Access Point name.
     */
    public static final String SUPPORT_AP_NAME_KEY = "SUPPORT_AP_NAME_KEY";
    /**
     * IContext Key for Object Lambda Access Point name.
     */
    public static final String OL_AP_NAME_KEY = "OL_AP_NAME_KEY";

    private KeyConstants() {
    }
}
