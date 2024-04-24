import { transformObject } from '../../src/transform/s3objectlambda_transformer';

test('Transform function returns same object', () => {
  expect(transformObject(Buffer.from('test-object'))).toStrictEqual(
    Buffer.from('test-object')
  );
});

// TODO Add tests for the other identity functions
