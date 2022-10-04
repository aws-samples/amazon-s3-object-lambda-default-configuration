import { mapPartNumber, mapPartNumberHead } from '../../src/response/part_number_mapper';
import { CONTENT_LENGTH } from '../../src/request/utils';

const HAS_ERROR = 'hasError';
const FIVE_MB = 'a'.repeat(5242880);
const TEN_MB = FIVE_MB.repeat(2);
const FIVE_MB_CONTENT_LENGTH = 5242880;
const TEN_MB_CONTENT_LENGTH = 10485760;

test('Non-number part number returns error', () => {
  expect(mapPartNumber('abc', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Non-positive part number returns error', () => {
  expect(mapPartNumber('-1', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Non-integer part number returns error', () => {
  expect(mapPartNumber('1.5', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Large part number returns error', () => {
  expect(mapPartNumber('10', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Valid part number works', () => {
  expect(mapPartNumber('1', createBuffer(TEN_MB)))
    .toStrictEqual({ object: createBuffer(FIVE_MB), [HAS_ERROR]: false });
});

test('Non-number part number for head returns error', () => {
  expect(mapPartNumberHead('abc', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
});

test('Non-positive part number for head returns error', () => {
  expect(mapPartNumberHead('-1', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
});

test('Non-integer part number for head returns error', () => {
  expect(mapPartNumberHead('1.5', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
});

test('Large part number returns error', () => {
  expect(mapPartNumberHead('10', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
});

test('Valid part number for head works', () => {
  expect(mapPartNumberHead('1', createHeaders(TEN_MB_CONTENT_LENGTH)))
    .toStrictEqual({
      headers: new Map([
        [CONTENT_LENGTH, Object(FIVE_MB_CONTENT_LENGTH)]
      ]),
      [HAS_ERROR]: false
    });
});

test('Valid part number for head works even if length is not exactly divisible', () => {
  expect(mapPartNumberHead('2', createHeaders(FIVE_MB_CONTENT_LENGTH + 2)))
    .toStrictEqual({
      headers: new Map([
        [CONTENT_LENGTH, Object(2)]
      ]),
      [HAS_ERROR]: false
    });
});

function createBuffer (input: string): Buffer {
  return Buffer.from(input, 'utf-8');
}

function createHeaders (contentLength?: number): Map<string, object> {
  const headers = new Map<string, object>();
  if (contentLength !== undefined) {
    headers.set(CONTENT_LENGTH, Object(contentLength));
  }
  return headers;
}
