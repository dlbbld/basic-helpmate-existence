package io.github.dlbbld.basiccheckmatereachability;

import java.util.BitSet;
import java.util.Set;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.common.Nulls;
import io.github.dlbbld.ashlarchess.common.model.MoveSpecification;

final class BasicLightBishopKnightHelpMateVerifier {

  private static final int WHITE_TO_MOVE = 0;
  private static final int BLACK_TO_MOVE = 1;
  private static final int NO_WITNESS = -1;
  private static final int SQUARE_COUNT = 64;
  private static final int STATE_COUNT = SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * 2;
  private static final int HAS_LEGAL_BLACK_MOVE = 1;
  private static final int HAS_NON_CAPTURE_BLACK_MOVE = 2;

  private static final int[][] KING_DELTAS = {
      {-1, -1},
      {-1, 0},
      {-1, 1},
      {0, -1},
      {0, 1},
      {1, -1},
      {1, 0},
      {1, 1}
  };

  private static final int[][] KNIGHT_DELTAS = {
      {-2, -1},
      {-2, 1},
      {-1, -2},
      {-1, 2},
      {1, -2},
      {1, 2},
      {2, -1},
      {2, 1}
  };

  private static final int[][] BISHOP_DELTAS = {
      {-1, -1},
      {-1, 1},
      {1, -1},
      {1, 1}
  };

  private BasicLightBishopKnightHelpMateVerifier() {
  }

  static VerificationResult verify() {
    final BitSet legalStates = new BitSet(STATE_COUNT);
    final BitSet checkmates = new BitSet(STATE_COUNT);
    final BitSet winning = new BitSet(STATE_COUNT);
    final int[] witnessByState = new int[STATE_COUNT];
    final byte[] distanceByState = new byte[STATE_COUNT];
    final IntQueue queue = new IntQueue();

    for (var i = 0; i < witnessByState.length; i++) {
      witnessByState[i] = NO_WITNESS;
    }

    var legalStateCount = 0;
    var terminalCheckmateCount = 0;
    for (var whiteKing = 0; whiteKing < SQUARE_COUNT; whiteKing++) {
      for (var whiteBishop = 0; whiteBishop < SQUARE_COUNT; whiteBishop++) {
        if (!isLightSquare(whiteBishop) || whiteBishop == whiteKing) {
          continue;
        }
        for (var whiteKnight = 0; whiteKnight < SQUARE_COUNT; whiteKnight++) {
          if (whiteKnight == whiteKing || whiteKnight == whiteBishop) {
            continue;
          }
          for (var blackKing = 0; blackKing < SQUARE_COUNT; blackKing++) {
            if (blackKing == whiteKing || blackKing == whiteBishop || blackKing == whiteKnight) {
              continue;
            }
            for (var havingMove = WHITE_TO_MOVE; havingMove <= BLACK_TO_MOVE; havingMove++) {
              final var state = encode(whiteKing, whiteBishop, whiteKnight, blackKing, havingMove);
              if (!isLegalState(whiteKing, whiteBishop, whiteKnight, blackKing, havingMove)) {
                continue;
              }
              legalStates.set(state);
              legalStateCount++;
              if (havingMove == BLACK_TO_MOVE && isBlackInCheck(whiteKing, whiteBishop, whiteKnight, blackKing)
                  && blackMoveMask(whiteKing, whiteBishop, whiteKnight, blackKing) == 0) {
                checkmates.set(state);
                winning.set(state);
                queue.add(state);
                terminalCheckmateCount++;
              }
            }
          }
        }
      }
    }

    final var maximumDistance = calculateCertificate(legalStates, winning, witnessByState, distanceByState, queue);
    final var verifiedTerminalCount = verifyTerminals(checkmates);
    final var verifiedWitnessCount = verifyWitnesses(winning, witnessByState, distanceByState);
    final var verifiedTheoremRootCount = verifyTheoremRoots(legalStates, checkmates, winning);

    return new VerificationResult(legalStateCount, terminalCheckmateCount, winning.cardinality(),
        verifiedWitnessCount, verifiedTerminalCount, verifiedWitnessCount, verifiedTheoremRootCount, maximumDistance);
  }

