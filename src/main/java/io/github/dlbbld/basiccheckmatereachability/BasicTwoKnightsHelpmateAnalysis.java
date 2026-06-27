package io.github.dlbbld.basiccheckmatereachability;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;

final class BasicTwoKnightsHelpmateAnalysis {

  private static final int WHITE_TO_MOVE = 0;
  private static final int BLACK_TO_MOVE = 1;
  private static final int SQUARE_COUNT = 64;
  private static final int STATE_COUNT = SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * 2;

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

  private BasicTwoKnightsHelpmateAnalysis() {
  }

  static AnalysisResult analyze() {
    final BitSet legalStates = new BitSet(STATE_COUNT);
    final BitSet winning = new BitSet(STATE_COUNT);
    final IntQueue queue = new IntQueue();

    var legalStateCount = 0;
    var whiteToMoveStateCount = 0;
    var blackToMoveStateCount = 0;
    var blackToMoveInCheckStateCount = 0;
    var blackCheckmateCount = 0;

    for (var whiteKing = 0; whiteKing < SQUARE_COUNT; whiteKing++) {
      for (var whiteKnightA = 0; whiteKnightA < SQUARE_COUNT; whiteKnightA++) {
        if (whiteKnightA == whiteKing) {
          continue;
        }
        for (var whiteKnightB = whiteKnightA + 1; whiteKnightB < SQUARE_COUNT; whiteKnightB++) {
          if (whiteKnightB == whiteKing) {
            continue;
          }
          for (var blackKing = 0; blackKing < SQUARE_COUNT; blackKing++) {
            if (blackKing == whiteKing || blackKing == whiteKnightA || blackKing == whiteKnightB) {
              continue;
            }
            for (var havingMove = WHITE_TO_MOVE; havingMove <= BLACK_TO_MOVE; havingMove++) {
              final var state = encode(whiteKing, whiteKnightA, whiteKnightB, blackKing, havingMove);
              if (!isLegalState(whiteKing, whiteKnightA, whiteKnightB, blackKing, havingMove)) {
                continue;
              }
              legalStates.set(state);
              legalStateCount++;
              if (havingMove == WHITE_TO_MOVE) {
                whiteToMoveStateCount++;
                continue;
              }

              blackToMoveStateCount++;
              final var blackInCheck = isBlackInCheck(whiteKing, whiteKnightA, whiteKnightB, blackKing);
              if (blackInCheck) {
                blackToMoveInCheckStateCount++;
                if (blackMoves(whiteKing, whiteKnightA, whiteKnightB, blackKing).legalMoveCount() == 0) {
                  blackCheckmateCount++;
                  winning.set(state);
                  queue.add(state);
                }
              }
            }
          }
        }
      }
    }

    calculateWinning(legalStates, winning, queue);

    var unwinnableWhiteToMoveStateCount = 0;
    var forcedPieceCaptureOneMoveStateCount = 0;
    var forcedPieceCaptureTwoMoveStateCount = 0;
    var stalemateStateCount = 0;
    var counterexampleStateCount = 0;
    final NavigableSet<TwoKnightsState> unwinnableWhiteToMoveRepresentatives = new TreeSet<>();
    final NavigableSet<TwoKnightsState> forcedPieceCaptureOneMoveRepresentatives = new TreeSet<>();
    final NavigableSet<TwoKnightsState> forcedPieceCaptureTwoMoveRepresentatives = new TreeSet<>();
    final NavigableSet<TwoKnightsState> stalemateRepresentatives = new TreeSet<>();
    final NavigableSet<TwoKnightsState> counterexampleRepresentatives = new TreeSet<>();

    for (var state = legalStates.nextSetBit(0); state >= 0; state = legalStates.nextSetBit(state + 1)) {
      if (winning.get(state)) {
        continue;
      }
      if (havingMove(state) == WHITE_TO_MOVE) {
        unwinnableWhiteToMoveStateCount++;
        unwinnableWhiteToMoveRepresentatives.add(canonical(toState(state)));
        continue;
      }

      final var whiteKing = whiteKing(state);
      final var whiteKnightA = whiteKnightA(state);
      final var whiteKnightB = whiteKnightB(state);
      final var blackKing = blackKing(state);
      final var blackMoves = blackMoves(whiteKing, whiteKnightA, whiteKnightB, blackKing);

      if (blackMoves.legalMoveCount() == 0 && !isBlackInCheck(whiteKing, whiteKnightA, whiteKnightB, blackKing)) {
        stalemateStateCount++;
        stalemateRepresentatives.add(canonical(toState(state)));
      } else if (blackMoves.legalMoveCount() > 0 && !blackMoves.hasMaterialPreservingMove()) {
        if (blackMoves.legalMoveCount() == 1) {
          forcedPieceCaptureOneMoveStateCount++;
          forcedPieceCaptureOneMoveRepresentatives.add(canonical(toState(state)));
        } else if (blackMoves.legalMoveCount() == 2) {
          forcedPieceCaptureTwoMoveStateCount++;
          forcedPieceCaptureTwoMoveRepresentatives.add(canonical(toState(state)));
        } else {
          counterexampleStateCount++;
          counterexampleRepresentatives.add(canonical(toState(state)));
        }
      } else if (blackMoves.hasMaterialPreservingMove()) {
        counterexampleStateCount++;
        counterexampleRepresentatives.add(canonical(toState(state)));
      }
    }

    return new AnalysisResult(legalStateCount, whiteToMoveStateCount, blackToMoveStateCount, blackCheckmateCount,
        blackToMoveInCheckStateCount, blackToMoveStateCount - blackToMoveInCheckStateCount,
        unwinnableWhiteToMoveStateCount, unwinnableWhiteToMoveRepresentatives, winning.cardinality(),
        blackToMoveStateCount - blackCheckmateCount - stalemateStateCount, forcedPieceCaptureOneMoveStateCount,
        forcedPieceCaptureOneMoveRepresentatives, forcedPieceCaptureTwoMoveStateCount,
        forcedPieceCaptureTwoMoveRepresentatives, stalemateStateCount, stalemateRepresentatives,
        counterexampleStateCount, counterexampleRepresentatives);
  }

