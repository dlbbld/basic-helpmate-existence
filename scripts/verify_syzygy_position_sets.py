"""Pointwise Syzygy cross-check for generated local endgame positions.

The Maven tests compare counts. This optional script goes one step further:
it generates one representative per relevant board-symmetry class and probes
each representative against local Syzygy files with python-chess.
"""

from __future__ import annotations

import argparse
import os
import shutil
import tempfile
from contextlib import contextmanager
from collections.abc import Callable, Iterable, Iterator, Sequence
from dataclasses import dataclass
from pathlib import Path

import chess
import chess.syzygy


FULL_BOARD_SYMMETRIES = (0, 1, 2, 3, 4, 5, 6, 7)
BISHOP_COLOUR_PRESERVING_SYMMETRIES = (0, 3, 4, 7)


@dataclass(frozen=True)
class Material:
    name: str
    table: str
    expected_unique: int
    generator: Callable[[], Iterator[tuple[tuple["PieceOnSquare", ...], bool]]]
    symmetries: Sequence[int]


@dataclass(frozen=True, order=True)
class PieceOnSquare:
    symbol: str
    square: int


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--tablebase",
        default=r"C:\Users\danie\git\python-chess\data\syzygy\regular",
        help="directory containing Syzygy .rtbw/.rtbz files",
    )
    parser.add_argument(
        "--material",
        action="append",
        choices=[material.name for material in materials()],
        help="material class to verify; may be repeated; defaults to all",
    )
    parser.add_argument(
        "--isolate-table",
        action="store_true",
        help="probe each material through a temporary directory containing only its .rtbw/.rtbz files",
    )
    parser.add_argument("--progress", type=int, default=250_000, help="print progress every N probed representatives")
    args = parser.parse_args()

    tablebase_path = Path(args.tablebase)
    selected = set(args.material or [material.name for material in materials()])

    for material in materials():
        if material.name not in selected:
            continue
        if args.isolate_table:
            with isolated_table_directory(tablebase_path, material.table) as isolated_path:
                with chess.syzygy.open_tablebase(isolated_path) as tablebase:
                    verify_material(tablebase, material, args.progress)
        else:
            with chess.syzygy.open_tablebase(tablebase_path) as tablebase:
                verify_material(tablebase, material, args.progress)


@contextmanager
def isolated_table_directory(source_directory: Path, table: str) -> Iterator[Path]:
    with tempfile.TemporaryDirectory(prefix=f"syzygy-{table}-") as temporary_directory:
        target_directory = Path(temporary_directory)
        for suffix in ("rtbw", "rtbz"):
            source = source_directory / f"{table}.{suffix}"
            if not source.exists():
                raise FileNotFoundError(f"missing Syzygy table file: {source}")
            target = target_directory / source.name
            try:
                os.link(source, target)
            except OSError:
                shutil.copy2(source, target)
        yield target_directory


def verify_material(tablebase: chess.syzygy.Tablebase, material: Material, progress: int) -> None:
    seen: set[tuple[tuple[PieceOnSquare, ...], bool]] = set()
    probed = 0
    for pieces, white_to_move in material.generator():
        representative = canonical(pieces, white_to_move, material.symmetries)
        if representative in seen:
            continue
        seen.add(representative)
        board = to_board(*representative)
        actual_table = chess.syzygy.calc_key(board)
        if actual_table != material.table:
            raise AssertionError(f"{material.name}: generated {actual_table}, expected {material.table}")
        tablebase.probe_wdl_table(board)
        probed += 1
        if progress and probed % progress == 0:
            print(f"{material.name}: probed {probed:,} / {material.expected_unique:,}")

    if probed != material.expected_unique:
        raise AssertionError(f"{material.name}: probed {probed:,}, expected {material.expected_unique:,}")
    print(f"{material.name}: probed {probed:,} unique representatives")


def materials() -> list[Material]:
    return [
        Material("KRvK", "KRvK", 50_015, generate_krvk, FULL_BOARD_SYMMETRIES),
        Material("KQvK", "KQvK", 46_137, generate_kqvk, FULL_BOARD_SYMMETRIES),
        Material(
            "KBNvK(light bishop)",
            "KBNvK",
            3_067_466,
            generate_kbnvk_light,
            BISHOP_COLOUR_PRESERVING_SYMMETRIES,
        ),
        Material(
            "KRvKB(light bishop)",
            "KRvKB",
            2_827_104,
            generate_krvkb_light,
            BISHOP_COLOUR_PRESERVING_SYMMETRIES,
        ),
        Material("KRvKN", "KRvKN", 2_915_128, generate_krvkn, FULL_BOARD_SYMMETRIES),
        Material(
            "KBBvK(opposite bishops)",
            "KBBvK",
            1_493_368,
            generate_kbbvk_opposite,
            BISHOP_COLOUR_PRESERVING_SYMMETRIES,
        ),
        Material("KBBvK(all bishop slots)", "KBBvK", 2_978_430, generate_kbbvk_all_ordered, FULL_BOARD_SYMMETRIES),
    ]


