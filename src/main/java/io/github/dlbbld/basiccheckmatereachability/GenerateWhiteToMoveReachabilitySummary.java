package io.github.dlbbld.basiccheckmatereachability;

import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpmateAnalysis.WhiteMajorPiece.QUEEN;
import static io.github.dlbbld.basiccheckmatereachability.BasicMajorPieceHelpmateAnalysis.WhiteMajorPiece.ROOK;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;

public class GenerateWhiteToMoveReachabilitySummary {

  private static final List<String> ORDERED_KBB_WHITE_TO_MOVE_EXCEPTIONS = List.of(
      "8/8/8/8/8/B7/B7/k1K5 w - - 0 1",
      "8/8/8/8/8/B7/B1K5/k7 w - - 0 1",
      "8/8/8/8/8/8/B1K5/k1B5 w - - 0 1");

  private static final List<String> ORDERED_KBN_WHITE_TO_MOVE_EXCEPTIONS = List.of(
      "8/8/8/8/8/8/B7/k1KN4 w - - 0 1",
      "8/8/8/8/8/3N4/B7/k1K5 w - - 0 1",
      "8/8/8/8/2N5/8/B7/k1K5 w - - 0 1",
      "8/8/8/8/N7/8/B7/k1K5 w - - 0 1",
      "8/8/8/8/8/8/BN6/k1K5 w - - 0 1",
      "8/8/8/8/8/8/2K5/kB1N4 w - - 0 1",
      "8/8/8/8/8/3N4/2K5/kB6 w - - 0 1",
      "8/8/8/8/2N5/8/2K5/kB6 w - - 0 1",
      "8/8/8/8/N7/8/2K5/kB6 w - - 0 1",
      "8/8/8/8/8/8/1NK5/kB6 w - - 0 1",
      "8/8/8/8/8/8/B1K5/k2N4 w - - 0 1",
      "8/8/8/8/8/3N4/B1K5/k7 w - - 0 1",
      "8/8/8/8/2N5/8/B1K5/k7 w - - 0 1",
      "8/8/8/8/N7/8/B1K5/k7 w - - 0 1",
      "8/8/8/8/8/8/BNK5/k7 w - - 0 1");

