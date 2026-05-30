package io.github.dlbbld.basiccheckmatereachability;

import io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece;

public class GenerateBasicMajorPieceHelpMateAnalysis {

  public static void main(String[] args) {
    System.out.println(BasicMajorPieceHelpMateAnalysis.format(BasicMajorPieceHelpMateAnalysis
        .analyze(WhiteMajorPiece.ROOK)));
    System.out.println(BasicMajorPieceHelpMateAnalysis.format(BasicMajorPieceHelpMateAnalysis
        .analyze(WhiteMajorPiece.QUEEN)));
  }
}
