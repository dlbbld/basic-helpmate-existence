# A finite-state certificate for basic-checkmate reachability in chess

Daniel Bächli  
Mathematician, chess player, and author of Clean Chess

Draft, 20 May 2026

## Abstract

FIDE's dead-position rule is phrased in terms of existence: a position is dead if neither player can checkmate the opponent by any possible series of legal moves. This is the right chess rule, but it is not a small local material rule. Miguel Ambrona's Chess Helpmate Analyzer (CHA) gives a practical and sound approach to the problem by searching for helpmate sequences and by recognizing large classes of unwinnable positions statically. In high-level implementations, however, some elementary endgames can still be annoying: the position is obviously part of a classical mating material class, but a blind helpmate search may need to discover a long cooperative line.

This note proposes a finite-state supplement for such cases. For selected basic-checkmate material classes, we enumerate the complete local legal state graph and compute, by retrograde propagation from checkmate states, whether White has a cooperative continuation to checkmate. The resulting black-to-move statement is intentionally modest and useful: excluding positions that are already checkmate or stalemate, if Black has at least one first move that does not immediately capture a mating piece, then White has a helpmate. The statement was checked for KRvK, KQvK, KBBvK with opposite-coloured bishops, KBNvK with a light-square bishop modulo one retro-illegal local exception, KRvKB(light), and KRvKN. The same computation also supports the white-to-move statement: every strictly game-legal, ongoing white-to-move position in these classes has a helpmate for White. The remaining local white-to-move exception representatives in KBBvK and KBNvK are retro-illegal by a last-move argument.

## 1. Motivation

Article 5.2.2 of the FIDE Laws states that the game is drawn when a position has arisen in which neither player can checkmate the opponent's king by any series of legal moves. The same existential phrase appears in timeout adjudication: under Article 6.9, a player normally loses on time, but the game is drawn if the opponent cannot checkmate by any possible series of legal moves. FIDE also distinguishes a legal move from a legal position: Article 3.10.3 says that a position is illegal if it cannot have been reached by any series of legal moves.

The computational problem is therefore not "does the side have enough material in ordinary chess folklore?" It is:

> Does there exist at least one legal continuation, with both players allowed to cooperate, that ends in checkmate by the intended winner?

This is a helpmate reachability question. A negative answer means the intended winner cannot win even with the opponent's cooperation.

Ambrona's CHA is a practical answer to this problem. Its public description says that CHA checks whether there exists a sequence of legal moves allowing a chosen player to checkmate, combining a search of variations with static analysis. The FUN 2022 paper proves soundness of the algorithmic approach. CHA is therefore the natural reference point for Clean Chess: it turns a difficult FIDE rule into an implementable algorithm.

The small gap addressed here is not mathematical completeness of CHA. It is engineering cost in elementary endgames. Some basic mates are known to be mating material, but if the implementation treats them only as ordinary helpmate searches, it may spend many recursive calls rediscovering a line. This draft describes a finite precomputation that can be used as a shortcut and, with a verifier, as a certificate-backed theorem for those material classes.

## 2. Terminology

Throughout this draft, "White has a helpmate" means:

> There exists a finite sequence of legal moves, starting from the given position, after which Black is checkmated.

This is cooperative reachability, not adversarial winning. Black is not trying to avoid mate. Black is only required to make legal moves.

The word "legal" has two layers.

Local legality means that the pieces occupy distinct squares, kings are not mutually attacking, the side-to-move convention is consistent with check, and every edge in the graph is a legal chess move in the usual move-generator sense.

Strict game legality means that the position can arise from the normal initial chess position by a legal game. FIDE Article 3.10.3 uses this stronger meaning. The present computation enumerates local legal states, not proof games from the initial position.

This distinction matters, but it does not invalidate the intended shortcut. If a strictly legal position reaches another position by legal moves, the target is also strictly legal. Therefore, a strictly illegal checkmate seed cannot create a false helpmate path from a strictly legal root when all graph edges are genuine legal moves. Illegal components remain outside the game-reachable component.

## 3. High-level algorithmic picture

There are two algorithms in the artifact.

The first algorithm finds the theorem. For a fixed material class, it builds the finite directed graph of all locally legal states in that material class. It then starts from every black-checkmate state and works backwards through legal moves. Whenever a state can legally move to a state already known to reach mate, it is also marked as reaching mate. This is a retrograde reachability computation: instead of trying a fresh helpmate search from each root, it computes the whole material class at once.

The second algorithm verifies the theorem. During the retrograde computation, the analyzer stores one witness move for each non-terminal state it marks as winning. The verifier does not trust the graph construction blindly. It reconstructs each source position, asks Ashlar Chess's ordinary legal move generator for the legal moves, checks that the stored witness is one of them, checks that it reaches the recorded successor, and checks that the successor is strictly closer to a recorded checkmate seed. The verifier therefore turns the computed table into a certificate: every accepted state has a finite chain of ordinary legal moves to mate.

