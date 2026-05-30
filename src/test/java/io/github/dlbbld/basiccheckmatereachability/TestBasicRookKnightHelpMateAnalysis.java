package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestBasicRookKnightHelpMateAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void krvKNBlackToMoveOngoingNonForcedNonCapturePositionsAreWinnable() {
    final var result = BasicRookKnightHelpMateAnalysis.analyze();

    assertEquals(23315984, result.legalStateCount());
    assertEquals(10780728, result.whiteToMoveStateCount());
    assertEquals(12535256, result.blackToMoveStateCount());
    assertEquals(2644536, result.blackToMoveInCheckStateCount());
    assertEquals(9890720, result.blackToMoveNotInCheckStateCount());
    assertEquals(9328, result.blackCheckmateCount());
    assertEquals(8, result.endedWhiteToMoveStateCount());
    assertEquals(24, result.reducibleWhiteToMoveStateCount());
    assertEquals(0, result.unwinnableWhiteToMoveStateCount());
    assertEquals(0, result.unwinnableWhiteToMoveRepresentatives().size());
    assertEquals(12525880, result.ongoingBlackToMoveStateCount());
    assertEquals(23308736, result.winningStateCount());

    assertEquals(7168, result.forcedRookCaptureStateCount());
    assertEquals(896, result.forcedRookCaptureRepresentatives().size());

    assertEquals(48, result.stalemateStateCount());
    assertEquals(6, result.stalemateRepresentatives().size());

    assertNoCounterexamplesAndCountsBalance(result);
  }

  private static void assertNoCounterexamplesAndCountsBalance(
      BasicRookKnightHelpMateAnalysis.AnalysisResult result) {
    assertEquals(0, result.counterexampleStateCount());
    assertTrue(result.counterexampleRepresentatives().isEmpty());
    assertEquals(result.legalStateCount(), result.whiteToMoveStateCount() + result.blackToMoveStateCount());
    assertEquals(result.blackToMoveStateCount(),
        result.blackToMoveInCheckStateCount() + result.blackToMoveNotInCheckStateCount());
    assertEquals(result.legalStateCount() - result.winningStateCount(),
        result.endedWhiteToMoveStateCount() + result.reducibleWhiteToMoveStateCount()
            + result.unwinnableWhiteToMoveStateCount() + result.forcedRookCaptureStateCount()
            + result.stalemateStateCount() + result.counterexampleStateCount());
  }
}

