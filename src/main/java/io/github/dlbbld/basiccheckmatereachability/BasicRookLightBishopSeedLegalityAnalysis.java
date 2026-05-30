package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.ashlarchess.board.enums.Square.A1;
import static io.github.dlbbld.ashlarchess.board.enums.Square.C8;
import static io.github.dlbbld.ashlarchess.board.enums.Square.E1;
import static io.github.dlbbld.ashlarchess.board.enums.Square.E8;

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

final class BasicRookLightBishopSeedLegalityAnalysis {

  private static final int WHITE_TO_MOVE = 0;
  private static final int BLACK_TO_MOVE = 1;
  private static final int SQUARE_COUNT = 64;
  private static final int STATE_COUNT = SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * 2;

  private BasicRookLightBishopSeedLegalityAnalysis() {
  }

  static AnalysisResult analyzeFromOriginalSquares() {
    final BitSet legalStates = enumerateLegalStates();
    final var seed = encode(E1.ordinal(), A1.ordinal(), E8.ordinal(), C8.ordinal(), BLACK_TO_MOVE);
    if (!legalStates.get(seed)) {
      throw new IllegalStateException("KRvKB original-square seed is not locally legal");
    }

    final BitSet reachable = calculateReachable(legalStates, seed);
    final BitSet unreachable = (BitSet) legalStates.clone();
    unreachable.andNot(reachable);

    return new AnalysisResult(legalStates.cardinality(), reachable.cardinality(), unreachable.cardinality(),
        countHavingMove(unreachable, BLACK_TO_MOVE), countHavingMoveAndCheck(unreachable, BLACK_TO_MOVE, true),
        countHavingMoveAndCheck(unreachable, BLACK_TO_MOVE, false), countHavingMove(unreachable, WHITE_TO_MOVE),
        countHavingMoveAndCheck(unreachable, WHITE_TO_MOVE, true), countHavingMoveAndCheck(unreachable, WHITE_TO_MOVE,
            false), canonicalRepresentatives(unreachable));
  }

