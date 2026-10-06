package io.github.dlbbld.basiccheckmatereachability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.dlbbld.ashlarchess.board.Board;
import io.github.dlbbld.ashlarchess.board.enums.Side;
import io.github.dlbbld.ashlarchess.board.enums.Square;
import io.github.dlbbld.ashlarchess.fen.StrictFenParser;
import io.github.dlbbld.basiccheckmatereachability.BasicLightBishopKnightHelpmateAnalysis.LightBishopKnightState;

class TestKbnPromotionExceptions {

  private static final List<ProofCase> CASES = List.of(
      new ProofCase(false, Square.E8, "Kd4 Ke7 Ke5 Kf8 Kf6 Kg8 Ne4 Kh8 Kf7 Kh7 Nf6+ Kh8 Ne8 Kh7 g4 Kh8 g5 Kh7 g6+ Kh8 g7+ Kh7"),
      new ProofCase(false, Square.E6, "Kd4 Ke7 Ke5 Kf8 Kf6 Kg8 Ne4 Kh8 Kf7 Kh7 Ng5+ Kh8 Ne6 Kh7 g4 Kh8 g5 Kh7 g6+ Kh8 g7+ Kh7"),
      new ProofCase(false, Square.F5, "Kd4 Ke7 Ke5 Kf8 Kf6 Kg8 Ne4 Kh8 Kf7 Kh7 Nd6 Kh8 Nf5 Kh7 g4 Kh8 g5 Kh7 g6+ Kh8 g7+ Kh7"),
      new ProofCase(false, Square.H5, "Kd4 Ke7 Ke5 Kf8 Kf6 Kg8 Ne4 Kh8 Kf7 Kh7 Nf6+ Kh8 Nh5 Kh7 g4 Kh8 g5 Kh7 g6+ Kh8 g7+ Kh7"),
      new ProofCase(true, Square.D8, "Kd4 Ke7 Ke5 Kf8 Kd6 Kg8 Kc7 Kh8 Ne4 Kg8 Nd6 Kf8 Nf7 Ke7 b4 Ke6 b5 Kd5 b6 Kc5 b7 Kb5 Kc8 Kb6 Nd8 Ka6 Kc7 Ka7"),
      new ProofCase(true, Square.D6, "Kd4 Ke7 Ke5 Kf8 Kd6 Kg8 Kc7 Kf8 Ne4 Ke7 b4 Ke6 b5 Kd5 b6 Kc4 b7 Kb5 Kd8 Kb6 Nd6 Ka6 Kc7 Ka7"),
      new ProofCase(true, Square.C5, "Kd4 Ke7 Ke5 Kf8 Kd6 Kg8 Kc7 Kf8 Ne4 Ke7 b4 Ke6 Nc5+ Kd5 b5 Kc4 b6 Kb5 b7 Ka5 Nd7 Ka6 Nc5+ Ka7"),
      new ProofCase(true, Square.A5, "Kd4 Ke7 Ke5 Kf8 Kd6 Kg8 Kc7 Kf8 Nc4 Ke7 b4 Ke6 b5 Kd5 b6 Kc5 b7 Kb5 Nd6+ Ka5 Nc4+ Ka6 Na5 Ka7"));

  @Test
  void everyPromotionTrapHasAnOrdinaryLegalMoveSequenceFromTheInitialPosition() throws IOException {
    for (final var proof : CASES) {
      final var board = seed(proof.mirrored());
      play(board, proof.continuation());
      board.moveLenient(proof.mirrored() ? "b8=B+" : "g8=B+");
      final var blackToMove = state(board);
      assertEquals(proof.knight(), blackToMove.whiteKnight());
      assertEquals(proof.knight() == (proof.mirrored() ? Square.C5 : Square.F5),
          KbnPromotionExceptions.contains(blackToMove));
      if (KbnPromotionExceptions.contains(blackToMove)) {
        assertNoHelpmate(board);
      }
      // In the F5/C5 case promotion is already dead. Kh8/Ka8 is an ordinary
      // Article-3 legal move used to classify the board graph, not ongoing play.
      board.moveLenient(proof.mirrored() ? "Ka8" : "Kh8");
      assertTrue(KbnPromotionExceptions.contains(state(board)), board.getFen());
      assertNoHelpmate(board);
    }
  }

