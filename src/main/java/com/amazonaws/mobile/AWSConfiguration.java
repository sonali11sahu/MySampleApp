//
// Copyright 2017 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to 
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.17
//
package com.amazonaws.mobile;

import com.amazonaws.mobilehelper.config.AWSMobileHelperConfiguration;
import com.amazonaws.regions.Regions;

/**
 * This class defines constants for the developer's resource
 * identifiers and API keys. This configuration should not
 * be shared or posted to any public source code repository.
 */
public class AWSConfiguration {
    // AWS MobileHub user agent string
    public static final String AWS_MOBILEHUB_USER_AGENT =
        "MobileHub 38f02d7e-552a-4f56-9784-e1b0ac750220 aws-my-sample-app-android-v0.17";
    // AMAZON COGNITO
    public static final Regions AMAZON_COGNITO_REGION =
      Regions.fromName("us-west-2");
    public static final String  AMAZON_COGNITO_IDENTITY_POOL_ID =
        "us-west-2:5a21b3ef-f235-4365-8076-3cece5857705";
    public static final Regions AMAZON_DYNAMODB_REGION =
       Regions.fromName("us-west-1");
    public static final String AMAZON_COGNITO_USER_POOL_ID =
        "us-west-2_KzXTCLS5j";
    public static final String AMAZON_COGNITO_USER_POOL_CLIENT_ID =
        "5020a1bbvdi4tk8bisv2g03si3";
    public static final String AMAZON_COGNITO_USER_POOL_CLIENT_SECRET =
        "1djro453lq1ado225h317eov0uo11jin7mag6p5hcknt2q39lnju";

    private static final AWSMobileHelperConfiguration helperConfiguration = new AWSMobileHelperConfiguration.Builder()
        .withCognitoRegion(AMAZON_COGNITO_REGION)
        .withCognitoIdentityPoolId(AMAZON_COGNITO_IDENTITY_POOL_ID)
        .withCognitoUserPool(AMAZON_COGNITO_USER_POOL_ID,
            AMAZON_COGNITO_USER_POOL_CLIENT_ID, AMAZON_COGNITO_USER_POOL_CLIENT_SECRET)
        .build();
    /**
     * @return the configuration for AWSKit.
     */
    public static AWSMobileHelperConfiguration getAWSMobileHelperConfiguration() {
        return helperConfiguration;
    }
}