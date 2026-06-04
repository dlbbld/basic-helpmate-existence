# Basic Checkmate Reachability

This project delivers a finite-state proof by code for basic helpmate reachability for all legal positions in selected low-material chess endgames. It is supplemented by machine-checkable algorithms for determining few positions as illegal by last move, where the theorem does not hold.

The covered material classes are `KRvK`, `KQvK`, `KBBvK` with opposite-coloured bishops, `KBNvK`, `KRvKB`, and `KRvKN`, together with their colour-reversed counterparts.

## Theorem

Let `M` be the side with the mating material, and let `D` be the defending side. In every ongoing legal position in the material classes above:

1.  If `M` is to move, then `M` has a helpmate.

2.  If `D` is to move, then `M` has a helpmate unless `D` has one or two legal moves and every legal move captures one of `M`'s pieces.


Equivalently, spelled out by colour:

* White has the mating material: if White is to move, White has a helpmate; if Black is to move, White has a helpmate unless Black has one or two legal moves and every legal move captures one of White's pieces.

* Black has the mating material: if Black is to move, Black has a helpmate; if White is to move, Black has a helpmate unless White has one or two legal moves and every legal move captures one of Black's pieces.

### Ongoing legal positions

The theorem applies only to ongoing legal positions. If the position is already checkmate or stalemate, the game has already ended and the helpmate-existence question is not applicable. Legal means that the position can arise from the initial chess position by a legal series of moves. There are a few illegal positions where the theorem does not apply to. These illegal positions are provided and are determined as illegal by machine-checkable algorithms.

### Finite-state proof

The computation is performed with White as `M` for the six material classes listed above. The corresponding Black-side statements follow by colour symmetry of chess. Swapping White and Black preserves legal moves, captures, checkmate, and helpmate existence.

The finite-state proof by code covers the light-square bishop case for `KBNvK` and `KRvKB`. The dark-square bishop case is obtained by board symmetry, so the light-square computation is sufficient.

## Motivation and difficulty

In chess, the FIDE rule on flag fall asks whether the opponent still has a helpmate; only then is the game a loss for the flagging player. This motivates checking whether the side with mating material has any possible cooperative continuation to checkmate, without having to construct such a continuation during adjudication.

For familiar material classes this sounds almost obvious. One might try to give a direct geometric proof by separating cases such as "king in the corner", "king on the edge", and "king in the middle". In practice this is surprisingly fragile. Side to move, immediate checks, stalemates, forced captures, and positions being illegal or not all matter.

The theorem is therefore proved by checking all positions in the finite material-class graph instead of relying on a hand proof over geometric cases.

A possible use is for an algorithm proving winnability for such positions. Instead of proving winnability by explicitly constructing a helpmate, the algorithm can rely on this theorem, and as such for these positions determine winnability more efficiently.

## Terms

### Legal and illegal position

The FIDE laws of chess call a position which cannot arise from the starting position by a series of legal moves an "illegal position". We call a position that can arise from the starting position by a series of legal moves a "legal position".

### Potentially legal position

When putting pieces on the board, one usually only checks that the king of the player not having the move is not in check. We call such a position a "potentially legal position". In most cases, such a potentially legal position is also a legal position. This is however not generally the case, there are potentially legal positions which are illegal. Later an example relevant to the theorem of KBNvK is given.

### Representative position

A position can have up to eight symmetric positions by mirroring and rotation. We call one chosen member of such a symmetry class a "representative position". Counts below are given both for all positions and for positions reduced to one representative per symmetry class.

## Illegal positions not satisfying the conclusion

The finite-state proof checks the theorem for all potentially legal positions. If the theorem holds, that is fine. We must not determine if it is legal or illegal. If it is illegal the check just proves more than it must, which is not a problem. If the theorem does not hold for a potentially legal position, we must however show that the position is illegal. The following shows representatives for the potentially legal positions where the theorem does not hold and shows that they are illegal.

For each such representative we give the chess reason in text and also check the reason mechanically in the tests. These machine-checkable algorithms are not creating a proof game. They formalize the human-checkable arguments by checking the last move that the position must be illegal, and so make these arguments verifiable by code.

### White to move

The following are the potentially legal positions which do not satisfy the conclusion for White to move. A manual analysis proves all of them to be illegal.

#### KBBvK, opposite bishops