def generate_krvk() -> Iterator[tuple[tuple[PieceOnSquare, ...], bool]]:
    yield from generate_major_piece("R", rook_attacks)


def generate_kqvk() -> Iterator[tuple[tuple[PieceOnSquare, ...], bool]]:
    yield from generate_major_piece(
        "Q",
        lambda queen, target, blockers: rook_attacks(queen, target, blockers)
        or bishop_attacks(queen, target, blockers),
    )


def generate_major_piece(
    symbol: str, attacks: Callable[[int, int, tuple[int, ...]], bool]
) -> Iterator[tuple[tuple[PieceOnSquare, ...], bool]]:
    for white_king in range(64):
        for white_piece in range(64):
            if white_piece == white_king:
                continue
            for black_king in range(64):
                if black_king in (white_king, white_piece) or kings_touch(white_king, black_king):
                    continue
                pieces = (
                    PieceOnSquare("K", white_king),
                    PieceOnSquare(symbol, white_piece),
                    PieceOnSquare("k", black_king),
                )
                if not attacks(white_piece, black_king, (white_king,)):
                    yield pieces, True
                yield pieces, False


def generate_kbnvk_light() -> Iterator[tuple[tuple[PieceOnSquare, ...], bool]]:
    light_squares = [square for square in range(64) if is_light_square(square)]
    for white_king in range(64):
        for white_bishop in light_squares:
            if white_bishop == white_king:
                continue
            for white_knight in range(64):
                if white_knight in (white_king, white_bishop):
                    continue
                for black_king in range(64):
                    if black_king in (white_king, white_bishop, white_knight):
                        continue
                    black_in_check = (
                        kings_touch(white_king, black_king)
                        or bishop_attacks(white_bishop, black_king, (white_king, white_knight))
                        or knight_attacks(white_knight, black_king)
                    )
                    pieces = (
                        PieceOnSquare("K", white_king),
                        PieceOnSquare("B", white_bishop),
                        PieceOnSquare("N", white_knight),
                        PieceOnSquare("k", black_king),
                    )
                    if not black_in_check:
                        yield pieces, True
                    if not kings_touch(white_king, black_king):
                        yield pieces, False


def generate_krvkb_light() -> Iterator[tuple[tuple[PieceOnSquare, ...], bool]]:
    light_squares = [square for square in range(64) if is_light_square(square)]
    for white_king in range(64):
        for white_rook in range(64):
            if white_rook == white_king:
                continue
            for black_king in range(64):
                if black_king in (white_king, white_rook):
                    continue
                for black_bishop in light_squares:
                    if black_bishop in (white_king, white_rook, black_king):
                        continue
                    black_in_check = kings_touch(white_king, black_king) or rook_attacks(
                        white_rook, black_king, (white_king, black_bishop)
                    )
                    white_in_check = kings_touch(white_king, black_king) or bishop_attacks(
                        black_bishop, white_king, (black_king, white_rook)
                    )
                    pieces = (
                        PieceOnSquare("K", white_king),
                        PieceOnSquare("R", white_rook),
                        PieceOnSquare("k", black_king),
                        PieceOnSquare("b", black_bishop),
                    )
                    if not black_in_check:
                        yield pieces, True
                    if not white_in_check:
                        yield pieces, False


def generate_krvkn() -> Iterator[tuple[tuple[PieceOnSquare, ...], bool]]:
    for white_king in range(64):
        for white_rook in range(64):
            if white_rook == white_king:
                continue
            for black_king in range(64):
                if black_king in (white_king, white_rook):
                    continue
                for black_knight in range(64):
                    if black_knight in (white_king, white_rook, black_king):
                        continue
                    black_in_check = kings_touch(white_king, black_king) or rook_attacks(
                        white_rook, black_king, (white_king, black_knight)
                    )
                    white_in_check = kings_touch(white_king, black_king) or knight_attacks(black_knight, white_king)
                    pieces = (
                        PieceOnSquare("K", white_king),
                        PieceOnSquare("R", white_rook),
                        PieceOnSquare("k", black_king),
                        PieceOnSquare("n", black_knight),
                    )
                    if not black_in_check:
                        yield pieces, True
                    if not white_in_check:
                        yield pieces, False


