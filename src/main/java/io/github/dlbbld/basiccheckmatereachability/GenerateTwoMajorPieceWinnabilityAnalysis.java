package io.github.dlbbld.basiccheckmatereachability;

public class GenerateTwoMajorPieceWinnabilityAnalysis {

  public static void main(String[] args) {
    print(TwoMajorPieceWinnabilityAnalysis.analyze(TwoMajorPieceWinnabilityAnalysis.MajorPiece.ROOK));
    print(TwoMajorPieceWinnabilityAnalysis.analyze(TwoMajorPieceWinnabilityAnalysis.MajorPiece.QUEEN));
  }

  private static void print(TwoMajorPieceWinnabilityAnalysis.AnalysisResult result) {
    System.out.printf("%s%n", result.majorPiece().materialName());
    System.out.printf("legal states in combined one/two-major space: %,d%n", result.legalStateCount());
    System.out.printf("one-major legal states: %,d%n", result.oneMajorLegalStateCount());
    System.out.printf("two-major legal states: %,d%n", result.twoMajorLegalStateCount());
    System.out.printf("two-major white-to-move states: %,d%n", result.twoMajorWhiteToMoveStateCount());
    System.out.printf("two-major black-to-move states: %,d%n", result.twoMajorBlackToMoveStateCount());
    System.out.printf("two-major checkmates: %,d%n", result.twoMajorCheckmateStateCount());
    System.out.printf("two-major stalemates: %,d%n", result.twoMajorStalemateStateCount());
    System.out.printf("two-major ongoing states: %,d%n", result.twoMajorOngoingStateCount());
    System.out.printf("two-major unwinnable ongoing states: %,d%n", result.twoMajorUnwinnableOngoingStateCount());
    System.out.printf("two-major forced first capture states: %,d%n",
        result.twoMajorForcedFirstCaptureStateCount());
    System.out.printf("two-major forced first capture states, total one move: %,d%n",
        result.twoMajorForcedFirstCaptureOneMoveStateCount());
    System.out.printf("two-major forced first capture states, total two moves: %,d%n",
        result.twoMajorForcedFirstCaptureTwoMoveStateCount());
    System.out.printf("two-major representative positions: %,d%n", result.twoMajorRepresentativeCount());
    System.out.printf("two-major checkmate representatives: %,d%n", result.twoMajorCheckmateRepresentativeCount());
    System.out.printf("two-major stalemate representatives: %,d%n", result.twoMajorStalemateRepresentativeCount());
    System.out.printf("two-major ongoing representatives: %,d%n", result.twoMajorOngoingRepresentativeCount());
    System.out.printf("two-major unwinnable ongoing representatives: %,d%n",
        result.twoMajorUnwinnableOngoingRepresentativeCount());
    System.out.printf("two-major forced first capture representatives, total one move: %,d%n",
        result.twoMajorForcedFirstCaptureOneMoveRepresentativeCount());
    System.out.printf("two-major forced first capture representatives, total two moves: %,d%n",
        result.twoMajorForcedFirstCaptureTwoMoveRepresentativeCount());
    System.out.printf("winning states in combined one/two-major space: %,d%n", result.winningStateCount());
    System.out.printf("maximum helpmate plies in combined space: %,d%n%n", result.maximumDistance());
  }
}
