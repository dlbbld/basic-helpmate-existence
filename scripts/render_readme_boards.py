"""Render the chessboard diagrams used in README.md.

The diagrams are generated with python-chess. The built-in piece images are
the Colin M. L. Burnett SVG pieces, which python-chess documents as
triple-licensed under GFDL, BSD, and GPL.
"""

from __future__ import annotations

from pathlib import Path

import chess
import chess.svg


ROOT = Path(__file__).resolve().parents[1]
OUTPUT_DIR = ROOT / "assets" / "boards"

DIAGRAMS = [
    (
        "kbbvk-opposite-1.svg",
        "8/8/8/8/8/B7/B7/k1K5 w - - 0 1",
    ),
    (
        "kbbvk-opposite-2.svg",
        "8/8/8/8/8/B7/B1K5/k7 w - - 0 1",
    ),
    (
        "kbbvk-opposite-3.svg",
        "8/8/8/8/8/8/B1K5/k1B5 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-1.svg",
        "8/8/8/8/8/8/B7/k1KN4 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-2.svg",
        "8/8/8/8/8/3N4/B7/k1K5 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-3.svg",
        "8/8/8/8/2N5/8/B7/k1K5 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-4.svg",
        "8/8/8/8/N7/8/B7/k1K5 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-5.svg",
        "8/8/8/8/8/8/BN6/k1K5 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-6.svg",
        "8/8/8/8/8/8/B1K5/k2N4 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-7.svg",
        "8/8/8/8/8/3N4/B1K5/k7 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-8.svg",
        "8/8/8/8/2N5/8/B1K5/k7 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-9.svg",
        "8/8/8/8/N7/8/B1K5/k7 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-10.svg",
        "8/8/8/8/8/8/BNK5/k7 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-11.svg",
        "8/8/8/8/8/8/2K5/kB1N4 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-12.svg",
        "8/8/8/8/8/3N4/2K5/kB6 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-13.svg",
        "8/8/8/8/2N5/8/2K5/kB6 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-14.svg",
        "8/8/8/8/N7/8/2K5/kB6 w - - 0 1",
    ),
    (
        "kbnvk-light-wtm-15.svg",
        "8/8/8/8/8/8/1NK5/kB6 w - - 0 1",
    ),
    (
        "kbnvk-light-btm-1.svg",
        "8/8/8/8/2N5/8/k1K5/1B6 b - - 0 1",
    ),
    (
        "forced-first-capture-1.svg",
        "8/8/8/8/8/8/3R4/K1k5 b - - 0 1",
    ),
    (
        "forced-first-capture-2.svg",
        "8/8/8/8/8/8/8/K1kQ4 b - - 0 1",
    ),
    (
        "forced-first-capture-3.svg",
        "8/8/8/8/8/8/8/K1kBB3 b - - 0 1",
    ),
    (
        "forced-first-capture-4.svg",
        "8/8/8/8/8/8/3B4/K1kB4 b - - 0 1",
    ),
    (
        "forced-first-capture-5.svg",
        "8/8/8/8/8/8/8/KNkB4 b - - 0 1",
    ),
    (
        "forced-first-capture-6.svg",
        "8/8/8/8/8/8/3N4/K1kB4 b - - 0 1",
    ),
    (
        "forced-first-capture-7.svg",
        "8/8/8/8/8/1b6/k7/R1K5 b - - 0 1",
    ),
    (
        "forced-first-capture-8.svg",
        "8/8/8/8/8/8/2b5/K1kR4 b - - 0 1",
    ),
    (
        "forced-first-capture-9.svg",
        "8/8/8/8/8/8/7n/K5Rk b - - 0 1",
    ),
    (
        "forced-first-capture-10.svg",
        "8/8/8/8/8/2n5/R7/k1K5 b - - 0 1",
    ),
    (
        "forced-first-capture-11.svg",
        "8/8/8/8/8/4N3/3N4/K1k5 b - - 0 1",
    ),
    (
        "forced-first-capture-12.svg",
        "8/8/8/8/8/N7/k7/N1K5 b - - 0 1",
    ),
]


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    for filename, fen in DIAGRAMS:
        board = chess.Board(fen)
        svg = chess.svg.board(
            board=board,
            orientation=board.turn,
            coordinates=True,
            size=420,
            borders=True,
        )
        (OUTPUT_DIR / filename).write_text(svg, encoding="utf-8")


if __name__ == "__main__":
    main()