  public static void main(String[] args) {
    final var rook = BasicMajorPieceHelpmateAnalysis.analyze(ROOK);
    final var rookVerification = BasicMajorPieceHelpmateVerifier.verifyKrvK();
    final var rookTerminals = whiteTerminalCounts(ROOK);
    print("KRvK", rook.whiteToMoveStateCount(), rook.unwinnableWhiteToMoveStateCount(),
        rook.unwinnableWhiteToMoveRepresentatives().size());

    final var queen = BasicMajorPieceHelpmateAnalysis.analyze(QUEEN);
    final var queenVerification = BasicMajorPieceHelpmateVerifier.verifyKqvK();
    final var queenTerminals = whiteTerminalCounts(QUEEN);
    print("KQvK", queen.whiteToMoveStateCount(), queen.unwinnableWhiteToMoveStateCount(),
        queen.unwinnableWhiteToMoveRepresentatives().size());

    final var lightBishopKnight = BasicLightBishopKnightHelpmateAnalysis.analyze();
    final var lightBishopKnightVerification = BasicLightBishopKnightHelpmateVerifier.verify();
    final var lightBishopKnightTerminals = whiteTerminalCountsKbnvK();
    print("KBNvK(light bishop)", lightBishopKnight.whiteToMoveStateCount(),
        lightBishopKnight.unwinnableWhiteToMoveStateCount(),
        lightBishopKnight.unwinnableWhiteToMoveRepresentatives().size());

    final var oppositeBishops = BasicOppositeBishopsHelpmateAnalysis.analyze();
    final var oppositeBishopsVerification = BasicFourPieceHelpmateVerifier.verifyKbbvK();
    final var oppositeBishopsTerminals = whiteTerminalCountsKbbvK();
    print("KBBvK(opposite bishops)", oppositeBishops.whiteToMoveStateCount(),
        oppositeBishops.unwinnableWhiteToMoveStateCount(),
        oppositeBishops.unwinnableWhiteToMoveRepresentatives().size());

    final var rookLightBishop = BasicRookLightBishopHelpmateAnalysis.analyze();
    final var rookLightBishopVerification = BasicFourPieceHelpmateVerifier.verifyKrvKbLightBishop();
    final var rookLightBishopTerminals = whiteTerminalCountsKrvKb();
    print("KRvKB(light bishop)", rookLightBishop.whiteToMoveStateCount(),
        rookLightBishop.unwinnableWhiteToMoveStateCount(),
        rookLightBishop.unwinnableWhiteToMoveRepresentatives().size());
    printNonOngoingOrReducible(rookLightBishop.endedWhiteToMoveStateCount(),
        rookLightBishop.reducibleWhiteToMoveStateCount());

    final var rookKnight = BasicRookKnightHelpmateAnalysis.analyze();
    final var rookKnightVerification = BasicFourPieceHelpmateVerifier.verifyKrvKn();
    final var rookKnightTerminals = whiteTerminalCountsKrvKn();
    print("KRvKN", rookKnight.whiteToMoveStateCount(), rookKnight.unwinnableWhiteToMoveStateCount(),
        rookKnight.unwinnableWhiteToMoveRepresentatives().size());
    printNonOngoingOrReducible(rookKnight.endedWhiteToMoveStateCount(), rookKnight.reducibleWhiteToMoveStateCount());

    final var twoKnights = BasicTwoKnightsHelpmateAnalysis.analyze();
    final var twoKnightsVerification = BasicFourPieceHelpmateVerifier.verifyKnnvK();
    final var twoKnightsTerminals = whiteTerminalCountsKnnvK();
    print("KNNvK", twoKnights.whiteToMoveStateCount(), twoKnights.unwinnableWhiteToMoveStateCount(),
        twoKnights.unwinnableWhiteToMoveRepresentatives().size());

    System.out.println();
    System.out.println("White-to-move all potentially legal positions:");
    System.out.println(
        "| Material class | Potentially legal positions | Checkmates | Stalemates | Counterexamples | Maximum helpmate plies |");
    System.out.println("| --- | ---: | ---: | ---: | --- | ---: |");
    printWhiteTableRow("KRvK", rook.whiteToMoveStateCount(), rookTerminals.checkmates(),
        rookTerminals.stalemates(), rook.unwinnableWhiteToMoveStateCount(),
        rookVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KQvK", queen.whiteToMoveStateCount(), queenTerminals.checkmates(),
        queenTerminals.stalemates(), queen.unwinnableWhiteToMoveStateCount(),
        queenVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KBBvK, opposite bishops", oppositeBishops.whiteToMoveStateCount(),
        oppositeBishopsTerminals.checkmates(), oppositeBishopsTerminals.stalemates(),
        oppositeBishops.unwinnableWhiteToMoveStateCount(), oppositeBishopsVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KBNvK, light bishop", lightBishopKnight.whiteToMoveStateCount(),
        lightBishopKnightTerminals.checkmates(), lightBishopKnightTerminals.stalemates(),
        lightBishopKnight.unwinnableWhiteToMoveStateCount(),
        lightBishopKnightVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KRvKB(light bishop)", rookLightBishop.whiteToMoveStateCount(),
        rookLightBishopTerminals.checkmates(), rookLightBishopTerminals.stalemates(),
        rookLightBishop.unwinnableWhiteToMoveStateCount(), rookLightBishopVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KRvKN", rookKnight.whiteToMoveStateCount(), rookKnightTerminals.checkmates(),
        rookKnightTerminals.stalemates(), rookKnight.unwinnableWhiteToMoveStateCount(),
        rookKnightVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KNNvK", twoKnights.whiteToMoveStateCount(), twoKnightsTerminals.checkmates(),
        twoKnightsTerminals.stalemates(), twoKnights.unwinnableWhiteToMoveStateCount(),
        twoKnightsVerification.maximumWhiteToMoveDistance());

    System.out.println();
    System.out.println("White-to-move potentially legal positions reduced to representative cases:");
    System.out.println(
        "| Material class | Potentially legal positions | Checkmates | Stalemates | Counterexamples | Maximum helpmate plies |");
    System.out.println("| --- | ---: | ---: | ---: | --- | ---: |");
    printWhiteTableRow("KRvK", 21959, rookTerminals.checkmateRepresentatives(),
        rookTerminals.stalemateRepresentatives(), rook.unwinnableWhiteToMoveRepresentatives().size(),
        rookVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KQvK", 18081, queenTerminals.checkmateRepresentatives(),
        queenTerminals.stalemateRepresentatives(), queen.unwinnableWhiteToMoveRepresentatives().size(),
        queenVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KBBvK, opposite bishops", 626032, oppositeBishopsTerminals.checkmateRepresentatives(),
        oppositeBishopsTerminals.stalemateRepresentatives(),
        oppositeBishops.unwinnableWhiteToMoveRepresentatives().size(),
        oppositeBishopsVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KBNvK, light bishop", 1359578, lightBishopKnightTerminals.checkmateRepresentatives(),
        lightBishopKnightTerminals.stalemateRepresentatives(),
        lightBishopKnight.unwinnableWhiteToMoveRepresentatives().size(),
        lightBishopKnightVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KRvKB(light bishop)", 1347906, rookLightBishopTerminals.checkmateRepresentatives(),
        rookLightBishopTerminals.stalemateRepresentatives(),
        rookLightBishop.unwinnableWhiteToMoveRepresentatives().size(),
        rookLightBishopVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KRvKN", 1347906, rookKnightTerminals.checkmateRepresentatives(),
        rookKnightTerminals.stalemateRepresentatives(), rookKnight.unwinnableWhiteToMoveRepresentatives().size(),
        rookKnightVerification.maximumWhiteToMoveDistance());
    printWhiteTableRow("KNNvK", 719130, twoKnightsTerminals.checkmateRepresentatives(),
        twoKnightsTerminals.stalemateRepresentatives(), twoKnights.unwinnableWhiteToMoveRepresentatives().size(),
        twoKnightsVerification.maximumWhiteToMoveDistance());

    System.out.println();
    System.out.println("Black-to-move local exception:");
    printPositionRow("KBNvK(light bishop)", "8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1", "retro-illegal");

    System.out.println();
    System.out.println("| Material class | Representative position | Strict-game status |");
    System.out.println("|---|---|---|");

    printOrderedPositionRows("KBBvK, opposite bishops", ORDERED_KBB_WHITE_TO_MOVE_EXCEPTIONS,
        toOppositeBishopsFenSet(oppositeBishops.unwinnableWhiteToMoveRepresentatives()), "retro-illegal");
    printOrderedPositionRows("KBNvK, light bishop", ORDERED_KBN_WHITE_TO_MOVE_EXCEPTIONS,
        toLightBishopKnightFenSet(lightBishopKnight.unwinnableWhiteToMoveRepresentatives()), "retro-illegal");
    for (final var state : rookLightBishop.unwinnableWhiteToMoveRepresentatives()) {
      printPositionRow("KRvKB(light bishop)", toFen(state), "unexpected local exception");
    }
    for (final var state : rookKnight.unwinnableWhiteToMoveRepresentatives()) {
      printPositionRow("KRvKN", toFen(state), "unexpected local exception");
    }
    for (final var state : twoKnights.unwinnableWhiteToMoveRepresentatives()) {
      printPositionRow("KNNvK", BasicTwoKnightsHelpmateAnalysis.toFen(state), "unexpected local exception");
    }
  }

