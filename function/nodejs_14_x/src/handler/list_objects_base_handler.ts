import { BaseObjectContext, UserRequest } from '../s3objectlambda_event.types';
import { makeS3Request } from '../request/utils';
import { errorResponse, responseForS3Errors } from '../error/error_response';
import ErrorCode from '../error/error_code';
import { Buffer } from 'buffer';
import { IBaseListObject } from '../s3objectlambda_list_type';
import { IErrorResponse, IListObjectsResponse, IResponse } from '../s3objectlambda_response.types';
import { ListObjectsXmlTransformer } from '../utils/listobject_xml_transformer';

/**
 * Class that handles ListObjects requests. Can be used
 * for both ListObjectsV1 and ListObjectsV2 requests
 */
export class ListObjectsHandler <T extends IBaseListObject> {
  private readonly transformObject: (listObject: T) => T;
  private readonly XMLTransformer = new ListObjectsXmlTransformer<T>();

  constructor (transformObject: (listObject: T) => T) {
    this.transformObject = transformObject;
  }

  /**
   * Handles a ListObjects request, by performing the following steps:
   * 1. Validates the incoming user request.
   * 2. Retrieves the original object from Amazon S3. Converts it into an Object.
   * 3. Applies a transformation. You can apply your custom transformation logic here.
   * 4. Sends the final transformed object back to Amazon S3 Object Lambda.
   */
  async handleListObjectsRequest (requestContext: BaseObjectContext, userRequest: UserRequest):
  Promise<IResponse> {
    const objectResponse = await makeS3Request(requestContext.inputS3Url, userRequest, 'GET');

    const originalObject = await objectResponse.arrayBuffer();

    if (objectResponse.status >= 400) {
      // Errors in the Amazon S3 response should be forwarded to the caller without invoking transformObject.
      return responseForS3Errors(objectResponse);
    }

    const parsedObject = await this.XMLTransformer.createListObjectsJsonResponse(ListObjectsHandler.stringFromArrayBuffer(originalObject));

    if (parsedObject == null) {
      console.log('Failure parsing the response from S3');
      return errorResponse(requestContext, ErrorCode.NO_SUCH_KEY, 'Requested key does not exist');
    }

    const transformedObject = this.transformObject(parsedObject);

    return this.writeResponse(transformedObject);
  }

  /**
   * Returns the response expected by Object Lambda on a LIST_OBJECTS request
   * @param objectResponse The response
   * @protected
   */
  protected writeResponse (objectResponse: T): IListObjectsResponse | IErrorResponse {
    console.log('Sending transformed results to the Object Lambda Access Point');
    const xmlListObject = this.XMLTransformer.createListObjectsXmlResponse(objectResponse);

    if (xmlListObject === null) {
      console.log('Failed transforming back to XML');
      return {
        statusCode: 500,
        errorMessage: 'The Lambda function failed to transform the result to XML'
      };
    }

    return {
      statusCode: 200,
      listResultXml: xmlListObject
    };
  }

  /**
   * Converts from the array buffer received to a string object.
   * @param arrayBuffer The array buffer containing the string
   * @private
   */
  private static stringFromArrayBuffer (arrayBuffer: ArrayBuffer): string {
    return Buffer.from(arrayBuffer).toString();
  }
}