| No. | Material class | Representative position | Status |
| --- | --- | --- | --- |
| 1 | `KBBvK`, opposite bishops | [![8/8/8/8/8/B7/B7/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/B7/B7/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B7/k1K5_w_-_-_0_1)<br>`8/8/8/8/8/B7/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B7/k1K5_w_-_-_0_1) | illegal position |
| 2 | `KBBvK`, opposite bishops | [![8/8/8/8/8/B7/B1K5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/B7/B1K5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B1K5/k7_w_-_-_0_1)<br>`8/8/8/8/8/B7/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B1K5/k7_w_-_-_0_1) | illegal position |
| 3 | `KBBvK`, opposite bishops | [![8/8/8/8/8/8/B1K5/k1B5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/B1K5/k1B5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k1B5_w_-_-_0_1)<br>`8/8/8/8/8/8/B1K5/k1B5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k1B5_w_-_-_0_1) | illegal position |

These three representatives are illegal. Since White is to move and Black has only a king, Black's previous move would have had to be a king move to `a1` in the displayed representatives. The only possible predecessor square not immediately ruled out by king adjacency is `a2`, but in each `KBBvK` representative `a2` is occupied by a white bishop. Hence there is no legal black last move, so the position must be illegal.

#### KBNvK, light bishop

| No. | Material class | Representative position | Status |
| --- | --- | --- | --- |
| 1 | `KBNvK`, light bishop | [![8/8/8/8/8/8/B7/k1KN4 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/B7/k1KN4%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/B7/k1KN4_w_-_-_0_1)<br>`8/8/8/8/8/8/B7/k1KN4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B7/k1KN4_w_-_-_0_1) | illegal position |
| 2 | `KBNvK`, light bishop | [![8/8/8/8/8/3N4/B7/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/3N4/B7/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B7/k1K5_w_-_-_0_1)<br>`8/8/8/8/8/3N4/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B7/k1K5_w_-_-_0_1) | illegal position |
| 3 | `KBNvK`, light bishop | [![8/8/8/8/2N5/8/B7/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/2N5/8/B7/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B7/k1K5_w_-_-_0_1)<br>`8/8/8/8/2N5/8/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B7/k1K5_w_-_-_0_1) | illegal position |
| 4 | `KBNvK`, light bishop | [![8/8/8/8/N7/8/B7/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/N7/8/B7/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B7/k1K5_w_-_-_0_1)<br>`8/8/8/8/N7/8/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B7/k1K5_w_-_-_0_1) | illegal position |
| 5 | `KBNvK`, light bishop | [![8/8/8/8/8/8/BN6/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/BN6/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/BN6/k1K5_w_-_-_0_1)<br>`8/8/8/8/8/8/BN6/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/BN6/k1K5_w_-_-_0_1) | illegal position |
| 6 | `KBNvK`, light bishop | [![8/8/8/8/8/8/B1K5/k2N4 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/B1K5/k2N4%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k2N4_w_-_-_0_1)<br>`8/8/8/8/8/8/B1K5/k2N4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k2N4_w_-_-_0_1) | illegal position |
| 7 | `KBNvK`, light bishop | [![8/8/8/8/8/3N4/B1K5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/3N4/B1K5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B1K5/k7_w_-_-_0_1)<br>`8/8/8/8/8/3N4/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B1K5/k7_w_-_-_0_1) | illegal position |
| 8 | `KBNvK`, light bishop | [![8/8/8/8/2N5/8/B1K5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/2N5/8/B1K5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B1K5/k7_w_-_-_0_1)<br>`8/8/8/8/2N5/8/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B1K5/k7_w_-_-_0_1) | illegal position |
| 9 | `KBNvK`, light bishop | [![8/8/8/8/N7/8/B1K5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/N7/8/B1K5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B1K5/k7_w_-_-_0_1)<br>`8/8/8/8/N7/8/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B1K5/k7_w_-_-_0_1) | illegal position |
| 10 | `KBNvK`, light bishop | [![8/8/8/8/8/8/BNK5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/BNK5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/BNK5/k7_w_-_-_0_1)<br>`8/8/8/8/8/8/BNK5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/BNK5/k7_w_-_-_0_1) | illegal position |
| 11 | `KBNvK`, light bishop | [![8/8/8/8/8/8/2K5/kB1N4 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/2K5/kB1N4%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/2K5/kB1N4_w_-_-_0_1)<br>`8/8/8/8/8/8/2K5/kB1N4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/2K5/kB1N4_w_-_-_0_1) | illegal position |
| 12 | `KBNvK`, light bishop | [![8/8/8/8/8/3N4/2K5/kB6 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/3N4/2K5/kB6%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/2K5/kB6_w_-_-_0_1)<br>`8/8/8/8/8/3N4/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/2K5/kB6_w_-_-_0_1) | illegal position |
| 13 | `KBNvK`, light bishop | [![8/8/8/8/2N5/8/2K5/kB6 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/2N5/8/2K5/kB6%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/2K5/kB6_w_-_-_0_1)<br>`8/8/8/8/2N5/8/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/2K5/kB6_w_-_-_0_1) | illegal position |
| 14 | `KBNvK`, light bishop | [![8/8/8/8/N7/8/2K5/kB6 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/N7/8/2K5/kB6%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/N7/8/2K5/kB6_w_-_-_0_1)<br>`8/8/8/8/N7/8/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/2K5/kB6_w_-_-_0_1) | illegal position |
| 15 | `KBNvK`, light bishop | [![8/8/8/8/8/8/1NK5/kB6 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/1NK5/kB6%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/1NK5/kB6_w_-_-_0_1)<br>`8/8/8/8/8/8/1NK5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/1NK5/kB6_w_-_-_0_1) | illegal position |

