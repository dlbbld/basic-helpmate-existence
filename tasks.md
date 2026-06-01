# Remaining tasks

Run the remaining isolated Syzygy pointwise probes:

```sh
python -u scripts\verify_syzygy_position_sets.py --isolate-table --progress 500000 --material "KBNvK(light bishop)"
python -u scripts\verify_syzygy_position_sets.py --isolate-table --progress 500000 --material "KRvKB(light bishop)"
python -u scripts\verify_syzygy_position_sets.py --isolate-table --progress 500000 --material KRvKN
```

Already completed in isolated mode:

- `KRvK`: 50,015 representatives
- `KQvK`: 46,137 representatives
- `KBBvK(opposite bishops)`: 1,493,368 representatives
