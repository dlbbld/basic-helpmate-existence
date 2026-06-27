package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.github.dlbbld.ashlarchess.bitboard.BitboardPosition;
import io.github.dlbbld.ashlarchess.board.Board;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.fen.StrictFenParser;

class TestForcedFirstCaptureSeedReachability {

  private static final List<SeedCase> SEED_CASES = List.of(
      new SeedCase(Material.KRVK, "4k3/8/8/8/8/8/8/R3K3 w - - 0 1", "KRvK.pgn", List.of(
          "8/8/8/8/8/8/3R4/K1k5 b - - 0 1")),
      new SeedCase(Material.KQVK, "4k3/8/8/8/8/8/8/3QK3 w - - 0 1", "KQvK.pgn", List.of(
          "8/8/8/8/8/8/8/K1kQ4 b - - 0 1")),
      new SeedCase(Material.KBBVK, "4k3/8/8/8/8/8/8/2B1KB2 w - - 0 1", "KBBvK.pgn", List.of(
          "8/8/8/8/8/8/8/K1kBB3 b - - 0 1",
          "8/8/8/8/8/8/3B4/K1kB4 b - - 0 1")),
      new SeedCase(Material.KBNVK, "4k3/8/8/8/8/8/8/1N2KB2 w - - 0 1", "KBNvK.pgn", List.of(
          "8/8/8/8/8/8/8/KNkB4 b - - 0 1",
          "8/8/8/8/8/8/3N4/K1kB4 b - - 0 1")),
      new SeedCase(Material.KRVKB, "2b1k3/8/8/8/8/8/8/R3K3 w - - 0 1", "KRvKB.pgn", List.of(
          "8/8/8/8/8/1b6/k7/R1K5 b - - 0 1",
          "8/8/8/8/8/8/2b5/K1kR4 b - - 0 1")),
      new SeedCase(Material.KRVKN, "1n2k3/8/8/8/8/8/8/R3K3 w - - 0 1", "KRvKN.pgn", List.of(
          "8/8/8/8/8/8/7n/K5Rk b - - 0 1",
          "8/8/8/8/8/2n5/R7/k1K5 b - - 0 1")),
      new SeedCase(Material.KNNVK, "4k3/8/8/8/8/8/8/1N2K1N1 w - - 0 1", "KNNvK.pgn", List.of(
          "8/8/8/8/8/4N3/3N4/K1k5 b - - 0 1",
          "8/8/8/8/8/N7/k7/N1K5 b - - 0 1")));

  @SuppressWarnings("static-method")
  @Test
  void forcedFirstCaptureExamplesAreReachableFromOriginalSquareSeeds() {
    for (final SeedCase seedCase : SEED_CASES) {
      final var missing = seedCase.material().missingReachableTargets(seedCase.seedFen(), seedCase.targetFens());
      assertTrue(missing.isEmpty(), () -> seedCase.material() + " seed did not reach: " + missing);
    }
  }

  @SuppressWarnings("static-method")
  @Test
  void seedProofGamesReachOriginalSquareSeeds() {
    for (final SeedCase seedCase : SEED_CASES) {
      final var seed = StrictFenParser.parse(seedCase.seedFen());
      final var board = new Board();
      for (final String token : proofGameTokens(proofGame(seedCase.proofGameResource()))) {
        board.moveLenient(token);
      }
      assertEquals(seed.sideToMove(), board.getSideToMove(),
          () -> seedCase.material() + " proof game does not reach seed side to move");
      assertEquals(seed.bitboardPosition(), board.getBitboardPosition(),
          () -> seedCase.material() + " proof game does not reach seed placement");
    }
  }

  private static List<String> proofGameTokens(String proofGame) {
    final List<String> result = new ArrayList<>();
    final var moveText = proofGame.replaceAll("(?m)^\\[[^\\r\\n]*]\\s*", " ").replaceAll("\\([^)]*\\)", " ")
        .replaceAll("\\{[^}]*}", " ").replaceAll(";[^\\r\\n]*", " ");
    for (final String rawToken : moveText.split("\\s+")) {
      var token = rawToken.strip();
      token = token.replaceFirst("^\\d+\\.\\.\\.", "");
      token = token.replaceFirst("^\\d+\\.", "");
      if (token.isEmpty() || token.matches("\\$\\d+")) {
        continue;
      }
      if ("1-0".equals(token) || "0-1".equals(token) || "1/2-1/2".equals(token) || "*".equals(token)) {
        continue;
      }
      result.add(token);
    }
    return result;
  }

