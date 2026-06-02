package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBasicLightBishopKnightHelpmateVerifier {

  @SuppressWarnings("static-method")
  @Test
  void kbnvKLightBishopCertificateVerifiesTheoremRoots() {
    final var result = BasicLightBishopKnightHelpmateVerifier.verify();

    assertEquals(12268044, result.legalStateCount());
    assertEquals(232, result.terminalCheckmateCount());
    assertEquals(12257062, result.winningStateCount());
    assertEquals(12256830, result.witnessStateCount());
    assertEquals(232, result.verifiedTerminalCount());
    assertEquals(12256830, result.verifiedWitnessCount());
    assertEquals(6819138, result.verifiedTheoremRootCount());
    assertEquals(15, result.maximumWhiteToMoveDistance());
    assertEquals(16, result.maximumDistance());
  }
}
