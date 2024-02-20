package net.messagevortex.asn1;

enum OperationType {
  SPLIT_PAYLOAD(SplitPayloadOperation.tagNumber, new SplitPayloadOperation()),
  MERGE_PAYLOAD(MergePayloadOperation.tagNumber, new MergePayloadOperation()),
  ENCRYPT_PAYLOAD(EncryptPayloadOperation.tagNumber, new EncryptPayloadOperation()),
  DECRYPT_PAYLOAD(DecryptPayloadOperation.tagNumber, new DecryptPayloadOperation()),
  ADD_REDUNDANCY(AddRedundancyOperation.tagNumber, new AddRedundancyOperation()),
  REMOVE_REDUNDANCY(RemoveRedundancyOperation.tagNumber, new RemoveRedundancyOperation()),
  MAP(MapBlockOperation.tagNumber, new MapBlockOperation());

  public final int id;
  Operation operation;

  OperationType(int id, Operation operation) {
    this.id = id;
    this.operation = operation;
  }

  int getId() {
    return id;
  }

  Operation getFactory() {
    return operation;
  }

  /***
   * <p>Look up an algorithm by id.</p>
   *
   * @param id     the idto be looked up
   * @return the algorithm or null if not known
   */
  public static OperationType getById(int id) {
    for (OperationType e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }
}