  private static void print(String materialClass, int whiteToMoveStates, int unwinnableWhiteToMoveStates,
      int canonicalUnwinnableWhiteToMoveStates) {
    System.out.printf("%s: white-to-move states %,d, unwinnable %,d, canonical %,d%n", materialClass,
        whiteToMoveStates, unwinnableWhiteToMoveStates, canonicalUnwinnableWhiteToMoveStates);
  }

  private static void printNonOngoingOrReducible(int endedWhiteToMoveStates, int reducibleWhiteToMoveStates) {
    System.out.printf("  ended white-to-move %,d, reducible by White capture %,d%n", endedWhiteToMoveStates,
        reducibleWhiteToMoveStates);
  }

  private static void printWhiteTableRow(String materialClass, int positions, int checkmates, int stalemates,
      int counterexamples, int maximumHelpmatePlies) {
    System.out.printf("| `%s` | %,d | %,d | %,d | %s | %,d |%n", materialClass, positions, checkmates, stalemates,
        counterexamples == 0 ? "0" : String.format("%,d illegal", counterexamples), maximumHelpmatePlies);
  }

  private static void printPositionRow(String materialClass, String fen, String strictGameStatus) {
    final String analysisUrl = lichessAnalysisUrl(fen);
    final String imageUrl = chessvisionImageUrl(fen);
    System.out.println("| `" + materialClass + "` | <a href=\"" + analysisUrl + "\"><img src=\""
        + imageUrl.replace("&", "&amp;") + "\" alt=\"" + fen
        + "\" width=\"180\"></a><br>`" + fen + "`<br>[Lichess analysis](" + analysisUrl + ") | "
        + strictGameStatus + " |");
  }

