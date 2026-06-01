package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.ROOK;
import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.QUEEN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBasicMajorPieceHelpMateVerifier {

  @SuppressWarnings("static-method")
  @Test
  void krvKCertificateVerifiesTheoremRoots() {
    final var result = BasicMajorPieceHelpMateVerifier.verifyKrvK();

    assertEquals(ROOK, result.whiteMajorPiece());
    assertEquals(399112, result.legalStateCount());
    assertEquals(216, result.terminalCheckmateCount());
    assertEquals(398632, result.winningStateCount());
    assertEquals(398416, result.witnessStateCount());
    assertEquals(216, result.verifiedTerminalCount());
    assertEquals(398416, result.verifiedWitnessCount());
    assertEquals(223248, result.verifiedTheoremRootCount());
    assertEquals(13, result.maximumWhiteToMoveDistance());
    assertEquals(14, result.maximumDistance());
  }

  @SuppressWarnings("static-method")
  @Test
  void kqvKCertificateVerifiesTheoremRoots() {
    final var result = BasicMajorPieceHelpMateVerifier.verifyKqvK();

    assertEquals(QUEEN, result.whiteMajorPiece());
    assertEquals(368452, result.legalStateCount());
    assertEquals(364, result.terminalCheckmateCount());
    assertEquals(365160, result.winningStateCount());
    assertEquals(364796, result.witnessStateCount());
    assertEquals(364, result.verifiedTerminalCount());
    assertEquals(364796, result.verifiedWitnessCount());
    assertEquals(220288, result.verifiedTheoremRootCount());
    assertEquals(13, result.maximumWhiteToMoveDistance());
    assertEquals(14, result.maximumDistance());
  }
}
