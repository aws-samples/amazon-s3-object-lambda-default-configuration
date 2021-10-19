# S3ObjectLambdaDefaultConfigNodejsFunction - `@aws-samples/s3object-lambda-default-config-nodejs-function`

This package contains an AWS Lambda function that can handle requests from Amazon S3 Object Lambda. The function is written in Typescript and supports the Node.js 14.x runtime.

The function handles a GetObject request, by performing the following steps.
1. Fetch the original object from Amazon S3 through the presigned URL in the input event payload.
1. Apply a transformation on the object. This is left as a TODO for you to add your own transformation logic.
1. Handle post-transformation processing steps such as applying Range or PartNumber parameters in the request, if applicable.
1. Return the final transformed object back to Amazon S3 Object Lambda.

### How to run

#### Build the AWS Lambda deployment package
1. Run `npm install` to install the required dependencies to run this function.
1. Run `npm run-script build` to trigger ESBuild and generate a single JS file from the source code.
1. Run `npm run-script package` to create a deployment package ZIP file under `release/s3objectlambda_deployment_package.zip`.

#### Create an Amazon S3 Object Lambda Access Point
1. Create a new Amazon S3 bucket or choose an existing bucket with versioning enabled to host your AWS Lambda function deployment package. 
1. Upload the deployment package to the Amazon S3 bucket.
1. Create a new Amazon S3 bucket or choose an existing bucket to try out Amazon S3 Object Lambda. You can transform objects in this bucket through the Amazon S3 Object Lambda Access Point.
1. Deploy the AWS CloudFormation template defined in `template/s3objectlambda_defaultconfig.yaml` to your AWS account. Please refer to the README file of the template for detailed instructions. 
This will deploy your package as an AWS Lambda function and set up an Amazon S3 Object Lambda Access Point for you. 

You can now start using your Amazon S3 Object Lambda Access Point to retrieve objects from your Amazon S3 bucket. The objects will be returned as is, without any transformations.

#### Transforming your objects through Amazon S3 Object Lambda
To start transforming your objects, you should add your transformation logic to the AWS Lambda function backing your Amazon S3 Object Lambda Access Point.

1. Implement the function under `src/transform/s3objectlambda_transformer.ts`.
1. Update your AWS Lambda deployment package by running the `npm run-script build` and `npm run-script package` commands.
1. Replace the deployment package on the Amazon S3 bucket. Once your upload is complete, you will see a new `versionId` for your latest version of the package.
1. Pass this `versionId` as the `LambdaFunctionS3ObjectVersion` parameter and re-deploy your AWS CloudFormation template. This will update your AWS Lambda function with your transformation code changes.
1. Use the Amazon S3 Object Lambda Access Point to retrieve objects from your Amazon S3 bucket. The objects will now be transformed according to your transformation function in the AWS Lambda function.

### NpmPrettyMuch Background - This should be removed before adding the package to GitHub.

The package is built with [NpmPrettyMuch](https://w.amazon.com/bin/view/NpmPrettyMuch/GettingStarted/v1) and allows using internal (first-party) dependencies as well as external npmjs.com packages.

Add external dependencies with `brazil-build install` exactly the same as [`npm install`](https://docs.npmjs.com/cli-commands/install.html). You can check latest state of external dependencies on https://npmpm.corp.amazon.com/ Important: Never mix Brazil and NPM install since different package sources are used.

Add internal packages to `test-dependencies` in the Brazil Config file to avoid [transitive conflicts](https://builderhub.corp.amazon.com/docs/brazil/user-guide/concepts-dependencies.html#how-do-i-build-against-a-dependency-in-a-way-that-doesn-t-pollute-my-consumers-dependency-graph) and declare a dependency in your `package.json` with a `*` as version since Brazil is determining latest.

NpmPrettyMuch 1.0 has special behavior for running tests during build. The option `"runTest": "never"` disabled this and instead tests are wired up in `prepublishOnly`. NpmPrettyMuch will invoke `prepublishOnly` and everything can configured in there the [same as with external npm](https://docs.npmjs.com/misc/scripts). Files to published are configured using [`files` in `package.json`](https://docs.npmjs.com/configuring-npm/package-json.html#files). The option `ciBuild` uses [`npm ci`](https://docs.npmjs.com/cli-commands/ci.html) instead of `npm install` and results in faster install times and guarantees all of your dependencies are locked appropriately.
