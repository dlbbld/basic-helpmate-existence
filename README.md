# Basic Checkmate Reachability

Reachability analysis for basic chess checkmate material.

This repository is a standalone research artifact built on top of
[ashlar-chess](https://github.com/dlbbld/ashlar-chess). Ashlar provides the
ordinary legal move generator; this project owns the finite material-class
reachability analyses, independent witness verifiers, seed-legality checks, and
the accompanying exposition.

## Central Results

This project delivers a finite-state proof, supplemented by retro-legality
arguments, for two strict-game conclusions.

First, for each material class below, every strictly game-legal, ongoing
position with Black to move has a helpmate for White, unless Black is forced to
capture White's mating material on the first move.

Second, for the same material classes, every strictly game-legal, ongoing
position with White to move has a helpmate for White.

| Material class | Statement |
|---|---|
| `KRvK` | White has a helpmate unless every legal Black move captures the rook. |
| `KQvK` | White has a helpmate unless every legal Black move captures the queen. |
| `KBBvK`, opposite bishops | White has a helpmate unless every legal Black move captures a bishop. |
| `KBNvK`, light bishop | White has a helpmate unless every legal Black move captures the bishop or knight. |
| `KRvKB(light bishop)` | White has a helpmate unless every legal Black move captures the rook. |
| `KRvKN` | White has a helpmate unless every legal Black move captures the rook. |

"Ongoing" excludes positions that are already checkmate or stalemate. The
statement is cooperative: a helpmate means that there exists some legal
continuation to Black checkmate, not that White can force mate against best
defense.

This covers the symmetric bishop-colour and colour-swapped cases. For
`KBNvK` and `KRvKB`, analyzing the light-square bishop case is enough because
the dark-square bishop case is obtained by board symmetry. If Black, rather than
White, has the mating material, the statement is obtained by swapping the
colours. This is the standard reduction to one representative from each
symmetry class.

The local graph contains one apparent black-to-move exception:

| Material class | Representative position | Strict-game status |
|---|---|---|
| `KBNvK(light bishop)` | <a href="https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/2N5/8/k1K5/1B6%20b%20-%20-%200%201?turn=black&amp;pov=black" alt="8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1" width="180"></a><br>`8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1) | retro-illegal |

That position is retro-illegal and therefore cannot arise in a real chess game.

The same computation also classifies White-to-move roots. For `KRvK`, `KQvK`,
`KRvKB(light bishop)`, and `KRvKN`, every ongoing locally legal White-to-move
position has a helpmate. In `KRvKB(light bishop)` and `KRvKN`, this includes
positions where White first captures Black's bishop or knight and reduces to
the verified `KRvK` case. Positions where White has already been checkmated or
stalemated are not ongoing roots.

Locally, `KBBvK` and `KBNvK` have apparent White-to-move no-helpmate
positions. They are locally legal enough to be enumerated, but all listed
representatives are retro-illegal. One representative per board-symmetry class
is:

| Material class | Representative position | Strict-game status |
|---|---|---|
| `KBBvK, opposite bishops` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/B7/B7/k1K5_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/B7/B7/k1K5%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/B7/B7/k1K5 w - - 0 1" width="180"></a><br>`8/8/8/8/8/B7/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B7/k1K5_w_-_-_0_1) | retro-illegal |
| `KBBvK, opposite bishops` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k1B5_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/8/B1K5/k1B5%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/8/B1K5/k1B5 w - - 0 1" width="180"></a><br>`8/8/8/8/8/8/B1K5/k1B5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k1B5_w_-_-_0_1) | retro-illegal |
| `KBBvK, opposite bishops` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/B7/B1K5/k7_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/B7/B1K5/k7%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/B7/B1K5/k7 w - - 0 1" width="180"></a><br>`8/8/8/8/8/B7/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B1K5/k7_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/8/B7/k1KN4_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/8/B7/k1KN4%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/8/B7/k1KN4 w - - 0 1" width="180"></a><br>`8/8/8/8/8/8/B7/k1KN4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B7/k1KN4_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/8/BN6/k1K5_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/8/BN6/k1K5%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/8/BN6/k1K5 w - - 0 1" width="180"></a><br>`8/8/8/8/8/8/BN6/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/BN6/k1K5_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B7/k1K5_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/3N4/B7/k1K5%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/3N4/B7/k1K5 w - - 0 1" width="180"></a><br>`8/8/8/8/8/3N4/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B7/k1K5_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/N7/8/B7/k1K5_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/N7/8/B7/k1K5%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/N7/8/B7/k1K5 w - - 0 1" width="180"></a><br>`8/8/8/8/N7/8/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B7/k1K5_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B7/k1K5_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/2N5/8/B7/k1K5%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/2N5/8/B7/k1K5 w - - 0 1" width="180"></a><br>`8/8/8/8/2N5/8/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B7/k1K5_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/8/2K5/kB1N4_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/8/2K5/kB1N4%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/8/2K5/kB1N4 w - - 0 1" width="180"></a><br>`8/8/8/8/8/8/2K5/kB1N4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/2K5/kB1N4_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/8/1NK5/kB6_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/8/1NK5/kB6%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/8/1NK5/kB6 w - - 0 1" width="180"></a><br>`8/8/8/8/8/8/1NK5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/1NK5/kB6_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/3N4/2K5/kB6_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/3N4/2K5/kB6%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/3N4/2K5/kB6 w - - 0 1" width="180"></a><br>`8/8/8/8/8/3N4/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/2K5/kB6_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/N7/8/2K5/kB6_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/N7/8/2K5/kB6%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/N7/8/2K5/kB6 w - - 0 1" width="180"></a><br>`8/8/8/8/N7/8/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/2K5/kB6_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/2N5/8/2K5/kB6_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/2N5/8/2K5/kB6%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/2N5/8/2K5/kB6 w - - 0 1" width="180"></a><br>`8/8/8/8/2N5/8/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/2K5/kB6_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k2N4_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/8/B1K5/k2N4%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/8/B1K5/k2N4 w - - 0 1" width="180"></a><br>`8/8/8/8/8/8/B1K5/k2N4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k2N4_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/8/BNK5/k7_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/8/BNK5/k7%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/8/BNK5/k7 w - - 0 1" width="180"></a><br>`8/8/8/8/8/8/BNK5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/BNK5/k7_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B1K5/k7_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/8/3N4/B1K5/k7%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/8/3N4/B1K5/k7 w - - 0 1" width="180"></a><br>`8/8/8/8/8/3N4/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B1K5/k7_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/N7/8/B1K5/k7_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/N7/8/B1K5/k7%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/N7/8/B1K5/k7 w - - 0 1" width="180"></a><br>`8/8/8/8/N7/8/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B1K5/k7_w_-_-_0_1) | retro-illegal |
| `KBNvK, light bishop` | <a href="https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B1K5/k7_w_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/2N5/8/B1K5/k7%20w%20-%20-%200%201?turn=white&amp;pov=white" alt="8/8/8/8/2N5/8/B1K5/k7 w - - 0 1" width="180"></a><br>`8/8/8/8/2N5/8/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B1K5/k7_w_-_-_0_1) | retro-illegal |

These positions cannot arise from an ordinary chess game. Since White is to
move and Black has only a king, Black's previous move would have had to be a
king move to `a1` in the displayed representatives. In the `KBBvK` rows, the
only possible predecessor square not immediately ruled out by king adjacency is
`a2`, but `a2` is occupied by a white bishop. In the `KBNvK` rows with a bishop
on `a2`, the same argument applies. In the `KBNvK` rows with a bishop on `b1`,
`b1` is occupied, `b2` is adjacent to the white king, and `a2` is attacked by
the bishop. Hence there is no legal black last move. Board symmetries preserve
this retro-illegality.

## Motivation

In chess, the FIDE dead-position rule asks whether checkmate is possible by any
series of legal moves. That is a cooperative reachability question: can the side
with mating material reach a position where the opponent is checkmated, assuming
both players may cooperate?

For familiar material classes this sounds almost obvious. One might try to give
a direct geometric proof by separating cases such as "king in the corner",
"king on the edge", and "king in the middle". In practice this is surprisingly
fragile. Side to move, immediate checks, stalemates, forced captures, and
retro-legality all matter.

The `KBNvK` case shows why a pure hand proof is dangerous. The local position
below is a counterexample to the naive local theorem: Black is not forced to
capture a white piece on the first move, but White still has no
material-preserving cooperative path to checkmate.

<a href="https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/2N5/8/k1K5/1B6%20b%20-%20-%200%201?turn=black&amp;pov=black" alt="8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1" width="240"></a>

```text
8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1
```

[Open this position on Lichess](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1).

The catch is that this position is not strictly legal. Black is in check from
the bishop on `b1`. If the position had arisen in a legal game, White's last
move would have had to create that check. But the bishop cannot have moved to
`b1`: the diagonal squares from which it could have arrived are blocked by the
black king on `a2` and the white king on `c2`. Nor can the check have been
discovered, because the bishop is already adjacent to the black king with no
intervening square. The position is locally coherent enough to enumerate, but it
cannot arise from the normal starting position.

This last step is intentionally not hidden behind the finite-state graph. The
reachability analysis is mechanical, but judging the possible counterexamples as
not strictly legal still uses retrograde chess intuition. A fully mechanical
strict-legality classifier is harder than it first appears, because positions
that look impossible by an ordinary last move may have arisen by capture. For
example, the position
[`4kR2/8/4K3/8/8/8/8/8 b - - 0 1`](https://lichess.org/analysis/standard/4kR2/8/4K3/8/8/8/8/8_b_-_-_0_1)
might look suspicious if considered in isolation: Black is to move and in check
from the rook on `f8`. But it can arise from
[`4kb1R/8/4K3/8/8/8/8/8 w - - 0 1`](https://lichess.org/analysis/standard/4kb1R/8/4K3/8/8/8/8/8_w_-_-_0_1)
by the legal move `Rxf8+`, with the rook from `h8` capturing a black bishop on
`f8`. Thus strict legality cannot be decided only by asking whether the checking
piece has a quiet origin square; captures have to be included, which makes the
general retro problem substantially more challenging.

This is the reason for the computational approach: enumerate the full finite
state space, compute reachability, and then verify the recorded witnesses with
the ordinary legal move generator.

## Material Classes

The current artifact covers the following classes.

Classical basic checkmates:

- `KRvK`
- `KQvK`
- `KBBvK`, with bishops on opposite-coloured squares
- `KBNvK`, with the bishop on light squares

Related basic rook endgames:

- `KRvKB(light bishop)`
- `KRvKN`

Throughout this project, `v` separates White's material from Black's material.
For example, `KRvK` means White king and rook versus Black king.

## Main Theorem Shape

The current theorem is a black-to-move statement.

For each material class, all locally legal states are enumerated. Checkmated
black-to-move states are used as terminal winning states. A reverse
reachability computation marks every state from which a material-preserving
legal path to such a checkmate exists.

The checked statement is:

> From every ongoing legal-position candidate with Black to move, if Black has
> at least one legal first move that does not immediately capture White's
> mating material, then White has a cooperative continuation to checkmate.

"Ongoing" excludes roots that are already checkmate or stalemate.

This is not adversarial search. It does not say that White can force mate
against best defense. It says that a helpmate exists: there is some legal
continuation, with both sides allowed to cooperate, that reaches Black
checkmate.

The same analysis also classifies the white-to-move question. White can either
follow a fixed-material witness, or, in the rook endgames with a black defender,
capture that defender and reduce to `KRvK`. Roots where White has already been
checkmated or stalemated are not ongoing games. With these distinctions,
`KRvK`, `KQvK`, `KRvKB(light bishop)`, and `KRvKN` have no ongoing local
white-to-move exceptions. `KBBvK` and `KBNvK` have small local exception sets,
but their canonical representatives are retro-illegal, so they do not affect
the strict-game white-to-move conclusion.

## Verification

The analyzer stores one witness move for every winning non-terminal state. The
independent verifier then checks:

1. each terminal seed is a real Black-checkmate position;
2. each recorded witness is generated by `BitboardPosition.legalMoves(...)`;
3. the witness reaches the recorded successor;
4. the witness preserves the material class;
5. the successor is closer to a terminal mate layer.

Thus the JUnit tests do not merely sample examples. They exhaust the finite
state spaces and verify the recorded certificate edges with Ashlar's normal
legal move generator.

Run:

```sh
mvn test
```

On the current machine this test suite takes roughly two minutes.

## Current Results

Counts are over locally legal states. "Maximum witness distance" is the largest
number of material-preserving legal plies in the stored cooperative path to a
terminal mate seed. It is not adversarial depth-to-mate.

| Material class | Legal states | Black-to-move states | Black checkmates | Stalemates | Forced first capture | Counterexamples | Maximum witness distance |
|---|---:|---:|---:|---:|---:|---:|---:|
| `KRvK` | 399,112 | 223,944 | 216 | 68 | 412 | 0 | 14 |
| `KQvK` | 368,452 | 223,944 | 364 | 872 | 2,420 | 0 | 14 |
| `KBBvK`, opposite bishops | 5,973,472 | 3,469,344 | 1,552 | 5,320 | 7,952 | 0 | 16 |
| `KBNvK`, light bishop | 12,268,044 | 6,830,292 | 232 | 6,444 | 4,474 | 4 local states, 1 canonical, retro-illegal | 16 |
| `KRvKB(light bishop)` | 11,306,596 | 5,916,232 | 3,264 | 48 | 3,740 | 0 | 14 |
| `KRvKN` | 23,315,984 | 12,535,256 | 9,328 | 48 | 7,168 | 0 | 14 |

The `KRvKB(light bishop)` and `KRvKN` rows are not basic mates in the narrow
textbook sense, because Black still has a defensive piece. They are included
because they are natural practical endgames for the same reachability method.

White-to-move local reachability:

| Material class | White-to-move states | Unwinnable white-to-move states | Canonical unwinnable representatives |
|---|---:|---:|---:|
| `KRvK` | 175,168 | 0 | 0 |
| `KQvK` | 144,508 | 0 | 0 |
| `KBBvK`, opposite bishops | 2,504,128 | 24 | 3 |
| `KBNvK`, light bishop | 5,437,752 | 60 | 15 |
| `KRvKB(light bishop)` | 5,390,364 | 0 | 0 |
| `KRvKN` | 10,780,728 | 0 | 0 |

The nonzero white-to-move exception rows are local material-class artifacts. The
canonical representatives are retro-illegal by a last-move black-king argument,
so they are not exceptions to the strict-game statement. The witness verifier
already checks all fixed-material white-to-move winning states, because the
stored witness edges are side-to-move agnostic.

For the rook endgames with a black defender, the corrected classification also
records non-exceptional local roots outside the fixed material-preserving graph:
`KRvKB(light bishop)` has 8 White-to-move states where White captures the bishop
and reduces to the verified `KRvK` case; `KRvKN` has 8 already-ended
White-to-move states and 24 states where White captures the knight and reduces
to `KRvK`. The tests replay each such White capture with Ashlar's legal move
generator and then check that the resulting `KRvK` position is a verified
helpmate root for White.

## External Count Cross-Checks

The raw state counts can be checked against the Syzygy tablebase statistics by
putting both computations in the same quotient space. Syzygy uses Kirill
Kryukov's
[Number of Unique Legal Positions](https://kirill-kryukov.com/chess/nulp/)
definition. In that definition, a position includes side to move, castling
rights, and en-passant rights, and "unique" means an equivalence class under
easy symmetries such as board mirroring, board rotation, and color swapping.
The legality test is local: among other basic constraints, a position is illegal
when the side to move can capture the opponent's king.

This is the same level of legality used by our state-space counts. In
particular, Syzygy/NULP can include positions that are locally legal but not
strictly reachable from a real game. The retro-illegal positions
`8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1` and
`8/8/8/8/8/B7/B7/k1K5 w - - 0 1` are therefore expected to be probeable in the
Syzygy tablebase. That does not contradict our strict-game conclusion; it
confirms that the count comparison is being made in the same local state space.

The Syzygy site displays aggregate WDL outcomes, while its
[machine-readable statistics](https://syzygy-tables.info/stats.json) keep the
side to move separated. For example, the displayed `KRvK` value of 47,219 White
wins is `21,959` White-to-move wins plus `25,260` Black-to-move losses.

Our theorem tables above use raw local states. The table below gives the
corresponding unique representative counts after quotienting by the applicable
board symmetries, and compares them with Syzygy. The unique count is not always
the raw count divided by 8, because symmetric positions can have smaller
orbits.

| Material class | Scope compared | Raw White to move | Raw Black to move | Raw total | Unique White to move | Unique Black to move | Unique total | Syzygy unique total | Comparison |
|---|---|---:|---:|---:|---:|---:|---:|---:|---|
| `KRvK` | theorem class | 175,168 | 223,944 | 399,112 | 21,959 | 28,056 | 50,015 | 50,015 | matches |
| `KQvK` | theorem class | 144,508 | 223,944 | 368,452 | 18,081 | 28,056 | 46,137 | 46,137 | matches |
| `KBNvK(light bishop)` | light-bishop theorem class, equivalent to Syzygy `KBNvK` after bishop-colour symmetry | 5,437,752 | 6,830,292 | 12,268,044 | 1,359,578 | 1,707,888 | 3,067,466 | 3,067,466 | matches |
| `KRvKB(light bishop)` | light-bishop theorem class, equivalent to Syzygy `KRvKB` after bishop-colour symmetry | 5,390,364 | 5,916,232 | 11,306,596 | 1,347,906 | 1,479,198 | 2,827,104 | 2,827,104 | matches |
| `KRvKN` | theorem class | 10,780,728 | 12,535,256 | 23,315,984 | 1,347,906 | 1,567,222 | 2,915,128 | 2,915,128 | matches |
| `KBBvK`, opposite bishops | theorem subset only | 2,504,128 | 3,469,344 | 5,973,472 | 626,032 | 867,336 | 1,493,368 | n/a | Syzygy does not split by bishop colours |
| `KBBvK`, all ordered bishop slots | expanded Syzygy material table | 10,164,056 | 13,660,584 | 23,824,640 | 1,270,542 | 1,707,888 | 2,978,430 | 2,978,430 | matches |

The `KBBvK` line needs special care. The theorem is only about opposite-coloured
bishops. The Syzygy material key `KBBvK` does not expose a separate statistic
for opposite-coloured bishops; it counts the whole two-bishop material table.
When the independent counter is expanded to the same all-bishop table, using two
bishop slots over all bishop-square colours, it matches Syzygy exactly. The
opposite-coloured theorem subset is listed separately because that is the class
proved here.

These Syzygy tables do not replace our enumeration. Syzygy can probe a given FEN
and its statistics expose aggregate counts, but the public statistics do not
give a material-class move graph or a list of all positions with helpmate
witnesses. Decoding tablebase files into such a graph would still require a
position enumeration step, and Syzygy WDL is adversarial tablebase value rather
than cooperative reachability. For this project, direct finite-state generation
is simpler, faster, and produces the certificate edges needed by the verifier.

The regression test `TestSyzygyCountCrossCheck` independently recomputes all
counts in this section without calling the main reachability analyzers.

Count agreement alone would still leave a theoretical worry: two different
position sets can have the same size. For that reason the repository also
contains an optional pointwise Syzygy probe:

```sh
python scripts/verify_syzygy_position_sets.py
```

The script generates one representative per relevant board-symmetry class and
probes each representative against local Syzygy files with python-chess. It does
not use the main reachability analyzers. On 30 May 2026, it was run against the
local Syzygy files in `C:\Users\danie\git\python-chess\data\syzygy\regular`
with these results:

| Material class | Unique representatives probed |
|---|---:|
| `KRvK` | 50,015 |
| `KQvK` | 46,137 |
| `KBNvK(light bishop)` | 3,067,466 |
| `KRvKB(light bishop)` | 2,827,104 |
| `KRvKN` | 2,915,128 |
| `KBBvK`, all ordered bishop slots | 2,978,430 |

Every generated representative probed successfully. Together with the matching
Syzygy/NULP unique counts, this gives a pointwise cross-check that the generated
local position sets are the Syzygy local position sets in the comparable
quotient space. The opposite-coloured `KBBvK` theorem class is contained in the
expanded all-bishop Syzygy probe.

## Strict-Legality Seed Checks

The main reachability enumeration works with local legality. A separate
strict-legality experiment starts from a known game-reachable seed position and
flood-fills forward using ordinary legal moves that preserve the material class.

This proves that every reached state is strictly legal in the sense of being
reachable from the normal starting position, assuming the seed itself has a
legal proof game.

Current seed checks:

| Material class | Seed | Legal states | Reached states | Unreached states | Unreached black-to-move non-check states |
|---|---|---:|---:|---:|---:|
| `KRvK` | `Ke1 Ra1 ke8 b` | 399,112 | 399,064 | 48 | 0 |
| `KQvK` | `Ke1 Qd1 ke8 b` | 368,452 | 368,452 | 0 | 0 |
| `KBNvK(light bishop)` | `Ke1 Bf1 Nb1 ke8 b` | 12,268,044 | 12,169,754 | 98,290 | 0 |
| `KBBvK(opposite bishops)` | `Ke1 Bf1 Bc1 ke8 b` | 5,973,472 | 5,929,808 | 43,664 | 0 |

The important pattern is that all checked classes reach every black-to-move
non-check state from the original-piece seed. The unreached states, where they
exist, are in-check states or white-to-move states that may require separate
last-move or "entered by capture" retro analysis.

## Status

Implemented:

- finite-state theorem analyzers;
- independent witness verifiers;
- JUnit tests pinning all current counts;
- Syzygy/NULP count cross-checks for the covered material classes;
- optional pointwise Syzygy position-set probe script;
- last-move retro-illegality classification for the local white-to-move
  exception representatives;
- seed strict-legality checks for `KRvK`, `KQvK`, `KBNvK(light bishop)`, and
  `KBBvK(opposite bishops)`;
- exposition draft in
  [docs/basic-checkmate-helpmate-exposition.md](docs/basic-checkmate-helpmate-exposition.md).

Not yet implemented:

- a complete strict-legality classification for all unreached in-check states;
- seed strict-legality checks for `KRvKB(light bishop)` and `KRvKN`;
- machine-readable exported result tables and representative FEN sets.

## Dependency

This project uses the Maven Central release of Ashlar Chess:

```xml
<dependency>
  <groupId>io.github.dlbbld</groupId>
  <artifactId>ashlar-chess</artifactId>
  <version>17.0.0</version>
</dependency>
```
