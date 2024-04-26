# S3 Object Lambda Default Configuration

[S3 Object Lambda](https://aws.amazon.com/s3/features/object-lambda/) is a feature of S3 that allows users to add their own code to Amazon S3 GET, HEAD and LIST requests to modify and process data as it is returned to an application. It uses AWS Lambda functions to automatically process the output of standard S3 requests.

Object Lambda eliminates the need to create and store derivative copies of your data, or manage
a proxy, and requires no application changes.

# About the Project

This package contains an AWS CloudFormation template that helps you get started with [Amazon S3 Object Lambda](https://docs.aws.amazon.com/AmazonS3/latest/userguide/transforming-objects.html). The AWS CloudFormation template automatically creates relevant resources, configures IAM roles, and sets up an AWS Lambda function to handle requests from your S3 Object Lambda Access Point. The setup contains examples for Lambda function in Node.js, Java 17 and Python 3. To quickly get started, you can check out our [Getting Started](#getting-started).

# How it works

Our example sends back the original object to the caller. You can add your own transformation in the appropriate function.  **By default, no transformation is applied.**

This default configuration creates one Lambda function that handles requests for GetObject, HeadObject, ListObjectsV1 and ListObjectsV2. **Head and List support is currently only in the NodeJS example function!**

### GetObject 
The function handles a GetObject request, by performing the following steps.

1. Fetch the original object from Amazon S3 through the pre-signed URL in the input event payload.
1. Apply a transformation on the object. You can extend the function to author the transformation and customize the Lambda function for your specific use case.
1. Handle post-transformation processing steps such as applying Range or PartNumber parameters in the request, if applicable.
1. Return the final transformed object back to Amazon S3 Object Lambda.

### HeadObject
Handles a HeadObject request, by performing the following steps:
1. Validates the incoming user request.
1. Retrieves the headers from Amazon S3 using a HeadObject request.
1. Apply a transformation on the headers. You can apply your custom transformation logic here. 
1. Sends the final transformed headers back to Amazon S3 Object Lambda.

*True only for NodeJS currently. Python and Java are forwarded directly to S3*

### ListObjectsV1 and ListObjectsV2

Handles a ListObjectsV1 or ListObjectsV2 request, by performing the following steps:
1. Validates the incoming user request.
1. Retrieves the original object from Amazon S3. Converts it into an Javascript Object.
1. Applies a transformation. You can apply your custom transformation logic here.
1. Transforms back the object to an XML string.
1. Sends the final transformed object back to Amazon S3 Object Lambda.

*True only for NodeJS currently. Python and Java are forwarded directly to S3*

# Getting Started

## Prerequisites

**Amazon S3 bucket**

Choose an S3 bucket containing objects that you would like to process with an Object Lambda Access Point. You will need the bucket name as input while deploying the template. If you don't already have a bucket then you can find how to create a bucket on the [public AWS docs](https://docs.aws.amazon.com/AmazonS3/latest/userguide/creating-bucket.html).

Beside the bucket which you would like to process with Object Lambda, you need a *versioned* bucket where you will upload the deployment package. If the main bucket is already versioned, then you can use the same one.

**AWS Lambda function deployment package**

Create a deployment package in an Amazon S3 bucket that has versioning enabled.

1. Download the `s3objectlambda_deployment_package.zip` file under `function/node.js20.x/release`.
2. Upload the Lambda function deployment package to any Amazon S3 bucket that supports versioning. You will use this S3 bucket name and object key while deploying the template.

## Usage

### **1. Deploy the template**

After you have uploaded the deployment package you can deploy the template.

The template can be found under `template/s3objectlambda_defaultconfig.yaml`. This can be deployed using the [AWS CLI](https://docs.aws.amazon.com/cli/latest/reference/cloudformation/index.html) or the [AWS CloudFormation Console](https://eu-west-1.console.aws.amazon.com/cloudformation/home?region=eu-west-1).

**Using AWS CLI**

The template can be deployed to your AWS account using the following command.

```

aws cloudformation deploy --template-file s3objectlambda_defaultconfig.yaml \
--stack-name <your-Cfn-stack-name> --parameter-overrides ObjectLambdaAccessPointName=<your-OLAP-name> \
SupportingAccessPointName=<your-S3-AP-name> S3BucketName=<your-S3-bucket-name> \
LambdaFunctionS3BucketName=<S3-bucket-containing-Lambda-package> \
LambdaFunctionS3Key=<S3-object-key-of-Lambda-package> LambdaFunctionRuntime=<Your-Lambda-Function-Runtime> \
LambdaFunctionS3ObjectVersion=<object-version-id> \
 --capabilities CAPABILITY_IAM
  
```

**Mandatory parameters**

* `stack-name` takes an identifier for your stack. Please see [Specifying stack name and parameters](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-using-console-create-stack-parameters.html) for guidance on choosing a stack name.
* `ObjectLambdaAccessPointName` takes the name of your Object Lambda Access Point. Please follow the [Rules for naming S3 Access Points](https://docs.aws.amazon.com/AmazonS3/latest/userguide/creating-access-points.html#access-points-names).
* `SupportingAccessPointName` takes the name of an S3 Access Point associated with the S3 bucket passed in the S3BucketName parameter. If you do not have an existing Access Point, you can pass the `CreateNewSupportingAccessPoint=true` parameter override to make the CloudFormation template create an Access Point for you.
* `S3BucketName` takes the bucket name to use with S3 Object Lambda. The bucket should exist in the same AWS account and AWS Region that will deploy this template. The bucket should also [delegate access control to Access Points](https://docs.aws.amazon.com/AmazonS3/latest/userguide/access-points-policies.html#access-points-delegating-control).
* `LambdaFunctionS3BucketName` takes the name of the Amazon S3 bucket where you have uploaded the Lambda function deployment package. The bucket should be in the same AWS Region as your function, but can be in a different AWS account. It should have versioning enabled.
* `LambdaFunctionS3Key` takes the Amazon S3 object key of the Lambda function deployment package. Example: s3objectlambda_deployment_package.zip
* `lambdaFunctionRuntime` takes the Lambda function run time. Example: nodejs20.x
* `LambdaFunctionS3ObjectVersion` takes the object version id of the Lambda function deployment package.
* `--capabilities CAPABILITY_IAM` is required as the template creates an IAM role for the Lambda function's execution.

**Optional parameters**

You can pass the following optional parameter overrides when deploying the template.

***CreateNewSupportingAccessPoint***

A boolean parameter with options true and false. The template will create a new S3 Access Point when this parameter is true.

**The default value is false**, so if you do not pass this parameter, you should ensure that the supporting Access Point exists
to avoid errors. 

*Example usage*
`CreateNewSupportingAccessPoint=true`

***LambdaFunctionPayload***

A string parameter that accepts a static payload in any format. You can use this to provide supplemental data to the AWS Lambda function.

*Example usage*
`LambdaFunctionPayload=“hello-world”`

***EnableCloudWatchMonitoring***

A boolean parameter with options true and false. If true, the template will enable CloudWatch request metrics from S3 Object Lambda. The template also creates CloudWatch alarms to monitor the request metrics. The default option is false. Please note that enabling this flag might incur [CloudWatch usage costs](https://aws.amazon.com/cloudwatch/pricing/).

*Example usage*
`EnableCloudWatchMonitoring=true`

### 2. Use the Object Lambda Access Point

Once the template is successfully deployed, you can start using it to get objects from your Amazon S3 bucket. By default, the function does not perform any transformation. So when you perform a GetObject request, you will see the objects exactly as they are stored in your buckets.

You can make a GetObject, HeadObject and ListObjects requests by passing the Object Lambda Access Point ARN as the Bucket parameter. If you haven't enabled
Object Lambda on the specific API then the request will just be proxied directly to S3.

Here is an example CLI command for [S3 GetObject](https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html).
```
aws s3api get-object --bucket 'your-object-lambda-access-point-arn' --key 'your-object-key' 'outfile'
```
Here is an example CLI command for [S3 HeadObject](https://docs.aws.amazon.com/AmazonS3/latest/API/API_HeadObject.html).
```
aws s3api head-object --bucket 'your-object-lambda-access-point-arn' --key 'your-object-key'
```
Here is an example CLI command for [S3 ListObjectsV2](https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjectsV2.html).
```
aws s3api list-objects-v2 'your-object-lambda-access-point-arn'
```

# Implementing your transformation

You can extend the AWS Lambda function provided in this package to create your transformation. By default, the function code does not run any transformation, and returns your objects as-is from your Amazon S3 bucket.

You can clone the function and add your own transformation code to the following file location


| Language | File                                                                                         | Method name             | Api           |
| -------- | -------------------------------------------------------------------------------------------- | ----------------------- | ------------- |
| NodeJS   | function/nodejs_20_x/src/transform/s3objectlambda_transformer.ts                             | transformObject         | GetObject     |
| NodeJS   | function/nodejs_20_x/src/transform/s3objectlambda_transformer.ts                             | transformHeaders        | HeadObject    |
| NodeJS   | function/nodejs_20_x/src/transform/s3objectlambda_transformer.ts                             | transformListObjectsV1  | ListObjectsV1 |
| NodeJS   | function/nodejs_20_x/src/transform/s3objectlambda_transformer.ts                             | transformListObjectsV2  | ListObjectsV2 |
| Java     | function/java17/src/main/java/com/example/s3objectlambda/transform/GetObjectTransformer.java | transformObjectResponse | GetObject     |
| Python   | function/python_3_9/src/transform/transform.py                                               | transform_object        | GetObject     |


As you implement your transformation function, you can test each iteration of your Lambda function code by updating your deployment package in S3 and re-deploying the template.

If you don't wish to process all the APIs by Object Lambda then you can just delete them from the [Cloudformation template file](./template/s3objectlambda_defaultconfig.yaml) 

### Build your deployment package

#### nodejs
1. Run `npm install` to install the required dependencies to run this function.
2. Run `npm run-script build` to run ESBuild and generate a single JS file called `s3objectlambda.js` from the source code.
3. Run `npm run-script test` to execute the unit tests.
4. Run `npm run-script package` to create a deployment package ZIP file under `release/s3objectlambda_deployment_package.zip`.

#### python
1. Run `python -m pip freeze > requirements.txt` to create the Requirements File
2. Run `pip3 install -r requirements.txt -t ./release/package` to add the package files into release folder
3. Run `zip ../s3objectlambda_deployment_package.zip . -r` inside `S3ObjectLambdaDefaultConfigPythonFunction/release/package` to zip the package file
4. Run `zip ../release/s3objectlambda_deployment_package s3objectlambda.py -g` inside `S3ObjectLambdaDefaultConfigPythonFunction/src` to zip s3objectlambda.py file
5. Run `zip ../release/s3objectlambda_deployment_package ./*/*.py -g` inside `S3ObjectLambdaDefaultConfigPythonFunction/src` to zip the sub folders `s3objectlambda_deployment_package.zip` will be created `release/s3objectlambda_deployment_package.zip`.

#### java
1. Run `mvn test` to run the unit tests.
2. Run `mvn package` to create the deployment package jar file in `target/S3ObjectLambdaDefaultConfigJavaFunction-1.0.jar`.

### Deploying your Lambda function update
1. Upload the new deployment package under the same object key `LambdaFunctionS3Key` in your Amazon S3 bucket `LambdaFunctionS3BucketName`. Once your upload is complete, you will see a new `versionId` for your latest version of the deployment package.
2. Pass the new `versionId` as the `LambdaFunctionS3ObjectVersion` parameter and re-deploy your AWS CloudFormation template. This will update the AWS Lambda function with your transformation code changes.
3. Use the Amazon S3 Object Lambda Access Point to get objects from your Amazon S3 bucket. The objects will now be transformed according to your transformation function in the AWS Lambda function. 
You can make a GetObject request by passing the Object Lambda Access Point ARN as the Bucket parameter.

```
aws s3api get-object --bucket <object-lambda-access-point-arn> --key <object-key> <outfile>
```

### Testing your update

You can test your code changes by running the integration test suite. The test suite will create an Object Lambda Access Point
 using the CloudFormation template and execute test cases to validate your Object Lambda Access Point works as expected.

1. Install [Apache Maven](https://maven.apache.org/install.html) and [Java 16](https://docs.oracle.com/en/java/javase/16/).
2. Choose an S3 bucket in your AWS account that has versioning enabled. You will use this bucket to host the resources and run the tests.
3. Upload template to the versioned S3 bucket. You will need the S3 object key to run the tests.
4. Upload the Lambda function deployment package under `release/s3objectlambda_deployment_package.zip` to the versioned S3 bucket. You will need the S3 object key and version ID of the deployment package to run the tests.
5. Run the tests using Maven from the project root directory `amazon-s3-object-lambda-default-configuration`. Replace the below parameters with actual values.

```
mvn test -f tests/pom.xml -Dregion=${{AWS_REGION}} -DtemplateUrl=https://${{AWS_BUCKET_NAME}}.s3.${{AWS_REGION}}.amazonaws.com/${{TEMPLATE_KEY}} \
-Ds3BucketName=${{AWS_BUCKET_NAME}} -DlambdaFunctionS3BucketName=${{AWS_BUCKET_NAME}} -DlambdaFunctionS3Key=${{LAMBDA_NODE_KEY}}
-DlambdaFunctionRuntime=${{LAMBDA_FUNCTION_RUNTIME}} -DcreateNewSupportingAccessPoint=true -DlambdaVersion=${{LAMBDA_VERSION}}
```

# Contributing

We welcome contributions! Please submit a pull request using the PR template.

# License

Distributed under the MIT License. See `LICENSE.txt` for more information.

# Contact

For bug reports or feature requests, please [open a new issue](https://github.com/aws-samples/amazon-s3-object-lambda-default-configuration/issues). 

If you discover a potential security issue in this project,
 we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](https://aws.amazon.com/security/vulnerability-reporting/) 
 or directly via email to [aws-security@amazon.com](mailto:aws-security@amazon.com). Please do **not** create a public GitHub issue.