  private static void printOrderedPositionRows(String materialClass, List<String> orderedFens, Set<String> actualFens,
      String strictGameStatus) {
    if (!actualFens.equals(new HashSet<>(orderedFens))) {
      throw new IllegalStateException("ordered FEN list no longer matches " + materialClass);
    }
    for (final String fen : orderedFens) {
      printPositionRow(materialClass, fen, strictGameStatus);
    }
  }

  private static Set<String> toOppositeBishopsFenSet(
      Set<BasicOppositeBishopsHelpmateAnalysis.OppositeBishopsState> states) {
    final Set<String> result = new HashSet<>();
    for (final var state : states) {
      result.add(toFen(state));
    }
    return result;
  }

  private static Set<String> toLightBishopKnightFenSet(
      Set<BasicLightBishopKnightHelpmateAnalysis.LightBishopKnightState> states) {
    final Set<String> result = new HashSet<>();
    for (final var state : states) {
      result.add(toFen(state));
    }
    return result;
  }

  private static String lichessAnalysisUrl(String fen) {
    return "https://lichess.org/analysis/standard/" + fen.strip().replace(' ', '_');
  }

  private static String chessvisionImageUrl(String fen) {
    final String sideToMove = fen.split(" ")[1].equals("w") ? "white" : "black";
    return "https://fen2image.chessvision.ai/" + fen.strip().replace(" ", "%20") + "?turn=" + sideToMove
        + "&pov=" + sideToMove;
  }

  private static WhiteTerminalCounts whiteTerminalCounts(BasicMajorPieceHelpmateAnalysis.WhiteMajorPiece piece) {
    final var accumulator = new WhiteTerminalAccumulator(0, 1, 2, 3, 4, 5, 6, 7);
    for (final Square whiteKing : Square.REAL) {
      for (final Square whiteMajor : Square.REAL) {
        if (whiteMajor == whiteKing) {
          continue;
        }
        for (final Square blackKing : Square.REAL) {
          if (blackKing == whiteKing || blackKing == whiteMajor) {
            continue;
          }
          final long whiteRooks = piece == ROOK ? bit(whiteMajor) : 0L;
          final long whiteQueens = piece == QUEEN ? bit(whiteMajor) : 0L;
          final var position = new BitboardPosition(0L, whiteRooks, 0L, 0L, whiteQueens, bit(whiteKing), 0L, 0L,
              0L, 0L, 0L, bit(blackKing));
          accumulator.add(position, new PieceOnSquare('K', whiteKing), new PieceOnSquare(piece.fenChar(), whiteMajor),
              new PieceOnSquare('k', blackKing));
        }
      }
    }
    return accumulator.toCounts();
  }

  private static WhiteTerminalCounts whiteTerminalCountsKbbvK() {
    final var accumulator = new WhiteTerminalAccumulator(0, 1, 2, 3, 4, 5, 6, 7);
    for (final Square whiteKing : Square.REAL) {
      for (final Square whiteLightBishop : Square.REAL) {
        if (!isLightSquare(whiteLightBishop) || whiteLightBishop == whiteKing) {
          continue;
        }
        for (final Square whiteDarkBishop : Square.REAL) {
          if (isLightSquare(whiteDarkBishop) || whiteDarkBishop == whiteKing || whiteDarkBishop == whiteLightBishop) {
            continue;
          }
          for (final Square blackKing : Square.REAL) {
            if (blackKing == whiteKing || blackKing == whiteLightBishop || blackKing == whiteDarkBishop) {
              continue;
            }
            final var position = new BitboardPosition(0L, 0L, 0L,
                bit(whiteLightBishop) | bit(whiteDarkBishop), 0L, bit(whiteKing), 0L, 0L, 0L, 0L, 0L,
                bit(blackKing));
            accumulator.add(position, new PieceOnSquare('K', whiteKing), new PieceOnSquare('B', whiteLightBishop),
                new PieceOnSquare('B', whiteDarkBishop), new PieceOnSquare('k', blackKing));
          }
        }
      }
    }
    return accumulator.toCounts();
  }

