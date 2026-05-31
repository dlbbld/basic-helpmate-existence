package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBasicOppositeBishopsSeedLegalityAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void kbbvKOriginalSquareSeedReachesAllBlackToMoveNonCheckStates() {
    final var result = BasicOppositeBishopsSeedLegalityAnalysis.analyzeFromOriginalSquares();

    assertEquals(5973472, result.legalStateCount());
    assertEquals(5929808, result.reachableStateCount());
    assertEquals(43664, result.unreachableStateCount());

    assertEquals(43096, result.unreachableBlackToMoveStateCount());
    assertEquals(43096, result.unreachableBlackToMoveInCheckStateCount());
    assertEquals(0, result.unreachableBlackToMoveNotInCheckStateCount());

    assertEquals(568, result.unreachableWhiteToMoveStateCount());
    assertEquals(0, result.unreachableWhiteToMoveInCheckStateCount());
    assertEquals(568, result.unreachableWhiteToMoveNotInCheckStateCount());

    assertEquals(5458, result.unreachableRepresentatives().size());
    assertEquals(43096, result.lastMoveFilterInputStateCount());
    assertEquals(43016, result.lastMoveFilterRejectedStateCount());
    assertEquals(80, result.afterLastMoveFilterStateCount());
    assertEquals(5387, result.lastMoveFilterInputRepresentativeCount());
    assertEquals(5377, result.lastMoveFilterRejectedRepresentativeCount());
    assertEquals(10, result.afterLastMoveFilterRepresentativeCount());
  }
}