These representatives are illegal by the same last-move idea. For the rows with a bishop on `a2`, a black king coming from `a2` is impossible because `a2` is occupied. For the rows with a bishop on `b1`, `b1` is occupied, `b2` is adjacent to the white king, and `a2` is attacked by the bishop. Hence again there is no legal black last move and the position must be illegal. Board symmetries preserve these arguments.

#### Machine-checkable illegality algorithm

1. Black made the last move, because White is to move.
2. Black has only a king in `KBBvK` and `KBNvK`, so Black's last move must have been a king move.
3. The algorithm enumerates every adjacent source square of the current black king square.
4. A source square is rejected if it is occupied now, adjacent to the white king, or still attacked by White even when the current black-king square is treated as a possible capture blocker.
5. If every adjacent source square is rejected, then there is no possible last black king move, so the position is illegal.

### Black to move

#### KBNvK, light bishop

The `KBNvK` case shows why a pure hand proof is dangerous. Here the theorem holds for all potentially legal position except four, which are symnetric, defined by the below representative: Black is not forced to capture a white piece on the first move, but White then cannot avoid the stalemate. However it is shown that this position is illegal, so the theorem holds.

| No. | Material class | Representative position | Status |
| --- | --- | --- | --- |
| 1 | `KBNvK(light bishop)` | [![8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/2N5/8/k1K5/1B6%20b%20-%20-%200%201?turn=black&pov=black)](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1)<br>`8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1) | illegal position |

As mentioned this is not a counterexample. The catch is that this position is illegal. Black is in check from the bishop on `b1`. If the position had arisen in a legal game, White's last move would have had to create that check. But the bishop cannot have moved to `b1`: the diagonal squares from which it could have arrived are blocked by the black king on `a2` and the white king on `c2`. Nor can the check have been discovered, because the bishop is already adjacent to the black king with no intervening square. So the position cannot arise from the normal starting position, it is illegal, and the conclusions remain applicable.

#### Machine-checkable illegality algorithm

1. White made the last move, because Black is to move.
2. Black is in check, so White's last move must either move a checking piece to its current square or uncover a discovered check.
3. The certificate verifies that there is a single checking white piece and that this piece is adjacent to the black king.
4. Because the checker is adjacent to the king, no discovered check is possible: there is no square between checker and king from which a blocker could have moved away.
5. Therefore the checking piece itself must have moved to its present square.
6. The certificate enumerates that checking piece's possible source squares.
7. If every source ray is blocked immediately, then the checking piece has no possible source square, so the position is illegal.

## Verification

The analyzer stores one witness move for every winning non-terminal state. The independent verifier then checks:

1.  each terminal seed is a real Black-checkmate position;
    
2.  each recorded witness is generated by `BitboardPosition.legalMoves(...)`;
    
3.  the witness reaches the recorded successor;
    
4.  the witness preserves the material class;
    
5.  the successor is closer to a terminal mate layer.
    

Thus the JUnit tests do not merely sample examples. They exhaust the finite state spaces and verify the recorded certificate edges with Ashlar's normal legal move generator.

Run:

```sh
mvn test

