# Basic Checkmate Reachability

This project delivers a finite-state proof by code, supplemented by manually proving some positions as illegal, for two strict-game conclusions for ongoing games in the material classes KRvK, KQvK, KBBvK (opposite bishops), KBNvK, KRvKB and KRvKN.

First conclusion: White having the move in such positions always has a helpmate.

Second conclusion: With Black having the move, White has a helpmate except for the positions where Black must forcibly capture a piece on its first move.

That the game is ongoing is of course a necessary condition. If the position is checkmate or stalemate, the game has already ended - the question for helpmate existence in this case is not applicable.

Most notably the theorem only applies to positions which are legal, that is, positions which can arise from the initial chess position by a legal series of moves.

## Extension

The finite-state proof by code covers the light-square bishop case for `KBNvK` and `KRvKB`. Analyzing the light-square bishop case is enough because the dark-square bishop case is obtained by board symmetry. If Black, rather than White, has the mating material, the statement is obtained by swapping the colours. This is the standard reduction to one representative from each symmetry class. As such, the statement also applies analogously to the material classes KvKR, KvKQ, KvKBB (opposite bishops), KvKBN, KBvKR and KNvKR.

## Motivation

In chess, the FIDE rule on flag fall asks whether the opponent still has a helpmate; only then is the game a loss for the flagging player. This motivates checking the case where White has mating material and Black flags. The conclusion identifies when Black loses, by performing one move, without having to construct a helpmate.

For familiar material classes this sounds almost obvious. One might try to give a direct geometric proof by separating cases such as "king in the corner", "king on the edge", and "king in the middle". In practice this is surprisingly fragile. Side to move, immediate checks, stalemates, forced captures, and positions being illegal or not all matter.

## Terms

### Potentially legal position

When putting the pieces on the board, one usually only checks that the king of the player not having the move is not in check (so was not exposed to check or left in check on the last move). We call such positions potentially legal.

### Legal and illegal position

The FIDE laws of chess call a position which cannot arise from the starting position by a series of legal moves illegal. We call a position that has arisen from the starting position by a series of legal moves legal. So a position that is not illegal is called legal.

### Representative position

A position can have up to eight symmetric positions by mirroring and rotation. We apply the standard reduction to check one such symmetric position only for each symmetry class. We call that a representative position.

## Illegal positions not satisfying the conclusion

The above conclusions are not correct for all potentially legal positions. However when the conclusion is not correct for a potentially legal position, we find that for each such case, the potentially legal position is illegal.

### Black to move

The `KBNvK` case shows why a pure hand proof is dangerous. The conclusion below does not hold for the below potentially legal position: Black is not forced to capture a white piece on the first move, but White then cannot avoid the stalemate.

The local graph contains one apparent black-to-move exception:

