import { headerToWgorParam } from '../../src/response/param_transformer';

test('headerToWgorParam works for one dash', () => {
  expect(headerToWgorParam('content-type')).toStrictEqual('ContentType');
});

test('headerToWgorParam works for two dashes', () => {
  expect(headerToWgorParam('x-amz-server-side-encryption')).toStrictEqual('ServerSideEncryption');
});

test('headerToWgorParam works for CRC', () => {
  expect(headerToWgorParam('x-amz-checksum-crc32')).toStrictEqual('ChecksumCRC32');
  expect(headerToWgorParam('x-amz-checksum-crc32c')).toStrictEqual('ChecksumCRC32C');
});

test('headerToWgorParam works for SHA', () => {
  expect(headerToWgorParam('x-amz-checksum-sha1')).toStrictEqual('ChecksumSHA1');
  expect(headerToWgorParam('x-amz-checksum-sha256')).toStrictEqual('ChecksumSHA256');
});