```

On a notebook with a per 2026 average performance, this test suite takes roughly two minutes.

## Current Position Counts for White to move

"Maximum helpmate plies" is the largest number of legal plies in the stored helpmate path to checkmate inside the fixed material class.

### All potentially legal positions

| Material class | Potentially legal positions | Checkmates | Stalemates | Counterexamples | Maximum helpmate plies |
| --- | --- | --- | --- | --- | --- |
| `KRvK` | 175,168 | 0 | 0 | 0 | 13 |
| `KQvK` | 144,508 | 0 | 0 | 0 | 13 |
| `KBBvK`, opposite bishops | 2,504,128 | 0 | 0 | 24 illegal | 15 |
| `KBNvK`, light bishop | 5,437,752 | 0 | 0 | 60 illegal | 15 |
| `KRvKB(light bishop)` | 5,390,364 | 0 | 0 | 0 | 13 |
| `KRvKN` | 10,780,728 | 8 | 0 | 0 | 13 |

The nonzero white-to-move counterexample rows are the potentially legal positions where the theorem does not hold. The representative cases are illegal by a last-move black-king argument, so they are not exceptions to the strict-game statement. 

### Potentially legal positions reduced to representative cases

| Material class | Potentially legal positions | Checkmates | Stalemates | Counterexamples | Maximum helpmate plies |
| --- | --- | --- | --- | --- | --- |
| `KRvK` | 21,959 | 0 | 0 | 0 | 13 |
| `KQvK` | 18,081 | 0 | 0 | 0 | 13 |
| `KBBvK`, opposite bishops | 626,032 | 0 | 0 | 3 illegal | 15 |
| `KBNvK`, light bishop | 1,359,578 | 0 | 0 | 15 illegal | 15 |
| `KRvKB(light bishop)` | 1,347,906 | 0 | 0 | 0 | 13 |
| `KRvKN` | 1,347,906 | 1 | 0 | 0 | 13 |

## Current Position Counts for Black to move

The forced first capture exception is split by the number of legal moves available to the defending side. No exception has more than two legal defending moves, so a position with three or more legal defending moves is immediately outside the exception. 

### All potentially legal positions

| Material class | Potentially legal positions | Checkmates | Stalemates | Forced first capture: one move | Forced first capture: two moves | Counterexamples | Maximum helpmate plies |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `KRvK` | 223,944 | 216 | 68 | 412 | 0 | 0 | 14 |
| `KQvK` | 223,944 | 364 | 872 | 2,420 | 0 | 0 | 14 |
| `KBBvK`, opposite bishops | 3,469,344 | 1,552 | 5,320 | 7,312 | 640 | 0 | 16 |
| `KBNvK`, light bishop | 6,830,292 | 232 | 6,444 | 4,042 | 432 | 4, 1 representative, illegal | 16 |
| `KRvKB(light bishop)` | 5,916,232 | 3,264 | 48 | 3,152 | 588 | 0 | 14 |
| `KRvKN` | 12,535,256 | 9,328 | 48 | 6,848 | 320 | 0 | 14 |

### Potentially legal positions reduced to representative cases

| Material class | Potentially legal positions | Checkmates | Stalemates | Forced first capture: one move | Forced first capture: two moves | Counterexamples | Maximum helpmate plies |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `KRvK` | 28,056 | 27 | 9 | 54 | 0 | 0 | 14 |
| `KQvK` | 28,056 | 46 | 109 | 305 | 0 | 0 | 14 |
| `KBBvK`, opposite bishops | 867,336 | 194 | 665 | 914 | 80 | 0 | 16 |
| `KBNvK`, light bishop | 1,707,888 | 58 | 1,611 | 1,013 | 108 | 1 illegal | 16 |
| `KRvKB(light bishop)` | 1,479,198 | 816 | 12 | 788 | 147 | 0 | 14 |
| `KRvKN` | 1,567,222 | 1,166 | 6 | 856 | 40 | 0 | 14 |

## External Cross-Checks

The potentially legal position counts can be checked against the Syzygy tablebases. Syzygy uses Kirill Kryukov's [Number of Unique Legal Positions](https://kirill-kryukov.com/chess/nulp/) (NULP) definition. In that definition, a position includes side to move, castling rights, and en-passant rights, and "unique" means an equivalence class under easy symmetries such as board mirroring, board rotation, and color swapping. NULP looks at potentially legal positions as we do, thus the comparison is valid.

For example the potentially legal but in fact illegal positions `8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1` and `8/8/8/8/8/B7/B7/k1K5 w - - 0 1` are in the Syzygy tablebase.

### Counts

The Syzygy site displays aggregate WDL outcomes, while its [machine-readable statistics](https://syzygy-tables.info/stats.json) keep the side to move separated. For example, the displayed `KRvK` value of 47,219 White wins is `21,959` White-to-move wins plus `25,260` Black-to-move losses.

The table below gives the corresponding unique representative counts. The unique count is not always the raw count divided by 8, because symnetry operations can lead to identical positions.

| Material class | Scope compared | Raw White to move | Raw Black to move | Raw total | Unique White to move | Unique Black to move | Unique total | Syzygy unique total | Comparison |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `KRvK` | theorem class | 175,168 | 223,944 | 399,112 | 21,959 | 28,056 | 50,015 | 50,015 | matches |
| `KQvK` | theorem class | 144,508 | 223,944 | 368,452 | 18,081 | 28,056 | 46,137 | 46,137 | matches |
| `KBNvK(light bishop)` | light-bishop theorem class, equivalent to Syzygy `KBNvK` after bishop-colour symmetry | 5,437,752 | 6,830,292 | 12,268,044 | 1,359,578 | 1,707,888 | 3,067,466 | 3,067,466 | matches |
| `KRvKB(light bishop)` | light-bishop theorem class, equivalent to Syzygy `KRvKB` after bishop-colour symmetry | 5,390,364 | 5,916,232 | 11,306,596 | 1,347,906 | 1,479,198 | 2,827,104 | 2,827,104 | matches |
| `KRvKN` | theorem class | 10,780,728 | 12,535,256 | 23,315,984 | 1,347,906 | 1,567,222 | 2,915,128 | 2,915,128 | matches |
| `KBBvK`, opposite bishops | converted to the same generic two-bishop-slot convention as Syzygy | 5,008,256 | 6,938,688 | 11,946,944 | 626,032 | 867,336 | 1,493,368 | n/a | generated subset |
| `KBBvK`, same-colour bishops | generated in the same generic two-bishop-slot convention as Syzygy | 5,155,800 | 6,721,896 | 11,877,696 | 644,510 | 840,552 | 1,485,062 | n/a | generated subset |
| `KBBvK`, sum | sum of the two preceding `KBBvK` rows | 10,164,056 | 13,660,584 | 23,824,640 | 1,270,542 | 1,707,888 | 2,978,430 | 2,978,430 | matches |

The `KBBvK` line needs special care. The theorem is only about opposite-coloured bishops, with one light-square bishop and one dark-square bishop. The Syzygy material key `KBBvK` does not expose a separate statistic for opposite-coloured bishops; it counts the whole two-bishop material table in a generic two-bishop-slot convention. For that external comparison, the opposite-bishop subset is therefore converted to the same convention, the same-colour bishop subset is generated separately, and their sum matches Syzygy exactly.

As a note the Syzygy WDL tables is adversarial tablebase so it cannot be used to deduce helpmate existence.

The regression test `TestSyzygyCountCrossCheck` independently recomputes all counts in this section without calling the main reachability analyzers.

### Position

Count agreement alone would still leave a theoretical worry: two different position sets can have the same size. For that reason the repository also contains an optional pointwise Syzygy probe:

```sh
python scripts/verify_syzygy_position_sets.py

