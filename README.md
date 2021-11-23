# About the Project

This package contains an AWS CloudFormation template that helps you get started with [Amazon S3 Object Lambda](https://docs.aws.amazon.com/AmazonS3/latest/userguide/transforming-objects.html). With S3 Object Lambda, you can add your own code to S3 GET requests to modify and process data as it is returned to an application.

The AWS CloudFormation template automatically creates relevant resources, configures IAM roles, and sets up an AWS Lambda function to handle requests from your S3 Object Lambda Access Point. The Lambda function is written in [Typescript](https://www.typescriptlang.org/) and supports the [Node.js](https://nodejs.org/en/download/) 14.x Lambda runtime.

The function handles a GetObject request, by performing the following steps.

1. Fetch the original object from Amazon S3 through the pre-signed URL in the input event payload.
2. Apply a transformation on the object. You can extend the function to author the transformation and customize the Lambda function for your specific use case. By default, no transformation is applied.
3. Handle post-transformation processing steps such as applying Range or PartNumber parameters in the request, if applicable.
4. Return the final transformed object back to Amazon S3 Object Lambda.

# Getting Started

## Prerequisites

**Amazon S3 bucket**

Choose an S3 bucket containing objects that you would like to process with an Object Lambda Access Point. You will need the bucket name as input while deploying the template.

**AWS Lambda function deployment package**

Create a deployment package in a separate Amazon S3 bucket that has versioning enabled.

1. Download the `s3objectlambda_deployment_package.zip` file under `function/node.js14.x/release`.
2. Upload the Lambda function deployment package to any Amazon S3 bucket that supports versioning. You will use this S3 bucket name and object key while deploying the template.

## Usage

### **1. Deploy the template**

The template can be found under `template/s3objectlambda_defaultconfig.yaml`. This can be deployed using the [AWS CLI](https://docs.aws.amazon.com/cli/latest/reference/cloudformation/index.html) or the [AWS CloudFormation Console](https://eu-west-1.console.aws.amazon.com/cloudformation/home?region=eu-west-1).

**Using AWS CLI**

The template can be deployed to your AWS account using the following command.

```

aws cloudformation deploy --template-file s3objectlambda_defaultconfig.yaml \
--stack-name <your-Cfn-stack-name> --parameter-overrides ObjectLambdaAccessPointName=<your-OLAP-name> \
SupportingAccessPointName=<your-S3-AP-name> S3BucketName=<your-S3-bucket-name> \
LambdaFunctionS3BucketName=<S3-bucket-containing-Lambda-package> \
LambdaFunctionS3Key=<S3-object-key-of-Lambda-package> LambdaFunctionS3ObjectVersion=<object-version-id>
  
  
```

**Mandatory parameters**

* `stack-name` takes an identifier for your stack. Please see [Specifying stack name and parameters](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/cfn-using-console-create-stack-parameters.html) for guidance on choosing a stack name.
* `ObjectLambdaAccessPointName` takes the name of your Object Lambda Access Point. Please follow the [Rules for naming S3 Access Points](https://docs.aws.amazon.com/AmazonS3/latest/userguide/creating-access-points.html#access-points-names).
* `SupportingAccessPointName` takes the name of an S3 Access Point associated with the S3 bucket passed in the S3BucketName parameter. If you do not have an existing Access Point, you can pass the `CreateNewSupportingAccessPoint=true` parameter override to make the CloudFormation template create an Access Point for you.
* `S3BucketName` takes the bucket name to use with S3 Object Lambda. The bucket should exist in the same AWS account and AWS Region that will deploy this template. The bucket should also [delegate access control to Access Points](https://docs.aws.amazon.com/AmazonS3/latest/userguide/access-points-policies.html#access-points-delegating-control).
* `LambdaFunctionS3BucketName` takes the name of the Amazon S3 bucket where you have uploaded the Lambda function deployment package. The bucket should be in the same AWS Region as your function, but can be in a different AWS account. It should have versioning enabled.
* `LambdaFunctionS3Key` takes the Amazon S3 object key of the Lambda function deployment package.
* `LambdaFunctionS3ObjectVersion` takes the object version id of the Lambda function deployment package.


**Optional parameters**

You can pass the following optional parameter overrides when deploying the template.

***CreateNewSupportingAccessPoint***

A boolean parameter with options true and false. The template will create a new S3 Access Point when this parameter is true.

The default value is false, so if you do not pass this parameter, you should ensure that the supporting Access Point exists
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

You can make a GetObject request by passing the Object Lambda Access Point ARN as the Bucket parameter. Here is an example CLI command for S3 GetObject.
```
aws s3api get-object --bucket 'your-object-lambda-access-point-arn' --key 'your-object-key' 'outfile'
```

For more information, please refer to the [GetObject API Guide](https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html).

# Implementing your transformation

You can extend the AWS Lambda function provided in this package to create your transformation. By default, the function code does not run any transformation, and returns your objects as-is from your Amazon S3 bucket. 
You can clone the function and add your own transformation code to the `transformObject` function under `function/nodejs_14_x/src/transform/s3objectlambda_transformer.ts`.

As you implement your transformation function, you can test each iteration of your Lambda function code by updating your deployment package in S3 and re-deploying the template.

### Build your deployment package

1. Run `npm install` to install the required dependencies to run this function.
2. Run `npm run-script build` to run ESBuild and generate a single JS file called `s3objectlambda.js` from the source code.
3. Run `npm run-script test` to execute the unit tests.
4. Run `npm run-script package` to create a deployment package ZIP file under `release/s3objectlambda_deployment_package.zip`.

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
 -DcreateNewSupportingAccessPoint=true -DlambdaVersion=${{LAMBDA_VERSION}}
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
