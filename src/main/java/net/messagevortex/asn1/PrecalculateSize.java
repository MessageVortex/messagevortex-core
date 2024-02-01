package net.messagevortex.asn1;

import java.io.IOException;

public interface PrecalculateSize {
  /***
   * Gets a prediction based on the current workspace IDs.
   *
   * @return The predicted total output size
   * @throws IOException if the size cannot be calculated based of the current parameters
   */
  int getTargetSize() throws IOException;

  /***
   * Gets a prediction based on the sizes of the input blocks offered.
   *
   * @param sizeOfInputBlocks Array of sizes of input block
   *
   * @return The predicted total output size
   * @throws IOException if the size cannot be calculated based of the current parameters
   */
  int getTargetSize(int[] sizeOfInputBlocks) throws IOException;
}
