package com.amazon.s3objectlambda.defaultconfig;

import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.Payer;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.utils.Md5Utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Test(groups = "headAccessPoint", dependsOnGroups = {"setup"})
public class ObjectLambdaHeadAccessPointTest extends ObjectLambdaAccessPointTest {

    private void assertSuccessfulResponse(HeadObjectRequest headObjectRequestOLAP, HeadObjectRequest headObjectRequestS3) {
        try {
            HeadObjectResponse object = s3Client.headObject(headObjectRequestOLAP);
            HeadObjectResponse originalObject = s3Client.headObject(headObjectRequestS3);
            Assert.assertTrue(object.sdkHttpResponse().isSuccessful());
            // Check if the request is equal to one without OL
            Assert.assertEquals(object.toString(), originalObject.toString());
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
    }

    @Parameters()
    @Test(description = "Calling OLAP to obtain the object,"
            + "verify if the status code is 200 and the content is equal to a call without OL.")
    public void headObjectSimple() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var headObjectRequest = HeadObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        var headObjectRequestS3 = HeadObjectRequest.builder().bucket(s3BucketName).key(objectKey).build();
        // assert
        assertSuccessfulResponse(headObjectRequest, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }

    @Parameters()
    @Test(description = "Calling OLAP HEAD_OBJECT to obtain the object headers,"
            + "verify if the status code is 404 and the headers are the same as from a simple S3 request")
    public void headObjectSimpleMissing() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        var headObjectRequest = HeadObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        // assert
        try {
            s3Client.headObject(headObjectRequest);
            Assert.fail("Request should throw 404");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_NOT_FOUND);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Parameters()
    @Test(description = "Calling OLAP HEAD_OBJECT to obtain the object headers,"
            + "verify if the status code is 200 and the contentLength header is correct")
    public void headObjectContentLength() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var headObjectRequest = HeadObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        // assert
        var response = s3Client.headObject(headObjectRequest);
        Assert.assertEquals(response.contentLength(), Long.valueOf(data.length()));
        Assert.assertTrue(response.sdkHttpResponse().isSuccessful());
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP HEAD_OBJECT with If-Match header(correct etag) to obtain the object headers, "
            + "verify if the status code is 200 and the headers are the same as from a simple S3 request")
    public void headObjectIfMatchPositive() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        var putObjectResponse = setupResource(objectKey, data);
        String eTag = putObjectResponse.eTag();
        var headObjectRequest = HeadObjectRequest.builder().bucket(olapArn).key(objectKey).ifMatch(eTag).build();
        var headObjectRequestS3 = HeadObjectRequest.builder().bucket(s3BucketName).key(objectKey).ifMatch(eTag).build();
        // assert
        assertSuccessfulResponse(headObjectRequest, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP HEAD_OBJECT with If-Modified-Since header(last modified time minus one second) " +
            "to obtain the temp object headers, verify if the status code is 200 and the headers are the same as from" +
            " a simple S3 request")
    public void headObjectIfModifiedSincePositive() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var responseInputStream = s3Client.getObject(builder -> builder
                .bucket(s3BucketName)
                .key(objectKey)
                .build());
        Instant createTime = responseInputStream.response().lastModified();
        Instant timeBeforeCreate = createTime.minusSeconds(1);
        var headObjectRequest = HeadObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .ifModifiedSince(timeBeforeCreate)
                .build();
        var headObjectRequestS3 = HeadObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .ifModifiedSince(timeBeforeCreate)
                .build();
        // assert
        assertSuccessfulResponse(headObjectRequest, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP HEAD_OBJECT with If-None-Match header(with incorrect object etag) to obtain the" +
            " temp object headers, verify if the status code is 200 and the headers are the same as from a simple S3 " +
            "request")
    public void headObjectIfNoneMatchNegative() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var headObjectRequest = HeadObjectRequest.builder().bucket(olapArn).key(objectKey).ifNoneMatch("").build();
        var headObjectRequestS3 = HeadObjectRequest.builder().bucket(s3BucketName).key(objectKey).ifNoneMatch("").build();
        // assert
        assertSuccessfulResponse(headObjectRequest, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP HEAD_OBJECT with If-None-Match header(last modified time) to obtain the temp " +
            "object headers, verify if the status code is 200 and the headers are the same as from a simple S3 request")
    public void headObjectIfUnmodifiedSincePositive() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var response = s3Client.headObject(builder -> builder
                .bucket(s3BucketName)
                .key(objectKey)
                .build());
        var lastModifiedInstant = response.lastModified();
        var headObjectRequest = HeadObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .ifUnmodifiedSince(lastModifiedInstant)
                .build();
        var headObjectRequestS3 = HeadObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .ifUnmodifiedSince(lastModifiedInstant)
                .build();
        // assert
        assertSuccessfulResponse(headObjectRequest, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP HEAD_OBJECT with If-Match header(incorrect etag) to obtain the temp object, " +
            "verify the status code is 412 due to there is no object with specified etag (Presign Url issue)")
    public void headObjectIfMatchNegative() {
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var headObjectRequest = HeadObjectRequest.builder().bucket(olapArn).key(objectKey)
                .ifMatch("asdfsdf").build();
        try {
            var headObjectResponse = s3Client.headObject(headObjectRequest);
            Assert.fail("If-Match negative should not be a successful call. Please see the response detail: "
                                + headObjectResponse);
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_PRECONDITION_FAILED);
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP HEAD_OBJECT with If-None-Match header(last modified time minus one second) " +
            "to obtain the temp object, verify the status code is 412 since the object was modified.")
    public void headObjectIfUnmodifiedSinceNegative() {
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var response = s3Client.headObject(builder ->
                                                             builder.bucket(s3BucketName).key(objectKey).build());
        var lastmodifiedInstant = response.lastModified();
        var headObjectRequest = HeadObjectRequest.builder().bucket(olapArn).key(objectKey)
                .ifUnmodifiedSince(lastmodifiedInstant.minusSeconds(20)).build();
        try {
            var headObjectResponse = s3Client.headObject(headObjectRequest);
            Assert.fail("If-None-Match Positive expect a NOT_MODIFIED exception but a SUCCESSFUL is returned");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_PRECONDITION_FAILED);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors");
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP HEAD_OBJECT with partnumber header to obtain part of the object, "
            + "verify if the status code is 200 and the content-length is the same size as the default part size")
    public void partNumberWorks() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, largeData);
        var headObjectRequest = HeadObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .partNumber(1)
                .build();
        // assert
        var headObjectResponse = s3Client.headObject(headObjectRequest);
        Assert.assertEquals(headObjectResponse.contentLength(), DEFAULT_PART_SIZE);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with range header to obtain part of the object, "
            + "verify if the status code is 200 and the headers are the same as from a simple S3 request")
    public void rangeWorks() {
        // setup
        int from = 1024;
        int to = 10240;
        String rangeQuery = String.format("bytes=%d-%d", from, to);
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, largeData);
        var headObjectRequest = HeadObjectRequest
                .builder()
                .bucket(olapArn)
                .key(objectKey)
                .range(rangeQuery)
                .build();

        var headObjectRequestS3 = HeadObjectRequest
                .builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .range(rangeQuery)
                .build();
        // assert
        assertSuccessfulResponse(headObjectRequest, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }


    @Test(description = "Calling OLAP HEAD_OBJECT with x-amz-expected-bucket-owner header(current user) to obtain the" +
            " object, verify if the status code is 200 and the headers are the same as from a simple S3 request")
    public void expectedBucketOwnerPositive() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var headObjectRequestOLAP = HeadObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .expectedBucketOwner(sdkHelper.getAWSAccountID(region))
                .build();
        var headObjectRequestS3 = HeadObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .expectedBucketOwner(sdkHelper.getAWSAccountID(region))
                .build();
        // assert
        assertSuccessfulResponse(headObjectRequestOLAP, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }


    @Test(description = "Calling OLAP HEAD_OBJECT with x-amz-expected-bucket-owner header(dummyuser) to obtain the " +
            "object, verify the status code is 403 since the dummyuser is not the owner of the bucket.")
    public void expectedBucketOwnerNegative() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var headObjectRequestOLAP = HeadObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .expectedBucketOwner(DUMMY_ACCOUNT_ID)
                .build();
        // assert
        try {
            s3Client.headObject(headObjectRequestOLAP);
            Assert.fail("expectedBucketOwnerNegative expecting a 403 errors");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_FORBIDDEN);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "versionID return a specific version of a particular object")
    public void versionID() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        String updatedTemp = "0987654321".repeat(1000);
        var responseInputStream = s3Client.getObject(builder -> builder
                .bucket(s3BucketName)
                .key(objectKey)
                .build());
        String oldVersionID = responseInputStream.response().versionId();
        var putResponse = s3Client.putObject(builder -> builder.bucket(s3BucketName).key(objectKey).build(),
                                             RequestBody.fromString(updatedTemp, StandardCharsets.UTF_8));
        var headObjectRequestOLAP = HeadObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .versionId(oldVersionID)
                .build();
        var headObjectRequestS3 = HeadObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .versionId(oldVersionID)
                .build();
        // assert
        Assert.assertNotEquals(putResponse.versionId(), oldVersionID);
        assertSuccessfulResponse(headObjectRequestOLAP, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Object Lambda passes forward x-amz-request-payer header. Verify that the status code is 200")
    public void requestPayerIsPassedToS3() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var headObjectRequestOLAP = HeadObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .requestPayer(Payer.UNKNOWN_TO_SDK_VERSION.toString())
                .build();

        var headObjectRequestS3 = HeadObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .requestPayer(Payer.UNKNOWN_TO_SDK_VERSION.toString())
                .build();

        // assert
        assertSuccessfulResponse(headObjectRequestOLAP, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }


    @Test(description = "Put a temp object into the bucket using the 3 SSE-C headers "
            + "then call OLAP HEAD_OBJECT with the 3 header to obtain the temp object, "
            + "verify the status code is 400 as Object Lambda does not support this header.")
    public void serverSideEncryptionCustomerNotSupport() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        byte[] secretKey = generateSecretKey();
        String b64Key = Base64.getEncoder().encodeToString(secretKey);
        String b64KeyMd5 = Md5Utils.md5AsBase64(secretKey);
        var put = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .sseCustomerAlgorithm(ServerSideEncryption.AES256.name())
                .sseCustomerKey(b64Key)
                .sseCustomerKeyMD5(b64KeyMd5).build();
        s3Client.putObject(put, RequestBody.fromString(data, StandardCharsets.UTF_8));
        var headObjectRequestOLAP = HeadObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .sseCustomerAlgorithm(ServerSideEncryption.AES256.name())
                .sseCustomerKey(b64Key)
                .sseCustomerKeyMD5(b64KeyMd5)
                .build();
        // assert
        try {
            s3Client.headObject(headObjectRequestOLAP);
            Assert.fail("Object Lambda does not support SSE-C");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_BAD_REQUEST);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Put a temp object using SSE-S3 then call OLAP HEAD_OBJECT to obtain the object, "
            + "verify if the status code is 200 and the headers are the same as from a simple S3 request.")
    public void serverSideEncryptionS3() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        var put = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build();
        s3Client.putObject(put, RequestBody.fromString(data, StandardCharsets.UTF_8));
        var headObjectRequestOLAP = HeadObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        var headObjectRequestS3 = HeadObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        // assert
        assertSuccessfulResponse(headObjectRequestOLAP, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Put a temp object using SSE-KMS then call OLAP HEAD_OBJECT to obtain the object, "
            + "verify if the status code is 200 and the headers are the same as from a simple S3 request")
    public void serverSideEncryptionKMS() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        var put = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .serverSideEncryption(ServerSideEncryption.AWS_KMS)
                .build();
        s3Client.putObject(put, RequestBody.fromString(data, StandardCharsets.UTF_8));
        var headObjectRequestOLAP = HeadObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        var headObjectRequestS3 = HeadObjectRequest.builder().bucket(s3BucketName).key(objectKey).build();
        // assert
        assertSuccessfulResponse(headObjectRequestOLAP, headObjectRequestS3);
        // cleanup
        cleanupResource(objectKey);
    }
}
