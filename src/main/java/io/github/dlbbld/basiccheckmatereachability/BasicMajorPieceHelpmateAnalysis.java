package io.github.dlbbld.basiccheckmatereachability;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.board.MoveSpecification;

final class BasicMajorPieceHelpmateAnalysis {

  private BasicMajorPieceHelpmateAnalysis() {
  }

  static AnalysisResult analyze(WhiteMajorPiece whiteMajorPiece) {
    final Set<MajorPieceState> states = enumerateLegalStates(whiteMajorPiece);
    final Map<MajorPieceState, Set<MoveSpecification>> legalMovesByState = calculateLegalMoves(whiteMajorPiece,
        states);
    final Set<MajorPieceState> checkmates = calculateCheckmates(whiteMajorPiece, states, legalMovesByState);
    final Set<MajorPieceState> winning = calculateWinning(legalMovesByState, checkmates);
    final Set<MajorPieceState> blackToMoveStates = filterHavingMove(states, Side.BLACK);
    final Set<MajorPieceState> whiteToMoveStates = filterHavingMove(states, Side.WHITE);
    final var blackToMoveInCheckStateCount = countInCheck(whiteMajorPiece, blackToMoveStates, Side.BLACK);

    final Set<MajorPieceState> unwinnableWhiteToMove = new HashSet<>();
    final Set<MajorPieceState> forcedMajorPieceCapture = new HashSet<>();
    final Set<MajorPieceState> stalemate = new HashSet<>();
    final Set<MajorPieceState> counterexamples = new HashSet<>();

    for (final MajorPieceState state : whiteToMoveStates) {
      if (!winning.contains(state)) {
        unwinnableWhiteToMove.add(state);
      }
    }

    for (final MajorPieceState state : blackToMoveStates) {
      if (winning.contains(state)) {
        continue;
      }

      final Set<MoveSpecification> legalMoves = legalMovesByState.get(state);
      final var hasLegalMove = !legalMoves.isEmpty();
      final var hasNonCaptureMove = hasNonCaptureMove(state, legalMoves);

      if (!hasLegalMove && !toBitboardPosition(whiteMajorPiece, state).isInCheck(Side.BLACK)) {
        stalemate.add(state);
      } else if (hasLegalMove && !hasNonCaptureMove) {
        forcedMajorPieceCapture.add(state);
      } else if (hasNonCaptureMove) {
        counterexamples.add(state);
      }
    }

    return new AnalysisResult(whiteMajorPiece, states.size(), whiteToMoveStates.size(), blackToMoveStates.size(),
        checkmates.size(), blackToMoveInCheckStateCount,
        blackToMoveStates.size() - blackToMoveInCheckStateCount, winning.size(),
        blackToMoveStates.size() - checkmates.size() - stalemate.size(), unwinnableWhiteToMove.size(),
        canonicalRepresentatives(unwinnableWhiteToMove), forcedMajorPieceCapture.size(),
        canonicalRepresentatives(forcedMajorPieceCapture), stalemate.size(), canonicalRepresentatives(stalemate),
        counterexamples.size(), canonicalRepresentatives(counterexamples));
  }

