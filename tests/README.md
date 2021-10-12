#S3OL Default Configuration Integration Test

This package contains a TestNG suite that runs integration tests against the Default Configuration CloudFormation template.

The test suits will deploy the CloudFormation template from a S3 bucket, using the object url to deploy the stack.
After the stack is deployed, it will run tests against the created S3OL access point.
In the end, the suite will delete the created stack.

#Set Up
The test suite uses AWS default credential. So make sure you have set up a default profile on AWS CLI. 

A few parameters are needed for this test suite which can be configurate in testng.xml.

`region` => the AWS region this test suite will run. 

`templateUrl` => object url for the Default Config CloudFormation template

`s3BucketName` => the S3 bucket name 

`lambdaFunctionS3BucketName` => the S3 bucket name where the Lambda function zip file is located

`lambdaFunctionS3Key` => the key of the Lambda function zip file

`createNewSupportingAccessPoint` => if it will create a new Access Point for the S3 bucket. `true` or `false` are the only
acceptable value 

#How to Run The Suite

Run the following commend with the parameters

```shell
brazil-build testng-run-development -Dregion=<region> -DtemplateUrl=<templateUrl> -Ds3BucketName=<s3BucketName> -DlambdaFunctionS3BucketName=<lambdaFunctionS3BucketName> -DlambdaFunctionS3Key=<lambdaFunctionS3BucketName> -DcreateNewSupportingAccessPoint=<true|false> 
```

#References
* Apache Ant Manual: https://ant.apache.org/manual/index.html
* Debugging Apache Ant Scripts: https://ant.apache.org/problems.html
* JUnit5 User Guide: https://junit.org/junit5/docs/current/user-guide/
* JUnit5 Apache Ant Task: https://ant.apache.org/manual/Tasks/junitlauncher.html
* HappierTrails Wiki: https://w.amazon.com/bin/view/BrazilBuildSystem/HappierTrails/
* HappierTrails Properties Guide: https://w.amazon.com/index.php/BrazilBuildSystem/HappierTrails/PropertiesGuide
