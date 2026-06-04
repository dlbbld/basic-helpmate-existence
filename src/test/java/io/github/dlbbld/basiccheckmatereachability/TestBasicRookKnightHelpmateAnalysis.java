package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.common.model.MoveSpecification;
import io.github.dlbbld.basiccheckmatereachability.BasicRookKnightHelpmateAnalysis.RookKnightState;

class TestBasicRookKnightHelpmateAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void krvKNBlackToMoveOngoingNonForcedNonCapturePositionsAreWinnable() {
    final var result = BasicRookKnightHelpmateAnalysis.analyze();

    assertEquals(23315984, result.legalStateCount());
    assertEquals(10780728, result.whiteToMoveStateCount());
    assertEquals(12535256, result.blackToMoveStateCount());
    assertEquals(2644536, result.blackToMoveInCheckStateCount());
    assertEquals(9890720, result.blackToMoveNotInCheckStateCount());
    assertEquals(9328, result.blackCheckmateCount());
    assertEquals(8, result.endedWhiteToMoveStateCount());
    assertEquals(24, result.reducibleWhiteToMoveStateCount());
    assertEquals(24, result.reducibleWhiteToMoveStates().size());
    assertReducibleStatesReallyEnterWinningKrvK(result);
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
      BasicRookKnightHelpmateAnalysis.AnalysisResult result) {
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

  private static void assertReducibleStatesReallyEnterWinningKrvK(
      BasicRookKnightHelpmateAnalysis.AnalysisResult result) {
    for (final RookKnightState state : result.reducibleWhiteToMoveStates()) {
      assertTrue(hasLegalCaptureToWinningKrvK(state), state.toString());
    }
  }

  private static boolean hasLegalCaptureToWinningKrvK(RookKnightState state) {
    for (final MoveSpecification move : toBitboardPosition(state).legalMoves(Side.WHITE, 0L)) {
      if (move.toSquare() != state.blackKnight()) {
        continue;
      }
      final var whiteKing = move.fromSquare() == state.whiteKing() ? move.toSquare() : state.whiteKing();
      final var whiteRook = move.fromSquare() == state.whiteRook() ? move.toSquare() : state.whiteRook();
      if (isWinningKrvK(whiteKing, whiteRook, state.blackKing())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isWinningKrvK(Square whiteKing, Square whiteRook, Square blackKing) {
    final var position = new BitboardPosition(0L, bit(whiteRook), 0L, 0L, 0L, bit(whiteKing), 0L, 0L, 0L, 0L,
        0L, bit(blackKing));
    final var legalBlackMoves = position.legalMoves(Side.BLACK, 0L);
    if (position.isInCheck(Side.BLACK) && legalBlackMoves.isEmpty()) {
      return true;
    }
    for (final MoveSpecification move : legalBlackMoves) {
      if (move.toSquare() != whiteRook) {
        return true;
      }
    }
    return false;
  }

  private static BitboardPosition toBitboardPosition(RookKnightState state) {
    return new BitboardPosition(0L, bit(state.whiteRook()), 0L, 0L, 0L, bit(state.whiteKing()), 0L, 0L,
        bit(state.blackKnight()), 0L, 0L, bit(state.blackKing()));
  }

  private static long bit(Square square) {
    return 1L << square.ordinal();
  }
}

