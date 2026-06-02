package io.github.dlbbld.basiccheckmatereachability;

import io.github.dlbbld.basiccheckmatereachability.BasicFourPieceHelpmateVerifier.VerificationResult;

public class GenerateBasicFourPieceHelpmateVerification {

  public static void main(String[] args) {
    printKbbvK();
    printKrvKb();
    printKrvKn();
  }

  private static void printKbbvK() {
    final var analysisStart = System.nanoTime();
    final var analysis = BasicOppositeBishopsHelpmateAnalysis.analyze();
    final var analysisElapsedNanos = System.nanoTime() - analysisStart;

    final var verificationStart = System.nanoTime();
    final var verification = BasicFourPieceHelpmateVerifier.verifyKbbvK();
    final var verificationElapsedNanos = System.nanoTime() - verificationStart;

    print("KBBvK", analysis.legalStateCount(), analysis.blackToMoveStateCount(), analysis.blackCheckmateCount(),
        analysis.ongoingBlackToMoveStateCount(), analysis.winningStateCount(), analysis.forcedPieceCaptureStateCount(),
        analysis.stalemateStateCount(), analysis.counterexampleStateCount(), verification, analysisElapsedNanos,
        verificationElapsedNanos);
  }

  private static void printKrvKb() {
    final var analysisStart = System.nanoTime();
    final var analysis = BasicRookLightBishopHelpmateAnalysis.analyze();
    final var analysisElapsedNanos = System.nanoTime() - analysisStart;

    final var verificationStart = System.nanoTime();
    final var verification = BasicFourPieceHelpmateVerifier.verifyKrvKbLightBishop();
    final var verificationElapsedNanos = System.nanoTime() - verificationStart;

    print("KRvKB(light bishop)", analysis.legalStateCount(), analysis.blackToMoveStateCount(),
        analysis.blackCheckmateCount(), analysis.ongoingBlackToMoveStateCount(), analysis.winningStateCount(),
        analysis.forcedRookCaptureStateCount(), analysis.stalemateStateCount(), analysis.counterexampleStateCount(),
        verification, analysisElapsedNanos, verificationElapsedNanos);
  }

  private static void printKrvKn() {
    final var analysisStart = System.nanoTime();
    final var analysis = BasicRookKnightHelpmateAnalysis.analyze();
    final var analysisElapsedNanos = System.nanoTime() - analysisStart;

    final var verificationStart = System.nanoTime();
    final var verification = BasicFourPieceHelpmateVerifier.verifyKrvKn();
    final var verificationElapsedNanos = System.nanoTime() - verificationStart;

    print("KRvKN", analysis.legalStateCount(), analysis.blackToMoveStateCount(), analysis.blackCheckmateCount(),
        analysis.ongoingBlackToMoveStateCount(), analysis.winningStateCount(), analysis.forcedRookCaptureStateCount(),
        analysis.stalemateStateCount(), analysis.counterexampleStateCount(), verification, analysisElapsedNanos,
        verificationElapsedNanos);
  }

  private static void print(String name, int legalStateCount, int blackToMoveStateCount, int blackCheckmateCount,
      int ongoingBlackToMoveStateCount, int winningStateCount, int forcedCaptureStateCount, int stalemateStateCount,
      int counterexampleStateCount, VerificationResult verification, long analysisElapsedNanos,
      long verificationElapsedNanos) {
    System.out.println(name + " theorem analysis");
    System.out.println("legal states: " + legalStateCount);
    System.out.println("black-to-move states: " + blackToMoveStateCount);
    System.out.println("black checkmates: " + blackCheckmateCount);
    System.out.println("ongoing black-to-move states: " + ongoingBlackToMoveStateCount);
    System.out.println("cooperatively winnable states: " + winningStateCount);
    System.out.println("forced capture states: " + forcedCaptureStateCount);
    System.out.println("stalemate states: " + stalemateStateCount);
    System.out.println("counterexample states: " + counterexampleStateCount);
    System.out.println(name + " certificate verification");
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
