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

final class BasicRookKnightHelpmateAnalysis {

  private static final int WHITE_TO_MOVE = 0;
  private static final int BLACK_TO_MOVE = 1;
  private static final int SQUARE_COUNT = 64;
  private static final int STATE_COUNT = SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * 2;
  private static final int HAS_LEGAL_BLACK_MOVE = 1;
  private static final int HAS_MATERIAL_PRESERVING_BLACK_MOVE = 2;

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

  private static final int[][] ROOK_DELTAS = {
      {-1, 0},
      {0, -1},
      {0, 1},
      {1, 0}
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

  private BasicRookKnightHelpmateAnalysis() {
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
      for (var whiteRook = 0; whiteRook < SQUARE_COUNT; whiteRook++) {
        if (whiteRook == whiteKing) {
          continue;
        }
        for (var blackKing = 0; blackKing < SQUARE_COUNT; blackKing++) {
          if (blackKing == whiteKing || blackKing == whiteRook) {
            continue;
          }
          for (var blackKnight = 0; blackKnight < SQUARE_COUNT; blackKnight++) {
            if (blackKnight == whiteKing || blackKnight == whiteRook || blackKnight == blackKing) {
              continue;
            }
            for (var havingMove = WHITE_TO_MOVE; havingMove <= BLACK_TO_MOVE; havingMove++) {
              final var state = encode(whiteKing, whiteRook, blackKing, blackKnight, havingMove);
              if (!isLegalState(whiteKing, whiteRook, blackKing, blackKnight, havingMove)) {
                continue;
              }
              legalStates.set(state);
              legalStateCount++;
              if (havingMove == WHITE_TO_MOVE) {
                whiteToMoveStateCount++;
                continue;
              }

              blackToMoveStateCount++;
              final var blackInCheck = isBlackInCheck(whiteKing, whiteRook, blackKing, blackKnight);
              if (blackInCheck) {
                blackToMoveInCheckStateCount++;
                if (blackMoveMask(whiteKing, whiteRook, blackKing, blackKnight) == 0) {
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

    var endedWhiteToMoveStateCount = 0;
    var reducibleWhiteToMoveStateCount = 0;
    var unwinnableWhiteToMoveStateCount = 0;
    var forcedRookCaptureStateCount = 0;
    var stalemateStateCount = 0;
    var counterexampleStateCount = 0;
    final NavigableSet<RookKnightState> unwinnableWhiteToMoveRepresentatives = new TreeSet<>();
    final NavigableSet<RookKnightState> reducibleWhiteToMoveStates = new TreeSet<>();
    final NavigableSet<RookKnightState> forcedRookCaptureRepresentatives = new TreeSet<>();
    final NavigableSet<RookKnightState> stalemateRepresentatives = new TreeSet<>();
    final NavigableSet<RookKnightState> counterexampleRepresentatives = new TreeSet<>();

    for (var state = legalStates.nextSetBit(0); state >= 0; state = legalStates.nextSetBit(state + 1)) {
      if (winning.get(state)) {
        continue;
      }
      if (havingMove(state) == WHITE_TO_MOVE) {
        final var whiteKing = whiteKing(state);
        final var whiteRook = whiteRook(state);
        final var blackKing = blackKing(state);
        final var blackKnight = blackKnight(state);
        if (!hasLegalWhiteMove(whiteKing, whiteRook, blackKing, blackKnight)) {
          endedWhiteToMoveStateCount++;
        } else if (hasWhiteCaptureToWinningRookEndgame(whiteKing, whiteRook, blackKing, blackKnight)) {
          reducibleWhiteToMoveStateCount++;
          reducibleWhiteToMoveStates.add(toState(state));
        } else {
          unwinnableWhiteToMoveStateCount++;
          unwinnableWhiteToMoveRepresentatives.add(canonical(toState(state)));
        }
        continue;
      }

      final var whiteKing = whiteKing(state);
      final var whiteRook = whiteRook(state);
      final var blackKing = blackKing(state);
      final var blackKnight = blackKnight(state);
      final var blackMoveMask = blackMoveMask(whiteKing, whiteRook, blackKing, blackKnight);
      final var hasLegalMove = (blackMoveMask & HAS_LEGAL_BLACK_MOVE) != 0;
      final var hasMaterialPreservingMove = (blackMoveMask & HAS_MATERIAL_PRESERVING_BLACK_MOVE) != 0;

      if (!hasLegalMove && !isBlackInCheck(whiteKing, whiteRook, blackKing, blackKnight)) {
        stalemateStateCount++;
        stalemateRepresentatives.add(canonical(toState(state)));
      } else if (hasLegalMove && !hasMaterialPreservingMove) {
        forcedRookCaptureStateCount++;
        forcedRookCaptureRepresentatives.add(canonical(toState(state)));
      } else if (hasMaterialPreservingMove) {
        counterexampleStateCount++;
        counterexampleRepresentatives.add(canonical(toState(state)));
      }
    }

    return new AnalysisResult(legalStateCount, whiteToMoveStateCount, blackToMoveStateCount, blackCheckmateCount,
        blackToMoveInCheckStateCount, blackToMoveStateCount - blackToMoveInCheckStateCount,
        endedWhiteToMoveStateCount, reducibleWhiteToMoveStateCount, unwinnableWhiteToMoveStateCount,
        reducibleWhiteToMoveStates, unwinnableWhiteToMoveRepresentatives, winning.cardinality(),
        blackToMoveStateCount - blackCheckmateCount - stalemateStateCount, forcedRookCaptureStateCount,
        forcedRookCaptureRepresentatives, stalemateStateCount, stalemateRepresentatives, counterexampleStateCount,
        counterexampleRepresentatives);
  }

  static String format(AnalysisResult result) {
    final StringBuilder sb = new StringBuilder();
    sb.append("KRvKN states").append('\n');
    sb.append("legal states: ").append(result.legalStateCount()).append('\n');
    sb.append("white-to-move states: ").append(result.whiteToMoveStateCount()).append('\n');
    sb.append("black-to-move states: ").append(result.blackToMoveStateCount()).append('\n');
    sb.append("black-to-move in-check states: ").append(result.blackToMoveInCheckStateCount()).append('\n');
    sb.append("black-to-move not-in-check states: ").append(result.blackToMoveNotInCheckStateCount()).append('\n');
    sb.append("black checkmates: ").append(result.blackCheckmateCount()).append('\n');
    sb.append("ended white-to-move states: ").append(result.endedWhiteToMoveStateCount()).append('\n');
    sb.append("white-to-move states reducible by capturing the knight: ")
        .append(result.reducibleWhiteToMoveStateCount()).append('\n');
    sb.append("unwinnable white-to-move states: ").append(result.unwinnableWhiteToMoveStateCount())
        .append(", canonical ").append(result.unwinnableWhiteToMoveRepresentatives().size()).append('\n');
    appendStates(sb, "unwinnable white-to-move representatives", result.unwinnableWhiteToMoveRepresentatives());
    sb.append("ongoing black-to-move states: ").append(result.ongoingBlackToMoveStateCount()).append('\n');
    sb.append("cooperatively winnable states: ").append(result.winningStateCount()).append('\n');
    sb.append("forced rook-capture states: ").append(result.forcedRookCaptureStateCount()).append(", canonical ")
        .append(result.forcedRookCaptureRepresentatives().size()).append('\n');
    appendStates(sb, "forced rook-capture representatives", result.forcedRookCaptureRepresentatives());
    sb.append("stalemate states: ").append(result.stalemateStateCount()).append(", canonical ")
        .append(result.stalemateRepresentatives().size()).append('\n');
    appendStates(sb, "stalemate representatives", result.stalemateRepresentatives());
    sb.append("counterexample states: ").append(result.counterexampleStateCount()).append(", canonical ")
        .append(result.counterexampleRepresentatives().size()).append('\n');
    appendStates(sb, "counterexample representatives", result.counterexampleRepresentatives());
    return sb.toString();
  }

  private static void appendStates(StringBuilder sb, String title, Set<RookKnightState> states) {
    sb.append(title).append(':').append('\n');
    for (final RookKnightState state : states) {
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
    final var whiteRook = whiteRook(state);
    final var blackKing = blackKing(state);
    final var blackKnight = blackKnight(state);

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(whiteKing, delta);
      if (origin != -1 && origin != whiteRook && origin != blackKing && origin != blackKnight) {
        addPredecessor(legalStates, winning, queue, origin, whiteRook, blackKing, blackKnight, WHITE_TO_MOVE);
      }
    }

    for (final int[] delta : ROOK_DELTAS) {
      var origin = offset(whiteRook, delta);
      while (origin != -1) {
        if (origin == whiteKing || origin == blackKing || origin == blackKnight) {
          break;
        }
        addPredecessor(legalStates, winning, queue, whiteKing, origin, blackKing, blackKnight, WHITE_TO_MOVE);
        origin = offset(origin, delta);
      }
    }
  }

  private static void addBlackPredecessors(BitSet legalStates, BitSet winning, IntQueue queue, int state) {
    final var whiteKing = whiteKing(state);
    final var whiteRook = whiteRook(state);
    final var blackKing = blackKing(state);
    final var blackKnight = blackKnight(state);

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(blackKing, delta);
      if (origin != -1 && origin != whiteKing && origin != whiteRook && origin != blackKnight) {
        addPredecessor(legalStates, winning, queue, whiteKing, whiteRook, origin, blackKnight, BLACK_TO_MOVE);
      }
    }

    for (final int[] delta : KNIGHT_DELTAS) {
      final var origin = offset(blackKnight, delta);
      if (origin != -1 && origin != whiteKing && origin != whiteRook && origin != blackKing) {
        addPredecessor(legalStates, winning, queue, whiteKing, whiteRook, blackKing, origin, BLACK_TO_MOVE);
      }
    }
  }

  private static void addPredecessor(BitSet legalStates, BitSet winning, IntQueue queue, int whiteKing, int whiteRook,
      int blackKing, int blackKnight, int havingMove) {
    final var predecessor = encode(whiteKing, whiteRook, blackKing, blackKnight, havingMove);
    if (legalStates.get(predecessor) && !winning.get(predecessor)) {
      winning.set(predecessor);
      queue.add(predecessor);
    }
  }

  private static int blackMoveMask(int whiteKing, int whiteRook, int blackKing, int blackKnight) {
    var result = 0;
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing || target == blackKnight) {
        continue;
      }
      final var capturesRook = target == whiteRook;
      final var rookAfterMove = capturesRook ? -1 : whiteRook;
      if (!isBlackInCheck(whiteKing, rookAfterMove, target, blackKnight)) {
        result |= HAS_LEGAL_BLACK_MOVE;
        if (!capturesRook) {
          result |= HAS_MATERIAL_PRESERVING_BLACK_MOVE;
        }
      }
    }

    for (final int[] delta : KNIGHT_DELTAS) {
      final var target = offset(blackKnight, delta);
      if (target == -1 || target == whiteKing || target == blackKing) {
        continue;
      }
      final var capturesRook = target == whiteRook;
      final var rookAfterMove = capturesRook ? -1 : whiteRook;
      if (!isBlackInCheck(whiteKing, rookAfterMove, blackKing, target)) {
        result |= HAS_LEGAL_BLACK_MOVE;
        if (!capturesRook) {
          result |= HAS_MATERIAL_PRESERVING_BLACK_MOVE;
        }
      }
    }
    return result;
  }

  private static boolean hasWhiteCaptureToWinningRookEndgame(int whiteKing, int whiteRook, int blackKing,
      int blackKnight) {
    if (areKingsAdjacent(blackKnight, whiteKing) && isWhiteMoveLegal(blackKnight, whiteRook, blackKing, -1)) {
      return isRookEndgameKnownWinningForWhite(blackKnight, whiteRook, blackKing);
    }

    for (final int[] delta : ROOK_DELTAS) {
      var target = offset(whiteRook, delta);
      while (target != -1) {
        if (target == whiteKing || target == blackKing) {
          break;
        }
        if (target == blackKnight) {
          return isWhiteMoveLegal(whiteKing, target, blackKing, -1)
              && isRookEndgameKnownWinningForWhite(whiteKing, target, blackKing);
        }
        target = offset(target, delta);
      }
    }
    return false;
  }

  private static boolean hasLegalWhiteMove(int whiteKing, int whiteRook, int blackKing, int blackKnight) {
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(whiteKing, delta);
      if (target == -1 || target == whiteRook || target == blackKing) {
        continue;
      }
      final var knightAfterMove = target == blackKnight ? -1 : blackKnight;
      if (isWhiteMoveLegal(target, whiteRook, blackKing, knightAfterMove)) {
        return true;
      }
    }

    for (final int[] delta : ROOK_DELTAS) {
      var target = offset(whiteRook, delta);
      while (target != -1) {
        if (target == whiteKing || target == blackKing) {
          break;
        }
        final var knightAfterMove = target == blackKnight ? -1 : blackKnight;
        if (isWhiteMoveLegal(whiteKing, target, blackKing, knightAfterMove)) {
          return true;
        }
        if (target == blackKnight) {
          break;
        }
        target = offset(target, delta);
      }
    }
    return false;
  }

  private static boolean isWhiteMoveLegal(int whiteKing, int whiteRook, int blackKing, int blackKnight) {
    return !areKingsAdjacent(whiteKing, blackKing)
        && (blackKnight == -1 || !isKnightAttack(blackKnight, whiteKing));
  }

  private static boolean isRookEndgameKnownWinningForWhite(int whiteKing, int whiteRook, int blackKing) {
    if (areKingsAdjacent(whiteKing, blackKing)) {
      return false;
    }
    final var blackMoveMask = rookEndgameBlackMoveMask(whiteKing, whiteRook, blackKing);
    return isRookEndgameBlackInCheck(whiteKing, whiteRook, blackKing) && blackMoveMask == 0
        || (blackMoveMask & HAS_MATERIAL_PRESERVING_BLACK_MOVE) != 0;
  }

  private static int rookEndgameBlackMoveMask(int whiteKing, int whiteRook, int blackKing) {
    var result = 0;
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing) {
        continue;
      }
      final var capturesRook = target == whiteRook;
      final var rookAfterMove = capturesRook ? -1 : whiteRook;
      if (!isRookEndgameBlackInCheck(whiteKing, rookAfterMove, target)) {
        result |= HAS_LEGAL_BLACK_MOVE;
        if (!capturesRook) {
          result |= HAS_MATERIAL_PRESERVING_BLACK_MOVE;
        }
      }
    }
    return result;
  }

  private static boolean isRookEndgameBlackInCheck(int whiteKing, int whiteRook, int blackKing) {
    return areKingsAdjacent(whiteKing, blackKing)
        || isRookAttack(whiteRook, whiteKing, -1, blackKing);
  }

  private static boolean isLegalState(int whiteKing, int whiteRook, int blackKing, int blackKnight, int havingMove) {
    return switch (havingMove) {
      case WHITE_TO_MOVE -> !isBlackInCheck(whiteKing, whiteRook, blackKing, blackKnight);
      case BLACK_TO_MOVE -> !isWhiteInCheck(whiteKing, whiteRook, blackKing, blackKnight);
      default -> throw new IllegalArgumentException();
    };
  }

  private static boolean isBlackInCheck(int whiteKing, int whiteRook, int blackKing, int blackKnight) {
    return areKingsAdjacent(whiteKing, blackKing) || isRookAttack(whiteRook, whiteKing, blackKnight, blackKing);
  }

  private static boolean isWhiteInCheck(int whiteKing, int whiteRook, int blackKing, int blackKnight) {
    return areKingsAdjacent(whiteKing, blackKing) || isKnightAttack(blackKnight, whiteKing);
  }

  private static boolean areKingsAdjacent(int whiteKing, int blackKing) {
    return Math.abs(file(whiteKing) - file(blackKing)) <= 1 && Math.abs(rank(whiteKing) - rank(blackKing)) <= 1;
  }

  private static boolean isRookAttack(int whiteRook, int whiteKing, int blackKnight, int blackKing) {
    if (whiteRook == -1) {
      return false;
    }
    if (file(whiteRook) != file(blackKing) && rank(whiteRook) != rank(blackKing)) {
      return false;
    }
    final var fileStep = Integer.signum(file(blackKing) - file(whiteRook));
    final var rankStep = Integer.signum(rank(blackKing) - rank(whiteRook));
    var file = file(whiteRook) + fileStep;
    var rank = rank(whiteRook) + rankStep;
    while (true) {
      final var square = square(file, rank);
      if (square == blackKing) {
        return true;
      }
      if (square == whiteKing || square == blackKnight) {
        return false;
      }
      file += fileStep;
      rank += rankStep;
    }
  }

  private static boolean isKnightAttack(int blackKnight, int whiteKing) {
    if (blackKnight == -1) {
      return false;
    }
    for (final int[] delta : KNIGHT_DELTAS) {
      if (offset(blackKnight, delta) == whiteKing) {
        return true;
      }
    }
    return false;
  }

  private static int encode(int whiteKing, int whiteRook, int blackKing, int blackKnight, int havingMove) {
    return (((whiteKing * SQUARE_COUNT + whiteRook) * SQUARE_COUNT + blackKing) * SQUARE_COUNT + blackKnight) * 2
        + havingMove;
  }

  private static int whiteKing(int state) {
    return state >>> 19;
  }

  private static int whiteRook(int state) {
    return state >>> 13 & 0x3F;
  }

  private static int blackKing(int state) {
    return state >>> 7 & 0x3F;
  }

  private static int blackKnight(int state) {
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

  private static RookKnightState toState(int state) {
    return new RookKnightState(square(whiteKing(state)), square(whiteRook(state)), square(blackKing(state)),
        square(blackKnight(state)), side(havingMove(state)));
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

  private static NavigableSet<RookKnightState> canonicalRepresentatives(
      Collection<RookKnightState> states) {
    final NavigableSet<RookKnightState> result = new TreeSet<>();
    for (final RookKnightState state : states) {
      result.add(canonical(state));
    }
    return Collections.unmodifiableNavigableSet(result);
  }

  private static RookKnightState canonical(RookKnightState state) {
    RookKnightState result = state;
    for (var transformIndex = 1; transformIndex < 8; transformIndex++) {
      final RookKnightState transformed = transform(state, transformIndex);
      if (transformed.compareTo(result) < 0) {
        result = transformed;
      }
    }
    return result;
  }

  private static RookKnightState transform(RookKnightState state, int transformIndex) {
    return new RookKnightState(transform(state.whiteKing(), transformIndex),
        transform(state.whiteRook(), transformIndex), transform(state.blackKing(), transformIndex),
        transform(state.blackKnight(), transformIndex), state.havingMove());
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

  private static String toFen(RookKnightState state) {
    final char[] board = new char[64];
    for (int i = 0; i < board.length; i++) {
      board[i] = '1';
    }
    board[state.whiteKing().ordinal()] = 'K';
    board[state.whiteRook().ordinal()] = 'R';
    board[state.blackKing().ordinal()] = 'k';
    board[state.blackKnight().ordinal()] = 'n';

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

  private static String formatBlackMoves(RookKnightState state) {
    if (state.havingMove() != Side.BLACK) {
      return "";
    }

    final var whiteKing = state.whiteKing().ordinal();
    final var whiteRook = state.whiteRook().ordinal();
    final var blackKing = state.blackKing().ordinal();
    final var blackKnight = state.blackKnight().ordinal();
    final StringBuilder sb = new StringBuilder();

    appendLegalBlackKingMoves(sb, whiteKing, whiteRook, blackKing, blackKnight);
    appendLegalBlackKnightMoves(sb, whiteKing, whiteRook, blackKing, blackKnight);
    return sb.toString();
  }

  private static void appendLegalBlackKingMoves(StringBuilder sb, int whiteKing, int whiteRook, int blackKing,
      int blackKnight) {
    for (final int[] delta : KING_DELTAS) {
      final var target = offset(blackKing, delta);
      if (target == -1 || target == whiteKing || target == blackKnight) {
        continue;
      }
      final var capturesRook = target == whiteRook;
      final var rookAfterMove = capturesRook ? -1 : whiteRook;
      if (isBlackInCheck(whiteKing, rookAfterMove, target, blackKnight)) {
        continue;
      }
      appendMoveSeparator(sb);
      sb.append(square(blackKing).getName()).append(square(target).getName());
      if (capturesRook) {
        sb.append("xR");
      }
    }
  }

  private static void appendLegalBlackKnightMoves(StringBuilder sb, int whiteKing, int whiteRook, int blackKing,
      int blackKnight) {
    for (final int[] delta : KNIGHT_DELTAS) {
      final var target = offset(blackKnight, delta);
      if (target == -1 || target == whiteKing || target == blackKing) {
        continue;
      }
      final var capturesRook = target == whiteRook;
      final var rookAfterMove = capturesRook ? -1 : whiteRook;
      if (!isBlackInCheck(whiteKing, rookAfterMove, blackKing, target)) {
        appendMoveSeparator(sb);
        sb.append(square(blackKnight).getName()).append(square(target).getName());
        if (capturesRook) {
          sb.append("xR");
        }
      }
    }
  }

  private static void appendMoveSeparator(StringBuilder sb) {
    if (sb.length() > 0) {
      sb.append(' ');
    }
  }

  record RookKnightState(Square whiteKing, Square whiteRook, Square blackKing, Square blackKnight,
      Side havingMove) implements Comparable<RookKnightState> {

    @Override
    public int compareTo(RookKnightState other) {
      if (whiteKing != other.whiteKing) {
        return whiteKing.compareTo(other.whiteKing);
      }
      if (whiteRook != other.whiteRook) {
        return whiteRook.compareTo(other.whiteRook);
      }
      if (blackKing != other.blackKing) {
        return blackKing.compareTo(other.blackKing);
      }
      if (blackKnight != other.blackKnight) {
        return blackKnight.compareTo(other.blackKnight);
      }
      return havingMove.compareTo(other.havingMove);
    }
  }

  record AnalysisResult(int legalStateCount, int whiteToMoveStateCount, int blackToMoveStateCount,
      int blackCheckmateCount, int blackToMoveInCheckStateCount, int blackToMoveNotInCheckStateCount,
      int endedWhiteToMoveStateCount, int reducibleWhiteToMoveStateCount, int unwinnableWhiteToMoveStateCount,
      Set<RookKnightState> reducibleWhiteToMoveStates, Set<RookKnightState> unwinnableWhiteToMoveRepresentatives,
      int winningStateCount,
      int ongoingBlackToMoveStateCount, int forcedRookCaptureStateCount,
      Set<RookKnightState> forcedRookCaptureRepresentatives, int stalemateStateCount,
      Set<RookKnightState> stalemateRepresentatives, int counterexampleStateCount,
      Set<RookKnightState> counterexampleRepresentatives) {

    AnalysisResult {
      reducibleWhiteToMoveStates = Collections.unmodifiableSet(new TreeSet<>(reducibleWhiteToMoveStates));
      unwinnableWhiteToMoveRepresentatives = canonicalRepresentatives(unwinnableWhiteToMoveRepresentatives);
      forcedRookCaptureRepresentatives = canonicalRepresentatives(forcedRookCaptureRepresentatives);
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

