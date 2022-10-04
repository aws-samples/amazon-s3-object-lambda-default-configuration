import getChecksum from '../../src/checksum/checksum';

test('Checksum is MD5 and works', () => {
  const algorithm = 'md5';
  const sample = 'sample';
  const md5Sample = '5e8ff9bf55ba3508199d22e984129be6';
  expect(getChecksum(Buffer.from(sample))).toEqual({
    algorithm,
    digest: md5Sample
  });
});
