package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.TwoMajorPieceWinnabilityAnalysis.MajorPiece.QUEEN;
import static io.github.dlbbld.basiccheckmatereachability.TwoMajorPieceWinnabilityAnalysis.MajorPiece.ROOK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestTwoMajorPieceWinnabilityAnalysis {

  @SuppressWarnings("static-method")
  @Test
  void krrvKOngoingPositionsAreWinnableEvenAfterForcedFirstCapture() {
    final var result = TwoMajorPieceWinnabilityAnalysis.analyze(ROOK);

    assertEquals(11391996, result.legalStateCount());
    assertEquals(399112, result.oneMajorLegalStateCount());
    assertEquals(10992884, result.twoMajorLegalStateCount());
    assertEquals(4162592, result.twoMajorWhiteToMoveStateCount());
    assertEquals(6830292, result.twoMajorBlackToMoveStateCount());
    assertEquals(72392, result.twoMajorCheckmateStateCount());
    assertEquals(19580, result.twoMajorStalemateStateCount());
    assertEquals(10900912, result.twoMajorOngoingStateCount());
    assertEquals(0, result.twoMajorUnwinnableOngoingStateCount());
    assertEquals(90408, result.twoMajorForcedFirstCaptureStateCount());
    assertEquals(80456, result.twoMajorForcedFirstCaptureOneMoveStateCount());
    assertEquals(9952, result.twoMajorForcedFirstCaptureTwoMoveStateCount());
    assertEquals(1374940, result.twoMajorRepresentativeCount());
    assertEquals(9052, result.twoMajorCheckmateRepresentativeCount());
    assertEquals(2468, result.twoMajorStalemateRepresentativeCount());
    assertEquals(1363420, result.twoMajorOngoingRepresentativeCount());
    assertEquals(0, result.twoMajorUnwinnableOngoingRepresentativeCount());
    assertEquals(10072, result.twoMajorForcedFirstCaptureOneMoveRepresentativeCount());
    assertEquals(1262, result.twoMajorForcedFirstCaptureTwoMoveRepresentativeCount());
    assertEquals(11371936, result.winningStateCount());
    assertEquals(14, result.maximumDistance());
  }

  @SuppressWarnings("static-method")
  @Test
  void kqqvKOngoingPositionsAreWinnableEvenAfterForcedFirstCapture() {
    final var result = TwoMajorPieceWinnabilityAnalysis.analyze(QUEEN);

    assertEquals(10027304, result.legalStateCount());
    assertEquals(368452, result.oneMajorLegalStateCount());
    assertEquals(9658852, result.twoMajorLegalStateCount());
    assertEquals(2828560, result.twoMajorWhiteToMoveStateCount());
    assertEquals(6830292, result.twoMajorBlackToMoveStateCount());
    assertEquals(251880, result.twoMajorCheckmateStateCount());
    assertEquals(141176, result.twoMajorStalemateStateCount());
    assertEquals(9265796, result.twoMajorOngoingStateCount());
    assertEquals(0, result.twoMajorUnwinnableOngoingStateCount());
    assertEquals(258664, result.twoMajorForcedFirstCaptureStateCount());
    assertEquals(242216, result.twoMajorForcedFirstCaptureOneMoveStateCount());
    assertEquals(16448, result.twoMajorForcedFirstCaptureTwoMoveStateCount());
    assertEquals(1208031, result.twoMajorRepresentativeCount());
    assertEquals(31523, result.twoMajorCheckmateRepresentativeCount());
    assertEquals(17672, result.twoMajorStalemateRepresentativeCount());
    assertEquals(1158836, result.twoMajorOngoingRepresentativeCount());
    assertEquals(0, result.twoMajorUnwinnableOngoingRepresentativeCount());
    assertEquals(30282, result.twoMajorForcedFirstCaptureOneMoveRepresentativeCount());
    assertEquals(2056, result.twoMajorForcedFirstCaptureTwoMoveRepresentativeCount());
    assertEquals(9882836, result.winningStateCount());
    assertEquals(14, result.maximumDistance());
  }
}
