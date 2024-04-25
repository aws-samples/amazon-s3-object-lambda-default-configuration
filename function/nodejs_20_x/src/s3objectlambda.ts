import type { S3ObjectLambdaEvent } from './s3objectlambda_event.types';
import handleGetObjectRequest from './handler/get_object_handler';
import { S3Client } from '@aws-sdk/client-s3';
import handleHeadObjectRequest from './handler/head_object_handler';
import { ListObjectsHandler } from './handler/list_objects_base_handler';
import {
  transformListObjectsV1,
  transformListObjectsV2
} from './transform/s3objectlambda_transformer';
import { IListObjectsV1, IListObjectsV2 } from './s3objectlambda_list_type';

/* Initialize clients outside Lambda handler to take advantage of execution environment reuse and improve function
performance. See {@link https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html | Best practices with AWS
 Lambda functions}
for details.
**/
const s3 = new S3Client();

export async function handler (event: S3ObjectLambdaEvent): Promise<null | any> {
  /*
  <p>The event object contains all information required to handle a request from Amazon S3 Object Lambda.</p>
  <p>The context objects contain information about the request, which resulted in this Lambda function being
   invoked.</p>
  <p>The userRequest object contains information related to the entity (user or application)
     that invoked Amazon S3 Object Lambda. This information can be used in multiple ways, for example, to allow or deny
     the request based on the entity. See the <i>Respond with a 403 Forbidden</i> example in
     {@link https://docs.aws.amazon.com/AmazonS3/latest/userguide/olap-writing-lambda.html | Writing Lambda functions}
     for sample code.</p>
   */

  if ('getObjectContext' in event) {
    await handleGetObjectRequest(s3, event.getObjectContext, event.userRequest);
    return null;
  } else if ('headObjectContext' in event) {
    return handleHeadObjectRequest(event.headObjectContext, event.userRequest);
  } else if ('listObjectsContext' in event) {
    const listObjectListObjectsHandler = new ListObjectsHandler<IListObjectsV1>(transformListObjectsV1);
    return listObjectListObjectsHandler.handleListObjectsRequest(event.listObjectsContext, event.userRequest);
  } else if ('listObjectsV2Context' in event) {
    const listObjectListObjectsHandler = new ListObjectsHandler<IListObjectsV2>(transformListObjectsV2);
    return listObjectListObjectsHandler.handleListObjectsRequest(event.listObjectsV2Context, event.userRequest);
  }

  return null;
}