  static String format(AnalysisResult result) {
    final StringBuilder sb = new StringBuilder();
    sb.append("KNNvK states").append('\n');
    sb.append("legal states: ").append(result.legalStateCount()).append('\n');
    sb.append("white-to-move states: ").append(result.whiteToMoveStateCount()).append('\n');
    sb.append("black-to-move states: ").append(result.blackToMoveStateCount()).append('\n');
    sb.append("black-to-move in-check states: ").append(result.blackToMoveInCheckStateCount()).append('\n');
    sb.append("black-to-move not-in-check states: ").append(result.blackToMoveNotInCheckStateCount()).append('\n');
    sb.append("black checkmates: ").append(result.blackCheckmateCount()).append('\n');
    sb.append("unwinnable white-to-move states: ").append(result.unwinnableWhiteToMoveStateCount())
        .append(", canonical ").append(result.unwinnableWhiteToMoveRepresentatives().size()).append('\n');
    appendStates(sb, "unwinnable white-to-move representatives", result.unwinnableWhiteToMoveRepresentatives());
    sb.append("ongoing black-to-move states: ").append(result.ongoingBlackToMoveStateCount()).append('\n');
    sb.append("cooperatively winnable states: ").append(result.winningStateCount()).append('\n');
    sb.append("forced knight-capture states with one move: ")
        .append(result.forcedPieceCaptureOneMoveStateCount()).append(", canonical ")
        .append(result.forcedPieceCaptureOneMoveRepresentatives().size()).append('\n');
    appendStates(sb, "forced knight-capture one-move representatives",
        result.forcedPieceCaptureOneMoveRepresentatives());
    sb.append("forced knight-capture states with two moves: ")
        .append(result.forcedPieceCaptureTwoMoveStateCount()).append(", canonical ")
        .append(result.forcedPieceCaptureTwoMoveRepresentatives().size()).append('\n');
    appendStates(sb, "forced knight-capture two-move representatives",
        result.forcedPieceCaptureTwoMoveRepresentatives());
    sb.append("stalemate states: ").append(result.stalemateStateCount()).append(", canonical ")
        .append(result.stalemateRepresentatives().size()).append('\n');
    appendStates(sb, "stalemate representatives", result.stalemateRepresentatives());
    sb.append("counterexample states: ").append(result.counterexampleStateCount()).append(", canonical ")
        .append(result.counterexampleRepresentatives().size()).append('\n');
    appendStates(sb, "counterexample representatives", result.counterexampleRepresentatives());
    return sb.toString();
  }

  private static void appendStates(StringBuilder sb, String title, Set<TwoKnightsState> states) {
    sb.append(title).append(':').append('\n');
    for (final TwoKnightsState state : states) {
      sb.append("  ").append(toFen(state)).append(" ; moves=").append(formatBlackMoves(state)).append('\n');
    }
  }

