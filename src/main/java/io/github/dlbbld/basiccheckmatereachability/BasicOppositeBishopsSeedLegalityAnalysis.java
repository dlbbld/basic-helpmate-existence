package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.ashlarchess.board.enums.Square.C1;
import static io.github.dlbbld.ashlarchess.board.enums.Square.E1;
import static io.github.dlbbld.ashlarchess.board.enums.Square.E8;
import static io.github.dlbbld.ashlarchess.board.enums.Square.F1;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.common.model.MoveSpecification;

final class BasicOppositeBishopsSeedLegalityAnalysis {

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

  private static final int[][] BISHOP_DELTAS = {
      {-1, -1},
      {-1, 1},
      {1, -1},
      {1, 1}
  };

  private BasicOppositeBishopsSeedLegalityAnalysis() {
  }

  static AnalysisResult analyzeFromOriginalSquares() {
    final BitSet legalStates = enumerateLegalStates();
    final var seed = encode(E1.ordinal(), F1.ordinal(), C1.ordinal(), E8.ordinal(), BLACK_TO_MOVE);
    if (!legalStates.get(seed)) {
      throw new IllegalStateException("KBBvK original-square seed is not locally legal");
    }

    final BitSet reachable = calculateReachable(legalStates, seed);
    final BitSet unreachable = (BitSet) legalStates.clone();
    unreachable.andNot(reachable);
    final BitSet unreachableBlackToMoveInCheck = filterHavingMoveAndCheck(unreachable, BLACK_TO_MOVE, true);
    final BitSet noLastWhiteMove = filterNoLastWhiteMove(unreachableBlackToMoveInCheck);

    return new AnalysisResult(legalStates.cardinality(), reachable.cardinality(), unreachable.cardinality(),
        countHavingMove(unreachable, BLACK_TO_MOVE), countHavingMoveAndCheck(unreachable, BLACK_TO_MOVE, true),
        countHavingMoveAndCheck(unreachable, BLACK_TO_MOVE, false), countHavingMove(unreachable, WHITE_TO_MOVE),
        countHavingMoveAndCheck(unreachable, WHITE_TO_MOVE, true), countHavingMoveAndCheck(unreachable, WHITE_TO_MOVE,
            false), canonicalRepresentatives(unreachable), unreachableBlackToMoveInCheck.cardinality(),
        noLastWhiteMove.cardinality(),
        unreachableBlackToMoveInCheck.cardinality() - noLastWhiteMove.cardinality(),
        canonicalRepresentatives(unreachableBlackToMoveInCheck).size(),
        canonicalRepresentatives(noLastWhiteMove).size(),
        canonicalRepresentativesWithout(unreachableBlackToMoveInCheck, noLastWhiteMove).size());
  }