```

The script generates one representative per relevant board-symmetry class and probes each representative against local Syzygy files with python-chess. It does not use the main reachability analyzers. Each generated record carries explicit piece symbols, and the script checks the actual Syzygy material key of the generated board before probing the WDL table for that key. With `--isolate-table`, the script creates a temporary directory for each selected material containing only that material's `.rtbw` and `.rtbz` files, so a wrong generated material cannot be satisfied by another table in the source directory. Missing table files fail with `FileNotFoundError`; python-chess does not download tables. On 30 May 2026, it was run against the local Syzygy files in with these results:

| Material class | Unique representatives probed |
| --- | --- |
| `KRvK` | 50,015 |
| `KQvK` | 46,137 |
| `KBNvK(light bishop)` | 3,067,466 |
| `KRvKB(light bishop)` | 2,827,104 |
| `KRvKN` | 2,915,128 |
| `KBBvK(opposite bishops)` | 1,493,368 |
| `KBBvK`, all ordered bishop slots | 2,978,430 |

Every generated representative probed successfully. Together with the matching Syzygy/NULP unique counts, this gives a pointwise cross-check that the generated potentially legal positions are sound and complete. The six theorem classes are probed directly; the expanded all-bishop `KBBvK` row is an additional check for the full Syzygy material table.

## Dependency

This project uses the Maven Central release of Ashlar Chess:

```xml
<dependency>
  <groupId>io.github.dlbbld</groupId>
  <artifactId>ashlar-chess</artifactId>
  <version>17.0.0</version>
</dependency>

```
