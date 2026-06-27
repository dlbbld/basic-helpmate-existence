package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestBasicTwoKnightsHelpmateAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void knnvKBlackToMoveOngoingNonForcedNonCapturePositionsAreWinnable() {
    final var result = BasicTwoKnightsHelpmateAnalysis.analyze();

    assertEquals(12579944, result.legalStateCount());
    assertEquals(5749652, result.whiteToMoveStateCount());
    assertEquals(6830292, result.blackToMoveStateCount());
    assertEquals(1080640, result.blackToMoveInCheckStateCount());
    assertEquals(5749652, result.blackToMoveNotInCheckStateCount());
    assertEquals(120, result.blackCheckmateCount());
    assertEquals(0, result.unwinnableWhiteToMoveStateCount());
    assertTrue(result.unwinnableWhiteToMoveRepresentatives().isEmpty());
    assertEquals(6826308, result.ongoingBlackToMoveStateCount());
    assertEquals(12574248, result.winningStateCount());

    assertEquals(1708, result.forcedPieceCaptureOneMoveStateCount());
    assertEquals(216, result.forcedPieceCaptureOneMoveRepresentatives().size());
    assertEquals(124, result.forcedPieceCaptureTwoMoveStateCount());
    assertEquals(16, result.forcedPieceCaptureTwoMoveRepresentatives().size());

    assertEquals(3864, result.stalemateStateCount());
    assertEquals(484, result.stalemateRepresentatives().size());

    assertNoCounterexamplesAndCountsBalance(result);
  }

  private static void assertNoCounterexamplesAndCountsBalance(
      BasicTwoKnightsHelpmateAnalysis.AnalysisResult result) {
    assertEquals(0, result.counterexampleStateCount());
    assertTrue(result.counterexampleRepresentatives().isEmpty());
    assertEquals(result.legalStateCount(), result.whiteToMoveStateCount() + result.blackToMoveStateCount());
    assertEquals(result.blackToMoveStateCount(),
        result.blackToMoveInCheckStateCount() + result.blackToMoveNotInCheckStateCount());
    assertEquals(result.legalStateCount() - result.winningStateCount(),
        result.unwinnableWhiteToMoveStateCount() + result.forcedPieceCaptureOneMoveStateCount()
            + result.forcedPieceCaptureTwoMoveStateCount() + result.stalemateStateCount()
            + result.counterexampleStateCount());
  }
}