  private static BitSet enumerateLegalStates() {
    final BitSet result = new BitSet(STATE_COUNT);
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
              if (isLegalState(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing, havingMove)) {
                result.set(encode(whiteKing, whiteLightBishop, whiteDarkBishop, blackKing, havingMove));
              }
            }
          }
        }
      }
    }
    return result;
  }

  private static BitSet calculateReachable(BitSet legalStates, int seed) {
    final BitSet reachable = new BitSet(STATE_COUNT);
    final ArrayDeque<Integer> queue = new ArrayDeque<>();
    reachable.set(seed);
    queue.addLast(seed);

    while (!queue.isEmpty()) {
      final var state = queue.removeFirst();
      final var side = havingMove(state) == WHITE_TO_MOVE ? Side.WHITE : Side.BLACK;
      for (final MoveSpecification move : toBitboardPosition(state).legalMoves(side, 0L)) {
        if (capturesWhiteBishop(state, move)) {
          continue;
        }
        final var successor = afterMove(state, move);
        if (legalStates.get(successor) && !reachable.get(successor)) {
          reachable.set(successor);
          queue.addLast(successor);
        }
      }
    }
    return reachable;
  }

  private static int countHavingMove(BitSet states, int havingMove) {
    var result = 0;
    for (var state = states.nextSetBit(0); state >= 0; state = states.nextSetBit(state + 1)) {
      if (havingMove(state) == havingMove) {
        result++;
      }
    }
    return result;
  }

  private static int countHavingMoveAndCheck(BitSet states, int havingMove, boolean inCheck) {
    var result = 0;
    for (var state = states.nextSetBit(0); state >= 0; state = states.nextSetBit(state + 1)) {
      if (havingMove(state) == havingMove && toBitboardPosition(state).isInCheck(side(havingMove)) == inCheck) {
        result++;
      }
    }
    return result;
  }

  private static BitSet filterHavingMoveAndCheck(BitSet states, int havingMove, boolean inCheck) {
    final BitSet result = new BitSet(STATE_COUNT);
    for (var state = states.nextSetBit(0); state >= 0; state = states.nextSetBit(state + 1)) {
      if (havingMove(state) == havingMove && toBitboardPosition(state).isInCheck(side(havingMove)) == inCheck) {
        result.set(state);
      }
    }
    return result;
  }

  private static BitSet filterNoLastWhiteMove(BitSet states) {
    final BitSet result = new BitSet(STATE_COUNT);
    for (var state = states.nextSetBit(0); state >= 0; state = states.nextSetBit(state + 1)) {
      if (!hasLastWhiteMove(state)) {
        result.set(state);
      }
    }
    return result;
  }

  private static boolean hasLastWhiteMove(int state) {
    final var whiteKing = whiteKing(state);
    final var whiteLightBishop = whiteLightBishop(state);
    final var whiteDarkBishop = whiteDarkBishop(state);
    final var blackKing = blackKing(state);

    for (final int[] delta : KING_DELTAS) {
      final var origin = offset(whiteKing, delta);
      if (origin != -1 && origin != whiteLightBishop && origin != whiteDarkBishop && origin != blackKing
          && isLegalState(origin, whiteLightBishop, whiteDarkBishop, blackKing, WHITE_TO_MOVE)) {
        return true;
      }
    }

    for (final int[] delta : BISHOP_DELTAS) {
      var origin = offset(whiteLightBishop, delta);
      while (origin != -1) {
        if (origin == whiteKing || origin == whiteDarkBishop || origin == blackKing) {
          break;
        }
        if (isLegalState(whiteKing, origin, whiteDarkBishop, blackKing, WHITE_TO_MOVE)) {
          return true;
        }
        origin = offset(origin, delta);
      }
    }

    for (final int[] delta : BISHOP_DELTAS) {
      var origin = offset(whiteDarkBishop, delta);
      while (origin != -1) {
        if (origin == whiteKing || origin == whiteLightBishop || origin == blackKing) {
          break;
        }
        if (isLegalState(whiteKing, whiteLightBishop, origin, blackKing, WHITE_TO_MOVE)) {
          return true;
        }
        origin = offset(origin, delta);
      }
    }

    return false;
  }

  private static boolean capturesWhiteBishop(int state, MoveSpecification move) {
    return havingMove(state) == BLACK_TO_MOVE
        && (move.toSquare().ordinal() == whiteLightBishop(state) || move.toSquare().ordinal() == whiteDarkBishop(
            state));
  }

  private static int afterMove(int state, MoveSpecification move) {
    final var fromSquare = move.fromSquare().ordinal();
    final var toSquare = move.toSquare().ordinal();
    return switch (havingMove(state)) {
      case WHITE_TO_MOVE -> {
        if (fromSquare == whiteKing(state)) {
          yield encode(toSquare, whiteLightBishop(state), whiteDarkBishop(state), blackKing(state), BLACK_TO_MOVE);
        }
        if (fromSquare == whiteLightBishop(state)) {
          yield encode(whiteKing(state), toSquare, whiteDarkBishop(state), blackKing(state), BLACK_TO_MOVE);
        }
        yield encode(whiteKing(state), whiteLightBishop(state), toSquare, blackKing(state), BLACK_TO_MOVE);
      }
      case BLACK_TO_MOVE -> encode(whiteKing(state), whiteLightBishop(state), whiteDarkBishop(state), toSquare,
          WHITE_TO_MOVE);
      default -> throw new IllegalArgumentException();
    };
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

  private static BitboardPosition toBitboardPosition(int state) {
    return new BitboardPosition(0L, 0L, 0L, bit(whiteLightBishop(state)) | bit(whiteDarkBishop(state)), 0L,
        bit(whiteKing(state)), 0L, 0L, 0L, 0L, 0L, bit(blackKing(state)));
  }

  private static long bit(int square) {
    return 1L << square;
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

  private static Side side(int havingMove) {
    return switch (havingMove) {
      case WHITE_TO_MOVE -> Side.WHITE;
      case BLACK_TO_MOVE -> Side.BLACK;
      default -> throw new IllegalArgumentException();
    };
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

  private static int offset(int square, int[] delta) {
    final var targetFile = file(square) + delta[0];
    final var targetRank = rank(square) + delta[1];
    if (targetFile < 0 || targetFile >= 8 || targetRank < 0 || targetRank >= 8) {
      return -1;
    }
    return square(targetFile, targetRank);
  }

  private static boolean isLightSquare(int square) {
    return (file(square) + rank(square)) % 2 == 1;
  }

  private static NavigableSet<OppositeBishopsState> canonicalRepresentatives(BitSet states) {
    final NavigableSet<OppositeBishopsState> result = new TreeSet<>();
    for (var state = states.nextSetBit(0); state >= 0; state = states.nextSetBit(state + 1)) {
      result.add(canonical(toState(state)));
    }
    return Collections.unmodifiableNavigableSet(result);
  }

  private static NavigableSet<OppositeBishopsState> canonicalRepresentativesWithout(BitSet states,
      BitSet excludedStates) {
    final BitSet remaining = (BitSet) states.clone();
    remaining.andNot(excludedStates);
    return canonicalRepresentatives(remaining);
  }

  private static NavigableSet<OppositeBishopsState> canonicalRepresentatives(
      Collection<OppositeBishopsState> states) {
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
    return Square.REAL.get(transformedRank * 8 + transformedFile);
  }

  private static OppositeBishopsState toState(int state) {
    return new OppositeBishopsState(Square.REAL.get(whiteKing(state)), Square.REAL.get(whiteLightBishop(state)),
        Square.REAL.get(whiteDarkBishop(state)), Square.REAL.get(blackKing(state)), side(havingMove(state)));
  }

  static String toFen(OppositeBishopsState state) {
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

  record AnalysisResult(int legalStateCount, int reachableStateCount, int unreachableStateCount,
      int unreachableBlackToMoveStateCount, int unreachableBlackToMoveInCheckStateCount,
      int unreachableBlackToMoveNotInCheckStateCount, int unreachableWhiteToMoveStateCount,
      int unreachableWhiteToMoveInCheckStateCount, int unreachableWhiteToMoveNotInCheckStateCount,
      Set<OppositeBishopsState> unreachableRepresentatives, int lastMoveFilterInputStateCount,
      int lastMoveFilterRejectedStateCount, int afterLastMoveFilterStateCount,
      int lastMoveFilterInputRepresentativeCount, int lastMoveFilterRejectedRepresentativeCount,
      int afterLastMoveFilterRepresentativeCount) {

    AnalysisResult {
      unreachableRepresentatives = canonicalRepresentatives(unreachableRepresentatives);
    }
  }
}
