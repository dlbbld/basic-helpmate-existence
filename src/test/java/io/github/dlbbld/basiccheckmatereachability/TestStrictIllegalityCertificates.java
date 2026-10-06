package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.noPossibleLastAdjacentCheckingPieceMove;
import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.noPossibleLastBlackKingMove;
import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.noPossibleLastTwoMoves;
import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.piece;
import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.position;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.basiccheckmatereachability.BasicLightBishopKnightHelpmateAnalysis.LightBishopKnightState;

class TestStrictIllegalityCertificates {

  @SuppressWarnings("static-method")
  @Test
  void kbnCounterexamplesAreAuditedInEveryOrientationIncludingBothBishopColours() {
    final var result = BasicLightBishopKnightHelpmateAnalysis.analyze();

    assertEquals(1, result.counterexampleRepresentatives().size());
    var promotionBlackToMove = 0;
    var illegalBlackToMove = 0;
    for (final var representative : result.counterexampleRepresentatives()) {
      for (final var state : BasicLightBishopKnightHelpmateAnalysis.symmetryOrbit(representative)) {
        final var illegal = noPossibleLastAdjacentCheckingPieceMove(KbnPromotionExceptions.positionOf(state));
        assertEquals(!KbnPromotionExceptions.contains(state), illegal,
            () -> BasicLightBishopKnightHelpmateAnalysis.toFen(state));
        if (illegal) {
          illegalBlackToMove++;
        } else {
          promotionBlackToMove++;
        }
      }
    }
    assertEquals(6, illegalBlackToMove);
    assertEquals(2, promotionBlackToMove);

    var promotionWhiteToMove = 0;
    var illegalWhiteToMove = 0;
    assertEquals(15, result.unwinnableWhiteToMoveRepresentatives().size());
    for (final var representative : result.unwinnableWhiteToMoveRepresentatives()) {
      for (final var state : BasicLightBishopKnightHelpmateAnalysis.symmetryOrbit(representative)) {
        final var illegal = noPossibleLastTwoMoves(KbnPromotionExceptions.positionOf(state));
        assertEquals(!KbnPromotionExceptions.contains(state), illegal,
            () -> BasicLightBishopKnightHelpmateAnalysis.toFen(state));
        if (illegal) {
          illegalWhiteToMove++;
        } else {
          promotionWhiteToMove++;
        }
      }
    }
    assertEquals(112, illegalWhiteToMove);
    assertEquals(8, promotionWhiteToMove);
  }

  @SuppressWarnings("static-method")
  @Test
  void kbbWhiteToMoveCounterexamplesHaveNoPossibleLastBlackKingMove() {
    final var result = BasicOppositeBishopsHelpmateAnalysis.analyze();

    assertEquals(3, result.unwinnableWhiteToMoveRepresentatives().size());
    for (final var state : result.unwinnableWhiteToMoveRepresentatives()) {
      final var orbit = BasicLightBishopKnightHelpmateAnalysis.symmetryOrbit(new LightBishopKnightState(
          state.whiteKing(), state.whiteLightBishop(), state.whiteDarkBishop(), state.blackKing(), Side.WHITE));
      for (final var oriented : orbit) {
        assertTrue(noPossibleLastBlackKingMove(position(Side.WHITE, piece('K', oriented.whiteKing()),
            piece('B', oriented.whiteBishop()), piece('B', oriented.whiteKnight()), piece('k', oriented.blackKing()))));
      }
    }
  }

  @SuppressWarnings("static-method")
  @Test
  void blackKingMayHaveMovedOutOfCheck() {
    final var target = position(Side.WHITE, piece('K', Square.F7), piece('B', Square.G8),
        piece('N', Square.E8), piece('k', Square.H8));
    assertFalse(noPossibleLastBlackKingMove(target));
    assertFalse(noPossibleLastTwoMoves(target));
  }

  @Test
  void adjacentRookCheckMayHaveArisenByCapturePromotion() {
    final var target = position(Side.BLACK, piece('K', Square.C6), piece('R', Square.A8),
        piece('N', Square.B8), piece('k', Square.A7));
    assertFalse(noPossibleLastAdjacentCheckingPieceMove(target));
    final var board = io.github.dlbbld.ashlarchess.board.Board.fromFenStrict("nN6/kP6/2K5/8/8/8/8/8 w - - 0 1");
    board.moveLenient("bxa8=R+");
    assertEquals(Side.BLACK, board.getSideToMove());
    assertTrue(board.isCheck());
  }

  @Test
  void promotionMustNotLeaveBlackAlreadyCheckedInThePredecessor() {
    final var target = position(Side.BLACK, piece('K', Square.F7), piece('B', Square.G8),
        piece('N', Square.F6), piece('k', Square.H7));
    assertTrue(noPossibleLastAdjacentCheckingPieceMove(target));
  }

  @Test
  void blackKingCertificateDoesNotApplyWhenBlackHasOtherPieces() {
    assertFalse(noPossibleLastBlackKingMove(position(Side.WHITE, piece('K', Square.C1), piece('B', Square.A2),
        piece('k', Square.A1), piece('n', Square.H8))));
  }
}
