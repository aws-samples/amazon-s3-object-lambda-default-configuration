package com.amazon.s3objectlambda.defaultconfig;

import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.s3.model.EncodingType;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Objects;
import java.util.UUID;

@Test(groups = "listV2AccessPoint", dependsOnGroups = {"setup"})
public class ObjectLambdaListV2AccessPointTest extends ObjectLambdaAccessPointTest {

    private void assertSuccessfulResponse(ListObjectsV2Request listObjectsRequestOLAP,
                                          ListObjectsV2Request listObjectsRequestS3) {
        try {
            ListObjectsV2Response object = s3Client.listObjectsV2(listObjectsRequestOLAP);
            ListObjectsV2Response originalObject = s3Client.listObjectsV2(listObjectsRequestS3);
            Assert.assertTrue(object.sdkHttpResponse().isSuccessful());
            // Check if the request is equal to one without OL
            Assert.assertTrue(areEqualListObjectV2Responses(object, originalObject));
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
    }

    private boolean areEqualListObjectV2Responses(Object obj1, Object obj2) {

        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        if (!(obj1 instanceof ListObjectsV2Response response1) || !(obj2 instanceof ListObjectsV2Response response2)) {
            return false;
        }
        return Objects.equals(response2.isTruncated(), response1.isTruncated()) && response2.hasContents() == response1.hasContents()
            && Objects.equals(response2.contents(), response1.contents()) && Objects.equals(response2.name(), response1.name())
            && Objects.equals(response2.prefix(), response1.prefix()) && Objects.equals(response2.delimiter(), response1.delimiter())
            && Objects.equals(response2.maxKeys(), response1.maxKeys()) && response2.hasCommonPrefixes() == response1.hasCommonPrefixes()
            && Objects.equals(response2.commonPrefixes(), response1.commonPrefixes())
            && Objects.equals(response2.encodingTypeAsString(), response1.encodingTypeAsString())
            && Objects.equals(response2.keyCount(), response1.keyCount()) && Objects.equals(response2.continuationToken(), response1.continuationToken())
            && Objects.equals(response2.startAfter(), response1.startAfter());
    }

    private void assertSuccessfulResponseContents(ListObjectsV2Request listObjectsRequestOLAP,
                                          ListObjectsV2Request listObjectsRequestS3) {
        try {
            ListObjectsV2Response object = s3Client.listObjectsV2(listObjectsRequestOLAP);
            ListObjectsV2Response originalObject = s3Client.listObjectsV2(listObjectsRequestS3);
            Assert.assertTrue(object.sdkHttpResponse().isSuccessful());
            // Check if the request is equal to one without OL
            Assert.assertEquals(object.contents().toString(), originalObject.contents().toString());
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2Simple() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV2Request = ListObjectsV2Request.builder().bucket(olapArn).build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "apply max-keys=1, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2MaxKeys1() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV2Request = ListObjectsV2Request.builder().bucket(olapArn).maxKeys(1).build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).maxKeys(1).build();
        // assert
        assertSuccessfulResponseContents(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "apply max-keys=0, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2MaxKeys0() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV2Request = ListObjectsV2Request.builder().bucket(olapArn).maxKeys(0).build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).maxKeys(0).build();
        // assert
        assertSuccessfulResponseContents(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the an empty bucket objects,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2EmptyBucket() {
        // setup
        var listObjectsV2Request = ListObjectsV2Request.builder().bucket(olapArn).build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "use the / delimiter, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2Delimiter() {
        // setup
        String objectKey1 = "sample.jpg";
        String objectKey2 = "photos/2006/January/sample.jpg";
        String objectKey3 = "photos/2006/February/sample2.jpg";
        String objectKey4 = "photos/2006/February/sample3.jpg";
        String objectKey5 = "photos/2006/February/sample4.jpg";

        setupResources(data, objectKey1, objectKey2, objectKey3, objectKey4, objectKey5);

        var listObjectsV2Request = ListObjectsV2Request.builder().bucket(olapArn).delimiter("/").build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).delimiter("/").build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2, objectKey3, objectKey4, objectKey5);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list an empty bucket objects,"
            + "use the / delimiter, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2DelimiterOnEmptyBucket() {
        // setup
        var listObjectsV2Request = ListObjectsV2Request.builder().bucket(olapArn).delimiter("/").build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).delimiter("/").build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "use prefix, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2Prefix() {
        // setup
        String objectKey1 = "sa-1";
        String objectKey2 = "sa-2";
        String objectKey3 = "ba-1";
        String objectKey4 = "ba-2";

        setupResources(data, objectKey1, objectKey2, objectKey3, objectKey4);

        var listObjectsV2Request = ListObjectsV2Request.builder().bucket(olapArn).prefix("ba").build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).prefix("ba").build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2, objectKey3, objectKey4);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects"
            + ", provide wrong encoding-type, verify if the status code is 200 and the content is equal to a call " +
            "without OL.")
    public void listObjectsV2EncodingTypeInvalidValue() {
        // setup
        String objectKey1 = "sample1";
        String objectKey2 = "sample2";

        setupResource(objectKey1, data);
        setupResource(objectKey2, data);

        var listObjectsV2Request =
                ListObjectsRequest.builder().bucket(olapArn).encodingType("WrongEncodingType").build();
        try {
            s3Client.listObjects(listObjectsV2Request);
            Assert.fail("Invalid encoding type. Expecting 400 error");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_BAD_REQUEST);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + ", use encoding-type=url, verify if the status code is 200 and the content is equal to a call without "
            + "OL.")
    public void listObjectsV2EncodingTypeURL() {
        // setup
        String objectKey1 = "sample1";
        String objectKey2 = "sample2";

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV2Request =
                ListObjectsV2Request.builder().bucket(olapArn).encodingType(EncodingType.URL).build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).encodingType(EncodingType.URL).build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + ", upload objects with special characters as keys,verify if the status code is 200 and the content is "
            + "equal to a call without OL.")
    public void listObjectsV2SpecialCharacterNotEncoded() {
        // setup
        String objectKey1 = "sample1";
        String objectKey2 = "sample2";

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV2Request =
                ListObjectsV2Request.builder().bucket(olapArn).build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + ", upload objects with special characters as keys, verify if the status code is 200 and the content is "
            + "equal to a call without OL when using encoding-type=url.")
    public void listObjectsV2SpecialCharacterEncoded() {
        // setup
        String objectKey1 = "sa&m'pl>e1";
        String objectKey2 = "sam&'pl>e2";

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV2Request =
                ListObjectsV2Request.builder().bucket(olapArn).encodingType(EncodingType.URL).build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).encodingType(EncodingType.URL).build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects and provide a correct expected owner,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2ExpectedOwnerPositive() {
        // setup
        String objectKey1 = "sa&m'pl>e1";
        String objectKey2 = "sa&m'pl>e1";

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV2Request =
                ListObjectsV2Request.builder().bucket(olapArn).expectedBucketOwner(sdkHelper.getAWSAccountID(region)).build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).expectedBucketOwner(sdkHelper.getAWSAccountID(region)).build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects and provide a incorrect expected owner,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV2ExpectedOwnerNegative() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var listObjectsV2OLAP = ListObjectsV2Request.builder()
                .bucket(olapArn)
                .expectedBucketOwner(DUMMY_ACCOUNT_ID)
                .build();
        // assert
        try {
            s3Client.listObjectsV2(listObjectsV2OLAP);
            Assert.fail("expectedBucketOwnerNegative expecting a 403 errors");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_FORBIDDEN);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects, "
            + "use fetch-owner=true, verify if the status code is 200 and the Owner is provided")
    public void listObjectsV2FetchOwnerTrue() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);

        var listObjectsV2Request =
                ListObjectsV2Request.builder().bucket(olapArn).fetchOwner(true).build();

        ListObjectsV2Response object = s3Client.listObjectsV2(listObjectsV2Request);

        // assert
        Assert.assertTrue(object.sdkHttpResponse().isSuccessful());
        Assert.assertNotNull(object.contents().get(0).owner(), "Owner was not fetched.");

        // cleanup
        cleanupResource(objectKey);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects, "
            + "use fetch-owner=false, verify if the status code is 200 and the Owner is not provided")
    public void listObjectsV2FetchOwnerFalse() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);

        var listObjectsV2Request =
                ListObjectsV2Request.builder().bucket(olapArn).fetchOwner(false).build();

        ListObjectsV2Response object = s3Client.listObjectsV2(listObjectsV2Request);

        // assert
        Assert.assertTrue(object.sdkHttpResponse().isSuccessful());
        Assert.assertNull(object.contents().get(0).owner(), "Owner should not be fetched.");

        // cleanup
        cleanupResource(objectKey);
    }



    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "upload 6 objects and use max-keys=2 and continuation-token, verify if the status code is 200 and the "
            + "content is equal to a call without OL.")
    public void listObjectsV2ContinuationToken() {
        int totalObjectsCount = 6;
        int maxKeys = 2;
        String objectPrefix = "sample";
        // setup
        for (int i = 0; i < totalObjectsCount; i++) {
            String objectKey = objectPrefix + i + ".jpg";
            setupResource(objectKey, data);
        }

        ListObjectsV2Request listObjectsReqManualOLAP = ListObjectsV2Request.builder()
                .bucket(olapArn)
                .maxKeys(maxKeys)
                .build();

        ListObjectsV2Request listObjectsReqManualS3 = ListObjectsV2Request.builder()
                .bucket(s3BucketName)
                .maxKeys(maxKeys)
                .build();

        // assert
        boolean done = false;
        while (!done) {
            ListObjectsV2Response listObjResponseOLAP = s3Client.listObjectsV2(listObjectsReqManualOLAP);
            ListObjectsV2Response listObjResponseS3 = s3Client.listObjectsV2(listObjectsReqManualS3);

            // Assert that the same amount of contents have been received
            Assert.assertEquals(listObjResponseOLAP.contents().size(), listObjResponseS3.contents().size());

            // Assert that the contents are the same
            for (int i = 0; i < listObjResponseOLAP.contents().size(); i++) {
                Assert.assertEquals(listObjResponseOLAP.contents().get(i).toString(), listObjResponseS3.contents().get(i).toString());
            }

            if (listObjResponseOLAP.nextContinuationToken() == null) {
                done = true;
            }

            listObjectsReqManualOLAP = listObjectsReqManualOLAP.toBuilder()
                    .continuationToken(listObjResponseOLAP.nextContinuationToken())
                    .build();

            listObjectsReqManualS3 = listObjectsReqManualS3.toBuilder()
                    .continuationToken(listObjResponseS3.nextContinuationToken())
                    .build();
        }
        // cleanup
        for (int i = 0; i < totalObjectsCount; i++) {
            String objectKey = "sample" + i + ".jpg";
            cleanupResource(objectKey);
        }
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects and use request-payer=requester,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void requestPayerPassedToS3() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV2Request = ListObjectsV2Request.builder().bucket(olapArn).requestPayer("requester").build();
        var listObjectsV2RequestS3 = ListObjectsV2Request.builder().bucket(s3BucketName).requestPayer("requester").build();
        // assert
        assertSuccessfulResponse(listObjectsV2Request, listObjectsV2RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }
}
