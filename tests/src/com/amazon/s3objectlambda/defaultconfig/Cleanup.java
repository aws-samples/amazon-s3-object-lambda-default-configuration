package com.amazon.s3objectlambda.defaultconfig;

import static com.amazon.s3objectlambda.defaultconfig.KeyConstants.*;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.cloudformation.model.DeleteStackRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.waiters.CloudFormationWaiter;

@Test(groups = "cleanup")
public class Cleanup {

    @Parameters({"region"})
    @Test(groups = "cleanup", dependsOnGroups = {"setup", "accessPoint"}, alwaysRun = true,
          description = "Delete the created stack and assert it's deleted")
    public void deletStack(ITestContext context, String region) {
        var sdkHelper = new SdkHelper();
        String stackName = (String) context.getAttribute(STACK_NAME_KEY);
        var cloudFormationClient = sdkHelper.getCloudFormationClient(region);
        DeleteStackRequest deleteRequest = DeleteStackRequest.builder().stackName(stackName).build();
        cloudFormationClient.deleteStack(deleteRequest);
        CloudFormationWaiter waiter = cloudFormationClient.waiter();
        DescribeStacksRequest describeStacksRequest = DescribeStacksRequest.builder().stackName(stackName).build();
        var waiterResponse = waiter.waitUntilStackDeleteComplete(describeStacksRequest);
        Assert.assertFalse(waiterResponse.matched().response().isPresent());
    }

    @AfterClass()
    @Test(description= "Suppose to fail")
    public void falseTest(){
        Assert.assertEquals(true,true);
        Assert.assertEquals(true,true);

    }
}
