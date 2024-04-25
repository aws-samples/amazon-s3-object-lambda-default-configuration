import { UserRequest } from '../../src/s3objectlambda_event.types';
import { applyRangeOrPartNumber, getPartNumber, getRange } from '../../src/request/utils';

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
