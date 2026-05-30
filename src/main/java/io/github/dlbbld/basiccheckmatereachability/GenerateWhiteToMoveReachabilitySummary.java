package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.QUEEN;
import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.ROOK;

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
  }

  private static void print(String materialClass, int whiteToMoveStates, int unwinnableWhiteToMoveStates,
      int canonicalUnwinnableWhiteToMoveStates) {
    System.out.printf("%s: white-to-move states %,d, unwinnable %,d, canonical %,d%n", materialClass,
        whiteToMoveStates, unwinnableWhiteToMoveStates, canonicalUnwinnableWhiteToMoveStates);
  }
}
