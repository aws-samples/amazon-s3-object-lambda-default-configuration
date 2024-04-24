import { Builder, Parser } from 'xml2js';
import { parseBooleans } from 'xml2js/lib/processors';
import { IBaseListObject } from '../s3objectlambda_list_type';

export class ListObjectsXmlTransformer <T extends IBaseListObject> {
  /**
     * Converts back from a ListObject to a string in an XML format
     * @param listObject The ListObjects which we want to obtain an XML string from
     * @private
     */
  public createListObjectsXmlResponse (listObject: T): string | null {
    const builder = new Builder({
      rootName: 'ListBucketResult'
    });

    const xmlListObject = builder.buildObject(listObject);

    if (xmlListObject == null) {
      console.log('Failed building back the XML');
      return null;
    }

    return xmlListObject;
  }

  /**
     * Used to force the Contents property to be an array in the converted object from XML.
     * If the result is only one key, then the parser creates an object instead of an array
     * @param result The JSON object after the conversion from XML
     * @private
     */
  private static makeContentsAsArray (result: any): void {
    if (result.Contents !== undefined && !Object.getOwnPropertyNames(result.Contents).includes('length')
    ) {
      result.Contents = [result.Contents];
    }
  }

  /**
     * Transforms from a xml containing a string to the corresponding JSON object
     * @param xml A string in the XML format
     */
  public async createListObjectsJsonResponse (xml: string): Promise < T | null > {
    const parser = new Parser({
      explicitArray: false,
      explicitRoot: false,
      valueProcessors: [parseBooleans]
    });

    const parsedXML = parser.parseStringPromise(xml);
    if (parsedXML === null
    ) {
      return null;
    }

    const listObjectResponse = await parsedXML as T;

    ListObjectsXmlTransformer.makeContentsAsArray(listObjectResponse);
    return listObjectResponse;
  }
}
