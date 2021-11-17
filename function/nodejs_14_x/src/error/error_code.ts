/**
 * The list of error codes returned from the Lambda function. We use the same error codes that are
 * supported by Amazon S3, where possible.
 *
 * See {@link https://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html|Amazon S3 Error Responses}
 * for more information.
 */
enum ErrorCode {
  INVALID_REQUEST = 'InvalidRequest',
  INVALID_RANGE = 'InvalidRange',
  INVALID_PART = 'InvalidPart',
  NO_SUCH_KEY = 'NoSuchKey',
}

export default ErrorCode;