  private static WhiteTerminalCounts whiteTerminalCountsKbnvK() {
    final var accumulator = new WhiteTerminalAccumulator(0, 3, 4, 7);
    for (final Square whiteKing : Square.REAL) {
      for (final Square whiteBishop : Square.REAL) {
        if (!isLightSquare(whiteBishop) || whiteBishop == whiteKing) {
          continue;
        }
        for (final Square whiteKnight : Square.REAL) {
          if (whiteKnight == whiteKing || whiteKnight == whiteBishop) {
            continue;
          }
          for (final Square blackKing : Square.REAL) {
            if (blackKing == whiteKing || blackKing == whiteBishop || blackKing == whiteKnight) {
              continue;
            }
            final var position = new BitboardPosition(0L, 0L, bit(whiteKnight), bit(whiteBishop), 0L,
                bit(whiteKing), 0L, 0L, 0L, 0L, 0L, bit(blackKing));
            accumulator.add(position, new PieceOnSquare('K', whiteKing), new PieceOnSquare('B', whiteBishop),
                new PieceOnSquare('N', whiteKnight), new PieceOnSquare('k', blackKing));
          }
        }
      }
    }
    return accumulator.toCounts();
  }

  private static WhiteTerminalCounts whiteTerminalCountsKrvKb() {
    final var accumulator = new WhiteTerminalAccumulator(0, 3, 4, 7);
    for (final Square whiteKing : Square.REAL) {
      for (final Square whiteRook : Square.REAL) {
        if (whiteRook == whiteKing) {
          continue;
        }
        for (final Square blackKing : Square.REAL) {
          if (blackKing == whiteKing || blackKing == whiteRook) {
            continue;
          }
          for (final Square blackBishop : Square.REAL) {
            if (!isLightSquare(blackBishop) || blackBishop == whiteKing || blackBishop == whiteRook
                || blackBishop == blackKing) {
              continue;
            }
            final var position = new BitboardPosition(0L, bit(whiteRook), 0L, 0L, 0L, bit(whiteKing), 0L, 0L, 0L,
                bit(blackBishop), 0L, bit(blackKing));
            accumulator.add(position, new PieceOnSquare('K', whiteKing), new PieceOnSquare('R', whiteRook),
                new PieceOnSquare('k', blackKing), new PieceOnSquare('b', blackBishop));
          }
        }
      }
    }
    return accumulator.toCounts();
  }

  private static WhiteTerminalCounts whiteTerminalCountsKrvKn() {
    final var accumulator = new WhiteTerminalAccumulator(0, 1, 2, 3, 4, 5, 6, 7);
    for (final Square whiteKing : Square.REAL) {
      for (final Square whiteRook : Square.REAL) {
        if (whiteRook == whiteKing) {
          continue;
        }
        for (final Square blackKing : Square.REAL) {
          if (blackKing == whiteKing || blackKing == whiteRook) {
            continue;
          }
          for (final Square blackKnight : Square.REAL) {
            if (blackKnight == whiteKing || blackKnight == whiteRook || blackKnight == blackKing) {
              continue;
            }
            final var position = new BitboardPosition(0L, bit(whiteRook), 0L, 0L, 0L, bit(whiteKing), 0L, 0L,
                bit(blackKnight), 0L, 0L, bit(blackKing));
            accumulator.add(position, new PieceOnSquare('K', whiteKing), new PieceOnSquare('R', whiteRook),
                new PieceOnSquare('k', blackKing), new PieceOnSquare('n', blackKnight));
          }
        }
      }
    }
    return accumulator.toCounts();
  }

  private static WhiteTerminalCounts whiteTerminalCountsKnnvK() {
    final var accumulator = new WhiteTerminalAccumulator(0, 1, 2, 3, 4, 5, 6, 7);
    for (final Square whiteKing : Square.REAL) {
      for (final Square whiteKnightA : Square.REAL) {
        if (whiteKnightA == whiteKing) {
          continue;
        }
        for (final Square whiteKnightB : Square.REAL) {
          if (whiteKnightB.ordinal() <= whiteKnightA.ordinal() || whiteKnightB == whiteKing) {
            continue;
          }
          for (final Square blackKing : Square.REAL) {
            if (blackKing == whiteKing || blackKing == whiteKnightA || blackKing == whiteKnightB) {
              continue;
            }
            final var position = new BitboardPosition(0L, 0L, bit(whiteKnightA) | bit(whiteKnightB), 0L, 0L,
                bit(whiteKing), 0L, 0L, 0L, 0L, 0L, bit(blackKing));
            accumulator.add(position, new PieceOnSquare('K', whiteKing), new PieceOnSquare('N', whiteKnightA),
                new PieceOnSquare('N', whiteKnightB), new PieceOnSquare('k', blackKing));
          }
        }
      }
    }
    return accumulator.toCounts();
  }

