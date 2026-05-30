package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.ashlarchess.board.enums.Square.A1;
import static io.github.dlbbld.ashlarchess.board.enums.Square.D1;
import static io.github.dlbbld.ashlarchess.board.enums.Square.E1;
import static io.github.dlbbld.ashlarchess.board.enums.Square.E8;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.common.model.MoveSpecification;

final class BasicMajorPieceSeedLegalityAnalysis {

  private BasicMajorPieceSeedLegalityAnalysis() {
  }

  static AnalysisResult analyzeRookFromOriginalSquares() {
    return analyzeFromOriginalSquares(WhiteMajorPiece.ROOK);
  }

  static AnalysisResult analyzeQueenFromOriginalSquares() {
    return analyzeFromOriginalSquares(WhiteMajorPiece.QUEEN);
  }

  private static AnalysisResult analyzeFromOriginalSquares(WhiteMajorPiece whiteMajorPiece) {
    final Set<MajorPieceState> states = enumerateLegalStates(whiteMajorPiece);
    final var seed = new MajorPieceState(E1, whiteMajorPiece.originalSquare(), E8, Side.BLACK);
    if (!states.contains(seed)) {
      throw new IllegalStateException(whiteMajorPiece.materialName() + " original-square seed is not locally legal");
    }

    final Set<MajorPieceState> reachable = calculateReachable(whiteMajorPiece, states, seed);
    final Set<MajorPieceState> unreachable = new HashSet<>(states);
    unreachable.removeAll(reachable);

    return new AnalysisResult(states.size(), reachable.size(), unreachable.size(), countHavingMove(unreachable,
        Side.BLACK), countHavingMoveAndCheck(whiteMajorPiece, unreachable, Side.BLACK, true),
        countHavingMoveAndCheck(whiteMajorPiece, unreachable, Side.BLACK, false), countHavingMove(unreachable,
            Side.WHITE), countHavingMoveAndCheck(whiteMajorPiece, unreachable, Side.WHITE, true),
        countHavingMoveAndCheck(whiteMajorPiece, unreachable, Side.WHITE, false), canonicalRepresentatives(unreachable));
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
            final var state = new MajorPieceState(whiteKing, whiteMajor, blackKing, havingMove);
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
    return !toBitboardPosition(whiteMajorPiece, state).isInCheck(state.havingMove().getOppositeSide());
  }

  private static Set<MajorPieceState> calculateReachable(WhiteMajorPiece whiteMajorPiece, Set<MajorPieceState> states,
      MajorPieceState seed) {
    final Set<MajorPieceState> reachable = new HashSet<>();
    final ArrayDeque<MajorPieceState> queue = new ArrayDeque<>();
    reachable.add(seed);
    queue.addLast(seed);

    while (!queue.isEmpty()) {
      final MajorPieceState state = queue.removeFirst();
      for (final MoveSpecification move : toBitboardPosition(whiteMajorPiece, state).legalMoves(state.havingMove(),
          0L)) {
        if (capturesMajorPiece(state, move)) {
          continue;
        }
        final MajorPieceState successor = afterMove(state, move);
        if (states.contains(successor) && reachable.add(successor)) {
          queue.addLast(successor);
        }
      }
    }
    return reachable;
  }

  private static int countHavingMove(Set<MajorPieceState> states, Side havingMove) {
    var result = 0;
    for (final MajorPieceState state : states) {
      if (state.havingMove() == havingMove) {
        result++;
      }
    }
    return result;
  }

  private static int countHavingMoveAndCheck(WhiteMajorPiece whiteMajorPiece, Set<MajorPieceState> states,
      Side havingMove, boolean inCheck) {
    var result = 0;
    for (final MajorPieceState state : states) {
      if (state.havingMove() == havingMove
          && toBitboardPosition(whiteMajorPiece, state).isInCheck(havingMove) == inCheck) {
        result++;
      }
    }
    return result;
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

  private static BitboardPosition toBitboardPosition(MajorPieceState state) {
    return toBitboardPosition(WhiteMajorPiece.ROOK, state);
  }

  private static BitboardPosition toBitboardPosition(WhiteMajorPiece whiteMajorPiece, MajorPieceState state) {
    final long whiteRooks = whiteMajorPiece == WhiteMajorPiece.ROOK ? bit(state.whiteMajor()) : 0L;
    final long whiteQueens = whiteMajorPiece == WhiteMajorPiece.QUEEN ? bit(state.whiteMajor()) : 0L;
    return new BitboardPosition(0L, whiteRooks, 0L, 0L, whiteQueens, bit(state.whiteKing()), 0L, 0L, 0L, 0L, 0L,
        bit(state.blackKing()));
  }

  private static long bit(Square square) {
    return 1L << square.ordinal();
  }

  private static NavigableSet<MajorPieceState> canonicalRepresentatives(Collection<MajorPieceState> states) {
    final NavigableSet<MajorPieceState> result = new TreeSet<>();
    for (final MajorPieceState state : states) {
      result.add(canonical(state));
    }
    return Collections.unmodifiableNavigableSet(result);
  }

  private static MajorPieceState canonical(MajorPieceState state) {
    MajorPieceState result = state;
    for (int transformIndex = 1; transformIndex < 8; transformIndex++) {
      final MajorPieceState transformed = transform(state, transformIndex);
      if (transformed.compareTo(result) < 0) {
        result = transformed;
      }
    }
    return result;
  }

  private static MajorPieceState transform(MajorPieceState state, int transformIndex) {
    return new MajorPieceState(transform(state.whiteKing(), transformIndex), transform(state.whiteMajor(),
        transformIndex), transform(state.blackKing(), transformIndex), state.havingMove());
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

  static String toFen(WhiteMajorPiece whiteMajorPiece, MajorPieceState state) {
    final char[] board = new char[64];
    for (int i = 0; i < board.length; i++) {
      board[i] = '1';
    }
    board[state.whiteKing().ordinal()] = 'K';
    board[state.whiteMajor().ordinal()] = whiteMajorPiece.fenChar();
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

  enum WhiteMajorPiece {
    ROOK("KRvK", A1, 'R'),
    QUEEN("KQvK", D1, 'Q');

    private final String materialName;
    private final Square originalSquare;
    private final char fenChar;

    WhiteMajorPiece(String materialName, Square originalSquare, char fenChar) {
      this.materialName = materialName;
      this.originalSquare = originalSquare;
      this.fenChar = fenChar;
    }

    String materialName() {
      return materialName;
    }

    Square originalSquare() {
      return originalSquare;
    }

    char fenChar() {
      return fenChar;
    }
  }

  record MajorPieceState(Square whiteKing, Square whiteMajor, Square blackKing, Side havingMove)
      implements Comparable<MajorPieceState> {

    @Override
    public int compareTo(MajorPieceState other) {
      if (whiteKing != other.whiteKing) {
        return whiteKing.compareTo(other.whiteKing);
      }
      if (whiteMajor != other.whiteMajor) {
        return whiteMajor.compareTo(other.whiteMajor);
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
      Set<MajorPieceState> unreachableRepresentatives) {

    AnalysisResult {
      unreachableRepresentatives = Collections.unmodifiableSet(new TreeSet<>(unreachableRepresentatives));
    }
  }
}
