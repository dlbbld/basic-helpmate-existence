package io.github.dlbbld.basiccheckmatereachability;

import java.util.BitSet;

final class TwoMajorPieceWinnabilityAnalysis {

  private static final int WHITE_TO_MOVE = 0;
  private static final int BLACK_TO_MOVE = 1;
  private static final int SQUARE_COUNT = 64;
  private static final int NO_PIECE = 64;
  private static final int MAJOR_SLOT_COUNT = 65;
  private static final int STATE_COUNT = SQUARE_COUNT * MAJOR_SLOT_COUNT * MAJOR_SLOT_COUNT * SQUARE_COUNT * 2;

  private static final int[][] KING_DELTAS = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 },
      { 1, 0 }, { 1, 1 } };
  private static final int[][] ROOK_DELTAS = { { -1, 0 }, { 0, -1 }, { 0, 1 }, { 1, 0 } };
  private static final int[][] BISHOP_DELTAS = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };

  private TwoMajorPieceWinnabilityAnalysis() {
  }

  static AnalysisResult analyze(MajorPiece majorPiece) {
    final var legalStates = new BitSet(STATE_COUNT);
    final var checkmates = new BitSet(STATE_COUNT);
    final var stalemates = new BitSet(STATE_COUNT);
    final var winning = new BitSet(STATE_COUNT);
    final var distanceByState = new byte[STATE_COUNT];
    final var queue = new IntQueue();
    final var counts = new MutableCounts();

    enumerateStates(majorPiece, legalStates, checkmates, stalemates, winning, queue, counts);
    final var maximumDistance = calculateWinning(majorPiece, legalStates, winning, distanceByState, queue);
    final var exactTwo = countExactTwo(majorPiece, legalStates, checkmates, stalemates, winning);

    return new AnalysisResult(majorPiece, counts.legalStateCount, counts.oneMajorLegalStateCount,
        counts.twoMajorLegalStateCount, counts.twoMajorWhiteToMoveStateCount, counts.twoMajorBlackToMoveStateCount,
        counts.checkmateStateCount, counts.stalemateStateCount, counts.twoMajorCheckmateStateCount,
        counts.twoMajorStalemateStateCount, exactTwo.twoMajorOngoingStateCount(),
        exactTwo.twoMajorUnwinnableOngoingStateCount(), exactTwo.twoMajorForcedFirstCaptureStateCount(),
        exactTwo.twoMajorForcedFirstCaptureOneMoveStateCount(), exactTwo.twoMajorForcedFirstCaptureTwoMoveStateCount(),
        winning.cardinality(), maximumDistance);
  }

  private static void enumerateStates(MajorPiece majorPiece, BitSet legalStates, BitSet checkmates, BitSet stalemates,
      BitSet winning, IntQueue queue, MutableCounts counts) {
    for (var whiteKing = 0; whiteKing < SQUARE_COUNT; whiteKing++) {
      for (var blackKing = 0; blackKing < SQUARE_COUNT; blackKing++) {
        if (blackKing == whiteKing) {
          continue;
        }
        for (var firstMajor = 0; firstMajor < SQUARE_COUNT; firstMajor++) {
          if (firstMajor == whiteKing || firstMajor == blackKing) {
            continue;
          }
          addLegalStates(majorPiece, legalStates, checkmates, stalemates, winning, queue, counts, whiteKing,
              firstMajor, NO_PIECE, blackKing);
          for (var secondMajor = firstMajor + 1; secondMajor < SQUARE_COUNT; secondMajor++) {
            if (secondMajor == whiteKing || secondMajor == blackKing) {
              continue;
            }
            addLegalStates(majorPiece, legalStates, checkmates, stalemates, winning, queue, counts, whiteKing,
                firstMajor, secondMajor, blackKing);
          }
        }
      }
    }
  }

  private static void addLegalStates(MajorPiece majorPiece, BitSet legalStates, BitSet checkmates, BitSet stalemates,
      BitSet winning, IntQueue queue, MutableCounts counts, int whiteKing, int firstMajor, int secondMajor,
      int blackKing) {
    for (var havingMove = WHITE_TO_MOVE; havingMove <= BLACK_TO_MOVE; havingMove++) {
      if (!isLegalState(majorPiece, whiteKing, firstMajor, secondMajor, blackKing, havingMove)) {
        continue;
      }
      final var state = encode(whiteKing, firstMajor, secondMajor, blackKing, havingMove);
      legalStates.set(state);
      counts.legalStateCount++;
      if (isTwoMajorState(secondMajor)) {
        counts.twoMajorLegalStateCount++;
        if (havingMove == WHITE_TO_MOVE) {
          counts.twoMajorWhiteToMoveStateCount++;
        } else {
          counts.twoMajorBlackToMoveStateCount++;
        }
      } else {
        counts.oneMajorLegalStateCount++;
      }

      final var legalMoveCount = legalMoveCount(majorPiece, whiteKing, firstMajor, secondMajor, blackKing, havingMove);
      if (havingMove == BLACK_TO_MOVE && isBlackInCheck(majorPiece, whiteKing, firstMajor, secondMajor, blackKing)
          && legalMoveCount == 0) {
        checkmates.set(state);
        winning.set(state);
        queue.add(state);
        counts.checkmateStateCount++;
        if (isTwoMajorState(secondMajor)) {
          counts.twoMajorCheckmateStateCount++;
        }
      } else if (legalMoveCount == 0
          && !isInCheck(majorPiece, whiteKing, firstMajor, secondMajor, blackKing, havingMove)) {
        stalemates.set(state);
        counts.stalemateStateCount++;
        if (isTwoMajorState(secondMajor)) {
          counts.twoMajorStalemateStateCount++;
        }
      }
    }
  }

  private static int calculateWinning(MajorPiece majorPiece, BitSet legalStates, BitSet winning, byte[] distanceByState,
      IntQueue queue) {
    var maximumDistance = 0;
    while (!queue.isEmpty()) {
      final var state = queue.remove();
      final var predecessorDistance = Byte.toUnsignedInt(distanceByState[state]) + 1;
      if (havingMove(state) == BLACK_TO_MOVE) {
        maximumDistance = Math.max(maximumDistance,
            addWhitePredecessors(majorPiece, legalStates, winning, distanceByState, queue, state, predecessorDistance));
      } else {
        maximumDistance = Math.max(maximumDistance,
            addBlackPredecessors(legalStates, winning, distanceByState, queue, state, predecessorDistance));
      }
    }
    return maximumDistance;
  }

  private static ExactTwoCounts countExactTwo(MajorPiece majorPiece, BitSet legalStates, BitSet checkmates,
      BitSet stalemates, BitSet winning) {
    var ongoing = 0;
    var unwinnableOngoing = 0;
    var forcedFirstCapture = 0;
    var forcedFirstCaptureOneMove = 0;
    var forcedFirstCaptureTwoMoves = 0;
    for (var state = legalStates.nextSetBit(0); state >= 0; state = legalStates.nextSetBit(state + 1)) {
      if (!isTwoMajorState(secondMajor(state)) || checkmates.get(state) || stalemates.get(state)) {
        continue;
      }
      ongoing++;
      if (!winning.get(state)) {
        unwinnableOngoing++;
      }
      if (havingMove(state) == BLACK_TO_MOVE) {
        final var moveCount = legalMoveCount(majorPiece, whiteKing(state), firstMajor(state), secondMajor(state),
            blackKing(state), BLACK_TO_MOVE);
        if (moveCount > 0 && moveCount == majorCaptureMoveCount(majorPiece, whiteKing(state), firstMajor(state),
            secondMajor(state), blackKing(state))) {
          forcedFirstCapture++;
          if (moveCount == 1) {
            forcedFirstCaptureOneMove++;
          } else if (moveCount == 2) {
            forcedFirstCaptureTwoMoves++;
          }
        }
      }
    }
    return new ExactTwoCounts(ongoing, unwinnableOngoing, forcedFirstCapture, forcedFirstCaptureOneMove,
        forcedFirstCaptureTwoMoves);
  }

  private static int addWhitePredecessors(MajorPiece majorPiece, BitSet legalStates, BitSet winning,
      byte[] distanceByState, IntQueue queue, int state, int predecessorDistance) {
    final var whiteKing = whiteKing(state);
    final var firstMajor = firstMajor(state);
    final var secondMajor = secondMajor(state);
    final var blackKing = blackKing(state);
    var result = 0;

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(whiteKing, delta);
      if (origin != -1 && origin != firstMajor && origin != secondMajor && origin != blackKing) {
        result = Math.max(result, addPredecessor(legalStates, winning, distanceByState, queue, origin, firstMajor,
            secondMajor, blackKing, WHITE_TO_MOVE, predecessorDistance));
      }
    }

    result = Math.max(result, addMajorPredecessors(majorPiece, legalStates, winning, distanceByState, queue, state,
        whiteKing, firstMajor, secondMajor, blackKing, firstMajor, predecessorDistance));
    if (isTwoMajorState(secondMajor)) {
      result = Math.max(result, addMajorPredecessors(majorPiece, legalStates, winning, distanceByState, queue, state,
          whiteKing, firstMajor, secondMajor, blackKing, secondMajor, predecessorDistance));
    }
    return result;
  }

  private static int addMajorPredecessors(MajorPiece majorPiece, BitSet legalStates, BitSet winning,
      byte[] distanceByState, IntQueue queue, int state, int whiteKing, int firstMajor, int secondMajor, int blackKing,
      int destination, int predecessorDistance) {
    var result = 0;
    for (final int[] delta : majorPiece.deltas()) {
      var origin = offset(destination, delta);
      while (origin != -1) {
        if (origin == whiteKing || origin == blackKing || origin == otherMajor(firstMajor, secondMajor, destination)) {
          break;
        }
        final var packed = replaceMajor(firstMajor, secondMajor, destination, origin);
        result = Math.max(result, addPredecessor(legalStates, winning, distanceByState, queue, whiteKing,
            first(packed), second(packed), blackKing, WHITE_TO_MOVE, predecessorDistance));
        origin = offset(origin, delta);
      }
    }
    return result;
  }

  private static int addBlackPredecessors(BitSet legalStates, BitSet winning, byte[] distanceByState, IntQueue queue,
      int state, int predecessorDistance) {
    final var whiteKing = whiteKing(state);
    final var firstMajor = firstMajor(state);
    final var secondMajor = secondMajor(state);
    final var blackKing = blackKing(state);
    var result = 0;

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(blackKing, delta);
      if (origin == -1 || origin == whiteKing || origin == firstMajor || origin == secondMajor) {
        continue;
      }
      result = Math.max(result, addPredecessor(legalStates, winning, distanceByState, queue, whiteKing, firstMajor,
          secondMajor, origin, BLACK_TO_MOVE, predecessorDistance));
      if (!isTwoMajorState(secondMajor)) {
        final var packed = packMajorPair(firstMajor, blackKing);
        result = Math.max(result, addPredecessor(legalStates, winning, distanceByState, queue, whiteKing,
            first(packed), second(packed), origin, BLACK_TO_MOVE, predecessorDistance));
      }
    }
    return result;
  }

  private static int addPredecessor(BitSet legalStates, BitSet winning, byte[] distanceByState, IntQueue queue,
      int whiteKing, int firstMajor, int secondMajor, int blackKing, int havingMove, int predecessorDistance) {
    final var predecessor = encode(whiteKing, firstMajor, secondMajor, blackKing, havingMove);
    if (legalStates.get(predecessor) && !winning.get(predecessor)) {
      winning.set(predecessor);
      distanceByState[predecessor] = (byte) predecessorDistance;
      queue.add(predecessor);
      return predecessorDistance;
    }
    return 0;
  }

  private static boolean isLegalState(MajorPiece majorPiece, int whiteKing, int firstMajor, int secondMajor,
      int blackKing, int havingMove) {
    if (whiteKing == blackKing || firstMajor == whiteKing || firstMajor == blackKing || secondMajor == whiteKing
        || secondMajor == blackKing || firstMajor == secondMajor || firstMajor == NO_PIECE) {
      return false;
    }
    return switch (havingMove) {
      case WHITE_TO_MOVE -> !isBlackInCheck(majorPiece, whiteKing, firstMajor, secondMajor, blackKing);
      case BLACK_TO_MOVE -> !areKingsAdjacent(whiteKing, blackKing);
      default -> throw new IllegalArgumentException();
    };
  }

  private static int legalMoveCount(MajorPiece majorPiece, int whiteKing, int firstMajor, int secondMajor, int blackKing,
      int havingMove) {
    return switch (havingMove) {
      case WHITE_TO_MOVE -> whiteLegalMoveCount(majorPiece, whiteKing, firstMajor, secondMajor, blackKing);
      case BLACK_TO_MOVE -> blackLegalMoveCount(majorPiece, whiteKing, firstMajor, secondMajor, blackKing);
      default -> throw new IllegalArgumentException();
    };
  }

  private static int whiteLegalMoveCount(MajorPiece majorPiece, int whiteKing, int firstMajor, int secondMajor,
      int blackKing) {
    var result = 0;
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(whiteKing, delta);
      if (target != -1 && target != firstMajor && target != secondMajor && target != blackKing
          && isLegalState(majorPiece, target, firstMajor, secondMajor, blackKing, BLACK_TO_MOVE)) {
        result++;
      }
    }
    result += majorLegalMoveCount(majorPiece, whiteKing, firstMajor, secondMajor, blackKing, firstMajor);
    if (isTwoMajorState(secondMajor)) {
      result += majorLegalMoveCount(majorPiece, whiteKing, firstMajor, secondMajor, blackKing, secondMajor);
    }
    return result;
  }

  private static int majorLegalMoveCount(MajorPiece majorPiece, int whiteKing, int firstMajor, int secondMajor,
      int blackKing, int origin) {
    var result = 0;
    for (final int[] delta : majorPiece.deltas()) {
      var target = offset(origin, delta);
      while (target != -1) {
        if (target == whiteKing || target == blackKing || target == otherMajor(firstMajor, secondMajor, origin)) {
          break;
        }
        final var packed = replaceMajor(firstMajor, secondMajor, origin, target);
        if (isLegalState(majorPiece, whiteKing, first(packed), second(packed), blackKing, BLACK_TO_MOVE)) {
          result++;
        }
        target = offset(target, delta);
      }
    }
    return result;
  }

  private static int blackLegalMoveCount(MajorPiece majorPiece, int whiteKing, int firstMajor, int secondMajor,
      int blackKing) {
    var result = 0;
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing) {
        continue;
      }
      var nextFirstMajor = firstMajor;
      var nextSecondMajor = secondMajor;
      if (target == firstMajor) {
        nextFirstMajor = secondMajor;
        nextSecondMajor = NO_PIECE;
      } else if (target == secondMajor) {
        nextSecondMajor = NO_PIECE;
      }
      if ((nextFirstMajor == NO_PIECE || isLegalState(majorPiece, whiteKing, nextFirstMajor, nextSecondMajor, target,
          WHITE_TO_MOVE)) && !isBlackInCheck(majorPiece, whiteKing, nextFirstMajor, nextSecondMajor, target)) {
        result++;
      }
    }
    return result;
  }

  private static int majorCaptureMoveCount(MajorPiece majorPiece, int whiteKing, int firstMajor, int secondMajor,
      int blackKing) {
    var result = 0;
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target != firstMajor && target != secondMajor) {
        continue;
      }
      final var nextFirstMajor = target == firstMajor ? secondMajor : firstMajor;
      if (nextFirstMajor != NO_PIECE && isLegalState(majorPiece, whiteKing, nextFirstMajor, NO_PIECE, target,
          WHITE_TO_MOVE)) {
        result++;
      }
    }
    return result;
  }

  private static boolean isInCheck(MajorPiece majorPiece, int whiteKing, int firstMajor, int secondMajor, int blackKing,
      int havingMove) {
    return havingMove == WHITE_TO_MOVE ? areKingsAdjacent(whiteKing, blackKing)
        : isBlackInCheck(majorPiece, whiteKing, firstMajor, secondMajor, blackKing);
  }

  private static boolean isBlackInCheck(MajorPiece majorPiece, int whiteKing, int firstMajor, int secondMajor,
      int blackKing) {
    if (areKingsAdjacent(whiteKing, blackKing)) {
      return true;
    }
    return majorPiece.attacks(firstMajor, whiteKing, secondMajor, blackKing)
        || majorPiece.attacks(secondMajor, whiteKing, firstMajor, blackKing);
  }

  private static boolean areKingsAdjacent(int whiteKing, int blackKing) {
    return Math.abs(file(whiteKing) - file(blackKing)) <= 1 && Math.abs(rank(whiteKing) - rank(blackKing)) <= 1;
  }

  private static boolean isRookAttack(int attacker, int blockerA, int blockerB, int target) {
    if (attacker == NO_PIECE || file(attacker) != file(target) && rank(attacker) != rank(target)) {
      return false;
    }
    return isSlidingAttack(attacker, blockerA, blockerB, target);
  }

  private static boolean isBishopAttack(int attacker, int blockerA, int blockerB, int target) {
    if (attacker == NO_PIECE) {
      return false;
    }
    final var fileDistance = file(target) - file(attacker);
    final var rankDistance = rank(target) - rank(attacker);
    if (Math.abs(fileDistance) != Math.abs(rankDistance)) {
      return false;
    }
    return isSlidingAttack(attacker, blockerA, blockerB, target);
  }

  private static boolean isSlidingAttack(int attacker, int blockerA, int blockerB, int target) {
    final var fileStep = Integer.signum(file(target) - file(attacker));
    final var rankStep = Integer.signum(rank(target) - rank(attacker));
    var file = file(attacker) + fileStep;
    var rank = rank(attacker) + rankStep;
    while (true) {
      final var square = square(file, rank);
      if (square == target) {
        return true;
      }
      if (square == blockerA || square == blockerB) {
        return false;
      }
      file += fileStep;
      rank += rankStep;
    }
  }

  private static int replaceMajor(int firstMajor, int secondMajor, int oldSquare, int newSquare) {
    if (!isTwoMajorState(secondMajor)) {
      return packMajorPair(newSquare, NO_PIECE);
    }
    return oldSquare == firstMajor ? packMajorPair(newSquare, secondMajor) : packMajorPair(firstMajor, newSquare);
  }

  private static int otherMajor(int firstMajor, int secondMajor, int major) {
    if (!isTwoMajorState(secondMajor)) {
      return NO_PIECE;
    }
    return major == firstMajor ? secondMajor : firstMajor;
  }

  private static int packMajorPair(int firstMajor, int secondMajor) {
    if (secondMajor == NO_PIECE) {
      return firstMajor << 7 | NO_PIECE;
    }
    return firstMajor < secondMajor ? firstMajor << 7 | secondMajor : secondMajor << 7 | firstMajor;
  }

  private static int first(int packed) {
    return packed >>> 7;
  }

  private static int second(int packed) {
    return packed & 0x7F;
  }

  private static boolean isTwoMajorState(int secondMajor) {
    return secondMajor != NO_PIECE;
  }

  private static int encode(int whiteKing, int firstMajor, int secondMajor, int blackKing, int havingMove) {
    return ((((whiteKing * MAJOR_SLOT_COUNT + firstMajor) * MAJOR_SLOT_COUNT + secondMajor) * SQUARE_COUNT
        + blackKing) * 2) + havingMove;
  }

  private static int whiteKing(int state) {
    return state / (MAJOR_SLOT_COUNT * MAJOR_SLOT_COUNT * SQUARE_COUNT * 2);
  }

  private static int firstMajor(int state) {
    return state / (MAJOR_SLOT_COUNT * SQUARE_COUNT * 2) % MAJOR_SLOT_COUNT;
  }

  private static int secondMajor(int state) {
    return state / (SQUARE_COUNT * 2) % MAJOR_SLOT_COUNT;
  }

  private static int blackKing(int state) {
    return state / 2 % SQUARE_COUNT;
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

  enum MajorPiece {
    ROOK("KRRvK", ROOK_DELTAS) {
      @Override
      boolean attacks(int attacker, int blockerA, int blockerB, int target) {
        return isRookAttack(attacker, blockerA, blockerB, target);
      }
    },
    QUEEN("KQQvK", concat(ROOK_DELTAS, BISHOP_DELTAS)) {
      @Override
      boolean attacks(int attacker, int blockerA, int blockerB, int target) {
        return isRookAttack(attacker, blockerA, blockerB, target)
            || isBishopAttack(attacker, blockerA, blockerB, target);
      }
    };

    private final String materialName;
    private final int[][] deltas;

    MajorPiece(String materialName, int[][] deltas) {
      this.materialName = materialName;
      this.deltas = deltas;
    }

    String materialName() {
      return materialName;
    }

    int[][] deltas() {
      return deltas;
    }

    abstract boolean attacks(int attacker, int blockerA, int blockerB, int target);
  }

  record AnalysisResult(MajorPiece majorPiece, int legalStateCount, int oneMajorLegalStateCount,
      int twoMajorLegalStateCount, int twoMajorWhiteToMoveStateCount, int twoMajorBlackToMoveStateCount,
      int checkmateStateCount, int stalemateStateCount, int twoMajorCheckmateStateCount,
      int twoMajorStalemateStateCount, int twoMajorOngoingStateCount, int twoMajorUnwinnableOngoingStateCount,
      int twoMajorForcedFirstCaptureStateCount, int twoMajorForcedFirstCaptureOneMoveStateCount,
      int twoMajorForcedFirstCaptureTwoMoveStateCount, int winningStateCount, int maximumDistance) {
  }

  private record ExactTwoCounts(int twoMajorOngoingStateCount, int twoMajorUnwinnableOngoingStateCount,
      int twoMajorForcedFirstCaptureStateCount, int twoMajorForcedFirstCaptureOneMoveStateCount,
      int twoMajorForcedFirstCaptureTwoMoveStateCount) {
  }

  private static final class MutableCounts {
    private int legalStateCount;
    private int oneMajorLegalStateCount;
    private int twoMajorLegalStateCount;
    private int twoMajorWhiteToMoveStateCount;
    private int twoMajorBlackToMoveStateCount;
    private int checkmateStateCount;
    private int stalemateStateCount;
    private int twoMajorCheckmateStateCount;
    private int twoMajorStalemateStateCount;
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
      final var grown = new int[elements.length * 2];
      System.arraycopy(elements, 0, grown, 0, elements.length);
      elements = grown;
    }
  }

  private static int[][] concat(int[][] first, int[][] second) {
    final var result = new int[first.length + second.length][];
    System.arraycopy(first, 0, result, 0, first.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }
}
