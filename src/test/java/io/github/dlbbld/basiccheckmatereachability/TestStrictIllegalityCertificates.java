package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.noPossibleLastAdjacentCheckingPieceMove;
import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.noPossibleLastBlackKingMove;
import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.piece;
import static io.github.dlbbld.basiccheckmatereachability.StrictIllegalityCertificates.position;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.dlbbld.ashlarchess.board.enums.Side;

class TestStrictIllegalityCertificates {

  @SuppressWarnings("static-method")
  @Test
  void kbnBlackToMoveCounterexampleHasNoPossibleLastWhiteCheckingMove() {
    final var result = BasicLightBishopKnightHelpmateAnalysis.analyze();

    assertEquals(1, result.counterexampleRepresentatives().size());
    for (final var state : result.counterexampleRepresentatives()) {
      assertTrue(noPossibleLastAdjacentCheckingPieceMove(position(state.havingMove(), piece('K', state.whiteKing()),
          piece('B', state.whiteBishop()), piece('N', state.whiteKnight()), piece('k', state.blackKing()))));
    }
  }

  @SuppressWarnings("static-method")
  @Test
  void kbbWhiteToMoveCounterexamplesHaveNoPossibleLastBlackKingMove() {
    final var result = BasicOppositeBishopsHelpmateAnalysis.analyze();

    assertEquals(3, result.unwinnableWhiteToMoveRepresentatives().size());
    for (final var state : result.unwinnableWhiteToMoveRepresentatives()) {
      assertTrue(noPossibleLastBlackKingMove(position(Side.WHITE, piece('K', state.whiteKing()),
          piece('B', state.whiteLightBishop()), piece('B', state.whiteDarkBishop()), piece('k', state.blackKing()))));
    }
  }

  @SuppressWarnings("static-method")
  @Test
  void kbnWhiteToMoveCounterexamplesHaveNoPossibleLastBlackKingMove() {
    final var result = BasicLightBishopKnightHelpmateAnalysis.analyze();

    assertEquals(15, result.unwinnableWhiteToMoveRepresentatives().size());
    for (final var state : result.unwinnableWhiteToMoveRepresentatives()) {
      assertTrue(noPossibleLastBlackKingMove(position(Side.WHITE, piece('K', state.whiteKing()),
          piece('B', state.whiteBishop()), piece('N', state.whiteKnight()), piece('k', state.blackKing()))));
    }
  }
}
