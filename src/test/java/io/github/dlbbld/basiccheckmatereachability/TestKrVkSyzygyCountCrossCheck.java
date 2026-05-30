package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TestKrVkSyzygyCountCrossCheck {

  @SuppressWarnings("static-method")
  @Test
  void krvKRawCountsCollapseToSyzygyUniquePositionsUnderBoardSymmetry() {
    final Set<State> whiteToMove = new HashSet<>();
    final Set<State> blackToMove = new HashSet<>();

    var rawWhiteToMove = 0;
    var rawBlackToMove = 0;

    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whiteRook = 0; whiteRook < 64; whiteRook++) {
        for (var blackKing = 0; blackKing < 64; blackKing++) {
          if (isLocallyLegal(whiteKing, whiteRook, blackKing, true)) {
            rawWhiteToMove++;
            whiteToMove.add(canonical(whiteKing, whiteRook, blackKing, true));
          }
          if (isLocallyLegal(whiteKing, whiteRook, blackKing, false)) {
            rawBlackToMove++;
            blackToMove.add(canonical(whiteKing, whiteRook, blackKing, false));
          }
        }
      }
    }

    final Set<State> allCanonicalStates = new HashSet<>();
    allCanonicalStates.addAll(whiteToMove);
    allCanonicalStates.addAll(blackToMove);

    assertEquals(175168, rawWhiteToMove);
    assertEquals(223944, rawBlackToMove);
    assertEquals(21959, whiteToMove.size());
    assertEquals(28056, blackToMove.size());
    assertEquals(50015, allCanonicalStates.size());
  }

  private static boolean isLocallyLegal(int whiteKing, int whiteRook, int blackKing, boolean whiteToMove) {
    if (whiteKing == whiteRook || whiteKing == blackKing || whiteRook == blackKing) {
      return false;
    }
    if (kingsTouch(whiteKing, blackKing)) {
      return false;
    }
    return !whiteToMove || !rookAttacks(whiteRook, blackKing, whiteKing);
  }

  private static boolean kingsTouch(int a, int b) {
    return Math.max(Math.abs(file(a) - file(b)), Math.abs(rank(a) - rank(b))) == 1;
  }

  private static boolean rookAttacks(int rook, int target, int blocker) {
    if (file(rook) != file(target) && rank(rook) != rank(target)) {
      return false;
    }
    if (file(rook) == file(target)) {
      return file(blocker) != file(rook) || !between(rank(blocker), rank(rook), rank(target));
    }
    return rank(blocker) != rank(rook) || !between(file(blocker), file(rook), file(target));
  }

  private static boolean between(int value, int end1, int end2) {
    return Math.min(end1, end2) < value && value < Math.max(end1, end2);
  }

  private static State canonical(int whiteKing, int whiteRook, int blackKing, boolean whiteToMove) {
    State best = null;
    for (var transform = 0; transform < 8; transform++) {
      final var state = new State(transform(whiteKing, transform), transform(whiteRook, transform),
          transform(blackKing, transform), whiteToMove);
      if (best == null || state.compareTo(best) < 0) {
        best = state;
      }
    }
    return best;
  }

  private static int transform(int square, int transform) {
    final var file = file(square);
    final var rank = rank(square);
    return switch (transform) {
      case 0 -> square(file, rank);
      case 1 -> square(7 - file, rank);
      case 2 -> square(file, 7 - rank);
      case 3 -> square(7 - file, 7 - rank);
      case 4 -> square(rank, file);
      case 5 -> square(7 - rank, file);
      case 6 -> square(rank, 7 - file);
      case 7 -> square(7 - rank, 7 - file);
      default -> throw new IllegalArgumentException("unknown transform");
    };
  }

  private static int square(int file, int rank) {
    return rank * 8 + file;
  }

  private static int file(int square) {
    return square % 8;
  }

  private static int rank(int square) {
    return square / 8;
  }

  private record State(int whiteKing, int whiteRook, int blackKing, boolean whiteToMove) implements Comparable<State> {

    @Override
    public int compareTo(State other) {
      var comparison = Integer.compare(whiteKing, other.whiteKing);
      if (comparison != 0) {
        return comparison;
      }
      comparison = Integer.compare(whiteRook, other.whiteRook);
      if (comparison != 0) {
        return comparison;
      }
      comparison = Integer.compare(blackKing, other.blackKing);
      if (comparison != 0) {
        return comparison;
      }
      return Boolean.compare(whiteToMove, other.whiteToMove);
    }
  }
}
