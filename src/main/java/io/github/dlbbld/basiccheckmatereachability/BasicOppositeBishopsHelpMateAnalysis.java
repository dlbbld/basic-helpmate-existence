package io.github.dlbbld.basiccheckmatereachability;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.common.Nulls;

final class BasicOppositeBishopsHelpMateAnalysis {

  private static final int WHITE_TO_MOVE = 0;
  private static final int BLACK_TO_MOVE = 1;
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

  private static final int[][] BISHOP_DELTAS = {
      {-1, -1},
      {-1, 1},
      {1, -1},
      {1, 1}
  };

  private BasicOppositeBishopsHelpMateAnalysis() {
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
      for (var whiteLightBishop = 0; whiteLightBishop < SQUARE_COUNT; whiteLightBishop++) {
        if (!isLightSquare(whiteLightBishop) || whiteLightBishop == whiteKing) {
          continue;
        }
        for (var whiteDarkBishop = 0; whiteDarkBishop < SQUARE_COUNT; whiteDarkBishop++) {
          if (isLightSquare(whiteDarkBishop) || whiteDarkBishop == whiteKing) {
            continue;
          }
          for (var blackKing = 0; blackKing < SQUARE_COUNT; blackKing++) {
            if (blackKing == whiteKing || blackKing == whiteLightBishop || blackKing == whiteDarkBishop) {
              continue;
            }
            for (var havingMove = WHITE_TO_MOVE; havingMove <= BLACK_TO_MOVE; havingMove++) {
              final var state = encode(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing, havingMove);
              if (!isLegalState(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing, havingMove)) {
                continue;
              }
              legalStates.set(state);
              legalStateCount++;
              if (havingMove == WHITE_TO_MOVE) {
                whiteToMoveStateCount++;
                continue;
              }

              blackToMoveStateCount++;
              final var blackInCheck = isBlackInCheck(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing);
              if (blackInCheck) {
                blackToMoveInCheckStateCount++;
                if (blackMoveMask(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing) == 0) {
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
    var forcedPieceCaptureStateCount = 0;
    var stalemateStateCount = 0;
    var counterexampleStateCount = 0;
    final NavigableSet<OppositeBishopsState> unwinnableWhiteToMoveRepresentatives = new TreeSet<>();
    final NavigableSet<OppositeBishopsState> forcedPieceCaptureRepresentatives = new TreeSet<>();
    final NavigableSet<OppositeBishopsState> stalemateRepresentatives = new TreeSet<>();
    final NavigableSet<OppositeBishopsState> counterexampleRepresentatives = new TreeSet<>();

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
      final var whiteLightBishop = whiteLightBishop(state);
      final var whiteDarkBishop = whiteDarkBishop(state);
      final var blackKing = blackKing(state);
      final var blackMoveMask = blackMoveMask(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing);
      final var hasLegalMove = (blackMoveMask & HAS_LEGAL_BLACK_MOVE) != 0;
      final var hasNonCaptureMove = (blackMoveMask & HAS_NON_CAPTURE_BLACK_MOVE) != 0;

      if (!hasLegalMove && !isBlackInCheck(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing)) {
        stalemateStateCount++;
        stalemateRepresentatives.add(canonical(toState(state)));
      } else if (hasLegalMove && !hasNonCaptureMove) {
        forcedPieceCaptureStateCount++;
        forcedPieceCaptureRepresentatives.add(canonical(toState(state)));
      } else if (hasNonCaptureMove) {
        counterexampleStateCount++;
        counterexampleRepresentatives.add(canonical(toState(state)));
      }
    }

    return new AnalysisResult(legalStateCount, whiteToMoveStateCount, blackToMoveStateCount, blackCheckmateCount,
        blackToMoveInCheckStateCount, blackToMoveStateCount - blackToMoveInCheckStateCount,
        unwinnableWhiteToMoveStateCount, unwinnableWhiteToMoveRepresentatives, winning.cardinality(),
        blackToMoveStateCount - blackCheckmateCount - stalemateStateCount, forcedPieceCaptureStateCount,
        forcedPieceCaptureRepresentatives, stalemateStateCount, stalemateRepresentatives, counterexampleStateCount,
        counterexampleRepresentatives);
  }

  static String format(AnalysisResult result) {
    final StringBuilder sb = new StringBuilder();
    sb.append("KBBvK(opposite bishops) states").append('\n');
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
    sb.append("forced bishop-capture states: ").append(result.forcedPieceCaptureStateCount()).append(", canonical ")
        .append(result.forcedPieceCaptureRepresentatives().size()).append('\n');
    appendStates(sb, "forced bishop-capture representatives", result.forcedPieceCaptureRepresentatives());
    sb.append("stalemate states: ").append(result.stalemateStateCount()).append(", canonical ")
        .append(result.stalemateRepresentatives().size()).append('\n');
    appendStates(sb, "stalemate representatives", result.stalemateRepresentatives());
    sb.append("counterexample states: ").append(result.counterexampleStateCount()).append(", canonical ")
        .append(result.counterexampleRepresentatives().size()).append('\n');
    appendStates(sb, "counterexample representatives", result.counterexampleRepresentatives());
    return sb.toString();
  }

  private static void appendStates(StringBuilder sb, String title, Set<OppositeBishopsState> states) {
    sb.append(title).append(':').append('\n');
    for (final OppositeBishopsState state : states) {
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
    final var whiteLightBishop = whiteLightBishop(state);
    final var whiteDarkBishop = whiteDarkBishop(state);
    final var blackKing = blackKing(state);

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(whiteKing, delta);
      if (origin != -1 && origin != whiteLightBishop && origin != whiteDarkBishop && origin != blackKing) {
        addPredecessor(legalStates, winning, queue, origin, whiteLightBishop, whiteDarkBishop, blackKing,
            WHITE_TO_MOVE);
      }
    }

    addBishopPredecessors(legalStates, winning, queue, whiteKing, whiteLightBishop, whiteDarkBishop, blackKing, true);
    addBishopPredecessors(legalStates, winning, queue, whiteKing, whiteLightBishop, whiteDarkBishop, blackKing, false);
  }

  private static void addBishopPredecessors(BitSet legalStates, BitSet winning, IntQueue queue, int whiteKing,
      int whiteLightBishop, int whiteDarkBishop, int blackKing, boolean moveLightBishop) {
    final var destination = moveLightBishop ? whiteLightBishop : whiteDarkBishop;
    final var otherBishop = moveLightBishop ? whiteDarkBishop : whiteLightBishop;

    for (final int[] delta : BISHOP_DELTAS) {
      var origin = offset(destination, delta);
      while (origin != -1) {
        if (origin == whiteKing || origin == otherBishop || origin == blackKing) {
          break;
        }
        if (moveLightBishop) {
          addPredecessor(legalStates, winning, queue, whiteKing, origin, whiteDarkBishop, blackKing, WHITE_TO_MOVE);
        } else {
          addPredecessor(legalStates, winning, queue, whiteKing, whiteLightBishop, origin, blackKing, WHITE_TO_MOVE);
        }
        origin = offset(origin, delta);
      }
    }
  }

  private static void addBlackPredecessors(BitSet legalStates, BitSet winning, IntQueue queue, int state) {
    final var whiteKing = whiteKing(state);
    final var whiteLightBishop = whiteLightBishop(state);
    final var whiteDarkBishop = whiteDarkBishop(state);
    final var blackKing = blackKing(state);

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(blackKing, delta);
      if (origin != -1 && origin != whiteKing && origin != whiteLightBishop && origin != whiteDarkBishop) {
        addPredecessor(legalStates, winning, queue, whiteKing, whiteLightBishop, whiteDarkBishop, origin,
            BLACK_TO_MOVE);
      }
    }
  }

  private static void addPredecessor(BitSet legalStates, BitSet winning, IntQueue queue, int whiteKing,
      int whiteLightBishop, int whiteDarkBishop, int blackKing, int havingMove) {
    final var predecessor = encode(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing, havingMove);
    if (legalStates.get(predecessor) && !winning.get(predecessor)) {
      winning.set(predecessor);
      queue.add(predecessor);
    }
  }

  private static int blackMoveMask(int whiteKing, int whiteLightBishop, int whiteDarkBishop, int blackKing) {
    var result = 0;
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing) {
        continue;
      }
      final var capturesLightBishop = target == whiteLightBishop;
      final var capturesDarkBishop = target == whiteDarkBishop;
      final var lightBishopAfterMove = capturesLightBishop ? -1 : whiteLightBishop;
      final var darkBishopAfterMove = capturesDarkBishop ? -1 : whiteDarkBishop;
      if (!isBlackInCheck(whiteKing, lightBishopAfterMove, darkBishopAfterMove, target)) {
        result |= HAS_LEGAL_BLACK_MOVE;
        if (!capturesLightBishop && !capturesDarkBishop) {
          result |= HAS_NON_CAPTURE_BLACK_MOVE;
        }
      }
    }
    return result;
  }

  private static boolean isLegalState(int whiteKing, int whiteLightBishop, int whiteDarkBishop, int blackKing,
      int havingMove) {
    return switch (havingMove) {
      case WHITE_TO_MOVE -> !isBlackInCheck(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing);
      case BLACK_TO_MOVE -> !areKingsAdjacent(whiteKing, blackKing);
      default -> throw new IllegalArgumentException();
    };
  }

  private static boolean isBlackInCheck(int whiteKing, int whiteLightBishop, int whiteDarkBishop, int blackKing) {
    return areKingsAdjacent(whiteKing, blackKing)
        || isBishopAttack(whiteLightBishop, whiteDarkBishop, whiteKing, blackKing)
        || isBishopAttack(whiteDarkBishop, whiteLightBishop, whiteKing, blackKing);
  }

  private static boolean areKingsAdjacent(int whiteKing, int blackKing) {
    return Math.abs(file(whiteKing) - file(blackKing)) <= 1 && Math.abs(rank(whiteKing) - rank(blackKing)) <= 1;
  }

  private static boolean isBishopAttack(int bishop, int otherBishop, int whiteKing, int blackKing) {
    if (bishop == -1) {
      return false;
    }
    final var fileDistance = file(blackKing) - file(bishop);
    final var rankDistance = rank(blackKing) - rank(bishop);
    if (Math.abs(fileDistance) != Math.abs(rankDistance)) {
      return false;
    }
    final var fileStep = Integer.signum(fileDistance);
    final var rankStep = Integer.signum(rankDistance);
    var file = file(bishop) + fileStep;
    var rank = rank(bishop) + rankStep;
    while (true) {
      final var square = square(file, rank);
      if (square == blackKing) {
        return true;
      }
      if (square == whiteKing || square == otherBishop) {
        return false;
      }
      file += fileStep;
      rank += rankStep;
    }
  }

  private static int encode(int whiteKing, int whiteLightBishop, int whiteDarkBishop, int blackKing, int havingMove) {
    return (((whiteKing * SQUARE_COUNT + whiteLightBishop) * SQUARE_COUNT + whiteDarkBishop) * SQUARE_COUNT
        + blackKing) * 2 + havingMove;
  }

  private static int whiteKing(int state) {
    return state >>> 19;
  }

  private static int whiteLightBishop(int state) {
    return state >>> 13 & 0x3F;
  }

  private static int whiteDarkBishop(int state) {
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

  private static OppositeBishopsState toState(int state) {
    return new OppositeBishopsState(square(whiteKing(state)), square(whiteLightBishop(state)),
        square(whiteDarkBishop(state)), square(blackKing(state)), side(havingMove(state)));
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

  private static NavigableSet<OppositeBishopsState> canonicalRepresentatives(Collection<OppositeBishopsState> states) {
    final NavigableSet<OppositeBishopsState> result = new TreeSet<>();
    for (final OppositeBishopsState state : states) {
      result.add(canonical(state));
    }
    return Collections.unmodifiableNavigableSet(result);
  }

  private static OppositeBishopsState canonical(OppositeBishopsState state) {
    OppositeBishopsState result = state;
    for (var transformIndex = 1; transformIndex < 8; transformIndex++) {
      final OppositeBishopsState transformed = transform(state, transformIndex);
      if (transformed.compareTo(result) < 0) {
        result = transformed;
      }
    }
    return result;
  }

  private static OppositeBishopsState transform(OppositeBishopsState state, int transformIndex) {
    final var transformedLightBishop = transform(state.whiteLightBishop(), transformIndex);
    final var transformedDarkBishop = transform(state.whiteDarkBishop(), transformIndex);
    final Square whiteLightBishop;
    final Square whiteDarkBishop;
    if (isLightSquare(transformedLightBishop.ordinal())) {
      whiteLightBishop = transformedLightBishop;
      whiteDarkBishop = transformedDarkBishop;
    } else {
      whiteLightBishop = transformedDarkBishop;
      whiteDarkBishop = transformedLightBishop;
    }
    return new OppositeBishopsState(transform(state.whiteKing(), transformIndex), whiteLightBishop, whiteDarkBishop,
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
    return Nulls.get(Square.REAL, transformedRank * 8 + transformedFile);
  }

  private static String toFen(OppositeBishopsState state) {
    final char[] board = new char[64];
    for (int i = 0; i < board.length; i++) {
      board[i] = '1';
    }
    board[state.whiteKing().ordinal()] = 'K';
    board[state.whiteLightBishop().ordinal()] = 'B';
    board[state.whiteDarkBishop().ordinal()] = 'B';
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

  private static String formatBlackMoves(OppositeBishopsState state) {
    if (state.havingMove() != Side.BLACK) {
      return "";
    }

    final var whiteKing = state.whiteKing().ordinal();
    final var whiteLightBishop = state.whiteLightBishop().ordinal();
    final var whiteDarkBishop = state.whiteDarkBishop().ordinal();
    final var blackKing = state.blackKing().ordinal();
    final StringBuilder sb = new StringBuilder();

    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing) {
        continue;
      }
      final var capturesLightBishop = target == whiteLightBishop;
      final var capturesDarkBishop = target == whiteDarkBishop;
      final var lightBishopAfterMove = capturesLightBishop ? -1 : whiteLightBishop;
      final var darkBishopAfterMove = capturesDarkBishop ? -1 : whiteDarkBishop;
      if (isBlackInCheck(whiteKing, lightBishopAfterMove, darkBishopAfterMove, target)) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(state.blackKing().getName()).append(square(target).getName());
      if (capturesLightBishop || capturesDarkBishop) {
        sb.append("xB");
      }
    }
    return sb.toString();
  }

  record OppositeBishopsState(Square whiteKing, Square whiteLightBishop, Square whiteDarkBishop, Square blackKing,
      Side havingMove) implements Comparable<OppositeBishopsState> {

    @Override
    public int compareTo(OppositeBishopsState other) {
      if (whiteKing != other.whiteKing) {
        return whiteKing.compareTo(other.whiteKing);
      }
      if (whiteLightBishop != other.whiteLightBishop) {
        return whiteLightBishop.compareTo(other.whiteLightBishop);
      }
      if (whiteDarkBishop != other.whiteDarkBishop) {
        return whiteDarkBishop.compareTo(other.whiteDarkBishop);
      }
      if (blackKing != other.blackKing) {
        return blackKing.compareTo(other.blackKing);
      }
      return havingMove.compareTo(other.havingMove);
    }
  }

  record AnalysisResult(int legalStateCount, int whiteToMoveStateCount, int blackToMoveStateCount,
      int blackCheckmateCount, int blackToMoveInCheckStateCount, int blackToMoveNotInCheckStateCount,
      int unwinnableWhiteToMoveStateCount, Set<OppositeBishopsState> unwinnableWhiteToMoveRepresentatives,
      int winningStateCount, int ongoingBlackToMoveStateCount, int forcedPieceCaptureStateCount,
      Set<OppositeBishopsState> forcedPieceCaptureRepresentatives, int stalemateStateCount,
      Set<OppositeBishopsState> stalemateRepresentatives, int counterexampleStateCount,
      Set<OppositeBishopsState> counterexampleRepresentatives) {

    AnalysisResult {
      unwinnableWhiteToMoveRepresentatives = canonicalRepresentatives(unwinnableWhiteToMoveRepresentatives);
      forcedPieceCaptureRepresentatives = canonicalRepresentatives(forcedPieceCaptureRepresentatives);
      stalemateRepresentatives = canonicalRepresentatives(stalemateRepresentatives);
      counterexampleRepresentatives = canonicalRepresentatives(counterexampleRepresentatives);
    }
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
