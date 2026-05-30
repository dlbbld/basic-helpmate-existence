package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestBasicOppositeBishopsHelpMateAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void kbbvKOppositeBishopsBlackToMoveOngoingNonForcedNonCapturePositionsAreWinnable() {
    final var result = BasicOppositeBishopsHelpMateAnalysis.analyze();

    assertEquals(5973472, result.legalStateCount());
    assertEquals(2504128, result.whiteToMoveStateCount());
    assertEquals(3469344, result.blackToMoveStateCount());
    assertEquals(965216, result.blackToMoveInCheckStateCount());
    assertEquals(2504128, result.blackToMoveNotInCheckStateCount());
    assertEquals(1552, result.blackCheckmateCount());
    assertEquals(24, result.unwinnableWhiteToMoveStateCount());
    assertEquals(3, result.unwinnableWhiteToMoveRepresentatives().size());
    assertEquals(3462472, result.ongoingBlackToMoveStateCount());
    assertEquals(5960176, result.winningStateCount());

    assertEquals(7952, result.forcedPieceCaptureStateCount());
    assertEquals(994, result.forcedPieceCaptureRepresentatives().size());

    assertEquals(5320, result.stalemateStateCount());
    assertEquals(665, result.stalemateRepresentatives().size());

    assertNoCounterexamplesAndCountsBalance(result);
  }

  private static void assertNoCounterexamplesAndCountsBalance(
      BasicOppositeBishopsHelpMateAnalysis.AnalysisResult result) {
    assertEquals(0, result.counterexampleStateCount());
    assertTrue(result.counterexampleRepresentatives().isEmpty());
    assertEquals(result.legalStateCount(), result.whiteToMoveStateCount() + result.blackToMoveStateCount());
    assertEquals(result.blackToMoveStateCount(),
        result.blackToMoveInCheckStateCount() + result.blackToMoveNotInCheckStateCount());
    assertEquals(result.legalStateCount() - result.winningStateCount(),
        result.unwinnableWhiteToMoveStateCount() + result.forcedPieceCaptureStateCount()
            + result.stalemateStateCount() + result.counterexampleStateCount());
  }
}
