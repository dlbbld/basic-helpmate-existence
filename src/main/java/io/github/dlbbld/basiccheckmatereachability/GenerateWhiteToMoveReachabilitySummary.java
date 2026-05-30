package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.QUEEN;
import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.ROOK;

import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;

public class GenerateWhiteToMoveReachabilitySummary {

  public static void main(String[] args) {
    final var rook = BasicMajorPieceHelpMateAnalysis.analyze(ROOK);
    print("KRvK", rook.whiteToMoveStateCount(), rook.unwinnableWhiteToMoveStateCount(),
        rook.unwinnableWhiteToMoveRepresentatives().size());

    final var queen = BasicMajorPieceHelpMateAnalysis.analyze(QUEEN);
    print("KQvK", queen.whiteToMoveStateCount(), queen.unwinnableWhiteToMoveStateCount(),
        queen.unwinnableWhiteToMoveRepresentatives().size());

    final var lightBishopKnight = BasicLightBishopKnightHelpMateAnalysis.analyze();
    print("KBNvK(light bishop)", lightBishopKnight.whiteToMoveStateCount(),
        lightBishopKnight.unwinnableWhiteToMoveStateCount(),
        lightBishopKnight.unwinnableWhiteToMoveRepresentatives().size());

    final var oppositeBishops = BasicOppositeBishopsHelpMateAnalysis.analyze();
    print("KBBvK(opposite bishops)", oppositeBishops.whiteToMoveStateCount(),
        oppositeBishops.unwinnableWhiteToMoveStateCount(),
        oppositeBishops.unwinnableWhiteToMoveRepresentatives().size());

    final var rookLightBishop = BasicRookLightBishopHelpMateAnalysis.analyze();
    print("KRvKB(light bishop)", rookLightBishop.whiteToMoveStateCount(),
        rookLightBishop.unwinnableWhiteToMoveStateCount(),
        rookLightBishop.unwinnableWhiteToMoveRepresentatives().size());

    final var rookKnight = BasicRookKnightHelpMateAnalysis.analyze();
    print("KRvKN", rookKnight.whiteToMoveStateCount(), rookKnight.unwinnableWhiteToMoveStateCount(),
        rookKnight.unwinnableWhiteToMoveRepresentatives().size());

    System.out.println();
    System.out.println("| Material class | Representative FEN | Strict-game status |");
    System.out.println("|---|---|---|");

    for (final var state : oppositeBishops.unwinnableWhiteToMoveRepresentatives()) {
      System.out.println("| `KBBvK`, opposite bishops | `" + toFen(state) + "` | not yet classified |");
    }
    for (final var state : lightBishopKnight.unwinnableWhiteToMoveRepresentatives()) {
      System.out.println("| `KBNvK`, light bishop | `" + toFen(state) + "` | not yet classified |");
    }
    for (final var state : rookLightBishop.unwinnableWhiteToMoveRepresentatives()) {
      System.out.println("| `KRvKB(light bishop)` | `" + toFen(state) + "` | not yet classified |");
    }
    for (final var state : rookKnight.unwinnableWhiteToMoveRepresentatives()) {
      System.out.println("| `KRvKN` | `" + toFen(state) + "` | not yet classified |");
    }
  }

  private static void print(String materialClass, int whiteToMoveStates, int unwinnableWhiteToMoveStates,
      int canonicalUnwinnableWhiteToMoveStates) {
    System.out.printf("%s: white-to-move states %,d, unwinnable %,d, canonical %,d%n", materialClass,
        whiteToMoveStates, unwinnableWhiteToMoveStates, canonicalUnwinnableWhiteToMoveStates);
  }

  private static String toFen(BasicOppositeBishopsHelpMateAnalysis.OppositeBishopsState state) {
    return toFen(Side.WHITE, new PieceOnSquare('K', state.whiteKing()),
        new PieceOnSquare('B', state.whiteLightBishop()), new PieceOnSquare('B', state.whiteDarkBishop()),
        new PieceOnSquare('k', state.blackKing()));
  }

  private static String toFen(BasicLightBishopKnightHelpMateAnalysis.LightBishopKnightState state) {
    return toFen(Side.WHITE, new PieceOnSquare('K', state.whiteKing()), new PieceOnSquare('B', state.whiteBishop()),
        new PieceOnSquare('N', state.whiteKnight()), new PieceOnSquare('k', state.blackKing()));
  }

  private static String toFen(BasicRookLightBishopHelpMateAnalysis.RookLightBishopState state) {
    return toFen(Side.WHITE, new PieceOnSquare('K', state.whiteKing()), new PieceOnSquare('R', state.whiteRook()),
        new PieceOnSquare('k', state.blackKing()), new PieceOnSquare('b', state.blackBishop()));
  }

  private static String toFen(BasicRookKnightHelpMateAnalysis.RookKnightState state) {
    return toFen(Side.WHITE, new PieceOnSquare('K', state.whiteKing()), new PieceOnSquare('R', state.whiteRook()),
        new PieceOnSquare('k', state.blackKing()), new PieceOnSquare('n', state.blackKnight()));
  }

  private static String toFen(Side havingMove, PieceOnSquare... pieces) {
    final char[] board = new char[64];
    for (int i = 0; i < board.length; i++) {
      board[i] = '1';
    }
    for (final PieceOnSquare piece : pieces) {
      board[piece.square().ordinal()] = piece.fenChar();
    }

    final StringBuilder sb = new StringBuilder();
    for (int rank = 7; rank >= 0; rank--) {
      if (rank < 7) {
        sb.append('/');
      }
      var emptyCount = 0;
      for (int file = 0; file < 8; file++) {
        final char piece = board[rank * 8 + file];
        if (piece == '1') {
          emptyCount++;
        } else {
          if (emptyCount > 0) {
            sb.append(emptyCount);
            emptyCount = 0;
          }
          sb.append(piece);
        }
      }
      if (emptyCount > 0) {
        sb.append(emptyCount);
      }
    }
    sb.append(' ').append(havingMove == Side.WHITE ? 'w' : 'b').append(" - - 0 1");
    return sb.toString();
  }

  private record PieceOnSquare(char fenChar, Square square) {
  }
}
