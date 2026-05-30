package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicFourPieceHelpMateVerifier.Material.OPPOSITE_BISHOPS;
import static io.github.dlbbld.basiccheckmatereachability.BasicFourPieceHelpMateVerifier.Material.ROOK_KNIGHT;
import static io.github.dlbbld.basiccheckmatereachability.BasicFourPieceHelpMateVerifier.Material.ROOK_LIGHT_BISHOP;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBasicFourPieceHelpMateVerifier {

  @SuppressWarnings("static-method")
  @Test
  void kbbvKCertificateVerifiesTheoremRoots() {
    final var result = BasicFourPieceHelpMateVerifier.verifyKbbvK();

    assertEquals(OPPOSITE_BISHOPS, result.material());
    assertEquals(5973472, result.legalStateCount());
    assertEquals(1552, result.terminalCheckmateCount());
    assertEquals(5960176, result.winningStateCount());
    assertEquals(5958624, result.witnessStateCount());
    assertEquals(1552, result.verifiedTerminalCount());
    assertEquals(5958624, result.verifiedWitnessCount());
    assertEquals(3454520, result.verifiedTheoremRootCount());
    assertEquals(16, result.maximumDistance());
  }

  @SuppressWarnings("static-method")
  @Test
  void krvKbLightBishopCertificateVerifiesTheoremRoots() {
    final var result = BasicFourPieceHelpMateVerifier.verifyKrvKbLightBishop();

    assertEquals(ROOK_LIGHT_BISHOP, result.material());
    assertEquals(11306596, result.legalStateCount());
    assertEquals(3264, result.terminalCheckmateCount());
    assertEquals(11302800, result.winningStateCount());
    assertEquals(11299536, result.witnessStateCount());
    assertEquals(3264, result.verifiedTerminalCount());
    assertEquals(11299536, result.verifiedWitnessCount());
    assertEquals(5909180, result.verifiedTheoremRootCount());
    assertEquals(14, result.maximumDistance());
  }

  @SuppressWarnings("static-method")
  @Test
  void krvKnCertificateVerifiesTheoremRoots() {
    final var result = BasicFourPieceHelpMateVerifier.verifyKrvKn();

    assertEquals(ROOK_KNIGHT, result.material());
    assertEquals(23315984, result.legalStateCount());
    assertEquals(9328, result.terminalCheckmateCount());
    assertEquals(23308736, result.winningStateCount());
    assertEquals(23299408, result.witnessStateCount());
    assertEquals(9328, result.verifiedTerminalCount());
    assertEquals(23299408, result.verifiedWitnessCount());
    assertEquals(12518712, result.verifiedTheoremRootCount());
    assertEquals(14, result.maximumDistance());
  }
}
