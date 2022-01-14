# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

import boto3
import logging
from handler import get_object_handler


logger = logging.getLogger()
logger.setLevel(logging.INFO)

s3_client = boto3.client('s3')


def handler(event, context):
    """ handles the request from Amazon S3 Object Lambda"""

    """
    <p>The getObjectContext object contains information about the GetObject request,
    which resulted in this Lambda function being invoked.</p>
    <p>The userRequest object contains information related to the entity (user or application)
    that invoked Amazon S3 Object Lambda. This information can be used in multiple ways, for example, to allow or deny
    the request based on the entity. See the <i>Respond with a 403 Forbidden</i> example in
    {@link https://docs.aws.amazon.com/AmazonS3/latest/userguide/olap-writing-lambda.html|Writing Lambda functions}
    for sample code.</p>
    """
    if "getObjectContext" in event:
        return get_object_handler.get_object_handler(s3_client, event["getObjectContext"], event["userRequest"])

    # There is nothing to return once the data has been sent to Amazon S3 Object Lambda, so just return None.
    return None