  private static void calculateWinning(BitSet legalStates, BitSet winning, IntQueue queue) {
    while (!queue.isEmpty()) {
      final var state = queue.remove();
      if (havingMove(state) == BLACK_TO_MOVE) {
        addWhitePredecessors(legalStates, winning, queue, state);
      } else {
        addBlackPredecessors(legalStates, winning, queue, state);
      }
    }
  }

  private static void addWhitePredecessors(BitSet legalStates, BitSet winning, IntQueue queue, int state) {
    final var whiteKing = whiteKing(state);
    final var whiteKnightA = whiteKnightA(state);
    final var whiteKnightB = whiteKnightB(state);
    final var blackKing = blackKing(state);

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(whiteKing, delta);
      if (origin != -1 && origin != whiteKnightA && origin != whiteKnightB && origin != blackKing) {
        addPredecessor(legalStates, winning, queue, origin, whiteKnightA, whiteKnightB, blackKing, WHITE_TO_MOVE);
      }
    }

    addKnightPredecessors(legalStates, winning, queue, whiteKing, whiteKnightA, whiteKnightB, blackKing, true);
    addKnightPredecessors(legalStates, winning, queue, whiteKing, whiteKnightA, whiteKnightB, blackKing, false);
  }

  private static void addKnightPredecessors(BitSet legalStates, BitSet winning, IntQueue queue, int whiteKing,
      int whiteKnightA, int whiteKnightB, int blackKing, boolean moveKnightA) {
    final var destination = moveKnightA ? whiteKnightA : whiteKnightB;
    final var otherKnight = moveKnightA ? whiteKnightB : whiteKnightA;

    for (final int[] delta : KNIGHT_DELTAS) {
      final var origin = offset(destination, delta);
      if (origin == -1 || origin == whiteKing || origin == otherKnight || origin == blackKing) {
        continue;
      }
      addPredecessor(legalStates, winning, queue, whiteKing, Math.min(origin, otherKnight),
          Math.max(origin, otherKnight), blackKing, WHITE_TO_MOVE);
    }
  }

  private static void addBlackPredecessors(BitSet legalStates, BitSet winning, IntQueue queue, int state) {
    final var whiteKing = whiteKing(state);
    final var whiteKnightA = whiteKnightA(state);
    final var whiteKnightB = whiteKnightB(state);
    final var blackKing = blackKing(state);

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(blackKing, delta);
      if (origin != -1 && origin != whiteKing && origin != whiteKnightA && origin != whiteKnightB) {
        addPredecessor(legalStates, winning, queue, whiteKing, whiteKnightA, whiteKnightB, origin, BLACK_TO_MOVE);
      }
    }
  }

  private static void addPredecessor(BitSet legalStates, BitSet winning, IntQueue queue, int whiteKing,
      int whiteKnightA, int whiteKnightB, int blackKing, int havingMove) {
    final var predecessor = encode(whiteKing, whiteKnightA, whiteKnightB, blackKing, havingMove);
    if (legalStates.get(predecessor) && !winning.get(predecessor)) {
      winning.set(predecessor);
      queue.add(predecessor);
    }
  }

  private static BlackMoveSummary blackMoves(int whiteKing, int whiteKnightA, int whiteKnightB, int blackKing) {
    var legalMoveCount = 0;
    var hasMaterialPreservingMove = false;
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing) {
        continue;
      }
      final var capturesKnightA = target == whiteKnightA;
      final var capturesKnightB = target == whiteKnightB;
      final var knightAAfterMove = capturesKnightA ? -1 : whiteKnightA;
      final var knightBAfterMove = capturesKnightB ? -1 : whiteKnightB;
      if (!isBlackInCheck(whiteKing, knightAAfterMove, knightBAfterMove, target)) {
        legalMoveCount++;
        if (!capturesKnightA && !capturesKnightB) {
          hasMaterialPreservingMove = true;
        }
      }
    }
    return new BlackMoveSummary(legalMoveCount, hasMaterialPreservingMove);
  }

  private static boolean isLegalState(int whiteKing, int whiteKnightA, int whiteKnightB, int blackKing,
      int havingMove) {
    return switch (havingMove) {
      case WHITE_TO_MOVE -> !isBlackInCheck(whiteKing, whiteKnightA, whiteKnightB, blackKing);
      case BLACK_TO_MOVE -> !areKingsAdjacent(whiteKing, blackKing);
      default -> throw new IllegalArgumentException();
    };
  }

  private static boolean isBlackInCheck(int whiteKing, int whiteKnightA, int whiteKnightB, int blackKing) {
    return areKingsAdjacent(whiteKing, blackKing) || isKnightAttack(whiteKnightA, blackKing)
        || isKnightAttack(whiteKnightB, blackKing);
  }

  private static boolean areKingsAdjacent(int whiteKing, int blackKing) {
    return Math.abs(file(whiteKing) - file(blackKing)) <= 1 && Math.abs(rank(whiteKing) - rank(blackKing)) <= 1;
  }

  private static boolean isKnightAttack(int knight, int target) {
    if (knight == -1) {
      return false;
    }
    return Math.abs(file(knight) - file(target)) * Math.abs(rank(knight) - rank(target)) == 2;
  }

  private static int encode(int whiteKing, int whiteKnightA, int whiteKnightB, int blackKing, int havingMove) {
    return (((whiteKing * SQUARE_COUNT + whiteKnightA) * SQUARE_COUNT + whiteKnightB) * SQUARE_COUNT
        + blackKing) * 2 + havingMove;
  }

  private static int whiteKing(int state) {
    return state >>> 19;
  }

  private static int whiteKnightA(int state) {
    return state >>> 13 & 0x3F;
  }

  private static int whiteKnightB(int state) {
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

  private static TwoKnightsState toState(int state) {
    return new TwoKnightsState(square(whiteKing(state)), square(whiteKnightA(state)), square(whiteKnightB(state)),
        square(blackKing(state)), side(havingMove(state)));
  }

  private static Square square(int square) {
    return Square.REAL.get(square);
  }

  private static Side side(int havingMove) {
    return switch (havingMove) {
      case WHITE_TO_MOVE -> Side.WHITE;
      case BLACK_TO_MOVE -> Side.BLACK;
      default -> throw new IllegalArgumentException();
    };
  }

  private static NavigableSet<TwoKnightsState> canonicalRepresentatives(Collection<TwoKnightsState> states) {
    final NavigableSet<TwoKnightsState> result = new TreeSet<>();
    for (final TwoKnightsState state : states) {
      result.add(canonical(state));
    }
    return Collections.unmodifiableNavigableSet(result);
  }

  private static TwoKnightsState canonical(TwoKnightsState state) {
    TwoKnightsState result = state;
    for (var transformIndex = 1; transformIndex < 8; transformIndex++) {
      final TwoKnightsState transformed = transform(state, transformIndex);
      if (transformed.compareTo(result) < 0) {
        result = transformed;
      }
    }
    return result;
  }

  private static TwoKnightsState transform(TwoKnightsState state, int transformIndex) {
    final var knightA = transform(state.whiteKnightA(), transformIndex);
    final var knightB = transform(state.whiteKnightB(), transformIndex);
    final Square firstKnight;
    final Square secondKnight;
    if (knightA.compareTo(knightB) < 0) {
      firstKnight = knightA;
      secondKnight = knightB;
    } else {
      firstKnight = knightB;
      secondKnight = knightA;
    }
    return new TwoKnightsState(transform(state.whiteKing(), transformIndex), firstKnight, secondKnight,
        transform(state.blackKing(), transformIndex), state.havingMove());
  }

  private static Square transform(Square square, int transformIndex) {
    final var file = square.ordinal() % 8;
    final var rank = square.ordinal() / 8;
    final int transformedFile;
    final int transformedRank;
    switch (transformIndex) {
      case 0 -> {
        transformedFile = file;
        transformedRank = rank;
      }
      case 1 -> {
        transformedFile = 7 - file;
        transformedRank = rank;
      }
      case 2 -> {
        transformedFile = file;
        transformedRank = 7 - rank;
      }
      case 3 -> {
        transformedFile = 7 - file;
        transformedRank = 7 - rank;
      }
      case 4 -> {
        transformedFile = rank;
        transformedRank = file;
      }
      case 5 -> {
        transformedFile = 7 - rank;
        transformedRank = file;
      }
      case 6 -> {
        transformedFile = rank;
        transformedRank = 7 - file;
      }
      case 7 -> {
        transformedFile = 7 - rank;
        transformedRank = 7 - file;
      }
      default -> throw new IllegalArgumentException("transformIndex out of range: " + transformIndex);
    }
    return Square.REAL.get(transformedRank * 8 + transformedFile);
  }

  static String toFen(TwoKnightsState state) {
    final char[] board = new char[64];
    for (int i = 0; i < board.length; i++) {
      board[i] = '1';
    }
    board[state.whiteKing().ordinal()] = 'K';
    board[state.whiteKnightA().ordinal()] = 'N';
    board[state.whiteKnightB().ordinal()] = 'N';
    board[state.blackKing().ordinal()] = 'k';

    final StringBuilder sb = new StringBuilder();
    for (int rank = 7; rank >= 0; rank--) {
      if (rank < 7) {
        sb.append('/');
      }
      var emptyCount = 0;
      for (int file = 0; file < 8; file++) {
        final char piece = board[rank * 8 + file];
        if (piece == '1') {
          emptyCount++;
        } else {
          if (emptyCount > 0) {
            sb.append(emptyCount);
            emptyCount = 0;
          }
          sb.append(piece);
        }
      }
      if (emptyCount > 0) {
        sb.append(emptyCount);
      }
    }
    sb.append(' ').append(state.havingMove() == Side.WHITE ? 'w' : 'b').append(" - - 0 1");
    return sb.toString();
  }

  private static String formatBlackMoves(TwoKnightsState state) {
    if (state.havingMove() != Side.BLACK) {
      return "";
    }

    final var whiteKing = state.whiteKing().ordinal();
    final var whiteKnightA = state.whiteKnightA().ordinal();
    final var whiteKnightB = state.whiteKnightB().ordinal();
    final var blackKing = state.blackKing().ordinal();
    final StringBuilder sb = new StringBuilder();

    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing) {
        continue;
      }
      final var capturesKnightA = target == whiteKnightA;
      final var capturesKnightB = target == whiteKnightB;
      final var knightAAfterMove = capturesKnightA ? -1 : whiteKnightA;
      final var knightBAfterMove = capturesKnightB ? -1 : whiteKnightB;
      if (isBlackInCheck(whiteKing, knightAAfterMove, knightBAfterMove, target)) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(state.blackKing().getName()).append(square(target).getName());
      if (capturesKnightA || capturesKnightB) {
        sb.append("xN");
      }
    }
    return sb.toString();
  }

  record TwoKnightsState(Square whiteKing, Square whiteKnightA, Square whiteKnightB, Square blackKing,
      Side havingMove) implements Comparable<TwoKnightsState> {

    @Override
    public int compareTo(TwoKnightsState other) {
      if (whiteKing != other.whiteKing) {
        return whiteKing.compareTo(other.whiteKing);
      }
      if (whiteKnightA != other.whiteKnightA) {
        return whiteKnightA.compareTo(other.whiteKnightA);
      }
      if (whiteKnightB != other.whiteKnightB) {
        return whiteKnightB.compareTo(other.whiteKnightB);
      }
      if (blackKing != other.blackKing) {
        return blackKing.compareTo(other.blackKing);
      }
      return havingMove.compareTo(other.havingMove);
    }
  }

  record AnalysisResult(int legalStateCount, int whiteToMoveStateCount, int blackToMoveStateCount,
      int blackCheckmateCount, int blackToMoveInCheckStateCount, int blackToMoveNotInCheckStateCount,
      int unwinnableWhiteToMoveStateCount, Set<TwoKnightsState> unwinnableWhiteToMoveRepresentatives,
      int winningStateCount, int ongoingBlackToMoveStateCount, int forcedPieceCaptureOneMoveStateCount,
      Set<TwoKnightsState> forcedPieceCaptureOneMoveRepresentatives, int forcedPieceCaptureTwoMoveStateCount,
      Set<TwoKnightsState> forcedPieceCaptureTwoMoveRepresentatives, int stalemateStateCount,
      Set<TwoKnightsState> stalemateRepresentatives, int counterexampleStateCount,
      Set<TwoKnightsState> counterexampleRepresentatives) {

    AnalysisResult {
      unwinnableWhiteToMoveRepresentatives = canonicalRepresentatives(unwinnableWhiteToMoveRepresentatives);
      forcedPieceCaptureOneMoveRepresentatives = canonicalRepresentatives(forcedPieceCaptureOneMoveRepresentatives);
      forcedPieceCaptureTwoMoveRepresentatives = canonicalRepresentatives(forcedPieceCaptureTwoMoveRepresentatives);
      stalemateRepresentatives = canonicalRepresentatives(stalemateRepresentatives);
      counterexampleRepresentatives = canonicalRepresentatives(counterexampleRepresentatives);
    }
  }

  private record BlackMoveSummary(int legalMoveCount, boolean hasMaterialPreservingMove) {
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
