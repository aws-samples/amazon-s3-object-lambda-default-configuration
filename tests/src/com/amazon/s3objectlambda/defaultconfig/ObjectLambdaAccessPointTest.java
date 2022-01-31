package com.amazon.s3objectlambda.defaultconfig;

import static com.amazon.s3objectlambda.defaultconfig.KeyConstants.*;

import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.utils.Md5Utils;

import javax.crypto.KeyGenerator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Test(groups = "accessPoint", dependsOnGroups = {"setup"})
public class ObjectLambdaAccessPointTest {

    private static final int DEFAULT_PART_SIZE = 5242880; // 5 MB
    private static final String DUMMY_ACCOUNT_ID = "111122223333";
    private final String data = "1234567890".repeat(10);
    private final String largeData = "123".repeat(DEFAULT_PART_SIZE);
    private S3Client s3Client;
    private String olAccessPointName;
    private String olapArn;
    private String s3BucketName;
    private SdkHelper sdkHelper;
    private Region region;

    @Parameters({"region", "s3BucketName"})
    @BeforeClass()
    void setup(ITestContext context, String region, String s3BucketName) {
        this.olAccessPointName = (String) context.getAttribute(OL_AP_NAME_KEY);
        this.sdkHelper = new SdkHelper();
        this.s3Client = sdkHelper.getS3Client(region);
        this.region = Region.of(region);
        this.olapArn = sdkHelper.getOLAccessPointArn(this.region, olAccessPointName);
        this.s3BucketName = s3BucketName;
    }

     private PutObjectResponse setupResource(String objectKey, String data) {
        return s3Client.putObject(builder -> builder.bucket(s3BucketName).key(objectKey).build(),
                RequestBody.fromString(data, StandardCharsets.UTF_8));
    }

    private void cleanupResource(String objectKey) {
        s3Client.deleteObject(builder -> builder.bucket(s3BucketName).key(objectKey).build());
    }

