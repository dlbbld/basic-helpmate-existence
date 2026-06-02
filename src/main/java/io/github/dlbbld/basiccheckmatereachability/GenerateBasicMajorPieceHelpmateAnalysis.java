package io.github.dlbbld.basiccheckmatereachability;

import io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpmateAnalysis.WhiteMajorPiece;

public class GenerateBasicMajorPieceHelpmateAnalysis {

  public static void main(String[] args) {
    System.out.println(BasicMajorPieceHelpmateAnalysis.format(BasicMajorPieceHelpmateAnalysis
        .analyze(WhiteMajorPiece.ROOK)));
    System.out.println(BasicMajorPieceHelpmateAnalysis.format(BasicMajorPieceHelpmateAnalysis
        .analyze(WhiteMajorPiece.QUEEN)));
  }
}
