package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBasicRookMinorPieceSeedLegalityAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void krvkbOriginalSquareSeedReachabilityCounts() {
    final var result = BasicRookLightBishopSeedLegalityAnalysis.analyzeFromOriginalSquares();

    assertEquals(11306596, result.legalStateCount());
    assertEquals(11264310, result.reachableStateCount());
    assertEquals(42286, result.unreachableStateCount());

    assertEquals(7032, result.unreachableBlackToMoveStateCount());
    assertEquals(7032, result.unreachableBlackToMoveInCheckStateCount());
    assertEquals(0, result.unreachableBlackToMoveNotInCheckStateCount());

    assertEquals(35254, result.unreachableWhiteToMoveStateCount());
    assertEquals(35154, result.unreachableWhiteToMoveInCheckStateCount());
    assertEquals(100, result.unreachableWhiteToMoveNotInCheckStateCount());

    assertEquals(10601, result.unreachableRepresentatives().size());
  }

  @SuppressWarnings("static-method")
  @Test
  void krvknOriginalSquareSeedReachabilityCounts() {
    final var result = BasicRookKnightSeedLegalityAnalysis.analyzeFromOriginalSquares();

    assertEquals(23315984, result.legalStateCount());
    assertEquals(23301272, result.reachableStateCount());
    assertEquals(14712, result.unreachableStateCount());

    assertEquals(14336, result.unreachableBlackToMoveStateCount());
    assertEquals(14328, result.unreachableBlackToMoveInCheckStateCount());
    assertEquals(8, result.unreachableBlackToMoveNotInCheckStateCount());

    assertEquals(376, result.unreachableWhiteToMoveStateCount());
    assertEquals(376, result.unreachableWhiteToMoveInCheckStateCount());
    assertEquals(0, result.unreachableWhiteToMoveNotInCheckStateCount());

    assertEquals(1839, result.unreachableRepresentatives().size());
  }
}
