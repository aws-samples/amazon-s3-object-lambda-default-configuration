import { UserRequest } from '../../src/s3objectlambda_event.types';
import { applyRangeOrPartNumber, getPartNumber, getRange, getSignedHeaders } from '../../src/request/utils';

test('GetSignedHeaders works for a URL with a single signed header', () => {

  const presigned_url: string = 'https://test-access-point-012345678901.s3-accesspoint.us-east-1.amazonaws.com/test?' +
        'X-Amz-Security-Token=TestToken' +
        '&X-Amz-Algorithm=AWS4-HMAC-SHA256' +
        '&X-Amz-Date=20250220T175710Z' +
        '&X-Amz-SignedHeaders=host%3Bx-amz-checksum-mode' +
        '&X-Amz-Expires=61' +
        '&X-Amz-Credential=AKIAEXAMPLE/20250220/us-east-1/s3/aws4_request' +
        '&X-Amz-Signature=a7f9b2c8e4d1f3a6b5c9e2d7a8f4b3c1d6e5f2a9b7c8d3e1f6a2b9c7d5e8f3a1';

  const result = getSignedHeaders(presigned_url);
  expect(result.length).toBe(2);
  expect(result[0]).toBe("host");
  expect(result[1]).toBe("x-amz-checksum-mode");
});

test('GetSignedHeaders works for a URL with a 2 signed headers', () => {

  const presigned_url: string = 'https://test-access-point-012345678901.s3-accesspoint.us-east-1.amazonaws.com/test?' +
      'X-Amz-Security-Token=TestToken' +
      '&X-Amz-Algorithm=AWS4-HMAC-SHA256' +
      '&X-Amz-Date=20250220T175710Z' +
      '&X-Amz-SignedHeaders=host' +
      '&X-Amz-Expires=61' +
      '&X-Amz-Credential=AKIAEXAMPLE/20250220/us-east-1/s3/aws4_request' +
      '&X-Amz-Signature=a7f9b2c8e4d1f3a6b5c9e2d7a8f4b3c1d6e5f2a9b7c8d3e1f6a2b9c7d5e8f3a1';

  const result = getSignedHeaders(presigned_url);
  expect(result.length).toBe(1);
  expect(result[0]).toBe("host");
});

test('GetSignedHeaders works for a URL with no signed headers', () => {

  const presigned_url: string = 'https://test-access-point-012345678901.s3-accesspoint.us-east-1.amazonaws.com/test?' +
      'X-Amz-Security-Token=TestToken' +
      '&X-Amz-Algorithm=AWS4-HMAC-SHA256' +
      '&X-Amz-Date=20250220T175710Z' +
      '&X-Amz-Expires=61' +
      '&X-Amz-Credential=AKIAEXAMPLE/20250220/us-east-1/s3/aws4_request' +
      '&X-Amz-Signature=a7f9b2c8e4d1f3a6b5c9e2d7a8f4b3c1d6e5f2a9b7c8d3e1f6a2b9c7d5e8f3a1';

  const result = getSignedHeaders(presigned_url);
  expect(result.length).toBe(0);
});

test('Get PartNumber works', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?partNumber=1', headers: { h1: 'v1' } };
  expect(getPartNumber(userRequest)).toBe('1');
});

test('Get PartNumber works even when case is different', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?hello=world&PARTnumber=1', headers: { h1: 'v1' } };
  expect(getPartNumber(userRequest)).toBe('1');
});

test("PartNumber is null when it isn't present", () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?Range=1', headers: { h1: 'v1' } };
  expect(getPartNumber(userRequest)).toBe(null);
});

test('Get Range from query parameters works', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?range=bytes=1', headers: { h1: 'v1' } };
  expect(getRange(userRequest)).toBe('bytes=1');
});

test('Get Range from query parameters works even when case is different', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?raNGe=bytes=1', headers: { h1: 'v1' } };
  expect(getRange(userRequest)).toBe('bytes=1');
});

test('Get Range from headers works', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com', headers: { Range: 'bytes=3-' } };
  expect(getRange(userRequest)).toBe('bytes=3-');
});

test('Get Range from headers works even when case is different', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com', headers: { RANge: 'bytes=3-' } };
  expect(getRange(userRequest)).toBe('bytes=3-');
});

test('ApplyRangeOrPartNumber without range or part can return the original object', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com', headers: { h1: 'v1' } };
  const transformedObject = Buffer.from('single-object');
  expect(applyRangeOrPartNumber(transformedObject, userRequest)).toStrictEqual({
    object: transformedObject,
    hasError: false
  });
});

test('Apply Range or Part Number applies range', () => {
  const rangeValue = 'bytes=3-';
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com', headers: { Range: rangeValue } };
  const transformedObject = Buffer.from('single-object');
  const range = getRange(userRequest);
  expect(range).toBe(rangeValue);
  expect(applyRangeOrPartNumber(transformedObject, userRequest)).toStrictEqual(
    {
      object: Buffer.from('gle-object'),
      hasError: false
    });
});

test('Apply Range or Part Number applies part number', () => {
  const partNumber = '1';
  const partSize = 5242880; // 5 MB in Bytes
  const userRequest: UserRequest = { url: `https://s3.amazonaws.com?partNumber=${partNumber}`, headers: { h1: 'v1' } };
  const transformedObject = Buffer.from('0'.repeat(1 * partSize * 2)); // Create a 10MB object
  const appliedPartNumber = applyRangeOrPartNumber(transformedObject, userRequest);
  expect(appliedPartNumber).not.toBeNull();
  expect(appliedPartNumber.object?.length).toBe(partSize);
});
