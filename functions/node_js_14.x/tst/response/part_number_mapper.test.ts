import mapPartNumber from '../../src/response/part_number_mapper'

const HAS_ERROR = 'hasError'
const TEN_KB = 'helloworld'.repeat(1024)
const HUNDRED_KB = TEN_KB.repeat(10)

test('Non-number part number returns error', () => {
  expect(mapPartNumber('abc', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true)
})
test('Non-positive part number returns error', () => {
  expect(mapPartNumber('-1', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true)
})

test('Non-integer part number returns error', () => {
  expect(mapPartNumber('1.5', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true)
})

test('Large part number returns error', () => {
  expect(mapPartNumber('10', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true)
})

test('Valid part number works', () => {
  expect(mapPartNumber('1', createBuffer(HUNDRED_KB)))
    .toStrictEqual({ object: createBuffer(TEN_KB), [HAS_ERROR]: false })
})

test('Valid part number works even if length is not exactly divisible', () => {
  expect(mapPartNumber('2', createBuffer(HUNDRED_KB + 'hundredkilobytes')))
    .toStrictEqual({ object: createBuffer(TEN_KB), [HAS_ERROR]: false })
})

function createBuffer (input: string): Buffer {
  return Buffer.from(input, 'utf-8')
}
