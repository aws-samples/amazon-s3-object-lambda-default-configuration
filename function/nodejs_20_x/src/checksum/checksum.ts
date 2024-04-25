import { createHash } from 'crypto';

const CHECKSUM_ALGORITHM = 'md5';

interface Checksum {
  algorithm: string
  digest: string
}

/**
 * Generates a checksum for the given object.
 *
 * @param object
 */
export default function getChecksum (object: Buffer): Checksum {
  const hash = createHash(CHECKSUM_ALGORITHM);
  hash.update(object);
  return { algorithm: CHECKSUM_ALGORITHM, digest: hash.digest('hex') };
}
