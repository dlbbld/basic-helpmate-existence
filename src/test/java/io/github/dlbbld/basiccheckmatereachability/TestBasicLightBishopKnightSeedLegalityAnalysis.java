package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.basiccheckmatereachability.BasicLightBishopKnightSeedLegalityAnalysis.LightBishopKnightState;

class TestBasicLightBishopKnightSeedLegalityAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void kbnvKOriginalSquareSeedReachesAllBlackToMoveNonCheckStates() {
    final var result = BasicLightBishopKnightSeedLegalityAnalysis.analyzeFromOriginalSquares();

    assertEquals(12268044, result.legalStateCount());
    assertEquals(12169754, result.reachableStateCount());
    assertEquals(98290, result.unreachableStateCount());

    assertEquals(97030, result.unreachableBlackToMoveStateCount());
    assertEquals(97030, result.unreachableBlackToMoveInCheckStateCount());
    assertEquals(0, result.unreachableBlackToMoveNotInCheckStateCount());

    assertEquals(1260, result.unreachableWhiteToMoveStateCount());
    assertEquals(0, result.unreachableWhiteToMoveInCheckStateCount());
    assertEquals(1260, result.unreachableWhiteToMoveNotInCheckStateCount());

    assertEquals(24600, result.unreachableRepresentatives().size());
    assertTrue(result.unreachableRepresentatives()
        .contains(new LightBishopKnightState(Square.C2, Square.B1, Square.C4, Square.A2, Side.BLACK)));
  }
}
