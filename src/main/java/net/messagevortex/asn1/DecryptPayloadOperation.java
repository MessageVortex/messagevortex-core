package net.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.io.Serializable;

public class DecryptPayloadOperation extends AbstractCryptPayloadOperation implements Serializable {

  public static final long serialVersionUID = 100000000030L;

  final static int tagNumber=310;

  DecryptPayloadOperation() {
  }

  /***
   * <p>Constructor to create an decrypt operation.</p>
   *
   * @param sourceBlock the ID of the source block in the workspace
   * @param targetBlock the ID of the target block in the workspace
   * @param key the key to be used for decryption
   *
   * @throws IOException if key generation fails when creating a new key
   */
  public DecryptPayloadOperation(int sourceBlock,int targetBlock, SymmetricKey key)
          throws IOException {
    this();
    if (key == null) {
      key = new SymmetricKey();
    }
    this.originalId = sourceBlock;
    this.newId = targetBlock;
    this.key = key;
  }

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param object the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  DecryptPayloadOperation(ASN1Encodable object) throws IOException {
    super(object);
  }

  @Override
  public Operation getNewInstance(ASN1Encodable object) throws IOException {
    return new DecryptPayloadOperation(object);
  }

  @Override
  public int getTagNumber() {
    return tagNumber;
  }

}
