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
}
