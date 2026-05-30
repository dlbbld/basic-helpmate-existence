package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBasicMajorPieceSeedLegalityAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void krvKOriginalSquareSeedReachesAllNonCheckStates() {
    final var result = BasicMajorPieceSeedLegalityAnalysis.analyzeRookFromOriginalSquares();

    assertEquals(399112, result.legalStateCount());
    assertEquals(399064, result.reachableStateCount());
    assertEquals(48, result.unreachableStateCount());

    assertEquals(48, result.unreachableBlackToMoveStateCount());
    assertEquals(48, result.unreachableBlackToMoveInCheckStateCount());
    assertEquals(0, result.unreachableBlackToMoveNotInCheckStateCount());

    assertEquals(0, result.unreachableWhiteToMoveStateCount());
    assertEquals(0, result.unreachableWhiteToMoveInCheckStateCount());
    assertEquals(0, result.unreachableWhiteToMoveNotInCheckStateCount());

    assertEquals(6, result.unreachableRepresentatives().size());
  }

  @SuppressWarnings("static-method")
  @Test
  void kqvKOriginalSquareSeedReachesAllNonCheckStates() {
    final var result = BasicMajorPieceSeedLegalityAnalysis.analyzeQueenFromOriginalSquares();

    assertEquals(368452, result.legalStateCount());
    assertEquals(368452, result.reachableStateCount());
    assertEquals(0, result.unreachableStateCount());

    assertEquals(0, result.unreachableBlackToMoveStateCount());
    assertEquals(0, result.unreachableBlackToMoveInCheckStateCount());
    assertEquals(0, result.unreachableBlackToMoveNotInCheckStateCount());

    assertEquals(0, result.unreachableWhiteToMoveStateCount());
    assertEquals(0, result.unreachableWhiteToMoveInCheckStateCount());
    assertEquals(0, result.unreachableWhiteToMoveNotInCheckStateCount());

    assertEquals(0, result.unreachableRepresentatives().size());
  }
}
