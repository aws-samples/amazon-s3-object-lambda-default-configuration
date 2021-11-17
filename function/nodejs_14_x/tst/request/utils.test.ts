import { UserRequest } from '../../src/s3objectlambda_event'
import { getPartNumber, getRange } from '../../src/request/utils'

test('Get PartNumber works', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?partNumber=1', headers: { h1: 'v1' } }
  expect(getPartNumber(userRequest)).toBe('1')
})

test('Get PartNumber works even when case is different', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?hello=world&PARTnumber=1', headers: { h1: 'v1' } }
  expect(getPartNumber(userRequest)).toBe('1')
})

test("PartNumber is null when it isn't present", () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?Range=1', headers: { h1: 'v1' } }
  expect(getPartNumber(userRequest)).toBe(null)
})

test('Get Range from query parameters works', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?range=bytes=1', headers: { h1: 'v1' } }
  expect(getRange(userRequest)).toBe('bytes=1')
})

test('Get Range from query parameters works even when case is different', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com?raNGe=bytes=1', headers: { h1: 'v1' } }
  expect(getRange(userRequest)).toBe('bytes=1')
})

test('Get Range from headers works', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com', headers: { Range: 'bytes=3-' } }
  expect(getRange(userRequest)).toBe('bytes=3-')
})

test('Get Range from headers works even when case is different', () => {
  const userRequest: UserRequest = { url: 'https://s3.amazonaws.com', headers: { RANge: 'bytes=3-' } }
  expect(getRange(userRequest)).toBe('bytes=3-')
})
