package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.QUEEN;
import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.ROOK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestBasicMajorPieceHelpMateAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void krvKBlackToMoveOngoingNonForcedNonCapturePositionsAreWinnable() {
    final var result = BasicMajorPieceHelpMateAnalysis.analyze(ROOK);

    assertEquals(399112, result.legalStateCount());
    assertEquals(175168, result.whiteToMoveStateCount());
    assertEquals(223944, result.blackToMoveStateCount());
    assertEquals(48776, result.blackToMoveInCheckStateCount());
    assertEquals(175168, result.blackToMoveNotInCheckStateCount());
    assertEquals(216, result.blackCheckmateCount());
    assertEquals(223660, result.ongoingBlackToMoveStateCount());
    assertEquals(398632, result.winningStateCount());

    assertEquals(0, result.unwinnableWhiteToMoveStateCount());
    assertEquals(0, result.unwinnableWhiteToMoveRepresentatives().size());

    assertEquals(412, result.forcedMajorPieceCaptureStateCount());
    assertEquals(54, result.forcedMajorPieceCaptureRepresentatives().size());

    assertEquals(68, result.stalemateStateCount());
    assertEquals(9, result.stalemateRepresentatives().size());

    assertNoCounterexamplesAndCountsBalance(result);
  }

  @SuppressWarnings("static-method")
  @Test
  void kqvKBlackToMoveOngoingNonForcedNonCapturePositionsAreWinnable() {
    final var result = BasicMajorPieceHelpMateAnalysis.analyze(QUEEN);

    assertEquals(368452, result.legalStateCount());
    assertEquals(144508, result.whiteToMoveStateCount());
    assertEquals(223944, result.blackToMoveStateCount());
    assertEquals(79436, result.blackToMoveInCheckStateCount());
    assertEquals(144508, result.blackToMoveNotInCheckStateCount());
    assertEquals(364, result.blackCheckmateCount());
    assertEquals(222708, result.ongoingBlackToMoveStateCount());
    assertEquals(365160, result.winningStateCount());

    assertEquals(0, result.unwinnableWhiteToMoveStateCount());
    assertEquals(0, result.unwinnableWhiteToMoveRepresentatives().size());

    assertEquals(2420, result.forcedMajorPieceCaptureStateCount());
    assertEquals(305, result.forcedMajorPieceCaptureRepresentatives().size());

    assertEquals(872, result.stalemateStateCount());
    assertEquals(109, result.stalemateRepresentatives().size());

    assertNoCounterexamplesAndCountsBalance(result);
  }

  private static void assertNoCounterexamplesAndCountsBalance(
      BasicMajorPieceHelpMateAnalysis.AnalysisResult result) {
    assertEquals(0, result.counterexampleStateCount());
    assertTrue(result.counterexampleRepresentatives().isEmpty());
    assertEquals(result.legalStateCount(), result.whiteToMoveStateCount() + result.blackToMoveStateCount());
    assertEquals(result.blackToMoveStateCount(),
        result.blackToMoveInCheckStateCount() + result.blackToMoveNotInCheckStateCount());
    assertEquals(result.legalStateCount() - result.winningStateCount(),
        result.unwinnableWhiteToMoveStateCount() + result.forcedMajorPieceCaptureStateCount()
            + result.stalemateStateCount() + result.counterexampleStateCount());
  }
}
