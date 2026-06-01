# A finite-state certificate for basic-checkmate reachability in chess

Daniel Bächli  
Mathematician, chess player, and author of Clean Chess

Draft, 20 May 2026

## Abstract

FIDE's dead-position rule is phrased in terms of existence: a position is dead if neither player can checkmate the opponent by any possible series of legal moves. This is the right chess rule, but it is not a small local material rule. Miguel Ambrona's Chess Helpmate Analyzer (CHA) gives a practical and sound approach to the problem by searching for helpmate sequences and by recognizing large classes of unwinnable positions statically. In high-level implementations, however, some elementary endgames can still be annoying: the position is obviously part of a classical mating material class, but a blind helpmate search may need to discover a long cooperative line.

This note proposes a finite-state supplement for such cases. For selected basic-checkmate material classes, we enumerate the complete local legal state graph and compute, by retrograde propagation from checkmate states, whether the side with mating material has a cooperative continuation to checkmate. Let `W` be the side with mating material, the winner in the timeout application, and let `L` be the defending side. The proved statement is: in every ongoing legal position in the covered classes, if `W` is to move then `W` has a helpmate; if `L` is to move then `W` has a helpmate unless `L` has one or two legal moves and every legal first move by `L` captures one of `W`'s pieces. The computation is performed with White as `W` for KRvK, KQvK, KBBvK with opposite-coloured bishops, KBNvK with a light-square bishop modulo one retro-illegal local exception, KRvKB(light), and KRvKN. The corresponding statements with Black as `W` follow by colour symmetry.

## 1. Motivation

Article 5.2.2 of the FIDE Laws states that the game is drawn when a position has arisen in which neither player can checkmate the opponent's king by any series of legal moves. The same existential phrase appears in timeout adjudication: under Article 6.9, a player normally loses on time, but the game is drawn if the opponent cannot checkmate by any possible series of legal moves. FIDE also distinguishes a legal move from a legal position: Article 3.10.3 says that a position is illegal if it cannot have been reached by any series of legal moves.

The computational problem is therefore not "does the side have enough material in ordinary chess folklore?" It is:

> Does there exist at least one legal continuation, with both players allowed to cooperate, that ends in checkmate by the intended winner?

This is a helpmate reachability question. A negative answer means the intended winner cannot win even with the opponent's cooperation.

Ambrona's CHA is a practical answer to this problem. Its public description says that CHA checks whether there exists a sequence of legal moves allowing a chosen player to checkmate, combining a search of variations with static analysis. The FUN 2022 paper proves soundness of the algorithmic approach. CHA is therefore the natural reference point for Clean Chess: it turns a difficult FIDE rule into an implementable algorithm.

The small gap addressed here is not mathematical completeness of CHA. It is engineering cost in elementary endgames. Some basic mates are known to be mating material, but if the implementation treats them only as ordinary helpmate searches, it may spend many recursive calls rediscovering a line. This draft describes a finite precomputation that can be used as a shortcut and, with a verifier, as a certificate-backed theorem for those material classes.

## 2. Terminology

Throughout this draft, "`W` has a helpmate" means:

> There exists a finite sequence of legal moves, starting from the given position, after which the opponent's king is checkmated.

This is cooperative reachability, not adversarial winning. `L` is not trying to avoid mate. `L` is only required to make legal moves.

The word "legal" has two layers.

Local legality means that the pieces occupy distinct squares, kings are not mutually attacking, the side-to-move convention is consistent with check, and every edge in the graph is a legal chess move in the usual move-generator sense.

Strict game legality means that the position can arise from the normal initial chess position by a legal game. FIDE Article 3.10.3 uses this stronger meaning. The present computation enumerates local legal states, not proof games from the initial position.

This distinction matters, but it does not invalidate the intended shortcut. If a strictly legal position reaches another position by legal moves, the target is also strictly legal. Therefore, a strictly illegal checkmate seed cannot create a false helpmate path from a strictly legal root when all graph edges are genuine legal moves. Illegal components remain outside the game-reachable component.

## 3. The statement proved

Let `W` be the side with the mating material, the winner in the timeout application, and let `L` be the defending side. In every ongoing legal position in the covered material classes:

1. If `W` is to move, then `W` has a helpmate.
2. If `L` is to move, then `W` has a helpmate unless `L` has one or two legal moves and every legal first move by `L` captures one of `W`'s pieces.

For White as the mating side this says:

1a. White to move: White has a helpmate.

1b. Black to move: White has a helpmate unless Black has one or two legal moves and every legal first move by Black captures one of White's pieces.

For Black as the mating side this says:

2a. Black to move: Black has a helpmate.

2b. White to move: Black has a helpmate unless White has one or two legal moves and every legal first move by White captures one of Black's pieces.

The computation is performed with White as `W` for KRvK, KQvK, KBBvK with opposite-coloured bishops, KBNvK, KRvKB, and KRvKN. The Black-side statements follow by colour symmetry of chess: swapping White and Black preserves legal moves, captures, checkmate, and helpmate existence. The light-square bishop cases cover the corresponding dark-square bishop cases by board symmetry.

## 4. High-level algorithmic picture

There are two algorithms in the artifact.

The first algorithm finds the theorem. For a fixed material class, it builds the finite directed graph of all locally legal states in that material class. It then starts from every black-checkmate state and works backwards through legal moves. Whenever a state can legally move to a state already known to reach mate, it is also marked as reaching mate. This is a retrograde reachability computation: instead of trying a fresh helpmate search from each root, it computes the whole material class at once.

