/*
   <p>The event object contains information about the request made to Amazon S3 Object Lambda. The Lambda function uses
   this information to identify the S3 operation and process the request appropriately.
   See {@link https://docs.aws.amazon.com/AmazonS3/latest/userguide/olap-writing-lambda.html#olap-event-context|Event context format and usage}
   for details.</p>
*/

export interface S3ObjectLambdaEvent {
  readonly xAmzrequestId: string
  readonly getObjectContext: GetObjectContext
  readonly configuration: Configuration
  readonly userRequest: UserRequest
  readonly userIdentity: UserIdentity
  readonly protocolVersion: string
}

export interface GetObjectContext {
  readonly inputS3Url: string
  readonly outputRoute: string
  readonly outputToken: string
}

interface Configuration {
  readonly accessPointArn: string
  readonly supportingAccessPointArn: string
  readonly payload: string
}

export interface UserRequest {
  readonly url: string
  readonly headers: object
}

interface UserIdentity {
  readonly type: string
  readonly principalId: string
  readonly arn: string
  readonly accountId: string
  readonly accessKeyId: string
  readonly sessionContext: SessionContext
  readonly userName: string
  readonly invokedBy: string
}

interface SessionContext {
  readonly attributes: Map<String, String>
  readonly sessionIssuer: Map<String, String>
}