The recorded distance is the length of this shortest witnessed chain in plies inside the material-preserving graph. The maximum witness distance is not a rule-theoretic depth-to-mate under best defense. It is the largest certificate layer needed by the cooperative reachability proof.

## 4. The finite material-class graph

For each material class, we enumerate every placement of the relevant pieces and the side to move. A state is retained if it passes the local legality test. For example, KRvK contains:

- White king square.
- White rook square.
- Black king square.
- Side to move.

KRvKN contains:

- White king square.
- White rook square.
- Black king square.
- Black knight square.
- Side to move.

The graph is directed. An edge `s -> t` exists when the side to move in `s` has a legal move to `t` and the move remains inside the material class. Thus, when the question is KRvKB, a black move that captures the rook is not a graph edge; it leaves the material class. Such moves are still recorded, because they identify the exceptional roots where Black is forced to destroy White's mating material immediately.

The terminal winning states are all states in which Black is checkmated. From those seeds we compute the reverse reachability set:

1. Insert every black-checkmated state into a queue.
2. Pop a known winning state `t`.
3. Enumerate all material-preserving legal predecessor states `s` such that `s -> t`.
4. If `s` was not already marked winning, mark it winning and enqueue it.
5. Continue until the queue is empty.

The result is exact for the enumerated graph: a state is marked winning if and only if some material-preserving legal path leads from it to a black-checkmate state.

This is not the same as `FindHelpmateExhaust`. The computation is a whole-graph dynamic program over a fixed material class. In exchange for being specialized, it avoids repeated rediscovery of the same suffixes.

## 5. The proposed theorem

For a material class `M`, let `G_M` be the local legal material-preserving move graph. Let `W_M` be the reverse closure of the black-checkmate states. A black-to-move root is called ongoing if it is neither already checkmate nor stalemate.

The proposed shortcut theorem has the following form:

> In material class `M`, every ongoing black-to-move state belongs to `W_M`, unless every legal first move by Black captures one of White's mating pieces.

Equivalently: if Black has at least one legal first move that keeps the mating material on the board, then White has a cooperative continuation to checkmate.

This is intentionally a first-move statement. If Black is forced to capture the rook, queen, bishop, or knight at once, the material class changes and this particular certificate should not be used. The surrounding unwinnability algorithm can then analyze the resulting reduced material instead.

The white-to-move question uses the same set `W_M`, but with two additional
bookkeeping distinctions. First, a root where White has already been checkmated
or stalemated is not an ongoing game root. Second, in KRvKB and KRvKN, a White
move that captures Black's bishop or knight is not an obstruction: it reduces
the position to the already verified KRvK case. With these distinctions, KRvK,
KQvK, KRvKB(light), and KRvKN have no ongoing local white-to-move exceptions.
KBBvK and KBNvK retain small local exception sets. Those exceptions are not part
of the black-to-move theorem, and they are not strict game exceptions: their
canonical representatives are retro-illegal by a last-move black-king argument.

## 6. Why a direct proof is difficult

One might hope to replace the finite computation by a classical geometric proof. For instance, one could try to divide the board into cases according to whether the black king is in a corner, on an edge, or in the interior, and then prove that a cooperative mating net can always be arranged unless Black is forced to capture material immediately.

Such a proof may exist, but it does not appear straightforward. The reason is that the statement is not merely "this material is mating material". It quantifies over every local arrangement of the pieces, the side to move, immediate checks, stalemates, forced captures, and short tactical obstructions. The KBNvK computation is a useful warning. It found a local counterexample that is surprising from ordinary endgame intuition: Black is not forced to capture on the first move, yet the local graph does not contain a material-preserving helpmate continuation. The position is retro-illegal, so it does not refute the strict game statement, but its existence shows how easily a hand proof could miss exceptional local geometry.

The finite-state method is therefore not only a brute-force substitute for elegance. It is a way to avoid relying on informal expectations about elementary mates. It enumerates all local cases, records the exceptional classes explicitly, and then lets a separate legal-move verifier check the certificate edge by edge. A future mathematical proof would be valuable, especially for explaining the phenomenon conceptually, but the current contribution is deliberately computational and certificate-based.

## 7. Certificate verification

The production version should not ask readers to trust the dense enumerator alone. The intended verifier is a separate pass using Ashlar Chess's ordinary legal move generator.

The analyzer stores, for every winning non-terminal state, one witness move to a state closer to mate. It also stores each terminal mate seed. The verifier then checks:

