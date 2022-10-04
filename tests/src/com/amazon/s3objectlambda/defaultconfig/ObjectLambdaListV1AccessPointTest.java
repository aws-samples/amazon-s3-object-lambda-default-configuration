package com.amazon.s3objectlambda.defaultconfig;

import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import software.amazon.awssdk.services.s3.model.EncodingType;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

@Test(groups = "listV1AccessPoint", dependsOnGroups = {"setup"})
public class ObjectLambdaListV1AccessPointTest extends ObjectLambdaAccessPointTest {

    private void assertSuccessfulResponse(ListObjectsRequest listObjectsRequestOLAP,
                                          ListObjectsRequest listObjectsRequestS3) {
        try {
            ListObjectsResponse object = s3Client.listObjects(listObjectsRequestOLAP);
            ListObjectsResponse originalObject = s3Client.listObjects(listObjectsRequestS3);
            Assert.assertTrue(object.sdkHttpResponse().isSuccessful());
            // Check if the request is equal to one without OL
            Assert.assertTrue(object.equalsBySdkFields(originalObject));
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1Simple() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "apply max-keys=1, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1MaxKeys1() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).maxKeys(1).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).maxKeys(1).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "apply max-keys=0, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1MaxKeys0() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).maxKeys(0).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).maxKeys(0).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "apply max-keys=3, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1MaxKeysBiggerThanActualKeys() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).maxKeys(3).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).maxKeys(3).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + ",upload 2 objects, apply max-keys=2, verify if the status code is 200" +
            " and the content is equal to a call without OL.")
    public void listObjectsV1MaxKeysEqualsActualKeys() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).maxKeys(2).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).maxKeys(2).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the an empty bucket objects,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1EmptyBucket() {
        // setup
        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "use the / delimiter, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1Delimiter() {
        // setup
        String objectKey1 = "sample.jpg";
        String objectKey2 = "photos/2006/January/sample.jpg";
        String objectKey3 = "photos/2006/February/sample2.jpg";
        String objectKey4 = "photos/2006/February/sample3.jpg";
        String objectKey5 = "photos/2006/February/sample4.jpg";

        setupResources(data, objectKey1, objectKey2, objectKey3, objectKey4, objectKey5);

        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).delimiter("/").build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).delimiter("/").build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2, objectKey3, objectKey4, objectKey5);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list an empty bucket objects,"
            + "use the / delimiter, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1DelimiterOnEmptyBucket() {
        // setup
        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).delimiter("/").build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).delimiter("/").build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "upload 6 objects and use max-keys=2 alongside marker, verify if the status code is 200 and the content" +
            " is equal to a call without OL.")
    public void listObjectsV1Marker() {
        int totalObjectsCount = 6;
        int maxKeys = 2;
        String objectPrefix = "sample";
        // setup
        for (int i = 0; i < totalObjectsCount; i++) {
            String objectKey = objectPrefix + i + ".jpg";
            setupResource(objectKey, data);
        }

        var listObjectsV1Request = ListObjectsRequest.builder()
                .bucket(olapArn)
                .marker("sample")
                .maxKeys(maxKeys);
        var listObjectsV1RequestS3 = ListObjectsRequest.builder()
                .bucket(s3BucketName)
                .marker("sample")
                .maxKeys(maxKeys);

        // assert
        for (int i = 0; i < totalObjectsCount; i+=maxKeys) {
            var OLAPRequest = listObjectsV1Request.marker(objectPrefix + i + ".jpg").build();
            var S3Request = listObjectsV1RequestS3.marker(objectPrefix + i + ".jpg").build();
            assertSuccessfulResponse(OLAPRequest, S3Request);
        }
        // cleanup
        for (int i = 0; i < totalObjectsCount; i++) {
            String objectKey = "sample" + i + ".jpg";
            cleanupResource(objectKey);
        }
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + "use prefix, verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1Prefix() {
        // setup
        String objectKey1 = "sa-1";
        String objectKey2 = "sa-2";
        String objectKey3 = "ba-1";
        String objectKey4 = "ba-2";

        setupResources(data, objectKey1, objectKey2, objectKey3, objectKey4);

        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).prefix("ba").build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).prefix("ba").build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2, objectKey3, objectKey4);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects"
            + ", provide wrong encoding-type, verify if the status code is 200 and the content is equal to a call " +
            "without OL.")
    public void listObjectsV1EncodingTypeInvalidValue() {
        // setup
        String objectKey1 = "sample1";
        String objectKey2 = "sample2";

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request =
                ListObjectsRequest.builder().bucket(olapArn).encodingType("WrongEncodingType").build();
        try {
            s3Client.listObjects(listObjectsV1Request);
            Assert.fail("Invalid encoding type. Expecting 400 error");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_BAD_REQUEST);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
        finally {
            // cleanup
            cleanupResources(objectKey1, objectKey2);
        }
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + ", use encoding-type=url, verify if the status code is 200 and the content is equal to a call without "
            + "OL.")
    public void listObjectsV1EncodingTypeURL() {
        // setup
        String objectKey1 = "sample1";
        String objectKey2 = "sample2";

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request =
                ListObjectsRequest.builder().bucket(olapArn).encodingType(EncodingType.URL).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).encodingType(EncodingType.URL).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + ", upload objects with special characters as keys,verify if the status code is 200 and the content is "
            + "equal to a call without OL.")
    public void listObjectsV1SpecialCharacterNotEncoded() {
        // setup
        String objectKey1 = "sa&m'pl>e1";
        String objectKey2 = "sam&'pl>e2";

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request =
                ListObjectsRequest.builder().bucket(olapArn).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects,"
            + ", upload objects with special characters as keys, verify if the status code is 200 and the content is "
            + "equal to a call without OL when using encoding-type=url.")
    public void listObjectsV1SpecialCharacterEncoded() {
        // setup
        String objectKey1 = "sa&m'pl>e1";
        String objectKey2 = "sam&'pl>e2";

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request =
                ListObjectsRequest.builder().bucket(olapArn).encodingType(EncodingType.URL).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).encodingType(EncodingType.URL).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects and provide a correct expected owner,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1ExpectedOwnerPositive() {
        // setup
        String objectKey1 = UUID.randomUUID().toString();
        String objectKey2 = UUID.randomUUID().toString();

        setupResources(data, objectKey1, objectKey2);

        var listObjectsV1Request =
                ListObjectsRequest.builder().bucket(olapArn).expectedBucketOwner(sdkHelper.getAWSAccountID(region)).build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).expectedBucketOwner(sdkHelper.getAWSAccountID(region)).build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }

    @Parameters()
    @Test(description = "Calling OLAP to list the bucket objects and provide a incorrect expected owner,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void listObjectsV1ExpectedOwnerNegative() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var listObjectsV1OLAP = ListObjectsRequest.builder()
                .bucket(olapArn)
                .expectedBucketOwner(DUMMY_ACCOUNT_ID)
                .build();
        // assert
        try {
            s3Client.listObjects(listObjectsV1OLAP);
            Assert.fail("expectedBucketOwnerNegative expecting a 403 errors");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_FORBIDDEN);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        } finally {
            // cleanup
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

        var listObjectsV1Request = ListObjectsRequest.builder().bucket(olapArn).requestPayer("requester").build();
        var listObjectsV1RequestS3 = ListObjectsRequest.builder().bucket(s3BucketName).requestPayer("requester").build();
        // assert
        assertSuccessfulResponse(listObjectsV1Request, listObjectsV1RequestS3);
        // cleanup
        cleanupResources(objectKey1, objectKey2);
    }
}
