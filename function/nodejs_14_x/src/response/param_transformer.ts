/**
 * Transforms a value got back from S3 as a Header to a proper parameter value
 * for WriteGetObjectResponse function.
 * https://docs.aws.amazon.com/AWSJavaScriptSDK/latest/AWS/S3.html#writeGetObjectResponse-property
 * @param value Header received from fetch
 */
export function headerToWgorParam (value: string): string {
  switch (value) {
    case 'x-amz-checksum-crc32':
      return 'ChecksumCRC32';
    case 'x-amz-checksum-crc32c':
      return 'ChecksumCRC32C';
    case 'x-amz-checksum-sha1':
      return 'ChecksumSHA1';
    case 'x-amz-checksum-sha256':
      return 'ChecksumSHA256';
    case 'x-amz-tagging-count':
      return 'TagCount';
    case 'x-amz-object-lock-legal-hold':
      return 'ObjectLockLegalHoldStatus';
    case 'x-amz-server-side-encryption':
      return 'ServerSideEncryption';
    case 'x-amz-server-side-encryption-customer-algorithm':
      return 'SSECustomerAlgorithm';
    case 'x-amz-server-side-encryption-aws-kms-key-id':
      return 'SSEKMSKeyId';
    case 'x-amz-server-side-encryption-customer-key-MD5':
      return 'SSECustomerKeyMD5';
    default:
      value = value.replace('x-amz-', '');
      return value.split('-').map((str) => {
        return upperFirst(
          str.split('/')
            .map(upperFirst)
            .join('/'));
      }).join('');
  }
}

/**
 * Returns the same string with the first letter uppercase
 * @param value String to have the first letter uppercase
 */
function upperFirst (value: string): string {
  if (value.length > 0) {
    return value.slice(0, 1).toUpperCase() + value.slice(1, value.length);
  }
  return value;
}

export const ParamsKeys = [
  'RequestRoute',
  'RequestToken',
  'AcceptRanges',
  'Body',
  'BucketKeyEnabled',
  'CacheControl',
  'ChecksumCRC32',
  'ChecksumCRC32C',
  'ChecksumSHA1',
  'ChecksumSHA256',
  'ContentDisposition',
  'ContentEncoding',
  'ContentLanguage',
  'ContentLength',
  'ContentRange',
  'ContentType',
  'DeleteMarker',
  'ETag',
  'ErrorCode',
  'ErrorMessage',
  'Expiration',
  'Expires',
  'LastModified',
  'Metadata',
  'MissingMeta',
  'ObjectLockLegalHoldStatus',
  'ObjectLockMode',
  'ObjectLockRetainUntilDate',
  'PartsCount',
  'ReplicationStatus',
  'RequestCharged',
  'Restore',
  'SSECustomerAlgorithm',
  'SSECustomerKeyMD5',
  'SSEKMSKeyId',
  'ServerSideEncryption',
  'StatusCode',
  'StorageClass',
  'TagCount',
  'VersionId'
];
