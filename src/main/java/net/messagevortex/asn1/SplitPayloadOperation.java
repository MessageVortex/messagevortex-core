package net.messagevortex.asn1;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

import java.io.IOException;
import java.io.Serializable;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * Splits a payload block in two blocks.
 */
public class SplitPayloadOperation extends Operation implements Serializable {

  public static final long serialVersionUID = 100000000023L;

  int originalId = -1;
  SizeBlock firstSize = null;
  int newFirstId = -1;
  int newSecondId = -1;

  final static int tagNumber=150;

  SplitPayloadOperation() {
  }

  public SplitPayloadOperation(ASN1Encodable object) throws IOException {
    this();
    parse(object);
  }

  public SplitPayloadOperation(int originalId, SizeBlock firstSize, int newFirstId, int newSecondId) throws IOException {
    this();
    this.originalId=originalId;
    this.firstSize=firstSize;
    this.newFirstId=newFirstId;
    this.newSecondId=newSecondId;
  }

  @Override
  protected final void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    int i = 0;
    originalId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    firstSize = new SizeBlock(s1.getObjectAt(i++));
    newFirstId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    newSecondId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append('[').append(tagNumber).append(']').append('{').append(CRLF);
    sb.append(prefix).append("  originalId ").append(originalId).append(',').append(CRLF);
    sb.append(prefix).append("  firstSize ").append(firstSize
        .dumpValueNotation(prefix + "  ", dumptype)).append(',').append(CRLF);
    sb.append(prefix).append("  newFirstId ").append(newFirstId).append(CRLF);
    sb.append(prefix).append("  newSecondId ").append(newSecondId).append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    ASN1EncodableVector s1 = new ASN1EncodableVector();
    s1.add(new ASN1Integer(originalId));
    s1.add(firstSize.toAsn1Object(dumpType));
    s1.add(new ASN1Integer(newFirstId));
    s1.add(new ASN1Integer(newSecondId));
    return new DERTaggedObject(true, tagNumber, new DERSequence(s1));
  }

  @Override
  public Operation getNewInstance(ASN1Encodable object) throws IOException {
    return new SplitPayloadOperation(object);
  }

  @Override
  public int getTagNumber() {
    return tagNumber;
  }

}
