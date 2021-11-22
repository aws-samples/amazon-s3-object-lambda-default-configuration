import ErrorCode from '../error/error_code';

interface RangeResponse {
  object?: Buffer
  hasError: boolean
  errorCode?: ErrorCode
  errorMessage?: string
}

export default RangeResponse;
