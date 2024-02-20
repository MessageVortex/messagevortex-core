package net.messagevortex.test.asn1;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AddRedundancyOperation;
import net.messagevortex.asn1.DecryptPayloadOperation;
import net.messagevortex.asn1.EncryptPayloadOperation;
import net.messagevortex.asn1.MapBlockOperation;
import net.messagevortex.asn1.Operation;
import net.messagevortex.asn1.RemoveRedundancyOperation;
import net.messagevortex.asn1.SizeBlock;
import net.messagevortex.asn1.SplitPayloadOperation;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GlobalJunitExtension.class)
public class OperationDumpParseTest {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Test
  public void allOperationSerializationTest() throws IOException {
    Operation[] ops = {
        new MapBlockOperation(1,2),
        new EncryptPayloadOperation(1,2,new SymmetricKey(Algorithm.AES128)),
        new DecryptPayloadOperation(1,2,new SymmetricKey(Algorithm.AES128)),
        new AddRedundancyOperation(1,1,1, Arrays.asList(new SymmetricKey[] {new SymmetricKey(Algorithm.AES128),new SymmetricKey(Algorithm.AES128)} ),2,8),
        new RemoveRedundancyOperation(2,1,1, Arrays.asList(new SymmetricKey[] {new SymmetricKey(Algorithm.AES128),new SymmetricKey(Algorithm.AES128)} ),1,8),
        new SplitPayloadOperation(1,new SizeBlock(SizeBlock.SizeType.PERCENT,50,100),2,3)
    };
    for(Operation op:ops) {
      LOGGER.log(Level.INFO,"testing operation tagged "+op.getTagNumber()+"\r\n"+op.dumpValueNotation("",DumpType.ALL));
      Operation op2=Operation.getInstance(op.toAsn1Object(DumpType.ALL));
      Assertions.assertEquals(op.dumpValueNotation("",DumpType.ALL),op2.dumpValueNotation("",DumpType.ALL),"Failed equalization test for type "+op.getTagNumber());
    }
  }

}
