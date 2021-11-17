/*
     At present, this function simply passes back the original object without performing any transformations.
     Implement this function to add your custom logic to transform objects stored in Amazon S3.
     */

export default function transformObject (originalObject: Buffer): Buffer {
  // TODO: Implement your transformation logic here.
  return originalObject;
}
