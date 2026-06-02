package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpmateAnalysis.WhiteMajorPiece.ROOK;

import io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpmateAnalysis.WhiteMajorPiece;

public class GenerateBasicMajorPieceHelpmateVerification {

  public static void main(String[] args) {
    print(ROOK);
    print(WhiteMajorPiece.QUEEN);
  }

  private static void print(WhiteMajorPiece whiteMajorPiece) {
    final var analysisStart = System.nanoTime();
    final var analysis = BasicMajorPieceHelpmateAnalysis.analyze(whiteMajorPiece);
    final var analysisElapsedNanos = System.nanoTime() - analysisStart;

    final var verificationStart = System.nanoTime();
    final var verification = switch (whiteMajorPiece) {
      case ROOK -> BasicMajorPieceHelpmateVerifier.verifyKrvK();
      case QUEEN -> BasicMajorPieceHelpmateVerifier.verifyKqvK();
    };
    final var verificationElapsedNanos = System.nanoTime() - verificationStart;

    System.out.println(whiteMajorPiece.materialName() + " theorem analysis");
    System.out.println("legal states: " + analysis.legalStateCount());
    System.out.println("black-to-move states: " + analysis.blackToMoveStateCount());
    System.out.println("black checkmates: " + analysis.blackCheckmateCount());
    System.out.println("ongoing black-to-move states: " + analysis.ongoingBlackToMoveStateCount());
    System.out.println("cooperatively winnable states: " + analysis.winningStateCount());
    System.out.println("forced " + whiteMajorPiece.pieceName() + "-capture states: "
        + analysis.forcedMajorPieceCaptureStateCount());
    System.out.println("stalemate states: " + analysis.stalemateStateCount());
    System.out.println("counterexample states: " + analysis.counterexampleStateCount());
    System.out.println(whiteMajorPiece.materialName() + " certificate verification");
    System.out.println("verified terminals: " + verification.verifiedTerminalCount());
    System.out.println("verified witnesses: " + verification.verifiedWitnessCount());
    System.out.println("verified theorem roots: " + verification.verifiedTheoremRootCount());
    System.out.println("maximum witness distance: " + verification.maximumDistance());
    System.out.println("analysis time: " + formatMillis(analysisElapsedNanos));
    System.out.println("verification time: " + formatMillis(verificationElapsedNanos));
    System.out.println("combined time: " + formatMillis(analysisElapsedNanos + verificationElapsedNanos));
    System.out.println();
  }

  private static String formatMillis(long elapsedNanos) {
    return "%d ms".formatted(elapsedNanos / 1_000_000);
  }
}