  private static int calculateCertificate(BitSet legalStates, BitSet winning, int[] witnessByState,
      byte[] distanceByState, IntQueue queue) {
    var maximumDistance = 0;
    while (!queue.isEmpty()) {
      final var state = queue.remove();
      final var predecessorDistance = Byte.toUnsignedInt(distanceByState[state]) + 1;
      if (havingMove(state) == BLACK_TO_MOVE) {
        maximumDistance = Math.max(maximumDistance,
            addWhitePredecessors(legalStates, winning, witnessByState, distanceByState, queue, state,
                predecessorDistance));
      } else {
        maximumDistance = Math.max(maximumDistance,
            addBlackPredecessors(legalStates, winning, witnessByState, distanceByState, queue, state,
                predecessorDistance));
      }
    }
    return maximumDistance;
  }

  private static int addWhitePredecessors(BitSet legalStates, BitSet winning, int[] witnessByState,
      byte[] distanceByState, IntQueue queue, int state, int predecessorDistance) {
    final var whiteKing = whiteKing(state);
    final var whiteBishop = whiteBishop(state);
    final var whiteKnight = whiteKnight(state);
    final var blackKing = blackKing(state);
    var result = 0;

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(whiteKing, delta);
      if (origin != -1 && origin != whiteBishop && origin != whiteKnight && origin != blackKing) {
        result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
            origin, whiteBishop, whiteKnight, blackKing, WHITE_TO_MOVE, predecessorDistance));
      }
    }

    for (final int[] delta : BISHOP_DELTAS) {
      var origin = offset(whiteBishop, delta);
      while (origin != -1) {
        if (origin == whiteKing || origin == whiteKnight || origin == blackKing) {
          break;
        }
        result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
            whiteKing, origin, whiteKnight, blackKing, WHITE_TO_MOVE, predecessorDistance));
        origin = offset(origin, delta);
      }
    }

    for (final int[] delta : KNIGHT_DELTAS) {
      final var origin = offset(whiteKnight, delta);
      if (origin != -1 && origin != whiteKing && origin != whiteBishop && origin != blackKing) {
        result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
            whiteKing, whiteBishop, origin, blackKing, WHITE_TO_MOVE, predecessorDistance));
      }
    }
    return result;
  }

  private static int addBlackPredecessors(BitSet legalStates, BitSet winning, int[] witnessByState,
      byte[] distanceByState, IntQueue queue, int state, int predecessorDistance) {
    final var whiteKing = whiteKing(state);
    final var whiteBishop = whiteBishop(state);
    final var whiteKnight = whiteKnight(state);
    final var blackKing = blackKing(state);
    var result = 0;

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(blackKing, delta);
      if (origin != -1 && origin != whiteKing && origin != whiteBishop && origin != whiteKnight) {
        result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
            whiteKing, whiteBishop, whiteKnight, origin, BLACK_TO_MOVE, predecessorDistance));
      }
    }
    return result;
  }

  private static int addPredecessor(BitSet legalStates, BitSet winning, int[] witnessByState, byte[] distanceByState,
      IntQueue queue, int witnessTarget, int whiteKing, int whiteBishop, int whiteKnight, int blackKing, int havingMove,
      int predecessorDistance) {
    final var predecessor = encode(whiteKing, whiteBishop, whiteKnight, blackKing, havingMove);
    if (legalStates.get(predecessor) && !winning.get(predecessor)) {
      winning.set(predecessor);
      witnessByState[predecessor] = witnessTarget;
      distanceByState[predecessor] = (byte) predecessorDistance;
      queue.add(predecessor);
      return predecessorDistance;
    }
    return 0;
  }

  private static int verifyTerminals(BitSet checkmates) {
    var result = 0;
    for (var state = checkmates.nextSetBit(0); state >= 0; state = checkmates.nextSetBit(state + 1)) {
      final BitboardPosition position = toBitboardPosition(state);
      if (havingMove(state) != BLACK_TO_MOVE || !position.isInCheck(Side.BLACK)
          || !position.legalMoves(Side.BLACK, 0L).isEmpty()) {
        throw new AssertionError("Terminal is not a verified black checkmate: " + state);
      }
      result++;
    }
    return result;
  }

  private static int verifyWitnesses(BitSet winning, int[] witnessByState, byte[] distanceByState) {
    var result = 0;
    for (var state = winning.nextSetBit(0); state >= 0; state = winning.nextSetBit(state + 1)) {
      final var target = witnessByState[state];
      if (target == NO_WITNESS) {
        continue;
      }
      final var witness = moveBetween(state, target);
      final Set<MoveSpecification> legalMoves = toBitboardPosition(state).legalMoves(side(havingMove(state)), 0L);
      if (!legalMoves.contains(witness)) {
        throw new AssertionError("Witness is not legal: " + state + " " + witness);
      }
      if (capturesWhitePiece(state, witness)) {
        throw new AssertionError("Witness leaves the material class: " + state + " " + witness);
      }
      if (!winning.get(target)) {
        throw new AssertionError("Witness target is not winning: " + state + " " + witness);
      }
      if (Byte.toUnsignedInt(distanceByState[target]) >= Byte.toUnsignedInt(distanceByState[state])) {
        throw new AssertionError("Witness does not descend toward mate: " + state + " " + witness);
      }
      result++;
    }
    return result;
  }

  private static int verifyTheoremRoots(BitSet legalStates, BitSet checkmates, BitSet winning) {
    var result = 0;
    for (var state = legalStates.nextSetBit(0); state >= 0; state = legalStates.nextSetBit(state + 1)) {
      if (havingMove(state) != BLACK_TO_MOVE || checkmates.get(state)) {
        continue;
      }
      final var blackMoveMask = blackMoveMask(whiteKing(state), whiteBishop(state), whiteKnight(state),
          blackKing(state));
      final var hasLegalMove = (blackMoveMask & HAS_LEGAL_BLACK_MOVE) != 0;
      final var hasNonCaptureMove = (blackMoveMask & HAS_NON_CAPTURE_BLACK_MOVE) != 0;
      if (!hasLegalMove) {
        if (isBlackInCheck(whiteKing(state), whiteBishop(state), whiteKnight(state), blackKing(state))) {
          throw new AssertionError("Non-terminal black-to-move state has no legal moves while in check: " + state);
        }
        continue;
      }
      if (hasNonCaptureMove && winning.get(state)) {
        result++;
      }
    }
    return result;
  }

  private static MoveSpecification moveBetween(int source, int target) {
    if (havingMove(source) == WHITE_TO_MOVE) {
      if (whiteKing(source) != whiteKing(target)) {
        return new MoveSpecification(square(whiteKing(source)), square(whiteKing(target)));
      }
      if (whiteBishop(source) != whiteBishop(target)) {
        return new MoveSpecification(square(whiteBishop(source)), square(whiteBishop(target)));
      }
      if (whiteKnight(source) != whiteKnight(target)) {
        return new MoveSpecification(square(whiteKnight(source)), square(whiteKnight(target)));
      }
    } else if (blackKing(source) != blackKing(target)) {
      return new MoveSpecification(square(blackKing(source)), square(blackKing(target)));
    }
    throw new AssertionError("States are not connected by one material-preserving move: " + source + " " + target);
  }

  private static boolean capturesWhitePiece(int state, MoveSpecification move) {
    return havingMove(state) == BLACK_TO_MOVE
        && (move.toSquare().ordinal() == whiteBishop(state) || move.toSquare().ordinal() == whiteKnight(state));
  }

  private static int blackMoveMask(int whiteKing, int whiteBishop, int whiteKnight, int blackKing) {
    var result = 0;
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing) {
        continue;
      }
      final var capturesBishop = target == whiteBishop;
      final var capturesKnight = target == whiteKnight;
      final var bishopAfterMove = capturesBishop ? -1 : whiteBishop;
      final var knightAfterMove = capturesKnight ? -1 : whiteKnight;
      if (!isBlackInCheck(whiteKing, bishopAfterMove, knightAfterMove, target)) {
        result |= HAS_LEGAL_BLACK_MOVE;
        if (!capturesBishop && !capturesKnight) {
          result |= HAS_NON_CAPTURE_BLACK_MOVE;
        }
      }
    }
    return result;
  }

  private static boolean isLegalState(int whiteKing, int whiteBishop, int whiteKnight, int blackKing, int havingMove) {
    return switch (havingMove) {
      case WHITE_TO_MOVE -> !isBlackInCheck(whiteKing, whiteBishop, whiteKnight, blackKing);
      case BLACK_TO_MOVE -> !areKingsAdjacent(whiteKing, blackKing);
      default -> throw new IllegalArgumentException();
    };
  }

  private static boolean isBlackInCheck(int whiteKing, int whiteBishop, int whiteKnight, int blackKing) {
    return areKingsAdjacent(whiteKing, blackKing) || isKnightAttack(whiteKnight, blackKing)
        || isBishopAttack(whiteBishop, whiteKing, whiteKnight, blackKing);
  }

  private static boolean areKingsAdjacent(int whiteKing, int blackKing) {
    return Math.abs(file(whiteKing) - file(blackKing)) <= 1 && Math.abs(rank(whiteKing) - rank(blackKing)) <= 1;
  }

  private static boolean isKnightAttack(int whiteKnight, int blackKing) {
    if (whiteKnight == -1) {
      return false;
    }
    final var fileDistance = Math.abs(file(whiteKnight) - file(blackKing));
    final var rankDistance = Math.abs(rank(whiteKnight) - rank(blackKing));
    return fileDistance * rankDistance == 2;
  }

  private static boolean isBishopAttack(int whiteBishop, int whiteKing, int whiteKnight, int blackKing) {
    if (whiteBishop == -1) {
      return false;
    }
    final var fileDistance = file(blackKing) - file(whiteBishop);
    final var rankDistance = rank(blackKing) - rank(whiteBishop);
    if (Math.abs(fileDistance) != Math.abs(rankDistance)) {
      return false;
    }
    final var fileStep = Integer.signum(fileDistance);
    final var rankStep = Integer.signum(rankDistance);
    var file = file(whiteBishop) + fileStep;
    var rank = rank(whiteBishop) + rankStep;
    while (true) {
      final var square = square(file, rank);
      if (square == blackKing) {
        return true;
      }
      if (square == whiteKing || square == whiteKnight) {
        return false;
      }
      file += fileStep;
      rank += rankStep;
    }
  }

  private static BitboardPosition toBitboardPosition(int state) {
    return new BitboardPosition(0L, 0L, bit(whiteKnight(state)), bit(whiteBishop(state)), 0L, bit(whiteKing(state)),
        0L, 0L, 0L, 0L, 0L, bit(blackKing(state)));
  }

  private static long bit(int square) {
    return 1L << square;
  }

  private static int encode(int whiteKing, int whiteBishop, int whiteKnight, int blackKing, int havingMove) {
    return (((whiteKing * SQUARE_COUNT + whiteBishop) * SQUARE_COUNT + whiteKnight) * SQUARE_COUNT + blackKing) * 2
        + havingMove;
  }

  private static int whiteKing(int state) {
    return state >>> 19;
  }

  private static int whiteBishop(int state) {
    return state >>> 13 & 0x3F;
  }

  private static int whiteKnight(int state) {
    return state >>> 7 & 0x3F;
  }

  private static int blackKing(int state) {
    return state >>> 1 & 0x3F;
  }

  private static int havingMove(int state) {
    return state & 1;
  }

  private static int offset(int square, int[] delta) {
    final var targetFile = file(square) + delta[0];
    final var targetRank = rank(square) + delta[1];
    if (targetFile < 0 || targetFile >= 8 || targetRank < 0 || targetRank >= 8) {
      return -1;
    }
    return square(targetFile, targetRank);
  }

  private static int file(int square) {
    return square & 7;
  }

  private static int rank(int square) {
    return square >>> 3;
  }

  private static int square(int file, int rank) {
    return rank * 8 + file;
  }

  private static boolean isLightSquare(int square) {
    return (file(square) + rank(square)) % 2 == 1;
  }

  private static Square square(int square) {
    return Nulls.get(Square.REAL, square);
  }

  private static Side side(int havingMove) {
    return switch (havingMove) {
      case WHITE_TO_MOVE -> Side.WHITE;
      case BLACK_TO_MOVE -> Side.BLACK;
      default -> throw new IllegalArgumentException();
    };
  }

  record VerificationResult(int legalStateCount, int terminalCheckmateCount, int winningStateCount,
      int witnessStateCount, int verifiedTerminalCount, int verifiedWitnessCount, int verifiedTheoremRootCount,
      int maximumDistance) {
  }

  private static final class IntQueue {
    private int[] elements = new int[1 << 20];
    private int head;
    private int tail;

    void add(int value) {
      if (tail == elements.length) {
        compactOrGrow();
      }
      elements[tail] = value;
      tail++;
    }

    int remove() {
      final var result = elements[head];
      head++;
      return result;
    }

    boolean isEmpty() {
      return head == tail;
    }

    private void compactOrGrow() {
      if (head > 0) {
        final var length = tail - head;
        System.arraycopy(elements, head, elements, 0, length);
        head = 0;
        tail = length;
        return;
      }

      final int[] grown = new int[elements.length * 2];
      System.arraycopy(elements, 0, grown, 0, elements.length);
      elements = grown;
    }
  }
}
