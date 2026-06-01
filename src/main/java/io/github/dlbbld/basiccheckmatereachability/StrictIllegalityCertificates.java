package io.github.dlbbld.basiccheckmatereachability;

import java.util.Arrays;
import java.util.List;

import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.common.Nulls;

final class StrictIllegalityCertificates {

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

  private static final int[][] ROOK_DELTAS = {
      {-1, 0},
      {0, -1},
      {0, 1},
      {1, 0}
  };

  private static final int[][] QUEEN_DELTAS = {
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

  private StrictIllegalityCertificates() {
  }

  static boolean noPossibleLastBlackKingMove(Position position) {
    if (position.sideToMove() != Side.WHITE) {
      return false;
    }

    final var blackKing = position.squareOf('k');
    final var whiteKing = position.squareOf('K');
    for (final int[] delta : KING_DELTAS) {
      final var source = offset(blackKing, delta);
      if (source == -1 || position.isOccupied(source) || areAdjacent(source, whiteKing)) {
        continue;
      }
      if (!position.isAttackedByWhite(source, blackKing)) {
        return false;
      }
    }
    return true;
  }

  static boolean noPossibleLastAdjacentCheckingPieceMove(Position position) {
    if (position.sideToMove() != Side.BLACK) {
      return false;
    }

    final var attackers = position.whiteAttackersToBlackKing();
    if (attackers.size() != 1) {
      return false;
    }

    final var checker = attackers.get(0);
    final var blackKing = position.squareOf('k');
    if (!isSlidingPiece(checker.fenChar()) || !areAdjacent(checker.square().ordinal(), blackKing)) {
      return false;
    }
    return !position.hasSourceSquareForSlidingPiece(checker);
  }

  static Position position(Side sideToMove, Piece... pieces) {
    return new Position(sideToMove, Arrays.asList(pieces));
  }

  static Piece piece(char fenChar, Square square) {
    return new Piece(fenChar, square);
  }

  private static boolean isSlidingPiece(char fenChar) {
    return switch (Character.toUpperCase(fenChar)) {
      case 'B', 'R', 'Q' -> true;
      default -> false;
    };
  }

  private static boolean attacks(char fenChar, int from, int target, Position position) {
    return switch (Character.toUpperCase(fenChar)) {
      case 'K' -> areAdjacent(from, target);
      case 'N' -> isKnightAttack(from, target);
      case 'B' -> isSlidingAttack(from, target, BISHOP_DELTAS, position);
      case 'R' -> isSlidingAttack(from, target, ROOK_DELTAS, position);
      case 'Q' -> isSlidingAttack(from, target, QUEEN_DELTAS, position);
      case 'P' -> isWhitePawnAttack(from, target);
      default -> false;
    };
  }

  private static boolean isSlidingAttack(int from, int target, int[][] deltas, Position position) {
    for (final int[] delta : deltas) {
      var square = offset(from, delta);
      while (square != -1) {
        if (square == target) {
          return true;
        }
        if (position.isOccupied(square)) {
          break;
        }
        square = offset(square, delta);
      }
    }
    return false;
  }

  private static boolean isKnightAttack(int from, int target) {
    final var fileDistance = Math.abs(file(from) - file(target));
    final var rankDistance = Math.abs(rank(from) - rank(target));
    return fileDistance * rankDistance == 2;
  }

  private static boolean isWhitePawnAttack(int from, int target) {
    return rank(target) == rank(from) + 1 && Math.abs(file(target) - file(from)) == 1;
  }

  private static boolean areAdjacent(int first, int second) {
    return Math.abs(file(first) - file(second)) <= 1 && Math.abs(rank(first) - rank(second)) <= 1;
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

  record Position(Side sideToMove, List<Piece> pieces) {

    Position {
      pieces = List.copyOf(pieces);
    }

    int squareOf(char fenChar) {
      for (final var piece : pieces) {
        if (piece.fenChar() == fenChar) {
          return piece.square().ordinal();
        }
      }
      throw new IllegalArgumentException("missing piece: " + fenChar);
    }

    boolean isOccupied(int square) {
      for (final var piece : pieces) {
        if (piece.square().ordinal() == square) {
          return true;
        }
      }
      return false;
    }

    boolean isAttackedByWhite(int square, int forcedBlocker) {
      final var withForcedBlocker = withPiece(new Piece('#', Nulls.get(Square.REAL, forcedBlocker)));
      for (final var piece : pieces) {
        if (Character.isUpperCase(piece.fenChar()) && attacks(piece.fenChar(), piece.square().ordinal(), square,
            withForcedBlocker)) {
          return true;
        }
      }
      return false;
    }

    List<Piece> whiteAttackersToBlackKing() {
      final var blackKing = squareOf('k');
      return pieces.stream()
          .filter(piece -> Character.isUpperCase(piece.fenChar()))
          .filter(piece -> attacks(piece.fenChar(), piece.square().ordinal(), blackKing, this))
          .toList();
    }

    boolean hasSourceSquareForSlidingPiece(Piece piece) {
      final var deltas = switch (Character.toUpperCase(piece.fenChar())) {
        case 'B' -> BISHOP_DELTAS;
        case 'R' -> ROOK_DELTAS;
        case 'Q' -> QUEEN_DELTAS;
        default -> throw new IllegalArgumentException();
      };

      for (final int[] delta : deltas) {
        var source = offset(piece.square().ordinal(), delta);
        while (source != -1) {
          if (isOccupied(source)) {
            break;
          }
          return true;
        }
      }
      return false;
    }

    private Position withPiece(Piece extraPiece) {
      if (isOccupied(extraPiece.square().ordinal())) {
        return this;
      }

      final var extended = new Piece[pieces.size() + 1];
      for (var i = 0; i < pieces.size(); i++) {
        extended[i] = pieces.get(i);
      }
      extended[pieces.size()] = extraPiece;
      return new Position(sideToMove, Arrays.asList(extended));
    }
  }

  record Piece(char fenChar, Square square) {
  }
}
