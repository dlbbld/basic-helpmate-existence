package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece.ROOK;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.common.Nulls;
import io.github.dlbbld.ashlarchess.common.model.MoveSpecification;
import io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.MajorPieceState;
import io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpMateAnalysis.WhiteMajorPiece;

final class BasicMajorPieceHelpMateVerifier {

  private BasicMajorPieceHelpMateVerifier() {
  }

  static VerificationResult verifyKrvK() {
    return verify(ROOK);
  }

  static VerificationResult verifyKqvK() {
    return verify(WhiteMajorPiece.QUEEN);
  }

  private static VerificationResult verify(WhiteMajorPiece whiteMajorPiece) {
    final Set<MajorPieceState> states = enumerateLegalStates(whiteMajorPiece);
    final Map<MajorPieceState, Set<MoveSpecification>> legalMovesByState = calculateLegalMoves(whiteMajorPiece,
        states);
    final Set<MajorPieceState> checkmates = calculateCheckmates(whiteMajorPiece, states, legalMovesByState);
    final Certificate certificate = calculateCertificate(legalMovesByState, checkmates);
    final var verifiedTerminalCount = verifyTerminals(whiteMajorPiece, checkmates);
    final var verifiedWitnessCount = verifyWitnesses(whiteMajorPiece, certificate);
    final var theoremRootCount = verifyTheoremRoots(whiteMajorPiece, states, legalMovesByState, checkmates,
        certificate);

    return new VerificationResult(whiteMajorPiece, states.size(), checkmates.size(), certificate.winning().size(),
        certificate.witnessByState().size(), verifiedTerminalCount, verifiedWitnessCount, theoremRootCount,
        certificate.maximumDistance());
  }

  private static Set<MajorPieceState> enumerateLegalStates(WhiteMajorPiece whiteMajorPiece) {
    final Set<MajorPieceState> result = new HashSet<>();
    for (final Square whiteKing : Square.REAL) {
      for (final Square whiteMajor : Square.REAL) {
        for (final Square blackKing : Square.REAL) {
          if (whiteKing == whiteMajor || whiteKing == blackKing || whiteMajor == blackKing) {
            continue;
          }
          for (final Side havingMove : Side.REAL) {
            final MajorPieceState state = new MajorPieceState(whiteKing, whiteMajor, blackKing, havingMove);
            if (isLegalState(whiteMajorPiece, state)) {
              result.add(state);
            }
          }
        }
      }
    }
    return result;
  }

  private static boolean isLegalState(WhiteMajorPiece whiteMajorPiece, MajorPieceState state) {
    final BitboardPosition position = toBitboardPosition(whiteMajorPiece, state);
    return !position.isInCheck(state.havingMove().getOppositeSide());
  }

  private static Map<MajorPieceState, Set<MoveSpecification>> calculateLegalMoves(WhiteMajorPiece whiteMajorPiece,
      Set<MajorPieceState> states) {
    final Map<MajorPieceState, Set<MoveSpecification>> result = new HashMap<>();
    for (final MajorPieceState state : states) {
      result.put(state, toBitboardPosition(whiteMajorPiece, state).legalMoves(state.havingMove(), 0L));
    }
    return result;
  }

  private static Set<MajorPieceState> calculateCheckmates(WhiteMajorPiece whiteMajorPiece, Set<MajorPieceState> states,
      Map<MajorPieceState, Set<MoveSpecification>> legalMovesByState) {
    final Set<MajorPieceState> result = new HashSet<>();
    for (final MajorPieceState state : states) {
      if (state.havingMove() == Side.BLACK && toBitboardPosition(whiteMajorPiece, state).isInCheck(Side.BLACK)
          && Nulls.get(legalMovesByState, state).isEmpty()) {
        result.add(state);
      }
    }
    return result;
  }

  private static Certificate calculateCertificate(Map<MajorPieceState, Set<MoveSpecification>> legalMovesByState,
      Set<MajorPieceState> checkmates) {
    final Map<MajorPieceState, Collection<WitnessEdge>> predecessors = calculatePredecessors(legalMovesByState);
    final Set<MajorPieceState> winning = new HashSet<>(checkmates);
    final Map<MajorPieceState, MoveSpecification> witnessByState = new HashMap<>();
    final Map<MajorPieceState, Integer> distanceByState = new HashMap<>();
    final ArrayDeque<MajorPieceState> queue = new ArrayDeque<>(checkmates);

    for (final MajorPieceState checkmate : checkmates) {
      distanceByState.put(checkmate, 0);
    }

    var maximumDistance = 0;
    while (!queue.isEmpty()) {
      final MajorPieceState state = queue.removeFirst();
      final var predecessorDistance = Nulls.get(distanceByState, state) + 1;
      for (final WitnessEdge edge : predecessors.getOrDefault(state, Set.of())) {
        if (winning.add(edge.source())) {
          witnessByState.put(edge.source(), edge.move());
          distanceByState.put(edge.source(), predecessorDistance);
          maximumDistance = Math.max(maximumDistance, predecessorDistance);
          queue.addLast(edge.source());
        }
      }
    }
    return new Certificate(winning, witnessByState, distanceByState, maximumDistance);
  }

  private static Map<MajorPieceState, Collection<WitnessEdge>> calculatePredecessors(
      Map<MajorPieceState, Set<MoveSpecification>> legalMovesByState) {
    final Map<MajorPieceState, Collection<WitnessEdge>> result = new HashMap<>();
    for (final Map.Entry<MajorPieceState, Set<MoveSpecification>> entry : legalMovesByState.entrySet()) {
      final MajorPieceState state = entry.getKey();
      for (final MoveSpecification move : entry.getValue()) {
        if (capturesMajorPiece(state, move)) {
          continue;
        }
        final MajorPieceState successor = afterMove(state, move);
        result.computeIfAbsent(successor, unused -> new ArrayList<>()).add(new WitnessEdge(state, move));
      }
    }
    return result;
  }