  private static BitSet enumerateLegalStates() {
    final BitSet result = new BitSet(STATE_COUNT);
    for (var whiteKing = 0; whiteKing < SQUARE_COUNT; whiteKing++) {
      for (var whiteRook = 0; whiteRook < SQUARE_COUNT; whiteRook++) {
        if (whiteRook == whiteKing) {
          continue;
        }
        for (var blackKing = 0; blackKing < SQUARE_COUNT; blackKing++) {
          if (blackKing == whiteKing || blackKing == whiteRook) {
            continue;
          }
          for (var blackBishop = 0; blackBishop < SQUARE_COUNT; blackBishop++) {
            if (!isLightSquare(blackBishop) || blackBishop == whiteKing || blackBishop == whiteRook
                || blackBishop == blackKing) {
              continue;
            }
            for (var havingMove = WHITE_TO_MOVE; havingMove <= BLACK_TO_MOVE; havingMove++) {
              if (isLegalState(whiteKing, whiteRook, blackKing, blackBishop, havingMove)) {
                result.set(encode(whiteKing, whiteRook, blackKing, blackBishop, havingMove));
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
        if (leavesMaterialClass(state, move)) {
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

  private static boolean leavesMaterialClass(int state, MoveSpecification move) {
    return havingMove(state) == WHITE_TO_MOVE && move.toSquare().ordinal() == blackBishop(state)
        || havingMove(state) == BLACK_TO_MOVE && move.toSquare().ordinal() == whiteRook(state);
  }

  private static int afterMove(int state, MoveSpecification move) {
    if (leavesMaterialClass(state, move)) {
      throw new IllegalArgumentException("Capture moves leave the state space");
    }
    final var fromSquare = move.fromSquare().ordinal();
    final var toSquare = move.toSquare().ordinal();
    return switch (havingMove(state)) {
      case WHITE_TO_MOVE -> {
        if (fromSquare == whiteKing(state)) {
          yield encode(toSquare, whiteRook(state), blackKing(state), blackBishop(state), BLACK_TO_MOVE);
        }
        yield encode(whiteKing(state), toSquare, blackKing(state), blackBishop(state), BLACK_TO_MOVE);
      }
      case BLACK_TO_MOVE -> {
        if (fromSquare == blackKing(state)) {
          yield encode(whiteKing(state), whiteRook(state), toSquare, blackBishop(state), WHITE_TO_MOVE);
        }
        yield encode(whiteKing(state), whiteRook(state), blackKing(state), toSquare, WHITE_TO_MOVE);
      }
      default -> throw new IllegalArgumentException();
    };
  }

  private static boolean isLegalState(int whiteKing, int whiteRook, int blackKing, int blackBishop, int havingMove) {
    return !toBitboardPosition(whiteKing, whiteRook, blackKing, blackBishop).isInCheck(side(havingMove)
        .getOppositeSide());
  }

  private static BitboardPosition toBitboardPosition(int state) {
    return toBitboardPosition(whiteKing(state), whiteRook(state), blackKing(state), blackBishop(state));
  }

  private static BitboardPosition toBitboardPosition(int whiteKing, int whiteRook, int blackKing, int blackBishop) {
    return new BitboardPosition(0L, bit(whiteRook), 0L, 0L, 0L, bit(whiteKing), 0L, 0L, 0L, bit(blackBishop),
        0L, bit(blackKing));
  }

  private static long bit(int square) {
    return 1L << square;
  }

  private static int encode(int whiteKing, int whiteRook, int blackKing, int blackBishop, int havingMove) {
    return (((whiteKing * SQUARE_COUNT + whiteRook) * SQUARE_COUNT + blackKing) * SQUARE_COUNT + blackBishop) * 2
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

  private static int blackBishop(int state) {
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

  private static boolean isLightSquare(int square) {
    return (file(square) + rank(square)) % 2 == 1;
  }

  private static NavigableSet<RookLightBishopState> canonicalRepresentatives(BitSet states) {
    final NavigableSet<RookLightBishopState> result = new TreeSet<>();
    for (var state = states.nextSetBit(0); state >= 0; state = states.nextSetBit(state + 1)) {
      result.add(canonical(toState(state)));
    }
    return Collections.unmodifiableNavigableSet(result);
  }

  private static NavigableSet<RookLightBishopState> canonicalRepresentatives(
      Collection<RookLightBishopState> states) {
    final NavigableSet<RookLightBishopState> result = new TreeSet<>();
    for (final RookLightBishopState state : states) {
      result.add(canonical(state));
    }
    return Collections.unmodifiableNavigableSet(result);
  }

  private static RookLightBishopState canonical(RookLightBishopState state) {
    RookLightBishopState result = state;
    for (final int transformIndex : new int[] {3, 4, 7}) {
      final RookLightBishopState transformed = transform(state, transformIndex);
      if (transformed.compareTo(result) < 0) {
        result = transformed;
      }
    }
    return result;
  }

  private static RookLightBishopState transform(RookLightBishopState state, int transformIndex) {
    return new RookLightBishopState(transform(state.whiteKing(), transformIndex),
        transform(state.whiteRook(), transformIndex), transform(state.blackKing(), transformIndex),
        transform(state.blackBishop(), transformIndex), state.havingMove());
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
      case 3 -> {
        transformedFile = 7 - file;
        transformedRank = 7 - rank;
      }
      case 4 -> {
        transformedFile = rank;
        transformedRank = file;
      }
      case 7 -> {
        transformedFile = 7 - rank;
        transformedRank = 7 - file;
      }
      default -> throw new IllegalArgumentException("transformIndex out of range: " + transformIndex);
    }
    return Square.REAL.get(transformedRank * 8 + transformedFile);
  }

  private static RookLightBishopState toState(int state) {
    return new RookLightBishopState(Square.REAL.get(whiteKing(state)), Square.REAL.get(whiteRook(state)),
        Square.REAL.get(blackKing(state)), Square.REAL.get(blackBishop(state)), side(havingMove(state)));
  }

  record RookLightBishopState(Square whiteKing, Square whiteRook, Square blackKing, Square blackBishop,
      Side havingMove) implements Comparable<RookLightBishopState> {

    @Override
    public int compareTo(RookLightBishopState other) {
      if (whiteKing != other.whiteKing) {
        return whiteKing.compareTo(other.whiteKing);
      }
      if (whiteRook != other.whiteRook) {
        return whiteRook.compareTo(other.whiteRook);
      }
      if (blackKing != other.blackKing) {
        return blackKing.compareTo(other.blackKing);
      }
      if (blackBishop != other.blackBishop) {
        return blackBishop.compareTo(other.blackBishop);
      }
      return havingMove.compareTo(other.havingMove);
    }
  }

  record AnalysisResult(int legalStateCount, int reachableStateCount, int unreachableStateCount,
      int unreachableBlackToMoveStateCount, int unreachableBlackToMoveInCheckStateCount,
      int unreachableBlackToMoveNotInCheckStateCount, int unreachableWhiteToMoveStateCount,
      int unreachableWhiteToMoveInCheckStateCount, int unreachableWhiteToMoveNotInCheckStateCount,
      Set<RookLightBishopState> unreachableRepresentatives) {

    AnalysisResult {
      unreachableRepresentatives = canonicalRepresentatives(unreachableRepresentatives);
    }
  }
}
