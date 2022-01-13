package com.amazon.s3objectlambda.defaultconfig;

import static com.amazon.s3objectlambda.defaultconfig.KeyConstants.*;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.ITestContext;
import software.amazon.awssdk.services.cloudformation.model.*;

import java.util.ArrayList;
import java.util.Arrays;


@Test(groups = "setup")
public class SetupCloudFormationTest {

    @BeforeClass(description = "Generate random name for Access Points and Stack name")
    public void generateName(ITestContext context) {
        var sdkHelper = new SdkHelper();
        context.setAttribute(STACK_NAME_KEY, sdkHelper.generateRandomResourceName(8));
        context.setAttribute(OL_AP_NAME_KEY, sdkHelper.generateRandomResourceName(8));
        context.setAttribute(SUPPORT_AP_NAME_KEY, sdkHelper.generateRandomResourceName(8));
    }

    @Parameters({"region", "templateUrl", "s3BucketName", "lambdaFunctionS3BucketName",
            "lambdaFunctionS3Key", "lambdaFunctionRuntime", "createNewSupportingAccessPoint", "lambdaVersion"})
    @Test(description = "Deploy the CloudFormation template to set up s3ol access point")
    @SuppressWarnings("checkstyle:parameternumber")
    public void deployStack(ITestContext context, String region, String templateUrl, String s3BucketName,
                            String lambdaFunctionS3BucketName, String lambdaFunctionS3Key, String lambdaFunctionRuntime,
                            String createNewSupportingAccessPoint, String lambdaVersion) {
        var sdkHelper = new SdkHelper();
        String stackName = (String) context.getAttribute(STACK_NAME_KEY);
        String olAccessPointName = (String) context.getAttribute(OL_AP_NAME_KEY);
        String supportingAccessPointName = (String) context.getAttribute(SUPPORT_AP_NAME_KEY);
        var cloudFormationClient = sdkHelper.getCloudFormationClient(region);
        ArrayList<Parameter> parameters = new ArrayList<>(Arrays.asList(
                sdkHelper.buildParameter("ObjectLambdaAccessPointName", olAccessPointName),
                sdkHelper.buildParameter("SupportingAccessPointName", supportingAccessPointName),
                sdkHelper.buildParameter("S3BucketName", s3BucketName),
                sdkHelper.buildParameter("LambdaFunctionS3BucketName", lambdaFunctionS3BucketName),
                sdkHelper.buildParameter("LambdaFunctionS3Key", lambdaFunctionS3Key),
                sdkHelper.buildParameter("LambdaFunctionRuntime", lambdaFunctionRuntime),
                sdkHelper.buildParameter("LambdaFunctionS3ObjectVersion", lambdaVersion),
                sdkHelper.buildParameter("CreateNewSupportingAccessPoint", createNewSupportingAccessPoint)));
        var createStackRequest = CreateStackRequest.builder()
                .templateURL(templateUrl)
                .stackName(stackName)
                .parameters(parameters)
                .capabilitiesWithStrings("CAPABILITY_NAMED_IAM")
                .build();
        cloudFormationClient.createStack(createStackRequest);
        var cloudFormationWaiter = cloudFormationClient.waiter();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder().stackName(stackName).build();
        //waiter.response will either return a response or throwable
        var waiterResponse = cloudFormationWaiter.waitUntilStackCreateComplete(describeStacksRequest);
        Assert.assertTrue(waiterResponse.matched().response().isPresent());
    }
}
