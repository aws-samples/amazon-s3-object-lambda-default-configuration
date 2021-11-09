import mapRange from '../../src/response/range_mapper'

const HAS_ERROR = 'hasError'

test('Invalid range format returns error', () => {
  expect(mapRange('123', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true)
})

test('Multiple ranges returns error', () => {
  expect(mapRange('bytes=1-2-3', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true)
})

test('Unsupported range unit returns error', () => {
  expect(mapRange('kilobytes=0-10', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true)
})

test('Invalid range value returns error', () => {
  expect(mapRange('bytes=-', createBuffer('hello')))
    .toHaveProperty(HAS_ERROR, true)
})

test('Range with only start works', () => {
  expect(mapRange('bytes=2-', createBuffer('hello')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('llo') })
})

test('Range with suffix length works', () => {
  expect(mapRange('bytes=-2', createBuffer('hello')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('lo') })
})

test('Range with 0 start and end works', () => {
  expect(mapRange('bytes=0-3', createBuffer('hello')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('hell') })
})

test('Range with non-zero start and end works', () => {
  expect(mapRange('bytes=1-3', createBuffer('hello')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('ell') })
})

test('Two digit range value works', () => {
  expect(mapRange('bytes=10-15', createBuffer('amazonwebservices')))
    .toStrictEqual({ [HAS_ERROR]: false, object: createBuffer('ervice') })
})

function createBuffer (input: string): Buffer {
  return Buffer.from(input, 'utf-8')
}
