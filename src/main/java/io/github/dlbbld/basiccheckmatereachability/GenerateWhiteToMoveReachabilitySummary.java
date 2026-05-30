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
    printNonOngoingOrReducible(rookLightBishop.endedWhiteToMoveStateCount(),
        rookLightBishop.reducibleWhiteToMoveStateCount());

    final var rookKnight = BasicRookKnightHelpMateAnalysis.analyze();
    print("KRvKN", rookKnight.whiteToMoveStateCount(), rookKnight.unwinnableWhiteToMoveStateCount(),
        rookKnight.unwinnableWhiteToMoveRepresentatives().size());
    printNonOngoingOrReducible(rookKnight.endedWhiteToMoveStateCount(), rookKnight.reducibleWhiteToMoveStateCount());

    System.out.println();
    System.out.println("Black-to-move local exception:");
    printPositionRow("KBNvK(light bishop)", "8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1", "retro-illegal");

    System.out.println();
    System.out.println("| Material class | Representative position | Strict-game status |");
    System.out.println("|---|---|---|");

    for (final var state : oppositeBishops.unwinnableWhiteToMoveRepresentatives()) {
      printPositionRow("KBBvK, opposite bishops", toFen(state), "retro-illegal");
    }
    for (final var state : lightBishopKnight.unwinnableWhiteToMoveRepresentatives()) {
      printPositionRow("KBNvK, light bishop", toFen(state), "retro-illegal");
    }
    for (final var state : rookLightBishop.unwinnableWhiteToMoveRepresentatives()) {
      printPositionRow("KRvKB(light bishop)", toFen(state), "unexpected local exception");
    }
    for (final var state : rookKnight.unwinnableWhiteToMoveRepresentatives()) {
      printPositionRow("KRvKN", toFen(state), "unexpected local exception");
    }
  }

  private static void print(String materialClass, int whiteToMoveStates, int unwinnableWhiteToMoveStates,
      int canonicalUnwinnableWhiteToMoveStates) {
    System.out.printf("%s: white-to-move states %,d, unwinnable %,d, canonical %,d%n", materialClass,
        whiteToMoveStates, unwinnableWhiteToMoveStates, canonicalUnwinnableWhiteToMoveStates);
  }

  private static void printNonOngoingOrReducible(int endedWhiteToMoveStates, int reducibleWhiteToMoveStates) {
    System.out.printf("  ended white-to-move %,d, reducible by White capture %,d%n", endedWhiteToMoveStates,
        reducibleWhiteToMoveStates);
  }

  private static void printPositionRow(String materialClass, String fen, String strictGameStatus) {
    final String analysisUrl = lichessAnalysisUrl(fen);
    final String imageUrl = chessvisionImageUrl(fen);
    System.out.println("| `" + materialClass + "` | <a href=\"" + analysisUrl + "\"><img src=\""
        + imageUrl.replace("&", "&amp;") + "\" alt=\"" + fen
        + "\" width=\"180\"></a><br>`" + fen + "`<br>[Lichess analysis](" + analysisUrl + ") | "
        + strictGameStatus + " |");
  }

  private static String lichessAnalysisUrl(String fen) {
    return "https://lichess.org/analysis/standard/" + fen.strip().replace(' ', '_');
  }

  private static String chessvisionImageUrl(String fen) {
    final String sideToMove = fen.split(" ")[1].equals("w") ? "white" : "black";
    return "https://fen2image.chessvision.ai/" + fen.strip().replace(" ", "%20") + "?turn=" + sideToMove
        + "&pov=" + sideToMove;
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