The second algorithm verifies the theorem. During the retrograde computation, the analyzer stores one witness move for each non-terminal state it marks as winning. The verifier does not trust the graph construction blindly. It reconstructs each source position, asks Ashlar Chess's ordinary legal move generator for the legal moves, checks that the stored witness is one of them, checks that it reaches the recorded successor, and checks that the successor is strictly closer to a recorded checkmate seed. The verifier therefore turns the computed table into a certificate: every accepted state has a finite chain of ordinary legal moves to mate.

The recorded distance is the length of this shortest witnessed chain in plies inside the material-preserving graph. The maximum witness distance is not a rule-theoretic depth-to-mate under best defense. It is the largest certificate layer needed by the cooperative reachability proof.

## 5. The finite material-class graph

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

The following table reports the current computed results. Counts are over local legal states. Canonical counts quotient by board symmetries appropriate to the material class. The forced first capture exception is split by the number of legal moves available to the defending side; no exception has more than two legal defending moves.

| Material class | Legal states | Black-to-move states | Black checkmates | Stalemates | Forced first capture: one move | Forced first capture: two moves | Counterexamples | Maximum witness distance |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| KRvK | 399,112 | 223,944 | 216 | 68 | 412, canonical 54 | 0, canonical 0 | 0 | 14 |
| KQvK | 368,452 | 223,944 | 364 | 872 | 2,420, canonical 305 | 0, canonical 0 | 0 | 14 |
| KBBvK, opposite bishops | 5,973,472 | 3,469,344 | 1,552 | 5,320 | 7,312, canonical 914 | 640, canonical 80 | 0 | 16 |
| KBNvK, light bishop | 12,268,044 | 6,830,292 | 232 | 6,444 | 4,042, canonical 1,013 | 432, canonical 108 | 4 local states, 1 canonical | 16 |
| KRvKB(light) | 11,306,596 | 5,916,232 | 3,264 | 48 | 3,152, canonical 788 | 588, canonical 147 | 0 | 14 |
| KRvKN | 23,315,984 | 12,535,256 | 9,328 | 48 | 6,848, canonical 856 | 320, canonical 40 | 0 | 14 |

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

As an external count check, the state spaces agree with the Syzygy statistics
after putting both computations in the same quotient space. Syzygy uses Kirill
Kryukov's Number of Unique Legal Positions (NULP) convention: positions include
side to move, and uniqueness quotients by easy symmetries such as board
mirroring, board rotation, and color swapping. Its machine-readable statistics
also keep the side to move separated. For KRvK, for example, the displayed
47,219 White wins are 21,959 White-to-move wins plus 25,260 Black-to-move
losses.

The comparison is exact for KRvK, KQvK, KBNvK, KRvKB, and KRvKN once the
appropriate symmetry quotient is used. For light-bishop classes, the
light-square restriction is equivalent to the unrestricted Syzygy bishop table
after bishop-colour symmetry. KBBvK is the special case: this paper proves only
the opposite-coloured bishop subset, while Syzygy exposes only the whole KBBvK
material table. The opposite-bishop subset has 1,493,368 unique representatives;
the expanded all-bishop table has 2,978,430, matching Syzygy exactly. The
artifact includes a regression test that independently recomputes these
bridging counts without calling the main reachability analyzers.

This comparison is deliberately local, not strict-game retro-legal. The
retro-illegal examples found in the artifact are still probeable in Syzygy,
which is consistent with NULP legality and with our local enumeration.

The artifact also includes an optional pointwise probe against local Syzygy
files. It generates one representative per relevant symmetry class and probes
each representative with python-chess. In a local run on 30 May 2026, every
generated representative probed successfully for KRvK, KQvK, KBNvK(light),
KRvKB(light), KRvKN, and the expanded all-bishop KBBvK Syzygy table. This is a
stronger check than count equality alone: combined with matching unique counts,
it verifies that the generated local position sets sit inside the corresponding
Syzygy local position sets with the same cardinality.

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

This retro-legality step is not merely mechanical bookkeeping. The finite graph
and witness verification are mechanical, but assessing possible counterexamples
as not strictly legal still requires chess-specific retro intuition unless a
separate full proof-game classifier is built. The difficulty is that apparent
last-move impossibilities can be explained by captures. For example,
`4kR2/8/4K3/8/8/8/8/8 b - - 0 1` may look suspicious in isolation: Black is to
move and in check from a rook on `f8`. But it can arise from
`4kb1R/8/4K3/8/8/8/8/8 w - - 0 1` by `Rxf8+`, with the rook from `h8`
capturing a black bishop on `f8`. Therefore a mechanical strict-legality test
cannot only search for quiet origins of the checking piece; it must also account
for possible captured material, which makes the general retro problem much more
challenging.

Consequently, this exception should not block the strict chess statement. A strictly legal root cannot have a legal path into a strictly illegal terminal component. For the KBNvK case, the only obstruction found by the local graph appears to be precisely such a retro-illegal artifact.

## 10. Consequence for CHA-style analysis

The intended use is as a sound shortcut inside a larger unwinnability analyzer.

Suppose the intended winner is White and the current material class is one of the verified classes. If it is Black to move, the analyzer checks:

1. Is the position already checkmate or stalemate? If so, handle that terminal result directly.
2. Does Black have three or more legal moves? If so, return "winnable for White" by the material-class certificate.
3. Otherwise, does Black have a legal first move that preserves White's mating material?
4. If yes, return "winnable for White" by the material-class certificate.
5. If no, all legal first moves capture a mating piece; analyze the resulting reduced material instead.

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
