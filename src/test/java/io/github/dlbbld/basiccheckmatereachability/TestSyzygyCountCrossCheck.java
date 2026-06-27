package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.BitSet;
import org.junit.jupiter.api.Test;

class TestSyzygyCountCrossCheck {

  private static final int[] FULL_BOARD_SYMMETRIES = {0, 1, 2, 3, 4, 5, 6, 7};
  private static final int[] BISHOP_COLOUR_PRESERVING_SYMMETRIES = {0, 3, 4, 7};

  @SuppressWarnings("static-method")
  @Test
  void majorPieceCountsMatchSyzygyUniquePositions() {
    assertCount(countMajorPiece(true), 175168, 223944, 21959, 28056, 50015);
    assertCount(countMajorPiece(false), 144508, 223944, 18081, 28056, 46137);
  }

  @SuppressWarnings("static-method")
  @Test
  void lightBishopKnightCountsMatchSyzygyUniquePositions() {
    assertCount(countLightBishopKnight(), 5437752, 6830292, 1359578, 1707888, 3067466);
  }

  @SuppressWarnings("static-method")
  @Test
  void rookLightBishopCountsMatchSyzygyUniquePositions() {
    assertCount(countRookLightBishop(), 5390364, 5916232, 1347906, 1479198, 2827104);
  }

  @SuppressWarnings("static-method")
  @Test
  void rookKnightCountsMatchSyzygyUniquePositions() {
    assertCount(countRookKnight(), 10780728, 12535256, 1347906, 1567222, 2915128);
  }

  @SuppressWarnings("static-method")
  @Test
  void twoKnightsCountsMatchSyzygyUniquePositions() {
    assertCount(countTwoKnights(), 5749652, 6830292, 719130, 854238, 1573368);
  }

  @SuppressWarnings("static-method")
  @Test
  void oppositeBishopSubsetAndExpandedBishopTableAreCountedSeparately() {
    assertCount(countOppositeBishops(), 2504128, 3469344, 626032, 867336, 1493368);
    assertCount(countOppositeBishopsOrderedSlots(), 5008256, 6938688, 626032, 867336, 1493368);
    assertCount(countSameColorBishops(), 5155800, 6721896, 644510, 840552, 1485062);
    assertCount(countAllBishopsBySummingColorSubsets(), 10164056, 13660584, 1270542, 1707888, 2978430);
  }

  private static void assertCount(Count count, int rawWhiteToMove, int rawBlackToMove, int uniqueWhiteToMove,
      int uniqueBlackToMove, int uniqueTotal) {
    assertEquals(rawWhiteToMove, count.rawWhiteToMove());
    assertEquals(rawBlackToMove, count.rawBlackToMove());
    assertEquals(uniqueWhiteToMove, count.uniqueWhiteToMove());
    assertEquals(uniqueBlackToMove, count.uniqueBlackToMove());
    assertEquals(uniqueTotal, count.uniqueTotal());
  }

