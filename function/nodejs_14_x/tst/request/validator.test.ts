import { validate } from '../../src/request/validator';
import { UserRequest } from '../../src/s3objectlambda_event.types';

test('Validation fails when both partNumber and range is provided', () => {
  const userRequest: UserRequest = {
    url: 'https://s3.amazonaws.com?partNumber=1',
    headers: { h1: 'v1', Range: 'bytes=2-' }
  };

  expect(validate(userRequest)).toBe('Cannot specify both Range header and partNumber query parameter');
});

test('Validation is successful when partNumber is provided', () => {
  const userRequest: UserRequest = {
    url: 'https://s3.amazonaws.com?partNumber=1',
    headers: { h1: 'v1'}
  };

  expect(validate(userRequest)).toBeNull();
});

test('Validation is successful when Range is provided', () => {
  const userRequest: UserRequest = {
    url: 'https://s3.amazonaws.com',
    headers: { h1: 'v1', Range: 'bytes=2-' }
  };

  expect(validate(userRequest)).toBeNull();
});