  private static int verifyTerminals(WhiteMajorPiece whiteMajorPiece, Set<MajorPieceState> checkmates) {
    var result = 0;
    for (final MajorPieceState checkmate : checkmates) {
      final BitboardPosition position = toBitboardPosition(whiteMajorPiece, checkmate);
      if (checkmate.havingMove() != Side.BLACK || !position.isInCheck(Side.BLACK)
          || !position.legalMoves(Side.BLACK, 0L).isEmpty()) {
        throw new AssertionError("Terminal is not a verified black checkmate: " + checkmate);
      }
      result++;
    }
    return result;
  }

  private static int verifyWitnesses(WhiteMajorPiece whiteMajorPiece, Certificate certificate) {
    var result = 0;
    for (final Map.Entry<MajorPieceState, MoveSpecification> entry : certificate.witnessByState().entrySet()) {
      final MajorPieceState state = entry.getKey();
      final MoveSpecification witness = entry.getValue();
      final Set<MoveSpecification> legalMoves = toBitboardPosition(whiteMajorPiece, state).legalMoves(
          state.havingMove(), 0L);
      if (!legalMoves.contains(witness)) {
        throw new AssertionError("Witness is not legal: " + state + " " + witness);
      }
      if (capturesMajorPiece(state, witness)) {
        throw new AssertionError("Witness leaves the material class: " + state + " " + witness);
      }
      final MajorPieceState target = afterMove(state, witness);
      if (!certificate.winning().contains(target)) {
        throw new AssertionError("Witness target is not winning: " + state + " " + witness);
      }
      if (Nulls.get(certificate.distanceByState(), target) >= Nulls.get(certificate.distanceByState(), state)) {
        throw new AssertionError("Witness does not descend toward mate: " + state + " " + witness);
      }
      result++;
    }
    return result;
  }

  private static int verifyTheoremRoots(WhiteMajorPiece whiteMajorPiece, Set<MajorPieceState> states,
      Map<MajorPieceState, Set<MoveSpecification>> legalMovesByState, Set<MajorPieceState> checkmates,
      Certificate certificate) {
    var result = 0;
    for (final MajorPieceState state : states) {
      if (state.havingMove() != Side.BLACK || checkmates.contains(state)) {
        continue;
      }
      final Set<MoveSpecification> legalMoves = Nulls.get(legalMovesByState, state);
      if (legalMoves.isEmpty()) {
        if (toBitboardPosition(whiteMajorPiece, state).isInCheck(Side.BLACK)) {
          throw new AssertionError("Non-terminal black-to-move state has no legal moves while in check: " + state);
        }
        continue;
      }
      if (hasMaterialPreservingMove(state, legalMoves)) {
        if (!certificate.winning().contains(state)) {
          throw new AssertionError("Theorem root is not verified winning: " + state);
        }
        result++;
      }
    }
    return result;
  }

  private static boolean hasMaterialPreservingMove(MajorPieceState state, Set<MoveSpecification> legalMoves) {
    for (final MoveSpecification move : legalMoves) {
      if (!capturesMajorPiece(state, move)) {
        return true;
      }
    }
    return false;
  }

  private static boolean capturesMajorPiece(MajorPieceState state, MoveSpecification move) {
    return state.havingMove() == Side.BLACK && move.toSquare() == state.whiteMajor();
  }

  private static MajorPieceState afterMove(MajorPieceState state, MoveSpecification move) {
    if (capturesMajorPiece(state, move)) {
      throw new IllegalArgumentException("Major-piece-capture moves leave the state space");
    }
    return switch (state.havingMove()) {
      case WHITE -> {
        if (move.fromSquare() == state.whiteKing()) {
          yield new MajorPieceState(move.toSquare(), state.whiteMajor(), state.blackKing(), Side.BLACK);
        }
        yield new MajorPieceState(state.whiteKing(), move.toSquare(), state.blackKing(), Side.BLACK);
      }
      case BLACK -> new MajorPieceState(state.whiteKing(), state.whiteMajor(), move.toSquare(), Side.WHITE);
      case NONE -> throw new IllegalArgumentException();
      default -> throw new IllegalArgumentException();
    };
  }

  private static BitboardPosition toBitboardPosition(WhiteMajorPiece whiteMajorPiece, MajorPieceState state) {
    final long whiteRooks = whiteMajorPiece == ROOK ? bit(state.whiteMajor()) : 0L;
    final long whiteQueens = whiteMajorPiece == WhiteMajorPiece.QUEEN ? bit(state.whiteMajor()) : 0L;
    return new BitboardPosition(0L, whiteRooks, 0L, 0L, whiteQueens, bit(state.whiteKing()), 0L, 0L, 0L, 0L, 0L,
        bit(state.blackKing()));
  }

  private static long bit(Square square) {
    return 1L << square.ordinal();
  }

  record VerificationResult(WhiteMajorPiece whiteMajorPiece, int legalStateCount, int terminalCheckmateCount,
      int winningStateCount, int witnessStateCount, int verifiedTerminalCount, int verifiedWitnessCount,
      int verifiedTheoremRootCount, int maximumDistance) {
  }

  private record Certificate(Set<MajorPieceState> winning, Map<MajorPieceState, MoveSpecification> witnessByState,
      Map<MajorPieceState, Integer> distanceByState, int maximumDistance) {
  }

  private record WitnessEdge(MajorPieceState source, MoveSpecification move) {
  }
}
