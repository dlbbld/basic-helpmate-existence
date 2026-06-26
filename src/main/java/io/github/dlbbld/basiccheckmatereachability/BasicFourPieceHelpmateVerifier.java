package io.github.dlbbld.basiccheckmatereachability;

import java.util.Arrays;
import java.util.BitSet;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.board.MoveSpecification;

final class BasicFourPieceHelpmateVerifier {

  private static final int WHITE_TO_MOVE = 0;
  private static final int BLACK_TO_MOVE = 1;
  private static final int NO_WITNESS = -1;
  private static final int SQUARE_COUNT = 64;
  private static final int STATE_COUNT = SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * SQUARE_COUNT * 2;
  private static final int HAS_LEGAL_BLACK_MOVE = 1;
  private static final int HAS_MATERIAL_PRESERVING_BLACK_MOVE = 2;

  private static final int[][] KING_DELTAS = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 },
      { 1, 0 }, { 1, 1 } };

  private static final int[][] ROOK_DELTAS = { { -1, 0 }, { 0, -1 }, { 0, 1 }, { 1, 0 } };

  private static final int[][] BISHOP_DELTAS = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };

  private static final int[][] KNIGHT_DELTAS = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 },
      { 2, -1 }, { 2, 1 } };

  private BasicFourPieceHelpmateVerifier() {
  }

  static VerificationResult verifyKbbvK() {
    return verify(Material.OPPOSITE_BISHOPS);
  }

  static VerificationResult verifyKrvKbLightBishop() {
    return verify(Material.ROOK_LIGHT_BISHOP);
  }

  static VerificationResult verifyKrvKn() {
    return verify(Material.ROOK_KNIGHT);
  }

  private static VerificationResult verify(Material material) {
    final var legalStates = new BitSet(STATE_COUNT);
    final var checkmates = new BitSet(STATE_COUNT);
    final var winning = new BitSet(STATE_COUNT);
    final var witnessByState = new int[STATE_COUNT];
    final var distanceByState = new byte[STATE_COUNT];
    final var queue = new IntQueue();

    Arrays.fill(witnessByState, NO_WITNESS);

    var legalStateCount = 0;
    var terminalCheckmateCount = 0;
    for (var first = 0; first < SQUARE_COUNT; first++) {
      for (var second = 0; second < SQUARE_COUNT; second++) {
        for (var third = 0; third < SQUARE_COUNT; third++) {
          for (var fourth = 0; fourth < SQUARE_COUNT; fourth++) {
            if (!material.isValidPlacement(first, second, third, fourth)) {
              continue;
            }
            for (var havingMove = WHITE_TO_MOVE; havingMove <= BLACK_TO_MOVE; havingMove++) {
              final var state = encode(first, second, third, fourth, havingMove);
              if (!material.isLegalState(first, second, third, fourth, havingMove)) {
                continue;
              }
              legalStates.set(state);
              legalStateCount++;
              if (havingMove == BLACK_TO_MOVE && material.isBlackInCheck(first, second, third, fourth)
                  && material.blackMoveMask(first, second, third, fourth) == 0) {
                checkmates.set(state);
                winning.set(state);
                queue.add(state);
                terminalCheckmateCount++;
              }
            }
          }
        }
      }
    }

    final var maximumDistance = calculateCertificate(material, legalStates, winning, witnessByState, distanceByState,
        queue);
    final var verifiedTerminalCount = verifyTerminals(material, checkmates);
    final var verifiedWitnessCount = verifyWitnesses(material, winning, witnessByState, distanceByState);
    final var verifiedTheoremRootCount = verifyTheoremRoots(material, legalStates, checkmates, winning);

    return new VerificationResult(material, legalStateCount, terminalCheckmateCount, winning.cardinality(),
        verifiedWitnessCount, verifiedTerminalCount, verifiedWitnessCount, verifiedTheoremRootCount,
        maximumDistanceForHavingMove(winning, distanceByState, WHITE_TO_MOVE), maximumDistance);
  }

  private static int calculateCertificate(Material material, BitSet legalStates, BitSet winning, int[] witnessByState,
      byte[] distanceByState, IntQueue queue) {
    var maximumDistance = 0;
    while (!queue.isEmpty()) {
      final var state = queue.remove();
      final var predecessorDistance = Byte.toUnsignedInt(distanceByState[state]) + 1;
      if (havingMove(state) == BLACK_TO_MOVE) {
        maximumDistance = Math.max(maximumDistance, material.addWhitePredecessors(legalStates, winning, witnessByState,
            distanceByState, queue, state, predecessorDistance));
      } else {
        maximumDistance = Math.max(maximumDistance, material.addBlackPredecessors(legalStates, winning, witnessByState,
            distanceByState, queue, state, predecessorDistance));
      }
    }
    return maximumDistance;
  }

  private static int addPredecessor(BitSet legalStates, BitSet winning, int[] witnessByState, byte[] distanceByState,
      IntQueue queue, int witnessTarget, int first, int second, int third, int fourth, int havingMove,
      int predecessorDistance) {
    final var predecessor = encode(first, second, third, fourth, havingMove);
    if (legalStates.get(predecessor) && !winning.get(predecessor)) {
      winning.set(predecessor);
      witnessByState[predecessor] = witnessTarget;
      distanceByState[predecessor] = (byte) predecessorDistance;
      queue.add(predecessor);
      return predecessorDistance;
    }
    return 0;
  }

  private static int verifyTerminals(Material material, BitSet checkmates) {
    var result = 0;
    for (var state = checkmates.nextSetBit(0); state >= 0; state = checkmates.nextSetBit(state + 1)) {
      final var position = material.toBitboardPosition(state);
      if (havingMove(state) != BLACK_TO_MOVE || !position.isInCheck(Side.BLACK)
          || !position.legalMoves(Side.BLACK, 0L).isEmpty()) {
        throw new AssertionError("Terminal is not a verified black checkmate: " + state);
      }
      result++;
    }
    return result;
  }

  private static int verifyWitnesses(Material material, BitSet winning, int[] witnessByState, byte[] distanceByState) {
    var result = 0;
    for (var state = winning.nextSetBit(0); state >= 0; state = winning.nextSetBit(state + 1)) {
      final var target = witnessByState[state];
      if (target == NO_WITNESS) {
        continue;
      }
      final var witness = Material.moveBetween(state, target);
      final var legalMoves = material.toBitboardPosition(state).legalMoves(side(havingMove(state)), 0L);
      if (!legalMoves.contains(witness)) {
        throw new AssertionError("Witness is not legal: " + state + " " + witness);
      }
      if (material.capturesWhiteMaterial(state, witness)) {
        throw new AssertionError("Witness leaves the material class: " + state + " " + witness);
      }
      if (!winning.get(target)) {
        throw new AssertionError("Witness target is not winning: " + state + " " + witness);
      }
      if (Byte.toUnsignedInt(distanceByState[target]) >= Byte.toUnsignedInt(distanceByState[state])) {
        throw new AssertionError("Witness does not descend toward mate: " + state + " " + witness);
      }
      result++;
    }
    return result;
  }

  private static int verifyTheoremRoots(Material material, BitSet legalStates, BitSet checkmates, BitSet winning) {
    var result = 0;
    for (var state = legalStates.nextSetBit(0); state >= 0; state = legalStates.nextSetBit(state + 1)) {
      if (havingMove(state) != BLACK_TO_MOVE || checkmates.get(state)) {
        continue;
      }
      final var blackMoveMask = material.blackMoveMask(first(state), second(state), third(state), fourth(state));
      final var hasLegalMove = (blackMoveMask & HAS_LEGAL_BLACK_MOVE) != 0;
      final var hasMaterialPreservingMove = (blackMoveMask & HAS_MATERIAL_PRESERVING_BLACK_MOVE) != 0;
      if (!hasLegalMove) {
        if (material.isBlackInCheck(first(state), second(state), third(state), fourth(state))) {
          throw new AssertionError("Non-terminal black-to-move state has no legal moves while in check: " + state);
        }
        continue;
      }
      if (hasMaterialPreservingMove && winning.get(state)) {
        result++;
      }
    }
    return result;
  }

  private static int maximumDistanceForHavingMove(BitSet winning, byte[] distanceByState, int havingMove) {
    var result = 0;
    for (var state = winning.nextSetBit(0); state >= 0; state = winning.nextSetBit(state + 1)) {
      if (havingMove(state) == havingMove) {
        result = Math.max(result, Byte.toUnsignedInt(distanceByState[state]));
      }
    }
    return result;
  }

  private static int encode(int first, int second, int third, int fourth, int havingMove) {
    return (((first * SQUARE_COUNT + second) * SQUARE_COUNT + third) * SQUARE_COUNT + fourth) * 2 + havingMove;
  }

  private static int first(int state) {
    return state >>> 19;
  }

  private static int second(int state) {
    return state >>> 13 & 0x3F;
  }

  private static int third(int state) {
    return state >>> 7 & 0x3F;
  }

  private static int fourth(int state) {
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

  private static boolean areKingsAdjacent(int whiteKing, int blackKing) {
    return Math.abs(file(whiteKing) - file(blackKing)) <= 1 && Math.abs(rank(whiteKing) - rank(blackKing)) <= 1;
  }

  private static boolean isRookAttack(int whiteRook, int blockerA, int blockerB, int blackKing) {
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
      if (square == blockerA || square == blockerB) {
        return false;
      }
      file += fileStep;
      rank += rankStep;
    }
  }

  private static boolean isBishopAttack(int bishop, int blockerA, int blockerB, int target) {
    if (bishop == -1) {
      return false;
    }
    final var fileDistance = file(target) - file(bishop);
    final var rankDistance = rank(target) - rank(bishop);
    if (Math.abs(fileDistance) != Math.abs(rankDistance)) {
      return false;
    }
    final var fileStep = Integer.signum(fileDistance);
    final var rankStep = Integer.signum(rankDistance);
    var file = file(bishop) + fileStep;
    var rank = rank(bishop) + rankStep;
    while (true) {
      final var square = square(file, rank);
      if (square == target) {
        return true;
      }
      if (square == blockerA || square == blockerB) {
        return false;
      }
      file += fileStep;
      rank += rankStep;
    }
  }

  private static boolean isKnightAttack(int knight, int target) {
    if (knight == -1) {
      return false;
    }
    final var fileDistance = Math.abs(file(knight) - file(target));
    final var rankDistance = Math.abs(rank(knight) - rank(target));
    return fileDistance * rankDistance == 2;
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

  private static long bit(int square) {
    return 1L << square;
  }

  enum Material {
    OPPOSITE_BISHOPS("KBBvK"),
    ROOK_LIGHT_BISHOP("KRvKB(light bishop)"),
    ROOK_KNIGHT("KRvKN");

    private final String name;

    Material(String name) {
      this.name = name;
    }

    String materialName() {
      return name;
    }

    boolean isValidPlacement(int first, int second, int third, int fourth) {
      if (first == second || first == third || first == fourth || second == third || second == fourth
          || third == fourth) {
        return false;
      }
      return switch (this) {
        case OPPOSITE_BISHOPS -> isLightSquare(second) && !isLightSquare(third);
        case ROOK_LIGHT_BISHOP -> isLightSquare(fourth);
        case ROOK_KNIGHT -> true;
      };
    }

    boolean isLegalState(int first, int second, int third, int fourth, int havingMove) {
      return switch (this) {
        case OPPOSITE_BISHOPS -> switch (havingMove) {
          case WHITE_TO_MOVE -> !isBlackInCheck(first, second, third, fourth);
          case BLACK_TO_MOVE -> !areKingsAdjacent(first, fourth);
          default -> throw new IllegalArgumentException();
        };
        case ROOK_LIGHT_BISHOP, ROOK_KNIGHT -> switch (havingMove) {
          case WHITE_TO_MOVE -> !isBlackInCheck(first, second, third, fourth);
          case BLACK_TO_MOVE -> !isWhiteInCheck(first, second, third, fourth);
          default -> throw new IllegalArgumentException();
        };
      };
    }

    boolean isBlackInCheck(int first, int second, int third, int fourth) {
      return switch (this) {
        case OPPOSITE_BISHOPS -> areKingsAdjacent(first, fourth) || isBishopAttack(second, third, first, fourth)
            || isBishopAttack(third, second, first, fourth);
        case ROOK_LIGHT_BISHOP -> areKingsAdjacent(first, third) || isRookAttack(second, first, fourth, third);
        case ROOK_KNIGHT -> areKingsAdjacent(first, third) || isRookAttack(second, first, fourth, third);
      };
    }

    private boolean isWhiteInCheck(int first, int second, int third, int fourth) {
      return switch (this) {
        case OPPOSITE_BISHOPS -> throw new IllegalStateException();
        case ROOK_LIGHT_BISHOP -> areKingsAdjacent(first, third) || isBishopAttack(fourth, third, second, first);
        case ROOK_KNIGHT -> areKingsAdjacent(first, third) || isKnightAttack(fourth, first);
      };
    }

    int blackMoveMask(int first, int second, int third, int fourth) {
      return switch (this) {
        case OPPOSITE_BISHOPS -> blackMoveMaskKbbvK(first, second, third, fourth);
        case ROOK_LIGHT_BISHOP -> blackMoveMaskKrvKb(first, second, third, fourth);
        case ROOK_KNIGHT -> blackMoveMaskKrvKn(first, second, third, fourth);
      };
    }

    private int blackMoveMaskKbbvK(int whiteKing, int lightBishop, int darkBishop, int blackKing) {
      var result = 0;
      for (final int[] delta : KING_DELTAS) {
        final var target = offset(blackKing, delta);
        if (target == -1 || target == whiteKing) {
          continue;
        }
        final var capturesLight = target == lightBishop;
        final var capturesDark = target == darkBishop;
        final var lightAfterMove = capturesLight ? -1 : lightBishop;
        final var darkAfterMove = capturesDark ? -1 : darkBishop;
        if (!isBlackInCheck(whiteKing, lightAfterMove, darkAfterMove, target)) {
          result |= HAS_LEGAL_BLACK_MOVE;
          if (!capturesLight && !capturesDark) {
            result |= HAS_MATERIAL_PRESERVING_BLACK_MOVE;
          }
        }
      }
      return result;
    }

    private int blackMoveMaskKrvKb(int whiteKing, int whiteRook, int blackKing, int blackBishop) {
      var result = 0;
      for (final int[] delta : KING_DELTAS) {
        final var target = offset(blackKing, delta);
        if (target == -1 || target == whiteKing || target == blackBishop) {
          continue;
        }
        final var capturesRook = target == whiteRook;
        final var rookAfterMove = capturesRook ? -1 : whiteRook;
        if (!isBlackInCheck(whiteKing, rookAfterMove, target, blackBishop)) {
          result |= HAS_LEGAL_BLACK_MOVE;
          if (!capturesRook) {
            result |= HAS_MATERIAL_PRESERVING_BLACK_MOVE;
          }
        }
      }
      for (final int[] delta : BISHOP_DELTAS) {
        var target = offset(blackBishop, delta);
        while (target != -1) {
          if (target == whiteKing || target == blackKing) {
            break;
          }
          final var capturesRook = target == whiteRook;
          final var rookAfterMove = capturesRook ? -1 : whiteRook;
          if (!isBlackInCheck(whiteKing, rookAfterMove, blackKing, target)) {
            result |= HAS_LEGAL_BLACK_MOVE;
            if (!capturesRook) {
              result |= HAS_MATERIAL_PRESERVING_BLACK_MOVE;
            }
          }
          if (capturesRook) {
            break;
          }
          target = offset(target, delta);
        }
      }
      return result;
    }

    private int blackMoveMaskKrvKn(int whiteKing, int whiteRook, int blackKing, int blackKnight) {
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

    int addWhitePredecessors(BitSet legalStates, BitSet winning, int[] witnessByState, byte[] distanceByState,
        IntQueue queue, int state, int predecessorDistance) {
      return switch (this) {
        case OPPOSITE_BISHOPS -> addWhitePredecessorsKbbvK(legalStates, winning, witnessByState, distanceByState, queue,
            state, predecessorDistance);
        case ROOK_LIGHT_BISHOP, ROOK_KNIGHT -> addWhitePredecessorsKrvKx(legalStates, winning, witnessByState,
            distanceByState, queue, state, predecessorDistance);
      };
    }

    private static int addWhitePredecessorsKbbvK(BitSet legalStates, BitSet winning, int[] witnessByState,
        byte[] distanceByState, IntQueue queue, int state, int predecessorDistance) {
      final var whiteKing = first(state);
      final var lightBishop = second(state);
      final var darkBishop = third(state);
      final var blackKing = fourth(state);
      var result = 0;
      for (final int[] delta : KING_DELTAS) {
        final var origin = offset(whiteKing, delta);
        if (origin != -1 && origin != lightBishop && origin != darkBishop && origin != blackKing) {
          result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
              origin, lightBishop, darkBishop, blackKing, WHITE_TO_MOVE, predecessorDistance));
        }
      }
      result = Math.max(result, addBishopPredecessors(legalStates, winning, witnessByState, distanceByState, queue,
          state, whiteKing, lightBishop, darkBishop, blackKing, true, predecessorDistance));
      result = Math.max(result, addBishopPredecessors(legalStates, winning, witnessByState, distanceByState, queue,
          state, whiteKing, lightBishop, darkBishop, blackKing, false, predecessorDistance));
      return result;
    }

    private static int addBishopPredecessors(BitSet legalStates, BitSet winning, int[] witnessByState,
        byte[] distanceByState, IntQueue queue, int state, int whiteKing, int lightBishop, int darkBishop,
        int blackKing, boolean moveLightBishop, int predecessorDistance) {
      final var destination = moveLightBishop ? lightBishop : darkBishop;
      final var otherBishop = moveLightBishop ? darkBishop : lightBishop;
      var result = 0;
      for (final int[] delta : BISHOP_DELTAS) {
        var origin = offset(destination, delta);
        while (origin != -1) {
          if (origin == whiteKing || origin == otherBishop || origin == blackKing) {
            break;
          }
          if (moveLightBishop) {
            result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue,
                state, whiteKing, origin, darkBishop, blackKing, WHITE_TO_MOVE, predecessorDistance));
          } else {
            result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue,
                state, whiteKing, lightBishop, origin, blackKing, WHITE_TO_MOVE, predecessorDistance));
          }
          origin = offset(origin, delta);
        }
      }
      return result;
    }

    private static int addWhitePredecessorsKrvKx(BitSet legalStates, BitSet winning, int[] witnessByState,
        byte[] distanceByState, IntQueue queue, int state, int predecessorDistance) {
      final var whiteKing = first(state);
      final var whiteRook = second(state);
      final var blackKing = third(state);
      final var blackPiece = fourth(state);
      var result = 0;
      for (final int[] delta : KING_DELTAS) {
        final var origin = offset(whiteKing, delta);
        if (origin != -1 && origin != whiteRook && origin != blackKing && origin != blackPiece) {
          result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
              origin, whiteRook, blackKing, blackPiece, WHITE_TO_MOVE, predecessorDistance));
        }
      }
      for (final int[] delta : ROOK_DELTAS) {
        var origin = offset(whiteRook, delta);
        while (origin != -1) {
          if (origin == whiteKing || origin == blackKing || origin == blackPiece) {
            break;
          }
          result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
              whiteKing, origin, blackKing, blackPiece, WHITE_TO_MOVE, predecessorDistance));
          origin = offset(origin, delta);
        }
      }
      return result;
    }

    int addBlackPredecessors(BitSet legalStates, BitSet winning, int[] witnessByState, byte[] distanceByState,
        IntQueue queue, int state, int predecessorDistance) {
      return switch (this) {
        case OPPOSITE_BISHOPS -> addBlackKingPredecessors(legalStates, winning, witnessByState, distanceByState, queue,
            state, predecessorDistance, first(state), second(state), third(state), fourth(state));
        case ROOK_LIGHT_BISHOP -> addBlackPredecessorsKrvKb(legalStates, winning, witnessByState, distanceByState,
            queue, state, predecessorDistance);
        case ROOK_KNIGHT -> addBlackPredecessorsKrvKn(legalStates, winning, witnessByState, distanceByState, queue,
            state, predecessorDistance);
      };
    }

    private static int addBlackKingPredecessors(BitSet legalStates, BitSet winning, int[] witnessByState,
        byte[] distanceByState, IntQueue queue, int state, int predecessorDistance, int whiteKing, int secondPiece,
        int thirdPiece, int blackKing) {
      var result = 0;
      for (final int[] delta : KING_DELTAS) {
        final var origin = offset(blackKing, delta);
        if (origin != -1 && origin != whiteKing && origin != secondPiece && origin != thirdPiece) {
          result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
              whiteKing, secondPiece, thirdPiece, origin, BLACK_TO_MOVE, predecessorDistance));
        }
      }
      return result;
    }

    private static int addBlackPredecessorsKrvKb(BitSet legalStates, BitSet winning, int[] witnessByState,
        byte[] distanceByState, IntQueue queue, int state, int predecessorDistance) {
      final var whiteKing = first(state);
      final var whiteRook = second(state);
      final var blackKing = third(state);
      final var blackBishop = fourth(state);
      var result = addBlackKingPredecessorsKrvKx(legalStates, winning, witnessByState, distanceByState, queue, state,
          predecessorDistance, whiteKing, whiteRook, blackKing, blackBishop);
      for (final int[] delta : BISHOP_DELTAS) {
        var origin = offset(blackBishop, delta);
        while (origin != -1) {
          if (origin == whiteKing || origin == whiteRook || origin == blackKing) {
            break;
          }
          result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
              whiteKing, whiteRook, blackKing, origin, BLACK_TO_MOVE, predecessorDistance));
          origin = offset(origin, delta);
        }
      }
      return result;
    }

    private static int addBlackPredecessorsKrvKn(BitSet legalStates, BitSet winning, int[] witnessByState,
        byte[] distanceByState, IntQueue queue, int state, int predecessorDistance) {
      final var whiteKing = first(state);
      final var whiteRook = second(state);
      final var blackKing = third(state);
      final var blackKnight = fourth(state);
      var result = addBlackKingPredecessorsKrvKx(legalStates, winning, witnessByState, distanceByState, queue, state,
          predecessorDistance, whiteKing, whiteRook, blackKing, blackKnight);
      for (final int[] delta : KNIGHT_DELTAS) {
        final var origin = offset(blackKnight, delta);
        if (origin != -1 && origin != whiteKing && origin != whiteRook && origin != blackKing) {
          result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
              whiteKing, whiteRook, blackKing, origin, BLACK_TO_MOVE, predecessorDistance));
        }
      }
      return result;
    }

    private static int addBlackKingPredecessorsKrvKx(BitSet legalStates, BitSet winning, int[] witnessByState,
        byte[] distanceByState, IntQueue queue, int state, int predecessorDistance, int whiteKing, int whiteRook,
        int blackKing, int blackPiece) {
      var result = 0;
      for (final int[] delta : KING_DELTAS) {
        final var origin = offset(blackKing, delta);
        if (origin != -1 && origin != whiteKing && origin != whiteRook && origin != blackPiece) {
          result = Math.max(result, addPredecessor(legalStates, winning, witnessByState, distanceByState, queue, state,
              whiteKing, whiteRook, origin, blackPiece, BLACK_TO_MOVE, predecessorDistance));
        }
      }
      return result;
    }

    static MoveSpecification moveBetween(int source, int target) {
      if (first(source) != first(target)) {
        return new MoveSpecification(square(first(source)), square(first(target)));
      }
      if (second(source) != second(target)) {
        return new MoveSpecification(square(second(source)), square(second(target)));
      }
      if (third(source) != third(target)) {
        return new MoveSpecification(square(third(source)), square(third(target)));
      }
      if (fourth(source) != fourth(target)) {
        return new MoveSpecification(square(fourth(source)), square(fourth(target)));
      }
      throw new AssertionError("States are not connected by one material-preserving move: " + source + " " + target);
    }

    boolean capturesWhiteMaterial(int state, MoveSpecification move) {
      return switch (this) {
        case OPPOSITE_BISHOPS -> havingMove(state) == BLACK_TO_MOVE
            && (move.toSquare().ordinal() == second(state) || move.toSquare().ordinal() == third(state));
        case ROOK_LIGHT_BISHOP, ROOK_KNIGHT -> havingMove(state) == BLACK_TO_MOVE
            && move.toSquare().ordinal() == second(state);
      };
    }

    BitboardPosition toBitboardPosition(int state) {
      return switch (this) {
        case OPPOSITE_BISHOPS -> new BitboardPosition(0L, 0L, 0L, bit(second(state)) | bit(third(state)), 0L,
            bit(first(state)), 0L, 0L, 0L, 0L, 0L, bit(fourth(state)));
        case ROOK_LIGHT_BISHOP -> new BitboardPosition(0L, bit(second(state)), 0L, 0L, 0L, bit(first(state)), 0L, 0L,
            0L, bit(fourth(state)), 0L, bit(third(state)));
        case ROOK_KNIGHT -> new BitboardPosition(0L, bit(second(state)), 0L, 0L, 0L, bit(first(state)), 0L, 0L,
            bit(fourth(state)), 0L, 0L, bit(third(state)));
      };
    }
  }

  record VerificationResult(Material material, int legalStateCount, int terminalCheckmateCount, int winningStateCount,
      int witnessStateCount, int verifiedTerminalCount, int verifiedWitnessCount, int verifiedTheoremRootCount,
      int maximumWhiteToMoveDistance, int maximumDistance) {
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
      final var grown = new int[elements.length * 2];
      System.arraycopy(elements, 0, grown, 0, elements.length);
      elements = grown;
    }
  }
}
