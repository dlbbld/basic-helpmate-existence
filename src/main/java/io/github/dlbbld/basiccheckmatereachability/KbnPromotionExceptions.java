package io.github.dlbbld.basiccheckmatereachability;

import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.basiccheckmatereachability.BasicLightBishopKnightHelpmateAnalysis.LightBishopKnightState;

/** Exact promotion traps for White's KBN versus a bare black king, with either bishop colour. */
final class KbnPromotionExceptions {

  private KbnPromotionExceptions() {
  }

  static boolean contains(LightBishopKnightState state) {
    // File reflection preserves White's promotion rank. Rotations do not.
    final var mirror = state.whiteBishop() == Square.B8;
    final var king = mirror ? reflect(state.whiteKing()) : state.whiteKing();
    final var bishop = mirror ? reflect(state.whiteBishop()) : state.whiteBishop();
    final var knight = mirror ? reflect(state.whiteKnight()) : state.whiteKnight();
    final var defender = mirror ? reflect(state.blackKing()) : state.blackKing();
    if (king != Square.F7 || bishop != Square.G8) {
      return false;
    }
    return state.havingMove() == Side.WHITE
        ? defender == Square.H8
            && (knight == Square.E8 || knight == Square.E6 || knight == Square.F5 || knight == Square.H5)
        : defender == Square.H7 && knight == Square.F5;
  }

  static StrictIllegalityCertificates.Position positionOf(LightBishopKnightState state) {
    return StrictIllegalityCertificates.position(state.havingMove(),
        StrictIllegalityCertificates.piece('K', state.whiteKing()),
        StrictIllegalityCertificates.piece('B', state.whiteBishop()),
        StrictIllegalityCertificates.piece('N', state.whiteKnight()),
        StrictIllegalityCertificates.piece('k', state.blackKing()));
  }

  private static Square reflect(Square square) {
    return Square.REAL.get(square.ordinal() ^ 7);
  }
}
