package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicFourPieceHelpmateVerifier.Material.OPPOSITE_BISHOPS;
import static io.github.dlbbld.basiccheckmatereachability.BasicFourPieceHelpmateVerifier.Material.ROOK_KNIGHT;
import static io.github.dlbbld.basiccheckmatereachability.BasicFourPieceHelpmateVerifier.Material.ROOK_LIGHT_BISHOP;
import static io.github.dlbbld.basiccheckmatereachability.BasicFourPieceHelpmateVerifier.Material.TWO_KNIGHTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBasicFourPieceHelpmateVerifier {

  @SuppressWarnings("static-method")
  @Test
  void kbbvKCertificateVerifiesTheoremRoots() {
    final var result = BasicFourPieceHelpmateVerifier.verifyKbbvK();

    assertEquals(OPPOSITE_BISHOPS, result.material());
    assertEquals(5973472, result.legalStateCount());
    assertEquals(1552, result.terminalCheckmateCount());
    assertEquals(5960176, result.winningStateCount());
    assertEquals(5958624, result.witnessStateCount());
    assertEquals(1552, result.verifiedTerminalCount());
    assertEquals(5958624, result.verifiedWitnessCount());
    assertEquals(3454520, result.verifiedTheoremRootCount());
    assertEquals(15, result.maximumWhiteToMoveDistance());
    assertEquals(16, result.maximumDistance());
  }

  @SuppressWarnings("static-method")
  @Test
  void krvKbLightBishopCertificateVerifiesTheoremRoots() {
    final var result = BasicFourPieceHelpmateVerifier.verifyKrvKbLightBishop();

    assertEquals(ROOK_LIGHT_BISHOP, result.material());
    assertEquals(11306596, result.legalStateCount());
    assertEquals(3264, result.terminalCheckmateCount());
    assertEquals(11302800, result.winningStateCount());
    assertEquals(11299536, result.witnessStateCount());
    assertEquals(3264, result.verifiedTerminalCount());
    assertEquals(11299536, result.verifiedWitnessCount());
    assertEquals(5909180, result.verifiedTheoremRootCount());
    assertEquals(13, result.maximumWhiteToMoveDistance());
    assertEquals(14, result.maximumDistance());
  }

  @SuppressWarnings("static-method")
  @Test
  void krvKnCertificateVerifiesTheoremRoots() {
    final var result = BasicFourPieceHelpmateVerifier.verifyKrvKn();

    assertEquals(ROOK_KNIGHT, result.material());
    assertEquals(23315984, result.legalStateCount());
    assertEquals(9328, result.terminalCheckmateCount());
    assertEquals(23308736, result.winningStateCount());
    assertEquals(23299408, result.witnessStateCount());
    assertEquals(9328, result.verifiedTerminalCount());
    assertEquals(23299408, result.verifiedWitnessCount());
    assertEquals(12518712, result.verifiedTheoremRootCount());
    assertEquals(13, result.maximumWhiteToMoveDistance());
    assertEquals(14, result.maximumDistance());
  }

  @SuppressWarnings("static-method")
  @Test
  void knnvKCertificateVerifiesTheoremRoots() {
    final var result = BasicFourPieceHelpmateVerifier.verifyKnnvK();

    assertEquals(TWO_KNIGHTS, result.material());
    assertEquals(12579944, result.legalStateCount());
    assertEquals(120, result.terminalCheckmateCount());
    assertEquals(12574248, result.winningStateCount());
    assertEquals(12574128, result.witnessStateCount());
    assertEquals(120, result.verifiedTerminalCount());
    assertEquals(12574128, result.verifiedWitnessCount());
    assertEquals(6824476, result.verifiedTheoremRootCount());
    assertEquals(15, result.maximumWhiteToMoveDistance());
    assertEquals(16, result.maximumDistance());
  }
}