  private static Count countMajorPiece(boolean rook) {
    final var counter = new Counter();
    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whitePiece = 0; whitePiece < 64; whitePiece++) {
        if (whitePiece == whiteKing) {
          continue;
        }
        for (var blackKing = 0; blackKing < 64; blackKing++) {
          if (blackKing == whiteKing || blackKing == whitePiece || kingsTouch(whiteKing, blackKing)) {
            continue;
          }
          final var blackInCheck = rook ? rookAttacks(whitePiece, blackKing, whiteKing)
              : rookAttacks(whitePiece, blackKing, whiteKing) || bishopAttacks(whitePiece, blackKing, whiteKing);
          if (!blackInCheck) {
            counter.addWhiteToMove(new int[] {whiteKing, whitePiece, blackKing}, FULL_BOARD_SYMMETRIES);
          }
          counter.addBlackToMove(new int[] {whiteKing, whitePiece, blackKing}, FULL_BOARD_SYMMETRIES);
        }
      }
    }
    return counter.count();
  }

  private static Count countLightBishopKnight() {
    final var counter = new Counter();
    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whiteBishop = 0; whiteBishop < 64; whiteBishop++) {
        if (!isLightSquare(whiteBishop) || whiteBishop == whiteKing) {
          continue;
        }
        for (var whiteKnight = 0; whiteKnight < 64; whiteKnight++) {
          if (whiteKnight == whiteKing || whiteKnight == whiteBishop) {
            continue;
          }
          for (var blackKing = 0; blackKing < 64; blackKing++) {
            if (blackKing == whiteKing || blackKing == whiteBishop || blackKing == whiteKnight) {
              continue;
            }
            final var blackInCheck = kingsTouch(whiteKing, blackKing)
                || bishopAttacks(whiteBishop, blackKing, whiteKing, whiteKnight)
                || knightAttacks(whiteKnight, blackKing);
            if (!blackInCheck) {
              counter.addWhiteToMove(new int[] {whiteKing, whiteBishop, whiteKnight, blackKing},
                  BISHOP_COLOUR_PRESERVING_SYMMETRIES);
            }
            if (!kingsTouch(whiteKing, blackKing)) {
              counter.addBlackToMove(new int[] {whiteKing, whiteBishop, whiteKnight, blackKing},
                  BISHOP_COLOUR_PRESERVING_SYMMETRIES);
            }
          }
        }
      }
    }
    return counter.count();
  }

  private static Count countRookLightBishop() {
    final var counter = new Counter();
    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whiteRook = 0; whiteRook < 64; whiteRook++) {
        if (whiteRook == whiteKing) {
          continue;
        }
        for (var blackKing = 0; blackKing < 64; blackKing++) {
          if (blackKing == whiteKing || blackKing == whiteRook) {
            continue;
          }
          for (var blackBishop = 0; blackBishop < 64; blackBishop++) {
            if (!isLightSquare(blackBishop) || blackBishop == whiteKing || blackBishop == whiteRook
                || blackBishop == blackKing) {
              continue;
            }
            final var blackInCheck = kingsTouch(whiteKing, blackKing)
                || rookAttacks(whiteRook, blackKing, whiteKing, blackBishop);
            final var whiteInCheck = kingsTouch(whiteKing, blackKing)
                || bishopAttacks(blackBishop, whiteKing, blackKing, whiteRook);
            if (!blackInCheck) {
              counter.addWhiteToMove(new int[] {whiteKing, whiteRook, blackKing, blackBishop},
                  BISHOP_COLOUR_PRESERVING_SYMMETRIES);
            }
            if (!whiteInCheck) {
              counter.addBlackToMove(new int[] {whiteKing, whiteRook, blackKing, blackBishop},
                  BISHOP_COLOUR_PRESERVING_SYMMETRIES);
            }
          }
        }
      }
    }
    return counter.count();
  }

  private static Count countRookKnight() {
    final var counter = new Counter();
    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whiteRook = 0; whiteRook < 64; whiteRook++) {
        if (whiteRook == whiteKing) {
          continue;
        }
        for (var blackKing = 0; blackKing < 64; blackKing++) {
          if (blackKing == whiteKing || blackKing == whiteRook) {
            continue;
          }
          for (var blackKnight = 0; blackKnight < 64; blackKnight++) {
            if (blackKnight == whiteKing || blackKnight == whiteRook || blackKnight == blackKing) {
              continue;
            }
            final var blackInCheck = kingsTouch(whiteKing, blackKing)
                || rookAttacks(whiteRook, blackKing, whiteKing, blackKnight);
            final var whiteInCheck = kingsTouch(whiteKing, blackKing) || knightAttacks(blackKnight, whiteKing);
            if (!blackInCheck) {
              counter.addWhiteToMove(new int[] {whiteKing, whiteRook, blackKing, blackKnight},
                  FULL_BOARD_SYMMETRIES);
            }
            if (!whiteInCheck) {
              counter.addBlackToMove(new int[] {whiteKing, whiteRook, blackKing, blackKnight},
                  FULL_BOARD_SYMMETRIES);
            }
          }
        }
      }
    }
    return counter.count();
  }

  private static Count countTwoKnights() {
    final var counter = new IdenticalKnightCounter();
    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whiteKnightA = 0; whiteKnightA < 64; whiteKnightA++) {
        if (whiteKnightA == whiteKing) {
          continue;
        }
        for (var whiteKnightB = whiteKnightA + 1; whiteKnightB < 64; whiteKnightB++) {
          if (whiteKnightB == whiteKing) {
            continue;
          }
          for (var blackKing = 0; blackKing < 64; blackKing++) {
            if (blackKing == whiteKing || blackKing == whiteKnightA || blackKing == whiteKnightB) {
              continue;
            }
            final var blackInCheck = kingsTouch(whiteKing, blackKing) || knightAttacks(whiteKnightA, blackKing)
                || knightAttacks(whiteKnightB, blackKing);
            if (!blackInCheck) {
              counter.addWhiteToMove(whiteKing, whiteKnightA, whiteKnightB, blackKing);
            }
            if (!kingsTouch(whiteKing, blackKing)) {
              counter.addBlackToMove(whiteKing, whiteKnightA, whiteKnightB, blackKing);
            }
          }
        }
      }
    }
    return counter.count();
  }

  private static Count countOppositeBishops() {
    final var counter = new Counter();
    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whiteLightBishop = 0; whiteLightBishop < 64; whiteLightBishop++) {
        if (!isLightSquare(whiteLightBishop) || whiteLightBishop == whiteKing) {
          continue;
        }
        for (var whiteDarkBishop = 0; whiteDarkBishop < 64; whiteDarkBishop++) {
          if (isLightSquare(whiteDarkBishop) || whiteDarkBishop == whiteKing
              || whiteDarkBishop == whiteLightBishop) {
            continue;
          }
          for (var blackKing = 0; blackKing < 64; blackKing++) {
            if (blackKing == whiteKing || blackKing == whiteLightBishop || blackKing == whiteDarkBishop) {
              continue;
            }
            final var blackInCheck = kingsTouch(whiteKing, blackKing)
                || bishopAttacks(whiteLightBishop, blackKing, whiteKing, whiteDarkBishop)
                || bishopAttacks(whiteDarkBishop, blackKing, whiteKing, whiteLightBishop);
            if (!blackInCheck) {
              counter.addWhiteToMove(new int[] {whiteKing, whiteLightBishop, whiteDarkBishop, blackKing},
                  BISHOP_COLOUR_PRESERVING_SYMMETRIES);
            }
            if (!kingsTouch(whiteKing, blackKing)) {
              counter.addBlackToMove(new int[] {whiteKing, whiteLightBishop, whiteDarkBishop, blackKing},
                  BISHOP_COLOUR_PRESERVING_SYMMETRIES);
            }
          }
        }
      }
    }
    return counter.count();
  }

  private static Count countSameColorBishops() {
    final var counter = new Counter();
    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whiteBishop1 = 0; whiteBishop1 < 64; whiteBishop1++) {
        if (whiteBishop1 == whiteKing) {
          continue;
        }
        for (var whiteBishop2 = 0; whiteBishop2 < 64; whiteBishop2++) {
          if (whiteBishop2 == whiteKing || whiteBishop2 == whiteBishop1
              || isLightSquare(whiteBishop1) != isLightSquare(whiteBishop2)) {
            continue;
          }
          for (var blackKing = 0; blackKing < 64; blackKing++) {
            if (blackKing == whiteKing || blackKing == whiteBishop1 || blackKing == whiteBishop2) {
              continue;
            }
            final var blackInCheck = kingsTouch(whiteKing, blackKing)
                || bishopAttacks(whiteBishop1, blackKing, whiteKing, whiteBishop2)
                || bishopAttacks(whiteBishop2, blackKing, whiteKing, whiteBishop1);
            if (!blackInCheck) {
              counter.addWhiteToMove(new int[] {whiteKing, whiteBishop1, whiteBishop2, blackKing},
                  FULL_BOARD_SYMMETRIES);
            }
            if (!kingsTouch(whiteKing, blackKing)) {
              counter.addBlackToMove(new int[] {whiteKing, whiteBishop1, whiteBishop2, blackKing},
                  FULL_BOARD_SYMMETRIES);
            }
          }
        }
      }
    }
    return counter.count();
  }

  private static Count countOppositeBishopsOrderedSlots() {
    final var counter = new Counter();
    for (var whiteKing = 0; whiteKing < 64; whiteKing++) {
      for (var whiteBishop1 = 0; whiteBishop1 < 64; whiteBishop1++) {
        if (whiteBishop1 == whiteKing) {
          continue;
        }
        for (var whiteBishop2 = 0; whiteBishop2 < 64; whiteBishop2++) {
          if (whiteBishop2 == whiteKing || whiteBishop2 == whiteBishop1
              || isLightSquare(whiteBishop1) == isLightSquare(whiteBishop2)) {
            continue;
          }
          for (var blackKing = 0; blackKing < 64; blackKing++) {
            if (blackKing == whiteKing || blackKing == whiteBishop1 || blackKing == whiteBishop2) {
              continue;
            }
            final var blackInCheck = kingsTouch(whiteKing, blackKing)
                || bishopAttacks(whiteBishop1, blackKing, whiteKing, whiteBishop2)
                || bishopAttacks(whiteBishop2, blackKing, whiteKing, whiteBishop1);
            if (!blackInCheck) {
              counter.addWhiteToMove(new int[] {whiteKing, whiteBishop1, whiteBishop2, blackKing},
                  FULL_BOARD_SYMMETRIES);
            }
            if (!kingsTouch(whiteKing, blackKing)) {
              counter.addBlackToMove(new int[] {whiteKing, whiteBishop1, whiteBishop2, blackKing},
                  FULL_BOARD_SYMMETRIES);
            }
          }
        }
      }
    }
    return counter.count();
  }

  private static Count countAllBishopsBySummingColorSubsets() {
    return countOppositeBishopsOrderedSlots().plus(countSameColorBishops());
  }

  private static boolean kingsTouch(int a, int b) {
    return Math.max(Math.abs(file(a) - file(b)), Math.abs(rank(a) - rank(b))) == 1;
  }

  private static boolean rookAttacks(int rook, int target, int... blockers) {
    return lineAttacks(rook, target, new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}, blockers);
  }

  private static boolean bishopAttacks(int bishop, int target, int... blockers) {
    return lineAttacks(bishop, target, new int[][] {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}}, blockers);
  }

  private static boolean lineAttacks(int piece, int target, int[][] deltas, int... blockers) {
    for (final int[] delta : deltas) {
      var file = file(piece) + delta[0];
      var rank = rank(piece) + delta[1];
      while (0 <= file && file < 8 && 0 <= rank && rank < 8) {
        final var square = square(file, rank);
        if (square == target) {
          return true;
        }
        if (contains(blockers, square)) {
          break;
        }
        file += delta[0];
        rank += delta[1];
      }
    }
    return false;
  }

  private static boolean contains(int[] squares, int square) {
    for (final var candidate : squares) {
      if (candidate == square) {
        return true;
      }
    }
    return false;
  }

  private static boolean knightAttacks(int knight, int target) {
    return Math.abs(file(knight) - file(target)) * Math.abs(rank(knight) - rank(target)) == 2;
  }

  private static boolean isLightSquare(int square) {
    return (file(square) + rank(square)) % 2 == 1;
  }

  private static int canonical(int[] pieces, boolean whiteToMove, int[] transformIndexes) {
    var result = Integer.MAX_VALUE;
    for (final var transformIndex : transformIndexes) {
      final var transformed = new int[pieces.length];
      for (var i = 0; i < pieces.length; i++) {
        transformed[i] = transform(pieces[i], transformIndex);
      }
      result = Math.min(result, encode(transformed, whiteToMove));
    }
    return result;
  }

  private static int encode(int[] pieces, boolean whiteToMove) {
    var result = 0;
    for (final var piece : pieces) {
      result = result * 64 + piece;
    }
    return result * 2 + (whiteToMove ? 0 : 1);
  }

  private static int transform(int square, int transformIndex) {
    final var file = file(square);
    final var rank = rank(square);
    return switch (transformIndex) {
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

  private static final class Counter {
    private final BitSet whiteToMove = new BitSet();
    private final BitSet blackToMove = new BitSet();
    private final BitSet all = new BitSet();
    private int rawWhiteToMove;
    private int rawBlackToMove;

    void addWhiteToMove(int[] pieces, int[] transformIndexes) {
      rawWhiteToMove++;
      add(whiteToMove, pieces, true, transformIndexes);
    }

    void addBlackToMove(int[] pieces, int[] transformIndexes) {
      rawBlackToMove++;
      add(blackToMove, pieces, false, transformIndexes);
    }

    Count count() {
      return new Count(rawWhiteToMove, rawBlackToMove, whiteToMove.cardinality(), blackToMove.cardinality(),
          all.cardinality());
    }

    private void add(BitSet side, int[] pieces, boolean isWhiteToMove, int[] transformIndexes) {
      final var canonical = canonical(pieces, isWhiteToMove, transformIndexes);
      side.set(canonical);
      all.set(canonical);
    }
  }

  private static final class IdenticalKnightCounter {
    private final BitSet whiteToMove = new BitSet();
    private final BitSet blackToMove = new BitSet();
    private final BitSet all = new BitSet();
    private int rawWhiteToMove;
    private int rawBlackToMove;

    void addWhiteToMove(int whiteKing, int whiteKnightA, int whiteKnightB, int blackKing) {
      rawWhiteToMove++;
      add(whiteToMove, whiteKing, whiteKnightA, whiteKnightB, blackKing, true);
    }

    void addBlackToMove(int whiteKing, int whiteKnightA, int whiteKnightB, int blackKing) {
      rawBlackToMove++;
      add(blackToMove, whiteKing, whiteKnightA, whiteKnightB, blackKing, false);
    }

    Count count() {
      return new Count(rawWhiteToMove, rawBlackToMove, whiteToMove.cardinality(), blackToMove.cardinality(),
          all.cardinality());
    }

    private void add(BitSet side, int whiteKing, int whiteKnightA, int whiteKnightB, int blackKing,
        boolean isWhiteToMove) {
      final var canonical = canonicalTwoKnights(whiteKing, whiteKnightA, whiteKnightB, blackKing, isWhiteToMove);
      side.set(canonical);
      all.set(canonical);
    }

    private static int canonicalTwoKnights(int whiteKing, int whiteKnightA, int whiteKnightB, int blackKing,
        boolean whiteToMove) {
      var result = Integer.MAX_VALUE;
      for (final var transformIndex : FULL_BOARD_SYMMETRIES) {
        final var transformedWhiteKing = transform(whiteKing, transformIndex);
        final var transformedKnightA = transform(whiteKnightA, transformIndex);
        final var transformedKnightB = transform(whiteKnightB, transformIndex);
        final var transformedBlackKing = transform(blackKing, transformIndex);
        result = Math.min(result, encode(new int[] {transformedWhiteKing,
            Math.min(transformedKnightA, transformedKnightB), Math.max(transformedKnightA, transformedKnightB),
            transformedBlackKing}, whiteToMove));
      }
      return result;
    }
  }

  private record Count(int rawWhiteToMove, int rawBlackToMove, int uniqueWhiteToMove, int uniqueBlackToMove,
      int uniqueTotal) {

    Count plus(Count other) {
      return new Count(rawWhiteToMove + other.rawWhiteToMove, rawBlackToMove + other.rawBlackToMove,
          uniqueWhiteToMove + other.uniqueWhiteToMove, uniqueBlackToMove + other.uniqueBlackToMove,
          uniqueTotal + other.uniqueTotal);
    }
  }
}