1. Every terminal seed is a real black-checkmate position according to the normal move generator.
2. Every witness move appears in `legalMoves(side, ...)` for the source state.
3. The witness target is the exact recorded successor position.
4. The witness preserves the material class.
5. The target has a strictly smaller distance-to-mate layer.

This proves the reachability result by induction on the layer number. Checkmate seeds are the base case. If every layer `d` state has a legal witness to a verified layer `< d`, then every verified state has a finite legal path to mate.

This is much cheaper than replaying a full helpmate line from every root. Full-line replay repeats shared suffixes many times. Certificate verification checks one edge per winning state plus the terminal mates.

For KRvK, this verifier checks 216 terminal mates, 398,416 witness edges, and 223,248 black-to-move theorem roots. For KQvK, it checks 364 terminal mates, 364,796 witness edges, and 220,288 black-to-move theorem roots. For KBNvK with a light-square bishop, it checks 232 terminal mates, 12,256,830 witness edges, and 6,819,138 black-to-move theorem roots. For KBBvK with opposite-coloured bishops, it checks 1,552 terminal mates, 5,958,624 witness edges, and 3,454,520 black-to-move theorem roots. For KRvKB with a light-square black bishop, it checks 3,264 terminal mates, 11,299,536 witness edges, and 5,909,180 black-to-move theorem roots. For KRvKN, it checks 9,328 terminal mates, 23,299,408 witness edges, and 12,518,712 black-to-move theorem roots.

The maximum witness distance to mate is 14 plies for KRvK, KQvK, KRvKB(light), and KRvKN; it is 16 plies for KBNvK(light bishop) and KBBvK(opposite bishops). This "distance" is the certificate layer described above: the number of material-preserving legal plies in the stored cooperative path to a terminal mate seed.

In one local run on 20 May 2026, the KRvK theorem analysis took 5,149 ms, the certificate verification took 4,844 ms, and the combined run took 9,994 ms. The KQvK theorem analysis took 4,659 ms, the certificate verification took 5,187 ms, and the combined run took 9,847 ms. In local runs on 21 May 2026, KBNvK(light bishop) took 2,516 ms for theorem analysis, 9,576 ms for verification, and 12,093 ms combined; KBBvK(opposite bishops) took 1,925 ms, 7,914 ms, and 9,839 ms; KRvKB(light) took 2,381 ms, 10,517 ms, and 12,898 ms; KRvKN took 4,154 ms, 20,073 ms, and 24,228 ms. These timings are implementation and machine dependent, but they give the right order of magnitude: verification remains practical, even when it is several times the finite-state theorem computation.

## 8. Results

The following table reports the current computed results. Counts are over local legal states. Canonical counts quotient by board symmetries appropriate to the material class.

| Material class | Legal states | Black-to-move states | Black checkmates | Stalemates | Forced first capture | Counterexamples | Maximum witness distance |
|---|---:|---:|---:|---:|---:|---:|---:|
| KRvK | 399,112 | 223,944 | 216 | 68 | 412, canonical 54 | 0 | 14 |
| KQvK | 368,452 | 223,944 | 364 | 872 | 2,420, canonical 305 | 0 | 14 |
| KBBvK, opposite bishops | 5,973,472 | 3,469,344 | 1,552 | 5,320 | 7,952, canonical 994 | 0 | 16 |
| KBNvK, light bishop | 12,268,044 | 6,830,292 | 232 | 6,444 | 4,474, canonical 1,121 | 4 local states, 1 canonical | 16 |
| KRvKB(light) | 11,306,596 | 5,916,232 | 3,264 | 48 | 3,740, canonical 935 | 0 | 14 |
| KRvKN | 23,315,984 | 12,535,256 | 9,328 | 48 | 7,168, canonical 896 | 0 | 14 |

The KRvKB(light) and KRvKN rows are not basic mates in the narrow textbook sense, because Black still has a defensive piece. They are nevertheless natural practical endgames for the same reason: if Black is not forced to capture the rook on move one, the entire local material class is cooperatively reachable to a rook mate.

The KRvKN state count is roughly twice the KRvKB(light) count because the knight can occupy all 64 squares, while the light-square bishop is restricted to 32 squares.

White-to-move local reachability is:

| Material class | White-to-move states | Unwinnable white-to-move states | Canonical unwinnable representatives |
|---|---:|---:|---:|
| KRvK | 175,168 | 0 | 0 |
| KQvK | 144,508 | 0 | 0 |
| KBBvK, opposite bishops | 2,504,128 | 24 | 3 |
| KBNvK, light bishop | 5,437,752 | 60 | 15 |
| KRvKB(light) | 5,390,364 | 0 | 0 |
| KRvKN | 10,780,728 | 0 | 0 |