  @Test
  void suppliedExampleAllowsKh6BeforeKh8CreatesTheDeadPosition() {
    final var board = Board.fromFenStrict("4N3/5KPk/8/8/8/8/8/8 w - - 0 1");
    board.moveLenient("g8=B+");
    assertTrue(board.getLegalMovesAsSan().contains("Kh6"));
    assertFalse(KbnPromotionExceptions.contains(state(board)));
    final var mate = board.copyCurrentPositionWithoutHistory();
    play(mate, "Kh6 Bh7 Kh5 Nd6 Kh6 Kf6 Kh5 Bg6+ Kh6 Nf7#");
    assertTrue(mate.isCheckmate());
    board.moveLenient("Kh8");
    assertTrue(KbnPromotionExceptions.contains(state(board)));
    assertNoHelpmate(board);
  }

  // Independent forward search with the ordinary move generator. Capturing either
  // minor leaves K+B or K+N versus K, where mate is impossible.
  private static void assertNoHelpmate(Board root) {
    final var queue = new ArrayDeque<Board>();
    final var seen = new HashSet<String>();
    queue.add(root.copyCurrentPositionWithoutHistory());
    while (!queue.isEmpty()) {
      final var board = queue.remove();
      final var key = board.getFen().split(" ")[0] + board.getSideToMove();
      if (!seen.add(key)) {
        continue;
      }
      assertFalse(board.isCheckmate(), () -> "Found a mate from " + root.getFen());
      for (final var move : board.getLegalMoveSpecifications()) {
        final var next = board.copyCurrentPositionWithoutHistory();
        next.move(move);
        final var placement = next.getBitboardPosition();
        if (Long.bitCount(placement.whiteBishops() | placement.whiteKnights()) == 2) {
          queue.add(next);
        }
      }
    }
    assertTrue(seen.size() < 100, "The traps should have only short material-preserving continuations");
  }

  private static Board seed(boolean mirrored) throws IOException {
    final var resource = "proof-games/KNPvK-" + (mirrored ? "b" : "g") + "-pawn.pgn";
    final var board = loadProofGame(resource);
    final var expected = StrictFenParser.parse(mirrored
        ? "8/8/3k4/8/8/2K5/1P1N4/8 w - - 0 1"
        : "8/8/3k4/8/8/2K5/3N2P1/8 w - - 0 1");
    assertEquals(expected.bitboardPosition(), board.getBitboardPosition());
    assertEquals(Side.WHITE, board.getSideToMove());
    return board;
  }

  private static Board loadProofGame(String resource) throws IOException {
    final var board = new Board();
    try (var input = TestKbnPromotionExceptions.class.getClassLoader().getResourceAsStream(resource)) {
      if (input == null) {
        throw new AssertionError("Missing proof game: " + resource);
      }
      final var pgn = new String(input.readAllBytes(), StandardCharsets.UTF_8)
          .replaceAll("(?m)^\\[[^\\r\\n]*]\\s*", " ").replaceAll("\\d+\\.", " ").replace("*", " ");
      play(board, pgn);
    }
    return board;
  }

  private static void play(Board board, String sans) {
    for (final var san : sans.strip().split("\\s+")) {
      board.moveLenient(san);
    }
  }

  private static LightBishopKnightState state(Board board) {
    final var placement = board.getBitboardPosition();
    return new LightBishopKnightState(square(placement.whiteKings()), square(placement.whiteBishops()),
        square(placement.whiteKnights()), square(placement.blackKings()), board.getSideToMove());
  }

  private static Square square(long pieces) {
    assertEquals(1, Long.bitCount(pieces));
    return Square.REAL.get(Long.numberOfTrailingZeros(pieces));
  }

  private record ProofCase(boolean mirrored, Square knight, String continuation) {
  }
}
