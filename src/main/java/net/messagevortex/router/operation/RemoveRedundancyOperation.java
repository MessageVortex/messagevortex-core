package net.messagevortex.router.operation;

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

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.PayloadChunk;
import net.messagevortex.asn1.VortexMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * <p>This is the core of the redundancy remove operation.</p>
 *
 * <p>It rebuilds the data stream from the existing data blocks.</p>
 */
public class RemoveRedundancyOperation extends AbstractOperation implements Serializable {

  public static final long serialVersionUID = 100000000020L;
  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  net.messagevortex.asn1.RemoveRedundancyOperation operation;

  public RemoveRedundancyOperation(net.messagevortex.asn1.RemoveRedundancyOperation op) {
    this.operation = op;
  }

  @Override
  public boolean canRun() {
    if (payload == null) {
      return false;
    }
    int j = 0;
    for (int i : getInputId()) {
      if (payload.getPayload(i) != null) {
        j++;
      }
    }
    return j >= operation.getDataStripes();
  }

  @Override
  public int[] execute(int[] id) {
    if (!canRun()) {
      return new int[0];
    }
    LOGGER.log(Level.INFO, "executing add redundancy operation");

    // generate list of missing rows
    LOGGER.log(Level.INFO, "  analysing stripes");
    List<Integer> l = new ArrayList<>();
    int stripeSize = -1;
    int stripesFound = 0;
    for (int i : getInputId()) {
      PayloadChunk p = payload.getPayload(i);
      if (p == null || stripesFound >= operation.getDataStripes()) {
        l.add(i);
        if (p != null) {
          stripesFound++;
        }
      } else {
        stripesFound++;
        if (stripeSize == -1) {
          stripeSize = p.getPayload().length;
        } else if (stripeSize != p.getPayload().length) {
          return new int[0];
        }
      }
    }
    int[] missingIds = new int[l.size()];
    for (int i = 0; i < missingIds.length; i++) {
      missingIds[i] = l.get(i);
    }
    LOGGER.log(Level.INFO, "  got " + stripesFound + "/" + (operation.getDataStripes()
            + operation.getRedundancy()) + " stripes. Stripe size is " + stripeSize);

    // prepare data set
    byte[] in2 = new byte[stripeSize * (operation.getDataStripes() + operation.getRedundancy()
            - missingIds.length)];
    try {
      int j = 0;
      for (int i : getInputId()) {
        if (!l.contains(i)) {
          for (byte b : operation.getKeys()[i - getInputId()[0]]
                        .decrypt(payload.getPayload(i).getPayload())) {
            in2[j++] = b;
          }
        }
      }
      //assert j==in2.length;
    } catch (IOException ioe) {
      return new int[0];
    }
    MathMode mm = GaloisFieldMathMode.getGaloisFieldMathMode(operation.getGfSize());
    Matrix data = new Matrix(in2.length / (operation.getDataStripes() + operation.getRedundancy()
            - missingIds.length), operation.getDataStripes() + operation.getRedundancy()
            - missingIds.length, mm, in2);
    LOGGER.log(Level.INFO, "  created " + data.getX() + "x" + data.getY() + " data matrixContent");

    // do the redundancy calc
    RedundancyMatrix r = new RedundancyMatrix(operation.getDataStripes(),
            operation.getDataStripes() + operation.getRedundancy(), mm);
    LOGGER.log(Level.INFO, "  created " + r.getX() + "x" + r.getY() + " redundancy matrixContent");
    Matrix recovery = r.getRecoveryMatrix(missingIds);
    LOGGER.log(Level.INFO, "  created " + recovery.getX() + "x" + recovery.getY()
            + " recovery matrixContent");
    LOGGER.log(Level.INFO, "  reconstructing data");
    Matrix out = recovery.mul(data);

    // remove padding
    int paddingSize = 4;
    LOGGER.log(Level.INFO, "  removing padding");
    byte[] out1 = out.getAsByteArray();
    byte[] len = Arrays.copyOf(out1,paddingSize);
    int outputLength = (int) VortexMessage.getBytesAsLong(len);
    LOGGER.log(Level.INFO, "    message size is " + outputLength + " (padded: " + out1.length
            + ")");
    byte[] out2 = new byte[outputLength];
    System.arraycopy(out1, paddingSize, out2, 0, outputLength);
    // FIXME verify padding

    // set output
    LOGGER.log(Level.INFO, "  setting output");
    payload.setCalculatedPayload(getOutputId()[0], new PayloadChunk(getOutputId()[0], out2,
            getUsagePeriod()));

    LOGGER.log(Level.INFO, "  done");
    return getOutputId();
  }

  @Override
  public int[] getOutputId() {
    int[] ret = new int[operation.getDataStripes() + operation.getRedundancy()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = operation.getOutputId() + i;
    }
    return ret;
  }

  @Override
  public int[] getInputId() {
    int[] ret = new int[operation.getDataStripes() + operation.getRedundancy()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = operation.getInputId() + i;
    }
    return ret;
  }

  public String toString() {
    return getInputId()[0] + "->(" + getInputId().length + ")removeRedundancy("
            + getOutputId().length + ")->" + getOutputId()[0];
  }


  @Override
  public int getTargetSize() throws IOException {
    int[] sizes=new int[operation.getRedundancy()+ operation.getDataStripes()];
    for(int i=0;i<sizes.length;i++) {
      sizes[i]=payload.getPayload(getInputId()[i]).getPayload().length;
    }
    return getTargetSize(sizes);
  }

  @Override
  public int getTargetSize(int[] sizeOfInputBlocks) throws IOException {
    int dataBlockSize=-1;
    int numberOfDataBlocks=0;
    if(sizeOfInputBlocks.length<operation.getDataStripes()) {
      throw new IOException("Not enough input blocks");
    }
    if(sizeOfInputBlocks.length>operation.getDataStripes()+operation.getRedundancy()) {
      throw new IOException("too many input blocks");
    }
    for(int i=0;i<sizeOfInputBlocks.length;i++) {
      if(sizeOfInputBlocks[i]>0 && dataBlockSize==-1) {
        dataBlockSize=sizeOfInputBlocks[i];
        numberOfDataBlocks++;
      } else if(sizeOfInputBlocks[i]>0 && dataBlockSize==sizeOfInputBlocks[i]) {
        numberOfDataBlocks++;
      } else if(sizeOfInputBlocks[i]>0) {
        throw new IOException("Unexpected number of bytes in data block (expected:"+dataBlockSize+"; got:"+sizeOfInputBlocks[i]+")");
      }
    }
    if(dataBlockSize<1) {
      throw new IOException("No datablock bigger than 0");
    }
    if(numberOfDataBlocks<operation.getDataStripes()) {
      throw new IOException("Not enough data blocks (minimum:"+operation.getDataStripes()+"; delivered:"+numberOfDataBlocks+")");
    }
    int inputSize=sizeOfInputBlocks[0];
    // FIXME add padding prediction
    int ret=inputSize/operation.getDataStripes()*(operation.getRedundancy()+operation.getDataStripes());
    return ret;
  }
}