For the rook endgames with a black defender, the corrected classification also
records local roots outside the fixed material-preserving graph: KRvKB(light)
has 8 White-to-move states where White captures the bishop and reduces to KRvK;
KRvKN has 8 already-ended White-to-move states and 24 states where White
captures the knight and reduces to KRvK. The tests replay each such White
capture with Ashlar's legal move generator and then check that the resulting
KRvK position is a verified helpmate root for White.

The nonzero KBBvK and KBNvK rows are local artifacts, not strict-game
exceptions. In their canonical representatives, Black's king is on `a1` and
White is to move, so Black's previous move would have had to be a king move to
`a1`. For the KBBvK representatives, `b1` and `b2` are ruled out by king
adjacency and the only remaining predecessor square `a2` is occupied by a white
bishop. For the KBNvK representatives with a bishop on `a2`, the same occupied
predecessor argument applies. For those with a bishop on `b1`, `b1` is
occupied, `b2` is adjacent to the white king, and `a2` is attacked by the
bishop. Thus none of the canonical representatives has a legal black last move,
and symmetries preserve that conclusion.

## 9. The KBNvK retro-illegal exception

The KBNvK computation found one canonical local counterexample:

<a href="https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1"><img src="https://fen2image.chessvision.ai/8/8/8/8/2N5/8/k1K5/1B6%20b%20-%20-%200%201?turn=black&amp;pov=black" alt="8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1" width="240"></a>

```text
8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1
```

[Open this position on Lichess](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1).

The pieces are White king `c2`, White bishop `b1`, White knight `c4`, Black king `a2`, Black to move. There are four symmetric local states of this type.

Locally, the state is coherent enough to be enumerated. Black is in check from the bishop on `b1`, and Black has a first move that does not immediately capture White's material. However, after that first move, White has no continuation that preserves a future mate: the play either reaches stalemate or Black is forced to capture a white mating piece on Black's next move.

The important observation is that the displayed position is not strictly legal. Since it is Black to move and Black is in check by a bishop adjacent on `b1-a2`, White's last move would have had to create that check. The bishop cannot have moved to `b1` from a legal origin: the diagonal square `a2` is occupied by the black king, and `c2` is occupied by the white king. Nor can the check have been discovered by moving another white piece, because the bishop and king are adjacent and there is no intervening blocker. Thus the position cannot arise from a legal game.

Consequently, this exception should not block the strict chess statement. A strictly legal root cannot have a legal path into a strictly illegal terminal component. For the KBNvK case, the only obstruction found by the local graph appears to be precisely such a retro-illegal artifact.

## 10. Consequence for CHA-style analysis

The intended use is as a sound shortcut inside a larger unwinnability analyzer.

Suppose the intended winner is White and the current material class is one of the verified classes. If it is Black to move, the analyzer checks:

1. Is the position already checkmate or stalemate? If so, handle that terminal result directly.
2. Does Black have a legal first move that preserves White's mating material?
3. If yes, return "winnable for White" by the material-class certificate.
4. If no, all legal first moves capture a mating piece; analyze the resulting reduced material instead.

For White to move, a similar certificate can be used by following the stored witness move directly, or by reducing to the black-to-move statement after one White move, depending on how the shortcut is integrated.

This does not replace CHA. It supplies a small, mechanically checked lemma for positions where general helpmate search is overkill.

## 11. Status and next work

The current artifact computes these results with specialized finite-state analyzers. The full test suite currently passes with these counts pinned. Independent certificate verifiers are implemented for all material classes in the table above.

For a paper-quality claim, the artifact should include:

- The dense analyzer.
- The witness-producing verifier.
- The independent verification pass using the standard legal move generator.
- Machine-readable result tables.
- Canonical representative FENs for the forced-capture, stalemate, counterexample, and white-to-move exception classes.
- A short proof that local illegal components cannot contaminate strictly legal game positions.

Once this is done, the core contribution can be stated cleanly:

> In the tested basic and near-basic mating material classes, cooperative reachability to checkmate is guaranteed from every ongoing black-to-move position except where Black is immediately forced to capture the mating material. The only KBNvK local exception is retro-illegal and therefore irrelevant to positions that can arise in a game.

The matching white-to-move conclusion is: every strictly game-legal, ongoing
white-to-move position in the same material classes is winnable for White. The
only local white-to-move exceptions found by enumeration are retro-illegal and
therefore outside the strict game domain.

## References

Miguel Ambrona. "A Practical Algorithm for Chess Unwinnability." 11th International Conference on Fun with Algorithms (FUN 2022), LIPIcs 226, 2:1-2:20. https://doi.org/10.4230/LIPIcs.FUN.2022.2

Chess Helpmate Analyzer (CHA). https://chasolver.org/

FIDE Laws of Chess, effective 1 January 2023. https://handbook1090.fide.com/chapter/E012023
