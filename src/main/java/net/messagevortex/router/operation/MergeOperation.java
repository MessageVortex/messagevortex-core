package net.messagevortex.router.operation;

import java.io.IOException;
import java.io.Serializable;
import net.messagevortex.MessageVortexLogger;

public class MergeOperation extends AbstractOperation implements Serializable {

  public static final long serialVersionUID = 1010283217391L;
  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  net.messagevortex.asn1.RemoveRedundancyOperation operation;

  public MergeOperation(net.messagevortex.asn1.RemoveRedundancyOperation op) {
    this.operation = op;
  }

  @Override
  public int[] getOutputId() {
    int[] ret = new int[operation.getDataStripes() + operation.getRedundancy()];
    int id = operation.getOutputId();
    for (int i = 0; i < ret.length; i++) {
      ret[i] = id + i;
    }
    return ret;
  }

  @Override
  public int[] getInputId() {
    int[] ret = new int[1];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = operation.getInputId() + i;
    }
    return ret;
  }

  @Override
  public int[] execute(int[] id) {
    return new int[0];
  }

  @Override
  public boolean canRun() {
    return payload.getPayload(operation.getInputId()) != null && payload.getPayload(operation.getInputId()+1) != null;
  }

  @Override
  public int getTargetSize() throws IOException {
    if(!canRun()) {
      throw new IOException("cannot calculate target size as not all prerequisites are met");
    }
    return getTargetSize(new int[] {payload.getPayload(operation.getInputId()).getPayload().length,payload.getPayload(operation.getInputId()+1).getPayload().length});
  }

  @Override
  public int getTargetSize(int[] sizeOfInputBlocks) throws IOException {
    if(sizeOfInputBlocks.length!=2) {
      throw new IOException("unexpected number of sizes");
    }
    if(sizeOfInputBlocks[0]<0) {
      throw new IOException("unexpected size of input block");
    }
    if(sizeOfInputBlocks[1]<0) {
      throw new IOException("unexpected size of input block");
    }
    return sizeOfInputBlocks[0]+sizeOfInputBlocks[1];
  }
}
