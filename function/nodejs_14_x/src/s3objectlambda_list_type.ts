export interface IBaseListObject {
  IsTruncated: boolean
  EncodingType: string
  MaxKeys: number
  Prefix: string
  Contents: IObject[]
  Delimiter: string
  CommonPrefixes: ICommonPrefix[]
}

export interface IOwner {
  DisplayName?: string
  ID?: string
}

export interface IObject {
  ChecksumAlgorithm?: 'CRC32' | 'CRC32C' | 'SHA1' | 'SHA256'
  ETag?: string
  Key?: string
  LastModified?: string
  Size?: number
  StorageClass?: 'STANDARD' | 'REDUCED_REDUNDANCY' | 'GLACIER' | 'STANDARD_IA' | 'ONEZONE_IA' |
  'INTELLIGENT_TIERING' | 'DEEP_ARCHIVE' | 'OUTPOSTS' | 'GLACIER_IR'
  Owner?: IOwner
}

export interface IListObjectsV1 extends IBaseListObject {
  Marker: string
  NextMarker: string
}

export interface IListObjectsV2 extends IBaseListObject {
  ContinuationToken: string
  NextContinuationToken: string
  StartAfter: string
  KeyCount: string
}

export interface ICommonPrefix {
  Prefix: string
}
