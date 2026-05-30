"""Pointwise Syzygy cross-check for generated local endgame positions.

The Maven tests compare counts. This optional script goes one step further:
it generates one representative per relevant board-symmetry class and probes
each representative against local Syzygy files with python-chess.
"""

from __future__ import annotations

import argparse
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
    generator: Callable[[], Iterator[tuple[tuple[int, ...], bool]]]
    symmetries: Sequence[int]


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
    parser.add_argument("--progress", type=int, default=250_000, help="print progress every N probed representatives")
    args = parser.parse_args()

    tablebase_path = Path(args.tablebase)
    selected = set(args.material or [material.name for material in materials()])

    with chess.syzygy.open_tablebase(tablebase_path) as tablebase:
        for material in materials():
            if material.name not in selected:
                continue
            verify_material(tablebase, material, args.progress)


def verify_material(tablebase: chess.syzygy.Tablebase, material: Material, progress: int) -> None:
    seen: set[tuple[tuple[int, ...], bool]] = set()
    probed = 0
    for pieces, white_to_move in material.generator():
        representative = canonical(pieces, white_to_move, material.symmetries)
        if representative in seen:
            continue
        seen.add(representative)
        board = to_board(material.table, *representative)
        tablebase.probe_wdl(board)
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
        Material("KBBvK(all bishop slots)", "KBBvK", 2_978_430, generate_kbbvk_all_ordered, FULL_BOARD_SYMMETRIES),
    ]


def generate_krvk() -> Iterator[tuple[tuple[int, ...], bool]]:
    yield from generate_major_piece(rook_attacks)


def generate_kqvk() -> Iterator[tuple[tuple[int, ...], bool]]:
    yield from generate_major_piece(lambda queen, target, blockers: rook_attacks(queen, target, blockers)
                                    or bishop_attacks(queen, target, blockers))


def generate_major_piece(attacks: Callable[[int, int, tuple[int, ...]], bool]) -> Iterator[tuple[tuple[int, ...], bool]]:
    for white_king in range(64):
        for white_piece in range(64):
            if white_piece == white_king:
                continue
            for black_king in range(64):
                if black_king in (white_king, white_piece) or kings_touch(white_king, black_king):
                    continue
                if not attacks(white_piece, black_king, (white_king,)):
                    yield (white_king, white_piece, black_king), True
                yield (white_king, white_piece, black_king), False


def generate_kbnvk_light() -> Iterator[tuple[tuple[int, ...], bool]]:
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
                    if not black_in_check:
                        yield (white_king, white_bishop, white_knight, black_king), True
                    if not kings_touch(white_king, black_king):
                        yield (white_king, white_bishop, white_knight, black_king), False


def generate_krvkb_light() -> Iterator[tuple[tuple[int, ...], bool]]:
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
                    if not black_in_check:
                        yield (white_king, white_rook, black_king, black_bishop), True
                    if not white_in_check:
                        yield (white_king, white_rook, black_king, black_bishop), False


def generate_krvkn() -> Iterator[tuple[tuple[int, ...], bool]]:
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
                    if not black_in_check:
                        yield (white_king, white_rook, black_king, black_knight), True
                    if not white_in_check:
                        yield (white_king, white_rook, black_king, black_knight), False


def generate_kbbvk_all_ordered() -> Iterator[tuple[tuple[int, ...], bool]]:
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
                    if not black_in_check:
                        yield (white_king, white_bishop1, white_bishop2, black_king), True
                    if not kings_touch(white_king, black_king):
                        yield (white_king, white_bishop1, white_bishop2, black_king), False


def to_board(table: str, pieces: tuple[int, ...], white_to_move: bool) -> chess.Board:
    board = chess.Board.empty()
    board.clear_stack()
    board.turn = chess.WHITE if white_to_move else chess.BLACK
    board.castling_rights = chess.BB_EMPTY
    board.ep_square = None

    if table == "KRvK":
        set_pieces(board, pieces, "KRk")
    elif table == "KQvK":
        set_pieces(board, pieces, "KQk")
    elif table == "KBNvK":
        set_pieces(board, pieces, "KBNk")
    elif table == "KRvKB":
        set_pieces(board, pieces, "KRkb")
    elif table == "KRvKN":
        set_pieces(board, pieces, "KRkn")
    elif table == "KBBvK":
        set_pieces(board, pieces, "KBBk")
    else:
        raise ValueError(table)
    return board


def set_pieces(board: chess.Board, pieces: tuple[int, ...], symbols: str) -> None:
    for square, symbol in zip(pieces, symbols):
        board.set_piece_at(square, chess.Piece.from_symbol(symbol))


def canonical(pieces: tuple[int, ...], white_to_move: bool, transform_indexes: Sequence[int]) -> tuple[tuple[int, ...], bool]:
    return min((tuple(transform(square, index) for square in pieces), white_to_move) for index in transform_indexes)


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
