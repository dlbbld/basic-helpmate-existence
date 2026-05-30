package io.github.dlbbld.basiccheckmatereachability;

final class GenerateBasicLightBishopKnightSeedLegalityAnalysis {

  private GenerateBasicLightBishopKnightSeedLegalityAnalysis() {
  }

  public static void main(String[] args) {
    final var result = BasicLightBishopKnightSeedLegalityAnalysis.analyzeFromOriginalSquares();
    System.out.println("KBNvK(light bishop) seed strict-legality reachability");
    System.out.println("seed: white king e1, white bishop f1, white knight b1, black king e8, black to move");
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
    var printedCount = 0;
    for (final var state : result.unreachableRepresentatives()) {
      System.out.println("  " + BasicLightBishopKnightSeedLegalityAnalysis.toFen(state));
      printedCount++;
      if (printedCount == 50) {
        System.out.println("  ... " + (result.unreachableRepresentatives().size() - printedCount)
            + " further representatives omitted");
        break;
      }
    }
  }
}
