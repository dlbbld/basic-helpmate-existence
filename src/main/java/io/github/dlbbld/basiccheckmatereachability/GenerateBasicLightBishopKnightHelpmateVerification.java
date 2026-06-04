package io.github.dlbbld.basiccheckmatereachability;

public class GenerateBasicLightBishopKnightHelpmateVerification {

  public static void main(String[] args) {
    final var analysisStart = System.nanoTime();
    final var analysis = BasicLightBishopKnightHelpmateAnalysis.analyze();
    final var analysisElapsedNanos = System.nanoTime() - analysisStart;

    final var verificationStart = System.nanoTime();
    final var verification = BasicLightBishopKnightHelpmateVerifier.verify();
    final var verificationElapsedNanos = System.nanoTime() - verificationStart;

    System.out.println("KBNvK(light bishop) theorem analysis");
    System.out.println("legal states: " + analysis.legalStateCount());
    System.out.println("black-to-move states: " + analysis.blackToMoveStateCount());
    System.out.println("black checkmates: " + analysis.blackCheckmateCount());
    System.out.println("ongoing black-to-move states: " + analysis.ongoingBlackToMoveStateCount());
    System.out.println("cooperatively winnable states: " + analysis.winningStateCount());
    System.out.println("forced piece-capture states: " + analysis.forcedPieceCaptureStateCount());
    System.out.println("stalemate states: " + analysis.stalemateStateCount());
    System.out.println("counterexample states: " + analysis.counterexampleStateCount());
    System.out.println("KBNvK(light bishop) certificate verification");
    System.out.println("verified terminals: " + verification.verifiedTerminalCount());
    System.out.println("verified witnesses: " + verification.verifiedWitnessCount());
    System.out.println("verified theorem roots: " + verification.verifiedTheoremRootCount());
    System.out.println("maximum witness distance: " + verification.maximumDistance());
    System.out.println("analysis time: " + formatMillis(analysisElapsedNanos));
    System.out.println("verification time: " + formatMillis(verificationElapsedNanos));
    System.out.println("combined time: " + formatMillis(analysisElapsedNanos + verificationElapsedNanos));
  }

  private static String formatMillis(long elapsedNanos) {
    return "%d ms".formatted(elapsedNanos / 1_000_000);
  }
}