  static String format(AnalysisResult result) {
    final StringBuilder sb = new StringBuilder();
    sb.append(result.whiteMajorPiece().materialName()).append(" states").append('\n');
    sb.append("legal states: ").append(result.legalStateCount()).append('\n');
    sb.append("white-to-move states: ").append(result.whiteToMoveStateCount()).append('\n');
    sb.append("black-to-move states: ").append(result.blackToMoveStateCount()).append('\n');
    sb.append("black-to-move in-check states: ").append(result.blackToMoveInCheckStateCount()).append('\n');
    sb.append("black-to-move not-in-check states: ").append(result.blackToMoveNotInCheckStateCount()).append('\n');
    sb.append("black checkmates: ").append(result.blackCheckmateCount()).append('\n');
    sb.append("ongoing black-to-move states: ").append(result.ongoingBlackToMoveStateCount()).append('\n');
    sb.append("cooperatively winnable states: ").append(result.winningStateCount()).append('\n');
    sb.append("unwinnable white-to-move states: ").append(result.unwinnableWhiteToMoveStateCount())
        .append(", canonical ").append(result.unwinnableWhiteToMoveRepresentatives().size()).append('\n');
    appendStates(sb, "unwinnable white-to-move representatives", result.whiteMajorPiece(),
        result.unwinnableWhiteToMoveRepresentatives());
    sb.append("forced ").append(result.whiteMajorPiece().pieceName()).append("-capture states: ")
        .append(result.forcedMajorPieceCaptureStateCount()).append(", canonical ")
        .append(result.forcedMajorPieceCaptureRepresentatives().size()).append('\n');
    appendStates(sb, "forced " + result.whiteMajorPiece().pieceName() + "-capture representatives",
        result.whiteMajorPiece(), result.forcedMajorPieceCaptureRepresentatives());
    sb.append("stalemate states: ").append(result.stalemateStateCount()).append(", canonical ")
        .append(result.stalemateRepresentatives().size()).append('\n');
    appendStates(sb, "stalemate representatives", result.whiteMajorPiece(), result.stalemateRepresentatives());
    sb.append("counterexample states: ").append(result.counterexampleStateCount()).append(", canonical ")
        .append(result.counterexampleRepresentatives().size()).append('\n');
    appendStates(sb, "counterexample representatives", result.whiteMajorPiece(),
        result.counterexampleRepresentatives());
    return sb.toString();
  }

