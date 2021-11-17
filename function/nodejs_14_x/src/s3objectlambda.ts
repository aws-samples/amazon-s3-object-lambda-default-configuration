import type { S3ObjectLambdaEvent } from './s3objectlambda_event';

import handleGetObjectRequest from './handler/get_object_handler';
import S3 from 'aws-sdk/clients/s3';

/* Initialize clients outside Lambda handler to take advantage of execution environment reuse and improve function
performance. See {@link https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html|Best practices with AWS Lambda functions}
for details.
* */
const S3Client = new S3();

export async function handler (event: S3ObjectLambdaEvent): Promise<null> {
/*
<p>The event object contains all information required to handle a request from Amazon S3 Object Lambda.</p>
<p>The getObjectContext object contains information about the GetObject request,
 which resulted in this Lambda function being invoked.</p>
<p>The userRequest object contains information related to the entity (user or application)
   that invoked Amazon S3 Object Lambda. This information can be used in multiple ways, for example, to allow or deny
   the request based on the entity. See the <i>Respond with a 403 Forbidden</i> example in
   {@link https://docs.aws.amazon.com/AmazonS3/latest/userguide/olap-writing-lambda.html|Writing Lambda functions}
   for sample code.</p>
 */
  if ('getObjectContext' in event) {
    await handleGetObjectRequest(S3Client, event.getObjectContext, event.userRequest);
  }

  // There is nothing to return once the data has been sent to Amazon S3 Object Lambda, so just return null.
  return null;
}