| Material class | Representative position | Status |
| --- | --- | --- |
| `KBNvK(light bishop)` | [![8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/2N5/8/k1K5/1B6%20b%20-%20-%200%201?turn=black&pov=black)](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1)<br>`8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/k1K5/1B6_b_-_-_0_1) | illegal |

The catch is that this position is illegal. Black is in check from the bishop on `b1`. If the position had arisen in a legal game, White's last move would have had to create that check. But the bishop cannot have moved to `b1`: the diagonal squares from which it could have arrived are blocked by the black king on `a2` and the white king on `c2`. Nor can the check have been discovered, because the bishop is already adjacent to the black king with no intervening square. So the position cannot arise from the normal starting position, it is illegal, and the conclusions remain applicable. In normal play, for example in online chess, the game cannot enter such a position because the system allows only legal moves.

### White to move

The following are the potentially legal positions which do not satisfy the conclusion for White to move. A manual analysis proves all of them to be illegal.

| No. | Material class | Representative position | Status |
| ---: | --- | --- | --- |
| 1 | `KBBvK`, opposite bishops | [![8/8/8/8/8/B7/B7/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/B7/B7/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B7/k1K5_w_-_-_0_1)<br>`8/8/8/8/8/B7/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B7/k1K5_w_-_-_0_1) | illegal |
| 2 | `KBBvK`, opposite bishops | [![8/8/8/8/8/B7/B1K5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/B7/B1K5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B1K5/k7_w_-_-_0_1)<br>`8/8/8/8/8/B7/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/B7/B1K5/k7_w_-_-_0_1) | illegal |
| 3 | `KBBvK`, opposite bishops | [![8/8/8/8/8/8/B1K5/k1B5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/B1K5/k1B5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k1B5_w_-_-_0_1)<br>`8/8/8/8/8/8/B1K5/k1B5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k1B5_w_-_-_0_1) | illegal |
| 1 | `KBNvK`, light bishop | [![8/8/8/8/8/8/B7/k1KN4 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/B7/k1KN4%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/B7/k1KN4_w_-_-_0_1)<br>`8/8/8/8/8/8/B7/k1KN4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B7/k1KN4_w_-_-_0_1) | illegal |
| 2 | `KBNvK`, light bishop | [![8/8/8/8/8/3N4/B7/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/3N4/B7/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B7/k1K5_w_-_-_0_1)<br>`8/8/8/8/8/3N4/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B7/k1K5_w_-_-_0_1) | illegal |
| 3 | `KBNvK`, light bishop | [![8/8/8/8/2N5/8/B7/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/2N5/8/B7/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B7/k1K5_w_-_-_0_1)<br>`8/8/8/8/2N5/8/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B7/k1K5_w_-_-_0_1) | illegal |
| 4 | `KBNvK`, light bishop | [![8/8/8/8/N7/8/B7/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/N7/8/B7/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B7/k1K5_w_-_-_0_1)<br>`8/8/8/8/N7/8/B7/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B7/k1K5_w_-_-_0_1) | illegal |
| 5 | `KBNvK`, light bishop | [![8/8/8/8/8/8/BN6/k1K5 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/BN6/k1K5%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/BN6/k1K5_w_-_-_0_1)<br>`8/8/8/8/8/8/BN6/k1K5 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/BN6/k1K5_w_-_-_0_1) | illegal |
| 6 | `KBNvK`, light bishop | [![8/8/8/8/8/8/B1K5/k2N4 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/B1K5/k2N4%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k2N4_w_-_-_0_1)<br>`8/8/8/8/8/8/B1K5/k2N4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/B1K5/k2N4_w_-_-_0_1) | illegal |
| 7 | `KBNvK`, light bishop | [![8/8/8/8/8/3N4/B1K5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/3N4/B1K5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B1K5/k7_w_-_-_0_1)<br>`8/8/8/8/8/3N4/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/B1K5/k7_w_-_-_0_1) | illegal |
| 8 | `KBNvK`, light bishop | [![8/8/8/8/2N5/8/B1K5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/2N5/8/B1K5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B1K5/k7_w_-_-_0_1)<br>`8/8/8/8/2N5/8/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/B1K5/k7_w_-_-_0_1) | illegal |
| 9 | `KBNvK`, light bishop | [![8/8/8/8/N7/8/B1K5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/N7/8/B1K5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B1K5/k7_w_-_-_0_1)<br>`8/8/8/8/N7/8/B1K5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/B1K5/k7_w_-_-_0_1) | illegal |
| 10 | `KBNvK`, light bishop | [![8/8/8/8/8/8/BNK5/k7 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/BNK5/k7%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/BNK5/k7_w_-_-_0_1)<br>`8/8/8/8/8/8/BNK5/k7 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/BNK5/k7_w_-_-_0_1) | illegal |
| 11 | `KBNvK`, light bishop | [![8/8/8/8/8/8/2K5/kB1N4 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/2K5/kB1N4%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/2K5/kB1N4_w_-_-_0_1)<br>`8/8/8/8/8/8/2K5/kB1N4 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/2K5/kB1N4_w_-_-_0_1) | illegal |
| 12 | `KBNvK`, light bishop | [![8/8/8/8/8/3N4/2K5/kB6 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/3N4/2K5/kB6%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/2K5/kB6_w_-_-_0_1)<br>`8/8/8/8/8/3N4/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/3N4/2K5/kB6_w_-_-_0_1) | illegal |
| 13 | `KBNvK`, light bishop | [![8/8/8/8/2N5/8/2K5/kB6 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/2N5/8/2K5/kB6%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/2K5/kB6_w_-_-_0_1)<br>`8/8/8/8/2N5/8/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/2N5/8/2K5/kB6_w_-_-_0_1) | illegal |
| 14 | `KBNvK`, light bishop | [![8/8/8/8/N7/8/2K5/kB6 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/N7/8/2K5/kB6%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/N7/8/2K5/kB6_w_-_-_0_1)<br>`8/8/8/8/N7/8/2K5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/N7/8/2K5/kB6_w_-_-_0_1) | illegal |
| 15 | `KBNvK`, light bishop | [![8/8/8/8/8/8/1NK5/kB6 w - - 0 1](https://fen2image.chessvision.ai/8/8/8/8/8/8/1NK5/kB6%20w%20-%20-%200%201?turn=white&pov=white)](https://lichess.org/analysis/standard/8/8/8/8/8/8/1NK5/kB6_w_-_-_0_1)<br>`8/8/8/8/8/8/1NK5/kB6 w - - 0 1`<br>[Lichess analysis](https://lichess.org/analysis/standard/8/8/8/8/8/8/1NK5/kB6_w_-_-_0_1) | illegal |

The three `KBBvK` representatives are illegal. Since White is to move and Black has only a king, Black's previous move would have had to be a king move to `a1` in the displayed representatives. The only possible predecessor square not immediately ruled out by king adjacency is `a2`, but in each `KBBvK` representative `a2` is occupied by a white bishop. Hence there is no legal black last move.

The `KBNvK` representatives are illegal by the same last-move idea. For the rows with a bishop on `a2`, a black king coming from `a2` is impossible because `a2` is occupied. For the rows with a bishop on `b1`, `b1` is occupied, `b2` is adjacent to the white king, and `a2` is attacked by the bishop. Hence again there is no legal black last move. Board symmetries preserve these arguments.

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

## Current Results for White to move

### All potentially legal positions

White-to-move local reachability:

| Material class | White-to-move states | Unwinnable white-to-move states | Canonical unwinnable representatives |
| --- | --- | --- | --- |
| `KRvK` | 175,168 | 0 | 0 |
| `KQvK` | 144,508 | 0 | 0 |
| `KBBvK`, opposite bishops | 2,504,128 | 24 | 3 |
| `KBNvK`, light bishop | 5,437,752 | 60 | 15 |
| `KRvKB(light bishop)` | 5,390,364 | 0 | 0 |
| `KRvKN` | 10,780,728 | 0 | 0 |

The nonzero white-to-move exception rows are local material-class artifacts. The canonical representatives are illegal by a last-move black-king argument, so they are not exceptions to the strict-game statement. The witness verifier already checks all fixed-material white-to-move winning states, because the stored witness edges are side-to-move agnostic.

For the rook endgames with a black defender, the corrected classification also records non-exceptional local roots outside the fixed material-preserving graph: `KRvKB(light bishop)` has 8 White-to-move states where White captures the bishop and reduces to the verified `KRvK` case; `KRvKN` has 8 already-ended White-to-move states and 24 states where White captures the knight and reduces to `KRvK`. The tests replay each such White capture with Ashlar's legal move generator and then check that the resulting `KRvK` position is a verified helpmate root for White.

### Potentially legal positions reduced to representative cases

| Material class | Representative White-to-move positions | Unwinnable representative cases |
| --- | ---: | ---: |
| `KRvK` | 21,959 | 0 |
| `KQvK` | 18,081 | 0 |
| `KBBvK`, opposite bishops | 626,032 | 3 |
| `KBNvK`, light bishop | 1,359,578 | 15 |
| `KRvKB(light bishop)` | 1,347,906 | 0 |
| `KRvKN` | 1,347,906 | 0 |

## Current Results for Black to move

Counts are over potentially legal positions. "Maximum witness distance" is the largest number of legal plies in the stored helpmate path to checkmate inside the fixed material class. Thus, in the `KRvKB(light bishop)` and `KRvKN` rows, the stored black-to-move witnesses do not use White captures of the black bishop or knight as a shortcut to `KRvK`. Such captures are handled separately in the White-to-move classification, where they are needed only for local positions outside the fixed material-class graph.

### All potentially legal positions

| Material class | Potentially legal states | Black-to-move states | Black checkmates | Stalemates | Forced first capture | Counterexamples | Maximum witness distance |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `KRvK` | 399,112 | 223,944 | 216 | 68 | 412 | 0 | 14 |
| `KQvK` | 368,452 | 223,944 | 364 | 872 | 2,420 | 0 | 14 |
| `KBBvK`, opposite bishops | 5,973,472 | 3,469,344 | 1,552 | 5,320 | 7,952 | 0 | 16 |
| `KBNvK`, light bishop | 12,268,044 | 6,830,292 | 232 | 6,444 | 4,474 | 4 local states, 1 representative, illegal | 16 |
| `KRvKB(light bishop)` | 11,306,596 | 5,916,232 | 3,264 | 48 | 3,740 | 0 | 14 |
| `KRvKN` | 23,315,984 | 12,535,256 | 9,328 | 48 | 7,168 | 0 | 14 |

### Potentially legal positions reduced to representative cases

| Material class | Representative potentially legal states | Representative Black-to-move states | Black checkmates | Stalemates | Forced first capture | Counterexamples | Maximum witness distance |
| --- | ---: | ---: | ---: | ---: | ---: | --- | ---: |
| `KRvK` | 50,015 | 28,056 | 27 | 9 | 54 | 0 | 14 |
| `KQvK` | 46,137 | 28,056 | 46 | 109 | 305 | 0 | 14 |
| `KBBvK`, opposite bishops | 1,493,368 | 867,336 | 194 | 665 | 994 | 0 | 16 |
| `KBNvK`, light bishop | 3,067,466 | 1,707,888 | 58 | 1,611 | 1,121 | 1 illegal | 16 |
| `KRvKB(light bishop)` | 2,827,104 | 1,479,198 | 816 | 12 | 935 | 0 | 14 |
| `KRvKN` | 2,915,128 | 1,567,222 | 1,166 | 6 | 896 | 0 | 14 |

The `KRvKB(light bishop)` and `KRvKN` rows are not basic mates in the narrow textbook sense, because Black still has a defensive piece. They are included because they are natural practical endgames for the same reachability method.

## External Count Cross-Checks

The potentially legal position counts can be checked against the Syzygy tablebases. Syzygy uses Kirill Kryukov's [Number of Unique Legal Positions](https://kirill-kryukov.com/chess/nulp/) (NULP) definition. In that definition, a position includes side to move, castling rights, and en-passant rights, and "unique" means an equivalence class under easy symmetries such as board mirroring, board rotation, and color swapping. NULP looks at potentially legal positions as we do, thus the comparison is valid.

For example the potentially legal but in fact illegal positions `8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1` and `8/8/8/8/8/B7/B7/k1K5 w - - 0 1` are in the Syzygy tablebase.

The Syzygy site displays aggregate WDL outcomes, while its [machine-readable statistics](https://syzygy-tables.info/stats.json) keep the side to move separated. For example, the displayed `KRvK` value of 47,219 White wins is `21,959` White-to-move wins plus `25,260` Black-to-move losses.

The table below gives the corresponding unique representative counts. The unique count is not always the raw count divided by 8, because symmetric positions can have smaller orbits.

| Material class | Scope compared | Raw White to move | Raw Black to move | Raw total | Unique White to move | Unique Black to move | Unique total | Syzygy unique total | Comparison |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `KRvK` | theorem class | 175,168 | 223,944 | 399,112 | 21,959 | 28,056 | 50,015 | 50,015 | matches |
| `KQvK` | theorem class | 144,508 | 223,944 | 368,452 | 18,081 | 28,056 | 46,137 | 46,137 | matches |
| `KBNvK(light bishop)` | light-bishop theorem class, equivalent to Syzygy `KBNvK` after bishop-colour symmetry | 5,437,752 | 6,830,292 | 12,268,044 | 1,359,578 | 1,707,888 | 3,067,466 | 3,067,466 | matches |
| `KRvKB(light bishop)` | light-bishop theorem class, equivalent to Syzygy `KRvKB` after bishop-colour symmetry | 5,390,364 | 5,916,232 | 11,306,596 | 1,347,906 | 1,479,198 | 2,827,104 | 2,827,104 | matches |
| `KRvKN` | theorem class | 10,780,728 | 12,535,256 | 23,315,984 | 1,347,906 | 1,567,222 | 2,915,128 | 2,915,128 | matches |
| `KBBvK`, opposite bishops | theorem subset only | 2,504,128 | 3,469,344 | 5,973,472 | 626,032 | 867,336 | 1,493,368 | n/a | Syzygy does not split by bishop colours |
| `KBBvK`, all ordered bishop slots | expanded Syzygy material table | 10,164,056 | 13,660,584 | 23,824,640 | 1,270,542 | 1,707,888 | 2,978,430 | 2,978,430 | matches |

The `KBBvK` line needs special care. The theorem is only about opposite-coloured bishops. The Syzygy material key `KBBvK` does not expose a separate statistic for opposite-coloured bishops; it counts the whole two-bishop material table. When the independent counter is expanded to the same all-bishop table, using two bishop slots over all bishop-square colours, it matches Syzygy exactly. The opposite-coloured theorem subset is listed separately because that is the class proved here.

As a note our further analysis cannot be derived from the Syzygy tables because Syzygy WDL is adversarial tablebase value rather than cooperative reachability.

The regression test `TestSyzygyCountCrossCheck` independently recomputes all counts in this section without calling the main reachability analyzers.

Count agreement alone would still leave a theoretical worry: two different position sets can have the same size. For that reason the repository also contains an optional pointwise Syzygy probe:

```sh
python scripts/verify_syzygy_position_sets.py

```

The script generates one representative per relevant board-symmetry class and probes each representative against local Syzygy files with python-chess. It does not use the main reachability analyzers. On 30 May 2026, it was run against the local Syzygy files in with these results:

| Material class | Unique representatives probed |
| --- | --- |
| `KRvK` | 50,015 |
| `KQvK` | 46,137 |
| `KBNvK(light bishop)` | 3,067,466 |
| `KRvKB(light bishop)` | 2,827,104 |
| `KRvKN` | 2,915,128 |
| `KBBvK`, all ordered bishop slots | 2,978,430 |

Every generated representative probed successfully. Together with the matching Syzygy/NULP unique counts, this gives a pointwise cross-check that the generated potentially legal positions are sound and complete. The opposite-coloured `KBBvK` theorem class is contained in the expanded all-bishop Syzygy probe.

## Status

Implemented:

*   finite-state theorem analyzers;
    
*   independent witness verifiers;
    
*   JUnit tests pinning all current counts;
    
*   Syzygy/NULP count cross-checks for the covered material classes;
    
*   optional pointwise Syzygy position-set probe script;
    
*   last-move illegality classification for the local white-to-move exception representatives;
    
*   exposition draft in [docs/basic-checkmate-helpmate-exposition.md](docs/basic-checkmate-helpmate-exposition.md).
    

Not yet implemented:

*   machine-checkable illegality certificates for the local exception representatives;
    
*   machine-readable exported result tables and representative FEN sets.
    

## Dependency

This project uses the Maven Central release of Ashlar Chess:

```xml
<dependency>
  <groupId>io.github.dlbbld</groupId>
  <artifactId>ashlar-chess</artifactId>
  <version>17.0.0</version>
</dependency>

```
