import { mapRange, mapRangeHead } from '../../src/response/range_mapper';
import { CONTENT_LENGTH } from '../../src/request/utils';

const HAS_ERROR = 'hasError';
const FIVE_MB_CONTENT_LENGTH = 5242880;

test('Invalid range format returns error', () => {
  expect(mapRange('123', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Multiple ranges returns error', () => {
  expect(mapRange('bytes=1-2-3', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Unsupported range unit returns error', () => {
  expect(mapRange('kilobytes=0-10', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Invalid range value returns error', () => {
  expect(mapRange('bytes=-', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Range with only start works', () => {
  expect(mapRange('bytes=2-', createBuffer('hello')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('llo') });
});

test('Range with suffix length works', () => {
  expect(mapRange('bytes=-2', createBuffer('hello')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('lo') });
});

test('Range with 0 start and end works', () => {
  expect(mapRange('bytes=0-3', createBuffer('hello')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('hell') });
});

test('Range with non-zero start and end works', () => {
  expect(mapRange('bytes=1-3', createBuffer('hello')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('ell') });
});

test('Two digit range value works', () => {
  expect(mapRange('bytes=10-15', createBuffer('amazonwebservices')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('ervice') });
});

test('Range End > Range Start returns error', () => {
  expect(mapRange('bytes=15-10', createBuffer('amazonwebservices')))
    .toHaveProperty(HAS_ERROR, true);
});

test('Invalid head range format returns error', () => {
  expect(mapRangeHead('123', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
});

test('Multiple head ranges returns error', () => {
  expect(mapRangeHead('bytes=1-2-3', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
});

test('Unsupported head range unit returns error', () => {
  expect(mapRangeHead('kilobytes=0-10', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
});

test('Invalid head range value returns error', () => {
  expect(mapRangeHead('bytes=-', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
});

test('Head Range with only start works', () => {
  expect(mapRangeHead('bytes=2-', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toStrictEqual({
      headers: new Map([
        [CONTENT_LENGTH, Object(FIVE_MB_CONTENT_LENGTH - 2)]
      ]),
      [HAS_ERROR]: false
    });
});

test('Head Range with suffix length works', () => {
  expect(mapRangeHead('bytes=-2', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toStrictEqual({
      headers: new Map([
        [CONTENT_LENGTH, Object(2)]
      ]),
      [HAS_ERROR]: false
    });
});

test('Head Range with 0 start and end works', () => {
  expect(mapRangeHead('bytes=0-3', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toStrictEqual({
      headers: new Map([
        [CONTENT_LENGTH, Object(4)]
      ]),
      [HAS_ERROR]: false
    });
});

test('Head Range with non-zero start and end works', () => {
  expect(mapRangeHead('bytes=1-3', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toStrictEqual({
      headers: new Map([
        [CONTENT_LENGTH, Object(3)]
      ]),
      [HAS_ERROR]: false
    });
});

test('Two digit range head value works', () => {
  expect(mapRangeHead('bytes=10-15', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toStrictEqual({
      headers: new Map([
        [CONTENT_LENGTH, Object(6)]
      ]),
      [HAS_ERROR]: false
    });
});

test('Head Range End > Range Start returns error', () => {
  expect(mapRangeHead('bytes=15-10', createHeaders(FIVE_MB_CONTENT_LENGTH)))
    .toHaveProperty(HAS_ERROR, true);
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
