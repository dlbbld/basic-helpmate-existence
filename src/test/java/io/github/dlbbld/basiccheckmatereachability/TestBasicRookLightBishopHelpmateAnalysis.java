package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.common.model.MoveSpecification;
import io.github.dlbbld.basiccheckmatereachability.BasicRookLightBishopHelpmateAnalysis.RookLightBishopState;

class TestBasicRookLightBishopHelpmateAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void krvKBLightBishopBlackToMoveOngoingNonForcedNonCapturePositionsAreWinnable() {
    final var result = BasicRookLightBishopHelpmateAnalysis.analyze();

    assertEquals(11306596, result.legalStateCount());
    assertEquals(5390364, result.whiteToMoveStateCount());
    assertEquals(5916232, result.blackToMoveStateCount());
    assertEquals(1249500, result.blackToMoveInCheckStateCount());
    assertEquals(4666732, result.blackToMoveNotInCheckStateCount());
    assertEquals(3264, result.blackCheckmateCount());
    assertEquals(0, result.endedWhiteToMoveStateCount());
    assertEquals(8, result.reducibleWhiteToMoveStateCount());
    assertEquals(8, result.reducibleWhiteToMoveStates().size());
    assertReducibleStatesReallyEnterWinningKrvK(result);
    assertEquals(0, result.unwinnableWhiteToMoveStateCount());
    assertEquals(0, result.unwinnableWhiteToMoveRepresentatives().size());
    assertEquals(5912920, result.ongoingBlackToMoveStateCount());
    assertEquals(11302800, result.winningStateCount());

    assertEquals(3740, result.forcedRookCaptureStateCount());
    assertEquals(935, result.forcedRookCaptureRepresentatives().size());

    assertEquals(48, result.stalemateStateCount());
    assertEquals(12, result.stalemateRepresentatives().size());

    assertNoCounterexamplesAndCountsBalance(result);
  }

  private static void assertNoCounterexamplesAndCountsBalance(
      BasicRookLightBishopHelpmateAnalysis.AnalysisResult result) {
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
      BasicRookLightBishopHelpmateAnalysis.AnalysisResult result) {
    for (final RookLightBishopState state : result.reducibleWhiteToMoveStates()) {
      assertTrue(hasLegalCaptureToWinningKrvK(state), state.toString());
    }
  }

  private static boolean hasLegalCaptureToWinningKrvK(RookLightBishopState state) {
    for (final MoveSpecification move : toBitboardPosition(state).legalMoves(Side.WHITE, 0L)) {
      if (move.toSquare() != state.blackBishop()) {
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

  private static BitboardPosition toBitboardPosition(RookLightBishopState state) {
    return new BitboardPosition(0L, bit(state.whiteRook()), 0L, 0L, 0L, bit(state.whiteKing()), 0L, 0L, 0L,
        bit(state.blackBishop()), 0L, bit(state.blackKing()));
  }

  private static long bit(Square square) {
    return 1L << square.ordinal();
  }
}
