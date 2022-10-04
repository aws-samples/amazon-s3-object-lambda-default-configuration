export interface IResponse {
  statusCode: number
}

export interface IHeadObjectResponse extends IResponse {
  metadata?: object
  headers: object
}

export interface IListObjectsResponse extends IResponse{
  listResultXml: string
}

export interface IErrorResponse extends IResponse {
  readonly errorCode?: string
  readonly errorMessage?: string
}
