package io.github.dlbbld.basiccheckmatereachability;

public class GenerateLastMoveFilterSummary {

  public static void main(String[] args) {
    final var oppositeBishops = BasicOppositeBishopsSeedLegalityAnalysis.analyzeFromOriginalSquares();
    print("KBBvK(opposite bishops)", "no same-material White last move",
        oppositeBishops.lastMoveFilterInputStateCount(), oppositeBishops.lastMoveFilterRejectedStateCount(),
        oppositeBishops.afterLastMoveFilterStateCount(), oppositeBishops.lastMoveFilterInputRepresentativeCount(),
        oppositeBishops.lastMoveFilterRejectedRepresentativeCount(),
        oppositeBishops.afterLastMoveFilterRepresentativeCount());

    final var lightBishopKnight = BasicLightBishopKnightSeedLegalityAnalysis.analyzeFromOriginalSquares();
    print("KBNvK(light bishop)", "no same-material White last move",
        lightBishopKnight.lastMoveFilterInputStateCount(), lightBishopKnight.lastMoveFilterRejectedStateCount(),
        lightBishopKnight.afterLastMoveFilterStateCount(), lightBishopKnight.lastMoveFilterInputRepresentativeCount(),
        lightBishopKnight.lastMoveFilterRejectedRepresentativeCount(),
        lightBishopKnight.afterLastMoveFilterRepresentativeCount());
  }

  private static void print(String materialClass, String filter, int inputStates, int filteredStates,
      int remainingStates, int inputRepresentatives, int filteredRepresentatives, int remainingRepresentatives) {
    System.out.println("| `" + materialClass + "` | " + filter + " | " + inputStates + " | " + filteredStates
        + " | " + remainingStates + " | " + inputRepresentatives + " | " + filteredRepresentatives + " | "
        + remainingRepresentatives + " |");
  }
}
