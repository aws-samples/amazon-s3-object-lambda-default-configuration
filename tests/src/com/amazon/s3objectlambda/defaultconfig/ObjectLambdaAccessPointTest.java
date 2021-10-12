package com.amazon.s3objectlambda.defaultconfig;

import static com.amazon.s3objectlambda.defaultconfig.KeyConstants.*;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Test(groups = "accessPoint", dependsOnGroups = {"setup"})
public class ObjectLambdaAccessPointTest {

    public final String tempData = "1234567890".repeat(1000);
    public final String tempObjectkey = UUID.randomUUID().toString();

    @Parameters({"region", "s3BucketName"})
    @BeforeMethod()
    void setupResource(String region, String s3BucketName) {
        var sdkHelper = new SdkHelper();
        var s3Client = sdkHelper.getS3Client(region);
        s3Client.putObject(builder -> builder.bucket(s3BucketName).key(tempObjectkey).build(),
                RequestBody.fromString(tempData, StandardCharsets.UTF_8));
    }

    @Parameters({"region", "s3BucketName"})
    @AfterMethod()
    void cleanupResource(String region, String s3BucketName) {
        var sdkHelper = new SdkHelper();
        var s3Client = sdkHelper.getS3Client(region);
        s3Client.deleteObject(builder -> builder.bucket(s3BucketName).key(tempObjectkey).build());
    }

    @Parameters({"region"})
    @Test(description = "Verify the happy path of Cloudformation template setup "
            + "obtaining an object through s3ol access point")
    public void getObjectSimple(ITestContext context, String region) {
        String olAccessPointName = (String) context.getAttribute(OL_AP_NAME_KEY);
        var sdkHelper = new SdkHelper();
        var s3Client = sdkHelper.getS3Client(region);
        String olapArn = sdkHelper.getOLAccessPointArn(region, olAccessPointName);
        var getObjectRequest = GetObjectRequest.builder().bucket(olapArn).key(tempObjectkey).build();
        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(getObjectRequest);
        try {
            Assert.assertTrue(object.response().sdkHttpResponse().isSuccessful());
            InputStream stream = new ByteArrayInputStream(object.readAllBytes());
            Assert.assertEquals(stream.readAllBytes(), tempData.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Assert.fail("Expected and actual result do not match");
        }
    }
}
