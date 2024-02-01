package net.messagevortex.router.operation;

import java.util.Random;
import net.messagevortex.asn1.encryption.Prng;

/***
 * <p>Wrapper for the java random number generator (not normative).</p>
 */
public class SimplePrng implements Prng {

  private final Random sr = new Random();
  private final long seed;

  public SimplePrng() {
    seed = sr.nextLong();
    sr.setSeed(seed);
  }

  public SimplePrng(long seed) {
    this.seed = seed;
  }


  /***
   * <p>Get the next random byte of the Prng.</p>
   *
   * @return the next random byte
   */
  public synchronized byte nextByte() {
    byte[] a = new byte[1];
    sr.nextBytes(a);
    return a[0];
  }

  /***
   * <p>Resets the Prng to the initially seeded state.</p>
   */
  public void reset() {
    sr.setSeed(seed);
  }
}