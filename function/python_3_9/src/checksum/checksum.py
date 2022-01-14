import hashlib
from collections import namedtuple


def get_checksum(object):
    """
    Get the md5 checksum of an object
    :param object: The object you would like to obtain checksum
    :return: Checksum object with algorithm name and digest
    """
    hash_md5 = hashlib.md5()
    hash_md5.update(object)
    Checksum = namedtuple('Checksum', ['algorithm', 'digest'])
    return Checksum(hash_md5.name, hash_md5.hexdigest())
