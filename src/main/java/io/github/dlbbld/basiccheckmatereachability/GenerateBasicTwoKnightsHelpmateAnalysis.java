package io.github.dlbbld.basiccheckmatereachability;

public class GenerateBasicTwoKnightsHelpmateAnalysis {

  public static void main(String[] args) {
    System.out.println(BasicTwoKnightsHelpmateAnalysis.format(BasicTwoKnightsHelpmateAnalysis.analyze()));
    final var verification = BasicFourPieceHelpmateVerifier.verifyKnnvK();
    System.out.println("KNNvK certificate verification");
    System.out.println("verified terminals: " + verification.verifiedTerminalCount());
    System.out.println("verified witnesses: " + verification.verifiedWitnessCount());
    System.out.println("verified theorem roots: " + verification.verifiedTheoremRootCount());
    System.out.println("maximum white-to-move witness distance: " + verification.maximumWhiteToMoveDistance());
    System.out.println("maximum witness distance: " + verification.maximumDistance());
  }
}