  private static String proofGame(String resourceName) {
    final var resourcePath = "proof-games/" + resourceName;
    try (var inputStream = TestForcedFirstCaptureSeedReachability.class.getClassLoader()
        .getResourceAsStream(resourcePath)) {
      if (inputStream == null) {
        throw new AssertionError("Missing proof game resource: " + resourcePath);
      }
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private enum Material {
    KRVK(3) {
      @Override
      int encode(BitboardPosition position, Side side) {
        return pack(side, square(position.whiteKings()), square(position.whiteRooks()), square(position.blackKings()));
      }

      @Override
      BitboardPosition position(int code) {
        return new BitboardPosition(0L, bit(this.piece(code, 1)), 0L, 0L, 0L, bit(this.piece(code, 0)), 0L, 0L, 0L,
            0L, 0L, bit(this.piece(code, 2)));
      }
    },
    KQVK(3) {
      @Override
      int encode(BitboardPosition position, Side side) {
        return pack(side, square(position.whiteKings()), square(position.whiteQueens()), square(position.blackKings()));
      }

      @Override
      BitboardPosition position(int code) {
        return new BitboardPosition(0L, 0L, 0L, 0L, bit(this.piece(code, 1)), bit(this.piece(code, 0)), 0L, 0L,
            0L, 0L, 0L, bit(this.piece(code, 2)));
      }
    },
    KBBVK(4) {
      @Override
      int encode(BitboardPosition position, Side side) {
        final var bishops = twoSquares(position.whiteBishops());
        return pack(side, square(position.whiteKings()), bishops[0], bishops[1], square(position.blackKings()));
      }

      @Override
      BitboardPosition position(int code) {
        return new BitboardPosition(0L, 0L, 0L, bit(this.piece(code, 1)) | bit(this.piece(code, 2)), 0L,
            bit(this.piece(code, 0)), 0L, 0L, 0L, 0L, 0L, bit(this.piece(code, 3)));
      }
    },
    KBNVK(4) {
      @Override
      int encode(BitboardPosition position, Side side) {
        return pack(side, square(position.whiteKings()), square(position.whiteBishops()),
            square(position.whiteKnights()), square(position.blackKings()));
      }

      @Override
      BitboardPosition position(int code) {
        return new BitboardPosition(0L, 0L, bit(this.piece(code, 2)), bit(this.piece(code, 1)), 0L,
            bit(this.piece(code, 0)), 0L, 0L, 0L, 0L, 0L, bit(this.piece(code, 3)));
      }
    },
    KRVKB(4) {
      @Override
      int encode(BitboardPosition position, Side side) {
        return pack(side, square(position.whiteKings()), square(position.whiteRooks()), square(position.blackKings()),
            square(position.blackBishops()));
      }

      @Override
      BitboardPosition position(int code) {
        return new BitboardPosition(0L, bit(this.piece(code, 1)), 0L, 0L, 0L, bit(this.piece(code, 0)), 0L, 0L, 0L,
            bit(this.piece(code, 3)), 0L, bit(this.piece(code, 2)));
      }
    },
    KRVKN(4) {
      @Override
      int encode(BitboardPosition position, Side side) {
        return pack(side, square(position.whiteKings()), square(position.whiteRooks()), square(position.blackKings()),
            square(position.blackKnights()));
      }

      @Override
      BitboardPosition position(int code) {
        return new BitboardPosition(0L, bit(this.piece(code, 1)), 0L, 0L, 0L, bit(this.piece(code, 0)), 0L, 0L,
            bit(this.piece(code, 3)), 0L, 0L, bit(this.piece(code, 2)));
      }
    },
    KNNVK(4) {
      @Override
      int encode(BitboardPosition position, Side side) {
        final var knights = twoSquares(position.whiteKnights());
        return pack(side, square(position.whiteKings()), knights[0], knights[1], square(position.blackKings()));
      }

      @Override
      BitboardPosition position(int code) {
        return new BitboardPosition(0L, 0L, bit(this.piece(code, 1)) | bit(this.piece(code, 2)), 0L, 0L,
            bit(this.piece(code, 0)), 0L, 0L, 0L, 0L, 0L, bit(this.piece(code, 3)));
      }
    };

    private final int pieceCount;

    Material(int pieceCount) {
      this.pieceCount = pieceCount;
    }

    abstract int encode(BitboardPosition position, Side side);

    abstract BitboardPosition position(int code);

    Set<String> missingReachableTargets(String seedFen, List<String> targetFens) {
      final BitSet targets = new BitSet();
      final Map<Integer, String> targetFenByCode = new HashMap<>();
      final List<Integer> targetCodes = new ArrayList<>();
      for (final String fen : targetFens) {
        final var target = StrictFenParser.parse(fen);
        final var code = encode(target.bitboardPosition(), target.sideToMove());
        targets.set(code);
        targetCodes.add(code);
        targetFenByCode.put(code, fen);
      }

      final var seed = StrictFenParser.parse(seedFen);
      final var queue = new IntQueue();
      final BitSet seen = new BitSet();
      addSeed(queue, seen, encode(seed.bitboardPosition(), seed.sideToMove()));

      while (!queue.isEmpty() && targets.cardinality() > 0) {
        final var state = queue.remove();
        targets.clear(state);
        final var side = side(state);
        final var position = position(state);
        for (final var move : position.legalMoves(side, 0L)) {
          final var nextPosition = position.afterMove(move, side);
          final int nextState;
          try {
            nextState = encode(nextPosition, side.getOppositeSide());
          } catch (IllegalArgumentException e) {
            continue;
          }
          if (!seen.get(nextState)) {
            seen.set(nextState);
            queue.add(nextState);
          }
        }
      }

      final Set<String> missing = new HashSet<>();
      for (final var target : targetCodes) {
        if (targets.get(target)) {
          missing.add(targetFenByCode.get(target));
        }
      }
      return missing;
    }

    int placementCode(String fen) {
      return placementCode(StrictFenParser.parse(fen).bitboardPosition());
    }

    int placementCode(BitboardPosition position) {
      return encode(position, Side.WHITE) & ~1;
    }

    private static void addSeed(IntQueue queue, BitSet seen, int state) {
      if (!seen.get(state)) {
        seen.set(state);
        queue.add(state);
      }
    }

    private static int pack(Side side, int... pieces) {
      var result = 0;
      for (final var piece : pieces) {
        if (piece < 0) {
          throw new IllegalArgumentException("Missing piece");
        }
        result = result << 6 | piece;
      }
      return result << 1 | (side == Side.WHITE ? 0 : 1);
    }

    private static Side side(int code) {
      return (code & 1) == 0 ? Side.WHITE : Side.BLACK;
    }

    final int piece(int code, int index) {
      final var shift = (pieceCount - 1 - index) * 6 + 1;
      return (int) (code >>> shift & 0x3F);
    }

    private static int square(long bitboard) {
      if (Long.bitCount(bitboard) != 1) {
        throw new IllegalArgumentException("Expected exactly one piece bit: " + bitboard);
      }
      return Long.numberOfTrailingZeros(bitboard);
    }

    private static int[] twoSquares(long bitboard) {
      if (Long.bitCount(bitboard) != 2) {
        throw new IllegalArgumentException("Expected exactly two piece bits: " + bitboard);
      }
      final var first = Long.numberOfTrailingZeros(bitboard);
      final var second = Long.numberOfTrailingZeros(bitboard & ~(1L << first));
      return new int[] {first, second};
    }

    private static long bit(int square) {
      return 1L << square;
    }
  }

  private record SeedCase(Material material, String seedFen, String proofGameResource, List<String> targetFens) {
  }

  private static final class IntQueue {
    private int[] elements = new int[1 << 16];
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
