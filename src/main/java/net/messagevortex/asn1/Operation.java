package net.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.io.Serializable;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * Represents a the Blending specification of the router block.
 */
public abstract class Operation extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000012L;

  /* constructor */
  Operation() {
  }

  /***
   * <p>Gets an instance of the object.</p>
   *
   * @param asn1Encodable the object to be parsed
   * @return the parsed operation object
   *
   * @throws IOException if parsing fails
   */
  public abstract Operation getNewInstance(ASN1Encodable asn1Encodable) throws IOException;

  public static Operation getInstance(ASN1Encodable obj) throws IOException {
    ASN1TaggedObject to = DERTaggedObject.getInstance(obj);
    return OperationType.getById(to.getTagNo()).getFactory().getNewInstance(to.getBaseObject());
  }

  public abstract int getTagNumber();
}