  private static void appendStates(StringBuilder sb, String title, WhiteMajorPiece whiteMajorPiece,
      Set<MajorPieceState> states) {
    sb.append(title).append(':').append('\n');
    for (final MajorPieceState state : states) {
      sb.append("  ").append(toFen(whiteMajorPiece, state)).append(" ; moves=")
          .append(formatMoves(whiteMajorPiece, state)).append('\n');
    }
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
          && legalMovesByState.get(state).isEmpty()) {
        result.add(state);
      }
    }
    return result;
  }

  private static Set<MajorPieceState> calculateWinning(
      Map<MajorPieceState, Set<MoveSpecification>> legalMovesByState, Set<MajorPieceState> checkmates) {
    final Map<MajorPieceState, Set<MajorPieceState>> predecessors = calculatePredecessors(legalMovesByState);
    final Set<MajorPieceState> winning = new HashSet<>(checkmates);
    final ArrayDeque<MajorPieceState> queue = new ArrayDeque<>(checkmates);

    while (!queue.isEmpty()) {
      final MajorPieceState state = queue.removeFirst();
      for (final MajorPieceState predecessor : predecessors.getOrDefault(state, Set.of())) {
        if (winning.add(predecessor)) {
          queue.addLast(predecessor);
        }
      }
    }
    return winning;
  }

  private static Map<MajorPieceState, Set<MajorPieceState>> calculatePredecessors(
      Map<MajorPieceState, Set<MoveSpecification>> legalMovesByState) {
    final Map<MajorPieceState, Set<MajorPieceState>> result = new HashMap<>();
    for (final Map.Entry<MajorPieceState, Set<MoveSpecification>> entry : legalMovesByState.entrySet()) {
      final MajorPieceState state = entry.getKey();
      for (final MoveSpecification move : entry.getValue()) {
        if (capturesMajorPiece(state, move)) {
          continue;
        }
        final MajorPieceState successor = afterMove(state, move);
        result.computeIfAbsent(successor, unused -> new HashSet<>()).add(state);
      }
    }
    return result;
  }

  private static Set<MajorPieceState> filterHavingMove(Set<MajorPieceState> states, Side havingMove) {
    final Set<MajorPieceState> result = new HashSet<>();
    for (final MajorPieceState state : states) {
      if (state.havingMove() == havingMove) {
        result.add(state);
      }
    }
    return result;
  }

  private static int countInCheck(WhiteMajorPiece whiteMajorPiece, Set<MajorPieceState> states, Side side) {
    var result = 0;
    for (final MajorPieceState state : states) {
      if (toBitboardPosition(whiteMajorPiece, state).isInCheck(side)) {
        result++;
      }
    }
    return result;
  }

  private static boolean hasNonCaptureMove(MajorPieceState state, Set<MoveSpecification> legalMoves) {
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

  private static BitboardPosition toBitboardPosition(WhiteMajorPiece whiteMajorPiece, MajorPieceState state) {
    final long whiteRooks = whiteMajorPiece == WhiteMajorPiece.ROOK ? bit(state.whiteMajor()) : 0L;
    final long whiteQueens = whiteMajorPiece == WhiteMajorPiece.QUEEN ? bit(state.whiteMajor()) : 0L;
    return new BitboardPosition(0L, whiteRooks, 0L, 0L, whiteQueens, bit(state.whiteKing()), 0L, 0L, 0L, 0L, 0L,
        bit(state.blackKing()));
  }

  private static long bit(Square square) {
    return 1L << square.ordinal();
  }

  private static String toFen(WhiteMajorPiece whiteMajorPiece, MajorPieceState state) {
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

  private static String formatMoves(WhiteMajorPiece whiteMajorPiece, MajorPieceState state) {
    final Set<MoveSpecification> moves = toBitboardPosition(whiteMajorPiece, state).legalMoves(state.havingMove(), 0L);
    if (moves.isEmpty()) {
      return "";
    }
    final StringBuilder sb = new StringBuilder();
    for (final MoveSpecification move : moves) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(move.fromSquare().getName()).append(move.toSquare().getName());
      if (capturesMajorPiece(state, move)) {
        sb.append('x').append(whiteMajorPiece.fenChar());
      }
    }
    return sb.toString();
  }

  enum WhiteMajorPiece {
    ROOK("KRvK", "rook", 'R'),
    QUEEN("KQvK", "queen", 'Q');

    private final String materialName;
    private final String pieceName;
    private final char fenChar;

    WhiteMajorPiece(String materialName, String pieceName, char fenChar) {
      this.materialName = materialName;
      this.pieceName = pieceName;
      this.fenChar = fenChar;
    }

    String materialName() {
      return materialName;
    }

    String pieceName() {
      return pieceName;
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

  record AnalysisResult(WhiteMajorPiece whiteMajorPiece, int legalStateCount, int whiteToMoveStateCount,
      int blackToMoveStateCount, int blackCheckmateCount, int blackToMoveInCheckStateCount,
      int blackToMoveNotInCheckStateCount, int winningStateCount, int ongoingBlackToMoveStateCount,
      int unwinnableWhiteToMoveStateCount, Set<MajorPieceState> unwinnableWhiteToMoveRepresentatives,
      int forcedMajorPieceCaptureStateCount, Set<MajorPieceState> forcedMajorPieceCaptureRepresentatives,
      int stalemateStateCount, Set<MajorPieceState> stalemateRepresentatives, int counterexampleStateCount,
      Set<MajorPieceState> counterexampleRepresentatives) {

    AnalysisResult {
      unwinnableWhiteToMoveRepresentatives = sortedCopy(unwinnableWhiteToMoveRepresentatives);
      forcedMajorPieceCaptureRepresentatives = sortedCopy(forcedMajorPieceCaptureRepresentatives);
      stalemateRepresentatives = sortedCopy(stalemateRepresentatives);
      counterexampleRepresentatives = sortedCopy(counterexampleRepresentatives);
    }

    private static Set<MajorPieceState> sortedCopy(Set<MajorPieceState> states) {
      return Collections.unmodifiableSet(new TreeSet<>(states));
    }
  }
}
