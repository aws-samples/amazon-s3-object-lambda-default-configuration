import { IListObjectsV1, IListObjectsV2 } from '../s3objectlambda_list_type';

/*
  At present, this function simply passes back the original object without performing any transformations.
  Implement this function to add your custom logic to transform objects stored in Amazon S3.
*/
export function transformObject (originalObject: Buffer): Buffer {
  // TODO: Implement your transformation logic here.
  return originalObject;
}

export function transformListObjectsV1 (originalList: IListObjectsV1): IListObjectsV1 {
  // TODO: Implement your transformation logic here.
  return originalList;
}

export function transformListObjectsV2 (originalList: IListObjectsV2): IListObjectsV2 {
  // TODO: Implement your transformation logic here.
  return originalList;
}

export function transformHeaders (originalHeaders: Map<string, object>): Map<string, object> {
  // TODO: Implement your transformation logic here.
  return originalHeaders;
}