def generate_kbbvk_opposite() -> Iterator[tuple[tuple[PieceOnSquare, ...], bool]]:
    light_squares = [square for square in range(64) if is_light_square(square)]
    dark_squares = [square for square in range(64) if not is_light_square(square)]
    for white_king in range(64):
        for white_light_bishop in light_squares:
            if white_light_bishop == white_king:
                continue
            for white_dark_bishop in dark_squares:
                if white_dark_bishop in (white_king, white_light_bishop):
                    continue
                for black_king in range(64):
                    if black_king in (white_king, white_light_bishop, white_dark_bishop):
                        continue
                    black_in_check = (
                        kings_touch(white_king, black_king)
                        or bishop_attacks(white_light_bishop, black_king, (white_king, white_dark_bishop))
                        or bishop_attacks(white_dark_bishop, black_king, (white_king, white_light_bishop))
                    )
                    pieces = (
                        PieceOnSquare("K", white_king),
                        PieceOnSquare("B", white_light_bishop),
                        PieceOnSquare("B", white_dark_bishop),
                        PieceOnSquare("k", black_king),
                    )
                    if not black_in_check:
                        yield pieces, True
                    if not kings_touch(white_king, black_king):
                        yield pieces, False


def generate_kbbvk_all_ordered() -> Iterator[tuple[tuple[PieceOnSquare, ...], bool]]:
    for white_king in range(64):
        for white_bishop1 in range(64):
            if white_bishop1 == white_king:
                continue
            for white_bishop2 in range(64):
                if white_bishop2 in (white_king, white_bishop1):
                    continue
                for black_king in range(64):
                    if black_king in (white_king, white_bishop1, white_bishop2):
                        continue
                    black_in_check = (
                        kings_touch(white_king, black_king)
                        or bishop_attacks(white_bishop1, black_king, (white_king, white_bishop2))
                        or bishop_attacks(white_bishop2, black_king, (white_king, white_bishop1))
                    )
                    pieces = (
                        PieceOnSquare("K", white_king),
                        PieceOnSquare("B", white_bishop1),
                        PieceOnSquare("B", white_bishop2),
                        PieceOnSquare("k", black_king),
                    )
                    if not black_in_check:
                        yield pieces, True
                    if not kings_touch(white_king, black_king):
                        yield pieces, False


def to_board(pieces: tuple[PieceOnSquare, ...], white_to_move: bool) -> chess.Board:
    board = chess.Board.empty()
    board.clear_stack()
    board.turn = chess.WHITE if white_to_move else chess.BLACK
    board.castling_rights = chess.BB_EMPTY
    board.ep_square = None

    for piece in pieces:
        board.set_piece_at(piece.square, chess.Piece.from_symbol(piece.symbol))
    return board


def canonical(
    pieces: tuple[PieceOnSquare, ...], white_to_move: bool, transform_indexes: Sequence[int]
) -> tuple[tuple[PieceOnSquare, ...], bool]:
    return min(
        (tuple(PieceOnSquare(piece.symbol, transform(piece.square, index)) for piece in pieces), white_to_move)
        for index in transform_indexes
    )


def transform(square: int, transform_index: int) -> int:
    file = chess.square_file(square)
    rank = chess.square_rank(square)
    match transform_index:
        case 0:
            return chess.square(file, rank)
        case 1:
            return chess.square(7 - file, rank)
        case 2:
            return chess.square(file, 7 - rank)
        case 3:
            return chess.square(7 - file, 7 - rank)
        case 4:
            return chess.square(rank, file)
        case 5:
            return chess.square(7 - rank, file)
        case 6:
            return chess.square(rank, 7 - file)
        case 7:
            return chess.square(7 - rank, 7 - file)
        case _:
            raise ValueError(transform_index)


def kings_touch(a: int, b: int) -> bool:
    return max(abs(chess.square_file(a) - chess.square_file(b)), abs(chess.square_rank(a) - chess.square_rank(b))) == 1


def rook_attacks(piece: int, target: int, blockers: tuple[int, ...]) -> bool:
    return line_attacks(piece, target, blockers, ((1, 0), (-1, 0), (0, 1), (0, -1)))


def bishop_attacks(piece: int, target: int, blockers: tuple[int, ...]) -> bool:
    return line_attacks(piece, target, blockers, ((1, 1), (1, -1), (-1, 1), (-1, -1)))


def line_attacks(piece: int, target: int, blockers: tuple[int, ...], deltas: Iterable[tuple[int, int]]) -> bool:
    blocker_set = set(blockers)
    for file_delta, rank_delta in deltas:
        file = chess.square_file(piece) + file_delta
        rank = chess.square_rank(piece) + rank_delta
        while 0 <= file < 8 and 0 <= rank < 8:
            square = chess.square(file, rank)
            if square == target:
                return True
            if square in blocker_set:
                break
            file += file_delta
            rank += rank_delta
    return False


def knight_attacks(knight: int, target: int) -> bool:
    return abs(chess.square_file(knight) - chess.square_file(target)) * abs(
        chess.square_rank(knight) - chess.square_rank(target)
    ) == 2


def is_light_square(square: int) -> bool:
    return (chess.square_file(square) + chess.square_rank(square)) % 2 == 1


if __name__ == "__main__":
    main()
