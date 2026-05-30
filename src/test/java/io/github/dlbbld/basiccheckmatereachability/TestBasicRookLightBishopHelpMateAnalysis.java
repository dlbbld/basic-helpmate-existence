package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestBasicRookLightBishopHelpMateAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void krvKBLightBishopBlackToMoveOngoingNonForcedNonCapturePositionsAreWinnable() {
    final var result = BasicRookLightBishopHelpMateAnalysis.analyze();

    assertEquals(11306596, result.legalStateCount());
    assertEquals(5390364, result.whiteToMoveStateCount());
    assertEquals(5916232, result.blackToMoveStateCount());
    assertEquals(1249500, result.blackToMoveInCheckStateCount());
    assertEquals(4666732, result.blackToMoveNotInCheckStateCount());
    assertEquals(3264, result.blackCheckmateCount());
    assertEquals(8, result.unwinnableWhiteToMoveStateCount());
    assertEquals(2, result.unwinnableWhiteToMoveRepresentatives().size());
    assertEquals(5912920, result.ongoingBlackToMoveStateCount());
    assertEquals(11302800, result.winningStateCount());

    assertEquals(3740, result.forcedRookCaptureStateCount());
    assertEquals(935, result.forcedRookCaptureRepresentatives().size());

    assertEquals(48, result.stalemateStateCount());
    assertEquals(12, result.stalemateRepresentatives().size());

    assertNoCounterexamplesAndCountsBalance(result);
  }

  private static void assertNoCounterexamplesAndCountsBalance(
      BasicRookLightBishopHelpMateAnalysis.AnalysisResult result) {
    assertEquals(0, result.counterexampleStateCount());
    assertTrue(result.counterexampleRepresentatives().isEmpty());
    assertEquals(result.legalStateCount(), result.whiteToMoveStateCount() + result.blackToMoveStateCount());
    assertEquals(result.blackToMoveStateCount(),
        result.blackToMoveInCheckStateCount() + result.blackToMoveNotInCheckStateCount());
    assertEquals(result.legalStateCount() - result.winningStateCount(),
        result.unwinnableWhiteToMoveStateCount() + result.forcedRookCaptureStateCount()
            + result.stalemateStateCount() + result.counterexampleStateCount());
  }
}
