package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.basiccheckmatereachability.BasicLightBishopKnightHelpmateAnalysis.LightBishopKnightState;

class TestBasicLightBishopKnightHelpmateAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void kbnLightBishopBlackToMoveOngoingNonForcedNonCapturePositionsHaveOneCounterexampleShape() {
    final var result = BasicLightBishopKnightHelpmateAnalysis.analyze();

    assertEquals(12268044, result.legalStateCount());
    assertEquals(5437752, result.whiteToMoveStateCount());
    assertEquals(6830292, result.blackToMoveStateCount());
    assertEquals(1392540, result.blackToMoveInCheckStateCount());
    assertEquals(5437752, result.blackToMoveNotInCheckStateCount());
    assertEquals(232, result.blackCheckmateCount());
    assertEquals(60, result.unwinnableWhiteToMoveStateCount());
    assertEquals(15, result.unwinnableWhiteToMoveRepresentatives().size());
    assertEquals(6823616, result.ongoingBlackToMoveStateCount());
    assertEquals(12257062, result.winningStateCount());

    assertEquals(4474, result.forcedPieceCaptureStateCount());
    assertEquals(1121, result.forcedPieceCaptureRepresentatives().size());

    assertEquals(6444, result.stalemateStateCount());
    assertEquals(1611, result.stalemateRepresentatives().size());

    assertEquals(4, result.counterexampleStateCount());
    assertEquals(1, result.counterexampleRepresentatives().size());
    assertTrue(result.counterexampleRepresentatives()
        .contains(new LightBishopKnightState(Square.C2, Square.B1, Square.C4, Square.A2, Side.BLACK)));

    assertCountsBalance(result);
  }

  private static void assertCountsBalance(BasicLightBishopKnightHelpmateAnalysis.AnalysisResult result) {
    assertEquals(result.legalStateCount(), result.whiteToMoveStateCount() + result.blackToMoveStateCount());
    assertEquals(result.blackToMoveStateCount(),
        result.blackToMoveInCheckStateCount() + result.blackToMoveNotInCheckStateCount());
    assertEquals(result.legalStateCount() - result.winningStateCount(),
        result.unwinnableWhiteToMoveStateCount() + result.forcedPieceCaptureStateCount()
            + result.stalemateStateCount() + result.counterexampleStateCount());
  }
}