    private void assertSuccessfulResponse(GetObjectRequest getObjectRequest, byte[] byteData) {
        try {
            ResponseInputStream<GetObjectResponse> object = s3Client.getObject(getObjectRequest);
            Assert.assertTrue(object.response().sdkHttpResponse().isSuccessful());
            InputStream stream = new ByteArrayInputStream(object.readAllBytes());
            Assert.assertEquals(stream.readAllBytes(), byteData);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
    }

    @Parameters()
    @Test(description = "Calling OLAP to obtain the object,"
            + "verify if the status code is 200 and the content is correct.")
    public void getObjectSimple() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var getObjectRequest = GetObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        // assert
        assertSuccessfulResponse(getObjectRequest, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with If-Match header(correct etag) to obtain the object, "
            + "verify if the status code is 200 and the content is correct.")
    public void getObjectIfMatchPositive() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        var putObjectResponse = setupResource(objectKey, data);
        String eTag = putObjectResponse.eTag();
        var getObjectRequest = GetObjectRequest.builder().bucket(olapArn).key(objectKey).ifMatch(eTag).build();
        // assert
        assertSuccessfulResponse(getObjectRequest, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with If-Modified-Since header(last modified time minus one second) to obtain the "
            + "temp object, verify if the status code is 200 and the content is correct.")
    public void getObjectIfModifiedSincePositive() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var responseInputStream = s3Client.getObject(builder -> builder
                .bucket(s3BucketName)
                .key(objectKey)
                .build());
        Instant createTime = responseInputStream.response().lastModified();
        Instant timeBeforeCreate = createTime.minusSeconds(1);
        var getObjectRequest = GetObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .ifModifiedSince(timeBeforeCreate)
                .build();
        // assert
        assertSuccessfulResponse(getObjectRequest, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with If-None-Match header(with incorrect object etag) to obtain the temp object, "
            + "verify if the status code is 200 and the content is correct.")
    public void getObjectIfNoneMatchNegative() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var getObjectRequest = GetObjectRequest.builder().bucket(olapArn).key(objectKey).ifNoneMatch("").build();
        // assert
        assertSuccessfulResponse(getObjectRequest, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with If-None-Match header(last modified time) to obtain the temp object, "
            + "verify if the status code is 200 and the content is correct.")
    public void getObjectIfUnmodifiedSincePositive() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var responseInputStream = s3Client.getObject(builder -> builder
                .bucket(s3BucketName)
                .key(objectKey)
                .build());
        var lastModifiedInstant = responseInputStream.response().lastModified();
        var getObjectRequest = GetObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .ifUnmodifiedSince(lastModifiedInstant)
                .build();
        // assert
        assertSuccessfulResponse(getObjectRequest, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with If-Match header(incorrect etag) to obtain the temp object, " +
            "verify the status code is 412 due to there is no object with specified etag (Presign Url issue)")
    public void getObjectIfMatchNegative() {
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var getObjectRequest = GetObjectRequest.builder().bucket(olapArn).key(objectKey)
                .ifMatch("asdfsdf").build();
        try {
            var getObjectResponse = s3Client.getObject(getObjectRequest);
            Assert.fail("If-Match negative should not be a successful call. Please see the response detail: "
                    + getObjectResponse);
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_PRECONDITION_FAILED);
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with If-None-Match header(last modified time minus one second) " +
            "to obtain the temp object, verify the status code is 412 since the object was modified.")
    public void getObjectIfUnmodifiedSinceNegative() {
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var responseInputStream = s3Client.getObject(builder ->
                builder.bucket(s3BucketName).key(objectKey).build());
        var lastmodifiedInstant = responseInputStream.response().lastModified();
        var getObjectRequest = GetObjectRequest.builder().bucket(olapArn).key(objectKey)
                .ifUnmodifiedSince(lastmodifiedInstant.minusSeconds(20)).build();
        try {
            ResponseInputStream<GetObjectResponse> object = s3Client.getObject(getObjectRequest);
            Assert.fail("If-None-Match Positive expect a NOT_MODIFIED exception but a SUCCESSFUL is returned");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_PRECONDITION_FAILED);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors");
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with partnumber header to obtain part of the object, "
            + "verify if the status code is 200 and the content of the part object is correct."
            + "Default size for part is " + DEFAULT_PART_SIZE)
    public void partNumberWorks() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, largeData);
        var getObjectRequest = GetObjectRequest.builder().bucket(olapArn).key(objectKey).partNumber(1).build();
        byte[] tempBytes = largeData.getBytes(StandardCharsets.UTF_8);
        byte[] slices = Arrays.copyOf(tempBytes, DEFAULT_PART_SIZE);
        // assert
        assertSuccessfulResponse(getObjectRequest, slices);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with range header to obtain part of the object, "
            + "verify the status code is 200 and the content of the partial object.")
    public void rangeWorks() {
        // setup
        int from = 1024;
        int to = 10240;
        String rangeQuery = String.format("bytes=%d-%d", from, to);
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, largeData);
        var getObjectRequest = GetObjectRequest.builder().bucket(olapArn).key(objectKey).range(rangeQuery).build();
        byte[] tempBytes = largeData.getBytes(StandardCharsets.UTF_8);
        byte[] slices = Arrays.copyOfRange(tempBytes, from, to + 1);
        // assert
        assertSuccessfulResponse(getObjectRequest, slices);
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Calling OLAP with x-amz-expected-bucket-owner header(current user) to obtain the object, "
            + "verify if the status code is 200 and the content is correct.")
    public void expectedBucketOwnerPositive() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var getObjectRequestOLAP = GetObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .expectedBucketOwner(sdkHelper.getAWSAccountID(region))
                .build();
        // assert
        assertSuccessfulResponse(getObjectRequestOLAP, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }


    @Test(description = "Calling OLAP with x-amz-expected-bucket-owner header(dummyuser) to obtain the object, "
            + "verify the status code is 403 since the dummyuser is not the owner of the bucket.")
    public void expectedBucketOwnerNegative() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var getObjectRequestOLAP = GetObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .expectedBucketOwner(DUMMY_ACCOUNT_ID)
                .build();
        // assert
        try {
            s3Client.getObject(getObjectRequestOLAP);
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
        var getObjectRequestOLAP = GetObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .versionId(oldVersionID)
                .build();
        // assert
        Assert.assertNotEquals(putResponse.versionId(), oldVersionID);
        assertSuccessfulResponse(getObjectRequestOLAP, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }


    @Test(description = "Object Lambda does not support x-amz-request-payer header, expecting a 403")
    public void requestPayerNotSupport() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        setupResource(objectKey, data);
        var getObjectRequestOLAP = GetObjectRequest.builder()
                .bucket(olapArn)
                .key(objectKey)
                .requestPayer(Payer.UNKNOWN_TO_SDK_VERSION.toString())
                .build();
        // assert
        try {
            s3Client.getObject(getObjectRequestOLAP);
            Assert.fail("Object Lambda does not support x-amz-request-payer header");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_FORBIDDEN);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
        // cleanup
        cleanupResource(objectKey);
    }


    private byte[] generateSecretKey() {
        KeyGenerator generator;
        try {
            generator = KeyGenerator.getInstance("AES");
            generator.init(256, new SecureRandom());
            return generator.generateKey().getEncoded();
        } catch (Exception e) {
            return null;
        }
    }

    @Test(description = "Put a temp object into the bucket using the 3 SSE-C headers "
            + "then call OLAP with the 3 header to obtain the temp object, "
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
        var getObjectRequestOLAP = GetObjectRequest.builder()
                                                                    .bucket(olapArn)
                .key(objectKey)
                .sseCustomerAlgorithm(ServerSideEncryption.AES256.name())
                .sseCustomerKey(b64Key)
                .sseCustomerKeyMD5(b64KeyMd5)
                .build();
        // assert
        try {
            s3Client.getObject(getObjectRequestOLAP);
            Assert.fail("Object Lambda does not support SSE-C");
        } catch (S3Exception s3Exception) {
            Assert.assertEquals(s3Exception.statusCode(), HttpStatus.SC_BAD_REQUEST);
        } catch (Exception e) {
            Assert.fail("Unexpected Errors: " + e.getMessage());
        }
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Put a temp object using SSE-S3 then call OLAP to obtain the object, "
            + "verify if the status code is 200 and the content is correct.")
    public void serverSideEncryptionS3() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        var put = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .build();
        s3Client.putObject(put, RequestBody.fromString(data, StandardCharsets.UTF_8));
        var getObjectRequestOLAP = GetObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        // assert
        assertSuccessfulResponse(getObjectRequestOLAP, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }

    @Test(description = "Put a temp object using SSE-KMS then call OLAP to obtain the object, "
            + "verify if the status code is 200 and the content is correct.")
    public void serverSideEncryptionKMS() {
        // setup
        String objectKey = UUID.randomUUID().toString();
        var put = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(objectKey)
                .serverSideEncryption(ServerSideEncryption.AWS_KMS)
                .build();
        s3Client.putObject(put, RequestBody.fromString(data, StandardCharsets.UTF_8));
        var getObjectRequestOLAP = GetObjectRequest.builder().bucket(olapArn).key(objectKey).build();
        // assert
        assertSuccessfulResponse(getObjectRequestOLAP, data.getBytes(StandardCharsets.UTF_8));
        // cleanup
        cleanupResource(objectKey);
    }
}