  private static boolean isLightSquare(Square square) {
    final var file = square.ordinal() % 8;
    final var rank = square.ordinal() / 8;
    return (file + rank) % 2 == 1;
  }

  private static long bit(Square square) {
    return 1L << square.ordinal();
  }

  private static String toFen(BasicOppositeBishopsHelpmateAnalysis.OppositeBishopsState state) {
    return toFen(Side.WHITE, new PieceOnSquare('K', state.whiteKing()),
        new PieceOnSquare('B', state.whiteLightBishop()), new PieceOnSquare('B', state.whiteDarkBishop()),
        new PieceOnSquare('k', state.blackKing()));
  }

  private static String toFen(BasicLightBishopKnightHelpmateAnalysis.LightBishopKnightState state) {
    return toFen(Side.WHITE, new PieceOnSquare('K', state.whiteKing()), new PieceOnSquare('B', state.whiteBishop()),
        new PieceOnSquare('N', state.whiteKnight()), new PieceOnSquare('k', state.blackKing()));
  }

  private static String toFen(BasicRookLightBishopHelpmateAnalysis.RookLightBishopState state) {
    return toFen(Side.WHITE, new PieceOnSquare('K', state.whiteKing()), new PieceOnSquare('R', state.whiteRook()),
        new PieceOnSquare('k', state.blackKing()), new PieceOnSquare('b', state.blackBishop()));
  }

  private static String toFen(BasicRookKnightHelpmateAnalysis.RookKnightState state) {
    return toFen(Side.WHITE, new PieceOnSquare('K', state.whiteKing()), new PieceOnSquare('R', state.whiteRook()),
        new PieceOnSquare('k', state.blackKing()), new PieceOnSquare('n', state.blackKnight()));
  }

  private static String toFen(Side havingMove, PieceOnSquare... pieces) {
    final char[] board = new char[64];
    for (int i = 0; i < board.length; i++) {
      board[i] = '1';
    }
    for (final PieceOnSquare piece : pieces) {
      board[piece.square().ordinal()] = piece.fenChar();
    }

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
    sb.append(' ').append(havingMove == Side.WHITE ? 'w' : 'b').append(" - - 0 1");
    return sb.toString();
  }

  private static PieceOnSquare transform(PieceOnSquare piece, int transformIndex) {
    return new PieceOnSquare(piece.fenChar(), transform(piece.square(), transformIndex));
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

  private static String canonicalFen(int[] transformIndexes, PieceOnSquare... pieces) {
    String result = null;
    for (final int transformIndex : transformIndexes) {
      final PieceOnSquare[] transformed = new PieceOnSquare[pieces.length];
      for (var i = 0; i < pieces.length; i++) {
        transformed[i] = transform(pieces[i], transformIndex);
      }
      final var fen = toFen(Side.WHITE, transformed);
      if (result == null || fen.compareTo(result) < 0) {
        result = fen;
      }
    }
    return result;
  }

  private static final class WhiteTerminalAccumulator {
    private final int[] transformIndexes;
    private int checkmates;
    private int stalemates;
    private final Set<String> checkmateRepresentatives = new HashSet<>();
    private final Set<String> stalemateRepresentatives = new HashSet<>();

    WhiteTerminalAccumulator(int... transformIndexes) {
      this.transformIndexes = transformIndexes.clone();
    }

    void add(BitboardPosition position, PieceOnSquare... pieces) {
      if (position.isInCheck(Side.BLACK) || !position.legalMoves(Side.WHITE, 0L).isEmpty()) {
        return;
      }
      if (position.isInCheck(Side.WHITE)) {
        checkmates++;
        checkmateRepresentatives.add(canonicalFen(transformIndexes, pieces));
      } else {
        stalemates++;
        stalemateRepresentatives.add(canonicalFen(transformIndexes, pieces));
      }
    }

    WhiteTerminalCounts toCounts() {
      return new WhiteTerminalCounts(checkmates, stalemates, checkmateRepresentatives.size(),
          stalemateRepresentatives.size());
    }
  }

  private record WhiteTerminalCounts(int checkmates, int stalemates, int checkmateRepresentatives,
      int stalemateRepresentatives) {
  }

  private record PieceOnSquare(char fenChar, Square square) {
  }
}
