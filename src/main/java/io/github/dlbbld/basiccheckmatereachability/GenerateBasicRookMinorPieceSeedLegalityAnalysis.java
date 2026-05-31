package io.github.dlbbld.basiccheckmatereachability;

final class GenerateBasicRookMinorPieceSeedLegalityAnalysis {

  private GenerateBasicRookMinorPieceSeedLegalityAnalysis() {
  }

  public static void main(String[] args) {
    print("KRvKB(light bishop)", BasicRookLightBishopSeedLegalityAnalysis.analyzeFromOriginalSquares());
    print("KRvKN", BasicRookKnightSeedLegalityAnalysis.analyzeFromOriginalSquares());
  }

  private static void print(String materialClass, BasicRookLightBishopSeedLegalityAnalysis.AnalysisResult result) {
    System.out.println(materialClass + " seed strict-legality reachability");
    printCounts(result.legalStateCount(), result.reachableStateCount(), result.unreachableStateCount(),
        result.unreachableBlackToMoveStateCount(), result.unreachableBlackToMoveInCheckStateCount(),
        result.unreachableBlackToMoveNotInCheckStateCount(), result.unreachableWhiteToMoveStateCount(),
        result.unreachableWhiteToMoveInCheckStateCount(), result.unreachableWhiteToMoveNotInCheckStateCount(),
        result.unreachableRepresentatives().size());
  }

  private static void print(String materialClass, BasicRookKnightSeedLegalityAnalysis.AnalysisResult result) {
    System.out.println(materialClass + " seed strict-legality reachability");
    printCounts(result.legalStateCount(), result.reachableStateCount(), result.unreachableStateCount(),
        result.unreachableBlackToMoveStateCount(), result.unreachableBlackToMoveInCheckStateCount(),
        result.unreachableBlackToMoveNotInCheckStateCount(), result.unreachableWhiteToMoveStateCount(),
        result.unreachableWhiteToMoveInCheckStateCount(), result.unreachableWhiteToMoveNotInCheckStateCount(),
        result.unreachableRepresentatives().size());
    System.out.println("unreachable black-to-move not-in-check representatives:");
    for (final var state : result.unreachableBlackToMoveNotInCheckRepresentatives()) {
      System.out.println("  " + toFen(state));
    }
  }

  private static void printCounts(int legalStateCount, int reachableStateCount, int unreachableStateCount,
      int unreachableBlackToMoveStateCount, int unreachableBlackToMoveInCheckStateCount,
      int unreachableBlackToMoveNotInCheckStateCount, int unreachableWhiteToMoveStateCount,
      int unreachableWhiteToMoveInCheckStateCount, int unreachableWhiteToMoveNotInCheckStateCount,
      int canonicalUnreachableStateCount) {
    System.out.println("legal states: " + legalStateCount);
    System.out.println("reachable states: " + reachableStateCount);
    System.out.println("unreachable states: " + unreachableStateCount);
    System.out.println("unreachable black-to-move states: " + unreachableBlackToMoveStateCount);
    System.out.println("unreachable black-to-move in-check states: " + unreachableBlackToMoveInCheckStateCount);
    System.out.println("unreachable black-to-move not-in-check states: " + unreachableBlackToMoveNotInCheckStateCount);
    System.out.println("unreachable white-to-move states: " + unreachableWhiteToMoveStateCount);
    System.out.println("unreachable white-to-move in-check states: " + unreachableWhiteToMoveInCheckStateCount);
    System.out.println("unreachable white-to-move not-in-check states: " + unreachableWhiteToMoveNotInCheckStateCount);
    System.out.println("canonical unreachable states: " + canonicalUnreachableStateCount);
  }

  private static String toFen(BasicRookKnightSeedLegalityAnalysis.RookKnightState state) {
    final char[] board = new char[64];
    for (int i = 0; i < board.length; i++) {
      board[i] = '1';
    }
    board[state.whiteKing().ordinal()] = 'K';
    board[state.whiteRook().ordinal()] = 'R';
    board[state.blackKing().ordinal()] = 'k';
    board[state.blackKnight().ordinal()] = 'n';

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
    sb.append(' ').append(state.havingMove() == io.github.dlbbld.ashlarchess.board.enums.Side.WHITE ? 'w' : 'b')
        .append(" - - 0 1");
    return sb.toString();
  }
}
