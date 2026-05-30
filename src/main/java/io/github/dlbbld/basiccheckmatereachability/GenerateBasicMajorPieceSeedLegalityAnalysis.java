package io.github.dlbbld.basiccheckmatereachability;

final class GenerateBasicMajorPieceSeedLegalityAnalysis {

  private GenerateBasicMajorPieceSeedLegalityAnalysis() {
  }

  public static void main(String[] args) {
    print("KRvK", "white king e1, white rook a1, black king e8, black to move",
        BasicMajorPieceSeedLegalityAnalysis.WhiteMajorPiece.ROOK,
        BasicMajorPieceSeedLegalityAnalysis.analyzeRookFromOriginalSquares());
    System.out.println();
    print("KQvK", "white king e1, white queen d1, black king e8, black to move",
        BasicMajorPieceSeedLegalityAnalysis.WhiteMajorPiece.QUEEN,
        BasicMajorPieceSeedLegalityAnalysis.analyzeQueenFromOriginalSquares());
  }

  private static void print(String materialName, String seedDescription,
      BasicMajorPieceSeedLegalityAnalysis.WhiteMajorPiece whiteMajorPiece,
      BasicMajorPieceSeedLegalityAnalysis.AnalysisResult result) {
    System.out.println(materialName + " seed strict-legality reachability");
    System.out.println("seed: " + seedDescription);
    System.out.println("legal states: " + result.legalStateCount());
    System.out.println("reachable states: " + result.reachableStateCount());
    System.out.println("unreachable states: " + result.unreachableStateCount());
    System.out.println("unreachable black-to-move states: " + result.unreachableBlackToMoveStateCount());
    System.out.println("unreachable black-to-move in-check states: "
        + result.unreachableBlackToMoveInCheckStateCount());
    System.out.println("unreachable black-to-move not-in-check states: "
        + result.unreachableBlackToMoveNotInCheckStateCount());
    System.out.println("unreachable white-to-move states: " + result.unreachableWhiteToMoveStateCount());
    System.out.println("unreachable white-to-move in-check states: "
        + result.unreachableWhiteToMoveInCheckStateCount());
    System.out.println("unreachable white-to-move not-in-check states: "
        + result.unreachableWhiteToMoveNotInCheckStateCount());
    System.out.println("unreachable canonical representatives: " + result.unreachableRepresentatives().size());
    for (final var state : result.unreachableRepresentatives()) {
      System.out.println("  " + BasicMajorPieceSeedLegalityAnalysis.toFen(whiteMajorPiece, state));
    }
  }
}
